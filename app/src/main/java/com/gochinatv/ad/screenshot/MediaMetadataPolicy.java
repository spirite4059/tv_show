package com.gochinatv.ad.screenshot;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.os.Build;

import com.gochinatv.ad.tools.LogCat;

import java.io.File;
import java.util.HashMap;

/**
 * Created by fq_mbp on 16/5/4.
 */
public class MediaMetadataPolicy implements VideoGrab {


    @Override
    public Bitmap getVideoGrab(File fileVideo, long duration, int width, int height) {
        int imgW = 320;
        int imgH = 180;
        if (duration < 0) {
            return null;
        }

        if (width != 0 && height != 0) {
            imgW = width;
            imgH = height;
        }
        LogCat.e("screenShot", "视频截图的大小  " + imgW + " x " + imgH);
        // 获取视频的截图
        Bitmap videoBitmap = null;
        LogCat.e("screenShot", "视频截图的名称  " + fileVideo.getName());
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {// MODE_CAPTURE_FRAME_ONLY
            if (Build.VERSION.SDK_INT >= 14) {//Android4.0以上的设备,必须使用这种方式来设置源播放视频的路径
                retriever.setDataSource(fileVideo.getAbsolutePath(), new HashMap<String, String>());
            } else {
                retriever.setDataSource(fileVideo.getAbsolutePath());
            }

            videoBitmap = retriever.getFrameAtTime((duration * 1000), MediaMetadataRetriever.OPTION_CLOSEST_SYNC); // frame at 2 seconds
            byte [] artwork = retriever.getEmbeddedPicture();
//            String timeString = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
//            long duration = Long.parseLong(timeString);
//            LogCat.e("screenShot", "获取到的视频长度duration：" + duration);
            LogCat.e("screenShot", "获取到的视频长度currentTime：" + duration);
//            long time = (long) (duration * currentTime);
//            LogCat.e("screenShot", "time：" + time);
            // 时间参数是微妙
            videoBitmap = ThumbnailUtils.extractThumbnail(videoBitmap, width, height);
            LogCat.e("screenShot", "22视频图片大小 ....." + videoBitmap.getWidth() + " x " + videoBitmap.getHeight());

        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        } finally {
            try {
                retriever.release();
            } catch (RuntimeException ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public String getFileName() {
        return null;
    }

    @Override
    public boolean isNeedCacheImageLocal() {
        return false;
    }
}
