package com.gochinatv.statistics.tools;

/**
 * Created by ulplanet on 2016/5/30.
 */
public class Constant {

    public static String LOG_HTTP_URL = "http://api.bm.gochinatv.com/device_v1/uploadLog";//日志的url

    public static String LOG_TYPE_INITIALIZE = "initialize";//initialize:初始化

    public static String LOG_TYPE_PLAY = "play";//play:播放

    public static String LOG_TYPE_DOWNLOAD = "download";//download:下载视频

    public static String LOG_TYPE_UPGRADE = "upgrade";//upgrade:升级

    public static String LOG_TYPE_LAYOUT = "layout";//layout:布局

    public static String LOG_TYPE_APKDOWNLOAD = "APKDownload";//APKDownload:apk下载

    //开机时间
    public static final  int APP_START_TIME = 101;
    //文件下载时长
    public static final  int VIDEO_DOWNLOAD_TIME = 102;
    //视频播放次数
    public static final  int VIDEO_PLAY_TIMES = 103;
    //视频删除反馈
    public static final  int VIDEO_DELETE = 104;


}
