package com.gochinatv.ad.thread;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.media.MediaMetadataRetriever;
import android.util.Log;
import android.view.View;

import com.gochinatv.ad.tools.Constants;
import com.gochinatv.ad.tools.DataUtils;
import com.gochinatv.ad.tools.LogCat;
import com.okhtttp.OkHttpUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by fq_mbp on 16/3/23.
 */
public class ScreenShotThread extends Thread{

    private Activity activity;

    private long currentTime;
    private float bgScale;
    private float videoScale;
    private String fileName;

    public ScreenShotThread(Activity activity, long currentTime, float bgScale, float videoScale, String fileName){
        this.activity = activity;
        this.bgScale = bgScale;
        this.videoScale = videoScale;
        this.fileName = fileName;
        this.currentTime = currentTime;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // 获取整个屏幕除了视频之外的整个截图
        View view = activity.getWindow().getDecorView();
        if(view == null){
            return;
        }
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap b1 = view.getDrawingCache();
        if(b1 == null){
            return;
        }

        // 获取状况栏高度
        int width = activity.getWindowManager().getDefaultDisplay().getWidth();
        int height = activity.getWindowManager().getDefaultDisplay()
                .getHeight();
        Matrix matrix = new Matrix();
        matrix.postScale(bgScale,bgScale); //长和宽放大缩小的比例
        Bitmap bitmap = Bitmap.createBitmap(b1, 0, 0, width, height, matrix, true);
        view.destroyDrawingCache();

        // 获取视频的截图
        Bitmap videoBitmap = null;
        android.media.MediaMetadataRetriever retriever = new android.media.MediaMetadataRetriever();
        try {// MODE_CAPTURE_FRAME_ONLY
            retriever.setDataSource(DataUtils.getVideoDirectory() + fileName);
            String timeString = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            long time = Long.parseLong(timeString) * 1000;
            Log.i("TAG", "time = " + time); // 64160000
            videoBitmap = retriever.getFrameAtTime(currentTime); //按视频长度比例选择帧
            Matrix videoMatrix = new Matrix();
            videoMatrix.postScale(videoScale, videoScale); //长和宽放大缩小的比例
            videoBitmap = Bitmap.createBitmap(videoBitmap, 0, 0, width, height, videoMatrix, true);
        } catch (IllegalArgumentException ex) {
            // Assume this is a corrupt video file
        } finally {
            try {
                retriever.release();
            } catch (RuntimeException ex) {
                // Ignore failures while cleaning up.
            }
        }
        if(videoBitmap == null){
            return;
        }
//
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

        String rootPath = DataUtils.getScreenShotDirectory();
        File fileRoot = new File(rootPath);
        if(!fileRoot.exists()){
            fileRoot.mkdirs();
        }
        File file = new File(rootPath, Constants.FILE_SCREEN_SHOT_NAME);
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

            boolean isScreenShot = resultBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);


            if (isScreenShot) {
                //截图成功
                LogCat.e("截屏成功......");

                try {
                    OkHttpUtils.getInstance().doUploadFile(file, "http://apk.gochinatv.com/api/quear");
                } catch (IOException e) {
                    e.printStackTrace();
                }

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


            if(b1 != null){
                b1.recycle();
                b1 = null;
            }
            if(bitmap != null){
                bitmap.recycle();
                bitmap = null;
            }

            if(videoBitmap != null){
                videoBitmap.recycle();
                videoBitmap = null;
            }


            if(resultBitmap != null){
                resultBitmap.recycle();
                resultBitmap = null;
            }


        }

    }
}
