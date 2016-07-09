package com.download;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;

import com.download.db.DLDao;
import com.download.db.DownloadInfo;
import com.download.dllistener.OnDownloadStatusListener;
import com.download.tools.CacheVideoListThread;
import com.download.tools.Constants;
import com.download.tools.LogCat;
import com.download.tools.ToolUtils;
import com.gochinatv.db.AdDao;
import com.okhtttp.response.AdDetailResponse;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import static com.download.ErrorCodes.ERROR_DOWNLOAD_FILE_UNKNOWN;
import static com.download.ErrorCodes.HTTP_OK;
import static com.download.ErrorCodes.HTTP_PARTIAL;

/**
 * Created by fq_mbp on 16/2/29.
 */
public class DownloadPrepareThread extends Thread {
    private String downloadUrl;// 下载链接地址
    private int threadNum;// 开启的线程数
    private File file;// 保存文件路径地址
    private OnDownloadStatusListener listener;
    private int errorCode;
    private boolean isCancel;
    private static final int CONNECT_TIME_OUT = 60000;
    private DownloadThread[] threads;
    private Context context;
    private boolean isToday;


    public DownloadPrepareThread(Context context, boolean isToday, String downloadUrl, int threadNum, File file, OnDownloadStatusListener listener) {
        this.downloadUrl = downloadUrl;
        this.context = context;
        this.isToday = isToday;
        this.threadNum = threadNum;
        this.file = file;
        this.listener = listener;
        errorCode = 0;
    }


    @Override
    public void run() {
        if (isCancel) {
            return;
        }

        if (listener == null) {
            return;
        }

        if (isCancel) {
            return;
        }

        if (threadNum < 0) {
            setErrorMsg(ErrorCodes.ERROR_THREAD_NUMBERS);
            return;
        }

        if (isCancel) {
            return;
        }

        threads = new DownloadThread[threadNum];
        if (threads.length == 0) {
            setErrorMsg(ErrorCodes.ERROR_THREAD_NUMBERS);
            return;
        }

        if (isCancel) {
            return;
        }


        URL url = null;
        try {
            url = new URL(downloadUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            errorCode = ErrorCodes.ERROR_DOWNLOAD_URL;
        }
        if (isCancel) {
            return;
        }
        if (url == null || errorCode != 0) {
            setErrorMsg(ErrorCodes.ERROR_DOWNLOAD_URL);
            return;
        }

        if (isCancel) {
            return;
        }


        HttpURLConnection connection = getHttpURLConnection(url);
        // 如果是请求文件体的conn出错，要继续访问3边，无需重新进行操作
        if (connection == null || errorCode == ErrorCodes.ERROR_DOWNLOAD_CONN) {
            setErrorMsg(ErrorCodes.ERROR_DOWNLOAD_CONN);
            return;
        }

        if (isCancel) {
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
            errorCode = ErrorCodes.ERROR_DOWNLOAD_CONN;
        }

        if (connection == null || errorCode == ErrorCodes.ERROR_DOWNLOAD_CONN) {
            setErrorMsg(ErrorCodes.ERROR_DOWNLOAD_CONN);
            return;
        }

        if (isCancel) {
            return;
        }

        if (!isConnectSuccess) {
            setErrorMsg(ErrorCodes.ERROR_DOWNLOAD_CONN);
            return;
        }

        if (isCancel) {
            return;
        }

        // 读取下载文件总大小
        int fileSize = connection.getContentLength();


        if (isCancel) {
            return;
        }


        if (fileSize <= 0) {
            errorCode = ErrorCodes.ERROR_DOWNLOAD_FILE_SIZE;
            setErrorMsg(ErrorCodes.ERROR_DOWNLOAD_FILE_SIZE);
            return;
        }

        if (isCancel) {
            return;
        }

        if (listener != null) {
            listener.onPrepare(fileSize);
        }

        LogCat.e("video", "将文件大小写入数据库.......");
        updateFileLength(fileSize);


        // 可用空间检查
        //获得SD卡空间的信息
        if (checkSdCardSpace(fileSize))
            return;

        LogCat.e("video", "fileSize: " + fileSize);


        if (isCancel) {
            return;
        }
        // 计算每条线程下载的数据长度
        int blockSize = (fileSize % threadNum) == 0 ? (fileSize / threadNum) : (fileSize / threadNum + 1);
        LogCat.e("video", "blockSize: " + blockSize);

        if (isCancel) {
            return;
        }


        if (file == null) {
            errorCode = ErrorCodes.ERROR_DOWNLOAD_FILE_LOCAL;
            setErrorMsg(ErrorCodes.ERROR_DOWNLOAD_FILE_LOCAL);
            return;
        }

        if (isCancel) {
            return;
        }
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (isCancel) {
            return;
        }


        // 检查sql
        int size = threads.length;
        ArrayList<DownloadInfo> downloadInfos = DLDao.queryAll(context, ToolUtils.getFileName(file.getName()));
        // 线程数变了
        if (downloadInfos != null && downloadInfos.size() > 0) {
            LogCat.e("video", "有当前记录的存储信息......");
            if (downloadInfos.size() != size) {
                LogCat.e("video", "线程数发生变化，删除记录，重新下载......");
                DLDao.delete(context);
                startThreadWithOutSql(url, fileSize, blockSize, size);
            } else if (file.length() == 0) {
                LogCat.e("video", "可能数据表还在，但是文件已经删除了......");
                DLDao.delete(context);
                startThreadWithOutSql(url, fileSize, blockSize, size);
            } else {
                if (file != null && file.exists()) {
                    LogCat.e("video", "从数据库中回复下载......");
                    startThreadWithSql(url, blockSize, size, downloadInfos, fileSize);
                } else {
                    LogCat.e("video", "有记录信息，但是文件遭到破坏，从开开始下载......");
                    DLDao.delete(context);
                    startThreadWithOutSql(url, fileSize, blockSize, size);
                }
            }
        } else {
            LogCat.e("video", "没有当前下载信息，从头开始下载......");
            startThreadWithOutSql(url, fileSize, blockSize, size);
        }


        if (errorCode == ErrorCodes.ERROR_DOWNLOAD_EXCUTORS) {
            setErrorMsg(errorCode);
            return;
        }


        boolean isFinished = false;


        if (isCancel) {
            return;
        }


        int downloadSize = -2;
        long startPosition;
        SQLiteDatabase sqLiteDatabase = DLDao.getConnection(context);
        try {
            while (!isFinished) {
                isFinished = true;
                int downloadedAllSize = threadNum;
                boolean isThreadError = false;
                // 当前所有线程下载总量
                for (DownloadThread downloadThread : threads) {
                    if (downloadThread != null) {
                        if (isCancel) {
                            LogCat.e("video", "停止线程threadId: " + downloadThread.threadId);
                            downloadThread.cancel();
//                            try {
//                                startPosition = downloadThread.startPos + downloadSize - 1;
//                                DLDao.updateOut(sqLiteDatabase, downloadThread.threadId, startPosition);
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                                errorCode = ErrorCodes.ERROR_DB_UPDATE;
//                            }
                            continue;
                        }

                        errorCode = downloadThread.getErrorCode();
                        if (errorCode != 0) {
                            // 线程出错了, 中断下载
                            LogCat.e("video", "下载过程有异常.......终止进度线程");
                            isThreadError = true;
                            break;
                        } else {

                            if (!downloadThread.isCompleted()) {
                                isFinished = false;
                                downloadedAllSize += downloadThread.getDownloadLength();
                            } else {
                                downloadedAllSize += downloadThread.getDownloadLength();
                            }

                            try {
                                startPosition = downloadThread.getStartPos() + downloadThread.getDownloadLength() - 1;
                                DLDao.updateOut(sqLiteDatabase, downloadThread.threadId, startPosition);
                            } catch (Exception e) {
                                e.printStackTrace();
                                errorCode = ErrorCodes.ERROR_DB_UPDATE;
                            }
                        }
                    }
                }

                downloadSize = downloadedAllSize;

                // 是否有子线程出错或者取消下载
                if (isThreadError || isCancel) {
                    isFinished = false;
                    if (isThreadError) {
                        LogCat.e("video", "下载线程出错......");
                    } else {
                        LogCat.e("video", "主动取消了下载......");

                    }
                    break;
                }
                // 通知handler去更新视图组件
                setDownloadMsg(downloadedAllSize);
                try {
                    Thread.sleep(1000);// 休息1秒后再读取下载进度
                } catch (Exception e) {

                }


            }
        } catch (Exception e) {
            e.printStackTrace();
            errorCode = ErrorCodes.ERROR_DOWNLOAD_UNKNOWN;
        }
        DLUtils.clearDownloadStatus();
        // 主动取消下载
        if (isCancel) {
            LogCat.e("video", "主动停止下载........");
            setCancel();
//            deleteFailFile(fileSize, downloadSize);
            return;
        }
        // 当下载过程出错
        if (errorCode != 0) {
            LogCat.e("video", "下载过程出错，主动停止下载........");
            setErrorMsg(errorCode);
//            deleteFailFile(fileSize, downloadSize);
            return;
        }
        // 此时是正常结束，无论是否正常下载成功，都要删除数据库记录
        DLDao.delete(sqLiteDatabase);
        DLDao.delete(context);
        // 完成所有的下载了
        if (isFinished) {
            LogCat.e("video", "文件下载完成......fileSize: " + fileSize);
            LogCat.e("video", "文件下载完成......downloadSize: " + downloadSize);
            if (deleteFailFile(fileSize, downloadSize)) {
                LogCat.e("video", "文件完整下载......");
                setFinish(file.getAbsolutePath());
                // 删除当前记录
            }
        } else {
            LogCat.e("video", "文件下载尚未完成......");
            setErrorMsg(ERROR_DOWNLOAD_FILE_UNKNOWN);
        }


    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private boolean checkSdCardSpace(int fileSize) {
        File path = Environment.getExternalStorageDirectory();
        StatFs statFs = new StatFs(path.getPath());
        long blockSizeLong = 0;
        long availableBlocksLong = 0;
        if (Build.VERSION.SDK_INT >= 18) {
            blockSizeLong = statFs.getBlockSizeLong();
            availableBlocksLong = statFs.getAvailableBlocksLong();
        } else {
            blockSizeLong = statFs.getBlockSizeLong();
            availableBlocksLong = statFs.getAvailableBlocksLong();
        }

        //计算SD卡的空间大小
        long availableSize = availableBlocksLong * blockSizeLong;
        long preSpace = 10 * 1024 * 1024; // 10M的预留空间
        if (availableSize < (fileSize + preSpace)) {
            // sdcard空间不足，无法下载当前视频
            setErrorMsg(ErrorCodes.ERROR_DOWNLOAD_SDCARD_SPACE);
            return true;

        }
        return false;
    }

    private void updateFileLength(int fileSize) {
        try {
            String fileName = ToolUtils.getFileName(file.getName());
            if (!TextUtils.isEmpty(fileName)) {
                AdDao.update(context, getTableName(isToday), fileName, AdDao.adVideoLength, String.valueOf(fileSize));
                LogCat.e("video", "文件修改成功......." + AdDao.queryDetail(context, getTableName(isToday), AdDao.adVideoName, fileName).adVideoLength);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startThreadWithSql(URL url, int blockSize, int size, ArrayList<DownloadInfo> downloadInfos, int fileSize) {
        for (int i = 0; i < size; i++) {
            // 启动线程，分别下载每个线程需要下载的部分
            int threadId = i + 1;
            DownloadInfo downloadInfo = downloadInfos.get(i);
            LogCat.e("video", "记录位置的startPos: " + downloadInfo.startPos);
            LogCat.e("video", "记录位置的endPos: " + downloadInfo.endPos);
            threads[i] = new DownloadThread(context, url, file, blockSize, threadId, downloadInfo);
            threads[i].start();
        }
    }

    private String getTableName(boolean isToday) {
        String table = null;
        if (isToday)
            table = AdDao.DBBASE_TD_VIDEOS_TABLE_NAME;
        else
            table = AdDao.DBBASE_TM_VIDEOS_TABLE_NAME;
        return table;
    }

    private void startThreadWithOutSql(URL url, int fileSize, int blockSize, int size) {
        // 插入数据前，将所有的表清空
        DLDao.delete(context);


        for (int i = 0; i < size; i++) {
            // 启动线程，分别下载每个线程需要下载的部分
            int threadId = i + 1;

            // 插入数据
            DownloadInfo downloadInfo = new DownloadInfo();
            downloadInfo.tid = threadId;
            downloadInfo.turl = downloadUrl;
            try {
                downloadInfo.tname = ToolUtils.getFileName(file.getName());
            } catch (Exception e) {
                e.printStackTrace();
            }

            downloadInfo.tlength = fileSize;

            boolean blockIsAdd = fileSize % threadNum == 0;

//            int sizeBlock = i * (fileSize / threadNum);
//            int startPos = sizeBlock;
//            int endPos = 0;
//            //设置最后一个结束点的位置
//            if (i == threadNum - 1) {
//                endPos = fileSize;
//            } else {
//                sizeBlock = (i + 1) * (fileSize / threadNum);
//                endPos = sizeBlock;
//            }
//            LogCat.e("video", "start-end Position[" + i + "]: " + startPos + "-" + endPos);


            long startPos = blockSize * (threadId - 1);//开始位置
            long endPos = 0;
            if (blockIsAdd) {
                endPos = blockSize * threadId - 1;//结束位置
            } else {
                endPos = blockSize * threadId - 2;//结束位置
            }

            downloadInfo.startPos = startPos;

//            if(endPos > fileSize - 1){
//                LogCat.e("video", "startThreadWithOutSql......endPos值不对 ，需要处理");
//                downloadInfo.endPos = fileSize - 1;
//            }else {
            downloadInfo.endPos = endPos;
//            }


            LogCat.e("video", "startThreadWithOutSql......startPos: " + startPos);
            LogCat.e("video", "startThreadWithOutSql......endPos: " + endPos);

            LogCat.e("将当前的下载加入数据表......");
            DLDao.insert(context, downloadInfo);

            threads[i] = new DownloadThread(context, url, file, blockSize, threadId, null);
            threads[i].start();

        }
        ArrayList<DownloadInfo> downloadInfos1 = DLDao.queryAll(context);
        for (DownloadInfo downloadInfo : downloadInfos1) {
            LogCat.e("downloadInfo.tname......" + downloadInfo.tname);
            LogCat.e("downloadInfo.turl......" + downloadInfo.turl);
            LogCat.e("downloadInfo.startPos......" + downloadInfo.startPos);
            LogCat.e("--------------------------");
        }
        LogCat.e("插入后的数据大小......" + DLDao.queryAll(context).size());
    }

    private boolean deleteFailFile(int fileSize, int downloadSize) {
        if (file != null && downloadSize != fileSize) {
            LogCat.e("video", "文件下载大小出错......删除出错的文件");
            if (file.delete()) {
                LogCat.e("video", "文件下载大小出错......删除成功");

            } else {
                LogCat.e("video", "文件下载大小出错......删除出错");
            }
            return false;
        }
        return true;
    }

    private HttpURLConnection getHttpURLConnection(URL url) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(CONNECT_TIME_OUT);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, application/xaml+xml, application/vnd.ms-xpsdocument, application/x-ms-xbap, application/x-ms-application, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*");
            connection.setRequestProperty("Accept-Language", "zh-CN");
            connection.setRequestProperty("Referer", downloadUrl);
            connection.setRequestProperty("Charset", "UTF-8");
            connection.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.2; Trident/4.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)");
            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setReadTimeout(CONNECT_TIME_OUT);

            System.setProperty("sun.net.client.defaultConnectTimeout", String.valueOf(CONNECT_TIME_OUT));
            System.setProperty("sun.net.client.defaultReadTimeout", String.valueOf(CONNECT_TIME_OUT));
            connection.connect();
        } catch (IOException e) {
            e.printStackTrace();
            errorCode = ErrorCodes.ERROR_DOWNLOAD_CONN;
        }
        return connection;
    }


    private void setDownloadMsg(long code) {
        if (listener != null) {
            listener.onProgress(code);
        }
    }

    private void setErrorMsg(int errorCode) {
        DLUtils.clearDownloadStatus();
        if (listener != null) {
            listener.onError(errorCode);
        }
    }

    private void setCancel() {
        if (listener != null) {
            listener.onCancel();
        }
    }

    private void setFinish(String filePath) {
        if (listener != null) {
            listener.onFinish(filePath);
        }
    }


    public void cancelDownload() {
        isCancel = true;
        if (threads != null) {
            for (DownloadThread thread : threads) {
                if (thread != null) {
                    thread.cancel();
                }
            }
        }

    }


    private synchronized void cacheVideoList(ArrayList<AdDetailResponse> cachePlayVideoLists) {
        new CacheVideoListThread(cachePlayVideoLists, ToolUtils.getCacheDirectory(), Constants.FILE_CACHE_TD_NAME).start();
        LogCat.e("video", "文件缓存成功.........");
    }


}