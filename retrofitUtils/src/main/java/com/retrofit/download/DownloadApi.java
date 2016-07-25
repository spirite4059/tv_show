package com.retrofit.download;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

/**
 * Created by fq_mbp on 16/7/19.
 */

public interface DownloadApi {

    @GET
    @Streaming
    Call<ResponseBody> getFile(@Url String url);

    @GET
    @Streaming
    Call<ResponseBody> getFile(@Header("Range") String header, @Url String url);

}
