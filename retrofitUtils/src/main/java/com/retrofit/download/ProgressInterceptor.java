package com.retrofit.download;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Response;

/**
 * Created by fq_mbp on 16/7/20.
 */

public class ProgressInterceptor implements Interceptor {

    private ProgressResponseBody progressResponseBody;

    public ProgressInterceptor(ProgressResponseBody progressResponseBody) {
        this.progressResponseBody = progressResponseBody;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        okhttp3.Response originalResponse = chain.proceed(chain.request());

        originalResponse.header("Connection", "Keep-Alive");

        Response builder = originalResponse.newBuilder()
                .body(progressResponseBody)
                .build();


        progressResponseBody.setResponseBody(originalResponse.body());
        return builder;
    }


}
