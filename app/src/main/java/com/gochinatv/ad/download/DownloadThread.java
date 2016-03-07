package com.gochinatv.ad.download;

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

    private int errorCode;

    @Override
    public void run() {
        BufferedInputStream bis = null;
        RandomAccessFile raf = null;

        int startPos = blockSize * (threadId - 1);//开始位置
        int endPos = blockSize * threadId - 1;//结束位置


        HttpURLConnection connection = getHttpURLConnection(startPos, endPos);

        // 如果是请求文件体的conn出错，要继续访问3边，无需重新进行操作
        if (connection == null || errorCode == ErrorCodes.ERROR_DOWNLOAD_CONN) {
            int errorConnTimes = 0;
            // 异常错误处理
            while (errorConnTimes < 3){
                if(isCancel){
                    return;
                }
                connection = getHttpURLConnection(startPos, endPos);
                if(connection != null){
                    break;
                }
                errorConnTimes += 1;
            }

            if(errorConnTimes == 3 || connection == null){
                // 此时放弃该线程的启动
                errorCode = ErrorCodes.ERROR_DOWNLOAD_CONN;
                return;
            }
        }



        try {
            bis = new BufferedInputStream(connection.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
            // 此时出错就是代表整个下载中断
            errorCode = ErrorCodes.ERROR_DOWNLOAD_BUFFER_IN;   // 整个下载中断的code
        }

        if(bis == null){
            errorCode = ErrorCodes.ERROR_DOWNLOAD_BUFFER_IN;   // 整个下载中断的code
            return;
        }

        if(errorCode == ErrorCodes.ERROR_DOWNLOAD_BUFFER_IN){   // 整个下载中断的code
            return;
        }



        byte[] buffer = new byte[1024];

        try {
            raf = new RandomAccessFile(file, "rwd");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            errorCode = ErrorCodes.ERROR_DOWNLOAD_RANDOM;   // 整个下载中断的code
        }

        if(raf == null){
            errorCode = ErrorCodes.ERROR_DOWNLOAD_RANDOM;   // 整个下载中断的code
            return;
        }

        if(errorCode == ErrorCodes.ERROR_DOWNLOAD_RANDOM){   // 整个下载中断的code
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


        int len;

        len = readFileBuffer(buffer, bis);

        if(len == -1 || errorCode == ErrorCodes.ERROR_DOWNLOADING_READ){
            // 此时多次读取数据出错，应该停止下载了

            return;
        }


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
                break;
            }

            downloadLength += len;

            len = readFileBuffer(buffer, bis);

            if(errorCode == ErrorCodes.ERROR_DOWNLOADING_READ){
                // 此时多次读取数据出错，应该停止下载了
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
            LogCat.e("current thread " + getName() + " has finished,all size:"
                    + downloadLength);
        }




    }

    private HttpURLConnection getHttpURLConnection(int startPos, int endPos) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) downloadUrl.openConnection();
            connection.setConnectTimeout(10000);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, application/xaml+xml, application/vnd.ms-xpsdocument, application/x-ms-xbap, application/x-ms-application, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*");
            connection.setRequestProperty("Accept-Language", "zh-CN");
            connection.setRequestProperty("Referer", downloadUrl.toString());
            connection.setRequestProperty("Charset", "UTF-8");
            connection.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.2; Trident/4.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)");
            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setReadTimeout(10000);
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

    private int retryReadTimes;
    private int readFileBuffer(byte[] buffer, BufferedInputStream bis){
        int len = 0;
        try {
            len = bis.read(buffer, 0, 1024);
            retryReadTimes = 0;
        }catch (Exception e) {
            if(retryReadTimes < 3){
                // 出错就继续读取
                readFileBuffer(buffer, bis);
                retryReadTimes++;
            }else{
                retryReadTimes = 0;
                errorCode = ErrorCodes.ERROR_DOWNLOADING_READ;
            }
        }
        return len;
    }


    /**
     * 由于进程运行期间出错，重新启动
     */
    private void reStartThread(){
        run();
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
