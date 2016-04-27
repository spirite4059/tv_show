package com.okhtttp.service;

import android.content.Context;

import com.okhtttp.OkHttpCallBack;
import com.okhtttp.OkHttpUtils;
import com.okhtttp.response.AdVideoListResponse;
import com.tools.MacUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by fq_mbp on 16/4/6.
 */
public class VideoHttpService {

    private static final String HTTP_URL_GET_VIDEO_LIST = "http://api.bm.gochinatv.com/ad_v1/getAdList";

    public static void doHttpGetVideoList(Context context, OkHttpCallBack<AdVideoListResponse> listener){
        Map<String, String> params = new HashMap<>();
//        params.put("mac", MacUtils.getMacAddress(context));
//        params.put("mac", "rf-ed-rt-ws");
//        try {
//            String mac = URLEncoder.encode("cc:79:cf:88:88:88", "UTF-8");
//            LogCat.e("mac: " + mac);
//            params.put("mac", mac);
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
        params.put("mac", MacUtils.getMacAddress(context));
        OkHttpUtils.getInstance().doHttpGet(HTTP_URL_GET_VIDEO_LIST, params, listener);
    }


}
