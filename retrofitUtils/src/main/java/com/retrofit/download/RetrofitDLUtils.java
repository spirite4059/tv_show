package com.retrofit.download;

import android.content.Context;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.retrofit.tools.Tools;

import okhttp3.ResponseBody;
import retrofit2.Call;

import static com.retrofit.tools.Tools.checkDownloadInfo;
import static com.retrofit.tools.Tools.getRealPath;

/**
 * Created by fq_mbp on 16/7/20.
 */

public class RetrofitDLUtils {

    private Call<ResponseBody> call;
    private ProgressHandler progressHandler;
    private String downloadingUrl;
    private DownloadCallback downloadCallback;

    private RetrofitDLUtils(){
        init();
    }

    public static RetrofitDLUtils getInstance(){
        return Singleton.instance;
    }



    //在第一次被引用时被加载
    private static class Singleton {
        private static RetrofitDLUtils instance = new RetrofitDLUtils();
    }

    private void init(){


    }


    public void download(Context context, String filePath, String fileName, String url, DownloadStatusListener listener){
        if (!Tools.isExistSDCard()) {
            listener.onError("sdcard不可用");
            return;
        }
        if(TextUtils.isEmpty(fileName) && TextUtils.isEmpty(filePath)){
            listener.onError("文件名或路径为空");
            return;
        }

        if(TextUtils.isEmpty(url)){
            listener.onError("下载地址为空");
            return;
        }

        if(!TextUtils.isEmpty(downloadingUrl) && downloadingUrl.equals(url)){
            Log.e("retrofit_dl", "当前视频正在下载中......");
            return;
        }

        fileName = "甘肃行still";
        url = "http://video.ottcloud.tv/publicspace/videos/dst/cp/71812_20160718160001/71812.mp4";

        ProgressHandler progressHandler = new ProgressHandler(Looper.getMainLooper(), context, listener);

        DownloadApi retrofit = RetrofitUtils.getRetrofit(progressHandler);

        downloadingUrl = url;
        // 取消之前的下载请求
        cancel();

        // 查询sql,看是否有下载记录,有就取记录
        String range = checkDownloadInfo(context, fileName, filePath);

        // 获取path路径
        String path = getRealPath(url);

        if(TextUtils.isEmpty(range)){
            call = retrofit.getFile(path);
        }else {
            call = retrofit.getFile(range, path);
        }
        call.enqueue(new DownloadCallback(context, filePath, fileName, url, progressHandler));
    }



    public void cancel(){
        if(call != null && !call.isCanceled()){
            call.cancel();
            call = null;
        }
    }
}
