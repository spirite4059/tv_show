package com.download.dllistener;

public interface OnDownloadStatusListener {

    void onError(int errorCode);

    void onPrepare(long fileSize);

    void onProgress(long progress);

    void onFinish(String filePath);

    void onCancel();

    void onDownloading(String fileName);
}
