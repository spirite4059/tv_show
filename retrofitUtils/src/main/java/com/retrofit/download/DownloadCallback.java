package com.retrofit.download;


import android.content.Context;
import android.os.Message;
import android.util.Log;

import com.retrofit.download.db.DLDao;
import com.retrofit.download.db.DownloadInfo;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.retrofit.download.db.DLDao.query;
import static com.retrofit.tools.Tools.getThrowableString;

/**
 * Created by fq_mbp on 16/7/21.
 */

public class DownloadCallback implements Callback<ResponseBody> {

    private ProgressHandler progressHandler;
    private Context context;
    private String fileName;
    private String filePath;
    private String url;
    private DownloadFileThread downloadFileThread;


    public DownloadCallback(Context context, String filePath, String fileName, String url, ProgressHandler progressHandler){
        this.progressHandler = progressHandler;
        this.context = context;
        this.fileName = fileName;
        this.filePath = filePath;
        this.url = url;

    }

    @Override
    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
        if(response != null && response.isSuccessful() && response.body() != null && response.body().byteStream() != null){
            // 存储下载记录
            insertDlInfo(response);
            // 开下写文件线程
            downloadFileThread = new DownloadFileThread(context, filePath, fileName, progressHandler, response.body().byteStream());
            downloadFileThread.start();
        } else {
            doError(null);
        }
    }



    @Override
    public void onFailure(Call<ResponseBody> call, Throwable t) {
        doError(t);
    }

    private void doError(Throwable t){
        Message msg = progressHandler.obtainMessage(2);
        if(t != null){
            msg.obj = getThrowableString(t);
        }
        progressHandler.sendMessage(msg);
    }

    private synchronized void insertDlInfo(Response<ResponseBody> response) {
        if(query(context) == null){
            DownloadInfo downloadInfo = new DownloadInfo();
            downloadInfo.tname = fileName;
            long fileLength = Long.parseLong(String.valueOf(response.headers().get("Content-Length")));
            Log.e("retrofit_dl", "文件大小: " + fileLength);
            downloadInfo.tlength = fileLength;
            downloadInfo.tid = 0;
            downloadInfo.startPos = 0;
            downloadInfo.turl = url;
            downloadInfo.endPos = fileLength - 1;
            DLDao.insert(context, downloadInfo);

            DownloadInfo downloadInfos = DLDao.query(context);
            if(downloadInfos != null){

            }
        }
    }


}
