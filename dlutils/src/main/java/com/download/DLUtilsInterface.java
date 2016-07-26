package com.download;

import com.download.dllistener.OnDownloadStatusListener;

/**
 * Created by fq_mbp on 16/7/26.
 */

public interface DLUtilsInterface {

    void setDLUtils(DLInterface utils);

    void download(String fileName, String filePath, String url, OnDownloadStatusListener listener);

    void cancel();

}
