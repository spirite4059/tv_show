package com.gochinatv.ad.tools;

import android.app.Activity;

import com.okhtttp.response.ScreenShotResponse;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by fq_mbp on 16/3/30.
 */
public class ScreenShotUtils {

    private static ScreenShotUtils instance;
    // 截屏时间间隔 默认 1小时  1000 * 60 * 60
    private int delay = 1000 * 60 * 60;


    private ScheduledExecutorService service;



    private ScreenShotUtils(){
        service = Executors.newScheduledThreadPool(2);
    }

    public static ScreenShotUtils getInstance(){
        if(instance == null){
            synchronized (ScreenShotUtils.class){
                if(instance == null){
                    instance = new ScreenShotUtils();
                }
            }
        }
        return instance;
    }
    /**
     *
     * @param activity
     * @param currentTime：当前播放进度
     * @param bgScale：背景图的缩放比例
     * @param videoName：当前播放视频的文件名称
     */
    public void screenShot(Activity activity, int currentTime, float bgScale, String videoName, ScreenShotResponse screenShotResponse){
        if(service != null){
            service.shutdownNow();
            service = Executors.newScheduledThreadPool(2);
        }


        if(screenShotResponse != null){
            delay = screenShotResponse.screenShotInterval;
        }

        service.schedule(new Runnable() {
            @Override
            public void run() {

            }
        }, delay, TimeUnit.SECONDS);



//        ScreenShotRunnable screenShotRunnable = new ScreenShotRunnable(activity, currentTime, bgScale, videoName, screenShotResponse);
//        service.schedule(screenShotRunnable, delay, TimeUnit.SECONDS);
    }


    public void shutdown(){
        if(service != null){
            service.shutdown();
        }
    }



}
