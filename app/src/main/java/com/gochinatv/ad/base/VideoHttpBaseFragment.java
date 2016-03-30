package com.gochinatv.ad.base;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import com.gochinatv.ad.tools.Constants;
import com.gochinatv.ad.tools.DataUtils;
import com.gochinatv.ad.tools.LogCat;
import com.gochinatv.ad.tools.SharedPreference;
import com.httputils.http.response.CdnPathResponse;
import com.httputils.http.response.TimeResponse;
import com.httputils.http.response.UpdateResponse;
import com.httputils.http.response.VideoDetailListResponse;
import com.okhtttp.OkHttpCallBack;
import com.okhtttp.OkHttpUtils;
import com.tools.HttpUrls;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by fq_mbp on 16/3/17.
 */
public abstract class VideoHttpBaseFragment extends BaseFragment {


    private boolean isTest = true;

    protected abstract void onGetVideoListSuccessful(VideoDetailListResponse response, String url);

    protected abstract void onGetVideoListFailed(String errorMsg, String url);

    protected abstract void onGetVideoPathSuccessful(String path);

    protected abstract void onGetVideoPathFailed(String path);

    protected abstract void onUpgradeSuccessful(UpdateResponse.UpdateInfoResponse updateInfo);

    /**
     * 请求视频列表数
     */
    protected void doHttpGetEpisode() {
        Map<String, String> map = new HashMap<>();
        map.put("albumId", "63272");    // 测试用
//        map.put("albumId", "66371");
        map.put("videoType", "1");
        map.put("serialType", "1");

        String url = isTest ? HttpUrls.URL_VIDEO_LIST_TEST : HttpUrls.URL_VIDEO_LIST;

        OkHttpUtils.getInstance().doHttpGet(url, map, new OkHttpCallBack<VideoDetailListResponse>() {
            @Override
            public void onSuccess(String url, VideoDetailListResponse response) {
                LogCat.e("onSuccess........" + url);
                onGetVideoListSuccessful(response, url);
            }

            @Override
            public void onError(String url, String errorMsg) {
                LogCat.e("onDownloadFileError........");
                onGetVideoListFailed(errorMsg, url);
            }
        });
    }


    protected void doHttpGetCdnPath(final String vid) {
        LogCat.e("获取cdn的真是地址。。。。。。。" + vid);
        Map<String, String> url = new HashMap();
        url.put("url", HttpUrls.SECURITY_CHAIN_URL + vid);

        OkHttpUtils.getInstance().doHttpGet(HttpUrls.HTTP_URL_CDN_PATH, url, new OkHttpCallBack<CdnPathResponse>() {
            @Override
            public void onSuccess(String url, CdnPathResponse response) {
                if (!isAdded()) {
                    return;
                }
                LogCat.e("onSuccess。。。。。。。" + url);
                if (response == null || !(response instanceof CdnPathResponse)) {
                    LogCat.e("cdn地址请求成功 数据错误1。。。。。。。");
                    onGetVideoPathFailed(url);
                    return;
                }
                if (response.data == null) {
                    LogCat.e("cdn地址请求成功 数据错误2。。。。。。。");
                    onGetVideoPathFailed(url);
                    return;
                }


                if (TextUtils.isEmpty(response.data.url)) {
                    LogCat.e("cdn地址为空。。。。。。。");
                    onGetVideoPathFailed(url);
                    return;
                }

                onGetVideoPathSuccessful(response.data.url);

            }

            @Override
            public void onError(String url, String errorMsg) {
                if (!isAdded()) {
                    return;
                }

                LogCat.e("cdn地址获取失败。。。。。。。" + url);
                onGetVideoPathFailed(url);
            }
        });

    }


    /**
     * 检查是否有版本更新
     */

    private int reTryTimes;

    protected void doHttpUpdate(final Context context) {
        Map<String, String> map = new HashMap<>();
        map.put("platformId", String.valueOf("22"));
        if (!TextUtils.isEmpty(android.os.Build.MODEL)) {
            // 不为空
            try {
                map.put("modelNumber", URLEncoder.encode(android.os.Build.MODEL, "utf-8")); // 型号
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                map.put("modelNumber", android.os.Build.MODEL); // 型号
            }
        }
        try {
            ApplicationInfo appInfo = getActivity().getPackageManager().getApplicationInfo(getActivity().getPackageName(),
                    PackageManager.GET_META_DATA);
            if (appInfo != null) {
                String brand = appInfo.metaData.getString("UMENG_CHANNEL");
                if (TextUtils.isEmpty(brand)) {
                    map.put("brandNumber", "chinarestaurant"); // 品牌
                } else {
                    map.put("brandNumber", brand); // 品牌
                }
            }
        } catch (PackageManager.NameNotFoundException e1) {
            e1.printStackTrace();
            map.put("brandNumber", "chinarestaurant"); // 品牌
        }

        OkHttpUtils.getInstance().

                doHttpGet(HttpUrls.URL_CHECK_UPDATE, map, new OkHttpCallBack<UpdateResponse>() {
                    @Override
                    public void onSuccess(String url, UpdateResponse response) {
                        LogCat.e("onSuccess adVideoUrl: " + url);
                        if (!isAdded()) {
                            return;
                        }


                        if (response == null || !(response instanceof UpdateResponse)) {
                            LogCat.e("升级数据出错，无法正常升级1。。。。。");
                            doError();
                            return;
                        }

                        if (response.resultForApk == null || !(response.resultForApk instanceof UpdateResponse.UpdateInfoResponse)) {
                            LogCat.e("升级数据出错，无法正常升级2。。。。。");
                            doError();
                            return;
                        }

                        if ("1".equals(response.status) == false) {
                            LogCat.e("升级接口的status == 0。。。。。");
                            doError();
                            return;
                        }


                        UpdateResponse.UpdateInfoResponse updateInfo = response.resultForApk;
                        // 获取当前最新版本号
                        if (TextUtils.isEmpty(updateInfo.versionCode) == false) {
                            double netVersonCode = Integer.parseInt(updateInfo.versionCode);
                            // 检测是否要升级
                            try {
                                if (DataUtils.getAppVersion(context) < netVersonCode) { // 升级
                                    // 升级
                                    // 下载最新安装包，下载完成后，提示安装
                                    LogCat.e("需要升级。。。。。");

                                    // 去下载当前的apk
                                    onUpgradeSuccessful(updateInfo);


                                } else {
                                    // 不升级
                                    LogCat.e("无需升级。。。。。");
                                    doHttpGetTime(context);
                                }
                            } catch (PackageManager.NameNotFoundException e) {
                                e.printStackTrace();
                                LogCat.e("判断升级过程中出错。。。。。");
                                doError();
                            }
                        } else {
                            // 不升级
                            LogCat.e("升级版本为null。。。。。");
                            doError();
                        }

                    }

                    private void doError() {
                        if (isAdded()) {
                            // 做不升级处理, 继续请求广告视频列表
                            reTryTimes++;
                            if (reTryTimes > 4) {
                                reTryTimes = 0;
                                doHttpGetTime(context);
                            } else {
                                LogCat.e("进行第 " + reTryTimes + " 次重试请求。。。。。。。");
                                doHttpUpdate(getActivity());
                            }
                        }
                    }

                    @Override
                    public void onError(String url, String errorMsg) {
                        LogCat.e("请求接口出错，无法升级。。。。。" + url);
                        doError();
                    }
                });


    }


    /**
     * 检查是否有版本更新
     */
    protected void doHttpGetTime(final Context context) {
        OkHttpUtils.getInstance().doHttpGet(HttpUrls.URL_GET_SERVER_TIMELONG, new OkHttpCallBack<TimeResponse>() {
            @Override
            public void onSuccess(String url, TimeResponse response) {
                if (!isAdded()) {
                    return;
                }

                LogCat.e("获取当前时间。。。。。。onSuccess");
                if (response == null || !(response instanceof TimeResponse)) {
                    LogCat.e("升级数据出错，无法正常升级1。。。。。");
                    doError();
                    return;
                }

                if (TextUtils.isEmpty(response.serverTime)) {
                    doError();
                } else {
                    try {
                        long time = Long.parseLong(response.serverTime);
                        LogCat.e("time):           " + time);
                        long timeInterval = Calendar.getInstance().getTimeInMillis() - time;
                        doHttpGetEpisode();
                    } catch (Exception e) {
                        e.printStackTrace();
                        doError();
                    }


                }
            }

            @Override
            public void onError(String url, String errorMsg) {
                // 升级失败
                LogCat.e("获取当前时间。。。。。。onDownloadFileError");
                doError();
            }


            private void doError() {
                if (!isAdded()) {
                    // 做不升级处理, 继续请求广告视频列表
                    doHttpGetEpisode();
                }

            }

//            private long getTimeOffset() {
//                Date sourceDate = Calendar.getInstance().getTime();
//                TimeZone sourceTimeZone = TimeZone.getTimeZone("EST");
//                TimeZone targetTimeZone = TimeZone.getTimeZone("GMT+8");
//                Long targetTime = sourceDate.getTime() - sourceTimeZone.getRawOffset() + targetTimeZone.getRawOffset();
//                return targetTime;
//            }
        });


    }


    protected void recordStartTime() {
        SharedPreference sharedPreference = SharedPreference.getSharedPreferenceUtils(getActivity());
        sharedPreference.saveDate(Constants.SHARE_KEY_DURATION, System.currentTimeMillis());
    }

    protected void computeTime() {
        SharedPreference sharedPreference = SharedPreference.getSharedPreferenceUtils(getActivity());
        // 计算离开的时候总时长
        try {
            long startLong = sharedPreference.getDate(Constants.SHARE_KEY_DURATION, 0L);
            if (startLong != 0) {
                long duration = System.currentTimeMillis() - startLong;
                if (duration > 0) {
                    long day = duration / (24 * 60 * 60 * 1000);
                    long hour = (duration / (60 * 60 * 1000) - day * 24);
                    long min = ((duration / (60 * 1000)) - day * 24 * 60 - hour * 60);
                    long s = (duration / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - min * 60);
                    String str = day + "天  " + hour + "时" + min + "分" + s + "秒";

                    LogCat.e(str);

                    LogCat.e("上报开机时长。。。。。。。。");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            sharedPreference.saveDate(Constants.SHARE_KEY_DURATION, 0);
        }
    }



//    protected class NetStatusReceiver extends BroadcastReceiver{
//
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            if(intent.getAction().equals(Constants.INTENT_RECEIVER_NET_STATUS){
//                ConnectivityManager connectivityManager=(ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
//                NetworkInfo activeInfo = connectivityManager.getActiveNetworkInfo();
//                if (activeInfo == null) {
//                    LogCat.e("网络不可以用");
//                    //改变背景或者 处理网络的全局变量
//                }else {
//                    //改变背景或者 处理网络的全局变量
//                }
//            }
//        }
//    }


}
