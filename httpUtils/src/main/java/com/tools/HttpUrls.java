package com.tools;

/**
 * Created by fq_mbp on 16/3/14.
 */
public class HttpUrls {

    /**
     * 新接口
     */
    private final static String NEW_BASE_HTTP_URL = "http://api.vego.tv";

    /**
     * 升级接口
     */
    public static final String URL_CHECK_UPDATE = "http://apk.gochinatv.com/api/queryApkUpdateVersion";

    /**
     * 视频列表,
     */
    public static final String URL_VIDEO_LIST = NEW_BASE_HTTP_URL + "/video_v1/videoList";

    public static final String URL_VIDEO_LIST_TEST = "http://210.14.158.187" + "/video_v1/videoList";


    /**
     * 点播地址
     */
    public static final String SECURITY_CHAIN_URL = "http://vod.vegocdn.com";


    /**
     * cdn防盗链加密
     */

    public final static String HTTP_URL_CDN_PATH = "http://h5api.ottcloud.tv/cdn-api/v1/getPlayUrl";

    /**
     * 获取时间接口
     */
    public static final String URL_GET_SERVER_TIMELONG = "http://cloudapi.vego.tv/device-tv/sys/time";


    /**
     * 广告三
     */
    public static final String URL_GET_AD_THREE = "http://210.14.151.100:8090/api/ad_v1/getImageAdList";


    /**
     * 广告三
     */
    public static final String URL_SCREEN_SHOT = "http://api.bm.gochinatv.com/api/device_v1/uploadImage";
}
