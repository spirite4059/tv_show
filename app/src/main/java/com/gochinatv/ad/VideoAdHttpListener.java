package com.gochinatv.ad;

import com.httputils.http.response.VideoDetailListResponse;

/**
 * Created by fq_mbp on 15/12/24.
 */
public interface VideoAdHttpListener {

    void onSuccess(VideoDetailListResponse response, String url);

    void onFailed(String errorMsg, String url);

}
