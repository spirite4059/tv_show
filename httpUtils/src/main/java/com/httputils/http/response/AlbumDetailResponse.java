package com.httputils.http.response;

import java.io.Serializable;

/**
 * Created by zfy on 2015/8/21.
 */
public class AlbumDetailResponse implements Serializable {



    /**
     *
     */
    private static final long serialVersionUID = -7333061017514564944L;

    //public int _id; // 12699,

    /******************************** 以下是新接口的字段******************************/
    public String ischarge; // 是否需要付费：0-不需要，1-需要

    public String aid; // 12699,专辑id

    public String name; // 我是歌手 第三季,专辑名称

    public String channelId;//频道id

    //public String terminalId;

    public String channelName;//频道name

    public String englishname;// 英文名称

    public String language;// 语言,

    public String areaId;//地区id

    public String areaName;//地区name

    //public String category; // 200,

    //public String othername; // null,

    //public String subname;// ,

    //public String area;// ,

    public String description;//描述   /2014年11月，第一位加盟《我是歌手》的明星身份确认，歌手孙楠已接受邀请。总制片人洪涛表示，“歌手舞台所呈现的音乐品质，还有他即将要对阵的多位天王天后级歌手，所带来的音乐竞技感和挑战性，让他答应了我们的邀请。”目前韩红，张靓颖，孙楠，胡彦斌，古巨基，ALin-黄丽玲，陈洁仪，已经确认参加《我是歌手》第三季。,

    public String tag;// 湖南电视台,歌手,对决,//标签

    public String episodes;// 剧集数

    //public String votes;// 0,

    public String copyrightName;// 版权方

    public String isend;// 0，是否完结

    public String isdisplay;// 1,是否上映

    public String directory;//导演

    public String starring;//主演

    public String standardPic;// 封面http;////img7.gochinatv.com/common/album/20150104/3b576557f438cfd72e562e3445c4d2c7_original.jpg,

    public String ystandardPic;//专辑竖图 http;////img9.gochinatv.com/common/yalbum/20150104/65eae639d91e7123cb5de978f5307b6b_original.jpg,

    //public ArrayList<String> categoryIds;
    public String categoryName;//类别名  //"青春,家庭,军旅,言情,剧情,偶像"

    public String displayOrder;//展示id // 3223,

    public String host;//主持人

    public String showtimes;//上映时间

    public String isUpdate;//是否更新  // 0,

    public String updateFre;//更新频率

    public String age;//年代  // 2015,

    /**
     * 1剧集（电视剧等）,2期数（综艺）,3通用（电影）
     */
    public String serialType;//专辑类型

    public String score;//评分

    public String television;//电视台
    public String guest;//嘉宾

    public String destination;// 目的地

    public String createTime;// 创建时间,

    public String modifyTime;// 修改时间,

    public String newVideoId;// 最新视频ID,

    public String oldVideoId;//最旧视频ID

    public String newVideoInstallments;// 20150109,最新视频剧集

    public String oldVideoInstallments;// 20140102,
    /******************************** 以上是新接口的字段******************************/

    public String postOrgin;// null,

    public String albumType;// 音乐,

    public String albumTypeName;// 音乐,

    public String albumKind;// 1,


    public String newVideoName;// 20150109 张靓颖力压韩红狂野逆袭,

    public String newVideoPostPic;// ,

    public String newVideoImg;// http;////img8.gochinatv.com/common/video/20150116/780b2bb67e99f912ac81e8facb194e9b_original.jpg,


    /**
     * 当前播放剧集数
     */
    public String installment;// 20150109,

    public String newVideoMtime;// 1421375640,

    public String newVideoYvid;// hGKUaSXE0hs,

    public String platform;// null,

    public String albumContent;// ,

    public String broadcastTime;// ,


    public String nearUpdateTime;// 1421375640,



    public String columnName;// null,

    public String img;// null,

    public String newImg;// null,

    public String url;// http;////www.youtube.com/watch?v=hGKUaSXE0hs,

    public String videoName;// null,

    public String update;// null,



    public String ystandar1;// http;////img9.gochinatv.com/common/yalbum/20150104/65eae639d91e7123cb5de978f5307b6b_360x480.jpg,
    public String ystandar2;// http;////img9.gochinatv.com/common/yalbum/20150104/65eae639d91e7123cb5de978f5307b6b_455x930.jpg,
    public String ystandar3;// http;////img9.gochinatv.com/common/yalbum/20150104/65eae639d91e7123cb5de978f5307b6b_550x735.jpg,
    public String firstVideoInstallments;// 20140102,

    public String roleName;// 男学生,男白领,男自由人,女学生,女白领,女自由人


    public boolean startScroll;
    /**
     * 当前播放时间
     */
    public String playDuration;// null,

    public String ageLevelDesc;//年龄段

    //public String terminal;

    public boolean isStartScroll() {
        return startScroll;
    }

    public void setStartScroll(boolean startScroll) {
        this.startScroll = startScroll;
    }

    /**
     * 朱丰源添加--2015-04-18
     */
    private int section;
    private String insertTime;

    public String getInsertTime() {
        return insertTime;
    }

    public void setInsertTime(String insertTime) {
        this.insertTime = insertTime;
    }

    public int getSection() {
        return section;
    }

    public void setSection(int section) {
        this.section = section;
    }

    /** vrs中的视频的id */
    public String vid;




    public String videoFlag;


















}
