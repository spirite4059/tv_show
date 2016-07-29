package com.gochinatv.ad.tools;

import com.okhtttp.response.AdDetailResponse;

import java.text.NumberFormat;
import java.util.ArrayList;

import static com.gochinatv.ad.tools.VideoAdUtils.logProgress;

/**
 * Created by fq_mbp on 16/7/29.
 */

public class LogMsgUtils {

    private StringBuilder downLoadingVideoList;
    private StringBuilder logBuilder;

    private LogMsgUtils() {
    }

    private static class Singleson {
        private static LogMsgUtils instance = new LogMsgUtils();
    }

    public static LogMsgUtils getInstance() {
        return Singleson.instance;
    }


    public String getVideoList(ArrayList<AdDetailResponse> playVideoLists, ArrayList<AdDetailResponse> cachePlayVideoLists) {
        initBuilder();
        downLoadingVideoList.append("Request video list ");
        downLoadingVideoList.append('\n');
        //下载完成的个数
        int size = 0;
        if (playVideoLists != null) {
            size = playVideoLists.size();
            LogCat.e("net", "cachePlayVideoLists.size：" + playVideoLists.size());
        } else {
            if (cachePlayVideoLists != null) {
                size = cachePlayVideoLists.size();
            }
        }
        downLoadingVideoList.append("completed video：" + size);
        return downLoadingVideoList.toString();
    }


    public String showTestMsg(AdDetailResponse playingVideoInfo, AdDetailResponse downloadingVideoResponse, long progress, long fileLength, ArrayList<AdDetailResponse> playVideoLists) {
        // 当前下载视频
        // 当前已下载视频的个数
        if (logBuilder == null) {
            logBuilder = new StringBuilder();
        }

        logBuilder.delete(0, logBuilder.length());

        if (playingVideoInfo != null) {
            logBuilder.append("正在播放：" + playingVideoInfo.adVideoName);
        }

        logBuilder.append('\n');
        logBuilder.append("当前正在下载：");
        if (downloadingVideoResponse != null) {
            logBuilder.append(downloadingVideoResponse.adVideoName);
        }
        logBuilder.append('\n');
        logBuilder.append("当前下载进度：");
        logBuilder.append(logProgress(progress, fileLength));
        logBuilder.append('\n');

        logBuilder.append("已下载视频列表：" + '\n');
        if (playVideoLists != null) {
            int size = playVideoLists.size();
            for (int i = 0; i < size; i++) {
                AdDetailResponse adDetailResponse = playVideoLists.get(i);
                logBuilder.append("        ");
                logBuilder.append(adDetailResponse.adVideoName);
                if (i < size - 1) {
                    logBuilder.append('\n');
                }
            }
        }
        return logBuilder.toString();
    }

    // 显示下载信息
    public String showDownLoadMsg(AdDetailResponse downloadingVideoResponse, long progress, long fileLength, ArrayList<AdDetailResponse> playVideoLists) {
        initBuilder();
        //当前正在下载
        if (downloadingVideoResponse != null) {
            downLoadingVideoList.append("downloading：vid_");
            downLoadingVideoList.append(downloadingVideoResponse.adVideoId + ", size: ");

        }
        //当前下载进度
        downLoadingVideoList.append(logProgress(progress, fileLength));
        if (downloadingVideoResponse != null) {
            downLoadingVideoList.append('\n');
        }
        //下载完成的个数
        if (playVideoLists != null) {
            int size = playVideoLists.size();
            downLoadingVideoList.append("completed video：" + size);
        }

        return downLoadingVideoList.toString();
    }


    /**
     * 所有视频下载完成，此时的下载信息
     */
    public String showDownloadMsgALLDownloadCompleted(ArrayList<AdDetailResponse> playVideoLists) {
        initBuilder();
        downLoadingVideoList.append("Completed");
        downLoadingVideoList.append('\n');
        //下载完成的个数
        int size = 0;
        if (playVideoLists != null) {
            size = playVideoLists.size();
        }
        downLoadingVideoList.append("completed video：" + size);
        return downLoadingVideoList.toString();

    }


    private long oldProgress;
    private NumberFormat numberFormat;
    public String getNetSpeed(boolean isHasNet, long progress, boolean isDownloadingAPK){
        if (!isHasNet) {
            return "wifi-off:0kb/s";
        }
        if (oldProgress <= 0) {
            oldProgress = progress;
            return "wifi-off:0kb/s";
        }
        String msg = null;
        long current = progress - oldProgress;

        String speed;
        if (current >= 0 && current < 1024) {
            speed = current + "B/s";
        } else if (current >= 1024 && current < 1048576) {
            speed = current / 1024 + "KB/s";
        } else {
            if(numberFormat == null){
                numberFormat = NumberFormat.getNumberInstance();
            }
            numberFormat.setMaximumFractionDigits(2);
            speed = numberFormat.format(current / 1048576f) + "MB/s";
        }
        LogCat.e("net_speed", "speed: " + speed);
        if (current >= 0) {
            if (isDownloadingAPK) {
                msg = "wifi-on:" + speed + "-upgrading";
            } else {
                msg = "wifi-on:" + speed + "-downloading";
            }
        }else {
            msg = "wifi-off:0kb/s";
        }
        oldProgress = progress;
        return msg;
    }


    public String getNoNetMsg(boolean isStartUpNotNet, ArrayList<AdDetailResponse> playVideoLists, ArrayList<AdDetailResponse> cachePlayVideoLists){
        initBuilder();
        //下载完成的个数
        int size = 0;
        if (isStartUpNotNet) {
            //启动时无网络
            if (cachePlayVideoLists != null) {
                size = cachePlayVideoLists.size();
                LogCat.e("net", "cachePlayVideoLists.size：" + cachePlayVideoLists.size());
            }
        } else {
            //app运行中无网络
            if (playVideoLists != null) {
                size = playVideoLists.size();
                LogCat.e("net", "playVideoLists.size：" + playVideoLists.size());
            }
        }
        downLoadingVideoList.append("completed video：" + size);
        return downLoadingVideoList.toString();
    }


    private void initBuilder() {
        if (downLoadingVideoList == null) {
            downLoadingVideoList = new StringBuilder();
        }
        downLoadingVideoList.delete(0, downLoadingVideoList.length());
    }

}
