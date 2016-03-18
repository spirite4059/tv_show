package com.gochinatv.ad.interfaces;

import com.httputils.http.response.UpdateResponse;

/**
 * Created by fq_mbp on 16/3/17.
 */
public interface OnUpgradeStatusListener {

    void onDownloadApkSuccess(String filePath);

    void onDownloadApkError(UpdateResponse.UpdateInfoResponse updateInfoResponse);

}
