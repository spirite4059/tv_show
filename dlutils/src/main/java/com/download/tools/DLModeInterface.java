package com.download.tools;

import com.download.dllistener.OnDownloadStatusListener;

import java.io.File;

/**
 * Created by fq_mbp on 16/7/26.
 */

public interface DLModeInterface {

    boolean checkInfos(String fileName, String filePath, String url, OnDownloadStatusListener listener);

    File createFile(String fileName, String filePath);

    void download();

}
