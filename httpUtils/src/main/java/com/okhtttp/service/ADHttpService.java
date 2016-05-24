package com.okhtttp.service;

import android.content.Context;

import com.okhtttp.OkHttpCallBack;
import com.okhtttp.OkHttpUtils;
import com.okhtttp.response.ADDeviceDataResponse;
import com.okhtttp.response.ADFourResponse;
import com.okhtttp.response.ADTwoOtherResponse;
import com.okhtttp.response.AdThreeDataResponse;
import com.tools.MacUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zfy on 2016/4/7.
 */
public class ADHttpService {

    private static final String BASE_HTTP_URL = "http://api.bm.gochinatv.com/ad_v1";

    /**
     * 广告体---布局的宽高
     */
    public static final String URL_GET_AD_DEVICE = BASE_HTTP_URL +"/getDeviceInfo";
    public static void doHttpGetDeviceInfo(Context context, OkHttpCallBack<ADDeviceDataResponse> listener){
        Map<String, String> params = new HashMap<>();
        params.put("mac", MacUtils.getMacAddress(context));
        OkHttpUtils.getInstance().doHttpGet(URL_GET_AD_DEVICE, params, listener);
    }


    /**
     * 广告二--web广告;
     */
//    public static final String URL_GET_AD_TWO = BASE_HTTP_URL + "/getWebAdInfo";
//    public static void doHttpGetWebADInfo(Context context, OkHttpCallBack<ADTwoResponse> listener){
//        Map<String, String> params = new HashMap<>();
//        params.put("mac", MacUtils.getMacAddress(context));
//        OkHttpUtils.getInstance().doHttpGet(URL_GET_AD_TWO, params, listener);
//    }

    //public static final String URL_GET_AD_TWO = "http://mock.vego.tv:8888/zfy/getAdList";
    public static final String URL_GET_AD_TWO = BASE_HTTP_URL+"/getWebAdInfo";
    public static void doHttpGetWebADInfo(Context context, OkHttpCallBack<ADTwoOtherResponse> listener){
        Map<String, String> params = new HashMap<>();
        params.put("mac", MacUtils.getMacAddress(context));
        OkHttpUtils.getInstance().doHttpGet(URL_GET_AD_TWO, params, listener);
    }


    /**
     * 广告三--图片广告
     */
    public static final String URL_GET_AD_THREE = BASE_HTTP_URL + "/getImageAdList";
    //public static final String URL_GET_AD_THREE = "http://mock.vego.tv:8888/zfy/getAdList";
    public static void doHttpGetImageADInfo(Context context, OkHttpCallBack<AdThreeDataResponse> listener){
        Map<String, String> params = new HashMap<>();
        params.put("mac", MacUtils.getMacAddress(context));
        OkHttpUtils.getInstance().doHttpGet(URL_GET_AD_THREE, params, listener);
    }


    /**
     * 广告四--文字广告
     */
    public static final String URL_GET_AD_FOUR = BASE_HTTP_URL + "/getTextAdList";
    //public static final String url = "http://mock.vego.tv:8888/zfy/getTextList";
    //public static final String url = "http://192.168.3.191:8080/api/ad_v1/getTextAdList";
    public static void doHttpGetTextADInfo(Context context, OkHttpCallBack<ADFourResponse> listener){
        Map<String, String> params = new HashMap<>();
        params.put("mac", MacUtils.getMacAddress(context));
        OkHttpUtils.getInstance().doHttpGet(URL_GET_AD_FOUR, params, listener);
    }


}
