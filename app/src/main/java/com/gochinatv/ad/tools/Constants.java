package com.gochinatv.ad.tools;

import java.io.File;

import wseemann.media.FFmpegMediaMetadataRetriever;

/**
 * Created by fq_mbp on 16/3/16.
 */
public class Constants {


    /**
     * --------------------------SharePreference-------------------------------
     */
    public static final String SHARE_KEY_DURATION = "SHARE_KEY_DURATION";
    public static final String SHARE_KEY_SERVER_TIME = "SHARE_KEY_SERVER_TIME";
    public static final String SHARE_KEY_UMENG = "SHARE_KEY_UMENG";
    public static final String SHARE_KEY_MAC = "SHARE_KEY_MAC";

    /**
     * --------------------------File-------------------------------
     * 文件结构目录
     * VegoPlus
     *       --video                需要播放的视频文件
     *       --apk                  升级文件包
     *       --cache                缓存文件
     *       --screenShot          屏幕截图
     *       --prepareVideo   预下载的视频文件
     *       --picture              图片文件
     *
     *
     */
    public static final String FILE_DIRECTORY = "VegoPlus" + File.separator;
    public static final String FILE_OLD_DIRECTORY = "gochinatv" + File.separator;
    public static final String FILE_DIRECTORY_VIDEO = "video" + File.separator;
    public static final String FILE_DIRECTORY_APK = "apkFile" + File.separator;
    public static final String FILE_DIRECTORY_CACHE = "cache" + File.separator;
    public static final String FILE_DIRECTORY_SCREEN_SHOT = "screenShot" + File.separator;
    public static final String FILE_DIRECTORY_PRE_VIDEO = "prepareVideo" + File.separator;
    public static final String FILE_DIRECTORY_PICTURE = "picture" + File.separator;
    public static final String FILE_SCREEN_SHOT_NAME = "screenShot.png";
    public static final String FILE_CACHE_NAME = "cacheVideo.json";
    public static final String FILE_CACHE_TD_NAME = "cacheTodayVideo.json";

    public static final String FILE_DOWNLOAD_EXTENSION = ".mp4";
    public static final String FILE_APK_NAME = "VegoPlus.apk";

    /**
     * --------------------------Intent-------------------------------
     */
    public static final String INTENT_RECEIVER_NET_STATUS = "INTENT_RECEIVER_NET_STATUS";


    /**
     * --------------------------other-------------------------------是方法三
     */

    public static final String FORMAT_VIDEO_AD_TIME = "yyyyMMddHHmmss";

    public static final String PRESET_PIECE = "预置片";


    /**
     * --------------------------是否是静默安装-------------------------------
     */
    public static boolean isClientInstall;


    /**
     * --------------------------是否是测试-------------------------------
     */
    public static boolean isTest = false;



    /**
     * --------------------------是否是3号位测试-------------------------------
     */
    public static boolean isImageTest = false;


    /**
     * --------------------------是否是适配手机-------------------------------
     */
    public static boolean isPhone = false;





    public static final String [] METADATA_KEYS = {
            FFmpegMediaMetadataRetriever.METADATA_KEY_ALBUM,
            FFmpegMediaMetadataRetriever.METADATA_KEY_ALBUM_ARTIST,
            FFmpegMediaMetadataRetriever.METADATA_KEY_ARTIST,
            FFmpegMediaMetadataRetriever.METADATA_KEY_COMMENT,
            FFmpegMediaMetadataRetriever.METADATA_KEY_COMPOSER,
            FFmpegMediaMetadataRetriever.METADATA_KEY_COPYRIGHT,
            FFmpegMediaMetadataRetriever.METADATA_KEY_CREATION_TIME,
            FFmpegMediaMetadataRetriever.METADATA_KEY_DATE,
            FFmpegMediaMetadataRetriever.METADATA_KEY_DISC,
            FFmpegMediaMetadataRetriever.METADATA_KEY_ENCODER,
            FFmpegMediaMetadataRetriever.METADATA_KEY_ENCODED_BY,
            FFmpegMediaMetadataRetriever.METADATA_KEY_FILENAME,
            FFmpegMediaMetadataRetriever.METADATA_KEY_GENRE,
            FFmpegMediaMetadataRetriever.METADATA_KEY_LANGUAGE,
            FFmpegMediaMetadataRetriever.METADATA_KEY_PERFORMER,
            FFmpegMediaMetadataRetriever.METADATA_KEY_PUBLISHER,
            FFmpegMediaMetadataRetriever.METADATA_KEY_SERVICE_NAME,
            FFmpegMediaMetadataRetriever.METADATA_KEY_SERVICE_PROVIDER,
            FFmpegMediaMetadataRetriever.METADATA_KEY_TITLE,
            FFmpegMediaMetadataRetriever.METADATA_KEY_TRACK,
            FFmpegMediaMetadataRetriever.METADATA_KEY_VARIANT_BITRATE,
            FFmpegMediaMetadataRetriever.METADATA_KEY_DURATION,
            FFmpegMediaMetadataRetriever.METADATA_KEY_AUDIO_CODEC,
            FFmpegMediaMetadataRetriever.METADATA_KEY_VIDEO_CODEC,
            FFmpegMediaMetadataRetriever.METADATA_KEY_ICY_METADATA,
            FFmpegMediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION,
            FFmpegMediaMetadataRetriever.METADATA_KEY_FRAMERATE,
            FFmpegMediaMetadataRetriever.METADATA_KEY_CHAPTER_START_TIME,
            FFmpegMediaMetadataRetriever.METADATA_KEY_CHAPTER_END_TIME,
            FFmpegMediaMetadataRetriever.METADATA_CHAPTER_COUNT,
            FFmpegMediaMetadataRetriever.METADATA_KEY_FILESIZE
    };

 


}
