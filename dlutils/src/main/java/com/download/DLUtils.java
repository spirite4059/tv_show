package com.download;

import android.content.Context;
import android.text.TextUtils;

import com.download.db.DLDao;
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

    private DLUtils() {
        downloadingPath = null;
    }


    public static DLUtils init() {
        if(instance == null){
            synchronized (DLUtils.class){
                if(instance == null){
                    LogCat.e("video", "DLUtils初始化...........");
                    instance = new DLUtils();
                }
            }
        }
        return instance;
    }


    public void download(Context context, String path, String fileName, String downloadUrl, int threadNum, OnDownloadStatusListener listener) {
        if(!TextUtils.isEmpty(downloadingPath) && downloadingPath.equals(path +fileName)){
            LogCat.e("video", "正在下载当前任务则不处理......");
            return;
        }

        // 记录当前下载任务
        downloadingPath = path + fileName;

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

        LogCat.e("video", "start file  path:" + apkFile.getAbsolutePath());
        if(downloadThread != null){
            downloadThread.cancelDownload();
            downloadThread = null;
        }

        downloadThread = new DownloadPrepareThread(context, downloadUrl, threadNum, apkFile, listener);

        // 开启当前下载任务
        downloadThread.start();

    }


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




    protected void clearDownloadStatus(){
        downloadingPath = null;
    }

    /**
     * 检测是否正在下载
     * @param fileName
     * @return
     */
    public boolean downloading(String fileName){
        if(!TextUtils.isEmpty(downloadingPath) && downloadingPath.equals(fileName)){
            LogCat.e("video", "正在下载当前任务则不处理......");
            return true;
        }
        return false;
    }


    public void cancel() {
        if (downloadThread != null) {
            downloadThread.cancelDownload();
        }
        downloadThread = null;
        downloadingPath = null;
        instance = null;
        LogCat.e("video", "DLUtils -> cancel.......");
    }

    public static void deleteDlMsg(Context context){
        DLDao.delete(context);
    }

}
