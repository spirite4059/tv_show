//package com.download;
//
//import android.annotation.TargetApi;
//import android.os.Build;
//import android.os.Environment;
//import android.os.StatFs;
//import android.text.TextUtils;
//
//import com.download.dllistener.InDLUtils;
//import com.download.dllistener.OnDownloadStatusListener;
//import com.download.tools.CacheVideoListThread;
//import com.download.tools.Constants;
//import com.download.tools.LogCat;
//import com.download.tools.ToolUtils;
//import com.httputils.http.response.AdDetailResponse;
//
//import java.io.BufferedInputStream;
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.IOException;
//import java.io.RandomAccessFile;
//import java.net.HttpURLConnection;
//import java.net.MalformedURLException;
//import java.net.URL;
//import java.util.ArrayList;
//
//import static com.download.ErrorCodes.ERROR_DOWNLOADING_READ;
//import static com.download.ErrorCodes.ERROR_DOWNLOAD_BUFFER_IN;
//import static com.download.ErrorCodes.ERROR_DOWNLOAD_CONN;
//import static com.download.ErrorCodes.ERROR_DOWNLOAD_EXCUTORS;
//import static com.download.ErrorCodes.ERROR_DOWNLOAD_FILE_LOCAL;
//import static com.download.ErrorCodes.ERROR_DOWNLOAD_FILE_NULL;
//import static com.download.ErrorCodes.ERROR_DOWNLOAD_FILE_SIZE;
//import static com.download.ErrorCodes.ERROR_DOWNLOAD_FILE_UNKNOWN;
//import static com.download.ErrorCodes.ERROR_DOWNLOAD_RANDOM;
//import static com.download.ErrorCodes.ERROR_DOWNLOAD_RANDOM_SEEK;
//import static com.download.ErrorCodes.ERROR_DOWNLOAD_URL;
//import static com.download.ErrorCodes.ERROR_DOWNLOAD_WRITE;
//import static com.download.ErrorCodes.ERROR_THREAD_NUMBERS;
//import static com.download.ErrorCodes.HTTP_OK;
//import static com.download.ErrorCodes.HTTP_PARTIAL;
//
///**
// * Created by fq_mbp on 16/4/14.
// */
//public class SingleDLUtils extends Thread implements InDLUtils{
//
//    private boolean isCancel;
//    private String downloadUrl;
//    private int errorCode;
//    private static final int CONNECT_TIME_OUT = 60000;
//    private static final int BUFFER_IN_SIZE = 2048;
//    private String fileName;
//    private File file;
//    private OnDownloadStatusListener listener;
//
//
//    private SingleDLUtils() {
//    }
//
//    private static class DLUtilsHolder {
//        private static final SingleDLUtils instance = new SingleDLUtils();
//    }
//
//    public static SingleDLUtils init() {
//        return DLUtilsHolder.instance;
//    }
//
//    public void download(String path, String fileName, String downloadUrl, OnDownloadStatusListener listener){
//        // 优先检测sdcard是否可用
//        LogCat.e("SingleDLUtils   ->   download ............");
//        if (!ToolUtils.isExistSDCard()) {
//            listener.onError(ErrorCodes.ERROR_DOWNLOAD_SDCARD_USESLESS, getErrorMsg(ErrorCodes.ERROR_DOWNLOAD_SDCARD_USESLESS));
//            return;
//        }
//        // 文件路径检查
//        if (TextUtils.isEmpty(path)) {
//            listener.onError(ErrorCodes.ERROR_DOWNLOAD_INIT_INFO, getErrorMsg(ErrorCodes.ERROR_DOWNLOAD_INIT_INFO));
//            return;
//        }
//        if (TextUtils.isEmpty(downloadUrl)) {
//            listener.onError(ErrorCodes.ERROR_DOWNLOAD_INIT_INFO, getErrorMsg(ErrorCodes.ERROR_DOWNLOAD_INIT_INFO));
//            return;
//        }
//        if (TextUtils.isEmpty(fileName)) {
//            listener.onError(ErrorCodes.ERROR_DOWNLOAD_INIT_INFO, getErrorMsg(ErrorCodes.ERROR_DOWNLOAD_INIT_INFO));
//            return;
//        }
//
//        this.listener = listener;
//        this.fileName = fileName;
//        this.downloadUrl = downloadUrl;
//
//        // 创建下载文件
//        if (initDownloadFile(path, fileName)) {
//            listener.onError(ErrorCodes.ERROR_DOWNLOAD_FILE_LOCAL, getErrorMsg(ErrorCodes.ERROR_DOWNLOAD_FILE_LOCAL));
//            return;
//        }
//
//        if (isCancel) {
//            return;
//        }
//        // 开启下载........
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                SingleDLUtils.this.run();
//            }
//        }){}.start();
//
//    }
//
//    private boolean initDownloadFile(String path, String fileName) {
//        file = ToolUtils.createFile(path, fileName);
//        LogCat.e("download file  path:" + file.getAbsolutePath());
//        if (file == null) {
//            return true;
//        }
//        return false;
//    }
//
//    public void cancel(){
//        isCancel = true;
//    }
//
//    @Override
//    public void run() {
//        new Runnable(){
//            @Override
//            public void run() {
//
//            }
//        };
//        LogCat.e("开始下载。。。。。。。。。。。---------------");
//        if (isCancel) {
//            return;
//        }
//
//        // 获取url
//        URL turl = null;
//        try {
//            turl = new URL(downloadUrl);
//        } catch (MalformedURLException e) {
//            e.printStackTrace();
//            errorCode = ErrorCodes.ERROR_DOWNLOAD_URL;
//        }
//        if (isCancel) {
//            return;
//        }
//        if (turl == null || errorCode != 0) {
//            listener.onError(ErrorCodes.ERROR_DOWNLOAD_URL, getErrorMsg(ErrorCodes.ERROR_DOWNLOAD_URL));
//            return;
//        }
//
//        if (isCancel) {
//            return;
//        }
//
//
//        HttpURLConnection connection = getHttpURLConnection(turl);
//        // 如果是请求文件体的conn出错，要继续访问3边，无需重新进行操作
//        if (connection == null || errorCode == ErrorCodes.ERROR_DOWNLOAD_CONN) {
//            listener.onError(ErrorCodes.ERROR_DOWNLOAD_CONN, getErrorMsg(ErrorCodes.ERROR_DOWNLOAD_CONN));
//            return;
//        }
//
//        if (isCancel) {
//            return;
//        }
//        boolean isConnectSuccess = false;
//        try {
//            final int code = connection.getResponseCode();
//            if (code == HTTP_OK || code == HTTP_PARTIAL) {
//                isConnectSuccess = true;
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//            errorCode = ErrorCodes.ERROR_DOWNLOAD_CONN;
//        }
//
//        if (connection == null || errorCode == ErrorCodes.ERROR_DOWNLOAD_CONN) {
//            listener.onError(ErrorCodes.ERROR_DOWNLOAD_CONN, getErrorMsg(ErrorCodes.ERROR_DOWNLOAD_CONN));
//            return;
//        }
//
//        if (isCancel) {
//            return;
//        }
//
//        if (!isConnectSuccess) {
//            listener.onError(ErrorCodes.ERROR_DOWNLOAD_CONN, getErrorMsg(ErrorCodes.ERROR_DOWNLOAD_CONN));
//            return;
//        }
//
//        if (isCancel) {
//            return;
//        }
//
//        // 读取下载文件总大小
//        int fileSize = connection.getContentLength();
//        LogCat.e("fileSize: " + fileSize);
//
//        if (isCancel) {
//            return;
//        }
//
//
//        if (fileSize <= 0) {
//            listener.onError(ErrorCodes.ERROR_DOWNLOAD_FILE_SIZE, getErrorMsg(ErrorCodes.ERROR_DOWNLOAD_FILE_SIZE));
//            return;
//        }
//
//        if (isCancel) {
//            return;
//        }
//
//        // 可用空间检查
//        //获得SD卡空间的信息
//        if (checkAvailableSpace(fileSize)) {
//            listener.onError(ErrorCodes.ERROR_DOWNLOAD_SDCARD_SPACE, getErrorMsg(ErrorCodes.ERROR_DOWNLOAD_SDCARD_SPACE));
//            return;
//        }
//
//
//        if (isCancel) {
//            return;
//        }
//
//        // 缓存文件大小
//        cacheFileSize(fileSize);
//
//
//        if (isCancel) {
//            return;
//        }
//
//
//        int downloadLength = 0;
//        BufferedInputStream bis = null;
//        try {
//            bis = new BufferedInputStream(connection.getInputStream());
//        } catch (IOException e) {
//            e.printStackTrace();
//            errorCode = ErrorCodes.ERROR_DOWNLOAD_BUFFER_IN;
//        }
//        if (bis == null || errorCode == ErrorCodes.ERROR_DOWNLOAD_BUFFER_IN) {
//            listener.onError(ErrorCodes.ERROR_DOWNLOAD_BUFFER_IN, getErrorMsg(ErrorCodes.ERROR_DOWNLOAD_BUFFER_IN));
//            return;
//        }
//        byte[] buffer = new byte[BUFFER_IN_SIZE];
//        RandomAccessFile raf = null;
//        try {
//            raf = new RandomAccessFile(file, "rwd");
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//            errorCode = ErrorCodes.ERROR_DOWNLOAD_RANDOM;
//        }
//        if (raf == null || errorCode == ErrorCodes.ERROR_DOWNLOAD_RANDOM) {
//            listener.onError(ErrorCodes.ERROR_DOWNLOAD_RANDOM, getErrorMsg(ErrorCodes.ERROR_DOWNLOAD_RANDOM));
//            return;
//        }
//        try {
//            raf.seek(0);
//        } catch (IOException e) {
//            e.printStackTrace();
//            errorCode = ErrorCodes.ERROR_DOWNLOAD_RANDOM_SEEK;   // 整个下载中断的code
//        }
//        if(errorCode == ErrorCodes.ERROR_DOWNLOAD_RANDOM_SEEK){   // 整个下载中断的code
//            listener.onError(ErrorCodes.ERROR_DOWNLOAD_RANDOM_SEEK, getErrorMsg(ErrorCodes.ERROR_DOWNLOAD_RANDOM_SEEK));
//            return;
//        }
//
//        int len = -1;
//        try {
//            len = bis.read(buffer, 0, BUFFER_IN_SIZE);
//        } catch (IOException e) {
//            e.printStackTrace();
//            errorCode = ErrorCodes.ERROR_DOWNLOADING_READ;
//        }
//        if(len < 0 || errorCode == ErrorCodes.ERROR_DOWNLOADING_READ){
//            // 此时多次读取数据出错，应该停止下载了
//            listener.onError(ErrorCodes.ERROR_DOWNLOADING_READ, getErrorMsg(ErrorCodes.ERROR_DOWNLOADING_READ));
//            return;
//        }
//
//        downloadLength = len;
//        LogCat.e("开始读取文件流........");
//        try {
//            while (len != -1 && !isCancel) {
//                try {
//                    raf.write(buffer, 0, len);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    // 写入过程出错，此过程就对于多线程来说是最难处理的
//                    // 简单处理就是从开头位置重新写入，能避免不出错，但是可能会导致已经下载的流量浪费
//                    errorCode = ErrorCodes.ERROR_DOWNLOAD_WRITE;
//                }
//                if(errorCode == ErrorCodes.ERROR_DOWNLOAD_WRITE){
//                    // 彻底放弃当前下载
//                    // TODO
//                    listener.onError(ErrorCodes.ERROR_DOWNLOAD_WRITE, getErrorMsg(ErrorCodes.ERROR_DOWNLOAD_WRITE));
//                    break;
//                }
//
//                try {
//                    len = bis.read(buffer, 0, BUFFER_IN_SIZE);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    errorCode = ErrorCodes.ERROR_DOWNLOADING_READ;
//                }
//                if(errorCode == ErrorCodes.ERROR_DOWNLOADING_READ){
//                    // 彻底放弃当前下载
//                    // TODO
//                    listener.onError(ErrorCodes.ERROR_DOWNLOADING_READ, getErrorMsg(ErrorCodes.ERROR_DOWNLOADING_READ));
//                    break;
//                }
//                downloadLength += len;
//            }
//        }catch (Exception e){
//            e.printStackTrace();
//        }finally {
//            if (bis != null) {
//                try {
//                    bis.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//            if (raf != null) {
//                try {
//                    raf.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//
//        if(errorCode == 0){
//            LogCat.e("current thread "  + " has finished,all size:"
//                    + downloadLength);
//        }
//
//    }
//
//    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
//    private boolean checkAvailableSpace(int fileSize) {
//        File path = Environment.getExternalStorageDirectory();
//        StatFs statFs = new StatFs(path.getPath());
//        long blockSizeLong = 0;
//        long availableBlocksLong = 0;
//        if (Build.VERSION.SDK_INT >= 18) {
//            blockSizeLong = statFs.getBlockSizeLong();
//            availableBlocksLong = statFs.getAvailableBlocksLong();
//        } else {
//            blockSizeLong = statFs.getBlockSizeLong();
//            availableBlocksLong = statFs.getAvailableBlocksLong();
//        }
//
//
//        //计算SD卡的空间大小
//        long availableSize = availableBlocksLong * blockSizeLong;
//        long preSpace = 10 * 1024 * 1024; // 10M的预留空间
//        if (availableSize < (fileSize + preSpace)) {
//            // sdcard空间不足，无法下载当前视频
//            return true;
//        }
//        return false;
//    }
//
//    private void cacheFileSize(int fileSize) {
//        // 取本地已经缓存的列表，生成对应实体类
//        ArrayList<AdDetailResponse> cacheVideos = ToolUtils.getCacheList();
//        // 根据fileName匹对当前的视频信息
//
//        if (cacheVideos != null && cacheVideos.size() > 0) {
//            for (AdDetailResponse cache : cacheVideos) {
//                if (cache != null && !TextUtils.isEmpty(cache.adVideoName)) {
//                    String cacheVideoName = cache.adVideoName + Constants.FILE_DOWNLOAD_EXTENSION;
//                    // 将对应文件的赋值后，再将该实体类写入缓存
//                    if (cacheVideoName.equals(fileName)) {
//                        cache.adVideoLength = fileSize;
//                        LogCat.e("缓存列表文件名字：" + cacheVideoName);
//                        LogCat.e("下载文件的名字：" + fileName);
//                        break;
//                    }
//                }
//            }
//        }
//        // 再将缓存文件写入sdcard
//        cacheVideoList(cacheVideos);
//    }
//
//
//    private HttpURLConnection getHttpURLConnection(URL turl) {
//        HttpURLConnection connection = null;
//        try {
//            connection = (HttpURLConnection) turl.openConnection();
//            connection.setConnectTimeout(CONNECT_TIME_OUT);
//            connection.setRequestMethod("GET");
//            connection.setRequestProperty("Accept", "image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, application/xaml+xml, application/vnd.ms-xpsdocument, application/x-ms-xbap, application/x-ms-application, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*");
//            connection.setRequestProperty("Accept-Language", "zh-CN");
//            connection.setRequestProperty("Referer", downloadUrl);
//            connection.setRequestProperty("Charset", "UTF-8");
//            connection.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.2; Trident/4.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)");
//            connection.setRequestProperty("Connection", "Keep-Alive");
//            connection.setReadTimeout(CONNECT_TIME_OUT);
//
//            System.setProperty("sun.net.client.defaultConnectTimeout", String.valueOf(CONNECT_TIME_OUT));
//            System.setProperty("sun.net.client.defaultReadTimeout", String.valueOf(CONNECT_TIME_OUT));
//            connection.connect();
//        } catch (IOException e) {
//            e.printStackTrace();
//            errorCode = ErrorCodes.ERROR_DOWNLOAD_CONN;
//        }
//        return connection;
//    }
//
//
//    private synchronized void cacheVideoList(ArrayList<AdDetailResponse> cachePlayVideoLists) {
//        new CacheVideoListThread(cachePlayVideoLists, ToolUtils.getCacheDirectory(), Constants.FILE_CACHE_TD_NAME).start();
//        LogCat.e("文件缓存成功.........");
//    }
//
//
//
//    private String getErrorMsg(int errorCode) {
//        String errorMsg = null;
//        switch (errorCode) {
//            case ERROR_DOWNLOAD_WRITE:
//                errorMsg = "error：输入流写入文件过程出错";
//                break;
//            case ERROR_DOWNLOADING_READ:
//                errorMsg = "error：读取文件流过程出错";
//                break;
//            case ERROR_THREAD_NUMBERS:
//                errorMsg = "error：多线程的线程数小于0";
//                break;
//            case ERROR_DOWNLOAD_URL:
//                errorMsg = "error：下载地址URL生成出错或null";
//                break;
//            case ERROR_DOWNLOAD_CONN:
//                errorMsg = "error：URL的链接过程出错或null";
//                break;
//            case ERROR_DOWNLOAD_FILE_SIZE:
//                errorMsg = "error：下载文件的大小异常";
//                break;
//            case ERROR_DOWNLOAD_FILE_LOCAL:
//                errorMsg = "error：下载文件的存储地址异常";
//                break;
//            case ERROR_DOWNLOAD_FILE_NULL:
//                errorMsg = "error：无法生成下载文件";
//                break;
//            case ERROR_DOWNLOAD_BUFFER_IN:
//                errorMsg = "error：输入流异常或null";
//                break;
//            case ERROR_DOWNLOAD_RANDOM:
//                errorMsg = "error：随机流异常或null";
//                break;
//            case ERROR_DOWNLOAD_RANDOM_SEEK:
//                errorMsg = "error：随机流定为异常";
//                break;
//            case ERROR_DOWNLOAD_FILE_UNKNOWN:
//                errorMsg = "error：下载文件大小出错";
//                break;
//            case ERROR_DOWNLOAD_EXCUTORS:
//                errorMsg = "error：线程池出错";
//                break;
//            default:
//                errorMsg = "error：未知的异常";
//                break;
//        }
//
//
//        return errorMsg;
//    }
//
//}
