package com.download.dllistener;

/**
 * Created by fq_mbp on 16/4/8.
 */
public interface InDLUtils {

    // 文件总大小
    public static final int HANDLER_WHAT_FILE_SIZE = 0;
    // 当前文件的下载大小
    public static final int HANDLER_WHAT_DOWNLOAD_FILE_SIZE = 1;
    // 下载出错
    public static final int HANDLER_WHAT_DOWNLOAD_ERROR = 2;
    // 下载完成
    public static final int HANDLER_WHAT_DOWNLOAD_FINISH = 3;

    public static final int HANDLER_WHAT_DOWNLOAD_CANCEL = 4;

    public static final int HANDLER_WHAT_DOWNLOADING = 5;
    // 下载出错
    public static final String BUNDLE_KEY_FILE_LENGTH = "BUNDLE_KEY_FILE_LENGTH";

    public static final String BUNDLE_KEY_FILE_DOWNLOAD_SIZE = "BUNDLE_KEY_FILE_DOWNLOAD_SIZE";

}
