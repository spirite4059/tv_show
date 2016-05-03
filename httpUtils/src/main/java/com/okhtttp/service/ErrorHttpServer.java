package com.okhtttp.service;

import android.content.Context;
import android.text.TextUtils;

import com.okhtttp.OkHttpCallBack;
import com.okhtttp.OkHttpUtils;
import com.okhtttp.request.ErrorMsgRequest;
import com.okhtttp.response.AdVideoListResponse;
import com.tools.MacUtils;

/**
 * Created by fq_mbp on 16/4/29.
 */
public class ErrorHttpServer {

    private static final String HTTP_URL_GET_VIDEO_LIST = " http://api.bm.gochinatv.com/device_v1/uploadLog";

    public static void doHttpUpLog(Context context, String errorMsg, OkHttpCallBack<AdVideoListResponse> listener) {
        if(context == null || TextUtils.isEmpty(errorMsg)){
            return;
        }
        ErrorMsgRequest errorMsgRequest = new ErrorMsgRequest();
        errorMsgRequest.mac = MacUtils.getMacAddress(context);
        errorMsgRequest.msg = errorMsg;

        OkHttpUtils.getInstance().doHttpPost(HTTP_URL_GET_VIDEO_LIST, errorMsgRequest, listener);
    }


}
