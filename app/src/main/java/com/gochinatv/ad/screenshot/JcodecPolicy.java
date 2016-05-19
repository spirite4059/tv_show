package com.gochinatv.ad.screenshot;

import android.graphics.Bitmap;

import com.gochinatv.ad.tools.LogCat;

import java.io.File;

/**
 * Created by fq_mbp on 16/5/4.
 */
public class JcodecPolicy implements VideoGrab {
    @Override
    public Bitmap getVideoGrab(File fileVideo, long duration, int width, int height) {
        LogCat.e("screen", "getVdieoScreenShot..................123123123123");
        Bitmap bitmap = null;
        try{
//            FrameGrab frameGrab = new FrameGrab(new FileChannelWrapper(new FileInputStream(fileVideo).getChannel()));
//            LogCat.e("screen", "getVdieoScreenShot..................1");
//            frameGrab.seekToFramePrecise(150);
//            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
//            frameGrab.getFrame(bitmap);
//            bitmap = FrameGrab.getFrame(fileVideo, duration / 1000);
            LogCat.e("screen", "getVdieoScreenShot..................2");
        }catch (Exception e){
            e.printStackTrace();
            LogCat.e("screen", "getVdieoScreenShot.................." +e.getLocalizedMessage());
        }
        return bitmap;
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
