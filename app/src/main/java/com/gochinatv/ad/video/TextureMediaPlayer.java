package com.gochinatv.ad.video;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;

import java.io.IOException;

/**
 * Created by fq_mbp on 16/5/4.
 */
public class TextureMediaPlayer extends TextureView {

    private Surface surface;
    private MediaPlayer mMediaPlayer;
    private String path;
    private Uri uri;


    public TextureMediaPlayer(Context context) {
        this(context, null);
    }

    public TextureMediaPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);


    }

    public void init(){
        setSurfaceTextureListener(new SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
                surface = new Surface(surfaceTexture);
                initMediaPlayer();
                if(onStartVideoListener != null){
                    onStartVideoListener.onStartVideo();
                }

            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                surface = null;
                stopPlayback();
                return true;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {

            }

        });
    }

    public interface OnStartVideoListener{
        void onStartVideo();
    }

    private OnStartVideoListener onStartVideoListener;

    public void setOnStartVideoListener(OnStartVideoListener onStartVideoListener) {
        this.onStartVideoListener = onStartVideoListener;
    }

    private void initMediaPlayer(){
        if (mMediaPlayer != null) {
            if(mMediaPlayer.isPlaying()){
                mMediaPlayer.stop();
            }
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        mMediaPlayer = new MediaPlayer();

        if(onPreparedListener != null){
            mMediaPlayer.setOnPreparedListener(onPreparedListener);
        }

        if(onCompletionListener != null){
            mMediaPlayer.setOnCompletionListener(onCompletionListener);
        }

        if(onInfoListener != null){
            mMediaPlayer.setOnInfoListener(onInfoListener);
        }

        if(onErrorListener != null){
            mMediaPlayer.setOnErrorListener(onErrorListener);
        }


        if(onSeekCompleteListener != null){
            mMediaPlayer.setOnSeekCompleteListener(onSeekCompleteListener);
        }


        try {
            mMediaPlayer.setSurface(surface);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setVideoPath(String path) {
        setVideoURI(Uri.parse(path));
    }



    public void setVideoUrl(String path) {
        this.path = path;
        openVideo();

    }

    public void setVideoURI(Uri uri) {
        this.uri = uri;
        openVideo();
    }


    public void start() {
        if (mMediaPlayer != null) {
            mMediaPlayer.start();
        }
    }

    public void pause() {
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
            }
        }
    }


    public int getDuration() {
        if (mMediaPlayer != null) {
            return mMediaPlayer.getDuration();
        }
        return -1;
    }

    public int getCurrentPosition() {
        if (mMediaPlayer != null) {
            return mMediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    public void seekTo(int msec) {
        if (mMediaPlayer != null) {
            mMediaPlayer.seekTo(msec);
        }
    }

    public boolean isPlaying() {
        if (mMediaPlayer != null) {
            try {
                return mMediaPlayer.isPlaying();
            } catch (Exception e) {
            }
        }
        return false;
    }


    public void stopPlayback() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }



    private void openVideo() {
        // System.out.println("***********************" + mUri + "   " + path);
        if (uri == null || surface == null) {
            if (path == null || surface == null) {
                // not ready for playback just yet, will try again later
                return;
            }
        }

        try {
            initMediaPlayer();

            if (uri == null) {
                mMediaPlayer.setDataSource(path);
            }else {
                mMediaPlayer.setDataSource(getContext(), uri);
            }

            mMediaPlayer.prepareAsync();

        } catch (IOException ex) {
            ex.printStackTrace();
            Log.e("video", "Unable to open content: " + uri, ex);
            return;
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
            Log.w("video", "Unable to open content: " + uri, ex);
            return;
        }
    }



    private MediaPlayer.OnPreparedListener onPreparedListener;

    public void setOnPreparedListener(MediaPlayer.OnPreparedListener listener){
        this.onPreparedListener = listener;
    }

    private MediaPlayer.OnInfoListener onInfoListener;
    public void setOnInfoListener(MediaPlayer.OnInfoListener listener){
        this.onInfoListener = listener;
    }

    private MediaPlayer.OnSeekCompleteListener onSeekCompleteListener;
    public void setOnSeekCompleteListener(MediaPlayer.OnSeekCompleteListener listener){
        this.onSeekCompleteListener = listener;
    }

    private MediaPlayer.OnCompletionListener onCompletionListener;
    public void setOnCompletionListener(MediaPlayer.OnCompletionListener listener){
        this.onCompletionListener = listener;
    }

    private MediaPlayer.OnErrorListener onErrorListener;
    public void setOnErrorListener(MediaPlayer.OnErrorListener listener){
        onErrorListener = listener;
    }


}
