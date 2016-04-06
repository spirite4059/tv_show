package com.download;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.download.dllistener.OnDownloadStatusListener;
import com.download.tools.LogCat;

import java.io.File;

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
    public static final int HANDLER_WHAT_DOWNLOAD_CANCEL = 4;
    public static final int HANDLER_WHAT_DOWNLOADING = 5;
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
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("fileName", "");
                    editor.commit();
                    onDownloadStatusListener.onError(msg.arg1, getErrorMsg(msg.arg1));

                    break;
                case HANDLER_WHAT_DOWNLOAD_FINISH:
                    SharedPreferences.Editor editor1 = sharedPreferences.edit();
                    editor1.putString("fileName", "");
                    editor1.commit();
                    onDownloadStatusListener.onFinish(String.valueOf(msg.obj));

                    break;
                case HANDLER_WHAT_DOWNLOAD_CANCEL:
                    SharedPreferences.Editor editor2 = sharedPreferences.edit();
                    editor2.putString("fileName", "");
                    editor2.commit();
                    onDownloadStatusListener.onCancel();

                    break;
                case HANDLER_WHAT_DOWNLOADING:
                    onDownloadStatusListener.onDownloading(String.valueOf(msg.obj));
                    break;
            }


        }
    }

    private DLUtils() {
    }

    static SharedPreferences sharedPreferences;
    public static DLUtils init(Context context) {
        if (instances == null) {
            instances = new DLUtils();
            sharedPreferences =  context.getSharedPreferences("dlutils", Activity.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("fileName", "");
            editor.commit();
        }
        return instances;
    }


    public boolean downloading(String fileName){
        if(TextUtils.isEmpty(fileName)){
            return false;
        }
        String preFileName = sharedPreferences.getString("fileName", "");
        if(fileName.equals(preFileName)){
            return true;
        }else {
            return false;
        }
    }


    public void download(String path, String fileName, String downloadUrl, int threadNum, OnDownloadStatusListener listener) {



        if (downLoadHandler == null) {
            downLoadHandler = new DownLoadHandler();
            downLoadHandler.setOnDownloadStatusListener(listener);
        }




        String preFileName = sharedPreferences.getString("fileName", "");

        if(!TextUtils.isEmpty(preFileName) && preFileName.equals(fileName)){
            Message msg = downLoadHandler.obtainMessage(DLUtils.HANDLER_WHAT_DOWNLOADING);
            msg.obj = fileName;
            downLoadHandler.sendMessage(msg);
            LogCat.e("***********当前下载正在执行************");
            return;
        }

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("fileName", fileName);
        editor.commit();


        // sdcard 检查
        if(!isExistSDCard()){
            Message msg = downLoadHandler.obtainMessage(DLUtils.HANDLER_WHAT_DOWNLOAD_ERROR);
            msg.arg1 = ErrorCodes.ERROR_DOWNLOAD_SDCARD_USESLESS;
            downLoadHandler.sendMessage(msg);
            return;
        }



        // 文件路径检查
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
            file.mkdirs();
        }
        // 简单起见，我先把URL和文件名称写死，其实这些都可以通过HttpHeader获取到
//        downloadUrl = "http://gdown.baidu.com/data/wisegame/91319a5a1dfae322/baidu_16785426.apk";
//        fileName = "baidu_16785426.apk";
        threadNum = 2;
        String filePath = path + fileName;
        LogCat.e("download file  path:" + filePath);
        if(downloadThread != null){
            downloadThread.cancelDownload();
            downloadThread = null;
        }
        downloadThread = new DownloadPrepareThread(downloadUrl, threadNum, filePath, downLoadHandler);
        downloadThread.start();

    }


    public void cancel() {
        if (downloadThread != null) {
            downloadThread.cancelDownload();

        }
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("fileName", "");
        editor.commit();
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
            case ERROR_DOWNLOAD_FILE_UNKNOWN:
                errorMsg = "error：下载文件大小出错";
                break;
            default:
                errorMsg = "error：未知的异常";
                break;
        }


        return errorMsg;
    }


    /**
     * 判断是否存在sdcard
     *
     * @return
     */
    private boolean isExistSDCard() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return true;
        } else
            return false;
    }



}
