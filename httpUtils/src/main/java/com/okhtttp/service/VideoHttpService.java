package com.okhtttp.service;

import android.content.Context;

import com.okhtttp.MacUtils;
import com.okhtttp.OkHttpCallBack;
import com.okhtttp.OkHttpUtils;
import com.okhtttp.response.AdVideoListResponse;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by fq_mbp on 16/4/6.
 */
public class VideoHttpService {

//    private static final String HTTP_URL_GET_VIDEO_LIST = "http://192.168.2.196:8080/api/ad_v1/getAdList";
    private static final String HTTP_URL_GET_VIDEO_LIST = "http://210.14.151.100:8090/api/ad_v1/getAdList";
//                                                           http://210.14.151.100:8090/api/ad_v1/getAdList?mac=cc-79-cf-88-88-88

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
