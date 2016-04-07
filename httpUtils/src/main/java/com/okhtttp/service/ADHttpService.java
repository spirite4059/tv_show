package com.okhtttp.service;

import android.content.Context;

import com.okhtttp.MacUtils;
import com.okhtttp.OkHttpCallBack;
import com.okhtttp.OkHttpUtils;
import com.okhtttp.response.ADDeviceDataResponse;
import com.okhtttp.response.ADFourResponse;
import com.okhtttp.response.ADTwoResponse;
import com.okhtttp.response.AdThreeDataResponse;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zfy on 2016/4/7.
 */
public class ADHttpService {

    /**
     * 广告体---布局的宽高
     */
    public static final String URL_GET_AD_DEVICE = "http://210.14.151.100:8090/api/ad_v1/getDeviceInfo";
    public static void doHttpGetDeviceInfo(Context context, OkHttpCallBack<ADDeviceDataResponse> listener){
        Map<String, String> params = new HashMap<>();
        params.put("mac", MacUtils.getMacAddress(context));
        OkHttpUtils.getInstance().doHttpGet(URL_GET_AD_DEVICE, params, listener);
    }


    /**
     * 广告二--web广告
     */
    public static final String URL_GET_AD_TWO = "http://210.14.151.100:8090/api/ad_v1/getWebAdInfo";
    public static void doHttpGetWebADInfo(Context context, OkHttpCallBack<ADTwoResponse> listener){
        Map<String, String> params = new HashMap<>();
        params.put("mac", MacUtils.getMacAddress(context));
        OkHttpUtils.getInstance().doHttpGet(URL_GET_AD_TWO, params, listener);
    }



    /**
     * 广告三--图片广告
     */
    public static final String URL_GET_AD_THREE = "http://210.14.151.100:8090/api/ad_v1/getImageAdList";
    public static void doHttpGetImageADInfo(Context context, OkHttpCallBack<AdThreeDataResponse> listener){
        Map<String, String> params = new HashMap<>();
        params.put("mac", MacUtils.getMacAddress(context));
        OkHttpUtils.getInstance().doHttpGet(URL_GET_AD_THREE, params, listener);
    }


    /**
     * 广告四--文字广告
     */
    public static final String URL_GET_AD_FOUR = "http://210.14.151.100:8090/api/ad_v1/getTextAdList";
    public static void doHttpGetTextADInfo(Context context, OkHttpCallBack<ADFourResponse> listener){
        Map<String, String> params = new HashMap<>();
        params.put("mac", MacUtils.getMacAddress(context));
        OkHttpUtils.getInstance().doHttpGet(URL_GET_AD_FOUR, params, listener);
    }


}
