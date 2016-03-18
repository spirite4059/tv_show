package com.gochinatv.ad.interfaces;

/**
 * Created by fq_mbp on 16/3/17.
 */
public interface OnUpgradeStatusListener {

    void onDownloadFileSuccess(String filePath);

    void onDownloadFileError(int errorCode, String errorMsg);

}
