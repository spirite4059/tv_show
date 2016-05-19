package com.gochinatv.ad.screenshot;

import android.graphics.Bitmap;
import android.view.TextureView;

import java.io.File;

/**
 * Created by fq_mbp on 16/5/4.
 */
public class TexturePolicy implements VideoGrab {

    private TextureView textureView;

    public TexturePolicy(TextureView textureView){
        this.textureView = textureView;
    }

    @Override
    public Bitmap getVideoGrab(File fileVideo, long duration, int width, int height) {
        return textureView.getBitmap(width, height);
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
