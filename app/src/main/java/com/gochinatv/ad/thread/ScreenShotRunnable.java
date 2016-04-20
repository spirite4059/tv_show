package com.gochinatv.ad.thread;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.media.MediaMetadataRetriever;
import android.view.View;

import com.gochinatv.ad.tools.Constants;
import com.gochinatv.ad.tools.DataUtils;
import com.gochinatv.ad.tools.LogCat;
import com.okhtttp.OkHttpUtils;
import com.okhtttp.response.ScreenShotResponse;
import com.tools.HttpUrls;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by fq_mbp on 16/3/23.
 */
public class ScreenShotRunnable implements Runnable{

    private Activity activity;

    private long currentTime;
    private float bgScale;
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
        Bitmap videoBitmap = getVideoScreenShot();
        if(videoBitmap == null){
            return;
        }
//        Bitmap resultBitmap = mergeBitmap(bitmap, videoBitmap);

        uploadBitmap(videoBitmap);

//        if(bitmap != null){
//            bitmap.recycle();
//            bitmap = null;
//        }

        if(videoBitmap != null){
            videoBitmap.recycle();
            videoBitmap = null;
        }

    }

    private void uploadBitmap(Bitmap resultBitmap) {
        String rootPath = DataUtils.getScreenShotDirectory();
        File fileRoot = new File(rootPath);
        if(!fileRoot.exists()){
            fileRoot.mkdirs();
        }
        LogCat.e("uploadBitmap..............");
        File file = new File(rootPath,  System.currentTimeMillis() + Constants.FILE_SCREEN_SHOT_NAME);
        if(!file.exists()){
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else {
            file.delete();

            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            LogCat.e("uploadBitmap..............1111");
            boolean isScreenShot = resultBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);


            if (isScreenShot) {
                //截图成功
                LogCat.e("截屏成功......");

                try {
                    OkHttpUtils.getInstance().doUploadFile(activity, file, HttpUrls.URL_SCREEN_SHOT);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }else {
                LogCat.e("截屏失败......");
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }finally {

            try {
                if(fos != null){
                    fos.flush();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                if(fos != null){
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            if(resultBitmap != null){
                resultBitmap.recycle();
                resultBitmap = null;
            }


        }
    }


    private Bitmap getFullScreenShot(){
        // 获取整个屏幕除了视频之外的整个截图
        View view = activity.getWindow().getDecorView();
        if(view == null){
            return null;
        }
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap b1 = view.getDrawingCache();
        if(b1 == null){
            return null;
        }

        // 获取状况栏高度
        int width = activity.getWindowManager().getDefaultDisplay().getWidth();
        int height = activity.getWindowManager().getDefaultDisplay()
                .getHeight();
        Matrix matrix = new Matrix();
        matrix.postScale(bgScale, bgScale); //长和宽放大缩小的比例
        Bitmap bitmap = Bitmap.createBitmap(b1, 0, 0, width, height, matrix, true);
        view.destroyDrawingCache();
        return bitmap;
    }

    private Bitmap getVideoScreenShot(){
        int width = 0;
        int height = 0;
        if(currentTime < 0){
            return null;
        }

        if(screenShotResponse == null){
            // 获取状况栏高度
            width = activity.getWindowManager().getDefaultDisplay().getWidth();
            height = activity.getWindowManager().getDefaultDisplay().getHeight();
        }else {
            width = screenShotResponse.screenShotImgW;
            height = screenShotResponse.screenShotImgH;
        }
        LogCat.e("开始截取指定帧的图片.....");

        // 获取视频的截图
        Bitmap videoBitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {// MODE_CAPTURE_FRAME_ONLY
            retriever.setDataSource(fileName);
//            String timeString = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
//            long time = Long.parseLong(timeString);
//            LogCat.e("获取到的视频长度：" + time);
//            LogCat.e("获取到的视频长度：" + retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM));
//            LogCat.e("获取到的视频名称：" + fileName);
//            LogCat.e("currentTime：" + (currentTime));
            // 时间参数是微妙
            videoBitmap = retriever.getFrameAtTime(currentTime * 1000,MediaMetadataRetriever.OPTION_CLOSEST_SYNC); //按视频长度比例选择帧
            Matrix videoMatrix = new Matrix();
            if(screenShotResponse == null){
                videoMatrix.postScale(videoScale, videoScale); //长和宽放大缩小的比例
            }else {
                videoMatrix.postScale(1.0f, 1.0f); //长和宽放大缩小的比例
            }
            videoBitmap = Bitmap.createBitmap(videoBitmap, 0, 0, width, height, videoMatrix , true);
            LogCat.e("开始截取指定帧的图片.....11111111111");
        } catch (IllegalArgumentException ex) {
            // Assume this is a corrupt video file
            ex.printStackTrace();
        } finally {
            try {
                retriever.release();
            } catch (RuntimeException ex) {
                // Ignore failures while cleaning up.
            }
        }
        return videoBitmap;
    }


    private Bitmap mergeBitmap(Bitmap bitmap, Bitmap videoBitmap){
        Bitmap resultBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(resultBitmap);
        // 将整个屏幕除了视频的截图绘制到面板
        Rect srcRect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());// 截取bmp1中的矩形区域
        Rect dstRect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());// bmp1在目标画布中的位置
        canvas.drawBitmap(bitmap, srcRect, dstRect, null);

        // 将视频截图绘制到面板
        srcRect = new Rect(0, 0, videoBitmap.getWidth(), videoBitmap.getHeight());// 截取bmp1中的矩形区域
        dstRect = new Rect(0, 0, videoBitmap.getWidth(), videoBitmap.getHeight());// bmp1在目标画布中的位置
        canvas.drawBitmap(videoBitmap, srcRect, dstRect, null);
        return resultBitmap;
    }

}
