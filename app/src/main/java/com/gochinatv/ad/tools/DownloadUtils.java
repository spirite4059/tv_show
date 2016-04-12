package com.gochinatv.ad.tools;

import android.content.Context;

import com.download.DLUtils;
import com.download.dllistener.OnDownloadStatusListener;
import com.gochinatv.ad.interfaces.OnUpgradeStatusListener;

import java.math.BigDecimal;

/**
 * Created by fq_mbp on 16/3/17.
 */
public class DownloadUtils {

    private static final int MAX_THREAD_NUMBER = 2;

    public static void download(Context context, String dir, String fileName, final String fileUrl, final OnUpgradeStatusListener listener){
        String path = DataUtils.getSdCardFileDirectory() + dir;
        DLUtils.init(context).download(path, fileName, fileUrl, MAX_THREAD_NUMBER, new OnDownloadStatusListener() {

            private long fileLength;

            @Override
            public void onError(int errorCode, String errorMsg) {
                LogCat.e("onDownloadFileError............. " + errorCode + ",  " + errorMsg);
                listener.onDownloadFileError(errorCode, errorMsg);
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
            }
        });
    }
}
