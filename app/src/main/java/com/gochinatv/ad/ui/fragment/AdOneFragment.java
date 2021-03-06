package com.gochinatv.ad.ui.fragment;

import android.app.FragmentTransaction;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.download.DLUtils;
import com.download.ErrorCodes;
import com.download.db.DLDao;
import com.download.db.DownloadInfo;
import com.gochinatv.ad.MainActivity;
import com.gochinatv.ad.R;
import com.gochinatv.ad.base.BaseFragment;
import com.gochinatv.ad.interfaces.OnUpgradeStatusListener;
import com.gochinatv.ad.screenshot.ScreenShotUtils;
import com.gochinatv.ad.thread.DeleteFileUtils;
import com.gochinatv.ad.thread.VideoStatusRunnable;
import com.gochinatv.ad.tools.Constants;
import com.gochinatv.ad.tools.DataUtils;
import com.gochinatv.ad.tools.DownloadUtils;
import com.gochinatv.ad.tools.LogCat;
import com.gochinatv.ad.tools.LogMsgUtils;
import com.gochinatv.ad.tools.SharedPreference;
import com.gochinatv.ad.tools.UmengUtils;
import com.gochinatv.ad.tools.VideoAdUtils;
import com.gochinatv.ad.video.MeasureVideoView;
import com.okhtttp.OkHttpCallBack;
import com.okhtttp.response.AdDetailResponse;
import com.okhtttp.response.AdVideoListResponse;
import com.okhtttp.response.LayoutResponse;
import com.okhtttp.response.ScreenShotResponse;
import com.okhtttp.service.VideoHttpService;
import com.retrofit.download.RetrofitDLUtils;
import com.umeng.analytics.MobclickAgent;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import static com.gochinatv.ad.tools.VideoAdUtils.getDistinctList;
import static com.gochinatv.ad.tools.VideoAdUtils.sendDeleteVideo;
import static com.gochinatv.ad.tools.VideoAdUtils.sendVideoDownloadTime;
import static com.gochinatv.ad.tools.VideoAdUtils.sendVideoPlayTimes;

/**
 * Created by zfy on 2016/3/16.
 */
public class AdOneFragment extends BaseFragment implements OnUpgradeStatusListener {

    private MeasureVideoView videoView;
    private LinearLayout loading;
    /**
     * 本地数据表
     */
    private ArrayList<AdDetailResponse> localVideoList;

    /**
     * 当前下载视频的info
     */
    private AdDetailResponse downloadingVideoResponse;
    /**
     * 下载重试次数
     */
    private int retryTimes;

    /**
     * 当前播放视频的序号
     */
    private int playVideoIndex;
    private ArrayList<AdDetailResponse> playVideoLists = null;
    private ArrayList<AdDetailResponse> cachePlayVideoLists = null;
    private ArrayList<AdDetailResponse> deleteLists = null;
    private ArrayList<AdDetailResponse> downloadLists = null;
    private ArrayList<AdDetailResponse> prepareDownloadLists = null;

    /**
     * 是否用更新的apk在下载
     */
    private boolean isDownloadAPK;

    private String videoUrl;

    private int delay = 1000 * 60 * 60;
    private ScheduledExecutorService screenShotService;

    private boolean isDownloadPrepare;

    private ScreenShotResponse screenShotResponse;


    /**
     * 进行重试的时间间隔
     */
    private final int TIME_RETRY_DURATION = 1000 * 20;
    /**
     * 测试的时间间隔
     */
    private final int TEST_TIME_DURATION = 1000 * 60;

    private final int TIME_CHECK_VIDEO_DURATION = 1000 * 60 * 2;
    /**
     * 最多进行重试的次数
     */
    private final int MAX_RETRY_TIMES = 3;
    //布局参数
    private LayoutResponse layoutResponse;

    private TextView tvProgress;

    private MyHandler handler;
    // 接口重复刷新时间
    private long pollInterval = 4 * 60 * 60 * 1000;//默认4个小时

    private int playOrderPos;
    private ArrayList<AdDetailResponse> orderVideoList;

    private AdDetailResponse playingVideoInfo;

    private long startDownloadTime;//开始下载时间
    //private long endDownloadTime;//开始下载时间
    private VideoStatusRunnable videoStatusRunnable;

    @Override
    protected View initLayout(LayoutInflater inflater, ViewGroup container) {
        rootView = inflater.inflate(R.layout.fragment_ad_video, container, false);
        intLayoutParams(layoutResponse);
        return rootView;
    }


    @Override
    protected void initView(View rootView) {
        videoView = (MeasureVideoView) rootView.findViewById(R.id.videoView);
        loading = (LinearLayout) rootView.findViewById(R.id.loading);
        tvProgress = (TextView) rootView.findViewById(R.id.tv_progress);
    }

    @Override
    protected void init() {
        // 显示loading加载状态
        showLoading();
        // 先判断sdcard状态是否可用，如果不可用，直接播放本地视频
        if (!DataUtils.isExistSDCard()) {
            LogCat.e("video", "sd卡状态不可用......");
            playPresetVideo();
            return;
        }
        //  开启轮询接口
        handler = new MyHandler();
        // 1.初始化本地缓存表
        LogCat.e("video", "获取本地缓存视频列表.......");
        localVideoList = VideoAdUtils.getLocalVideoList(getActivity());
        cachePlayVideoLists = new ArrayList<>();
        if (localVideoList.size() != 0) {
            // 5.根据本地缓存视频列表和缓存明日列表，得出当前的播放列表
            LogCat.e("video", "更换缓存的所有信息,包括今天和明天来获取播放列表.....");
            cachePlayVideoLists.addAll(VideoAdUtils.getPlayVideoList(getActivity(), localVideoList));
            LogCat.e("video", "------------------------------");
        } else {
            cachePlayVideoLists.addAll(localVideoList);
        }

        // 4.播放缓存列表
        LogCat.e("video", "查找可以播放的视频.....");
        startPlayVideo();

        // 删除旧的文件目录
        LogCat.e("video", "请求接口.....");
        // 6.请求视频列表
        if (!isDownloadAPK) { // 当有下载任务的时候，就不会再去请求视频列表，全部资源给下载apk
            httpRequest(true);
        }

        // 7.开启上传截图
        startScreenShot();


        videoStatusRunnable = new VideoStatusRunnable(this, videoView, playingVideoInfo, handler);
        // 开始视频的守护线程
        handler.postDelayed(videoStatusRunnable, TIME_CHECK_VIDEO_DURATION);

        // 没有网络
        if (!DataUtils.isNetworkConnected(getActivity())) {
            showNetSpeed(false, false, 0);
            //显示完成视频数量
            showCompletedNotNetwork(true);
        }
    }

    /**
     * 当无网络时显示完成视频数量
     *
     * @param isStartUpNotNet -- 是否是启动时无网络
     */
    public void showCompletedNotNetwork(final boolean isStartUpNotNet) {
        if (!isAdded()) {
            return;
        }
        if(handler == null){
            handler = new MyHandler();
        }
        Message msg = handler.obtainMessage(5);
        Bundle bundle = new Bundle();
        bundle.putBoolean("isStartUpNotNet", isStartUpNotNet);
        msg.setData(bundle);
        handler.sendMessage(msg);
    }


    @Override
    protected void bindEvent() {
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                if (isDetached()) {
                    return;
                }
                hideLoading();
                LogCat.e("video", "video_onPrepared....");
                videoView.start();


            }
        });

        videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                if (isDetached()) {
                    return true;
                }
                //上报播放错误
                VideoAdUtils.sendPlayVideoError(getActivity(),what,extra,playingVideoInfo);
                LogCat.e("video", "视频播放出错......");
                if (what != 1) {
                    return true;
                }
                fixErrorVideo();
                return true;
            }
        });


        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (isDetached()) {
                    return;
                }
                LogCat.e("video", "视频播放完成。。。。。。");
                // 播放下一个视频
                playNext(false);
            }
        });
    }

    /**
     * 修复错误视频
     */
    public void fixErrorVideo() {
        videoView.stopPlayback();

        videoView.postDelayed(new Runnable() {
            @Override
            public void run() {
                fixVideoError();
                LogCat.e("video", "开始播放下一个视频......");
                playNext(true);
            }
        }, 2000);
    }


    protected void doHttpGetVideoList() {
        VideoHttpService.doHttpGetVideoList(getActivity(), new OkHttpCallBack<AdVideoListResponse>() {
            @Override
            public void onSuccess(String url, AdVideoListResponse response) {
                LogCat.e("video", "新接口成功了........*******************.");
                if (!isAdded()) {
                    return;
                }
                onGetVideoListSuccess(response, url);
            }

            @Override
            public void onError(String url, String errorMsg) {
                LogCat.e("video", "新接口onError了.........********************");
                onGetVideoListFailed(errorMsg, url);

            }
        });

    }

    private void onGetVideoListSuccess(AdVideoListResponse response, String url) {
        if (!isAdded()) {
            return;
        }
        LogCat.e("video", "turl: " + url);
        if (response == null) {
            // 默认继续播放之前的缓存文件
            // 显示开发下载模式，主要是为了显示日志
            //rollPoling();
            return;
        }

        if (response.current == null || response.current.size() == 0) {
            // 默认继续播放之前的缓存文件
            // 显示开发下载模式，主要是为了显示日志
            //rollPoling();
            return;
        }




        // 创建排播列表，并添加数据
        orderVideoList = new ArrayList<>();
        orderVideoList.addAll(response.current);

        // 去除重复
        LogCat.e("video", "去除今日播放列表重复的视频.......");
        ArrayList<AdDetailResponse> currentVideoList = getDistinctList(response.current);

        // 更新今日数据表
        LogCat.e("video", "根据今日下载列表和今日删除列表，更新sql表.......");
        VideoAdUtils.updateSqlVideoList(getActivity(), true, currentVideoList);

        LogCat.e("video", "去除明日播放列表重复的视频.......");
        ArrayList<AdDetailResponse> nextVideoList = null;
        if (response.next != null) {
            nextVideoList = getDistinctList(response.next);
            // 更新sql表
            LogCat.e("video", "根据明日需要下载的视频列表和明日的删除列表，更新sql表......");
            VideoAdUtils.updateSqlVideoList(getActivity(), false, nextVideoList);
        }

        // 更新本地缓存视频列表，将新下载的视频添加入本地列表
        LogCat.e("video", "获取本地缓存视频列表.......");
        localVideoList = VideoAdUtils.getLocalVideoList(getActivity(), currentVideoList, nextVideoList);

        // 2.匹配今天要下载的视频
        LogCat.e("video", "根据今日播放列表，获取下载列表......");
        downloadLists = VideoAdUtils.getDownloadList(localVideoList, currentVideoList);
        LogCat.e("video", "------------------------------");

        // 3.匹配要删除的视频
        LogCat.e("video", "根据今日播放列表，获取删除列表......");
        deleteLists = VideoAdUtils.getDeleteList(localVideoList, currentVideoList);
        LogCat.e("video", "------------------------------");

        // 4.匹配要播放的视频
        LogCat.e("video", "根据今日播放列表，获取播放列表......");
        playVideoLists = VideoAdUtils.getTodayPlayVideoList(localVideoList, currentVideoList);
        LogCat.e("video", "------------------------------");



        if (response.next != null && response.next.size() != 0 && nextVideoList != null) {
            // 根据明日列表 来剔除在今日删除列表 中需要用到的视频
            LogCat.e("video", "根据明日列表来 剔除在 今日删除列表 中需要用到的视频.......");
            for (AdDetailResponse adDetailResponse : nextVideoList) {
                for (int i = deleteLists.size() - 1; i >= 0; i--) {
                    AdDetailResponse deleteVideo = deleteLists.get(i);
                    if (deleteVideo != null && !TextUtils.isEmpty(deleteVideo.adVideoName) && deleteVideo.adVideoName.equals(adDetailResponse.adVideoName)) {
                        // 当前视频需要使用，从删除列表删除
                        deleteLists.remove(i);
                        LogCat.e("video", "当前视频明日播放列表使用到，从删除列表删除------------------------------" + adDetailResponse.adVideoName);
                    }
                }
            }
            LogCat.e("video", "最终今日删除列表大小------------------------------" + deleteLists.size());
            // 5.匹配明天要下载的视频
            LogCat.e("video", "根据明日播放列表，获取下载列表......");
            prepareDownloadLists = VideoAdUtils.getDownloadList(localVideoList, nextVideoList);
            LogCat.e("video", "------------------------------");
            // 得到明日列表需要删除的文件
            ArrayList<AdDetailResponse> deleteListTomorrow = VideoAdUtils.getDeleteList(localVideoList, nextVideoList);




            LogCat.e("video", "根据今日列表来 剔除在 明日删除列表 中需要用到的视频.......");
            for (AdDetailResponse adDetailResponse : currentVideoList) {
                for (int i = deleteListTomorrow.size() - 1; i >= 0; i--) {
                    AdDetailResponse deleteVideo = deleteListTomorrow.get(i);
                    if (deleteVideo != null && !TextUtils.isEmpty(deleteVideo.adVideoName) && deleteVideo.adVideoName.equals(adDetailResponse.adVideoName)) {
                        // 当前视频需要使用，从删除列表删除
                        deleteListTomorrow.remove(i);
                        LogCat.e("video", "当前视频今日播放列表使用到，从删除列表删除------------------------------" + adDetailResponse.adVideoName);
                    }
                }
            }
            LogCat.e("video", "最终明日删除列表大小------------------------------" + deleteListTomorrow.size());

            LogCat.e("video", "删除今日删除表和明日删除表重复的视频，得到最终的删除列表......");
            deleteLists = VideoAdUtils.dealWithDeleteVideos(deleteLists, deleteListTomorrow);
            for (AdDetailResponse adDetailResponse : deleteLists) {
                LogCat.e("video", "删除视频......" + adDetailResponse.adVideoName);
            }

            // 匹对今天的下载列表，提出重复下载的视频
            LogCat.e("video", "开始处理重复的下载任务......");
            VideoAdUtils.reconnectedPrepare(downloadLists, prepareDownloadLists);
        }
        // 9.进行删除控制
        if (deleteLists.size() > 0) {
            LogCat.e("video", "开始执行删除操作......");
            if (playVideoLists.size() >= 2) {
                // 立即执行删除文件操作
                executeDeleteVideos();
            } else {
                // 否则就等待播放列表个数多余2个再进行删除
                LogCat.e("video", "等待播放列表个数多余2个再进行删除......");
            }
        }


        // 10.开始下载
        LogCat.e("video", "开始下载......");
        for (AdDetailResponse adDetailResponse : downloadLists) {
            LogCat.e("video", "需要下载的视频..." + adDetailResponse.adVideoName);
        }


        // 显示开发下载模式，主要是为了显示日志
        showLogMsg(0, 0);
        LogCat.e("video", "++++++++++++++++++++++++++++++++++++++++++++++++");
        prepareDownloading();

        //rollPoling();


//        throw new NullPointerException("错误日志上传测试..............");
    }


//    // 轮询接口
//    private void rollPoling() {
//        LogCat.e("video", "++++++++++++++++++++++++++++++++++++++++++该刷新接口了******.");
//        if(handler != null){
//            handler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    LogCat.e("video", "该刷新接口了******.");
//                    if (isAdded() && getActivity() != null) {
//                        doHttpGetVideoList();
//                    }
//
//                }
//            }, pollInterval);
//        }
//    }


    private void onGetVideoListFailed(String errorMsg, String url) {
        videoView.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isAdded()) {
                    return;
                }
                doHttpGetVideoList();
            }
        }, TIME_RETRY_DURATION);
    }


    @Override
    public void onDownloadFileSuccess(String filePath) {
        if (isDetached()) {
            return;
        }
        LogCat.e("video", "onFinish..../*/*/*/*/*/**/......... " + filePath);
        if (downloadingVideoResponse != null) {
            UmengUtils.onEvent(getActivity(), UmengUtils.UMENG_DOWNLOAD_FILE, downloadingVideoResponse.adVideoName);
            if (!isDownloadPrepare) { // 如果是下载预下载视频，则不会加入到播放列表
                // 把下载成功的视频添加到播放列表中
                LogCat.e("video", "将视频添加到播放列表.......");
                playVideoLists.add(downloadingVideoResponse);
            } else {
                LogCat.e("video", "此时是下载明日视频，无需添加到播放列表");
            }
            // 把当前下载的任务从播放列删除
            downloadLists.remove(downloadingVideoResponse);

            showLogMsg(0, 0);

            // 统计下载完成的视频
            UmengUtils.onEvent(getActivity(), UmengUtils.UMENG_DOWNLOAD_FILE, downloadingVideoResponse.adVideoName);

            //上传视频下载的时长
            long endDownloadTime = System.currentTimeMillis();
            LogCat.e("MainActivity", downloadingVideoResponse.adVideoName + " 完成下载的时间： " + endDownloadTime + " 毫秒");
            long time = endDownloadTime - startDownloadTime;
            LogCat.e("MainActivity", downloadingVideoResponse.adVideoName + " 下载的时长： " + time + " 毫秒");
            if (time > 0) {
                long second = time / 1000;
                sendVideoDownloadTime(getActivity(), downloadingVideoResponse.adVideoId, downloadingVideoResponse.adVideoName, second);
                LogCat.e("MainActivity", downloadingVideoResponse.adVideoName + " 下载的时长： " + second + " 秒");
            }

        }

        // 继续进行下载任务
        prepareDownloading();
        // 还原状态
        retryTimes = 0;

    }


    @Override
    public void onDownloadFileError(int errorCode, String errorMsg) {
        if(downloadingVideoResponse != null){
            //上报下载失败
            VideoAdUtils.sendVideoDownloadError(getActivity(),errorCode,errorMsg,downloadingVideoResponse);
        }

        if (errorCode == ErrorCodes.ERROR_DOWNLOAD_SDCARD_SPACE) { // 如果是空间不足的错误，就不在进行下载
            // TODO 上报情况
        } else {
            downVideoError();
        }
    }


    @Override
    public void onDownloadProgress(final long progress, final long fileLength) {
        showLogMsg(progress, fileLength);
        showNetSpeed(true, false, progress);
    }




    public void showNetSpeed(final boolean isHasNet, final boolean isDownloadingAPK, final long progress) {
        if(handler == null){
            handler = new MyHandler();
        }
        Message msg = handler.obtainMessage(4);
        Bundle bundle = new Bundle();
        bundle.putBoolean("isHasNet", isHasNet);
        bundle.putBoolean("isDownloadingAPK", isDownloadingAPK);
        bundle.putLong("progress", progress);
        msg.setData(bundle);
        handler.sendMessage(msg);
    }

    private void downVideoError() {
        if (isDetached()) {
            return;
        }
        if(handler == null){
            handler = new MyHandler();
        }
        // 出错就放弃当前下载任务，继续下载下一个任务，并将当前任务放到最后一个，如果已经是最后一个，再重试2边
        handler.sendEmptyMessageDelayed(3, TIME_RETRY_DURATION);
    }


    public void setLayoutResponse(LayoutResponse layoutResponse) {
        this.layoutResponse = layoutResponse;
    }


    /**
     * 开始截屏
     */
    private void startScreenShot() {
        screenShotService = Executors.newScheduledThreadPool(2);
        if (screenShotResponse != null) {
            if(screenShotResponse.screenShotInterval < 900000){
                delay = 900000;
            }else {
                delay = screenShotResponse.screenShotInterval;
            }
        } else {
            delay = 900000; // 15分钟
        }
//        if (Constants.isTest) {
//            delay = 1000 * 20;
//        }

        screenShotService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                LogCat.e("screenShot", "开始进行截图...........");
                if (getActivity() == null || isDetached() || playingVideoInfo == null) {
                    return;
                }


                long currentPosition = videoView.getCurrentPosition();

                ScreenShotUtils screenShotUtils = new ScreenShotUtils();
                screenShotUtils.screenShot(getActivity(), playingVideoInfo, currentPosition, screenShotResponse);

                // 下载设备网速
                if (!TextUtils.isEmpty(speed)) {
                    UmengUtils.onEvent(getActivity(), UmengUtils.UMENG_NET_SPEED, speed);
                }


                // 下载设备网速
                UmengUtils.onEvent(getActivity(), UmengUtils.UMENG_SCREEN_SHOT, playingVideoInfo.adVideoName + " 截图时间：" + VideoAdUtils.computeTime(currentPosition));
            }
        }, delay, delay, TimeUnit.MILLISECONDS);
    }

    private void startPlayVideo() {
        if (playVideoLists == null || playVideoLists.size() < 2) {
            // 如果播放列表视频数量不足2个，继续检查本地缓存列表
            if (cachePlayVideoLists == null || cachePlayVideoLists.size() < 2) {
                // 本地缓存列表数量也不足2个，播放预置片
                LogCat.e("video", "由于播放列表和缓存播放列表可以播放视频都不足2个，播放预置片.......");
                playingVideoInfo = getRawVideoInfo();
            } else {
                playingVideoInfo = findPlayingVideo(cachePlayVideoLists);
            }
        } else {
            // 进行排播
            playingVideoInfo = findPlayingOrderVideo();
        }
        playVideo(playingVideoInfo.videoPath);
    }

    // 查找当前要播放的排播视频
    private AdDetailResponse findPlayingOrderVideo() {
        AdDetailResponse playingVideoInfo = null;
        if (orderVideoList == null || orderVideoList.size() == 0) {
            // 此时没有排播列表，按下载视频进行播放
            playingVideoInfo = findPlayingVideo(playVideoLists);
        } else {
            int size = orderVideoList.size();
            // 处理播放位置
            if (playOrderPos >= size) {
                LogCat.e("order", "重置playOrderPos.......");
                playOrderPos = 0;
            }
            LogCat.e("order", "开始排播.......");
            boolean isHasFindOrderVideo = false;
            for (; playOrderPos < size; ) {
                boolean isPlayVideo = false;
                AdDetailResponse adDetailResponse = orderVideoList.get(playOrderPos);
                LogCat.e("order", "当前排播应该播放的视频......." + adDetailResponse.adVideoName);
                for (AdDetailResponse playVideo : playVideoLists) {
                    // 如果找到可以播放的视频，获取器播放地址
                    if (adDetailResponse.adVideoId == playVideo.adVideoId) {
                        isPlayVideo = true;
                        playingVideoInfo = playVideo;
                        LogCat.e("order", "当前视频可以按照排播列表播放......." + adDetailResponse.adVideoName);
                        break;
                    }
                }
                playOrderPos += 1;
                // 已经找到壳播视频，就退出循环
                if (isPlayVideo) {
                    isHasFindOrderVideo = true;
                    break;
                }
                LogCat.e("order", "找不到当前排播视频，继续便利下一个视频.......");
            }
            if (!isHasFindOrderVideo) {
                playingVideoInfo = getRawVideoInfo();
            }
            if (playingVideoInfo != null) {
                LogCat.e("order", "最终播放对视频的地址......." + playingVideoInfo.videoPath);
            }
        }
        return playingVideoInfo;
    }

    // 查找当前非排播要播放的视频
    private AdDetailResponse findPlayingVideo(ArrayList<AdDetailResponse> videoLists) {
        boolean isHasPlayingVideo = false;
        AdDetailResponse playingVideoInfo = null;
        for (AdDetailResponse adDetailResponse : videoLists) {
            if (!TextUtils.isEmpty(adDetailResponse.videoPath)) {
                playingVideoInfo = adDetailResponse;
                isHasPlayingVideo = true;
                LogCat.e("video", "播放缓存播放列表......." + adDetailResponse.adVideoId);
                break;
            }
        }

        if (!isHasPlayingVideo) {
            playingVideoInfo = getRawVideoInfo();
        }
        return playingVideoInfo;
    }


    private void executeDeleteVideos() {
        if (deleteLists != null && deleteLists.size() > 0) {
            //上报视频删除
            sendDeleteVideo(getActivity(), deleteLists);
            LogCat.e("video", "删除列表还有内容，则进行删除操作.......");
            // 如果要删除的文件正在播放，那就等待稍后再进行删除
            for (int i = 0; i < deleteLists.size(); i++) {
                AdDetailResponse adDetailResponse = deleteLists.get(i);
                if (playingVideoInfo != null && !TextUtils.isEmpty(playingVideoInfo.adVideoName) && playingVideoInfo.adVideoName.equals(adDetailResponse.adVideoName)) {
                    LogCat.e("video", "当前视频正在播放，暂不删除，稍后再说....." + adDetailResponse.adVideoName);
                } else {
                    DeleteFileUtils.getInstance().deleteFile(adDetailResponse.videoPath);
                    LogCat.e("video", "删除的文件....." + adDetailResponse.adVideoName);
                    --i;
                    deleteLists.remove(adDetailResponse);
                }
            }
        } else {
            LogCat.e("video", "无需删除文件......");
        }
    }



    /**
     * 处理视频播放错
     */
    private void fixVideoError() {
        if (playVideoLists == null || playVideoLists.size() < 2) {
            LogCat.e("video", "当前视频在本地缓存列表中......");
            if (localVideoList != null && localVideoList.size() < 2) {
                AdDetailResponse adDetailResponse = searchPlayVideo(playVideoIndex, localVideoList);
                if (adDetailResponse != null && !Constants.PRESET_PIECE.equals(adDetailResponse.adVideoName)) {
                    localVideoList.remove(adDetailResponse);
                    LogCat.e("video", "不是预置片，则从本地缓存列表中删除，并删除文件......" + adDetailResponse.adVideoName);
                    // 如果是缓存文件出错，直接删除当前文件，并播放下一个
                    DeleteFileUtils.getInstance().deleteFile(adDetailResponse.videoPath);
                } else {
                    // TODO 上报预置片出错
                    LogCat.e("video", "预置片播放出错......");
                }
            }

        } else {
            int size = playVideoLists.size();
            if (playVideoLists != null && size > 0 && playVideoIndex < size) {
                AdDetailResponse adDetailResponse = searchPlayVideo(playVideoIndex, playVideoLists);
                if (adDetailResponse != null) {
                    LogCat.e("video", "当前视频在播放列表中......" + adDetailResponse.adVideoName);
                    // 从播放列表将当前视频删除
                    playVideoLists.remove(adDetailResponse);
                    // 删除当前的视频文件
                    DeleteFileUtils.getInstance().deleteFile(adDetailResponse.videoPath);
                    LogCat.e("video", "从播放列表中移除，并删除文件......");
                    // 添加重新下载当前文件
                    if (downloadingVideoResponse != null) {
                        // 仍有任务没有下载完成，将当前任务添加到最后一个
                        downloadLists.add(adDetailResponse);
                        if (downloadLists.size() <= 1) { // 此时表示已经没有下载任务，需要主动开启下载，否则表示下载还在进行中
                            prepareDownloading();
                            LogCat.e("video", "此时表示已经没有下载任务，需要主动开启下载，否则表示下载还在进行中......");
                        } else {
                            LogCat.e("video", "下载任务还没完成，将其加入到下载列表中......");
                        }
                    } else {
                        LogCat.e("video", "此时表示已经没有下载任务，需要主动开启下载，否则表示下载还在进行中......");
                        downloadLists = new ArrayList<AdDetailResponse>();
                        downloadLists.add(adDetailResponse);
                        prepareDownloading();
                    }
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        release();
        super.onDestroy();

    }

    /**
     * 释放资源
     */
    private void release() {
        DLUtils.init().cancel();

        if (screenShotService != null) {
            screenShotService.shutdownNow();
            screenShotService = null;
        }

        if(handler != null && videoStatusRunnable != null){
            handler.removeCallbacks(videoStatusRunnable);
        }

        RetrofitDLUtils.getInstance().cancel();

    }



    /**
     * 下载准备工作
     */
    private void prepareDownloading() {
        if (downloadLists.size() == 0) {
            if (prepareDownloadLists.size() == 0) {
                LogCat.e("video", "所有视频下载完成。。。。。。。。");
                downloadingVideoResponse = null;
                isDownloadPrepare = false;
                // 显示下载完成信息
                cachePlayVideoLists.clear();
                // 更新日志
                if(handler == null){
                    handler = new MyHandler();
                }
                handler.sendEmptyMessage(2);

            } else {
                LogCat.e("video", "当日的下载列表下载完成，继续下载明日与播放的视频列表");
                if (downloadLists != null) {
                    downloadLists.addAll(prepareDownloadLists);
                }
                if (prepareDownloadLists != null) {
                    prepareDownloadLists.clear();
                }
                isDownloadPrepare = true;
                prepareDownloading();
            }

        } else {
            if(downloadLists == null){
                // 此时说明出错,继续做http请求
                doHttpGetVideoList();
                return;
            }
            // 如果要下载的文件不是第一个,强制放到第一个下载位置
            changeHasDlInfoPosition();


            LogCat.e("video", "获取下载列表第一个视频，并开始下载。。。。。。。。 还剩余下载任务：" + downloadLists.size());
            downloadingVideoResponse = downloadLists.get(0);

            // 当获取到的对象为null 提出当前的下载,继续下载后面的
            if(downloadingVideoResponse == null){
                downloadLists.remove(0);
                prepareDownloading();
                return;
            }
            // 添加下载视频的文件路径
            downloadingVideoResponse.videoPath = DataUtils.getVideoDirectory() + downloadingVideoResponse.adVideoId + Constants.FILE_DOWNLOAD_EXTENSION;
            LogCat.e("video", "修改数据库视频地址信息........" + downloadingVideoResponse.adVideoId);
            // 保存下载文件的本地地址
            VideoAdUtils.updateVideoPath(!isDownloadPrepare, getActivity(), downloadingVideoResponse.adVideoId, downloadingVideoResponse.videoPath);
            // 开始获取文件地址
            if (TextUtils.isEmpty(downloadingVideoResponse.adVideoUrl)) {
                LogCat.e("video", "视频的playInfo数据出错，放弃当前视频，进行下一个.......");
                downloadLists.remove(0);
                prepareDownloading();
            } else {
                retryTimes = 0;
                videoUrl = downloadingVideoResponse.adVideoUrl;
                LogCat.e("video", "获取到当前视频的下载地址。。。。。。。。" + videoUrl);

                //记录第一次下载视频的时间
                startDownloadTime = System.currentTimeMillis();
                LogCat.e("MainActivity", downloadingVideoResponse.adVideoName + " 开始下载的时间： " + startDownloadTime + " 毫秒");
                startDownloading(videoUrl);
            }

        }
    }

    // 修改已经下载的文件在下载列表中的位置,强制放到第一个位置
    private void changeHasDlInfoPosition() {
        ArrayList<DownloadInfo> downloadInfos = DLDao.queryAll(getActivity());
        if(downloadInfos != null && downloadInfos.size() > 0){
            int size = downloadLists.size();
            for (int i = 0; i < size; i++) {
                AdDetailResponse adDetailResponse = downloadLists.get(i);
                if (DLDao.queryByName(getActivity(), adDetailResponse.adVideoName)) {
                    if(i != 0){
                        downloadLists.remove(i);
                        downloadLists.add(0, adDetailResponse);
                    }
                    break;
                }
            }
        }
    }


    private void showLogMsg(final long progress, final long fileLength) {
        if(handler == null){
            handler = new MyHandler();
        }
        Message msg = handler.obtainMessage(1);
        Bundle bundle = new Bundle();
        bundle.putLong("progress", progress);
        bundle.putLong("fileLength", fileLength);
        msg.setData(bundle);
        handler.sendMessage(msg);

    }

    private void startDownloading(String url) {
        if(downloadingVideoResponse == null){
            downloadLists.remove(0);
            prepareDownloading();
            return;
        }
        // 一个视频一个视频的下载
        int vid = downloadingVideoResponse.adVideoId;
        DownloadUtils.download(!isDownloadPrepare, getActivity(), DataUtils.getVideoDirectory(), vid + Constants.FILE_DOWNLOAD_EXTENSION, url, this);
    }


    public void intLayoutParams(LayoutResponse layoutResponse) {
        if (layoutResponse == null) {
            layoutResponse = new LayoutResponse();
        }
        layoutResponse.setAdOneLayout(rootView, DataUtils.getDisplayMetricsWidth(getActivity()), DataUtils.getDisplayMetricsHeight(getActivity()));
    }

    private void playNext(boolean isVideoError) {
        // 此时说明还没有可以播放的视频，播放预置片或者缓存文件
        // 视频正常播放
        LogCat.e("video", "即将开始播放下一个视频.......");
        if (playVideoLists == null || playVideoLists.size() < 2) {
            LogCat.e("video", "播放列表数据不足2个.......");
            if (isVideoError) {
                LogCat.e("video", "由于当前视频播放出错，导致播放下一个.......");
                if (playVideoLists != null && playVideoLists.size() > 0) {
                    LogCat.e("video", "播放列表还有视频可以播放.......");
                    playNextVideo(true, playVideoLists);
                } else {
                    LogCat.e("video", "播放列表无数据了，播放预置片.......");
                    playVideo(getRawVideoInfo().videoPath);
                }
            } else {
                LogCat.e("video", "正常进行播放下一个.......");
                if (cachePlayVideoLists == null || cachePlayVideoLists.size() < 2) {
                    playVideo(getRawVideoInfo().videoPath);
                    LogCat.e("video", "缓存播放列表无数据了，播放预置片.......");
                } else {
                    LogCat.e("video", "缓存播放列表还有视频可以播放.......");
                    // 播放缓存列表文件
                    playNextVideo(false, cachePlayVideoLists);
                }
            }
        } else {
            LogCat.e("video", "播放列表数据多于2个.......");
            // 删除所有需要删除的缓存文件
            executeDeleteVideos();
            // 情况缓存文件列表，重置状态
            if (cachePlayVideoLists != null && cachePlayVideoLists.size() > 0) {
                LogCat.e("video", "清空缓存播放列表.......");
                cachePlayVideoLists.clear();
            }
            // 播放缓存列表文件
            playNextVideo(true, playVideoLists);

        }
    }


    private void playNextVideo(boolean isOrderPlay, ArrayList<AdDetailResponse> playVideoLists) {
        int length = playVideoLists.size();
        if (isOrderPlay) {
            LogCat.e("order", "开始排播.......");
            // 处理播放位置
            int orderSize = orderVideoList.size();
            if (playOrderPos >= orderSize) {
                LogCat.e("order", "重置playOrderPos.......");
                playOrderPos = 0;
            }

            if (playingVideoInfo != null) {
                LogCat.e("video", "playingVideoInfo --- 当前正在播放结束的是：" + playingVideoInfo.adVideoName);
                LogCat.e("MainActivity", "上传播放完成的视频：" + playingVideoInfo.adVideoName);
                // 添加友盟统计
                UmengUtils.onEvent(getActivity(), UmengUtils.UMENG_VIDEO_PLAY_TIMES, playingVideoInfo.adVideoName);
                //上传播放次数
                sendVideoPlayTimes(getActivity(), playingVideoInfo.adVideoId, playingVideoInfo.adVideoName);
            } else {
                LogCat.e("video", "playingVideoInfo == null ");
            }


            for (; playOrderPos < orderSize; ) {
                boolean isPlayVideo = false;
                LogCat.e("order", "playOrderPos......." + playOrderPos);
                AdDetailResponse adDetailResponse = orderVideoList.get(playOrderPos);
                LogCat.e("order", "当前排播应该播放的视频......." + adDetailResponse.adVideoName);
                for (AdDetailResponse playVideo : playVideoLists) {
                    // 如果找到可以播放的视频，获取器播放地址
                    if (adDetailResponse.adVideoId == playVideo.adVideoId) {
                        isPlayVideo = true;
                        playingVideoInfo = playVideo;
                        LogCat.e("order", "当前视频可以按照排播列表播放......." + adDetailResponse.adVideoName);
                        break;
                    }
                }
                playOrderPos += 1;
                // 已经找到壳播视频，就退出循环
                if (isPlayVideo) {
                    break;
                }
                LogCat.e("order", "找不到当前排播视频，继续便利下一个视频.......");
            }
            LogCat.e("order", "playOrderPos......." + playOrderPos);
            playVideo(playingVideoInfo.videoPath);

        } else {
            if (length > playVideoIndex) {
                LogCat.e("video", "-----------------------------");
                for (int i = 0; i < length; i++) {
                    AdDetailResponse adDetailResponse = playVideoLists.get(i);
                    LogCat.e("video", "播放列表：" + adDetailResponse.adVideoName);
                    LogCat.e("video", "播放地址：" + adDetailResponse.videoPath);
                }
                LogCat.e("video", "-----------------------------");

                AdDetailResponse adDetailResponse = searchPlayVideo(playVideoIndex, playVideoLists);
                LogCat.e("video", "当前正在播放结束的是：" + adDetailResponse.adVideoName);
                LogCat.e("MainActivity", "上传播放完成的视频：" + playingVideoInfo.adVideoName);
                // 添加友盟统计
                UmengUtils.onEvent(getActivity(), UmengUtils.UMENG_VIDEO_PLAY_TIMES, adDetailResponse.adVideoName);
                //上传播放次数
                sendVideoPlayTimes(getActivity(), adDetailResponse.adVideoId, adDetailResponse.adVideoName);

            }

            LogCat.e("video", "当前播放列表数量：" + length);
            playVideoIndex++;
            if (playVideoIndex >= length) {
                playVideoIndex = 0;
            }

            playingVideoInfo = playVideoLists.get(playVideoIndex);
            playVideo(playingVideoInfo.videoPath);
            LogCat.e("video", "即将播放视频。。。" + playingVideoInfo.adVideoName + "  " + playVideoIndex);

        }


    }

    /**
     * 查找当前播放index应该播放的视频
     */
    private AdDetailResponse searchPlayVideo(int playVideoIndex, ArrayList<AdDetailResponse> playVideoLists) {
        int size = playVideoLists.size();
        AdDetailResponse detailResponse = null;
        if (playVideoIndex < size) {
            detailResponse = playVideoLists.get(playVideoIndex);
            if (detailResponse == null) {
                searchPlayVideo(playVideoIndex + 1, playVideoLists);
            }
        }
        return detailResponse;
    }


    /**
     * 检测网络，如果链接正常则请求接口
     */
    public void httpRequest(boolean isShowLog) {
        LogCat.e("video", "正在检测是否联网。。。。。");
        if (isAdded()) {
            if (DataUtils.isNetworkConnected(getActivity())) {
                if (isShowLog) {
                    //接口请求完成前的下载信息
                    showBeforeRequestCompleted();
                }
                // 先去请求服务器，查看视频列表
                doHttpGetVideoList();
                LogCat.e("video", "已经联网。。。。。");
            } else {
                LogCat.e("video", "没有联网。。。。。");
            }
        }
    }


    /**
     * 播放预置片
     */
    private void playPresetVideo() {
        playVideo(getRawVideoUri());
    }


    /**
     * 播放本地视频
     *
     * @param videoPath
     */
    private void playVideo(String videoPath) {
        if (!TextUtils.isEmpty(videoPath) && videoView != null) {
            videoView.setVideoPath(videoPath);
//            videoPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/video/test.mp4";
//            videoView.setVideoPath(videoPath);
        }
    }

    /**
     * 获取预置片的地址
     *
     * @return
     */
    private String getRawVideoUri() {
        return DataUtils.getRawVideoUri(getActivity(), R.raw.video);
    }

    private AdDetailResponse getRawVideoInfo() {
        AdDetailResponse adDetailResponse = new AdDetailResponse();
        adDetailResponse.videoPath = DataUtils.getRawVideoUri(getActivity(), R.raw.video);
        adDetailResponse.adVideoName = Constants.PRESET_PIECE;
        return adDetailResponse;
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


    public void setIsDownloadAPK(boolean isDownloadAPK) {
        this.isDownloadAPK = isDownloadAPK;
    }

    /**
     * 开始下载视频
     */
    public void startDownloadVideo() {
        httpRequest(true);
    }

    public void setScreenShotResponse(ScreenShotResponse screenShotResponse) {
        this.screenShotResponse = screenShotResponse;
    }

    public void setPollInterval(long pollInterval) {
        this.pollInterval = pollInterval;
    }


    @Override
    public void onResume() {
        super.onResume();

        SharedPreference sharedPreference = SharedPreference.getSharedPreferenceUtils(getActivity());
        boolean isHasMac = sharedPreference.getDate(Constants.SHARE_KEY_UMENG, false);
        if (isHasMac) {
            LogCat.e("mac", "umeng可以使用。。。。。");
            MobclickAgent.onPageStart("AdOneFragment");
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        SharedPreference sharedPreference = SharedPreference.getSharedPreferenceUtils(getActivity());
        boolean isHasMac = sharedPreference.getDate(Constants.SHARE_KEY_UMENG, false);
        if (isHasMac) {
            LogCat.e("mac", "umeng可以使用。。。。。");
            MobclickAgent.onPageEnd("AdOneFragment");
        }

    }


    @Override
    public void doHttpRequest() {
        LogCat.e("net", "doHttpRequest..........");
        //接口请求完成前的下载信息
        showBeforeRequestCompleted();
        doHttpGetVideoList();
    }


    @Override
    public void removeFragment() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.remove(this);
        ft.commit();
    }

//    /**
//     * 上报开始时间
//     */
//    private void sendAPPStartTime() {
//        if (!TextUtils.isEmpty(DataUtils.getMacAddress(getActivity())) && DataUtils.isNetworkConnected(getActivity())) {
//            String msg = "{\"time\"" + ":}";
//            ErrorHttpServer.doStatisticsHttp(getActivity(), Constant.APP_START_TIME, msg, new OkHttpCallBack<ErrorResponse>() {
//                @Override
//                public void onSuccess(String turl, ErrorResponse response) {
//                    LogCat.e("MainActivity", "上传开机时间成功");
//                }
//
//                @Override
//                public void onError(String turl, String errorMsg) {
//                    LogCat.e("MainActivity", "上传开机时间失败");
//                }
//            });
//        }
//    }



    /**
     * 设置下载速度
     *
     * @param speed
     */
    private void setSpeedInfo(String speed) {
        if (getActivity() != null) {
            ((MainActivity) getActivity()).setSpeedInfo(speed);
        }

    }

    /**
     * 设置下载速度
     *
     * @param info
     */
    private void setDownloadInfo(String info) {
        if (getActivity() != null) {
            ((MainActivity) getActivity()).setDownloadInfo(info);
        }

    }

    /**
     * 当有网络，但是接口没请求完成，显示下载信息
     */
    public void showBeforeRequestCompleted() {
        if (!isAdded()) {
            return;
        }
        if(handler == null){
            handler = new MyHandler();
        }
        handler.sendEmptyMessage(0);

    }


    String speed;
    private class MyHandler extends Handler{

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(!isAdded() || getActivity() == null){
                return;
            }

            if(msg == null){
                return;
            }
            switch (msg.what){
                case 0:
                    setSpeedInfo("wifi-on:0kb/s");
                    setDownloadInfo(LogMsgUtils.getInstance().getVideoList(playVideoLists, cachePlayVideoLists));
                    break;
                case 1:
                    Bundle bundle = msg.getData();
                    if(bundle == null){
                        break;
                    }
                    long progress = bundle.getLong("progress");
                    long fileSize = bundle.getLong("fileLength");
                    // 显示log日志
                    if(Constants.isTest){
                        if (tvProgress.getVisibility() != View.VISIBLE) {
                            tvProgress.setVisibility(View.VISIBLE);
                            tvProgress.bringToFront();
                        }
                        tvProgress.setText(LogMsgUtils.getInstance().showTestMsg(playingVideoInfo, downloadingVideoResponse, progress, fileSize, playVideoLists));
                    }

                    // 显示下载信息
                    setDownloadInfo(LogMsgUtils.getInstance().showDownLoadMsg(downloadingVideoResponse, progress, fileSize, playVideoLists));
                    break;
                case 2:
                    LogCat.e("net", "所有的视频都下载完成了，显示下载完成信息--Completed");
                    setSpeedInfo("wifi-on:0kb/s");
                    setDownloadInfo(LogMsgUtils.getInstance().showDownloadMsgALLDownloadCompleted(playVideoLists));
                    // 再次请求下接口,做一次确认
//                    doHttpGetVideoList();
                    break;

                case 3: // 下载出错
                    if (isDetached()) {
                        return;
                    }
                    if (retryTimes < MAX_RETRY_TIMES) {
                        LogCat.e("video", "继续重试3次下载，此时是第" + (retryTimes + 1) + "次尝试。。。。");
                        retryTimes++;
                        try {
                            startDownloading(videoUrl);
                        }catch (Exception e){
                            e.printStackTrace();
                            if(handler == null){
                                handler = new MyHandler();
                            }
                            handler.sendEmptyMessageDelayed(3, TIME_RETRY_DURATION);
                        }
                    } else {
                        retryTimes = 0;
                        // 删除下载的记录
                        DownloadUtils.deleteErrorDl(getActivity());
                        // 删除下载的文件
                        DeleteFileUtils.getInstance().deleteFile(downloadingVideoResponse.videoPath);
                        // 准备下载下一个
                        LogCat.e("video", "将当前下载失败的视频放到最后一个，继续下载后续的视频。。。。");
                        downloadLists.add(downloadLists.size(), downloadingVideoResponse);
                        downloadLists.remove(0);
                        prepareDownloading();
                    }
                    break;
                case 4:
                    Bundle bundleNet = msg.getData();
                    if(bundleNet == null){
                        break;
                    }
                    speed = LogMsgUtils.getInstance().getNetSpeed(bundleNet.getBoolean("isHasNet"), bundleNet.getLong("progress"), bundleNet.getBoolean("isDownloadingAPK"));
                    setSpeedInfo(speed);
                    break;
                case 5:
                    Bundle bundleNoNet = msg.getData();
                    if(bundleNoNet == null){
                        break;
                    }
                    setDownloadInfo(LogMsgUtils.getInstance().getNoNetMsg(bundleNoNet.getBoolean("isStartUpNotNet"), playVideoLists, cachePlayVideoLists));
                    break;
            }
        }
    }
}
