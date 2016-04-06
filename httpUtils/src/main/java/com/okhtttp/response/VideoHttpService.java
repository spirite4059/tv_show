package com.okhtttp.response;

import android.content.Context;

import com.okhtttp.MacUtils;
import com.okhtttp.OkHttpCallBack;
import com.okhtttp.OkHttpUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by fq_mbp on 16/4/6.
 */
public class VideoHttpService {

    private static final String HTTP_URL_GET_VIDEO_LIST = "http://210.14.151.100:8090/api/ad_v1/getAdList";


    public static void doHttpGetVideoList(Context context, OkHttpCallBack<AdVideoListResponse> listener){
        Map<String, String> params = new HashMap<>();
        params.put("mac", MacUtils.getMacAddress(context));
        OkHttpUtils.getInstance().doHttpGet(HTTP_URL_GET_VIDEO_LIST, params, listener);
    }


}
