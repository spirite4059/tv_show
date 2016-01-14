package com.httputils.http.service;

import android.content.Context;

import com.httputils.http.HttpUtils;
import com.httputils.http.OnRequestListener;
import com.httputils.http.response.YoutubeUrlDataResponse;

import java.util.Map;

/**
 * Created by fq_mbp on 15/9/8.
 */
public class YoutubeHttpService {
    /**
     * 获取youtube地址
     */
    private final static String HTTP_URL_YOUTUBE = "http://api.vego.tv/h5/video_v1/videoDetail.json";

    /**
     * 首页的分类
     */
    public static void doHttpYoutube(Context context, Map<String, String> map, OnRequestListener<YoutubeUrlDataResponse> listener) {
        HttpUtils.getInstance(context).doHttpGet(YoutubeUrlDataResponse.class, HTTP_URL_YOUTUBE, map, listener, "youtube_url");
    }

    public static void cancleHttpService(Context context){
        HttpUtils.getInstance(context).cancelPendingRequests("youtube_url");
    }

}
