package com.gochinatv.statistics;

import android.app.Activity;
import android.content.Context;
import android.os.Build;

import com.gochinatv.statistics.request.RetryErrorRequest;
import com.gochinatv.statistics.request.UpgradeLogRequest;
import com.gochinatv.statistics.server.InitializeServerLog;
import com.gochinatv.statistics.server.UpgradeServerLog;
import com.gochinatv.statistics.tools.DataUtils;
import com.gochinatv.statistics.tools.LogCat;
import com.okhtttp.OkHttpCallBack;
import com.okhtttp.request.ErrorMsgRequest;
import com.tools.MacUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by zfy on 2016/5/5.
 */
public class SendStatisticsLog {


    /**
     * 上传激活日志
     */
    private static int initializeRetry = 0;
    public static void sendInitializeLog(final Context context){
        initializeRetry++;
        Map<String,String> map = new HashMap<>();
        map.put("mac", MacUtils.getMacAddress(context));
        map.put("versionName", DataUtils.getVersionName(context));
        map.put("versionNum",String.valueOf(DataUtils.getAppVersionCode(context)));
        map.put("sdk",String.valueOf(Build.VERSION.SDK_INT));
        LogCat.e("statistics",DataUtils.getVersionName(context) + "   "+ String.valueOf(DataUtils.getAppVersionCode(context)) + "  " + String.valueOf(Build.VERSION.SDK_INT));
        InitializeServerLog.doPostHttpInitializeLog(map, new OkHttpCallBack<ErrorMsgRequest>() {
            @Override
            public void onSuccess(String url, ErrorMsgRequest response) {
                LogCat.e("statistics","提交激活日志成功!!!!!!!!!");
            }

            @Override
            public void onError(String url, String errorMsg) {
                LogCat.e("statistics","提交激活日志失败*********");
                if(initializeRetry < 4){
                    sendInitializeLog(context);
                }
            }
        });
    }


    /**
     * 提交升级接口失败日志
     */
    public static void sendUpgradeLog(Context context, int times,ArrayList<RetryErrorRequest> list){
        //接口失败
        if(times >0 && list != null && list.size()>0){
            //此时接口存在请求失败
            UpgradeLogRequest upgradeLogRequest = new UpgradeLogRequest();
            upgradeLogRequest.mac = MacUtils.getMacAddress(context);
            upgradeLogRequest.versionName = DataUtils.getVersionName(context);
            upgradeLogRequest.versionCode = String.valueOf(DataUtils.getAppVersionCode(context));
            upgradeLogRequest.sdk = String.valueOf(Build.VERSION.SDK_INT);
            upgradeLogRequest.downloadError = null;
            upgradeLogRequest.isUpdateSuccess = null;
            upgradeLogRequest.isDownloadSuccess = null;
            upgradeLogRequest.interfaceError = list;
            upgradeLogRequest.isNeedUpdate = null;
            upgradeLogRequest.isGetUpdateInfo = null;
            UpgradeServerLog.doPostHttpUpgradeLog((Activity)context,"upgrade",upgradeLogRequest);
        }

//        //接口成功
//        if(times == 0){
//            UpgradeLogRequest upgradeLogRequest = new UpgradeLogRequest();
//            upgradeLogRequest.mac = MacUtils.getMacAddress(context);
//            upgradeLogRequest.versionName = DataUtils.getVersionName(context);
//            upgradeLogRequest.versionCode = String.valueOf(DataUtils.getAppVersionCode(context));
//            upgradeLogRequest.sdk = String.valueOf(Build.VERSION.SDK_INT);
//            upgradeLogRequest.downloadError = null;
//            upgradeLogRequest.isUpdateSuccess = null;
//            upgradeLogRequest.isDownloadSuccess = null;
//            upgradeLogRequest.interfaceError = null;
//            upgradeLogRequest.isNeedUpdate = null;
//            upgradeLogRequest.isGetUpdateInfo = "1";
//            UpgradeServerLog.doPostHttpUpgradeLog((Activity)context,"upgrade",upgradeLogRequest, new OkHttpCallBack<ErrorMsgRequest>() {
//                @Override
//                public void onSuccess(String url, ErrorMsgRequest response) {
//                    LogCat.e("statistics","提交升级日志成功!!!!!!!!!");
//                }
//
//                @Override
//                public void onError(String url, String errorMsg) {
//                    LogCat.e("statistics","提交升级日志失败**********");
//                }
//            });
//        }
    }


    /**
     * 提交布局接口失败日志
     */
    public static void sendLayoutLog(Context context, int times, ArrayList<RetryErrorRequest> list){
        if(times >0 && list != null && list.size()>0){
            UpgradeLogRequest upgradeLogRequest = new UpgradeLogRequest();
            upgradeLogRequest.mac = MacUtils.getMacAddress(context);
            upgradeLogRequest.versionName = DataUtils.getVersionName(context);
            upgradeLogRequest.versionCode = String.valueOf(DataUtils.getAppVersionCode(context));
            upgradeLogRequest.sdk = String.valueOf(Build.VERSION.SDK_INT);
            upgradeLogRequest.downloadError = null;
            upgradeLogRequest.isUpdateSuccess = null;
            upgradeLogRequest.isDownloadSuccess = null;
            upgradeLogRequest.interfaceError = list;
            upgradeLogRequest.isNeedUpdate = null;
            upgradeLogRequest.isGetUpdateInfo = null;

            UpgradeServerLog.doPostHttpUpgradeLog((Activity)context,"layout",upgradeLogRequest);



        }
    }


    /**
     * 提交APK下载失败接口日志
     */
    public static void sendAPKDownloadLog(Context context, int times, ArrayList<RetryErrorRequest> list){

        //接口失败
        if(times >0 && list != null && list.size()>0){
            //此时接口存在请求失败
            UpgradeLogRequest upgradeLogRequest = new UpgradeLogRequest();
            upgradeLogRequest.mac = MacUtils.getMacAddress(context);
            upgradeLogRequest.versionName = DataUtils.getVersionName(context);
            upgradeLogRequest.versionCode = String.valueOf(DataUtils.getAppVersionCode(context));
            upgradeLogRequest.sdk = String.valueOf(Build.VERSION.SDK_INT);
            upgradeLogRequest.downloadError = list;
            upgradeLogRequest.isUpdateSuccess = "0";
            upgradeLogRequest.isDownloadSuccess = "0";
            upgradeLogRequest.interfaceError = null;
            upgradeLogRequest.isNeedUpdate = "1";
            upgradeLogRequest.isGetUpdateInfo = "1";
            UpgradeServerLog.doPostHttpUpgradeLog((Activity)context,"APKDownload",upgradeLogRequest);
        }

    }


    /**
     * 提交视频接口失败日志
     */

    //public







}
