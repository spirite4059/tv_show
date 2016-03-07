package com.gochinatv.ad.download;

/**
 * Created by fq_mbp on 16/2/29.
 */
public interface OnDownloadStatusListener {

    void onError(int errorCode, String errorMsg);

    void onPrepare(long fileSize);

    void onProgress(long progress);

    void onFinish();


}
