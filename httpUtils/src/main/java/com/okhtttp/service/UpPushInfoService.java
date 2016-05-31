package com.okhtttp.service;

import com.okhtttp.OkHttpCallBack;
import com.okhtttp.OkHttpUtils;
import com.okhtttp.response.AdVideoListResponse;

import java.util.Map;

/**
 * Created by fq_mbp on 16/4/6.
 */
public class UpPushInfoService {

    private static final String HTTP_URL_GET_VIDEO_LIST = "http://114.215.142.23:7001/hudong/api/v2/device/config";

    public static void doHttpUpPushInfo(Map<String, String> params, OkHttpCallBack<AdVideoListResponse> listener){
        OkHttpUtils.getInstance().doHttpGet(HTTP_URL_GET_VIDEO_LIST, params, listener);
    }


}
