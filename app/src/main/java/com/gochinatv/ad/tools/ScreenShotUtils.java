package com.gochinatv.ad.tools;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

import com.okhtttp.OkHttpUtils;
import com.okhtttp.response.ScreenShotResponse;
import com.tools.HttpUrls;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

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


    public static Bitmap getVideoScreenShot(Activity activity, long currentTime, String filePath, ScreenShotResponse screenShotResponse) {
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

//        LogCat.e("screenShot", "视频截图的大小  " + width + " x " + height + "，截图时间："  + format(currentTime));
        LogCat.e("screenShot", "视频截图的大小  " + width + " x " + height);
        // 获取视频的截图
        Bitmap videoBitmap = null;
        LogCat.e("screenShot", "视频截图的名称  " + filePath);
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {// MODE_CAPTURE_FRAME_ONLY
            if (Build.VERSION.SDK_INT >= 14) {//Android4.0以上的设备,必须使用这种方式来设置源播放视频的路径
                retriever.setDataSource(filePath, new HashMap<String, String>());
            } else {
                retriever.setDataSource(filePath);
            }

            videoBitmap = retriever.getFrameAtTime((currentTime * 1000), MediaMetadataRetriever.OPTION_CLOSEST); // frame at 2 seconds
            byte [] artwork = retriever.getEmbeddedPicture();

//            String timeString = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
//            long duration = Long.parseLong(timeString);
//            LogCat.e("screenShot", "获取到的视频长度duration：" + duration);
                LogCat.e("screenShot", "获取到的视频长度currentTime：" + currentTime);
//            long time = (long) (duration * currentTime);
//            LogCat.e("screenShot", "time：" + time);
            // 时间参数是微妙
//            videoBitmap = retriever.getFrameAtTime((currentTime * 1000), MediaMetadataRetriever.OPTION_CLOSEST_SYNC); //按视频长度比例选择帧
//            videoBitmap = Bitmap.createBitmap(tempBitmap, 0, 0, width, height, videoMatrix, true);
//            videoBitmap = resizeImage(tempBitmap, width, height);
            videoBitmap = ThumbnailUtils.extractThumbnail(videoBitmap, width, height);
            LogCat.e("screenShot", "22视频图片大小 ....." + videoBitmap.getWidth() + " x " + videoBitmap.getHeight());

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





    @SuppressWarnings("deprecation")
    public static Bitmap acquireScreenshot(Context context) {
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

        WindowManager mWinManager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        Display display = mWinManager.getDefaultDisplay();
        display.getMetrics(metrics);
        // 屏幕高
        int height = metrics.heightPixels;
        // 屏幕的宽
        int width = metrics.widthPixels;

        int pixelformat = display.getPixelFormat();
        PixelFormat localPixelFormat1 = new PixelFormat();
        PixelFormat.getPixelFormatInfo(pixelformat, localPixelFormat1);
        // 位深
        int deepth = localPixelFormat1.bytesPerPixel;

        byte[] arrayOfByte = new byte[height * width * deepth];
        try {
            // 读取设备缓存，获取屏幕图像流
            InputStream localInputStream = readAsRoot();
            DataInputStream localDataInputStream = new DataInputStream(
                    localInputStream);
            localDataInputStream.readFully(arrayOfByte);
            localInputStream.close();

            int[] tmpColor = new int[width * height];
            int r, g, b;
            for (int j = 0; j < width * height * deepth; j += deepth) {
                b = arrayOfByte[j] & 0xff;
                g = arrayOfByte[j + 1] & 0xff;
                r = arrayOfByte[j + 2] & 0xff;
                tmpColor[j / deepth] = (r << 16) | (g << 8) | b | (0xff000000);
            }
            // 构建bitmap
            Bitmap scrBitmap = Bitmap.createBitmap(tmpColor, width, height,
                    Bitmap.Config.RGB_565);





            uploadBitmap(context, scrBitmap);
            return scrBitmap;

        } catch (Exception e) {
            LogCat.e("screenShot", "#### 读取屏幕截图失败");
            e.printStackTrace();
        }
        return null;

    }

    /**
     * @Title: readAsRoot
     * @Description: 以root权限读取屏幕截图
     * @throws Exception
     * @throws
     */
    private static final String DEVICE_NAME = "/dev/graphics/fb0";
    public static InputStream readAsRoot() throws Exception {
        File deviceFile = new File(DEVICE_NAME);
        Process localProcess = Runtime.getRuntime().exec("su");
        String str = "cat " + deviceFile.getAbsolutePath() + "\n";
        localProcess.getOutputStream().write(str.getBytes());
        return localProcess.getInputStream();
    }

}
