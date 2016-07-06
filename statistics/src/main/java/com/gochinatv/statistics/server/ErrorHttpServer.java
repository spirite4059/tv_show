package com.gochinatv.statistics.server;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;

import com.gochinatv.statistics.response.ErrorContent;
import com.gochinatv.statistics.tools.DataUtils;
import com.google.gson.Gson;
import com.okhtttp.OkHttpCallBack;
import com.okhtttp.OkHttpUtils;
import com.okhtttp.request.ErrorMsgRequest;
import com.okhtttp.response.ErrorResponse;
import com.tools.Constants;
import com.tools.LogCat;
import com.tools.MacUtils;

/**
 * Created by fq_mbp on 16/4/29.
 */
public class ErrorHttpServer {

    private static final String HTTP_URL_GET_VIDEO_LIST = " http://api.bm.gochinatv.com/app-api2/device_v1/uploadLog";

    public static void doHttpUpLog(Context context, String errorMsg, OkHttpCallBack<ErrorResponse> listener) {
        if(context == null || TextUtils.isEmpty(errorMsg)){
            return;
        }
        try {
            ErrorMsgRequest errorMsgRequest = new ErrorMsgRequest();

            errorMsgRequest.mac = MacUtils.getMacAddress(context);

            errorMsgRequest.type = Constants.CRASH;
            // 错误日志模块的内容
            ErrorContent errorContent = new ErrorContent();
            errorContent.mac = errorMsgRequest.mac;
            errorContent.versionCode = DataUtils.getAppVersionCode(context);
            try {
                errorContent.versionName = DataUtils.getAppVersionName(context);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            errorContent.sdk = DataUtils.getAndroidOSVersion();
            errorContent.errorMsg = errorMsg;

            Gson gson = new Gson();

            errorMsgRequest.msg = gson.toJson(errorContent);

            LogCat.e("error", "doHttpUpLog...............");
            OkHttpUtils.getInstance().doHttpPost(HTTP_URL_GET_VIDEO_LIST, errorMsgRequest, listener);


//            Map<String, String> params = new HashMap<>();
//            params.put("mac", MacUtils.getMacAddress(context));
//            params.put("msg", gson.toJson(errorContent));
//            params.put("type", String.valueOf(Constants.CRASH));
//
//
//            OkHttpUtils.getInstance().doHttpGet(HTTP_URL_GET_VIDEO_LIST, params, listener);


        }catch (Exception e){
            e.printStackTrace();
        }

    }


    /**
     * 上报统计数据
     */
    public static void doStatisticsHttp(Context context, int type, String errorMsg, OkHttpCallBack<ErrorResponse> listener){
        if(context == null || TextUtils.isEmpty(errorMsg)){
            return;
        }
        try{
            ErrorMsgRequest errorMsgRequest = new ErrorMsgRequest();
            errorMsgRequest.mac = MacUtils.getMacAddress(context);
            errorMsgRequest.type = type;
            errorMsgRequest.sdk = Build.VERSION.SDK_INT;
            errorMsgRequest.versionCode = DataUtils.getAppVersionCode(context);
            errorMsgRequest.versionName = DataUtils.getVersionName(context);
            errorMsgRequest.msg = errorMsg;

            OkHttpUtils.getInstance().doHttpPost(HTTP_URL_GET_VIDEO_LIST, errorMsgRequest, listener);
        }catch (Exception e){
            e.printStackTrace();
        }


    }















}
