package com.okhtttp;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.tools.LogCat;
import com.tools.MacUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


/**
 * Created by fq_mbp on 16/3/14.
 */
public class OkHttpUtils {

    private static OkHttpUtils mInstance;
    private OkHttpClient mOkHttpClient;
    private Handler mDelivery;
    private Gson mGson;
    private static final int OK_HTTP_READ_TIME_OUT = 30000;
    private static final int OK_HTTP_WRITE_TIME_OUT = 30000;
    private static final int OK_HTTP_CONNECT_TIME_OUT = 30000;

    private static final int OK_HTTP_CACHE_SIZE = 1024 * 1024 * 10;   // 缓存200K

    private static final String APP_NAME = "VegoPlus";
    private static final String CACHE_DIRECTORY = "cache";

    private long downloadProgress;
    private boolean isDownloadFinish;


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
                        try {
                            sendSuccessResultCallback(url, mGson.<T>fromJson(json, okHttpCallBack.mType), okHttpCallBack);
                        }catch (Exception e){
                            sendFailedStringCallback(url, e, okHttpCallBack);
                        }
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
//                    LogCat.e("json: " + json);
//                    LogCat.e("realUrl: " + realUrl);
                    if (!TextUtils.isEmpty(json)) {
                        try {
                            sendSuccessResultCallback(realUrl, mGson.<T>fromJson(json, okHttpCallBack.mType), okHttpCallBack);
                        }catch (Exception e){
                            sendFailedStringCallback(realUrl, e, okHttpCallBack);
                        }
                    }
                }
            }
        });
    }


    public void doFileDownload(final String url, final String path, final String fileName, final OnDownloadStatusListener listener) {
        final Request request = new Request.Builder()
                .url(url)
                .build();
        final Call call = mOkHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                new DownLoadStatusThread(path, fileName, response, listener).start();
            }
        });

    }






    public void doUploadFile(Context context, File file, String url, long duration, String name) throws IOException {
        RequestBody fileBody = RequestBody.create(MediaType.parse("application/octet-stream"), file);

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addPart(fileBody)
                .addFormDataPart("file", file.getName(), fileBody)
                .addFormDataPart("mac", MacUtils.getMacAddress(context))
                .addFormDataPart("duration", String.valueOf(duration))
                .addFormDataPart("name", name)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        final Call call = mOkHttpClient.newCall(request);

        Response response = call.execute();
        if(response.isSuccessful()){
            LogCat.e("screenShot", "上传成功。。。。。");
        }else {
            LogCat.e("screenShot", "截屏上传失败。。。。。");
        }
    }


    private boolean isCancelDL;

    public void cancelFileDownloading() {
        isCancelDL = true;
    }


    private boolean isCancel(OnDownloadStatusListener listener) {
        if (isCancelDL) {
            if (listener != null) {
                listener.onCancel();
            }
            return true;
        }
        return false;
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


    private String getParamsUrl(String url, Map params) {
        if (params == null || params.size() == 0) {
            return url;
        }
        StringBuilder sbUrl = new StringBuilder(url);
        sbUrl.append("?");
        Set<String> keySet = params.keySet();
        int size = keySet.size();
        Iterator<String> iterator = keySet.iterator();
        for (int i = 0; i < size; i++) {
            String key = iterator.next();
            sbUrl.append(key);
            sbUrl.append("=");
            sbUrl.append(params.get(key));
            if (i < size - 1) {
                sbUrl.append("&");
            }

        }
        return sbUrl.toString();
    }


    private class DownLoadStatusThread extends Thread {

        private Response response;
        private String path;
        private String fileName;
        private OnDownloadStatusListener listener;

        public DownLoadStatusThread(String path, String fileName, Response response, OnDownloadStatusListener listener) {
            this.path = path;
            this.fileName = fileName;
            this.response = response;
            this.listener = listener;
        }

        @Override
        public void run() {
            int len = 0;
            InputStream is = null;
            FileOutputStream fos = null;
            if (isCancel(listener)) {
                return;
            }
            byte[] buf = new byte[2048];
            if (isCancel(listener)) {
                return;
            }
            try {
                is = response.body().byteStream();

                if (isCancel(listener)) {
                    return;
                }

                final long total = response.body().contentLength();


                if (isCancel(listener)) {
                    return;
                }


                if (listener != null) {
                    listener.onPrepare(total);
                }

                long sum = 0;
                File dir = new File(path);
                LogCat.e("path: " + path);
                if (!dir.exists() && !dir.isDirectory()) {
                    LogCat.e("文件目录不存在 " + path);
                    if (dir.mkdirs()) {
                        LogCat.e("文件目录创建成功 " + path);
                    }

                } else {
                    LogCat.e("文件目录存在 " + path);
                }

                if (isCancel(listener)) {
                    return;
                }

                File file = new File(dir, fileName);
                if (!file.exists()) {
                    LogCat.e("文件不存在: " + fileName);
                    if (file.createNewFile()) {
                        LogCat.e("文件创建成功: " + fileName);
                    }
                } else {
                    LogCat.e("文件已经存在: " + fileName);
                }


                if (isCancel(listener)) {
                    return;
                }

                fos = new FileOutputStream(file);

                if (isCancel(listener)) {
                    return;
                }

                // 回调当前下载进度
                new DownloadProgressThread(listener).start();

                while ((len = is.read(buf)) != -1) {
                    if (isCancel(listener)) {
                        break;
                    }

                    sum += len;
                    fos.write(buf, 0, len);
                    downloadProgress = sum;

                    if (isCancel(listener)) {
                        break;
                    }

                }


                if (isCancelDL) {
                    isCancel(listener);
                } else {
                    isDownloadFinish = true;
                    if (listener != null) {
                        listener.onFinish(path + fileName);
                    }
                }
                fos.flush();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (is != null) is.close();
                } catch (IOException e) {
                }
                try {
                    if (fos != null) fos.close();
                } catch (IOException e) {
                }

            }


        }
    }


    private class DownloadProgressThread extends Thread {

        private OnDownloadStatusListener listener;

        public DownloadProgressThread(OnDownloadStatusListener listener) {
            this.listener = listener;
        }

        @Override
        public void run() {
            super.run();
            while (!isDownloadFinish && !isCancelDL) {
                mDelivery.post(new Runnable() {
                    @Override
                    public void run() {
                        if (listener != null) {
                            listener.onProgress(downloadProgress);
                        }
                    }
                });

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    }


    //文件下载线程
    class DownloadThread extends Thread {
        long start;
        long end;
        int threadId;
        File file = null;
        InputStream inStream = null;

        public DownloadThread(int threadId, long block, File file, InputStream is) {
            this.threadId = threadId;
            start = block * threadId;
            end = block * (threadId + 1) - 1;
            this.file = file;
            this.inStream = is;
        }

        public void run() {
            try {
                //此步骤是关键。
                RandomAccessFile raf = new RandomAccessFile(file, "rw");
                //移动指针至该线程负责写入数据的位置。
                raf.seek(start);
                //读取数据并写入
                byte[] b = new byte[1024];
                int len = 0;
                while ((len = inStream.read(b)) != -1) {
                    raf.write(b, 0, len);
                }
                System.out.println("线程" + threadId + "下载完毕");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
