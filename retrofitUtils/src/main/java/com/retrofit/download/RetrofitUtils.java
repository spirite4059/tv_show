package com.retrofit.download;

import com.retrofit.tools.Constants;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by fq_mbp on 16/7/22.
 */

public class RetrofitUtils {

    public static DownloadApi getRetrofit(ProgressHandler progressHandler){

        Retrofit.Builder retrofitBuilder = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(Constants.BASE_URL);

        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.readTimeout(60, TimeUnit.SECONDS)
                .connectTimeout(60, TimeUnit.SECONDS)
                .networkInterceptors()
                .add(new ProgressInterceptor(new ProgressResponseBody(progressHandler)));

        return retrofitBuilder
                .client(builder.build())
                .build().create(DownloadApi.class);
    }


}
