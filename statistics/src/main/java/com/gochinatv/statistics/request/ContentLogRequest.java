package com.gochinatv.statistics.request;

/**
 * Created by zfy on 2016/5/4.
 */
public class ContentLogRequest {
    public String mac;
    public String type;//initialize:初始化；play:播放；download:下载；upgrade:升级;layout:布局接口; APKDownload:APK下载
    public Object content;
}
