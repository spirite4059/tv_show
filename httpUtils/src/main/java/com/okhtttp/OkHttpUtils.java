package com.okhtttp;

import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.httputils.utils.LogCat;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


/**
 * Created by fq_mbp on 16/3/14.
 */
public class OkHttpUtils {

    private static OkHttpUtils mInstance;
    private OkHttpClient mOkHttpClient;
    private Handler mDelivery;
    private Gson mGson;
    private static final int OK_HTTP_READ_TIME_OUT = 10000;
    private static final int OK_HTTP_WRITE_TIME_OUT = 10000;
    private static final int OK_HTTP_CONNECT_TIME_OUT = 10000;

    private static final int OK_HTTP_CACHE_SIZE = 1024 * 1024 * 10;   // 缓存200K

    private static final String APP_NAME = "VegoPlus";
    private static final String CACHE_DIRECTORY = "cache";


    private OkHttpUtils() {
        mOkHttpClient = new OkHttpClient();
        OkHttpClient.Builder builder = mOkHttpClient.newBuilder();
        builder.connectTimeout(OK_HTTP_CONNECT_TIME_OUT, TimeUnit.MILLISECONDS);
        builder.readTimeout(OK_HTTP_READ_TIME_OUT, TimeUnit.MILLISECONDS);
        builder.writeTimeout(OK_HTTP_WRITE_TIME_OUT, TimeUnit.MILLISECONDS);
        Cache cache = getCache();
        if (cache != null) {
            builder.cache(cache);
        }
        builder.build();


        mDelivery = new Handler(Looper.getMainLooper());
        mGson = new Gson();
    }


    public static OkHttpUtils getInstance() {
        if (mInstance == null) {
            synchronized (OkHttpUtils.class) {
                if (mInstance == null) {
                    mInstance = new OkHttpUtils();
                }
            }
        }
        return mInstance;
    }


    public <T> void doHttpGet(final String url, final OkHttpCallBack<T> okHttpCallBack) {
        final Request request = new Request.Builder()
                .url(url)
                .build();
        Call call = mOkHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                sendFailedStringCallback(url, e, okHttpCallBack);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response != null && response.body() != null) {
                    String json = response.body().string();
                    if (!TextUtils.isEmpty(json)) {
                        sendSuccessResultCallback(url, mGson.<T>fromJson(json, okHttpCallBack.mType), okHttpCallBack);
                    }
                }
            }
        });
    }


    public <T> void doHttpGet(String url, Map params, final OkHttpCallBack<T> okHttpCallBack) {
        final String realUrl = getParamsUrl(url, params);
        final Request request = new Request.Builder()
                .url(realUrl)
                .build();
        Call call = mOkHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                sendFailedStringCallback(realUrl, e, okHttpCallBack);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response != null && response.body() != null) {
                    String json = response.body().string();
                    if (!TextUtils.isEmpty(json)) {
                        sendSuccessResultCallback(realUrl, mGson.<T>fromJson(json, okHttpCallBack.mType), okHttpCallBack);
                    }
                }
            }
        });
    }


    public void cancelCall(String url) {
        final Request request = new Request.Builder()
                .url(url)
                .build();
        Call call = mOkHttpClient.newCall(request);
        if (!call.isCanceled()) {
            call.cancel();
        }
    }


    /**
     * 检测sd卡状态是否可用
     */
    private boolean isSdCardUsefully() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }


    /**
     * 创建缓存目录   Vego/cache/
     *
     * @return
     */
    private Cache getCache() {
        Cache cache = null;
        if (isSdCardUsefully()) {
            // 创建总目录VegoPlus
            String vegoPath = Environment.getExternalStorageState() + APP_NAME + File.separator;
            File vegoFile = new File(vegoPath);
            if (!vegoFile.exists()) {
                if (vegoFile.mkdirs()) {
                    LogCat.e("VegoPlus 新创建成功......");
                }
            }

            String cachePath = vegoFile + CACHE_DIRECTORY + File.separator;

            File cacheFile = new File(cachePath);
            if (!cacheFile.exists()) {
                if (cacheFile.mkdirs()) {
                    LogCat.e("cache目录 新创建成功......");
                }
            }
            cache = new Cache(cacheFile, OK_HTTP_CACHE_SIZE);
        }
        return cache;
    }

    private <T> void sendFailedStringCallback(final String url, final Exception e, final OkHttpCallBack<T> okHttpCallBack) {
        mDelivery.post(new Runnable() {
            @Override
            public void run() {
                if (okHttpCallBack != null)
                    okHttpCallBack.onError(url, e.getMessage());
            }
        });
    }

    private <T> void sendSuccessResultCallback(final String url, final T response, final OkHttpCallBack<T> okHttpCallBack) {
        mDelivery.post(new Runnable() {
            @Override
            public void run() {
                if (okHttpCallBack != null) {
                    okHttpCallBack.onSuccess(url, response);
                }
            }
        });
    }


    private String getParamsUrl(String url, Map params){
        if(params == null || params.size() == 0){
            return url;
        }
        StringBuilder sbUrl = new StringBuilder(url);
        sbUrl.append("?");
        Set<String> keySet = params.keySet();
        int size = keySet.size();
        Iterator<String> iterator = keySet.iterator();
        for(int i = 0; i < size; i++){
            String key = iterator.next();
            sbUrl.append(key);
            sbUrl.append("=");
            sbUrl.append(params.get(key));
            if(i < size - 1){
                sbUrl.append("&");
            }

        }
        return sbUrl.toString();
    }

}
