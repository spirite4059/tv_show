package com.retrofit.download;

import android.content.Context;
import android.os.Message;
import android.util.Log;

import com.retrofit.download.db.DLDao;
import com.retrofit.download.db.DownloadInfo;
import com.retrofit.tools.Tools;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

/**
 * Created by fq_mbp on 16/7/21.
 */

public class DownloadFileThread extends Thread {

    private Context context;
    private String fileName;
    private String filePath;
    private ProgressHandler progressHandler;
    private InputStream is;
    private boolean isCancel;
    private static final int BUFFER_IN_SIZE = 4096;



    public DownloadFileThread(Context context, String filePath, String fileName, ProgressHandler progressHandler, InputStream is) {
        this.context = context;
        this.fileName = fileName;
        this.filePath = filePath;
        this.progressHandler = progressHandler;
        this.is = is;
    }

    @Override
    public void run() {
        super.run();
        File file = createFile();

        if(file == null || !file.exists()){
            sendErrorMsg("本地文件创建失败");
            return;
        }

        BufferedInputStream bis = new BufferedInputStream(is);

        byte[] buffer = new byte[BUFFER_IN_SIZE];

        RandomAccessFile raf = null;
        boolean isRandomError = false;
        try {
            raf = new RandomAccessFile(file, "rwd");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            isRandomError = true;
        }

        if (raf == null || isRandomError) {
            sendErrorMsg("随机流创建失败......");
            return;
        }

        // 定为下载位置
        try {
            DownloadInfo downloadInfo = DLDao.query(context);
            raf.seek(downloadInfo != null ? downloadInfo.startPos: 0);
        } catch (IOException e) {
            e.printStackTrace();
        }

        int len = 0;
        boolean isReadError = false;
        try {
            len = bis.read(buffer);
        } catch (IOException e) {
            e.printStackTrace();
            isReadError = true;
        }
        if(isReadError){
            // 此时多次读取数据出错，应该停止下载了
            sendErrorMsg("读取流文件出错......");
            return;
        }
        boolean isWriteError = false;
        long downloadLength = 0;
        while (len != -1 && !isCancel) {
            try {
                raf.write(buffer, 0, len);
            } catch (Exception e) {
                e.printStackTrace();
                // 写入过程出错，此过程就对于多线程来说是最难处理的
                // 简单处理就是从开头位置重新写入，能避免不出错，但是可能会导致已经下载的流量浪费
                isWriteError = true;
            }

            if(isWriteError){
                break;
            }

            downloadLength += len;

            try {
                len = bis.read(buffer);
            } catch (IOException e) {
                e.printStackTrace();
                isReadError = true;
            }

            if(len <= 0 || isReadError){
                break;
            }

        }
        if (bis != null) {
            try {
                bis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (raf != null) {
            try {
                raf.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(isReadError || isWriteError){
            Log.e("retrofit_dl", "下载过程出错,停止下载......");
            if(isReadError){
                sendErrorMsg("读取流文件出错......");
            }else {
                sendErrorMsg("写文件出错......");
            }
        }else {
            Log.e("retrofit_dl", "文件下载完成......");
        }

        Log.e("retrofit_dl", "当前线程已经下载....." + downloadLength);


    }

    private File createFile() {
        File file = null;
        try {
            file = Tools.creatFile(filePath, fileName + ".mp4");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    private void sendErrorMsg(String errorMsg) {
        Message msg = progressHandler.obtainMessage(2);
        msg.obj = errorMsg;
        progressHandler.sendMessage(msg);
    }


    public void cancel(){
        isCancel = true;
    }


}
