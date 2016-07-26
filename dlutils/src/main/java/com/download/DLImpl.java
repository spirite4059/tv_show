package com.download;

import android.content.Context;

import com.download.dllistener.OnDownloadStatusListener;

/**
 * Created by fq_mbp on 16/7/26.
 */

public class DLImpl implements DLInterface {

    private DLImpl(){}

    private static class DLInner{
        private static DLImpl instance = new DLImpl();
    }

    public static DLImpl getInstance(){
        return DLInner.instance;
    }


    @Override
    public void start(String fileName, String filePath, String url, OnDownloadStatusListener listener) {

    }

    @Override
    public void cancel() {

    }

    @Override
    public void getReport(Context context) {

    }

    @Override
    public void deleteReport(Context context) {

    }
}
