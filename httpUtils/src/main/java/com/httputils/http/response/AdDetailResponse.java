package com.httputils.http.response;

import java.util.ArrayList;

/**
 * Created by zfy on 2015/8/24.
 */
public class AdDetailResponse {

    public String adVideoName; // 视频名称
    public String name; // 视频名称



    public String tag; // 标签

    public String adVideoUrl;// 第三方视频播放地址


    public String videoPath;// 第三方视频播放地址

    public boolean isPresetPiece;// 第三方视频播放地址


    public ArrayList<PlayInfoResponse> playInfo = new ArrayList<PlayInfoResponse>();

    public int adVideoIndex;

}
