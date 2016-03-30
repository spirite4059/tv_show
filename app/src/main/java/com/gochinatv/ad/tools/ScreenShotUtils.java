package com.gochinatv.ad.tools;

import android.app.Activity;

import com.gochinatv.ad.thread.ScreenShotRunnable;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by fq_mbp on 16/3/30.
 */
public class ScreenShotUtils {

    // 截屏时间间隔 默认 1小时  1000 * 60 * 60
    private int delay = 1000 * 60 * 60;

    private static ExecutorService service;


    /**
     *
     * @param activity
     * @param currentTime：当前播放进度
     * @param bgScale：背景图的缩放比例
     * @param videoScale：视频的缩放比例
     * @param videoName：当前播放视频的文件名称
     */
    public static void screenShot(Activity activity, int currentTime, float bgScale, float videoScale, String videoName){
        if(service == null){
            service = Executors.newSingleThreadScheduledExecutor();
        }
        service.shutdown();

        ScreenShotRunnable screenShotRunnable = new ScreenShotRunnable(activity, currentTime, bgScale, videoScale, videoName);

        service.submit(screenShotRunnable);
    }


    public static void shutdown(){
        if(service != null){
            service.shutdown();
        }
    }



}
