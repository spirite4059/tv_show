package com.gochinatv.ad.screenshot;

import android.app.Activity;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.view.View;

import java.io.File;

/**
 * Created by fq_mbp on 16/6/2.
 */
public class FullScreenPolicy implements VideoGrab {

    private Activity context;

    public FullScreenPolicy(Activity context){
        this.context = context;
    }


    @Override
    public Bitmap getVideoGrab(File fileVideo, long duration, int width, int height) {
        //生成相同大小的图片
        //找到当前页面的跟布局
        View view = context.getWindow().getDecorView().getRootView();
        //设置缓存
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        //从缓存中获取当前屏幕的图片
        Bitmap temBitmap = view.getDrawingCache();
        temBitmap = ThumbnailUtils.extractThumbnail(temBitmap, width, height);
        return temBitmap;
    }

    @Override
    public String getFileName() {
        String fileName = "screenShot_" + System.currentTimeMillis() + ".png";
        return fileName;
    }

    @Override
    public boolean isNeedCacheImageLocal() {
        return true;
    }


}
