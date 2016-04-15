package com.gochinatv.ad.tools;

import com.download.DLUtils;
import com.download.dllistener.OnDownloadStatusListener;
import com.gochinatv.ad.interfaces.OnUpgradeStatusListener;

import java.math.BigDecimal;

import static com.download.ErrorCodes.ERROR_DOWNLOADING_READ;
import static com.download.ErrorCodes.ERROR_DOWNLOAD_BUFFER_IN;
import static com.download.ErrorCodes.ERROR_DOWNLOAD_CONN;
import static com.download.ErrorCodes.ERROR_DOWNLOAD_EXCUTORS;
import static com.download.ErrorCodes.ERROR_DOWNLOAD_FILE_LOCAL;
import static com.download.ErrorCodes.ERROR_DOWNLOAD_FILE_NULL;
import static com.download.ErrorCodes.ERROR_DOWNLOAD_FILE_SIZE;
import static com.download.ErrorCodes.ERROR_DOWNLOAD_FILE_UNKNOWN;
import static com.download.ErrorCodes.ERROR_DOWNLOAD_RANDOM;
import static com.download.ErrorCodes.ERROR_DOWNLOAD_RANDOM_SEEK;
import static com.download.ErrorCodes.ERROR_DOWNLOAD_URL;
import static com.download.ErrorCodes.ERROR_DOWNLOAD_WRITE;
import static com.download.ErrorCodes.ERROR_THREAD_NUMBERS;

/**
 * Created by fq_mbp on 16/3/17.
 */
public class DownloadUtils {


    public static void download(String dir, String fileName, String fileUrl, final OnUpgradeStatusListener listener) {
        String path = DataUtils.getSdCardFileDirectory() + dir;
        DLUtils.init().download(path, fileName, fileUrl, 2, new OnDownloadStatusListener() {

            private long fileLength;

            @Override
            public void onError(int errorCode) {
                LogCat.e("onDownloadFileError............. " + errorCode + ",  " + getErrorMsg(errorCode));
                listener.onDownloadFileError(errorCode, getErrorMsg(errorCode));
            }


            @Override
            public void onPrepare(long fileSize) {
                LogCat.e("fileSize............. " + fileSize);
                fileLength = fileSize;

            }

            @Override
            public void onProgress(long progress) {
                if (fileLength == 0) {
                    return;
                }
                logProgress(progress);


            }

            @Override
            public void onFinish(String filePath) {
                LogCat.e("DownloadUtils -> onFinish......");
                listener.onDownloadFileSuccess(filePath);
            }

            @Override
            public void onCancel() {
                LogCat.e("onCancel............. ");
            }

            @Override
            public void onDownloading(String fileName) {
                LogCat.e("当前下载正在进行中............. " + fileName);
            }

            private void logProgress(long progress) {
                double size = (int) (progress / 1024);
                String sizeStr;
                int s = (int) (progress * 100 / fileLength);
                if (size > 1000) {
                    size = (progress / 1024) / 1024f;
                    BigDecimal b = new BigDecimal(size);
                    double f1 = b.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
                    sizeStr = String.valueOf(f1 + "MB，  ");
                } else {
                    sizeStr = String.valueOf((int) size + "KB，  ");
                }
                LogCat.e("progress............. " + sizeStr + s + "%");
                listener.onDownloadProgress(sizeStr + s + "%");
            }
        });

//        SingleDLUtils singleDLUtils = new SingleDLUtils();
//        SingleDLUtils.init().download(path, fileName, fileUrl, new OnDownloadStatusListener() {
//
//            private long fileLength;
//
//            @Override
//            public void onError(int errorCode, String errorMsg) {
//                LogCat.e("onDownloadFileError............. " + errorCode + ",  " + errorMsg);
//                listener.onDownloadFileError(errorCode, errorMsg);
//            }
//
//
//            @Override
//            public void onPrepare(long fileSize) {
//                LogCat.e("fileSize............. " + fileSize);
//                fileLength = fileSize;
//
//            }
//
//            @Override
//            public void onProgress(long progress) {
//                if (fileLength == 0) {
//                    return;
//                }
//                logProgress(progress);
//
//
//            }
//
//            @Override
//            public void onFinish(String filePath) {
//                LogCat.e("DownloadUtils -> onFinish......");
//                listener.onDownloadFileSuccess(filePath);
//            }
//
//            @Override
//            public void onCancel() {
//                LogCat.e("onCancel............. ");
//
//
//            }
//
//            @Override
//            public void onDownloading(String fileName) {
//                LogCat.e("当前下载正在进行中............. " + fileName);
//            }
//
//            private void logProgress(long progress) {
//                double size = (int) (progress / 1024);
//                String sizeStr;
//                int s = (int) (progress * 100 / fileLength);
//                if (size > 1000) {
//                    size = (progress / 1024) / 1024f;
//                    BigDecimal b = new BigDecimal(size);
//                    double f1 = b.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
//                    sizeStr = String.valueOf(f1 + "MB，  ");
//                } else {
//                    sizeStr = String.valueOf((int) size + "KB，  ");
//                }
//                LogCat.e("progress............. " + sizeStr + s + "%");
//                listener.onDownloadProgress(sizeStr + s + "%");
//            }
//
//        });

    }


    public static void cancel(){
//        SingleDLUtils.init().cancel();
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
            default:
                errorMsg = "error：未知的异常";
                break;
        }


        return errorMsg;
    }

}
