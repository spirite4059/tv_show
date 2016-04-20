package com.download;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.download.dllistener.InDLUtils;
import com.download.dllistener.OnDownloadStatusListener;
import com.download.tools.LogCat;
import com.download.tools.ToolUtils;

import java.io.File;
import java.io.IOException;

/**
 * Created by fq_mbp on 16/2/29.
 */
public class DLUtils implements InDLUtils {

    private static DLUtils instance;

    private DownloadPrepareThread downloadThread;

    private String downloadingPath;

    private Context context;

    private DLUtils(Context context) {
        this.context = context;
        downloadingPath = null;
    }


    public static DLUtils init(Context context) {
        if(instance == null){
            synchronized (DLUtils.class){
                if(instance == null){
                    instance = new DLUtils(context);
                }
            }
        }
        return instance;
    }


    public void download(boolean isToday, String path, String fileName, String downloadUrl, int threadNum, OnDownloadStatusListener listener) {
        if(!TextUtils.isEmpty(downloadingPath) && downloadingPath.equals(path +fileName)){
            LogCat.e("正在下载当前任务则不处理......");
            return;
        }

        this.downloadingPath = path + fileName;
        // 记录当前下载任务
//        SharedUtils.put(context, fileName);

        // sdcard 检查
        if (!ToolUtils.isExistSDCard()) {
            listener.onError(ErrorCodes.ERROR_DOWNLOAD_SDCARD_USESLESS);
            return;
        }

        // 文件路径检查
        if (TextUtils.isEmpty(path)) {
            listener.onError(ErrorCodes.ERROR_DOWNLOAD_FILE_LOCAL);
            return;
        }

        // 获取SD卡路径
        File apkFile = createFile(path, fileName);

        LogCat.e("download file  path:" + apkFile.getAbsolutePath());
        if(downloadThread != null){
            downloadThread.cancelDownload();
            downloadThread = null;
        }

        downloadThread = new DownloadPrepareThread(context, isToday, downloadUrl, threadNum, apkFile, listener);

        // 开启当前下载任务
        downloadThread.start();

    }

    @NonNull
    private File createFile(String path, String fileName) {
        File file = new File(path);
        // 如果SD卡目录不存在创建
        if (!file.exists()) {
            file.mkdirs();
        }

        File apkFile = new File(path, fileName);
        if (!apkFile.exists()) {
            try {
                apkFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return apkFile;
    }

//    private boolean isDownloading(Context context, String fileName) {
//        String preFileName = SharedUtils.getValue(context);
//        if(!TextUtils.isEmpty(preFileName) && preFileName.equals(fileName)){
//            Message msg = downLoadHandler.obtainMessage(DLUtils.HANDLER_WHAT_DOWNLOADING);
//            msg.obj = fileName;
//            downLoadHandler.sendMessage(msg);
//            LogCat.e("***********当前下载正在执行************");
//            return true;
//
//        }
//        return false;
//    }



    /**
     * 检测是否正在下载
     * @param fileName
     * @return
     */
    public boolean downloading(String fileName){
        if(!TextUtils.isEmpty(downloadingPath) && downloadingPath.equals(fileName)){
            LogCat.e("正在下载当前任务则不处理......");
            return true;
        }
        return false;
    }


    public void cancel() {
        if (downloadThread != null) {
            downloadThread.cancelDownload();
        }

        downloadingPath = null;
    }


}
