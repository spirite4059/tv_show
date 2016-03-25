package com.httputils.http.response;

import java.util.ArrayList;

/**
 * Created by zfy on 2015/8/24.
 */
public class VideoDetailResponse  {

    public String aid; // 专辑ID

    public String vid; // 视频Id

    public String name; // 视频名称

    public String aname; // 专辑名称

    public String tag; // 标签

    public int vedioType; // 1，正片，2片花、3预告片,

//    public int isdisplay;// 是否上线

//    public long createTime;// 创建时间

    public String description;// 描述

    // public int duration;// 时长

    public String installments;// 期数

//    public long modifyTime;// 修改时间

//    public String videoName;// 视频名称

//    public String standardPic;// 截图地址

//    public String subname;// 副标题

    public String ytbVid;// 第三方视频Id

//    public String videoMarked;//

    public String url;// 第三方视频播放地址

//    public String displayOrder;// 显示顺序

//    public String sort;// 排序

//    public int viewCount;// 观看数量

//    public int likeCount;// 喜欢数量

//    public int dislikeCount;// 不喜欢数量


    public String videoPath;// 第三方视频播放地址
    public boolean isPresetPiece;// 第三方视频播放地址


    public ArrayList<PlayInfoResponse> playInfo = new ArrayList<PlayInfoResponse>();



    public int index;

    public boolean isUseLocalUrl;

    public String localUrl;


    public boolean isDownloading;// 第三方视频播放地址





}
