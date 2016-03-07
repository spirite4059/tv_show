package com.gochinatv.ad.download;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.gochinatv.ad.tools.LogCat;

import java.io.File;

import static com.gochinatv.ad.download.ErrorCodes.ERROR_DOWNLOADING_READ;
import static com.gochinatv.ad.download.ErrorCodes.ERROR_DOWNLOAD_BUFFER_IN;
import static com.gochinatv.ad.download.ErrorCodes.ERROR_DOWNLOAD_CONN;
import static com.gochinatv.ad.download.ErrorCodes.ERROR_DOWNLOAD_FILE_LOCAL;
import static com.gochinatv.ad.download.ErrorCodes.ERROR_DOWNLOAD_FILE_NULL;
import static com.gochinatv.ad.download.ErrorCodes.ERROR_DOWNLOAD_FILE_SIZE;
import static com.gochinatv.ad.download.ErrorCodes.ERROR_DOWNLOAD_RANDOM;
import static com.gochinatv.ad.download.ErrorCodes.ERROR_DOWNLOAD_RANDOM_SEEK;
import static com.gochinatv.ad.download.ErrorCodes.ERROR_DOWNLOAD_URL;
import static com.gochinatv.ad.download.ErrorCodes.ERROR_DOWNLOAD_WRITE;
import static com.gochinatv.ad.download.ErrorCodes.ERROR_THREAD_NUMBERS;

/**
 * Created by fq_mbp on 16/2/29.
 */
public class DLUtils {
    // 文件总大小
    public static final int HANDLER_WHAT_FILE_SIZE = 0;
    // 当前文件的下载大小
    public static final int HANDLER_WHAT_DOWNLOAD_FILE_SIZE = 1;
    // 下载出错
    public static final int HANDLER_WHAT_DOWNLOAD_ERROR = 2;
    // 下载完成
    public static final int HANDLER_WHAT_DOWNLOAD_FINISH = 3;
    // 下载出错
    public static final String BUNDLE_KEY_FILE_LENGTH = "BUNDLE_KEY_FILE_LENGTH";

    public static final String BUNDLE_KEY_FILE_DOWNLOAD_SIZE = "BUNDLE_KEY_FILE_DOWNLOAD_SIZE";

    public static DLUtils instances;

    private static DownLoadHandler downLoadHandler;

    private DownloadPrepareThread downloadThread;


    private static class DownLoadHandler extends Handler {

        private OnDownloadStatusListener onDownloadStatusListener;

        public void setOnDownloadStatusListener(OnDownloadStatusListener onDownloadStatusListener) {
            this.onDownloadStatusListener = onDownloadStatusListener;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (onDownloadStatusListener == null) {
                return;
            }
            Bundle bundle = msg.getData();

            switch (msg.what) {
                case HANDLER_WHAT_FILE_SIZE:
                    if (bundle != null) {
                        onDownloadStatusListener.onPrepare(bundle.getLong(BUNDLE_KEY_FILE_LENGTH, 0));
                    }
                    break;
                case HANDLER_WHAT_DOWNLOAD_FILE_SIZE:
                    if (bundle != null) {
                        onDownloadStatusListener.onProgress(bundle.getLong(BUNDLE_KEY_FILE_DOWNLOAD_SIZE, 0));
                    }
                    break;
                case HANDLER_WHAT_DOWNLOAD_ERROR:
                    //

                    onDownloadStatusListener.onError(msg.arg1, getErrorMsg(msg.arg1));
                    break;
                case HANDLER_WHAT_DOWNLOAD_FINISH:
                    onDownloadStatusListener.onFinish();
                    break;
            }


        }
    }

    private DLUtils() {
    }

    public static DLUtils init() {
        if (instances == null) {
            instances = new DLUtils();
        }
        return instances;
    }


    public void download(String path, String fileName, String downloadUrl, int threadNum, OnDownloadStatusListener listener) {
        if (downLoadHandler == null) {
            downLoadHandler = new DownLoadHandler();
        }
        downLoadHandler.setOnDownloadStatusListener(listener);

        if (TextUtils.isEmpty(path)) {
            Message msg = downLoadHandler.obtainMessage(DLUtils.HANDLER_WHAT_DOWNLOAD_ERROR);
            msg.arg1 = ErrorCodes.ERROR_DOWNLOAD_FILE_LOCAL;
            downLoadHandler.sendMessage(msg);
            return;
        }

        // 获取SD卡路径
        File file = new File(path);
        // 如果SD卡目录不存在创建
        if (!file.exists()) {
            file.mkdir();
        }
        // 简单起见，我先把URL和文件名称写死，其实这些都可以通过HttpHeader获取到
//        downloadUrl = "http://gdown.baidu.com/data/wisegame/91319a5a1dfae322/baidu_16785426.apk";
//        fileName = "baidu_16785426.apk";
        threadNum = 2;
        String filePath = path + fileName;
        LogCat.e("download file  path:" + filePath);

        downloadThread = new DownloadPrepareThread(downloadUrl, threadNum, filePath, downLoadHandler);
        downloadThread.start();

    }


    public void cancel() {
        if (downloadThread != null) {
            downloadThread.cancelDownload();
        }
    }


    private static String getErrorMsg(int errorCode) {
        String errorMsg = null;
        switch (errorCode) {
            case ERROR_DOWNLOAD_WRITE:
                errorMsg = "error：输入流写入文件过程出错";
                break;
            case ERROR_DOWNLOADING_READ:
                errorMsg = "error：读取文件流过程出错";
                break;
            case ERROR_THREAD_NUMBERS:
                errorMsg = "error：多线程的线程数小于0";
                break;
            case ERROR_DOWNLOAD_URL:
                errorMsg = "error：下载地址URL生成出错或null";
                break;
            case ERROR_DOWNLOAD_CONN:
                errorMsg = "error：URL的链接过程出错或null";
                break;
            case ERROR_DOWNLOAD_FILE_SIZE:
                errorMsg = "error：下载文件的大小异常";
                break;
            case ERROR_DOWNLOAD_FILE_LOCAL:
                errorMsg = "error：下载文件的存储地址异常";
                break;
            case ERROR_DOWNLOAD_FILE_NULL:
                errorMsg = "error：无法生成下载文件";
                break;
            case ERROR_DOWNLOAD_BUFFER_IN:
                errorMsg = "error：输入流异常或null";
                break;
            case ERROR_DOWNLOAD_RANDOM:
                errorMsg = "error：随机流异常或null";
                break;
            case ERROR_DOWNLOAD_RANDOM_SEEK:
                errorMsg = "error：随机流定为异常";
                break;
            default:
                errorMsg = "error：未知的异常";
                break;
        }


        return errorMsg;
    }


}
