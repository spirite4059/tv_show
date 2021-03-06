package com.gochinatv.ad.tools;

import android.content.Context;
import android.text.TextUtils;

import com.download.DLUtils;
import com.download.ErrorCodes;
import com.download.dllistener.OnDownloadStatusListener;
import com.gochinatv.ad.interfaces.OnUpgradeStatusListener;
import com.gochinatv.db.AdDao;

import java.text.NumberFormat;

import static com.download.ErrorCodes.ERROR_DOWNLOADING_READ;
import static com.download.ErrorCodes.ERROR_DOWNLOAD_BUFFER_IN;
import static com.download.ErrorCodes.ERROR_DOWNLOAD_CONN;
import static com.download.ErrorCodes.ERROR_DOWNLOAD_EXCUTORS;
import static com.download.ErrorCodes.ERROR_DOWNLOAD_FILE_LOCAL;
import static com.download.ErrorCodes.ERROR_DOWNLOAD_FILE_NULL;
import static com.download.ErrorCodes.ERROR_DOWNLOAD_FILE_SIZE;
import static com.download.ErrorCodes.ERROR_DOWNLOAD_FILE_UNKNOWN;
import static com.download.ErrorCodes.ERROR_DOWNLOAD_NO_INSTREAM;
import static com.download.ErrorCodes.ERROR_DOWNLOAD_RANDOM;
import static com.download.ErrorCodes.ERROR_DOWNLOAD_RANDOM_SEEK;
import static com.download.ErrorCodes.ERROR_DOWNLOAD_SDCARD_SPACE;
import static com.download.ErrorCodes.ERROR_DOWNLOAD_URL;
import static com.download.ErrorCodes.ERROR_DOWNLOAD_WRITE;
import static com.download.ErrorCodes.ERROR_THREAD_NUMBERS;
import static com.gochinatv.ad.tools.VideoAdUtils.getTableName;

/**
 * Created by fq_mbp on 16/3/17.
 */
public class DownloadUtils {

    private static final int THREAD_NUMBER = 1;

    public static void download(final boolean isToday, final Context context, String dir, final String fileName, final String fileUrl, final OnUpgradeStatusListener listener) {
        if(context == null){
            return;
        }
        Context appContext = null;

        try {
            appContext = context.getApplicationContext();
        }catch (Exception e){
            e.printStackTrace();

        }
        if(appContext == null){
            return;
        }

        DLUtils.init().download(context, dir, fileName, fileUrl, THREAD_NUMBER, new OnDownloadStatusListener() {

            private long fileLength;

            @Override
            public void onError(int errorCode) {
                LogCat.e("video", "onDownloadFileError............. " + errorCode + ",  " + getErrorMsg(errorCode));
                listener.onDownloadFileError(errorCode, getErrorMsg(errorCode));
            }


            @Override
            public void onPrepare(long fileSize) {
                LogCat.e("video", "fileSize............. " + fileSize);
                fileLength = fileSize;
                // 修改文件大小
                try {
                    if (!TextUtils.isEmpty(fileName) && fileSize != 0) {
                        String fileRealName = getFileName(fileName);
                        LogCat.e("video", "要修改的文件修改......." + fileRealName);

                        String tableName = getTableName(isToday);
//                        AdDetailResponse adDetailResponse = AdDao.queryDetailById(context, tableName, Integer.parseInt(fileRealName));
//                        if(adDetailResponse == null){
//                            LogCat.e("video", "找不到当前id视频.......");
//                        }else {
//                            LogCat.e("video", "找到当前id视频......." + adDetailResponse.adVideoId);
//                        }
                        try {
                            int vid = Integer.parseInt(fileRealName);
                            AdDao.update(context, tableName, vid, AdDao.adVideoLength, String.valueOf(fileSize));
                            LogCat.e("video", "文件修改成功......." + AdDao.queryDetail(context, tableName, AdDao.adVideoId, fileRealName).adVideoLength);
                        }catch (Exception e){
                            e.printStackTrace();
                        }

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onProgress(long progress) {
                if (fileLength == 0) {
                    return;
                }
                listener.onDownloadProgress(progress, fileLength);
                logProgress(progress);


            }

            @Override
            public void onFinish(String filePath) {
                LogCat.e("video", "DownloadUtils -> onFinish......");
                listener.onDownloadFileSuccess(filePath);
            }

            @Override
            public void onCancel() {
                LogCat.e("video", "onCancel............. ");
            }

            @Override
            public void onDownloading(String fileName) {
//                LogCat.e("video", "当前下载正在进行中............. " + fileName);
            }
            NumberFormat numberFormat;
            private void logProgress(long progress) {
                float size =  progress / 1024f;
                String sizeStr;
                int s = (int) (progress * 100 / fileLength);
                if (size > 1024) {
                    size = progress / 1048576f;
                    if(numberFormat == null){
                        numberFormat = NumberFormat.getNumberInstance();
                    }
                    numberFormat.setMaximumFractionDigits(2);
                    sizeStr = String.valueOf(numberFormat.format(size) + "MB，  ");
                } else {
                    sizeStr = String.valueOf((int) size + "KB，  ");
                }
                LogCat.e("video", "progress............. " + sizeStr + s + "%");
//                listener.onDownloadProgress(sizeStr + s + "%");

            }
        });


//        RetrofitDLUtils.getInstance().start(context, dir, fileName, fileUrl, new DownloadStatusListener() {
//            @Override
//            public void onProgress(long progress, long total, boolean done) {
//
//            }
//
//            @Override
//            public void onError(String msg) {
//
//            }
//        });


    }


    public static void downloadApk(final Context context, String fileUrl, final OnUpgradeStatusListener listener) {
        if(context == null){
            return;
        }
        Context appContext = null;

        try {
            appContext = context.getApplicationContext();
        }catch (Exception e){
            e.printStackTrace();

        }
        if(appContext == null){
            return;
        }


        DLUtils.init().download(context, DataUtils.getApkDirectory(), Constants.FILE_APK_NAME, fileUrl, THREAD_NUMBER, new OnDownloadStatusListener() {

            private long fileLength;

            @Override
            public void onError(int errorCode) {
                LogCat.e("video", "onDownloadFileError............. " + errorCode + ",  " + getErrorMsg(errorCode));
                listener.onDownloadFileError(errorCode, getErrorMsg(errorCode));
            }


            @Override
            public void onPrepare(long fileSize) {
                LogCat.e("video", "fileSize............. " + fileSize);
                fileLength = fileSize;
            }

            @Override
            public void onProgress(long progress) {
                if (fileLength == 0) {
                    return;
                }
                listener.onDownloadProgress(progress, fileLength);
//                logProgress(progress);
            }

            @Override
            public void onFinish(String filePath) {
                LogCat.e("video", "DownloadUtils -> onFinish......");
                listener.onDownloadFileSuccess(filePath);
            }

            @Override
            public void onCancel() {
                LogCat.e("video", "onCancel............. ");
            }

            @Override
            public void onDownloading(String fileName) {
                LogCat.e("video", "当前下载正在进行中............. " + fileName);
            }

//            private void logProgress(long progress) {
//                double size = (int) (progress / 1024);
//                String sizeStr;
//                int s = (int) (progress * 100 / tlength);
//                if (size > 1000) {
//                    size = (progress / 1024) / 1024f;
//                    BigDecimal b = new BigDecimal(size);
//                    double f1 = b.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
//                    sizeStr = String.valueOf(f1 + "MB，  ");
//                } else {
//                    sizeStr = String.valueOf((int) size + "KB，  ");
//                }
//                LogCat.e("video", "progress............. " + sizeStr + s + "%");
//            }
        });
    }



    private static String getErrorMsg(int errorCode) {
        String errorMsg = null;
        switch (errorCode) {
            case ERROR_DOWNLOAD_WRITE:
                errorMsg = "error：输入流写入文件过程出错";
                break;
            case ERROR_DOWNLOADING_READ:
                errorMsg = "error：读取文件流过程出错";
                break;
            case ERROR_THREAD_NUMBERS:
                errorMsg = "error：多线程的线程数小于0";
                break;
            case ERROR_DOWNLOAD_URL:
                errorMsg = "error：下载地址URL生成出错或null";
                break;
            case ERROR_DOWNLOAD_CONN:
                errorMsg = "error：URL的链接过程出错或null";
                break;
            case ERROR_DOWNLOAD_FILE_SIZE:
                errorMsg = "error：下载文件的大小异常";
                break;
            case ERROR_DOWNLOAD_FILE_LOCAL:
                errorMsg = "error：下载文件的存储地址异常";
                break;
            case ERROR_DOWNLOAD_FILE_NULL:
                errorMsg = "error：无法生成下载文件";
                break;
            case ERROR_DOWNLOAD_BUFFER_IN:
                errorMsg = "error：输入流异常或null";
                break;
            case ERROR_DOWNLOAD_RANDOM:
                errorMsg = "error：随机流异常或null";
                break;
            case ERROR_DOWNLOAD_RANDOM_SEEK:
                errorMsg = "error：随机流定为异常";
                break;
            case ERROR_DOWNLOAD_FILE_UNKNOWN:
                errorMsg = "error：下载文件大小出错";
                break;
            case ERROR_DOWNLOAD_EXCUTORS:
                errorMsg = "error：线程池出错";
                break;
            case ErrorCodes.ERROR_DB_UPDATE:
                errorMsg = "error：数据库更新下载内容时出错";
                break;
            case ERROR_DOWNLOAD_SDCARD_SPACE:
                errorMsg = "error：磁盘空间不足";
                break;
            case ERROR_DOWNLOAD_NO_INSTREAM:
                errorMsg = "error：长时间没有下载,就主动报错";
                break;

            default:
                errorMsg = "error：未知的异常";
                break;
        }


        return errorMsg;
    }

    private static String getFileName(String sourceName){
        String fileName = null;
        try {
            if(!TextUtils.isEmpty(sourceName) && sourceName.contains(com.download.tools.Constants.FILE_DOWNLOAD_EXTENSION)){
                int index = sourceName.lastIndexOf(com.download.tools.Constants.FILE_DOWNLOAD_EXTENSION);
                fileName = sourceName.substring(0, index);
            }else {
                fileName = sourceName;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return fileName;
    }



    public static void deleteErrorDl(Context context){
        DLUtils.deleteDlMsg(context);
    }
}
