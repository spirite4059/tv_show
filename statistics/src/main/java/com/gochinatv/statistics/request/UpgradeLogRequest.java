package com.gochinatv.statistics.request;

import java.util.ArrayList;

/**
 * 升级
 * Created by zfy on 2016/5/4.
 */
public class UpgradeLogRequest {


    public String mac;
    public String versionCode;
    public String versionName;
    public String sdk;
    public String isNeedUpdate;//1：升级，0：不升级

    public String isGetUpdateInfo;//1：接口请求成功，0：接口请求不成

    public ArrayList<RetryErrorRequest> interfaceError;//接口请求错误信息

    public String isDownloadSuccess;// 1：下载成功，0：下载不成功

    public ArrayList<RetryErrorRequest> downloadError;//下载出错信息，包括重试次数和错误原因

    public String isUpdateSuccess;//升级是否成功
}
