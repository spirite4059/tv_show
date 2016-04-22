package com.gochinatv.ad.tools;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.view.View;

import com.okhtttp.OkHttpUtils;
import com.okhtttp.response.ScreenShotResponse;
import com.tools.HttpUrls;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by fq_mbp on 16/3/30.
 */
public class ScreenShotUtils {

    public static void uploadBitmap(Context context, Bitmap resultBitmap) {
        String rootPath = DataUtils.getScreenShotDirectory();
        File fileRoot = new File(rootPath);
        if (!fileRoot.exists()) {
            fileRoot.mkdirs();
        }
        File file = new File(rootPath, System.currentTimeMillis() + Constants.FILE_SCREEN_SHOT_NAME);
        if (!file.exists()) {
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
                LogCat.e("screenShot", "截屏成功......");

                try {
                    OkHttpUtils.getInstance().doUploadFile(context, file, HttpUrls.URL_SCREEN_SHOT);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else {
                LogCat.e("screenShot", "截屏失败......");
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {

            try {
                if (fos != null) {
                    fos.flush();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

//            if(file != null){
//                file.delete();
//            }

            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (resultBitmap != null) {
                resultBitmap.recycle();
                resultBitmap = null;
            }


        }
    }


    public static Bitmap getFullScreenShot(Activity activity) {
        // 获取整个屏幕除了视频之外的整个截图
        View view = activity.getWindow().getDecorView();
        if (view == null) {
            return null;
        }
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap b1 = view.getDrawingCache();
        if (b1 == null) {
            return null;
        }

        // 获取状况栏高度
        int width = activity.getWindowManager().getDefaultDisplay().getWidth();
        int height = activity.getWindowManager().getDefaultDisplay()
                .getHeight();
        Matrix matrix = new Matrix();
        matrix.postScale(0.5f, 0.5f); //长和宽放大缩小的比例
        Bitmap bitmap = Bitmap.createBitmap(b1, 0, 0, width, height, matrix, true);
        view.destroyDrawingCache();
        return bitmap;
    }

    public static Bitmap getVideoScreenShot(Activity activity, long currentTime, String fileName, ScreenShotResponse screenShotResponse) {
        int width = 0;
        int height = 0;
        if (currentTime < 0) {
            return null;
        }

        if (screenShotResponse == null) {
            // 获取状况栏高度
            width = activity.getWindowManager().getDefaultDisplay().getWidth();
            height = activity.getWindowManager().getDefaultDisplay().getHeight();
        } else {
            width = screenShotResponse.screenShotImgW;
            height = screenShotResponse.screenShotImgH;
        }

        LogCat.e("screenShot", "11视频截图的大小  " + width + " x " + height + "，截图时间："  + format(currentTime));
        // 获取视频的截图
        Bitmap videoBitmap = null;
        LogCat.e("screenShot", "11视频截图的名称  " +fileName);
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {// MODE_CAPTURE_FRAME_ONLY
            retriever.setDataSource(fileName);
//            String timeString = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
//            long time = Long.parseLong(timeString);
//            LogCat.e("获取到的视频长度：" + time);
//            LogCat.e("获取到的视频长度：" + retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM));
//            LogCat.e("获取到的视频名称：" + fileName);
            LogCat.e("screenShot", "currentTime：" +currentTime);
            long time = currentTime * 1000;
            // 时间参数是微妙
            videoBitmap = retriever.getFrameAtTime(time, MediaMetadataRetriever.OPTION_CLOSEST_SYNC); //按视频长度比例选择帧
//            videoBitmap = Bitmap.createBitmap(tempBitmap, 0, 0, width, height, videoMatrix, true);
//            videoBitmap = resizeImage(tempBitmap, width, height);
            videoBitmap = ThumbnailUtils.extractThumbnail(videoBitmap, width, height);
            LogCat.e("screenShot", "22视频图片大小 " + videoBitmap.getWidth() + " x " + videoBitmap.getHeight());

        } catch (IllegalArgumentException ex) {
            // Assume this is a corrupt video file
            ex.printStackTrace();
        } finally {
            try {
                retriever.release();
            } catch (RuntimeException ex) {
                // Ignore failures while cleaning up.
                ex.printStackTrace();
            }
        }
        return videoBitmap;
    }

    public static Bitmap resizeImage(Bitmap bitmap, int w, int h) {
        Bitmap BitmapOrg = bitmap;
        int width = BitmapOrg.getWidth();
        int height = BitmapOrg.getHeight();
        int newWidth = w;
        int newHeight = h;

        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;

        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        // if you want to rotate the Bitmap
        // matrix.postRotate(45);
        Bitmap resizedBitmap = Bitmap.createBitmap(BitmapOrg, 0, 0, width,
                height, matrix, true);
        return resizedBitmap;
    }


    private Bitmap mergeBitmap(Bitmap bitmap, Bitmap videoBitmap) {
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


    public static String format(long ms) {//将毫秒数换算成x天x时x分x秒x毫秒
        int ss = 1000;
        int mi = ss * 60;
        int hh = mi * 60;
        int dd = hh * 24;

        long day = ms / dd;
        long hour = (ms - day * dd) / hh;
        long minute = (ms - day * dd - hour * hh) / mi;
        long second = (ms - day * dd - hour * hh - minute * mi) / ss;
        long milliSecond = ms - day * dd - hour * hh - minute * mi - second * ss;

        String strDay = day < 10 ? "0" + day : "" + day;
        String strHour = hour < 10 ? "0" + hour : "" + hour;
        String strMinute = minute < 10 ? "0" + minute : "" + minute;
        String strSecond = second < 10 ? "0" + second : "" + second;
        String strMilliSecond = milliSecond < 10 ? "0" + milliSecond : "" + milliSecond;
        strMilliSecond = milliSecond < 100 ? "0" + strMilliSecond : "" + strMilliSecond;
        return strHour + ":" + strMinute + ":" + strSecond;
    }


}
