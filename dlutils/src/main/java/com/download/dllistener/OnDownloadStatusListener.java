package com.download.dllistener;

import java.io.File;


public interface OnDownloadStatusListener {

    void onError(int errorCode, String errorMsg);

    void onPrepare(long fileSize);

    void onProgress(long progress);

    void onFinish(File file);

    void onCancel();
}
