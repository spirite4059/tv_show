package com.vego.player;

import android.os.AsyncTask;
import android.text.TextUtils;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

/**
 * Created by fq_mbp on 15/8/18.
 */
public class DailymotionUtils {

    private final String URL = "http://www.dailymotion.com/embed/video/";

    private final String USER_AGENT = "Mozilla/5.0 (iPad; CPU OS 7_0 like Mac OS X) AppleWebKit/537.51.1 (KHTML, like Gecko) Version/7.0 Mobile/11A465 Safari/9537.53";

    private QueryResetTask queryResetTask;
    private QueryDMTask queryDMTask;
    private String dmVid;

    private String calculateDMUrl(String dmVid) {
        HttpParams httpParameters = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParameters, 30 * 1000);// 设置请求超时10秒
        HttpConnectionParams.setSoTimeout(httpParameters, 30 * 1000); // 设置等待数据超时10秒
        HttpClient lClient = new DefaultHttpClient(httpParameters);// 此时构造DefaultHttpClient时将参数传入
        this.dmVid = dmVid;
        String url = URL + dmVid;
        LogCat.e("dm", "youtube*****url : " + url);
        try {
            HttpGet lGetMethod = new HttpGet(url);
            lGetMethod.setHeader("User-Agent", USER_AGENT);
            HttpResponse lResp = lClient.execute(lGetMethod);
            ByteArrayOutputStream lBOS = new ByteArrayOutputStream();

            lResp.getEntity().writeTo(lBOS);
            String lInfoStr = new String(lBOS.toString("UTF-8"));
            return lInfoStr;

        } catch (IOException e) {
            e.printStackTrace();
            setErrorMsg("dm解析过程中出现 IOException " + e.getMessage() + "-> calculateDMUrl()");
        }
        return null;
    }

    private class QueryDMTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            if (params != null) {
                return calculateDMUrl(params[0]);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (TextUtils.isEmpty(s)) {
                setErrorMsg("dm解析  onPostExecute result == null 地址解析失败，无任何反馈信息。。");
                LogCat.e("地址解析失败，无任何反馈信息。。");
                return;
            }

            if (s.contains("window.playerV5 =")) {

                LogCat.e("找到包含地址的信息，需要对该位置进行解析");
                s = s.substring(s.indexOf("document.getElementById('player'),"));
                s = s.replace("document.getElementById('player'),", "");
                s = s.substring(0, s.indexOf(");"));

                try {
                    // 解析包含地址的信息
                    JSONObject jsonObject = new JSONObject(s);
                    if (jsonObject != null && jsonObject.has("metadata")) {
                        JSONObject metadata = jsonObject.getJSONObject("metadata");
                        if (metadata != null && metadata.has("qualities")) {
                            JSONObject qualities = metadata.getJSONObject("qualities");
                            LogCat.e("解析到的地址是： " + qualities.toString());
                            if (qualities != null) {
                                // 开始解析视频相关信息，讲信息保存到集合中
                                ArrayList<DMQualityInfo> dmQualityInfos = new ArrayList<DMQualityInfo>();
                                Iterator<String> iterator = qualities.keys();
                                while (iterator.hasNext()) {
                                    String key = iterator.next();
                                    if (TextUtils.isEmpty(key)) {
                                        continue;
                                    }
                                    // auto的视频信息不采集
                                    if ("auto".equals(key)) {
                                        continue;
                                    }

                                    DMQualityInfo dmQualityInfo = new DMQualityInfo();
                                    dmQualityInfo.quality = key;

                                    Object value = qualities.get(key.toString());
                                    if (value instanceof JSONArray) {
                                        ArrayList<DMVideoInfo> list = new ArrayList<DMVideoInfo>();
                                        JSONArray array = (JSONArray) value;
                                        for (int i = 0; i < array.length(); i++) {
                                            if (array.get(i) instanceof JSONObject) {
                                                JSONObject json2 = (JSONObject) array.get(i);
                                                DMVideoInfo dmVideoInfo = new DMVideoInfo();
                                                dmVideoInfo.type = json2.getString("type");
                                                dmVideoInfo.url = json2.getString("url");
                                                list.add(dmVideoInfo);
                                            }
                                        }
                                        dmQualityInfo.dmVideoInfos = list;
                                    }

                                    dmQualityInfos.add(dmQualityInfo);
                                }
                                // 按质量来个从小到大排序
                                Collections.sort(dmQualityInfos);
                                if (queryResetTask != null) {
                                    queryResetTask.cancel(true);
                                    queryResetTask = null;
                                }
                                queryResetTask = new QueryResetTask();

                                queryResetTask.execute(dmQualityInfos.get(0).dmVideoInfos.get(0).url);

								for (DMQualityInfo qualityInfo : dmQualityInfos) {
									LogCat.e("qualityInfo.quality: " + qualityInfo.quality);
								}


                            }

                        } else {
                            setErrorMsg("dm解析 不包含 qualities字段信息");
                        }

                    } else {
                        setErrorMsg("dm解析  不包含metadata字段信息");

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    setErrorMsg("dm解析  JSONException " + e.getMessage() + "   -> onPostExecute()");
                }
            }

        }
    }

    public class DMQualityInfo implements Comparable<DMQualityInfo> {
        public String quality;
        public ArrayList<DMVideoInfo> dmVideoInfos;

        @Override
        public int compareTo(DMQualityInfo another) {
            if (another != null && !TextUtils.isEmpty(another.quality)) {
                float anotherQuality = Float.parseFloat(another.quality);
                float quality = Float.parseFloat(this.quality);
                if (quality > anotherQuality) {
                    return -1;
                }
            }
            return 1;
        }
    }

    public class DMVideoInfo {
        public String type;
        public String url;
    }



    private class QueryResetTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            return getResponseLocation(params[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            LogCat.e("最终url ： " + result);
            if (OnSuccessListener != null) {
                OnSuccessListener.onSuccess(result);
            }

//            if (onFailListener != null) {
//                onFailListener.onFail("test");
//            }
        }

    }

    public void cancleTask() {
        if (queryDMTask != null) {
            queryDMTask.cancel(true);
            queryDMTask = null;
        }

        if (queryResetTask != null) {
            queryResetTask.cancel(true);
            queryResetTask = null;
        }
    }

    private String getResponseLocation(String requestUrl) {
        String location = null;
        try {
            URL url = new URL(requestUrl);
            HttpURLConnection mConnection = (HttpURLConnection) url.openConnection();
            mConnection.setInstanceFollowRedirects(false);
            mConnection.setRequestMethod("GET");
            mConnection.setConnectTimeout(5 * 1000);
            mConnection.connect();
            location = mConnection.getHeaderField("location");
        } catch (MalformedURLException e) {
            e.printStackTrace();
            setErrorMsg("dm解析 MalformedURLException " + e.getMessage() + " -> " + "getResponseLocation() ");
        } catch (IOException e) {
            e.printStackTrace();
            setErrorMsg("dm解析 IOException " + e.getMessage() + " -> " + "getResponseLocation() ");
        }
        return location;
    }

    public void getDMVideoUrl(String dmVid) {
        if (TextUtils.isEmpty(dmVid)) {
            setErrorMsg("dmId == null, 无法继续解析 -> getDMVideoUrl()");
            return;
        }

        cancleTask();
        queryDMTask = new QueryDMTask();
        queryDMTask.execute(dmVid);
    }

    private void setErrorMsg(String msg) {
        if (onFailListener != null) {
            onFailListener.onFail(dmVid, msg);
        }
    }


    private OnSuccessListener OnSuccessListener;

    public interface OnSuccessListener {
        void onSuccess(String url);
    }

    private OnFailListener onFailListener;

    public interface OnFailListener {
        void onFail(String dmId, String errorMsg);
    }

    public void setOnSuccessListener(DailymotionUtils.OnSuccessListener onSuccessListener) {
        OnSuccessListener = onSuccessListener;
    }

    public void setOnFailListener(OnFailListener onFailListener) {
        this.onFailListener = onFailListener;
    }

}
