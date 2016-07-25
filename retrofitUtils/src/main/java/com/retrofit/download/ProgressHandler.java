package com.retrofit.download;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.retrofit.download.db.DLDao;

/**
 * Created by fq_mbp on 16/7/20.
 */

public class ProgressHandler extends Handler{

    private DownloadStatusListener listener;
    private SQLiteDatabase sqLiteDatabase;
    private Context context;

    public ProgressHandler(Looper looper, Context context, DownloadStatusListener listener) {
        super(looper);
        this.listener = listener;
        this.context = context;
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        if(msg == null || msg.getData() == null || listener == null){
            return;
        }

        switch (msg.what){
            case 0: // 开始下载
                Bundle bundle = msg.getData();
                long progress = bundle.getLong("contentLength");
                long total = bundle.getLong("fileSize");
//                boolean isFinish = bundle.putBoolean("isFinish", bytesRead == -1);
                Log.e("retrofit_dl", String.format("%d%% done\n", (100 * progress) / total));

                // 更新数据库
                if(context != null){
                    if(sqLiteDatabase == null){
                        sqLiteDatabase = DLDao.getConnection(context);
                    }
                    DLDao.updateOut(sqLiteDatabase, 0, progress);
                }

                break;
            case 1: // 下载完成
                Log.e("retrofit_dl", "下载完成......");
                DLDao.closeDB(sqLiteDatabase);

                break;
            case 2: // 下载异常

                break;
            case 3: // 磁盘空间不足
                // 停止下载
                RetrofitDLUtils.getInstance().cancel();


                break;
        }

    }

}
