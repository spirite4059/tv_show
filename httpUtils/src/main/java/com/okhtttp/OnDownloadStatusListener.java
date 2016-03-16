package com.okhtttp;

public interface OnDownloadStatusListener {

    void onError(int errorCode, String errorMsg);

    void onPrepare(long fileSize);

    void onProgress(long progress);

    void onFinish(String filePath);

    void onCancel();
}
