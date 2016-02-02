package com.httputils.http.service;

import android.content.Context;

import com.httputils.http.HttpUtils;
import com.httputils.http.OnRequestListener;
import com.httputils.http.response.CdnPathResponse;

import java.util.Map;

/**
 * Created by fq_mbp on 15/9/8.
 */
public class CDNHttpService {
    /**
     * 获取youtube地址
     */
    private final static String HTTP_URL_CDN_PATH = "http://cloudapi.vego.tv/decive-api/mobile/iv1/getCdnKey";

    /**
     * 首页的分类
     */
    public static void doHttpGetCdnPath(Context context, OnRequestListener<CdnPathResponse> listener) {
        HttpUtils.getInstance(context).doHttpGet(CdnPathResponse.class, HTTP_URL_CDN_PATH, listener, "cdn_path");
    }

    public static void cancleHttpService(Context context){
        HttpUtils.getInstance(context).cancelPendingRequests("cdn_path");
    }


    /**
     * cdn防盗链加密
     */

    private final static String HTTP_URL_CDN_PATH_EN = "http://h5api.ottcloud.tv/cdn-api/v1/getPlayUrl";
    public static void doHttpGetCdnPathEncryption(Context context, Map url, OnRequestListener<CdnPathResponse> listener) {
        HttpUtils.getInstance(context).doHttpGet(CdnPathResponse.class, HTTP_URL_CDN_PATH_EN, url, listener, "cdn_path_en");
    }


}
