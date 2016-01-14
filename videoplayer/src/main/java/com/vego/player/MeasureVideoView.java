package com.vego.player;

/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.MediaController.MediaPlayerControl;
import android.widget.RelativeLayout;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.math.BigDecimal;

/**
 * Displays a video file. The VideoView class can load images from various
 * sources (such as resources or content providers), takes care of computing its
 * measurement from the video so that it can be used in any layout manager, and
 * provides various display options such as scaling and tinting.
 */
public class MeasureVideoView extends SurfaceView implements MediaPlayerControl {
    private String TAG = "VideoView";
    private Context mContext;
    // settable by the client
    private Uri mUri;
    private int mDuration;
    // All the stuff we need for playing and showing a video
    private SurfaceHolder mSurfaceHolder = null;
    public MediaPlayer mMediaPlayer = null;
    private boolean mIsPrepared;
    private int mVideoWidth;
    private int mVideoHeight;
    private int mSurfaceWidth;
    private int mSurfaceHeight;
    private MediaController mMediaController;
    private OnCompletionListener mOnCompletionListener;
    private MediaPlayer.OnPreparedListener mOnPreparedListener;
    private int mCurrentBufferPercentage;
    private OnErrorListener mOnErrorListener;
    private boolean mStartWhenPrepared;
    private int mSeekWhenPrepared;
    private MySizeChangeLinstener mMyChangeLinstener;
    private String path = null;

    public int getVideoWidth() {
        return mVideoWidth;
    }

    public int getVideoHeight() {
        return mVideoHeight;
    }

    public void setVideoScale(int width, int height) {
        LayoutParams lp = getLayoutParams();
        lp.height = height;
        lp.width = width;
        setLayoutParams(lp);
    }

    public interface MySizeChangeLinstener {
        public void doMyThings();
    }

    public void setMySizeChangeLinstener(MySizeChangeLinstener l) {
        mMyChangeLinstener = l;
    }

    public MeasureVideoView(Context context) {
        super(context);
        mContext = context;
        initVideoView();
    }

    public MeasureVideoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        mContext = context;
        initVideoView();
    }

    public MeasureVideoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        initVideoView();
    }


    int width;
    int height;

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        width = getDefaultSize(mVideoWidth, widthMeasureSpec);
        height = getDefaultSize(mVideoHeight, heightMeasureSpec);
        setMeasuredDimension(width, height);
//        LogCat.e("onMeasure...............");
    }

    public int resolveAdjustedSize(int desiredSize, int measureSpec) {
        int result = desiredSize;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        switch (specMode) {
            case MeasureSpec.UNSPECIFIED:
            /*
             * Parent says we can be as big as we want. Just don't be larger
			 * than max size imposed on ourselves.
			 */
                result = desiredSize;
                break;
            case MeasureSpec.AT_MOST:
            /*
             * Parent says we can be as big as we want, up to specSize. Don't be
			 * larger than specSize, and don't be larger than the max size
			 * imposed on ourselves.
			 */
                result = Math.min(desiredSize, specSize);
                break;
            case MeasureSpec.EXACTLY:
                // No choice. Do what we are told.
                result = specSize;
                break;
        }
        return result;
    }

    @SuppressWarnings("deprecation")
    private void initVideoView() {
        mVideoWidth = 0;
        mVideoHeight = 0;
        getHolder().addCallback(mSHCallback);
        getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();
    }

    public void setVideoPath(String path) {
        setVideoURI(Uri.parse(path));
    }

    public void setVideoUrl(String path1) {
        path = path1;
        mStartWhenPrepared = false;
        mSeekWhenPrepared = 0;
        openVideo();
        requestLayout();
        invalidate();
    }

    public void setVideoURI(Uri uri) {
        mUri = uri;
        mStartWhenPrepared = false;
        mSeekWhenPrepared = 0;
        setVideoSize(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        openVideo();
        requestLayout();
        invalidate();
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
        if (mUri == null || mSurfaceHolder == null) {
            if (path == null || mSurfaceHolder == null) {
                // not ready for playback just yet, will try again later
                return;
            }
        }
        // Tell the music playback service to pause
        // framework.
        Intent i = new Intent("com.android.music.musicservicecommand");
        i.putExtra("command", "pause");
        mContext.sendBroadcast(i);
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        try {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setOnPreparedListener(mPreparedListener);
            mMediaPlayer.setOnVideoSizeChangedListener(mSizeChangedListener);
            mIsPrepared = false;
            Log.v(TAG, "reset duration to -1 in openVideo");
            mDuration = -1;
            mMediaPlayer.setOnCompletionListener(mCompletionListener);
            mMediaPlayer.setOnErrorListener(mErrorListener);
            mMediaPlayer.setOnBufferingUpdateListener(mBufferingUpdateListener);
            mMediaPlayer.setOnSeekCompleteListener(mSeekCompleteListener);
            mMediaPlayer.setOnInfoListener(onInfoListener);
            mCurrentBufferPercentage = 0;
            if (mUri == null)
                mMediaPlayer.setDataSource(path);
            else
                mMediaPlayer.setDataSource(mContext, mUri);
//            mMediaPlayer.setDisplay(mSurfaceHolder);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setScreenOnWhilePlaying(true);
            mMediaPlayer.prepareAsync();
            attachMediaController();
        } catch (IOException ex) {
            Log.w(TAG, "Unable to open content: " + mUri, ex);
            return;
        } catch (IllegalArgumentException ex) {
            Log.w(TAG, "Unable to open content: " + mUri, ex);
            return;
        }
    }

    public void setMediaController(MediaController controller) {
        if (mMediaController != null) {
            mMediaController.hide();
        }
        mMediaController = controller;
        attachMediaController();
    }

    private void attachMediaController() {
        if (mMediaPlayer != null && mMediaController != null) {
            mMediaController.setMediaPlayer(this);
            View anchorView = this.getParent() instanceof View ? (View) this.getParent() : this;
            mMediaController.setAnchorView(anchorView);
            mMediaController.setEnabled(mIsPrepared);
        }
    }


//    private int vW, vH;

    MediaPlayer.OnVideoSizeChangedListener mSizeChangedListener = new MediaPlayer.OnVideoSizeChangedListener() {
        public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
            mVideoWidth = mp.getVideoWidth();
            mVideoHeight = mp.getVideoHeight();
            if (mMyChangeLinstener != null) {
                mMyChangeLinstener.doMyThings();
            }


//            if (mVideoWidth != 0 && mVideoHeight != 0) {
//                getHolder().setFixedSize(mVideoWidth, mVideoHeight);
//            }
//            if (mVideoWidth != 0 && mVideoHeight != 0) {
//                getHolder().setFixedSize(mVideoWidth, mVideoHeight);
//            }
            setHolderSize();
        }
    };

    private void setHolderSize(){
        if (mVideoWidth != 0 && mVideoHeight != 0) {
            if(mSurfaceHolder != null){
                mSurfaceHolder.setFixedSize(mVideoWidth, mVideoHeight);
                mMediaPlayer.setDisplay(mSurfaceHolder);
            }
            int vW = 0;
            int vH = 0;

//            LogCat.e("mSurfaceWidth..........." + mSurfaceWidth);
//            LogCat.e("mSurfaceHeight..........." + mSurfaceHeight);
            WindowManager windowManager = ((Activity)getContext()).getWindowManager();
            Display display = windowManager.getDefaultDisplay();
            int screenWidth = display.getWidth();
            int screenHeight = display.getHeight();

//            if(screenWidth > mSurfaceWidth && screenHeight > mSurfaceHeight){
//                screenWidth = mSurfaceWidth;
//                screenHeight = mSurfaceHeight;
//            }




            float screenWH = screenWidth / (float) screenHeight;
            BigDecimal decimalScreen = new BigDecimal(screenWH);
            screenWH = decimalScreen.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();

            // 视频的宽高比
            float videoWH = mVideoWidth / (float) mVideoHeight;
            BigDecimal decimalVideo = new BigDecimal(videoWH);
            videoWH = decimalVideo.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
//            LogCat.e("screenWH..........." + screenWH);
//            LogCat.e("videoWH..........." + videoWH);
//            LogCat.e("mSurfaceWidth..........." + screenWidth);
//            LogCat.e("mSurfaceHeight..........." + screenHeight);
//            LogCat.e("mVideoWidth..........." + mVideoWidth);
//            LogCat.e("mVideoHeight..........." + mVideoHeight);
            if (screenWH < videoWH) { // 此时表示，视频的宽高比比原视频的宽高比大，即视频的宽比高更突出，此时应该以视频的宽为标准来计算视频的分辨率
                // 此时的宽就是屏幕的宽
                vW = screenWidth;
                // 需要按照此时的宽来计算新的高
                vH = (int) (vW * mVideoHeight / (float)mVideoWidth);
            } else if (screenWH == videoWH) { // 此时说明需要全屏显示即可
                vW = screenWidth;
                vH = screenHeight;
            } else { // 此时表示，视频的高度突出，需要按照高度来控制视频显示
                // 此时的高就是屏幕的高
                vH = screenHeight;
                // 按照此时的高度来计算宽度
                vW = (int) (vH * mVideoWidth / (float)mVideoHeight);
            }


            setVideoSize(vW, vH);

        }else {
            if(mSurfaceHolder != null){
                mSurfaceHolder.setFixedSize(mSurfaceWidth, mSurfaceHeight);
                mMediaPlayer.setDisplay(mSurfaceHolder);
            }
//
            setVideoSize(mSurfaceWidth, mSurfaceHeight);
        }



    }



    MediaPlayer.OnPreparedListener mPreparedListener = new MediaPlayer.OnPreparedListener() {
        public void onPrepared(MediaPlayer mp) {
            mIsPrepared = true;
            if (mOnPreparedListener != null) {
                mOnPreparedListener.onPrepared(mMediaPlayer);
            }
            if (mMediaController != null) {
                mMediaController.setEnabled(true);
            }

            if (mVideoWidth == 0 && mVideoHeight == 0) {
                try {
                    mVideoWidth = mp.getVideoWidth();
                    mVideoHeight = mp.getVideoHeight();
                }catch (Exception e){
                }
            }
            setHolderSize();


            // 修改视频分辨率
            if (mSeekWhenPrepared != 0) {
                mMediaPlayer.seekTo(mSeekWhenPrepared);
                mSeekWhenPrepared = 0;
            }
            if (mStartWhenPrepared) {
                mMediaPlayer.start();
                mStartWhenPrepared = false;
            }
        }
    };

    MediaPlayer.OnInfoListener onInfoListener = new MediaPlayer.OnInfoListener() {
        @Override
        public boolean onInfo(MediaPlayer mp, int what, int extra) {
            // 当一些特定信息出现或者警告时触发
            switch (what) {
                case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                    if (onVideoBufferStateListener != null) {
                        onVideoBufferStateListener.OnVideoBufferState(true);
                    }
                    break;

                case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                    if (onVideoBufferStateListener != null) {
                        onVideoBufferStateListener.OnVideoBufferState(false);
                    }
                    break;

                case MediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING:
                    break;

                case MediaPlayer.MEDIA_INFO_NOT_SEEKABLE:
                    break;

            }
            return false;
        }
    };



    private VideoHandler videoHandler;
    private static class VideoHandler extends Handler{

        private WeakReference<Context> weakReference;

        public VideoHandler(Context context){
            weakReference = new WeakReference<Context>(context);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);


        }
    }







    private OnVideoBufferStateListener onVideoBufferStateListener;

    public interface OnVideoBufferStateListener {
        void OnVideoBufferState(boolean isBuffering);
    }

    public void setOnVideoBufferStateListener(OnVideoBufferStateListener onVideoBufferStateListener) {
        this.onVideoBufferStateListener = onVideoBufferStateListener;
    }

    private MediaPlayer.OnCompletionListener mCompletionListener = new MediaPlayer.OnCompletionListener() {
        public void onCompletion(MediaPlayer mp) {
            if (mMediaController != null) {
                mMediaController.hide();
            }
            if (mOnCompletionListener != null) {
                mOnCompletionListener.onCompletion(mMediaPlayer);
            }
        }
    };
    private MediaPlayer.OnErrorListener mErrorListener = new MediaPlayer.OnErrorListener() {
        public boolean onError(MediaPlayer mp, int framework_err, int impl_err) {
            Log.d(TAG, "Error: " + framework_err + "," + impl_err);
            if (mMediaController != null) {
                mMediaController.hide();
            }
            /* If an error handler has been supplied, use it and finish. */
            if (mOnErrorListener != null) {
                if (mOnErrorListener.onError(mMediaPlayer, framework_err, impl_err)) {
                    return true;
                }
            }
            /*
             * Otherwise, pop up an error dialog so the user knows that
			 * something bad has happened. Only try and pop up the dialog if
			 * we're attached to a window. When we're going away and no longer
			 * have a window, don't bother showing the user an error.
			 */
            if (getWindowToken() != null) {
                // Resources r = mContext.getResources();
                // int messageId;
                /*
                 * if (framework_err ==
				 * MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK) {
				 * messageId = com.android.internal.R.string.
				 * VideoView_error_text_invalid_progressive_playback; } else {
				 * messageId =
				 * com.android.internal.R.string.VideoView_error_text_unknown; }
				 * new AlertDialog.Builder(mContext)
				 * .setTitle(com.android.internal
				 * .R.string.VideoView_error_title) .setMessage(messageId)
				 * .setPositiveButton
				 * (com.android.internal.R.string.VideoView_error_button, new
				 * DialogInterface.OnClickListener() { public void
				 * onClick(DialogInterface dialog, int whichButton) { If we get
				 * here, there is no onError listener, so at least inform them
				 * that the video is over. if (mOnCompletionListener != null) {
				 * mOnCompletionListener.onCompletion(mMediaPlayer); } } })
				 * .setCancelable(false) .show();
				 */
            }
            return true;
        }
    };

    private MediaPlayer.OnBufferingUpdateListener mBufferingUpdateListener = new MediaPlayer.OnBufferingUpdateListener() {
        public void onBufferingUpdate(MediaPlayer mp, int percent) {
            mCurrentBufferPercentage = percent;
        }
    };

    private MediaPlayer.OnSeekCompleteListener mSeekCompleteListener = new MediaPlayer.OnSeekCompleteListener() {

        @Override
        public void onSeekComplete(MediaPlayer mp) {
            if (mOnSeekCompleteListener != null) {
                mOnSeekCompleteListener.onSeekComplete(mMediaPlayer);
            }
        }

    };


    public void setVideoSize(int width, int height) {
        android.widget.RelativeLayout.LayoutParams sufaceviewParams = (android.widget.RelativeLayout.LayoutParams) MeasureVideoView.this
                .getLayoutParams();

        sufaceviewParams.height = height;
        sufaceviewParams.width = width;
        sufaceviewParams.addRule(RelativeLayout.CENTER_IN_PARENT);

        MeasureVideoView.this.setLayoutParams(sufaceviewParams);
    }

    public void recoverySize() {
        android.widget.RelativeLayout.LayoutParams sufaceviewParams = (android.widget.RelativeLayout.LayoutParams) MeasureVideoView.this
                .getLayoutParams();

        sufaceviewParams.height = LayoutParams.MATCH_PARENT;
        sufaceviewParams.width = LayoutParams.MATCH_PARENT;
        sufaceviewParams.addRule(RelativeLayout.CENTER_IN_PARENT);

        MeasureVideoView.this.setLayoutParams(sufaceviewParams);
        if (mMediaPlayer != null) {
            mMediaPlayer.setDisplay(mSurfaceHolder);
        }
    }


    /**
     * Register a callback to be invoked when the media file is loaded and ready
     * to go.
     *
     * @param l The callback that will be run
     */
    public void setOnPreparedListener(MediaPlayer.OnPreparedListener l) {
        mOnPreparedListener = l;
    }

    public void setmBufferingUpdateListener(MediaPlayer.OnBufferingUpdateListener mBufferingUpdateListener) {
        this.mBufferingUpdateListener = mBufferingUpdateListener;
    }

    /**
     * Register a callback to be invoked when the end of a media file has been
     * reached during playback.
     *
     * @param l The callback that will be run
     */
    public void setOnCompletionListener(OnCompletionListener l) {
        mOnCompletionListener = l;
    }

    OnSeekCompleteListener mOnSeekCompleteListener;

    public void setOnSeekFinishedListener(OnSeekCompleteListener l) {
        mOnSeekCompleteListener = l;
    }

    /**
     * Register a callback to be invoked when an error occurs during playback or
     * setup. If no listener is specified, or if the listener returned false,
     * VideoView will inform the user of any errors.
     *
     * @param l The callback that will be run
     */
    public void setOnErrorListener(OnErrorListener l) {
        mOnErrorListener = l;
    }

    SurfaceHolder.Callback mSHCallback = new SurfaceHolder.Callback() {
        public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
            // surface 已经释放了就return
            if (mSurfaceHolder != null && mSurfaceHolder.getSurface() != null && !mSurfaceHolder.getSurface().isValid()) {
                return;
            }
            mSurfaceWidth = w;
            mSurfaceHeight = h;
            // 这里没有啊
            if (mMediaPlayer != null && mIsPrepared && mVideoWidth == w && mVideoHeight == h) {
                if (mSeekWhenPrepared != 0) {
                    mMediaPlayer.seekTo(mSeekWhenPrepared);
                    mSeekWhenPrepared = 0;
                }
                mMediaPlayer.start();
                if (mMediaController != null) {
                    mMediaController.show();
                }
            }
            if (mMediaPlayer != null) {
                mMediaPlayer.setDisplay(mSurfaceHolder);
            }

        }

        public void surfaceCreated(SurfaceHolder holder) {
            mSurfaceHolder = holder;
            openVideo();
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            // after we return from this we can't use the surface any more
            mSurfaceHolder = null;
            if (mMediaController != null)
                mMediaController.hide();
            if (mMediaPlayer != null) {
                mMediaPlayer.reset();
                mMediaPlayer.release();
                mMediaPlayer = null;
            }
        }
    };

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mIsPrepared && mMediaPlayer != null && mMediaController != null) {
            toggleMediaControlsVisiblity();
        }
        return false;
    }

    @Override
    public boolean onTrackballEvent(MotionEvent ev) {
        if (mIsPrepared && mMediaPlayer != null && mMediaController != null) {
            toggleMediaControlsVisiblity();
        }
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mIsPrepared && keyCode != KeyEvent.KEYCODE_BACK && keyCode != KeyEvent.KEYCODE_VOLUME_UP
                && keyCode != KeyEvent.KEYCODE_VOLUME_DOWN && keyCode != KeyEvent.KEYCODE_MENU
                && keyCode != KeyEvent.KEYCODE_CALL && keyCode != KeyEvent.KEYCODE_ENDCALL && mMediaPlayer != null
                && mMediaController != null) {
            if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
                if (mMediaPlayer.isPlaying()) {
                    pause();
                    mMediaController.show();
                } else {
                    start();
                    mMediaController.hide();
                }
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP && mMediaPlayer.isPlaying()) {
                pause();
                mMediaController.show();
            } else {
                toggleMediaControlsVisiblity();
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private void toggleMediaControlsVisiblity() {
        if (mMediaController.isShowing()) {
            mMediaController.hide();
        } else {
            mMediaController.show();
        }
    }

    public void start() {
        if (mMediaPlayer != null && mIsPrepared) {
            mMediaPlayer.start();
            mStartWhenPrepared = false;
        } else {
            mStartWhenPrepared = true;
        }
    }

    public void pause() {
        if (mMediaPlayer != null && mIsPrepared) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
            }
        }
        mStartWhenPrepared = false;
    }

    public int getDuration() {
        if (mMediaPlayer != null && mIsPrepared) {
            if (mDuration > 0) {
                return mDuration;
            }
            mDuration = mMediaPlayer.getDuration();
            return mDuration;
        }
        mDuration = -1;
        return mDuration;
    }

    public int getCurrentPosition() {
        if (mMediaPlayer != null && mIsPrepared) {
            return mMediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    public void seekTo(int msec) {
        if (mMediaPlayer != null && mIsPrepared) {
            mMediaPlayer.seekTo(msec);
        } else {
            mSeekWhenPrepared = msec;
        }
    }

    public boolean isPlaying() {
        if (mMediaPlayer != null && mIsPrepared) {
            try {
                return mMediaPlayer.isPlaying();
            } catch (Exception e) {
            }
        }
        return false;
    }

    public int getBufferPercentage() {
        if (mMediaPlayer != null) {
            return mCurrentBufferPercentage;
        }
        return 0;
    }

    public boolean canSeekBackward() {
        return true;
    }

    public boolean canSeekForward() {
        return true;
    }

    public boolean canPause() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }

}
