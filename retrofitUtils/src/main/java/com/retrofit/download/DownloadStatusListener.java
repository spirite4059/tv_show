package com.retrofit.download;

/**
 * Created by fq_mbp on 16/7/19.
 */

public interface DownloadStatusListener {

    /**
     * @param progress     已经下载或上传字节数
     * @param total        总字节数
     * @param done         是否完成
     */
    void onProgress(long progress, long total, boolean done);

    void onError(String msg);


}
