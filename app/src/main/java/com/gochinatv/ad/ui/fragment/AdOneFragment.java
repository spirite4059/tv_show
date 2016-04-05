package com.gochinatv.ad.ui.fragment;

import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.download.DLUtils;
import com.download.ErrorCodes;
import com.gochinatv.ad.R;
import com.gochinatv.ad.base.VideoHttpBaseFragment;
import com.gochinatv.ad.interfaces.OnUpgradeStatusListener;
import com.gochinatv.ad.thread.CacheVideoListThread;
import com.gochinatv.ad.thread.DeleteFileUtils;
import com.gochinatv.ad.tools.Constants;
import com.gochinatv.ad.tools.DataUtils;
import com.gochinatv.ad.tools.DownloadUtils;
import com.gochinatv.ad.tools.LogCat;
import com.gochinatv.ad.tools.RootUtils;
import com.gochinatv.ad.tools.ScreenShotUtils;
import com.gochinatv.ad.tools.SharedPreference;
import com.gochinatv.ad.video.MeasureVideoView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.httputils.http.response.AdDetailResponse;
import com.httputils.http.response.PlayInfoResponse;
import com.httputils.http.response.UpdateResponse;
import com.httputils.http.response.VideoDetailListResponse;

import java.io.File;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by zfy on 2016/3/16.
 */
public class AdOneFragment extends VideoHttpBaseFragment implements OnUpgradeStatusListener {

    private MeasureVideoView videoView;
    private LinearLayout loading;
    /**
     * 本地数据表
     */
    private ArrayList<AdDetailResponse> localVideoList;

    /**
     * 重复请求的次数
     */
    private Timer httpTimer;
    /**
     * 当前下载视频的info
     */
    private AdDetailResponse downloadingVideoResponse;
    /**
     * 下载重试次数
     */
    private int retryGetVideoListTimes;

    /**
     * 下载info
     */
    private UpdateResponse.UpdateInfoResponse updateInfo;

    /**
     * 当前播放视频的序号
     */
    private int playVideoIndex;
    private ArrayList<AdDetailResponse> playVideoLists = null;
    private ArrayList<AdDetailResponse> cachePlayVideoLists = null;
    private ArrayList<AdDetailResponse> deleteLists = null;
    private ArrayList<AdDetailResponse> downloadLists = null;
    private ArrayList<AdDetailResponse> prepareDownloadLists = null;

    private boolean isDownloadVideo;

    private String videoUrl;

    private Timer screenShotTimer;

    private Handler handler;

    private String playingVideoName;


    @Override
    protected View initLayout(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.fragment_ad_video, container, false);
    }

    @Override
    protected void initView(View rootView) {
        videoView = (MeasureVideoView) rootView.findViewById(R.id.videoView);
        loading = (LinearLayout) rootView.findViewById(R.id.loading);
    }

    @Override
    protected void init() {
        // 显示loading加载状态
        showLoading();
        // 记录开始时间
        recordStartTime();
        // 先判断sdcard状态是否可用，如果不可用，直接播放本地视频
        if (!DataUtils.isExistSDCard()) {
            LogCat.e("sd卡状态不可用......");
            playPresetVideo();
            return;
        }
        // 1.初始化本地缓存表
        LogCat.e("获取本地缓存视频列表.......");
        localVideoList = getLocalVideoList();
        LogCat.e("------------------------------");


        // 2.取明日缓存列表
        LogCat.e("获取缓存的明日播放列表.......");
        ArrayList<AdDetailResponse> cacheTomorrowList = getCacheTomorrowList();
        LogCat.e("------------------------------");


        // 3.根据本地缓存视频列表和缓存明日列表，得出当前的播放列表
        LogCat.e("根据本地缓存视频列表和缓存明日列表，得出当前的播放列表.....");
        cachePlayVideoLists = getPlayVideoList(localVideoList, cacheTomorrowList);


        // 4.播放缓存列表
        LogCat.e("查找可以播放的视频.....");
        startPlayVideo();

        LogCat.e("清空升级apk.....");
        // 5.清空所有升级包，为了节省空间
        DeleteFileUtils.getInstance().deleteFile(DataUtils.getSdCardFileDirectory() + Constants.FILE_DIRECTORY_APK);

        String oldPath = DataUtils.getSdCardOldFileDirectory();
        LogCat.e("清空旧文件目录....." + oldPath);
        DeleteFileUtils.getInstance().deleteDir(new File(DataUtils.getSdCardOldFileDirectory()));

        LogCat.e("请求接口.....");
        // 6.请求视频列表
        httpRequest();

        LogCat.e("开始上传截屏文件timer.....");
        // 7.开启上传截图
        startScreenShot();


        // 先开始播放视频
        // 优先查看是否有缓存的视频可以播放，有就播放，没有则播放本地视频
//        initLocalBufferList();

        handler = new Handler(Looper.getMainLooper());


    }

    /**
     * 得到本地的缓存视频的列表
     *
     * @return
     */
    private ArrayList<AdDetailResponse> getLocalVideoList() {
        ArrayList<AdDetailResponse> localVideos = new ArrayList<>();
        File fileVideo = new File(DataUtils.getVideoDirectory());
        if (fileVideo.exists() && fileVideo.isDirectory()) {
            localVideos.addAll(getLocalList(fileVideo));
        }
        return localVideos;
    }

    /**
     * 得到明日的播放列表
     *
     * @return
     */
    private ArrayList<AdDetailResponse> getCacheTomorrowList() {
        ArrayList<AdDetailResponse> cacheTomorrowList = new ArrayList<>();
        File cacheFile = new File(DataUtils.getCacheDirectory() + Constants.FILE_CACHE_NAME);
        if (cacheFile.exists() && cacheFile.isFile()) {
            String json = DataUtils.readFileFromSdCard(cacheFile);
            if (!TextUtils.isEmpty(json)) {
                Gson gson = new Gson();
                LogCat.e("缓存的明日列表已经找到........");
                cacheTomorrowList = gson.fromJson(json, new TypeToken<ArrayList<AdDetailResponse>>() {
                }.getType());
                // TODO 以后删除
                LogCat.e("明日播放列表内容........");
                for (AdDetailResponse adDetailResponse : cacheTomorrowList) {
                    LogCat.e("视频名称：" + adDetailResponse.adVideoName);
                    adDetailResponse.adVideoName = adDetailResponse.name;
                }

            }
        }
        return cacheTomorrowList;
    }

    /**
     * 根据本地缓存视频列表和明日播放列，得出当前的视频播放列表
     *
     * @param localVideoList
     * @param cacheTomorrowList
     * @return
     */
    private ArrayList<AdDetailResponse> getPlayVideoList(ArrayList<AdDetailResponse> localVideoList, ArrayList<AdDetailResponse> cacheTomorrowList) {
        ArrayList<AdDetailResponse> cachePlayVideos = new ArrayList<>();
        int localLength = localVideoList.size();
        if (localLength < 2) {
            // 此时无需匹对列表，直接将预置片放入缓存播放列表
//            AdDetailResponse adDetailResponse = new AdDetailResponse();
//            adDetailResponse.adVideoName = "预置片";
//            adDetailResponse.videoPath = getRawVideoUri();
//            cachePlayVideos.add(adDetailResponse);

        } else {
            // 匹对列表
            for (AdDetailResponse localVideo : localVideoList) {
                for (AdDetailResponse cacheVideo : cacheTomorrowList) {
                    if (!TextUtils.isEmpty(localVideo.adVideoName) && localVideo.adVideoName.equals(cacheVideo.adVideoName)) {
                        cacheVideo.videoPath = localVideo.videoPath;
                        LogCat.e("缓存视频：" + cacheVideo.adVideoName);
                        cachePlayVideos.add(cacheVideo);
                        break;
                    }
                }
            }
            // 匹配后结果不满足2个，仍然要播放预告片
            if (cachePlayVideos.size() < 2) {
//                cachePlayVideos.clear();
//                AdDetailResponse adDetailResponse = new AdDetailResponse();
//                adDetailResponse.adVideoName = "预置片";
//                adDetailResponse.videoPath = getRawVideoUri();
//                cachePlayVideos.add(adDetailResponse);

                cachePlayVideos.addAll(localVideoList);
            }
        }
        return cachePlayVideos;
    }


    /**
     * 开始截屏
     */
    private void startScreenShot() {
        screenShotTimer = new Timer();

        screenShotTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                // 开始截屏
                // 当前正在播放的视频
                AdDetailResponse videoAdBean = getPlayingVideoInfo();

                if (videoAdBean == null) {
                    return;
                }
                if (videoView == null) {
                    return;
                }
                LogCat.e("开始截图.......");
                LogCat.e("当前视频截图位置......." + videoView.getCurrentPosition());
                LogCat.e("当前视频总时长......." + videoView.getDuration());
                LogCat.e("当前视频名称......." + videoAdBean.adVideoName);
                ScreenShotUtils.screenShot(getActivity(), videoView.getCurrentPosition(), 0.5f, 0.5f, videoAdBean.videoPath);
            }
        }, 1000 * 60, 1000 * 60);
    }

    private void startPlayVideo() {
        if (playVideoLists == null || playVideoLists.size() < 2) {
            // 如果播放列表视频数量不足2个，继续检查本地缓存列表
            if (cachePlayVideoLists == null || cachePlayVideoLists.size() < 2) {
                // 本地缓存列表数量也不足2个，播放预置片
                LogCat.e("由于播放列表和缓存播放列表可以播放视频都不足2个，播放预置片.......");
                playingVideoName = getRawVideoUri();
            } else {
                for (AdDetailResponse adDetailResponse : cachePlayVideoLists) {
                    if (!TextUtils.isEmpty(adDetailResponse.videoPath)) {
                        playingVideoName = adDetailResponse.videoPath;
                        LogCat.e("播放缓存播放列表......." + adDetailResponse.adVideoName);
                        break;
                    }
                }

            }
        } else {
            for (AdDetailResponse adDetailResponse : playVideoLists) {
                if (!TextUtils.isEmpty(adDetailResponse.videoPath)) {
                    playingVideoName = adDetailResponse.videoPath;
                    LogCat.e("播放播放列表......." + adDetailResponse.adVideoName);
                    break;
                }
            }
        }
        playVideo(playingVideoName);
    }


    @Override
    protected void bindEvent() {
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                if (!isAdded()) {
                    return;
                }
                hideLoading();
                LogCat.e("video_onPrepared....");
                videoView.start();


            }
        });

        videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                if (!isAdded()) {
                    return false;
                }

                if (what == -38) {
                    return true;
                }
                LogCat.e("视频播放出错......");
                videoView.stopPlayback();

                // 继续播放下一个
                // 删除当前的视频
                // 继续下载播放失败的
                LogCat.e("开始处理当前出错的视频......");
                if (playVideoLists == null || playVideoLists.size() < 2) {
                    LogCat.e("当前视频在本地缓存列表中......");
                    int size = localVideoList.size();
                    if (localVideoList != null && size > 0 && playVideoIndex < size) {
                        AdDetailResponse adDetailResponse = localVideoList.get(playVideoIndex);
                        if (!adDetailResponse.isPresetPiece) {
                            localVideoList.remove(adDetailResponse);
                            LogCat.e("不是预置片，则从本地缓存列表中删除，并删除文件......" + adDetailResponse.adVideoName);
                            // 如果是缓存文件出错，直接删除当前文件，并播放下一个
                            DeleteFileUtils.getInstance().deleteFile(adDetailResponse.videoPath);
                        } else {
                            // TODO 上报预置片出错
                            LogCat.e("预置片播放出错......");
                        }
                    }

                } else {
                    int size = playVideoLists.size();
                    if (playVideoLists != null && size > 0 && playVideoIndex < size) {
                        AdDetailResponse adDetailResponse = playVideoLists.get(playVideoIndex);
                        LogCat.e("当前视频在播放列表中......" + adDetailResponse.adVideoName);
                        // 从播放列表将当前视频删除
                        playVideoLists.remove(adDetailResponse);
                        // 删除当前的视频文件
                        DeleteFileUtils.getInstance().deleteFile(adDetailResponse.videoPath);
                        LogCat.e("从播放列表中移除，并删除文件......");
                        // 添加重新下载当前文件
                        if (downloadLists != null) {

                            // 仍有任务没有下载完成，将当前任务添加到最后一个
                            downloadLists.add(adDetailResponse);
                            if (downloadLists.size() == 1) { // 此时表示已经没有下载任务，需要主动开启下载，否则表示下载还在进行中
                                prepareDownloading();
                                LogCat.e("此时表示已经没有下载任务，需要主动开启下载，否则表示下载还在进行中......");
                            } else {
                                LogCat.e("下载任务还没完成，将其加入到下载列表中......");
                            }
                        } else {
                            LogCat.e("此时表示已经没有下载任务，需要主动开启下载，否则表示下载还在进行中......");
                            downloadLists = new ArrayList<AdDetailResponse>();
                            downloadLists.add(adDetailResponse);
                            prepareDownloading();
                        }
                    }
                }
                LogCat.e("开始播放下一个视频......");
                playNext(true);

                return false;
            }
        });


        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (!isAdded()) {
                    return;
                }
                LogCat.e("视频播放完成。。。。。。");
                // 播放下一个视频
                playNext(false);
            }
        });
    }


    @Override
    public void onStop() {
        super.onStop();

        if (httpTimer != null) {
            httpTimer.cancel();
            httpTimer = null;
        }

        DLUtils.init(getActivity()).cancel();

        ScreenShotUtils.shutdown();


        if (screenShotTimer != null) {
            screenShotTimer.cancel();
        }

        if(downloadingVideoResponse != null){
            DeleteFileUtils.getInstance().deleteFile(downloadingVideoResponse.videoPath);
        }



    }

    @Override
    protected void onUpgradeSuccessful(UpdateResponse.UpdateInfoResponse updateInfo) {
        this.updateInfo = updateInfo;
        isDownloadVideo = false;
        DownloadUtils.download(getActivity(), Constants.FILE_DIRECTORY_APK, Constants.FILE_APK_NAME, updateInfo.fileUrl, this);

    }

    @Override
    protected void onGetVideoListSuccessful(VideoDetailListResponse response, String url) {
        if (!isAdded()) {
            return;
        }

        if (response == null) {
            // TODO 默认继续播放之前的缓存文件
            return;
        }
        ArrayList<AdDetailResponse> adDetailResponses = response.data;

        if (adDetailResponses == null || adDetailResponses.size() == 0) {
            // TODO 默认继续播放之前的缓存文件
            return;
        }

        // TODO 以后要删除的
        for (AdDetailResponse adDetailResponse : response.data) {
            adDetailResponse.adVideoName = adDetailResponse.name;
        }


        // 1.更新数据内容
//        LogCat.e("停止播放视频......");
//        videoView.stopPlayback();
        // 重新获取本地缓存列表
        LogCat.e("重新获取本地缓存列表......");
        localVideoList = getLocalVideoList();
        LogCat.e("------------------------------");
        // 缓存播放列表也更新
//        LogCat.e("根据今日播放列表，更新缓存播放列表......" + cachePlayVideoLists.size());
//        cachePlayVideoLists = getPlayVideoList(localVideoList, adDetailResponses);
//        LogCat.e("------------------------------");


        // 2.匹配今天要下载的视频
        LogCat.e("根据今日播放列表，获取下载列表......");
        downloadLists = getDownloadList(localVideoList, adDetailResponses);
        LogCat.e("------------------------------");


        // 3.匹配要删除的视频
        LogCat.e("根据今日播放列表，获取删除列表......");
        deleteLists = getDeleteList(localVideoList, adDetailResponses);
        LogCat.e("------------------------------");

        // 4.匹配要播放的视频
        LogCat.e("根据今日播放列表，获取播放列表......");
        playVideoLists = getTodayPlayVideoList(localVideoList, adDetailResponses);
        LogCat.e("------------------------------");

        // 5.匹配明天要下载的视频
        LogCat.e("根据明日播放列表，获取下载列表......");
        prepareDownloadLists = getDownloadList(localVideoList, adDetailResponses);
        LogCat.e("------------------------------");


        // 6.再次匹配要删除的视频列表,去除明日需要用到的视频，然后得到最终的删除列表

        LogCat.e("再次匹配要删除的视频列表,去除明日需要用到的视频，然后得到最终的删除列表......");
        removeTomorrowVideos(localVideoList, adDetailResponses);
        for (AdDetailResponse adDetailResponse : deleteLists) {
            LogCat.e("删除列表视频：" + adDetailResponse.adVideoName);
        }

        // 8.进行播放控制
//        LogCat.e("开始播放视频......");
//        startPlayVideo();

        // 9.进行删除控制
        LogCat.e("开始执行删除操作......");
        if (playVideoLists.size() > 2) {
            // 立即执行删除文件操作
            executeDeleteVideos();
        } else {
            // 否则就等待播放列表个数多余2个再进行删除
            LogCat.e("等待播放列表个数多余2个再进行删除......");
        }


        // 10.开始下载
        LogCat.e("开始下载......");
        for (AdDetailResponse adDetailResponse : downloadLists) {
            LogCat.e("需要下载的视频..." + adDetailResponse.adVideoName);
        }
        prepareDownloading();

        // 11.将明日播放列表缓存到本地
        LogCat.e("将明日播放列表缓存到本地......");
        new CacheVideoListThread(adDetailResponses, DataUtils.getCacheDirectory(), Constants.FILE_CACHE_NAME).start();


        LogCat.e("++++++++++++++++++++++++++++++++++++++++++++++++");
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isAdded()) {
                    doHttpGetEpisode();
                }

            }
        }, 1000 * 10);

        // 匹对视频，并进行播放和下载
//        matchVideos(adDetailResponses, adDetailResponses);

    }

    /**
     * 根据本地缓存视频列表和今日播放列表，得到需要下载的视频列表
     *
     * @param localVideoList
     * @param todayVideoList
     * @return
     */
    private ArrayList<AdDetailResponse> getDownloadList(ArrayList<AdDetailResponse> localVideoList, ArrayList<AdDetailResponse> todayVideoList) {
        ArrayList<AdDetailResponse> downloadList = new ArrayList<>();
        // 今天播放列表的视频不在本地缓存列表中，说明当前视频需要下载
        boolean isNeedDl;
        for (AdDetailResponse todayResponse : todayVideoList) {
            isNeedDl = true;
            for (AdDetailResponse localVideoResponse : localVideoList) {
                // 如果当前视频在本地缓存中，改视频不需要下载，反之下载
                if (!TextUtils.isEmpty(todayResponse.adVideoName) && todayResponse.adVideoName.equals(localVideoResponse.adVideoName)) {
                    isNeedDl = false;
                    break;
                }
            }
            if (isNeedDl) {
                downloadList.add(todayResponse);
                LogCat.e("需下载的视频：" + todayResponse.adVideoName);
            }
        }
        return downloadList;
    }


    /**
     * 根据本地缓存视频列表和今日播放列表，得到需要下载的删除文件列表
     *
     * @param localVideoList
     * @param todayVideoList
     * @return
     */
    private ArrayList<AdDetailResponse> getDeleteList(ArrayList<AdDetailResponse> localVideoList, ArrayList<AdDetailResponse> todayVideoList) {
        // 删除文件为在本地缓存视频列表，而不在今日播放列表的内容
        ArrayList<AdDetailResponse> deleteList = new ArrayList<>();
        boolean isNeedDel;
        for (AdDetailResponse localVideoResponse : localVideoList) {
            isNeedDel = true;
            for (AdDetailResponse todayResponse : todayVideoList) {
                // 今天播放列表的视频不在本地缓存列表中，说明当前视频需要下载
                if (!TextUtils.isEmpty(todayResponse.adVideoName) && todayResponse.adVideoName.equals(localVideoResponse.adVideoName)) {
                    isNeedDel = false;
                    break;
                }
            }
            if (isNeedDel) {
                LogCat.e("需要删除的视频：" + localVideoResponse.adVideoName);
                deleteList.add(localVideoResponse);
            }
        }
        return deleteList;
    }


    /**
     * 根据本地缓存视频列表和今日播放列表，得到需要下载的删除文件列表
     *
     * @param localVideoList
     * @param todayVideoList
     * @return
     */
    private ArrayList<AdDetailResponse> getTodayPlayVideoList(ArrayList<AdDetailResponse> localVideoList, ArrayList<AdDetailResponse> todayVideoList) {
        // 播放列表的内容为本地缓存视频在今日播放列表中
        ArrayList<AdDetailResponse> playList = new ArrayList<>();
        for (AdDetailResponse localVideoResponse : localVideoList) {
            for (AdDetailResponse todayResponse : todayVideoList) {
                // 本地缓存视频在今日播放列表中，说明当前视频可以直接播放
                if (!TextUtils.isEmpty(todayResponse.adVideoName) && todayResponse.adVideoName.equals(localVideoResponse.adVideoName)) {
                    todayResponse.videoPath = localVideoResponse.videoPath;
                    playList.add(todayResponse);
                    LogCat.e("播放列表：" + todayResponse.adVideoName);
                    break;
                }
            }
        }
        return playList;
    }


    private void removeTomorrowVideos(ArrayList<AdDetailResponse> localVideoList, ArrayList<AdDetailResponse> tomorrowList) {
        // 获取明日需要用到的视频
        ArrayList<AdDetailResponse> preparePlayList = getTodayPlayVideoList(localVideoList, tomorrowList);

        // 从删除列表中取出明日需要用到的视频
        boolean isTomorrowUsed;
        int indexUsed = 0;
        for (AdDetailResponse prepareVideo : preparePlayList) {
            isTomorrowUsed = false;
            int length = deleteLists.size();
            for (int i = 0; i < length; i++) {
                AdDetailResponse deleteVideo = deleteLists.get(i);
                // 当前视频需要从删除列表中删除
                if (!TextUtils.isEmpty(deleteVideo.adVideoName) && deleteVideo.adVideoName.equals(prepareVideo.adVideoName)) {
                    isTomorrowUsed = true;
                    indexUsed = i;
                    break;
                }
            }
            if (isTomorrowUsed) {
                // 当前视频在在明日
                deleteLists.remove(indexUsed);
            }

        }
    }


    private void matchVideos(ArrayList<AdDetailResponse> todayVideos, ArrayList<AdDetailResponse> tomorrowVideos) {
        // serverList 去除本地缓存中需要用到的就是都要下载的

        downloadLists = new ArrayList<>();

        // 本地缓存中还有用的视频，或者预置片
        playVideoLists = new ArrayList<>();

        // 本地缓存中已经无用的视频
        deleteLists = new ArrayList<>();

        // 预下载视频列表
        prepareDownloadLists = new ArrayList<>();

        // 默认全部视频都需要下载
        downloadLists.addAll(todayVideos);

        // 匹配当天的视频列表，可用的视频加入playVideoLists，同时将其从downloadLists中删除，无用的视频加入deleteLists中
        ArrayList<AdDetailResponse> localVideos = matchServerVideos(todayVideos);

        // 开启下载
        for (AdDetailResponse adDetailResponse : downloadLists) {
            LogCat.e("需要下载的视频..." + adDetailResponse.adVideoName);
        }
        // 开启下载
        prepareDownloading();


        LogCat.e("开始匹对明天的播放列表");
        // 开始匹配第二天的视频列表，可用的视频从prepareDownloadLists删除，并检测是否在deleteLists中，如果在就从deleteLists删除，否在就下载
        matchTomorrowList(tomorrowVideos, localVideos);

        // 释放资源
        localVideos.clear();
        localVideos = null;

        // 将明日的列表缓存到本地
        new CacheVideoListThread(tomorrowVideos, DataUtils.getCacheDirectory(), Constants.FILE_CACHE_NAME).start();
        // 查看是在playlist中播放视频还是从deleteList中播放视频
        playAndDeleteVideos();

    }

    private void matchTomorrowList(ArrayList<AdDetailResponse> tomorrowVideos, ArrayList<AdDetailResponse> localVideos) {
        if (tomorrowVideos == null || tomorrowVideos.size() == 0) {
            prepareDownloadLists = null;
            playAndDeleteVideos();
            return;
        }

        if (localVideos == null || localVideos.size() == 0) {
            prepareDownloadLists = null;
            playAndDeleteVideos();
            return;
        }

        // 默认所有的视频都可以下载
        prepareDownloadLists.addAll(tomorrowVideos);
        for (AdDetailResponse localVideo : localVideos) {
            // 将当前的本地视频文件跟预下载的列表匹对，如果匹对成功，说明不需要下载，否则下载
            for (AdDetailResponse prepareVideo : tomorrowVideos) {
                if (prepareVideo.adVideoName.equals(localVideo.adVideoName)) {
                    // 无需下载
                    LogCat.e("无需下载的明日视频：" + prepareVideo.adVideoName);
                    prepareDownloadLists.remove(prepareVideo);
                    // 判断当前列表是否在删除列表
                    int length = deleteLists.size();
                    if (length > 0) {
                        int index = -1;
                        for (int i = length - 1; i >= 0; i--) {
                            AdDetailResponse deleteResponse = deleteLists.get(i);
                            // 如果当前视频在明日列表仍然需要使用，从删除列表中删除当前的
                            if (deleteResponse.adVideoName.equals(localVideo.adVideoName)) {
                                index = i;
                                break;
                            }
                        }

                        if (index != -1) {
                            deleteLists.remove(index);
                        }
                    }
                }
            }
        }


        if (prepareDownloadLists != null && prepareDownloadLists.size() != 0) {
            for (AdDetailResponse adDetailResponse : prepareDownloadLists) {
                LogCat.e("需要下载的prepareVideo: " + adDetailResponse.adVideoName);
            }
        } else {
            LogCat.e("所有预下载的视频已经存在，无需下载......");
            prepareDownloadLists = null;
        }
    }

    // 对视频进行播放，并在可以删除的情况下删除视频
    private void playAndDeleteVideos() {
        // 查看播放列表
        boolean isAccessDelete = checkPlayVideos();
        // 进行文件的删除
        if (isAccessDelete) {
            // 删除文件
            LogCat.e("运行执行删除文件......" + (deleteLists == null ? 0 : deleteLists.size()));
            executeDeleteVideos();
        } else {
            LogCat.e("此时正在播放删除文件列表，稍后进行删除文件......" + (deleteLists == null ? 0 : deleteLists.size()));
            // 等待下载超过2个缓存文件再删除

        }
    }

    private boolean checkPlayVideos() {
        LogCat.e("确定如何播放视频......");
        boolean isAccessDelete = false;
        if (playVideoLists.size() < 2) {
            LogCat.e("当前播放列表数量<2，继续检查删除列表的视频......");
            // 如果少于2个可以播放的 就播放旧视频
            if (deleteLists.size() < 2) {
                LogCat.e("删除列表的视频数量也不足2个，播放预置片......");
                // 如果旧的可以播放的视频数量少于2个，就播放预置片
                // TODO: 16/3/25   立即停止播放，开始播放与质朴而

                // 删除  删除列表中的视频文件
                isAccessDelete = true;
            } else {
                LogCat.e("删除列表的视频数量>2个，停止当前播放，开始播放删除列表......");
                // 停止当前播放的视频，开始播放删除列表的视频，等下载个数超过2个在进行删除操作
                videoView.stopPlayback();
                // TODO 以后为了优化存储空间，可以先删除多余视频，只保留2个循环播放
                playVideo(downloadLists.get(0).videoPath);
                isAccessDelete = false;
            }
        } else {
            LogCat.e("当前可播放的视频数量>2，播放当前的播放列表......");
            // 立即停止播放
            videoView.stopPlayback();
            // 开始播放播放列表的内容
            int length = playVideoLists.size();
            for (int i = 0; i < length; i++) {
                AdDetailResponse adDetailResponse = playVideoLists.get(i);
                if (adDetailResponse != null) {
                    playVideo(adDetailResponse.videoPath);
                }
            }
            isAccessDelete = true;
        }

        return isAccessDelete;
    }

    private ArrayList<AdDetailResponse> matchServerVideos(ArrayList<AdDetailResponse> adDetailResponses) {
        LogCat.e("检测本地video目录......");
        if (adDetailResponses == null || adDetailResponses.size() == 0) {
            return null;
        }

        ArrayList<AdDetailResponse> localVideos = null;
        File videoFile = new File(DataUtils.getVideoDirectory());
        if (videoFile.exists()) {
            LogCat.e("本地video目录存在......");
            // 将目录中的文件生成一个video表
            localVideos = getLocalList(videoFile);
            if (localVideos != null && localVideos.size() > 0) {
                // 有缓存视频文件
                // 匹对当前的视频内容
                LogCat.e("将video目录中的文件与server表进行匹对......");
                for (AdDetailResponse localVideoResponse : localVideos) {
                    boolean isDelete = true;
                    for (AdDetailResponse serverResponse : adDetailResponses) {
                        // 当前本地缓存视频可以继续使用，加入播放列表
                        if (serverResponse.adVideoName.equals(localVideoResponse.adVideoName)) {
                            isDelete = false;
                            serverResponse.videoPath = localVideoResponse.videoPath;
                            playVideoLists.add(serverResponse);
                            // 从下载列表中删除当前的item
                            downloadLists.remove(serverResponse);
                            LogCat.e("匹对成功的视频，无需删除......" + serverResponse.adVideoName);
                        }
                    }
                    // 没有匹对的视频，将其加入删除列表
                    if (isDelete) {
                        deleteLists.add(localVideoResponse);
                    }
                }
            }
        }
        return localVideos;
    }

    private void executeDeleteVideos() {
        if (deleteLists != null && deleteLists.size() > 0) {
            AdDetailResponse playingVideo = null;
            for (AdDetailResponse adDetailResponse : deleteLists) {
                if (!TextUtils.isEmpty(playingVideoName) && playingVideoName.equals(adDetailResponse.adVideoName)) {
                    playingVideo = adDetailResponse;
                } else {
                    DeleteFileUtils.getInstance().deleteFile(adDetailResponse.videoPath);
                }
                LogCat.e("删除的文件....." + adDetailResponse.adVideoName);
            }
            deleteLists.clear();
            if (playingVideo != null) {
                deleteLists.add(playingVideo);
            }
        } else {
            LogCat.e("无需删除文件......");
        }
    }


    @Override
    protected void onGetVideoListFailed(String errorMsg, String url) {
        if (!isAdded()) {
            return;
        }

        videoView.postDelayed(new Runnable() {
            @Override
            public void run() {
                doHttpGetEpisode();
            }
        }, 1000 * 10);
    }

    @Override
    protected void onGetVideoPathSuccessful(String url) {
        if (!isAdded()) {
            return;
        }
        this.videoUrl = url;
        retryGetVideoListTimes = 0;
        LogCat.e("获取到当前视频的下载地址。。。。。。。。" + url);
        download(url);

    }


    @Override
    protected void onGetVideoPathFailed(String path) {
        LogCat.e("当前视频的下载地址获取失败。。。。");
        final int size = downloadLists.size();
        if (size > 1) {
            LogCat.e("将当前下载地址获取失败的视频放到最后一个，继续下载后续的视频。。。。");

            videoView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    downloadLists.add(size, downloadingVideoResponse);
                    downloadLists.remove(0);
                    prepareDownloading();
                }
            }, 50000);


        } else {
            LogCat.e("只剩最后一个视频。。。。");
            if (retryGetVideoListTimes < 2) {
                retryGetVideoListTimes++;
                LogCat.e("第 " + retryGetVideoListTimes + " 次重新尝试获取下载地址");
                prepareDownloading();
            } else {
                retryGetVideoListTimes = 0;
                LogCat.e("经过尝试后，仍无法获取到当前视频的下载地址，放弃此次下载");
            }
        }
    }

    @Override
    public void onDownloadFileSuccess(String filePath) {
        if (isDownloadVideo) {
            LogCat.e("onFinish............. " + filePath);
            // 把下载成功的视频添加到播放列表中
            playVideoLists.add(downloadingVideoResponse);
            // 把当前下载的任务从播放列删除
            downloadLists.remove(downloadingVideoResponse);
            // 继续进行下载任务
            prepareDownloading();
        } else {
            // 把下载成功的视频添加到播放列表中
            downloadApkSuccess(filePath);
        }

    }


    @Override
    public void onDownloadFileError(int errorCode, String errorMsg) {
        if (isDownloadVideo) {
            if (errorCode == ErrorCodes.ERROR_DOWNLOAD_SDCARD_SPACE) { // 如果是空间不足的错误，就不在进行下载
                // TODO 上报情况
            } else {
                downVideoError();
            }
        } else {
            downloadApkError();
        }

    }

    private void downVideoError() {
        // 出错就放弃当前下载任务，继续下载下一个任务，并将当前任务放到最后一个，如果已经是最后一个，再重试2边
        final int size = downloadLists.size();
//        if (size > 1) {
        videoView.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isAdded()) {
                    return;
                }
                LogCat.e("5秒后继续尝试，如此循环。。。。");
                if (retryGetVideoListTimes < 3) {
                    retryGetVideoListTimes++;
                    LogCat.e("继续重试3次下载，此时是第" + retryGetVideoListTimes + "次尝试。。。。");
                    download(videoUrl);
                } else {
                    retryGetVideoListTimes = 0;
                    LogCat.e("将当前下载失败的视频放到最后一个，继续下载后续的视频。。。。");
                    downloadLists.add(size, downloadingVideoResponse);
                    downloadLists.remove(0);
                    prepareDownloading();
                }
            }
        }, 5000);

//        } else {
//            LogCat.e("只剩最后一个视频。。。。");
//            videoView.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//
//                    if (!isAdded()) {
//                        return;
//                    }
//                    LogCat.e("5秒后继续尝试，如此循环。。。。");
//                    download(videoUrl);
//
//                }
//            }, 5000);
//        }
    }


    private void downloadApkSuccess(String filePath) {
        updateInfo = null;
        //新包下载完成得安装
        if(RootUtils.hasRootPerssion()){
            //RootUtils.clientInstall("/sdcard/Music/test.apk");
            SharedPreference.getSharedPreferenceUtils(getActivity()).saveDate("isClientInstall", true);
            RootUtils.clientInstall(DataUtils.getApkDirectory() + Constants.FILE_APK_NAME);
            //Toast.makeText(getActivity(), "有root权限，静默安装方式", Toast.LENGTH_LONG).show();
            LogCat.e("有root权限，静默安装方式");
        }else{
            //Toast.makeText(getActivity(),"没有root权限，普通安装方式",Toast.LENGTH_LONG).show();
            //RootUtils.installApk(CustomActivity.this,"/sdcard/Music/test.apk");
            LogCat.e("没有root权限，普通安装方式");
            DataUtils.installApk(getActivity(), filePath);
        }
        getActivity().finish();
    }


    private void downloadApkError() {
        // 此时出错，需要判断是否是强制升级，如果是强制升级，说明是接口等重大功能改变，必须优先升级
        // 强制升级：如果出错，就要循环去做升级操作，直至升级成
        // 普通升级：如果出错，不再请求，去请求视频接口
        if ("1".equals(updateInfo.type)) {
            // 强制更新
            videoView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    LogCat.e("5秒后继续尝试，如此循环。。。。");
                    isDownloadVideo = false;
                    DownloadUtils.download(getActivity(), Constants.FILE_DIRECTORY_APK, Constants.FILE_APK_NAME, updateInfo.fileUrl, AdOneFragment.this);
                }
            }, 5000);

        } else {
            updateInfo = null;
            doHttpGetEpisode();
        }
    }


    /**
     * 下载准备工作
     */
    private void prepareDownloading() {
        if (downloadLists.size() == 0) {
            LogCat.e("所有视频下载完成。。。。。。。。");
            downloadingVideoResponse = null;
        } else {
            LogCat.e("获取下载列表第一个视频，并开始下载。。。。。。。。 还剩余下载任务：" + downloadLists.size());
            downloadingVideoResponse = downloadLists.get(0);
            String path = DataUtils.getSdCardFileDirectory() + Constants.FILE_DIRECTORY_VIDEO;
            downloadingVideoResponse.videoPath = path + downloadingVideoResponse.adVideoName + Constants.FILE_DOWNLOAD_EXTENSION;
            if (downloadingVideoResponse.playInfo != null && downloadingVideoResponse.playInfo.size() != 0) {
                PlayInfoResponse playInfoResponse = downloadingVideoResponse.playInfo.get(0);
                doHttpGetCdnPath(playInfoResponse.remotevid);
            }
        }
    }

    private void download(String url) {
        isDownloadVideo = true;
        // 一个视频一个视频的下载
        DownloadUtils.download(getActivity(), Constants.FILE_DIRECTORY_VIDEO, downloadingVideoResponse.adVideoName + Constants.FILE_DOWNLOAD_EXTENSION, url, this);
    }


    private void playNext(boolean isVideoError) {
        // 此时说明还没有可以播放的视频，播放预置片或者缓存文件
        // 视频正常播放
        LogCat.e("即将开始播放下一个视频.......");
        if (playVideoLists == null || playVideoLists.size() < 2) {
            LogCat.e("播放列表数据不足2个.......");
            if (isVideoError) {
                LogCat.e("由于当前视频播放出错，导致播放下一个.......");
                if (playVideoLists != null && playVideoLists.size() > 0) {
                    LogCat.e("播放列表还有视频可以播放.......");
                    playNextVideo(playVideoLists);

                } else {
                    LogCat.e("播放列表无数据了，播放预置片.......");
                    playPresetVideo();
                }
            } else {
                LogCat.e("正常进行播放下一个.......");
                if (cachePlayVideoLists == null || cachePlayVideoLists.size() < 2) {
                    playPresetVideo();
                    LogCat.e("播放列表无数据了，播放预置片.......");

                } else {
                    LogCat.e("播放列表还有视频可以播放.......");
                    // 播放缓存列表文件
                    playNextVideo(cachePlayVideoLists);
                }
            }
        } else {
            LogCat.e("播放列表数据多于2个.......");
            // 删除所有需要删除的缓存文件
            if (deleteLists != null && deleteLists.size() > 0) {
                LogCat.e("删除列表还有内容，则进行删除操作.......");
                for (AdDetailResponse deleteResponse : deleteLists) {
                    LogCat.e("删除文件......." + deleteResponse.adVideoName);
                    DeleteFileUtils.getInstance().deleteFile(deleteResponse.videoPath);
                }
                deleteLists.clear();
            }

            // 情况缓存文件列表，重置状态
            if (cachePlayVideoLists != null && cachePlayVideoLists.size() > 0) {
                LogCat.e("清空缓存播放列表.......");
                cachePlayVideoLists.clear();
            }

            AdDetailResponse videoAdBean = playVideoLists.get(playVideoIndex);
            LogCat.e("添加一次视频播放" + videoAdBean.adVideoName);
            // 播放缓存列表文件
            playNextVideo(playVideoLists);

        }
    }

    private void playNextVideo(ArrayList<AdDetailResponse> playVideoLists) {
        int length = playVideoLists.size();
        if (length > playVideoIndex) {
            LogCat.e("-----------------------------");
            for (int i = 0; i < length; i++) {
                AdDetailResponse adDetailResponse = playVideoLists.get(i);
                LogCat.e("播放列表：" + adDetailResponse.adVideoName);
                LogCat.e("播放地址：" + adDetailResponse.videoPath);
            }
            LogCat.e("-----------------------------");
            AdDetailResponse adDetailResponse = playVideoLists.get(playVideoIndex);
            LogCat.e("当前正在播放结束的是：" + adDetailResponse.adVideoName);
        }

        LogCat.e("当前播放列表数量：" + length);
        playVideoIndex++;
        if (playVideoIndex >= length) {
            playVideoIndex = 0;
        }
        AdDetailResponse adDetailResponse = playVideoLists.get(playVideoIndex);
        playVideo(adDetailResponse.videoPath);
        playingVideoName = adDetailResponse.adVideoName;
        LogCat.e("即将播放视频。。。" + adDetailResponse.adVideoName + "  " + playVideoIndex);
    }


    /**
     * 检测网络，如果链接正常则请求接口
     */
    private void httpRequest() {
        httpTimer = new Timer();
        httpTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                LogCat.e("正在检测是否联网。。。。。");
                if (DataUtils.isNetworkConnected(getActivity())) {
                    // 先去请求服务器，查看视频列表
                    doHttpUpdate(getActivity());
                    httpTimer.cancel();
                    httpTimer = null;
                    LogCat.e("已经联网。。。。。");
                } else {
                    LogCat.e("没有联网。。。。。继续检查");
                }
            }
        }, 0, 10 * 1000);
    }

    // 初始化本地缓存表
    private void initLocalBufferList() {
        File fileVideo = new File(DataUtils.getVideoDirectory());
        if (fileVideo.exists() && fileVideo.isDirectory()) {
            LogCat.e("Video文件目录存在.....");
            File[] fileVideos = fileVideo.listFiles();
            if (fileVideos.length < 2) {
                // TODO 播放预置片
                LogCat.e("Video文件目录文件数量不足2个，播放预置片.....");
                playPresetVideo();
            } else {
                // 检测缓存文件是否存在
                File cacheFile = new File(DataUtils.getCacheDirectory() + Constants.FILE_CACHE_NAME);
                if (cacheFile.exists() && cacheFile.isFile()) {
                    // 文件存在
                    LogCat.e("准备读取cache的内容.....");
                    // 读取缓存文件
                    String json = DataUtils.readFileFromSdCard(cacheFile);
                    if (TextUtils.isEmpty(json)) {
                        LogCat.e("cache文件内容为null.....");
                        // TODO 播放video的视频
                        playDirectoryVideo(fileVideo);
                        LogCat.e("播放Video文件目录的视频内容.....");
                    } else {
                        // 将其转换成实体类
                        Gson gson = new Gson();
                        ArrayList<AdDetailResponse> adDetailResponses = gson.fromJson(json, new TypeToken<ArrayList<AdDetailResponse>>() {
                        }.getType());
                        if (adDetailResponses == null || adDetailResponses.size() < 2) {
                            // 检测video视频目录。如果视频多余2个，加入local，少于2个播放预置片
                            LogCat.e("cache文件内视频列表的数量小于2.....");
                            // TODO 播放video的视频
                            playDirectoryVideo(fileVideo);
                            LogCat.e("播放Video文件目录的视频内容.....");
                        } else {
                            // TODO
                            for (AdDetailResponse adDetailResponse : adDetailResponses) {
                                adDetailResponse.adVideoName = adDetailResponse.name;
                            }

                            LogCat.e("开始匹配cache的列表和video的文件列表.....");
                            // 检测prepareVideo目录下的视频，跟上面的列表进行配对，如果有则加入Local列表
                            ArrayList<AdDetailResponse> fileVideoList = getLocalList(fileVideo);
                            // 匹配视频列表，获取有用的视频
                            int fileVideoSize = fileVideoList.size();

                            int cacheVideoSize = adDetailResponses.size();
                            for (int i = 0; i < fileVideoSize; i++) {
                                AdDetailResponse fileVideoResponse = fileVideoList.get(i);
                                for (int j = 0; j < cacheVideoSize; j++) {
                                    AdDetailResponse cacheVideoResponse = adDetailResponses.get(j);
                                    if (cacheVideoResponse.adVideoName.equals(fileVideoResponse.adVideoName)) {
                                        // 当前视频可以使用
                                        cacheVideoResponse.videoPath = fileVideoResponse.videoPath;
                                        // 加入本地视频列表
                                        localVideoList.add(cacheVideoResponse);
                                        LogCat.e("跟cache匹对成功的视频......" + cacheVideoResponse.adVideoName);
                                        break;
                                    }
                                }
                            }

                            if (localVideoList.size() < 2) {
                                // 检测video视频目录。如果视频多余2个，加入local，少于2个播放预置片
                                LogCat.e("匹配后的结果集数量不足2个，情况local，准备播放昨天的内容.....");
                                // TODO 播放video的视频
                                LogCat.e("播放Video文件目录的视频内容.....");
                                playDirectoryVideo(fileVideo);
                            } else {
                                // 播放缓存列表的视频内容
                                LogCat.e("匹配到合适的结果，准备播放.....");
                                playVideo(localVideoList.get(0).videoPath);
                            }
                        }
                    }
                } else {
                    // 缓存文件不存在，直接查看video目录
                    // TODO 播放video的视频
                    LogCat.e("播放Video文件目录的视频内容.....");
                    playDirectoryVideo(fileVideo);
                }
            }
        } else {
            // TODO 播放预置片
            LogCat.e("video目录不存在，播放预置片.....");
            playPresetVideo();
        }

    }


    private void playPresetVideo() {
//        localVideoList.clear();
//        AdDetailResponse videoAdBean = new AdDetailResponse();
//        videoAdBean.adVideoName = "预置片";
//        videoAdBean.videoPath = getRawVideoUri();
//        videoAdBean.isPresetPiece = true;
//        videoAdBean.adVideoIndex = 0;
//        localVideoList.add(videoAdBean);
        playVideo(getRawVideoUri());
        playingVideoName = "预置片";

    }

    private void playDirectoryVideo(File file) {
        localVideoList.clear();

        localVideoList = getLocalList(file);

        playVideo(localVideoList.get(0).videoPath);

    }


    private ArrayList<AdDetailResponse> getLocalList(File videoFiles) {
        ArrayList<AdDetailResponse> adDetailResponses = new ArrayList<>();
        File[] files = videoFiles.listFiles();
        for (File file : files) {
            if (file.isFile()) {
                AdDetailResponse videoAdBean = new AdDetailResponse();
                String name = file.getName();
                // 正在下载的文件不能算到本地缓存列表中
                if (DLUtils.init(getActivity()).downloading(name)) {
                    LogCat.e("当前文件正在下载。。。。。");
                    continue;
                }

                int index = name.lastIndexOf(Constants.FILE_DOWNLOAD_EXTENSION);
                name = name.substring(0, index);
                // TODO 以后要删除的
                videoAdBean.adVideoName = name;
                videoAdBean.videoPath = file.getAbsolutePath();
                adDetailResponses.add(videoAdBean);
                LogCat.e("本地缓存视频：" + videoAdBean.adVideoName);
            } else {
                DeleteFileUtils.getInstance().deleteFile(file.getAbsolutePath());
            }
        }
        return adDetailResponses;
    }


    /**
     * 播放本地视频
     *
     * @param videoPath
     */
    private void playVideo(String videoPath) {
        if (!TextUtils.isEmpty(videoPath) && videoView != null) {
            videoView.setVideoPath(videoPath);
        }
    }

    /**
     * 获取预置片的地址
     *
     * @return
     */
    private String getRawVideoUri() {
        return DataUtils.getRawVideoUri(getActivity(), R.raw.video_test);
    }


    /**
     * 显示loading状态
     */
    private void showLoading() {
        if (loading != null && loading.getVisibility() != View.VISIBLE) {
            loading.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 隐藏loading状态
     */
    private void hideLoading() {
        if (loading != null && loading.getVisibility() != View.GONE) {
            loading.setVisibility(View.GONE);
        }
    }


    private AdDetailResponse getPlayingVideoInfo() {
        AdDetailResponse videoAdBean = null;
        if (playVideoLists == null || playVideoLists.size() < 2) {
            if (localVideoList != null && localVideoList.size() > 0) {
                int index = 0;
                if(playVideoIndex < localVideoList.size()){
                    index = playVideoIndex;
                }
                videoAdBean = localVideoList.get(index);
            } else {
                videoAdBean = new AdDetailResponse();
                videoAdBean.adVideoName = "预置片";
                videoAdBean.videoPath = getRawVideoUri();
            }
        } else {
            int index = 0;
            if(playVideoIndex < playVideoLists.size()){
                index = playVideoIndex;
            }
            videoAdBean = playVideoLists.get(index);
        }
        return videoAdBean;
    }


}
