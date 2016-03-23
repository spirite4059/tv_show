package com.gochinatv.ad.tools;

import java.io.File;

/**
 * Created by fq_mbp on 16/3/16.
 */
public class Constants {


    /**
     * --------------------------SharePreference-------------------------------
     */
    public static final String SHARE_KEY_DURATION = "SHARE_KEY_DURATION";
    public static final String SHARE_KEY_SERVER_TIME = "SHARE_KEY_SERVER_TIME";

    /**
     * --------------------------File-------------------------------
     * 文件结构目录
     * VegoPlus
     *       --video                需要播放的视频文件
     *       --apk                  升级文件包
     *       --cache                缓存文件
     *       --screen_shot          屏幕截图
     *       --pre_download_video   预下载的视频文件
     *       --picture              图片文件
     *
     *
     */
    public static final String FILE_DIRECTORY = "VegoPlus" + File.separator;
    public static final String FILE_DIRECTORY_VIDEO = "video" + File.separator;
    public static final String FILE_DIRECTORY_APK = "apk" + File.separator;
    public static final String FILE_DIRECTORY_CACHE = "cache" + File.separator;
    public static final String FILE_DIRECTORY_SCREEN_SHOT = "screen_shot" + File.separator;
    public static final String FILE_DIRECTORY_PRE_VIDEO = "pre_download_video" + File.separator;
    public static final String FILE_DIRECTORY_PICTURE = "picture" + File.separator;
    public static final String FILE_SCREEN_SHOT_NAME = "screenShot.png";

    public static final String FILE_DOWNLOAD_EXTENSION = ".mp4";
    public static final String FILE_APK_NAME = "VegoPlus.apk";

    /**
     * --------------------------Intent-------------------------------
     */
    public static final String INTENT_RECEIVER_NET_STATUS = "INTENT_RECEIVER_NET_STATUS";


    /**
     * --------------------------other-------------------------------
     */

    public static final String FORMAT_VIDEO_AD_TIME = "yyyyMMddHHmmss";





}
