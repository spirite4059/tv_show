package com.download;

import android.content.Context;

import com.download.dllistener.OnDownloadStatusListener;

/**
 * Created by fq_mbp on 16/7/26.
 */

public interface DLInterface {

    void start(String fileName, String filePath, String url, OnDownloadStatusListener listener);

    void cancel();

    void getReport(Context context);

    void deleteReport(Context context);


}
