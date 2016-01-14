package com.httputils.http.service;

import android.content.Context;

import com.httputils.http.HttpUtils;
import com.httputils.http.OnRequestListener;
import com.httputils.http.response.TimeResponse;

/**
 * Created by zfy on 2015/8/18.
 */
public class TimeHttpService {

    /**
     * 升级接口
     */
    public static final String URL_GET_SERVER_TIMELONG = "http://cloudapi.vego.tv/device-tv/sys/time";


    public static void doHttpGetTime(Context context, OnRequestListener<TimeResponse> listener) {
        HttpUtils.getInstance(context).doHttpGet(TimeResponse.class, URL_GET_SERVER_TIMELONG, listener, "update");
    }


}
