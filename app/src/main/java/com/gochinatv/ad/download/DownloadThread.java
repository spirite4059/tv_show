package com.gochinatv.ad.download;

import android.support.annotation.Nullable;

import com.gochinatv.ad.tools.LogCat;

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
    /** 线程下载异常重试次数 */
    private int retryTimes;
    /** 下载无法恢复的监听 */
    private OnDownloadErrorListener onDownloadErrorListener;
    /** 缓冲流 */
    private BufferedInputStream bis;
    /** 随机流 */
    private  RandomAccessFile raf;
    /** 线程下载的起始位置 */
    private int startPos;
    /** 线程下载的结束位置 */
    private int endPos;

    private static final int MAX_RETRY_DOWNLOAD_TIMES = 3;
    private static final int CONNECT_TIME_OUT = 10000;
    private static final int BUFFER_IN_SIZE = 2048;

    /**
     *
     * @param downloadUrl:文件下载地址
     * @param file:文件保存路径
     * @param blockSize:下载数据长度
     * @param threadId:线程ID
     */
    public DownloadThread(URL downloadUrl, File file, int blockSize,
                              int threadId) {
        this.downloadUrl = downloadUrl;
        this.file = file;
        this.threadId = threadId;
        this.blockSize = blockSize;
    }




    @Override
    public void run() {

        startPos = blockSize * (threadId - 1);//开始位置
        endPos = blockSize * threadId - 1;//结束位置


        HttpURLConnection connection = getHttpURLConnection(startPos, endPos);
        if (connection == null) {
            // 彻底放弃当前下载
            if(onDownloadErrorListener != null){
                onDownloadErrorListener.onDownloadError(errorCode);
            }
            return;
        }
            
            
        // 还原数据状态
        retryTimes = 0;


        downloadFile(connection);



    }

//    private int retryReader(int startPos, HttpURLConnection connection, BufferedInputStream bis, RandomAccessFile raf) {
//        // 关闭流
//        if (bis != null) {
//            try {
//                bis.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        if (raf != null) {
//            try {
//                raf.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        // 重新打开
//        bis = getBufferedInputStream(connection);
//        if (bis == null) {
//            // 彻底放弃当前下载
//            // TODO
//
//            return -1;
//        }
//
//        // 还原数据状态
//        retryTimes = 0;
//
//
//        byte[] buffer = new byte[1024];
//
//        raf = getRandomAccessFile();
//        if (raf == null) {
//            // 彻底放弃当前下载
//            // TODO
//
//            return -1;
//        }
//
//        // 还原数据状态
//        retryTimes = 0;
//
//
//        try {
//            raf.seek(startPos);
//        } catch (IOException e) {
//            e.printStackTrace();
//            errorCode = ErrorCodes.ERROR_DOWNLOAD_RANDOM_SEEK;   // 整个下载中断的code
//        }
//
//        if(errorCode == ErrorCodes.ERROR_DOWNLOAD_RANDOM_SEEK){   // 整个下载中断的code
//            return -1;
//        }
//
//
//        int len = -1;
//        try {
//            len = bis.read(buffer, 0, 1024);
//        } catch (IOException e) {
//            e.printStackTrace();
//            errorCode = ErrorCodes.ERROR_DOWNLOADING_READ;
//        }
//
//        if(len < 0 && retryReadTimes < 3){
//            retryReadTimes++;
//            len = retryReader(startPos, connection, bis, raf);
//        }
//
//        return len;
//    }

//    @Nullable
//    private RandomAccessFile getRandomAccessFile() {
//
//        RandomAccessFile raf = obtainRandomAccessFile();
//
//
//        if(raf == null || errorCode == ErrorCodes.ERROR_DOWNLOAD_RANDOM){
//            while (retryTimes < 3){
//                if(isCancel){
//                    return null;
//                }
//                raf = obtainRandomAccessFile();
//                if(raf != null){
//                    break;
//                }
//                retryTimes += 1;
//            }
//
//            if(retryTimes == 3 || raf == null){
//                // 此时放弃该线程的启动
//                errorCode = ErrorCodes.ERROR_DOWNLOAD_RANDOM;
//                // 彻底放弃当前下载
//                // TODO
//
//                return null;
//            }
//        }
//        return raf;
//    }
//
//    private RandomAccessFile obtainRandomAccessFile() {
//        RandomAccessFile raf = null;
//        try {
//            raf = new RandomAccessFile(file, "rwd");
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//            errorCode = ErrorCodes.ERROR_DOWNLOAD_RANDOM;   // 整个下载中断的code
//        }
//        return raf;
//    }
//
//    @Nullable
//    private BufferedInputStream getBufferedInputStream(HttpURLConnection connection) {
//        BufferedInputStream bis;
//        bis = obtainBufferedInputStream(connection);
//
//        if(bis == null || errorCode == ErrorCodes.ERROR_DOWNLOAD_BUFFER_IN){
//            // 重试3次
//            while (retryTimes < 3){
//                if(isCancel){
//                    return null;
//                }
//                bis = obtainBufferedInputStream(connection);
//                if(bis != null){
//                    break;
//                }
//                retryTimes += 1;
//            }
//
//            if(retryTimes == 3 || bis == null){
//                // 此时放弃该线程的启动
//                errorCode = ErrorCodes.ERROR_DOWNLOAD_BUFFER_IN;
//                return null;
//            }
//        }
//        return bis;
//    }

    @Nullable
    private HttpURLConnection getHttpURLConnection(int startPos, int endPos) {
        HttpURLConnection connection = getConnection(startPos, endPos);

        // 如果是请求文件体的conn出错，要继续访问3边，无需重新进行操作
        if (connection == null || errorCode == ErrorCodes.ERROR_DOWNLOAD_CONN) {
            // 异常错误处理
            while (retryTimes < MAX_RETRY_DOWNLOAD_TIMES){
                if(isCancel){

                    return null;
                }
                LogCat.e("URL connect 出错，进行第 " + (1 + retryTimes) +" 次尝试");
                connection = getConnection(startPos, endPos);
                if(connection != null){
                    LogCat.e("URL connect 出错，进行第 " + (1 + retryTimes) +" 次尝试, 已经成功的connect");
                    break;
                }
                retryTimes += 1;
            }

            if(retryTimes == MAX_RETRY_DOWNLOAD_TIMES || connection == null){
                // 此时放弃该线程的启动
                LogCat.e("进行多次尝试后，仍然无法URL connect，放弃此次下载。。。。");
                errorCode = ErrorCodes.ERROR_DOWNLOAD_CONN;
                return null;
            }
        }
        return connection;
    }

//    private BufferedInputStream obtainBufferedInputStream(HttpURLConnection connection) {
//        BufferedInputStream bis = null;
//        try {
//            bis = new BufferedInputStream(connection.getInputStream());
//        } catch (IOException e) {
//            e.printStackTrace();
//            // 此时出错就是代表整个下载中断
//            errorCode = ErrorCodes.ERROR_DOWNLOAD_BUFFER_IN;   // 整个下载中断的code
//        }
//        return bis;
//    }

    private HttpURLConnection getConnection(int startPos, int endPos) {
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
            connection.setReadTimeout(CONNECT_TIME_OUT);
            //设置当前线程下载的起点、终点
            connection.setRequestProperty("Range", "bytes=" + startPos + "-" + endPos);

            LogCat.e(Thread.currentThread().getName() + "  bytes="
                    + startPos + "-" + endPos);
            connection.connect();
        } catch (IOException e) {
            e.printStackTrace();
            errorCode = ErrorCodes.ERROR_DOWNLOAD_CONN;   // 整个下载中断的code
        }
        return connection;
    }

//    private int retryReadTimes;
//    private int readFileBuffer(byte[] buffer, BufferedInputStream bis){
//        int len = 0;
//        try {
//            len = bis.read(buffer, 0, 1024);
//            retryReadTimes = 0;
//        }catch (Exception e) {
//            if(retryReadTimes < 3){
//                // 出错就继续读取
//                readFileBuffer(buffer, bis);
//                retryReadTimes++;
//            }else{
//                retryReadTimes = 0;
//                errorCode = ErrorCodes.ERROR_DOWNLOADING_READ;
//            }
//        }
//        return len;
//    }


    private  boolean isException;
    private void downloadFile(HttpURLConnection connection){
        if(isException){
            if(retryTimes < MAX_RETRY_DOWNLOAD_TIMES){
                retryTimes++;
                LogCat.e("下载过程出现异常，进行第 " + retryTimes +" 次尝试");
            }else {
                LogCat.e("下载过程出现异常，多次尝试后仍然无法继续，放弃此次下载");
                retryTimes = 0;
                if(onDownloadErrorListener != null){
                    onDownloadErrorListener.onDownloadError(errorCode);
                }
                return;
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
            // 重置状态
            isException = false;
        }


        try {
            bis = new BufferedInputStream(connection.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
            errorCode = ErrorCodes.ERROR_DOWNLOAD_BUFFER_IN;
        }
        if (bis == null || errorCode == ErrorCodes.ERROR_DOWNLOAD_BUFFER_IN) {
            isException = true;
            LogCat.e("缓冲流获取异常。。。。。");
            downloadFile(connection);
            return;
        }

        // 还原数据状态
        retryTimes = 0;


        byte[] buffer = new byte[BUFFER_IN_SIZE];

        try {
            raf = new RandomAccessFile(file, "rwd");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            errorCode = ErrorCodes.ERROR_DOWNLOAD_RANDOM;
        }
        if (raf == null || errorCode == ErrorCodes.ERROR_DOWNLOAD_RANDOM) {
            isException = true;
            LogCat.e("随机流获取异常。。。。。");
            downloadFile(connection);
            return;
        }

        // 还原数据状态
        retryTimes = 0;

        try {
            raf.seek(startPos);
        } catch (IOException e) {
            e.printStackTrace();
            errorCode = ErrorCodes.ERROR_DOWNLOAD_RANDOM_SEEK;   // 整个下载中断的code
        }

        if(errorCode == ErrorCodes.ERROR_DOWNLOAD_RANDOM_SEEK){   // 整个下载中断的code
            isException = true;
            LogCat.e("随机流定为异常。。。。。");
            downloadFile(connection);
            return;
        }

        retryTimes = 0;
        int len = -1;
        try {
            len = bis.read(buffer, 0, BUFFER_IN_SIZE);
        } catch (IOException e) {
            e.printStackTrace();
            errorCode = ErrorCodes.ERROR_DOWNLOADING_READ;
        }

        if(len < 0 || errorCode == ErrorCodes.ERROR_DOWNLOADING_READ){
            // 此时多次读取数据出错，应该停止下载了
            isException = true;
            LogCat.e("输出流写入文件异常。。。。。");
            downloadFile(connection);
            return;
        }
        retryTimes = 0;

        downloadLength = 0;

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
                // TODO
                LogCat.e("写入文件异常。。。。。");
                isException = true;
                downloadFile(connection);
                break;
            }


            try {
                len = bis.read(buffer, 0, BUFFER_IN_SIZE);
            } catch (IOException e) {
                e.printStackTrace();
                LogCat.e("输出流写入文件异常。。。。。");
                errorCode = ErrorCodes.ERROR_DOWNLOADING_READ;
            }

            if(errorCode == ErrorCodes.ERROR_DOWNLOADING_READ){
                // 彻底放弃当前下载
                // TODO
                isException = true;
                downloadFile(connection);

                break;
            }

            downloadLength += len;
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
            LogCat.e("current thread " + getName() + " has finished,all size:"
                    + downloadLength);
        }


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



    public interface OnDownloadErrorListener {
        void onDownloadError(int errorCode);
    }

    public void setOnDownloadErrorListener(OnDownloadErrorListener onDownloadErrorListener) {
        this.onDownloadErrorListener = onDownloadErrorListener;
    }
}
