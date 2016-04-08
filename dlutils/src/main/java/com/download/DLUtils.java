package com.download;

import android.content.Context;
import android.os.Message;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.download.dllistener.InDLUtils;
import com.download.dllistener.OnDownloadStatusListener;
import com.download.tools.LogCat;
import com.download.tools.ToolUtils;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by fq_mbp on 16/2/29.
 */
public class DLUtils implements InDLUtils{

    private DownloadHandler downLoadHandler;

    private static ExecutorService service;

    private DownloadPrepareThread downloadThread;

    private static Context context;

    private DLUtils() {
    }

    private static class DLUtilsHolder{
        private static  final DLUtils instance = new DLUtils();
    }


    public static DLUtils init(Context context) {
        SharedUtils.clear(context);
        DLUtils.context = context;
        service = Executors.newCachedThreadPool();
        return DLUtilsHolder.instance;
    }



    public void download(String path, String fileName, String downloadUrl, int threadNum, OnDownloadStatusListener listener) {
        if (downLoadHandler == null) {
            downLoadHandler = new DownloadHandler(context, listener);
        }
        // 正在下载当前任务则不处理
        if(isDownloading(fileName))
            return;

        // 记录当前下载任务
        SharedUtils.put(context, fileName);

        // sdcard 检查
        if(!ToolUtils.isExistSDCard()){
            sendHandlerMsg(ErrorCodes.ERROR_DOWNLOAD_SDCARD_USESLESS);
            return;
        }

        // 文件路径检查
        if (TextUtils.isEmpty(path)) {
            sendHandlerMsg(ErrorCodes.ERROR_DOWNLOAD_FILE_LOCAL);
            return;
        }

        // 获取SD卡路径
        File apkFile = createFile(path, fileName);

        threadNum = 2;
        LogCat.e("download file  path:" + apkFile.getAbsolutePath());

        // 如果还有线程在执行，则优先停止其他下载
        if(service != null){
            if(!service.isShutdown()){
                service.shutdownNow();
                service = Executors.newCachedThreadPool();
            }
        }

        downloadThread = new DownloadPrepareThread(service, downloadUrl, threadNum, apkFile, downLoadHandler);
        service.execute(downloadThread);

//        if(downloadThread != null){
//            downloadThread.cancelDownload();
//            downloadThread = null;
//        }
//
//        // 开启当前下载任务

//        downloadThread.start();

    }

    @NonNull
    private File createFile(String path, String fileName) {
        File file = new File(path);
        // 如果SD卡目录不存在创建
        if (!file.exists()) {
            file.mkdirs();
        }

        File apkFile = new File(path, fileName);
        if(!apkFile.exists()){
            try {
                apkFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return apkFile;
    }

    private boolean isDownloading(String fileName) {
        String preFileName = SharedUtils.getValue(context);
        if(!TextUtils.isEmpty(preFileName) && preFileName.equals(fileName)){
            Message msg = downLoadHandler.obtainMessage(DLUtils.HANDLER_WHAT_DOWNLOADING);
            msg.obj = fileName;
            downLoadHandler.sendMessage(msg);
            LogCat.e("***********当前下载正在执行************");
            return true;

        }
        return false;
    }


    private void sendHandlerMsg(int code){
        Message msg = downLoadHandler.obtainMessage(DLUtils.HANDLER_WHAT_DOWNLOAD_ERROR);
        msg.arg1 = code;
        downLoadHandler.sendMessage(msg);
    }


    /**
     * 检测是否正在下载
     * @param fileName
     * @return
     */
    public boolean downloading(String fileName){
        if(TextUtils.isEmpty(fileName)){
            return false;
        }
        String preFileName = SharedUtils.getValue(context);
        if(fileName.equals(preFileName)){
            return true;
        }else {
            return false;
        }
    }


    public void cancel() {
        if(service != null){
            if(!service.isShutdown()){
                LogCat.e("关闭线程池,,,,,,,,,");
                service.shutdownNow();
            }
        }
        if(downloadThread != null){
            downloadThread.cancelDownload();
        }
        SharedUtils.clear(context);

    }








}
