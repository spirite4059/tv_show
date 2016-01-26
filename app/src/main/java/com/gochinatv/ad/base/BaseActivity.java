package com.gochinatv.ad.base;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.gochinatv.ad.tools.DataUtils;
import com.gochinatv.ad.tools.LogCat;
import com.gochinatv.ad.tools.SharedPreference;
import com.gochinatv.ad.ui.dialog.DialogLoading;
import com.gochinatv.ad.ui.dialog.DialogUtils;
import com.httputils.http.OnRequestListener;
import com.httputils.http.response.CdnPathResponse;
import com.httputils.http.response.TimeResponse;
import com.httputils.http.response.UpdateResponse;
import com.httputils.http.response.VideoDetailListResponse;
import com.httputils.http.service.AlbnumHttpService;
import com.httputils.http.service.CDNHttpService;
import com.httputils.http.service.TimeHttpService;
import com.umeng.analytics.MobclickAgent;
import com.vego.player.SecurityChain;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

/**
 * Created by fq_mbp on 15/12/23.
 */
public abstract class BaseActivity extends Activity {

    private DialogLoading loadingDialog;

    private static final String FORMAT_VIDEO_AD_TIME = "yyyyMMddHHmmss";

    private boolean isTest = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
//        MobclickAgent.setCheckDevice(false);
        MobclickAgent.setCatchUncaughtExceptions(true);
        MobclickAgent.setDebugMode(true);
    }

    public void showLoading() {
        if (loadingDialog == null) {
            loadingDialog = DialogUtils.showLoading(this);
        } else {
            loadingDialog.show();
        }
    }

    public void hideLoading() {
        if (loadingDialog != null) {
            loadingDialog.dismiss();
        }
    }

    protected abstract void onSuccessFul(VideoDetailListResponse response, String url);

    protected abstract void onFailed(String errorMsg, String url);


    /**
     * 请求视频列表数
     */
    protected void doHttpGetEpisode() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("albumId", "66371");
        map.put("videoType", "1");
        map.put("serialType", "1");
        AlbnumHttpService.doHttpAlbnumEpisodesList(this, map, isTest, new OnRequestListener<VideoDetailListResponse>() {
            @Override
            public void onSuccess(VideoDetailListResponse response, String url) {
                onSuccessFul(response, url);

            }

            @Override
            public void onError(String errorMsg, String url) {
                onFailed(errorMsg, url);
            }
        });
    }


    public class VideoHandler extends Handler {
        public WeakReference<Activity> mActivity;

        public VideoHandler(Activity activity) {
            mActivity = new WeakReference<>(activity);
        }


        public void handleMessage(Message msg) {
            if (mActivity == null) {
                return;
            }
            final Activity activity = mActivity.get();
            if (activity == null || activity.isFinishing()) {
                return;
            }
            doHandlerMessage(msg);
        }
    }


    protected void doHandlerMessage(Message msg) {
    }

    protected void getVideoCdnPath(String path) {
    }
    protected void onVideoCdnError(String path) {
    }



    protected void doHttpGetCdnPath(Context context, final String vid, final Date date) {
        LogCat.e("获取cdn的真是地址。。。。。。。" + vid);
        CDNHttpService.doHttpGetCdnPath(context, new OnRequestListener<CdnPathResponse>() {

            @Override
            public void onSuccess(CdnPathResponse response, String url) {
                LogCat.e("获取到真正地址。。。。。。。" + url);
                StringBuilder sbUrl = new StringBuilder(SecurityChain.SECURITY_CHAIN_URL);
                // 添加路径
                sbUrl.append(vid);
                sbUrl.append("?");
                // 添加st 有效期
                sbUrl.append("st=");
                String st = SecurityChain.getOutDate(date);
                sbUrl.append(st);
                // 添加token
                sbUrl.append("&token=");
                String token = SecurityChain.getSecurityTokey(vid, st, response.data.key);
                sbUrl.append(token);


                getVideoCdnPath(sbUrl.toString());

            }

            @Override
            public void onError(String errorMsg, String url) {
                LogCat.e("cdn地址获取失败。。。。。。。");
                onVideoCdnError(url);
            }

        });
    }


    protected class DeleteFileThread extends Thread {

        private String path;
        private String name;

        public DeleteFileThread(String path, String name) {
            this.path = path;
            this.name = name;
        }

        @Override
        public void run() {
            super.run();
            File file = new File(path, name);
            if (file != null && file.exists()) {
                file.delete();
            }
        }
    }

    protected class DeleteApkThread extends Thread {

        private String path;

        public DeleteApkThread(String path) {
            this.path = path;
        }

        @Override
        public void run() {
            super.run();
            LogCat.e("删除所有安装文件");
            delAllFile(path);
        }
    }


    UpdateResponse.UpdateInfoResponse updateInfo;
    boolean isUpdate;
    boolean isUpdateFinish;

    protected abstract void onUpdateSuccess(UpdateResponse.UpdateInfoResponse updateInfo);

    /**
     * 检查是否有版本更新
     */
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
            ApplicationInfo appInfo = this.getPackageManager().getApplicationInfo(getPackageName(),
                    PackageManager.GET_META_DATA);
            if (appInfo != null) {
                map.put("brandNumber", appInfo.metaData.getString("UMENG_CHANNEL")); // 品牌
            }
        } catch (PackageManager.NameNotFoundException e1) {
            e1.printStackTrace();
            map.put("brandNumber", "unkown"); // 品牌
        }

        AlbnumHttpService.doHttpUpdateApk(this, map, new OnRequestListener<UpdateResponse>() {
            @Override
            public void onSuccess(UpdateResponse response, String url) {
                if (isFinishing()) {
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
                            onUpdateSuccess(updateInfo);


                        } else {
                            // 不升级
                            LogCat.e("无需升级。。。。。");
                            doError();
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
                if (!isFinishing()) {
                    // 做不升级处理, 继续请求广告视频列表
                    doHttpGetTime(context);
                }

            }

            @Override
            public void onError(String errorMsg, String url) {
                // 升级失败
                LogCat.e("请求接口出错，无法升级。。。。。");
                doError();
            }
        });
    }


    protected void installApk(Context context, String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            Log.e("TAG", "installApk……" + filePath);
            Intent intent = new Intent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setAction(Intent.ACTION_VIEW); // 浏览网页的Action(动作)
            String type = "application/vnd.android.package-archive";
            intent.setDataAndType(Uri.fromFile(file), type); // 设置数据类型
            context.startActivity(intent);
        } else {
            Log.e("TAG", "uninstallApk……");
        }

    }
    public String dateTransformBetweenTimeZone(Date sourceDate, DateFormat formatter,
                                                      TimeZone sourceTimeZone, TimeZone targetTimeZone) {
        Long targetTime = sourceDate.getTime() - sourceTimeZone.getRawOffset() + targetTimeZone.getRawOffset();
        return getTime(new Date(targetTime), formatter);
    }

    public long dateTransformBetweenTimeZoneLong(Date sourceDate,
                                               TimeZone sourceTimeZone, TimeZone targetTimeZone) {
        Long targetTime = sourceDate.getTime() - sourceTimeZone.getRawOffset() + targetTimeZone.getRawOffset();
        return targetTime;
    }

    public String getTime(Date date, DateFormat formatter){
        return formatter.format(date);
    }

    protected boolean isExpired(Context context, String time) {
        boolean isExpired = false;
        timeInterval = SharedPreference.getSharedPreferenceUtils(context).getDate("timeInterval", 0L);
        // 判断当前视频是否过期，过期就不处理
        if (!TextUtils.isEmpty(time)) {
            // 继续判断是否过期
            SimpleDateFormat sdf = new SimpleDateFormat(FORMAT_VIDEO_AD_TIME);

            try {
                // 网络当前时间
                long localTime = Calendar.getInstance().getTimeInMillis() - timeInterval;
                // 视频的结束时间
                sdf.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
                Date endDate = sdf.parse(time);
//                LogCat.e("当前广告的结束时间：" + time);

                DateFormat formatter = new SimpleDateFormat(FORMAT_VIDEO_AD_TIME);
                Date date = new Date(localTime);
                TimeZone srcTimeZone = TimeZone.getDefault();
                TimeZone destTimeZone = TimeZone.getTimeZone("GMT+8");
//                LogCat.e("当前时间       ： " + dateTransformBetweenTimeZone(date, formatter, srcTimeZone, destTimeZone));
//                LogCat.e("当前广告结束时间：" + endDate.getTime());
//                LogCat.e("当前服务器的时间：" + localTime);
                // 过期数据
                if (localTime <= endDate.getTime()) {
                    isExpired = false;
                } else {
                    isExpired = true;
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return isExpired;
    }


    protected long timeInterval;
    private long getTimeOffset() {
        Date sourceDate = Calendar.getInstance().getTime();
        TimeZone sourceTimeZone = TimeZone.getTimeZone("EST");
        TimeZone targetTimeZone = TimeZone.getTimeZone("GMT+8");
        Long targetTime = sourceDate.getTime() - sourceTimeZone.getRawOffset() + targetTimeZone.getRawOffset();
        return targetTime;
    }

    /**
     * 检查是否有版本更新
     */
    protected void doHttpGetTime(final  Context context) {

        TimeHttpService.doHttpGetTime(context, new OnRequestListener<TimeResponse>() {
            @Override
            public void onSuccess(TimeResponse response, String url) {
                if (isFinishing()) {
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
                    long time = 0l;
                    try {
                        time = Long.parseLong(response.serverTime);
                        LogCat.e("time):           " + time);
                        LogCat.e("getTimeOffset(): " + getTimeOffset());
                        timeInterval = Calendar.getInstance().getTimeInMillis() - time;
                        SharedPreference sharedPreference = SharedPreference.getSharedPreferenceUtils(context);
                        sharedPreference.saveDate("timeInterval", timeInterval);

                        LogCat.e("timeInterval):               " + timeInterval);
                        doHttpGetEpisode();
                    }catch (Exception e){
                        e.printStackTrace();
                        doError();
                    }


                }


            }

            private void doError() {
                if (!isFinishing()) {
                    // 做不升级处理, 继续请求广告视频列表
                    doHttpGetEpisode();
                }

            }

            @Override
            public void onError(String errorMsg, String url) {
                // 升级失败
                LogCat.e("获取当前时间。。。。。。onError");
                doError();
            }
        });
    }




    //删除指定文件夹下所有文件
    //param path 文件夹完整绝对路径
    public boolean delAllFile(String path) {
        boolean flag = false;
        File file = new File(path);
        if (!file.exists()) {
            return flag;
        }
        if (!file.isDirectory()) {
            return flag;
        }
        String[] tempList = file.list();
        File temp = null;
        for (int i = 0; i < tempList.length; i++) {
            if (path.endsWith(File.separator)) {
                temp = new File(path + tempList[i]);
            } else {
                temp = new File(path + File.separator + tempList[i]);
            }
            if (temp.isFile()) {
                temp.delete();
            }
            if (temp.isDirectory()) {
                delAllFile(path + "/" + tempList[i]);//先删除文件夹里面的文件
                delFolder(path + "/" + tempList[i]);//再删除空文件夹
                flag = true;
            }
        }
        return flag;
    }

    public  void delFolder(String folderPath) {
        try {
            delAllFile(folderPath); //删除完里面所有内容
            String filePath = folderPath;
            filePath = filePath.toString();
            java.io.File myFilePath = new java.io.File(filePath);
            myFilePath.delete(); //删除空文件夹
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 如果服务器不支持中文路径的情况下需要转换url的编码。
     * @param string
     * @return
     */
    public String encodeGB(String string)
    {
        //转换中文编码
        String split[] = string.split("/");
        for (int i = 1; i < split.length; i++) {
            try {
                split[i] = URLEncoder.encode(split[i], "GB2312");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            split[0] = split[0]+"/"+split[i];
        }
        split[0] = split[0].replaceAll("\\+", "%20");//处理空格
        return split[0];
    }

}
