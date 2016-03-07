package com.gochinatv.ad.download;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.gochinatv.ad.tools.LogCat;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import static com.gochinatv.ad.download.ErrorCodes.HTTP_OK;
import static com.gochinatv.ad.download.ErrorCodes.HTTP_PARTIAL;

/**
 * Created by fq_mbp on 16/2/29.
 */
public class DownloadPrepareThread extends Thread {
    private String downloadUrl;// 下载链接地址
    private int threadNum;// 开启的线程数
    private String filePath;// 保存文件路径地址
    private Handler mHandler;
    private int errorCode;
    private Bundle bundle;
    private boolean isCancel;


    public DownloadPrepareThread(String downloadUrl, int threadNum, String filePath, Handler mHandler) {
        this.downloadUrl = downloadUrl;
        this.threadNum = threadNum;
        this.filePath = filePath;
        this.mHandler = mHandler;
        errorCode = 0;
    }

    @Override
    public void run() {
        if(isCancel){
            return;
        }

        if (mHandler == null) {
            return;
        }

        if(isCancel){
            return;
        }

        if (threadNum < 0) {
            setErrorMsg(ErrorCodes.ERROR_THREAD_NUMBERS);
            return;
        }

        if(isCancel){
            return;
        }

        DownloadThread[] threads = new DownloadThread[threadNum];
        if (threads.length == 0) {
            setErrorMsg(ErrorCodes.ERROR_THREAD_NUMBERS);
            return;
        }

        if(isCancel){
            return;
        }

        URL url = null;
        try {
            url = new URL(downloadUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            errorCode = ErrorCodes.ERROR_DOWNLOAD_URL;
        }
        if(isCancel){
            return;
        }
        if (url == null || errorCode != 0) {
            setErrorMsg(ErrorCodes.ERROR_DOWNLOAD_URL);
            return;
        }

        if(isCancel){
            return;
        }


        HttpURLConnection connection = getHttpURLConnection(url);
        // 如果是请求文件体的conn出错，要继续访问3边，无需重新进行操作
        if (connection == null || errorCode == ErrorCodes.ERROR_DOWNLOAD_CONN) {
            int errorConnTimes = 0;
            while (errorConnTimes < 3){
                if(isCancel){
                    return;
                }
                connection = getHttpURLConnection(url);
                if(connection != null){
                    break;
                }
                errorConnTimes += 1;
            }

            if(errorConnTimes == 3){
                setErrorMsg(ErrorCodes.ERROR_DOWNLOAD_CONN);
                return;
            }
        }

        if(isCancel){
            return;
        }
        boolean isConnectSuccess = false;
        try {
            final int code = connection.getResponseCode();
            if (code == HTTP_OK || code == HTTP_PARTIAL) {
                isConnectSuccess = true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(isCancel){
            return;
        }

        if(!isConnectSuccess){
            setErrorMsg(ErrorCodes.ERROR_DOWNLOAD_CONN);
            return;
        }

        if(isCancel){
            return;
        }

        // 读取下载文件总大小
        int fileSize = connection.getContentLength();


        if(isCancel){
            return;
        }


        if (fileSize <= 0) {
            errorCode = ErrorCodes.ERROR_DOWNLOAD_FILE_SIZE;
            setErrorMsg(ErrorCodes.ERROR_DOWNLOAD_FILE_SIZE);
            return;
        }

        if(isCancel){
            return;
        }

        LogCat.e("fileSize: " + fileSize);
        setDownloadMsg(DLUtils.HANDLER_WHAT_FILE_SIZE, fileSize);

        if(isCancel){
            return;
        }
        // 计算每条线程下载的数据长度
        int blockSize = (fileSize % threadNum) == 0 ? fileSize / threadNum
                : fileSize / threadNum + 1;


        if(isCancel){
            return;
        }


        if (TextUtils.isEmpty(filePath)) {
            errorCode = ErrorCodes.ERROR_DOWNLOAD_FILE_LOCAL;
            setErrorMsg(ErrorCodes.ERROR_DOWNLOAD_FILE_LOCAL);
            return;
        }

        if(isCancel){
            return;
        }

        File file = new File(filePath);

        if(isCancel){
            return;
        }

        int size = threads.length;
        for (int i = 0; i < size; i++) {
            // 启动线程，分别下载每个线程需要下载的部分
            threads[i] = new DownloadThread(url, file, blockSize,
                    (i + 1));
            threads[i].setName("Thread:" + i);
            threads[i].start();
        }

        boolean isFinished = false;

        int threadErrorCode;

        if(isCancel){
            return;
        }

        try{
            while (!isFinished) {
                isFinished = true;
                int downloadedAllSize = 0;
                boolean isThreadError = false;
                // 当前所有线程下载总量
                for (DownloadThread downloadThread : threads) {
                    if (downloadThread != null) {
                        if(isCancel){
                            downloadThread.cancel();
                            continue;
                        }
                        threadErrorCode = downloadThread.getErrorCode();
                        if (threadErrorCode != 0) {
                            // 线程出错了, 中断下载
                            isThreadError = true;
                            setErrorMsg(ErrorCodes.ERROR_DOWNLOAD_INTERRUPT);
                            break;
                        } else {
                            if (!downloadThread.isCompleted()) {
                                isFinished = false;
                                downloadedAllSize += downloadThread.getDownloadLength();
                            }else {
                                downloadedAllSize += downloadThread.getDownloadLength();
                            }
                        }
                    }
                }


                // 是否有子线程出错或者取消下载
                if (isThreadError || isCancel) {
                    if(isThreadError){
                        LogCat.e("下载线程出错......");
                    }else {
                        LogCat.e("主动取消了下载......");
                    }

                    break;
                }

                // 通知handler去更新视图组件
                setDownloadMsg(DLUtils.HANDLER_WHAT_DOWNLOAD_FILE_SIZE, downloadedAllSize);

                Thread.sleep(1000);// 休息1秒后再读取下载进度

            }
        }catch (Exception e){
            e.printStackTrace();
        }


        // 完成所有的下载了
        if (isFinished) {
            Message msg = mHandler.obtainMessage(DLUtils.HANDLER_WHAT_DOWNLOAD_FINISH);
            mHandler.sendMessage(msg);
        }
    }

    private HttpURLConnection getHttpURLConnection(URL url) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(10000);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, application/xaml+xml, application/vnd.ms-xpsdocument, application/x-ms-xbap, application/x-ms-application, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*");
            connection.setRequestProperty("Accept-Language", "zh-CN");
            connection.setRequestProperty("Referer", downloadUrl);
            connection.setRequestProperty("Charset", "UTF-8");
            connection.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.2; Trident/4.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)");
            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setReadTimeout(10000);
            connection.connect();
        } catch (IOException e) {
            e.printStackTrace();
            errorCode = ErrorCodes.ERROR_DOWNLOAD_CONN;
        }
        return connection;
    }


    private void setDownloadMsg(int type, long code) {
        if (mHandler != null) {
            Message msg = mHandler.obtainMessage(type);
            if(bundle == null){
                bundle = new Bundle();
            }
            if(type == DLUtils.HANDLER_WHAT_FILE_SIZE){
                bundle.putLong(DLUtils.BUNDLE_KEY_FILE_LENGTH, code);
            }else {
                bundle.putLong(DLUtils.BUNDLE_KEY_FILE_DOWNLOAD_SIZE, code);
            }

            msg.setData(bundle);
            mHandler.sendMessage(msg);
        }
    }

    private void setErrorMsg(int errorCode) {
        if (mHandler != null) {
            Message msg = mHandler.obtainMessage(DLUtils.HANDLER_WHAT_DOWNLOAD_ERROR);
            msg.arg1 = errorCode;
            mHandler.sendMessage(msg);
        }
    }


    public void cancelDownload(){
        isCancel = true;
    }
}