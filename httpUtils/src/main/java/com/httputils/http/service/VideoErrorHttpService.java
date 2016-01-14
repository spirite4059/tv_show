package com.httputils.http.service;

import android.content.Context;

import com.httputils.http.HttpUtils;
import com.httputils.http.OnRequestListener;

import java.util.Map;

/**
 * Created by fq_mbp on 15/9/8.
 * 错误日志上报
 */
public class VideoErrorHttpService {
    /**
     * 正式
     */
    private final static String HTTP_URL_VIDEO_ERROR_REPORT = "http://na.uas.vego.tv/usrAction/errorLogServlet";
    /**
     * 测试
     */
//    private final static String HTTP_URL_VIDEO_ERROR_REPORT = "http://210.14.158.50:8090/usrAction/errorLogServlet";

    /**
     * 首页的分类
     */
    public static void doHttpYoutube(Context context, Map<String, String> map, OnRequestListener<String> listener) {
        HttpUtils.getInstance(context).doHttpPostString(HTTP_URL_VIDEO_ERROR_REPORT, map, listener, "video_error_report");
    }

    public static void cancleHttpService(Context context){
        HttpUtils.getInstance(context).cancelPendingRequests("video_error_report");
    }



}
