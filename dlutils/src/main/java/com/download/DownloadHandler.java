package com.download;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.download.dllistener.InDLUtils;
import com.download.dllistener.OnDownloadStatusListener;

import static com.download.ErrorCodes.ERROR_DOWNLOADING_READ;
import static com.download.ErrorCodes.ERROR_DOWNLOAD_BUFFER_IN;
import static com.download.ErrorCodes.ERROR_DOWNLOAD_CONN;
import static com.download.ErrorCodes.ERROR_DOWNLOAD_FILE_LOCAL;
import static com.download.ErrorCodes.ERROR_DOWNLOAD_FILE_NULL;
import static com.download.ErrorCodes.ERROR_DOWNLOAD_FILE_SIZE;
import static com.download.ErrorCodes.ERROR_DOWNLOAD_FILE_UNKNOWN;
import static com.download.ErrorCodes.ERROR_DOWNLOAD_RANDOM;
import static com.download.ErrorCodes.ERROR_DOWNLOAD_RANDOM_SEEK;
import static com.download.ErrorCodes.ERROR_DOWNLOAD_URL;
import static com.download.ErrorCodes.ERROR_DOWNLOAD_WRITE;
import static com.download.ErrorCodes.ERROR_THREAD_NUMBERS;

/**
 * Created by fq_mbp on 16/4/8.
 */
public class DownloadHandler extends Handler implements InDLUtils{

    private OnDownloadStatusListener onDownloadStatusListener;
    private Context context;

    public DownloadHandler(Context context, OnDownloadStatusListener onDownloadStatusListener) {
        this.context = context;
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
                SharedUtils.clear(context);
                onDownloadStatusListener.onError(msg.arg1, getErrorMsg(msg.arg1));
                break;
            case HANDLER_WHAT_DOWNLOAD_FINISH:
                SharedUtils.clear(context);
                onDownloadStatusListener.onFinish(String.valueOf(msg.obj));
                break;
            case HANDLER_WHAT_DOWNLOAD_CANCEL:
                SharedUtils.clear(context);
                onDownloadStatusListener.onCancel();
                break;
            case HANDLER_WHAT_DOWNLOADING:
                onDownloadStatusListener.onDownloading(String.valueOf(msg.obj));
                break;
        }


    }


    private String getErrorMsg(int errorCode) {
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
            case ERROR_DOWNLOAD_FILE_UNKNOWN:
                errorMsg = "error：下载文件大小出错";
                break;
            default:
                errorMsg = "error：未知的异常";
                break;
        }


        return errorMsg;
    }

}
