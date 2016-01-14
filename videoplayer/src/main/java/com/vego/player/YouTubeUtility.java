package com.vego.player;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeoutException;

public class YouTubeUtility {

    private static String el = "embedded";
    private static String el1 = "detailpage";
    private static int retryTimes = 0;
    private QueryYouTubeTask queryYouTubeTask;
    /**
     * youtube视频单个视频
     */
    public static final String TYPE_YOUTUBE_VIDEO_SINGLE = "ytv://";
    /**
     * youtube视频列表
     */
    public static final String TYPE_YOUTUBE_VIDEO_LIST = "ytpl://";
    //	13, 17, 18, 22, 37
    public final String VIDEO_QUALITY = "18";

    static final String YOUTUBE_VIDEO_INFORMATION_URL = "http://www.youtube.com/get_video_info?&video_id=";


    /**
     * Calculate the YouTube URL to load the video. Includes retrieving a token
     * that YouTube requires to play the video.
     *
     * @param pYouTubeFmtQuality quality of the video. 17=low, 18=high
     *                           whether to fallback to lower quality in case the supplied
     *                           quality is not available
     * @param pYouTubeVideoId    the id of the video
     * @return the url string that will retrieve the video
     * @throws IOException
     * @throws ClientProtocolException
     * @throws UnsupportedEncodingException
     */
    @SuppressWarnings("deprecation")
    private String calculateYouTubeUrl(String pYouTubeFmtQuality, boolean pFallback, String pYouTubeVideoId)
            throws TimeoutException, IOException {
        retryTimes = 0;
        String lUriStr = null;
        HttpParams httpParameters = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParameters, 30 * 1000);// 设置请求超时10秒
        HttpConnectionParams.setSoTimeout(httpParameters, 30 * 1000); // 设置等待数据超时10秒

        // HttpProtocolParams
        // .setUserAgent(httpParameters,
        // "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2272.101 Safari/537.36");

        HttpClient lClient = new DefaultHttpClient(httpParameters);// 此时构造DefaultHttpClient时将参数传入

        String url = YOUTUBE_VIDEO_INFORMATION_URL + pYouTubeVideoId + "&el=" + el
                + "&hl=en&ps=default";

        Log.e("youtube", "youtube*****url : " + url);

        HttpGet lGetMethod = new HttpGet(url);
        // http://www.youtube.com/get_video_info?&video_id=
        HttpResponse lResp = null;
        lResp = lClient.execute(lGetMethod);

        ByteArrayOutputStream lBOS = new ByteArrayOutputStream();
        String lInfoStr = null;

        lResp.getEntity().writeTo(lBOS);
        lInfoStr = new String(lBOS.toString("UTF-8"));

        String[] lArgs = lInfoStr.split("&");
        Map<String, String> lArgMap = new HashMap<String, String>();
        for (int i = 0; i < lArgs.length; i++) {
            String[] lArgValStrArr = lArgs[i].split("=");
            if (lArgValStrArr != null) {
                if (lArgValStrArr.length >= 2) {
                    lArgMap.put(lArgValStrArr[0], URLDecoder.decode(lArgValStrArr[1]));
                }
            }
        }

        // Find out the URI string from the parameters

        // Populate the list of formats for the video
        if (lArgMap == null || TextUtils.isEmpty(lArgMap.get("fmt_list"))) {
            // Log.e("youtube_error", "reason: " + lArgMap.get("reason"));
            // 没有播放权限
            // if (lArgMap != null &&
            // !TextUtils.isEmpty(lArgMap.get("errorcode"))
            // && "150".equals(lArgMap.get("errorcode"))) {
            // return getYoutubeDeatailPage(3, pFallback, pYouTubeVideoId);
            // }
            //
            // if (!TextUtils.isEmpty(lArgMap.get("reason"))) {
            // throw new SocketTimeoutException("没有版权，无法在站外播放此视频");
            // }
            int[] youtubeQuantity = {13, 17, 18, 22, 37};
            int oldQuantity = Integer.parseInt(pYouTubeFmtQuality);
            int level = 5;
            for (int i = 0; i < youtubeQuantity.length; i++) {
                if (oldQuantity == youtubeQuantity[i]) {
                    level = i + 1;
                    break;
                }
            }
            return getYoutubeDeatailPage(level, pFallback, pYouTubeVideoId);
            // return null;
        }
        String lFmtList = URLDecoder.decode(lArgMap.get("fmt_list"));
        ArrayList<Format> lFormats = new ArrayList<Format>();
        if (null != lFmtList) {
            String lFormatStrs[] = lFmtList.split(",");
            for (String lFormatStr : lFormatStrs) {
                Format lFormat = new Format(lFormatStr);
                lFormats.add(lFormat);
            }
        }

        // Populate the list of streams for the video
        String lStreamList = lArgMap.get("url_encoded_fmt_stream_map");
        if (null != lStreamList) {
            String lStreamStrs[] = lStreamList.split(",");
            ArrayList<VideoStream> lStreams = new ArrayList<VideoStream>();
            for (String lStreamStr : lStreamStrs) {
                VideoStream lStream = new VideoStream(lStreamStr);
                lStreams.add(lStream);
            }

            // Search for the given format in the list of video formats
            // if it is there, select the corresponding stream
            // otherwise if fallback is requested, check for next lower format
            int lFormatId = Integer.parseInt(pYouTubeFmtQuality);
            Format lSearchFormat = new Format(lFormatId);
            while (!lFormats.contains(lSearchFormat) && pFallback) {
                int lOldId = lSearchFormat.getId();
                int lNewId = getSupportedFallbackId(lOldId);

                if (lOldId == lNewId) {
                    break;
                }
                lSearchFormat = new Format(lNewId);
            }
            Log.e("youtube", "当前播放youtube的清晰度：" + lSearchFormat.mId);

            int lIndex = lFormats.indexOf(lSearchFormat);
            if (lIndex >= 0) {
                VideoStream lSearchStream = lStreams.get(lIndex);
                lUriStr = lSearchStream.getUrl();
            }
            Log.e("youtube", "当前播放youtube的url：" + Uri.decode(lUriStr));
        } else {
            setYoutubeErrorMessage(0, "播放地址获取失败", "url_encoded_fmt_stream_map is null,无法解析到youtube的播放地址");
            throw new SocketTimeoutException("url_encoded_fmt_stream_map is null,无法解析到youtube的播放地址");
        }
        // Return the URI string. It may be null if the format (or a fallback
        // format if enabled)
        // is not found in the list of formats for the video
        return lUriStr;
    }

    /**
     * itag: 240P 36； 360P 18； 720P 22； 1080P 37；
     */

    private String getYoutubeDeatailPage(int pYouTubeFmtQuality, boolean pFallback, String pYouTubeVideoId) {
        String realUrl = null;
        String url = YOUTUBE_VIDEO_INFORMATION_URL + pYouTubeVideoId + "&el=" + el1
                + "&hl=en&ps=default";

        Log.e("youtube", "youtube*****url : " + url);
        HttpParams httpParameters = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParameters, 30 * 1000);// 设置请求超时10秒
        HttpConnectionParams.setSoTimeout(httpParameters, 30 * 1000); // 设置等待数据超时10秒
        HttpProtocolParams
                .setUserAgent(httpParameters,
                        "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2272.101 Safari/537.36");

        HttpClient lClient = new DefaultHttpClient(httpParameters);// 此时构造DefaultHttpClient时将参数传入

        HttpGet lGetMethod = new HttpGet(url);
        lGetMethod.setHeader("Accept-Language", "en");
        // http://www.youtube.com/get_video_info?&video_id=
        HttpResponse lResp = null;
        try {
            lResp = lClient.execute(lGetMethod);
            ByteArrayOutputStream lBOS = new ByteArrayOutputStream();
            String lInfoStr = null;

            lResp.getEntity().writeTo(lBOS);
            lInfoStr = new String(lBOS.toString("UTF-8"));
            if (!TextUtils.isEmpty(lInfoStr)) {
                // Log.e("youtube", "解码前的字符串 : " + lInfoStr);
                try {
                    lInfoStr = URLDecoder.decode(lInfoStr, "UTF-8");
                    // Log.e("youtube", "解码后的字符串 : " + lInfoStr);
                    String[] lArgsUrl = lInfoStr.split("&url=http");
                    int m = 0;
                    SparseArray<Map<String, String>> urlSparseArray = new SparseArray<Map<String, String>>();
                    for (String curUrl : lArgsUrl) {
                        if (TextUtils.isEmpty(curUrl)) {
                            continue;
                        }
                        String formatUrl = "url=http" + curUrl;
                        // Log.e("youtube", "youtube没有裁剪：" + Uri.decode(formatUrl));
                        String[] urlParams = formatUrl.split("&");
                        Map<String, String> paramsMap = new HashMap<String, String>();
                        for (String urlParam : urlParams) {
                            if (TextUtils.isEmpty(urlParam) || !urlParam.contains("=")) {
                                continue;
                            }
                            String[] urlSplit = urlParam.split("=");
                            if (urlSplit.length >= 2) {
                                String params = urlSplit[1];
                                if (TextUtils.isEmpty(params)) {
                                    continue;
                                }
                                if ("url".equals(urlSplit[0])) {
                                    params = Uri.decode(params);
                                    if (!params.contains("://r")) {
                                        continue;
                                    }

                                    if (params.contains(",")) {
                                        String[] paramsFormat = params.split(",");
                                        for (String param : paramsFormat) {
                                            if (param.contains("://r")) {
                                                params = param;
                                                break;
                                            }
                                        }
                                    }
                                    // Log.e("youtube",
                                    // "==========================================");
                                    // Log.e("youtube", "youtube的真正播放地址：" + params);
                                }

                                paramsMap.put(urlSplit[0], params);

                            }
                        }
                        urlSparseArray.append(m++, paramsMap);
                    }

                    /**
                     * itag: 240P 36; 360P 18; 720P 22; 1080P 37; mp4 720P 137; 480P
                     * 136; 360P 135; 240P 134
                     */
                    // Log.e("youtube_", "要使用的清晰度是：   " + paramsMaps.get("itag"));
                    // Log.e("youtube_", "要使用的url是：   " + realUrl);
                    // realUrl = getTheHighestResolution(realUrl, urlSparseArray);

                    realUrl = getStanderResolution(pYouTubeFmtQuality, pYouTubeVideoId, realUrl, urlSparseArray);

                    if (TextUtils.isEmpty(realUrl) && retryTimes < 5) {
                        // Log.e("youtube", "没有查询到合适的播放地址，第几次尝试" + retryTimes);
                        retryTimes++;
                        realUrl = getYoutubeDeatailPage(pYouTubeFmtQuality, pFallback, pYouTubeVideoId);
                        if (!TextUtils.isEmpty(realUrl)) {
                            retryTimes = 0;
                            return realUrl;
                        }
                    }
                }catch (IllegalArgumentException e){
                    return null;
                }
                // Log.e("youtube", "realUrl： " + realUrl);

            }
            return realUrl;
        } catch (ClientProtocolException e) {
            e.printStackTrace();
            setYoutubeErrorMessage(2, "视频信息获取失败！", "ClientProtocolException youTubeId == null -> getYoutubeDeatailPage " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            setYoutubeErrorMessage(2, "视频信息获取失败！", "IOException youTubeId == null -> getYoutubeDeatailPage 无法继续" + e.getMessage());
        }
        return null;
    }

    public void executeQueryYouTubeTask(String type, String youTubeId) { // R3lSUXl_VQI
        if(TextUtils.isEmpty(youTubeId)){
            setYoutubeErrorMessage(2, "视频信息获取失败！", "youTubeId == null -> executeQueryYouTubeTask 无法继续");
            return;
        }

        if (queryYouTubeTask != null) {
            queryYouTubeTask.cancel(true);
            queryYouTubeTask = null;
        }
        queryYouTubeTask = new QueryYouTubeTask();
        queryYouTubeTask.execute(youTubeId);
    }


    public void cancelQueryYoutubeTask() {
        if (queryYouTubeTask != null) {
            queryYouTubeTask.cancel(true);
            queryYouTubeTask = null;
        }
        retryTimes = 30;
    }


    public class QueryYouTubeTask extends AsyncTask<String, Void, Uri> {

        @Override
        protected Uri doInBackground(String... pParams) {
            Log.e("QueryYouTubeTask", "QueryYouTubeTask//////////////doInBackground");
            String lUriStr = null;
            // 清晰度
            String lYouTubeFmtQuality = VIDEO_QUALITY;
            String lYouTubeVideoId = null;

            if (isCancelled())
                return null;
            try {

                lYouTubeVideoId = pParams[0];

                if (isCancelled())
                    return null;

                // //////////////////////////////////
                // calculate the actual URL of the video, encoded with
                // proper YouTube token
                lUriStr = calculateYouTubeUrl(lYouTubeFmtQuality, true, lYouTubeVideoId);

                if (isCancelled())
                    return null;

                if (!TextUtils.isEmpty(lUriStr) && lUriStr.startsWith("youtube_error")) {
                    // youtubeReason = "该视频由于版权问题，无法正常播放！";
                    // LogCat.e("QueryYouTubeTask//////--------------reason: "
                    // + lUriStr);
                    return null;
                }
                // publishProgress(new ProgressUpdateInfo(mMsgHiBand));

            } catch (SocketTimeoutException e) {
                e.printStackTrace();
                setYoutubeErrorMessage(2, "视频信息获取失败！", "youtube客户端解析 SocketTimeoutException nQueryYouTubeTask -> doInBackground -> et error SocketTimeoutException " + e.getMessage());
            } catch (ClientProtocolException e) {
                e.printStackTrace();
                setYoutubeErrorMessage(2, "视频信息获取失败！", "youtube客户端解析 ClientProtocolException nQueryYouTubeTask -> doInBackground -> ClientProtocolException " + e.getMessage());
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                setYoutubeErrorMessage(2, "视频信息获取失败！", "youtube客户端解析 UnsupportedEncodingException nQueryYouTubeTask -> doInBackground -> UnsupportedEncodingException " + e.getMessage());
            } catch (IOException e) {
                e.printStackTrace();
                setYoutubeErrorMessage(2, "视频信息获取失败！", "youtube客户端解析 nIOException QueryYouTubeTask -> doInBackground -> IOException" + e.getMessage());
            } catch (TimeoutException e) {
                e.printStackTrace();
                setYoutubeErrorMessage(2, "视频信息获取失败！", "youtube客户端解析 TimeoutException nIOException QueryYouTubeTask -> doInBackground -> IOException" + e.getMessage());
            }

            if (lUriStr != null) {
                return Uri.parse(lUriStr);
            } else {
                return null;
            }
        }

        // }

        @Override
        protected void onPostExecute(Uri pResult) {
            super.onPostExecute(pResult);
            try {
                if (isCancelled()) {
                    return;
                }
                if (pResult == null) {
                    setYoutubeErrorMessage(0, "视频地址信息获取失败！", "QueryYouTubeTask onPostExecute -> 地址获取失败，url == null");
                    return;
                }

                if (onQueryYoutubeSuccessListener != null) {
                    onQueryYoutubeSuccessListener.onQueryYoutubeSuccess(pResult);
                }

//                if(onQueryYoutubeErrorListener != null){
//                    onQueryYoutubeErrorListener.onQueryYoutubeError(1, "TSET", "TSET");
//                }

            } catch (Exception e) {
                Log.e(this.getClass().getSimpleName(), "Error playing video!", e);
                setYoutubeErrorMessage(2, "视频地址信息获取失败！", "未知异常， Exception " + e.getMessage());
            }
        }
    }

    OnQueryYoutubeErrorListener onQueryYoutubeErrorListener;

    public interface OnQueryYoutubeErrorListener {
        void onQueryYoutubeError(int errorTag, String errorMsg, String description);
    }

    public void setOnQueryYoutubeErrorListener(OnQueryYoutubeErrorListener onQueryYoutubeErrorListener) {
        this.onQueryYoutubeErrorListener = onQueryYoutubeErrorListener;
    }

    OnQueryYoutubeSuccessListener onQueryYoutubeSuccessListener;

    public interface OnQueryYoutubeSuccessListener {
        void onQueryYoutubeSuccess(Uri pResult);
    }

    public void setOnQueryYoutubeSuccessListener(OnQueryYoutubeSuccessListener onQueryYoutubeSuccessListener) {
        this.onQueryYoutubeSuccessListener = onQueryYoutubeSuccessListener;
    }


    /**
     * @param resoluteLevel  清晰度水平，数值越大，清晰度越高，最高清晰度5（1080），依次递减
     * @param realUrl
     * @param urlSparseArray
     * @return
     */
    private static String getStanderResolution(int resoluteLevel, String videoId, String realUrl,
                                               SparseArray<Map<String, String>> urlSparseArray) {
        // 现获取有url的itag的集合
        int length = urlSparseArray.size();
        ArrayList<Map<String, String>> itags = new ArrayList<Map<String, String>>();
        // 13, // 3GPP (MPEG-4 encoded) Low quality
        // 17, // 3GPP (MPEG-4 encoded) Medium quality
        // 18, // MP4 (H.264 encoded) Normal quality
        // 22, // MP4 (H.264 encoded) High quality
        // 37 // MP4 (H.264 encoded) High quality
        Map<String, String> tagsHigher = new HashMap<String, String>();
        Map<String, String> tagsHigh = new HashMap<String, String>();
        Map<String, String> tagsNormal = new HashMap<String, String>();
        Map<String, String> tagsMedium = new HashMap<String, String>();
        Map<String, String> tagsLow = new HashMap<String, String>();
        // 根据清晰度来个排序
        for (int i = 0; i < length; i++) {
            // 提出没有包含url和itag的数据
            Map<String, String> paramsMaps = urlSparseArray.get(i);
            if (TextUtils.isEmpty(paramsMaps.get("url"))) {
                continue;
            }
            String itagStr = paramsMaps.get("itag");
            if (TextUtils.isEmpty(itagStr)) {
                String url = Uri.decode(paramsMaps.get("url"));
                int index = url.indexOf("&itag");
                String itag0 = url.substring(index + 6, url.length());
                int index1 = itag0.indexOf("&");
                if (index1 > 0) {
                    String itag1 = itag0.substring(0, index1);
                    int tag = 0;
                    try {
                        tag = Integer.parseInt(itag1);
                        if (tag > 0 && tag < 200) {
                            itagStr = String.valueOf(tag);
                            // Log.e("youtube", "没有itag字段，但是处理后符合条件的情况： " +
                            // itagStr);
                        }
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                        continue;
                    }
                }
            }
            /**
             * itag: web / 3gp 240P 36; 240P 17; 360P 18; 720P 22; 1080P 37; mp4
             * 720P 137; 480P 136; 360P 135; 240P 134
             */
            // 13, // 3GPP (MPEG-4 encoded) Low quality
            // 17, // 3GPP (MPEG-4 encoded) Medium quality
            // 18, // MP4 (H.264 encoded) Normal quality
            // 22, // MP4 (H.264 encoded) High quality
            // 37 // MP4 (H.264 encoded) High quality

            if ("37".equals(itagStr)) { // 1080P
                tagsHigher.put("itag", itagStr);
                tagsHigher.put("url", paramsMaps.get("url"));
            } else if ("22".equals(itagStr)) { // 720P
                tagsHigh.put("itag", itagStr);
                tagsHigh.put("url", paramsMaps.get("url"));
            } else if ("18".equals(itagStr)) { // 360P
                tagsNormal.put("itag", itagStr);
                tagsNormal.put("url", paramsMaps.get("url"));
            } else if ("17".equals(itagStr)) { // 240P 或者其他未知
                tagsMedium.put("itag", itagStr);
                tagsMedium.put("url", paramsMaps.get("url"));
            } else if ("13".equals(itagStr)) {
                tagsLow.put("itag", itagStr);
                tagsLow.put("url", paramsMaps.get("url"));
            }
        }

        int i = 0;

        if (tagsLow != null) {
            itags.add(i++, tagsLow);
        }

        if (tagsMedium != null) {
            itags.add(i++, tagsMedium);
        }

        if (tagsNormal != null) {
            itags.add(i++, tagsNormal);
        }

        if (tagsHigh != null) {
            itags.add(i++, tagsHigh);
        }

        if (tagsHigher != null) {
            itags.add(i++, tagsHigher);
        }

        int size = itags.size();
        if (resoluteLevel <= size) {
            Map<String, String> paramsMap = itags.get(resoluteLevel - 1);
            if (paramsMap != null && paramsMap.size() != 0) {
                for (int m = 0; m < paramsMap.size(); m++) {
                    if (!TextUtils.isEmpty(paramsMap.get("url"))) {
                        realUrl = paramsMap.get("url");
                        Log.e("youtube", "有符合条件的url， itag： " + paramsMap.get("itag") + " ,url: " + realUrl);
                        break;
                    }
                }
            } else { // 没有对应清晰度的视频，往下查找对应的清晰度
                // Log.e("youtube", "没有符合条件的的url");
                for (int j = resoluteLevel - 2; j >= 0; j--) {
                    Map<String, String> paramsMaps = itags.get(j);
                    for (int m = 0; m < paramsMaps.size(); m++) {
                        if (!TextUtils.isEmpty(paramsMaps.get("url"))) {
                            realUrl = paramsMaps.get("url");
                            // Log.e("youtube", "没有符合条件的的url,查找到更低层次的视频: " +
                            // realUrl);
                            break;
                        }
                    }
                    if (TextUtils.isEmpty(realUrl) == false) {
                        break;
                    }
                }
            }
        }
        return realUrl;
    }

    public void setYoutubeErrorMessage(int errorTag, String msg, String description) {
        if (onQueryYoutubeErrorListener != null) {
            onQueryYoutubeErrorListener.onQueryYoutubeError(errorTag, msg, description);
        }

    }

    public static boolean hasVideoBeenViewed(Context pCtxt, String pVideoId) {
        SharedPreferences lPrefs = PreferenceManager.getDefaultSharedPreferences(pCtxt);

        String lViewedVideoIds = lPrefs.getString("com.keyes.screebl.lastViewedVideoIds", null);

        if (lViewedVideoIds == null) {
            return false;
        }

        String[] lSplitIds = lViewedVideoIds.split(";");
        if (lSplitIds == null || lSplitIds.length == 0) {
            return false;
        }

        for (int i = 0; i < lSplitIds.length; i++) {
            if (lSplitIds[i] != null && lSplitIds[i].equals(pVideoId)) {
                return true;
            }
        }

        return false;

    }

    public static void markVideoAsViewed(Context pCtxt, String pVideoId) {

        SharedPreferences lPrefs = PreferenceManager.getDefaultSharedPreferences(pCtxt);

        if (pVideoId == null) {
            return;
        }

        String lViewedVideoIds = lPrefs.getString("com.keyes.screebl.lastViewedVideoIds", null);

        if (lViewedVideoIds == null) {
            lViewedVideoIds = "";
        }

        String[] lSplitIds = lViewedVideoIds.split(";");
        if (lSplitIds == null) {
            lSplitIds = new String[]{};
        }

        // make a hash table of the ids to deal with duplicates
        Map<String, String> lMap = new HashMap<String, String>();
        for (int i = 0; i < lSplitIds.length; i++) {
            lMap.put(lSplitIds[i], lSplitIds[i]);
        }

        // recreate the viewed list
        String lNewIdList = "";
        Set<String> lKeys = lMap.keySet();
        Iterator<String> lIter = lKeys.iterator();
        while (lIter.hasNext()) {
            String lId = lIter.next();
            if (!lId.trim().equals("")) {
                lNewIdList += lId + ";";
            }
        }

        // add the new video id
        lNewIdList += pVideoId + ";";

        Editor lPrefEdit = lPrefs.edit();
        lPrefEdit.putString("com.keyes.screebl.lastViewedVideoIds", lNewIdList);
        lPrefEdit.commit();

    }

    public static int getSupportedFallbackId(int pOldId) {
        final int lSupportedFormatIds[] = {13, // 3GPP (MPEG-4 encoded) Low
                // quality
                17, // 3GPP (MPEG-4 encoded) Medium quality
                18, // MP4 (H.264 encoded) Normal quality
                22, // MP4 (H.264 encoded) High quality
                37 // MP4 (H.264 encoded) High quality
        };
        int lFallbackId = pOldId;
        for (int i = lSupportedFormatIds.length - 1; i >= 0; i--) {
            if (pOldId == lSupportedFormatIds[i] && i > 0) {
                lFallbackId = lSupportedFormatIds[i - 1];
            }
        }
        return lFallbackId;
    }


}
