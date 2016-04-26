package com.download;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.download.tools.LogCat;
import com.gochinatv.db.DLDao;
import com.gochinatv.db.DownloadInfo;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by fq_mbp on 16/2/29.
 */
public class DownloadThread extends Thread {
    /** 当前下载是否完成 */
    private boolean isCompleted = false;
    /** 当前下载文件长度 */
    private int downloadLength = 0;
    /** 文件保存路径 */
    private File file;
    /** 文件下载路径 */
    private URL downloadUrl;
    /** 当前下载线程ID */
    private int threadId;
    /** 线程下载数据长度 */
    private int blockSize;
    private boolean isCancel;
    private int errorCode;
    /** 缓冲流 */
    private BufferedInputStream bis;
    /** 随机流 */
    private  RandomAccessFile raf;
    private static final int MAX_RETRY_DOWNLOAD_TIMES = 3;
    private static final int CONNECT_TIME_OUT = 60000;
    private static final int READ_TIME_OUT = 60000;
    private static final int BUFFER_IN_SIZE = 2048;
    private Context context;
    private long startPos;
    private long endPos;
    private DownloadInfo downloadInfo;
    /**
     *
     * @param downloadUrl:文件下载地址
     * @param file:文件保存路径
     * @param blockSize:下载数据长度
     * @param threadId:线程ID
     */

    public DownloadThread(Context context, URL downloadUrl, File file, int blockSize,
                              int threadId, DownloadInfo downloadInfo) {
        this.downloadUrl = downloadUrl;
        this.file = file;
        this.threadId = threadId;
        this.blockSize = blockSize;
        this.context = context;
        this.downloadInfo = downloadInfo;
    }
    @Override
    public void run() {
        if(downloadInfo != null){
            startPos = downloadInfo.endPos;
        } else {
            startPos = blockSize * (threadId - 1);//开始位置
        }
        endPos = blockSize * threadId - 1;//结束位置

        HttpURLConnection connection = getConnection();
        if (connection == null || errorCode == ErrorCodes.ERROR_DOWNLOAD_CONN) {
            // 彻底放弃当前下载
            return;
        }
        downloadFile(connection);
    }
    private HttpURLConnection getConnection() {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) downloadUrl.openConnection();
            connection.setConnectTimeout(CONNECT_TIME_OUT);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, application/xaml+xml, application/vnd.ms-xpsdocument, application/x-ms-xbap, application/x-ms-application, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*");
            connection.setRequestProperty("Accept-Language", "zh-CN");
            connection.setRequestProperty("Referer", downloadUrl.toString());
            connection.setRequestProperty("Charset", "UTF-8");
            connection.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.2; Trident/4.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)");
            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setReadTimeout(READ_TIME_OUT);
            System.setProperty("sun.net.client.defaultConnectTimeout", String.valueOf(CONNECT_TIME_OUT));
            System.setProperty("sun.net.client.defaultReadTimeout", String.valueOf(READ_TIME_OUT));
            //设置当前线程下载的起点、终点
            connection.setRequestProperty("Range", "bytes=" + startPos + "-" + endPos);
            LogCat.e("video", Thread.currentThread().getName() + "  bytes="
                    + startPos + "-" + endPos);
            connection.connect();
        } catch (IOException e) {
            e.printStackTrace();
            errorCode = ErrorCodes.ERROR_DOWNLOAD_CONN;   // 整个下载中断的code
        }
        return connection;
    }
    private void downloadFile(HttpURLConnection connection){
        downloadLength = 0;
        try {
            bis = new BufferedInputStream(connection.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
            errorCode = ErrorCodes.ERROR_DOWNLOAD_BUFFER_IN;
        }
        if (bis == null || errorCode == ErrorCodes.ERROR_DOWNLOAD_BUFFER_IN) {
            return;
        }
        byte[] buffer = new byte[BUFFER_IN_SIZE];
        try {
            raf = new RandomAccessFile(file, "rwd");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            errorCode = ErrorCodes.ERROR_DOWNLOAD_RANDOM;
        }
        if (raf == null || errorCode == ErrorCodes.ERROR_DOWNLOAD_RANDOM) {
            return;
        }
        try {
            raf.seek(startPos);
        } catch (IOException e) {
            e.printStackTrace();
            errorCode = ErrorCodes.ERROR_DOWNLOAD_RANDOM_SEEK;   // 整个下载中断的code
        }
        if(errorCode == ErrorCodes.ERROR_DOWNLOAD_RANDOM_SEEK){   // 整个下载中断的code
            return;
        }
        int len = -1;
        try {
            len = bis.read(buffer, 0, BUFFER_IN_SIZE);
        } catch (IOException e) {
            e.printStackTrace();

            errorCode = ErrorCodes.ERROR_DOWNLOADING_READ;
        }
        if(len < 0 || errorCode == ErrorCodes.ERROR_DOWNLOADING_READ){
            // 此时多次读取数据出错，应该停止下载了
            return;
        }
        downloadLength = len;

        // TODO 打开数据库
        SQLiteDatabase sqLiteDatabase = DLDao.getConnection(context);
        while (len != -1 && !isCancel) {
            try {
                raf.write(buffer, 0, len);
            } catch (Exception e) {
                e.printStackTrace();
                // 写入过程出错，此过程就对于多线程来说是最难处理的
                // 简单处理就是从开头位置重新写入，能避免不出错，但是可能会导致已经下载的流量浪费
                errorCode = ErrorCodes.ERROR_DOWNLOAD_WRITE;
            }

            if(errorCode == ErrorCodes.ERROR_DOWNLOAD_WRITE){
                // 彻底放弃当前下载
                break;
            }

            downloadLength += len;

            // TODO 修改文件的下载值
            try {
                long endPosition = startPos + downloadLength;
                DLDao.updateOut(sqLiteDatabase, downloadUrl.toString(), endPosition);
            }catch (Exception e){
                e.printStackTrace();
                errorCode = ErrorCodes.ERROR_DB_UPDATE;
            }
            if(errorCode == ErrorCodes.ERROR_DB_UPDATE){
                // 彻底放弃当前下载
                break;
            }

//            if(downloadLength > 1028 * 1024){
//                errorCode = ErrorCodes.ERROR_DOWNLOADING_READ;
//                break;
//            }

            try {
                len = bis.read(buffer, 0, BUFFER_IN_SIZE);
            } catch (IOException e) {
                e.printStackTrace();
                errorCode = ErrorCodes.ERROR_DOWNLOADING_READ;
            }
            if(errorCode == ErrorCodes.ERROR_DOWNLOADING_READ){
                // 彻底放弃当前下载
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
        if(errorCode == 0){
            isCompleted = true;
            LogCat.e("video", "current thread "  + " has finished,all size:"
                    + downloadLength);

            // 删除当前tid的数据表
            DLDao.delete(sqLiteDatabase, threadId);
        }

        // TODO 关闭数据库
        DLDao.closeDB(sqLiteDatabase);
    }
    /**
     * 线程文件是否下载完毕
     */
    public boolean isCompleted() {
        return isCompleted;
    }

    /**
     * 线程下载文件长度
     */
    public int getDownloadLength() {
        return downloadLength;
    }
    /**
     * 获取错误code
     */
    public int getErrorCode() {
        return errorCode;
    }
    public void cancel(){
        isCancel = true;
    }

}
