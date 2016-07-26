package com.download;

import com.download.dllistener.OnDownloadStatusListener;

/**
 * Created by fq_mbp on 16/7/26.
 */

public class DLUtilsImpl implements DLUtilsInterface {

    private DLInterface utils;

    @Override
    public void setDLUtils(DLInterface utils) {
        this.utils = utils;
    }

    @Override
    public void download(String fileName, String filePath, String url, OnDownloadStatusListener listener) {
        utils.start(fileName, filePath, url, listener);
    }

    @Override
    public void cancel() {
        utils.cancel();
    }
}
