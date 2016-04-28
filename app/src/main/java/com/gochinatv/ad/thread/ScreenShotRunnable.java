package com.gochinatv.ad.thread;

import android.app.Activity;
import android.graphics.Bitmap;

import com.gochinatv.ad.tools.LogCat;
import com.gochinatv.ad.tools.ScreenShotUtils;
import com.okhtttp.response.ScreenShotResponse;

/**
 * Created by fq_mbp on 16/3/23.
 */
public class ScreenShotRunnable implements Runnable{

    private Activity activity;

    private long currentTime;
    private float videoScale;
    private String fileName;
    private ScreenShotResponse screenShotResponse;

    public ScreenShotRunnable(Activity activity, long currentTime, String fileName, ScreenShotResponse screenShotResponse){
        this.activity = activity;
        this.fileName = fileName;
        this.currentTime = currentTime;
        this.screenShotResponse = screenShotResponse;
        videoScale = 0.4f;
    }



    @Override
    public void run() {

//        Bitmap bitmap = getFullScreenShot();


        LogCat.e("开始进行截图...........");
        Bitmap videoBitmap = ScreenShotUtils.getVideoScreenShot(activity, currentTime, fileName, screenShotResponse);
        if(videoBitmap == null){
            return;
        }
//        Bitmap resultBitmap = mergeBitmap(bitmap, videoBitmap);

//        ScreenShotUtils.uploadBitmap(activity, videoBitmap);

//        if(bitmap != null){
//            bitmap.recycle();
//            bitmap = null;
//        }



    }






}
