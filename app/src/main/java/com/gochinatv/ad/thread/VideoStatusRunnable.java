package com.gochinatv.ad.thread;

import android.content.Context;
import android.os.Handler;
import android.os.PowerManager;

import com.gochinatv.ad.tools.LogCat;
import com.gochinatv.ad.video.MeasureVideoView;
import com.okhtttp.response.AdDetailResponse;

/**
 * Created by fq_mbp on 16/6/20.
 */
public class VideoStatusRunnable implements Runnable {

    private MeasureVideoView videoView;
    private AdDetailResponse playingVideoInfo;
    private Handler handler;
    private Context context;
    private long oldVideoPosition;
    private int videoId;
    private long errorPlayTime;
    private int errorPlayTimes;
    private final int TIME_CHECK_VIDEO_DURATION = 1000 * 2 * 60;

    public VideoStatusRunnable(Context context, MeasureVideoView videoView, AdDetailResponse playingVideoInfo, Handler handler ){
        this.videoView = videoView;
        this.context = context;
        this.playingVideoInfo = playingVideoInfo;
        this.handler = handler;
    }


    @Override
    public void run() {
        LogCat.e("alarm", "开始检测视频......");
        if (oldVideoPosition == 0) {
            LogCat.e("alarm", "初始化......");
            try {
                if (videoView != null) {
                    oldVideoPosition = videoView.getCurrentPosition();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (playingVideoInfo == null) {
                handler.postDelayed(this, TIME_CHECK_VIDEO_DURATION);
                return;
            }
            videoId = playingVideoInfo.adVideoId;
            handler.postDelayed(this, TIME_CHECK_VIDEO_DURATION);
            return;
        }

        if (playingVideoInfo == null) {
            LogCat.e("alarm", "playingVideoInfo == null......");
            handler.postDelayed(this, TIME_CHECK_VIDEO_DURATION);
            return;
        }

        // 重置错误时间
        if(errorPlayTime >= 1800000 && errorPlayTimes < 2){
            errorPlayTime = 0;
            errorPlayTimes = 0;
        }

        try {
            int currentVideoId = playingVideoInfo.adVideoId;

            if (currentVideoId == videoId && oldVideoPosition == videoView.getCurrentPosition()) {
                LogCat.e("alarm", "播放同一个视频， 同一个位置......");
                LogCat.e("alarm", "视频卡死了......");
                // 需要重新开始视频
                rePlayView();
            } else {
                LogCat.e("alarm", "继续监控视频播放......");
                oldVideoPosition = videoView.getCurrentPosition();
                videoId = playingVideoInfo.adVideoId;
                handler.postDelayed(this, TIME_CHECK_VIDEO_DURATION);
            }

        } catch (Exception e) {
            e.printStackTrace();
            rePlayView();
        }
    }

    private void rePlayView() {


        if (videoView != null) {
            try {
                videoView.stopPlayback();
                videoView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (playingVideoInfo != null) {
                            if(videoView != null){
                                videoView.setVideoPath(playingVideoInfo.videoPath);
                            }
                        }
                    }
                }, 3000);
            } catch (Exception e) {
                e.printStackTrace();
                rePlayError();
            }
        } else {
            rePlayError();
        }
        handler.postDelayed(this, TIME_CHECK_VIDEO_DURATION);
    }

    private void rePlayError(){
        errorPlayTimes++;
//            AlertUtils.alert(getActivity(), "app出错了，第 " + errorPlayTimes +" 次");
        // 超过2次出现错误，并且在30分钟之内，就重启设备
        if(errorPlayTimes >= 2 && errorPlayTime <= 1800000){
            PowerManager pManager=(PowerManager) context.getSystemService(Context.POWER_SERVICE);
            pManager.reboot("");
            return;
        }
        errorPlayTime = System.currentTimeMillis();
    }
}
