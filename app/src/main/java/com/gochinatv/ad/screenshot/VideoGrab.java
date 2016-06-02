package com.gochinatv.ad.screenshot;

import android.graphics.Bitmap;

import java.io.File;

/**
 * Created by fq_mbp on 16/5/4.
 */
public interface VideoGrab {

    Bitmap getVideoGrab(File fileVideo, long duration, int width, int height);


    String getFileName();

    /**
     * 是否要将图片缓存到本地
     * @return
     */
    boolean isNeedCacheImageLocal();

}
