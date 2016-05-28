package com.gochinatv.ad.ui.fragment;

import android.app.FragmentTransaction;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.download.DLUtils;
import com.download.ErrorCodes;
import com.gochinatv.ad.R;
import com.gochinatv.ad.base.BaseFragment;
import com.gochinatv.ad.interfaces.OnUpgradeStatusListener;
import com.gochinatv.ad.screenshot.ScreenShotUtils;
import com.gochinatv.ad.screenshot.SystemScreenShotPolicy;
import com.gochinatv.ad.thread.DeleteFileUtils;
import com.gochinatv.ad.tools.Constants;
import com.gochinatv.ad.tools.DataUtils;
import com.gochinatv.ad.tools.DownloadUtils;
import com.gochinatv.ad.tools.LogCat;
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
import com.umeng.analytics.MobclickAgent;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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

    private long startReportTime;


    /**
     * 进行重试的时间间隔
     */
    private final int TIME_RETRY_DURATION = 1000 * 20;
    /**
     * 测试的时间间隔
     */
    private final int TEST_TIME_DURATION = 1000 * 60;
    /**
     * 最多进行重试的次数
     */
    private final int MAX_RETRY_TIMES = 3;
    //布局参数
    private LayoutResponse layoutResponse;

    private TextView tvProgress;
    private TextView tvSpeed;

    private Handler handler;
    // 接口重复刷新时间
    private long pollInterval;

    private int playOrderPos;
    private ArrayList<AdDetailResponse> orderVideoList;

    private AdDetailResponse playingVideoInfo;


    @Override
    protected View initLayout(LayoutInflater inflater, ViewGroup container) {
        return getRelativeLayout(inflater, container);
    }


    @Override
    protected void initView(View rootView) {
        videoView = (MeasureVideoView) rootView.findViewById(R.id.videoView);
        loading = (LinearLayout) rootView.findViewById(R.id.loading);
        tvProgress = (TextView) rootView.findViewById(R.id.tv_progress);
        tvSpeed = (TextView) rootView.findViewById(R.id.tv_internet_speeds);
    }

    @Override
    protected void init() {
        // 显示loading加载状态
        showLoading();

        // 记录开始时间
        startReportTime = System.currentTimeMillis();
        // 先判断sdcard状态是否可用，如果不可用，直接播放本地视频
        if (!DataUtils.isExistSDCard()) {
            LogCat.e("video", "sd卡状态不可用......");
            playPresetVideo();
            return;
        }
        // 没有网络
        if (!DataUtils.isNetworkConnected(getActivity())) {
            showNetSpeed(false, false, 0);
        }

        // 1.初始化本地缓存表
        LogCat.e("video", "获取本地缓存视频列表.......");
        localVideoList = VideoAdUtils.getLocalVideoList(getActivity());

        LogCat.e("video", "------------------------------");
        if (localVideoList.size() != 0) {
            // 2.获取今日播放的缓存列表
            LogCat.e("video", "获取今日播放的缓存列表.......");
            ArrayList<AdDetailResponse> cacheTodayList = VideoAdUtils.getCacheList(getActivity(), true);
            for (AdDetailResponse adDetailResponse : cacheTodayList) {
                LogCat.e("video", "视频名称......." + adDetailResponse.adVideoName + ", " + adDetailResponse.adVideoLength + ", " + adDetailResponse.videoPath);
            }
            LogCat.e("video", "------------------------------");

            LogCat.e("video", "开始根据今日播放列表检测文件的完整性......." + localVideoList.size());
            ArrayList<AdDetailResponse> deleteVideosTD = VideoAdUtils.checkFileLength(getActivity(), localVideoList, cacheTodayList);


            LogCat.e("video", "获取缓存的明日播放列表.......");
            ArrayList<AdDetailResponse> cacheTomorrowList = VideoAdUtils.getCacheList(getActivity(), false);
            for (AdDetailResponse adDetailResponse : cacheTomorrowList) {
                LogCat.e("video", "视频名称......." + adDetailResponse.adVideoName + ", " + adDetailResponse.adVideoLength + ", " + adDetailResponse.videoPath);
            }
            LogCat.e("video", "------------------------------");
            // 3.取明日缓存列表
            // 4.根据今明两日的文件列表检查localVideoList列表文件的完整性
            if (cacheTomorrowList != null && cacheTomorrowList.size() != 0) {
                LogCat.e("video", "开始根据明日播放列表检测文件的完整性......." + localVideoList.size());
                // 处理deleteVideosTD的视频，检测看是否有视频需要使用
                for (AdDetailResponse adDetailResponse : cacheTomorrowList) {
                    for (int i = deleteVideosTD.size() - 1; i >= 0; i--) {
                        AdDetailResponse deleteVideo = deleteVideosTD.get(i);
                        if (deleteVideo.adVideoName.equals(adDetailResponse.adVideoName)) {
                            // 当前视频需要使用，从删除列表删除
                            deleteVideosTD.remove(i);
                            LogCat.e("video", "当前视频在明日播放列表使用到，从删除列表删除------------------------------" + adDetailResponse.adVideoName);
                        }
                    }
                }
                LogCat.e("video", "----------------最终deleteVideosTD的大小--------------" + deleteVideosTD.size());


                ArrayList<AdDetailResponse> deleteVideosTM = VideoAdUtils.checkFileLength(getActivity(), localVideoList, cacheTomorrowList);

                for (AdDetailResponse adDetailResponse : cacheTodayList) {
                    for (int i = deleteVideosTM.size() - 1; i >= 0; i--) {
                        AdDetailResponse deleteVideo = deleteVideosTM.get(i);
                        if (deleteVideo.adVideoName.equals(adDetailResponse.adVideoName)) {
                            // 当前视频需要使用，从删除列表删除
                            deleteVideosTM.remove(i);
                            LogCat.e("video", "当前视频在今日播放列表使用到，从删除列表删除------------------------------" + adDetailResponse.adVideoName);
                        }
                    }
                }
                LogCat.e("video", "----------------最终deleteVideosTM的大小--------------" + deleteVideosTM.size());
                ArrayList<AdDetailResponse> deleteVideos = VideoAdUtils.reconnectedPrepare(deleteVideosTD, deleteVideosTM);
                for (int i = deleteVideos.size() - 1; i >= 0; i--) {
                    AdDetailResponse delete = deleteVideos.get(i);
                    DeleteFileUtils.getInstance().deleteFile(delete.videoPath);
                    LogCat.e("video", "最终删除的缓存视频-----------------------" + delete.adVideoName);
                }
            }

            LogCat.e("video", "检测完后的视频列表个数......." + localVideoList.size());

            // 5.根据本地缓存视频列表和缓存明日列表，得出当前的播放列表
            LogCat.e("video", "根据本地缓存视频列表和缓存明日列表，得出当前的播放列表.....");
            cachePlayVideoLists = VideoAdUtils.getPlayVideoList(localVideoList, cacheTomorrowList);


            LogCat.e("video", "------------------------------");
        } else {
            cachePlayVideoLists = localVideoList;
        }

        // 4.播放缓存列表
        LogCat.e("video", "查找可以播放的视频.....");
        startPlayVideo();

        // 删除旧的文件目录
        VideoAdUtils.deleteOldDir();
//        VideoAdUtils.deleteScreenShotDir();
        LogCat.e("video", "请求接口.....");
        // 6.请求视频列表
        if (!isDownloadAPK) { // 当有下载任务的时候，就不会再去请求视频列表，全部资源给下载apk
            httpRequest();
        }


//        LogCat.e("video", "开始上传截屏文件.....");
        // 7.开启上传截图
//        startScreenShot();


        //  开启轮询接口
        handler = new Handler(Looper.getMainLooper());


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
                if (what != 1) {
                    return true;
                }
                LogCat.e("video", "视频播放出错......");
                videoView.stopPlayback();
                fixVideoError();
                LogCat.e("video", "开始播放下一个视频......");
                playNext(true);

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
        LogCat.e("video", "url: " + url);
        if (response == null) {
            // 默认继续播放之前的缓存文件
            // 显示开发下载模式，主要是为了显示日志
            rollPoling();
            return;
        }

        if (response == null || response.current == null || response.current.size() == 0) {
            // 默认继续播放之前的缓存文件
            // 显示开发下载模式，主要是为了显示日志
            rollPoling();
            return;
        }


        // 更新本地缓存视频列表，将新下载的视频添加入本地列表
        LogCat.e("video", "获取本地缓存视频列表.......");
        localVideoList = VideoAdUtils.getLocalVideoList(getActivity());

        // 创建排播列表，并添加数据
        orderVideoList = new ArrayList<>();
        orderVideoList.addAll(response.current);

        // 去除重复
        LogCat.e("video", "去除今日播放列表重复的视频.......");
        ArrayList<AdDetailResponse> currentVideoList = getDistinctVideoList(response.current);
        LogCat.e("video", "去除明日播放列表重复的视频.......");
        ArrayList<AdDetailResponse> nextVideoList = getDistinctVideoList(response.next);


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

        // 更新今日数据表
        LogCat.e("video", "根据今日下载列表和今日删除列表，更新sql表.......");
        VideoAdUtils.updateSqlVideoList(getActivity(), true, downloadLists, deleteLists);

        if (response.next != null && response.next.size() != 0) {
            // 5.匹配明天要下载的视频
            LogCat.e("video", "根据明日播放列表，获取下载列表......");
            prepareDownloadLists = VideoAdUtils.getDownloadList(localVideoList, nextVideoList);
            LogCat.e("video", "------------------------------");
            // 得到明日列表需要删除的文件
            ArrayList<AdDetailResponse> deleteListTomorrow = VideoAdUtils.getDeleteList(localVideoList, nextVideoList);

            // 更新sql表
            LogCat.e("video", "根据明日需要下载的视频列表和明日的删除列表，更新sql表......");
            VideoAdUtils.updateSqlVideoList(getActivity(), false, prepareDownloadLists, deleteListTomorrow);


            // 6.再次匹配要删除的视频列表,去除明日需要用到的视频，然后得到最终的删除列表
            LogCat.e("video", "再次匹配要删除的视频列表,去除明日需要用到的视频，然后得到最终的删除列表......");
            removeTomorrowVideos(nextVideoList);
            LogCat.e("video", "最终的删除列表......");
            for (AdDetailResponse adDetailResponse : deleteLists) {
                LogCat.e("video", "删除列表视频：" + adDetailResponse.adVideoName);
            }

            LogCat.e("video", "------------------------------");
            // 匹对今天的下载列表，提出重复下载的视频
            LogCat.e("video", "开始处理重复的下载任务......");
            VideoAdUtils.reconnectedPrepare(downloadLists, prepareDownloadLists);
            // 11.将明日播放列表缓存到本地
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

        prepareDownloading();

        // 显示开发下载模式，主要是为了显示日志
        showLogMsg(0, 0);
        LogCat.e("video", "++++++++++++++++++++++++++++++++++++++++++++++++");


        rollPoling();


//        throw new NullPointerException("错误日志上传测试..............");
    }

    // 轮询接口
    private void rollPoling() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isAdded() && getActivity() != null) {
                    doHttpGetVideoList();
                }

            }
        }, pollInterval);
    }


    private void onGetVideoListFailed(String errorMsg, String url) {
        videoView.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isDetached()) {
                    return;
                }
                doHttpGetVideoList();
            }
        }, TIME_RETRY_DURATION);
    }


    private ArrayList<AdDetailResponse> getDistinctVideoList(ArrayList<AdDetailResponse> videos) {
        ArrayList<AdDetailResponse> videoList = new ArrayList<>();
        for (AdDetailResponse adDetailResponse : videos) {
            int length = videoList.size();
            if (length == 0) {
                videoList.add(adDetailResponse);
            } else {
                boolean isHasVideo = false;
                for (int i = 0; i < length; i++) {
                    AdDetailResponse currentVideo = videoList.get(i);
                    if (currentVideo.adVideoName.equals(adDetailResponse.adVideoName)) {
                        isHasVideo = true;
                        break;
                    }
                }
                if (!isHasVideo) {
                    videoList.add(adDetailResponse);
                }
            }
        }
        return videoList;
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
        }


        // 继续进行下载任务
        prepareDownloading();
        // 还原状态
        retryTimes = 0;

    }


    @Override
    public void onDownloadFileError(int errorCode, String errorMsg) {
        if (errorCode == ErrorCodes.ERROR_DOWNLOAD_SDCARD_SPACE) { // 如果是空间不足的错误，就不在进行下载
            // TODO 上报情况
        } else {
            downVideoError();
        }
    }


    private long oldProgress;
    private final int K_SIZE = 1024 * 1024;

    @Override
    public void onDownloadProgress(final long progress, final long fileLength) {
        showLogMsg(progress, fileLength);
        showNetSpeed(true, false, progress);
    }

    public void hideNetSpeed() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvSpeed.setVisibility(View.GONE);
                }
            });
        }
    }

    public void showNetSpeed(final boolean isHasNet, final boolean isDownloadingAPK, final long progress) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (!isHasNet) {
                        tvSpeed.setText("wifi:off");
                        return;
                    }


                    if (oldProgress <= 0) {
                        oldProgress = progress;
                        return;
                    }

                    long current = progress - oldProgress;
                    String speed;
                    if (current >= 0 && current < 1024) {
                        speed = current + "B/s";
                    } else if (current >= 1024 && current < K_SIZE) {
                        speed = current / 1024 + "KB/s";
                    } else {
                        speed = current / K_SIZE + "MB/s";
                    }
                    LogCat.e("net_speed", "speed: " + speed);
                    if (current > 0) {
                        String msg = null;
                        if (isDownloadingAPK) {
                            msg = "wifi:on-" + speed + "-upgrading";
                        } else {
                            msg = "wifi:on-" + speed + "-downloading";
                        }
                        tvSpeed.setText(msg);
                        // 下载设备网速
                        UmengUtils.onEvent(getActivity(), UmengUtils.UMENG_NET_SPEED, speed);
                    }
                    oldProgress = progress;
                }
            });
        }
    }

    private void downVideoError() {
        if (isDetached()) {
            return;
        }
        // 出错就放弃当前下载任务，继续下载下一个任务，并将当前任务放到最后一个，如果已经是最后一个，再重试2边
        final int size = downloadLists.size();
        videoView.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isDetached()) {
                    return;
                }
                if (retryTimes < MAX_RETRY_TIMES) {
                    LogCat.e("video", "继续重试3次下载，此时是第" + (retryTimes + 1) + "次尝试。。。。");
                    download(videoUrl);
                    retryTimes++;
                } else {
                    retryTimes = 0;
                    if (size != 1) { // 如果不是最后一个视频，就继续下载下一个，如果是就放弃下载
                        LogCat.e("video", "将当前下载失败的视频放到最后一个，继续下载后续的视频。。。。");
                        downloadLists.add(size, downloadingVideoResponse);
                        downloadLists.remove(0);

                        // 删除下载的记录
                        DownloadUtils.deleteErrorDl(getActivity());
                        // 删除下载的文件
                        DeleteFileUtils.getInstance().deleteFile(downloadingVideoResponse.videoPath);
                        // 准备下载下一个
                        prepareDownloading();
                    } else {
                        download(videoUrl);
                    }

                }
            }
        }, TIME_RETRY_DURATION);

    }


    public void setLayoutResponse(LayoutResponse layoutResponse) {
        this.layoutResponse = layoutResponse;
    }


    /**
     * 开始截屏
     */
    private int mId = 0;

    private void startScreenShot() {
        screenShotService = Executors.newScheduledThreadPool(2);
        if (screenShotResponse != null) {
            delay = screenShotResponse.screenShotInterval;
        } else {
            delay = 1000 * 60 * 15; // 15分钟
        }
        if (Constants.isTest) {
            delay = 1000 * 60;
        }

        screenShotService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                LogCat.e("screenShot", "开始进行截图...........");
                if (getActivity() == null || isDetached()) {
                    return;
                }

                AdDetailResponse videoAdBean = getPlayingVideoInfo();
                if (videoAdBean == null) {
                    return;
                }
                long currentPosition = videoView.getCurrentPosition();

                ScreenShotUtils screenShotUtils = new ScreenShotUtils();
//                screenShotUtils.setScreenShotPolicy(new MediaMetadataPolicy());
//                screenShotUtils.setScreenShotPolicy(new JcodecPolicy());
//                screenShotUtils.setScreenShotPolicy(new TexturePolicy(videoView));
                screenShotUtils.setScreenShotPolicy(new SystemScreenShotPolicy());
                screenShotUtils.screenShot(getActivity(), videoAdBean.videoPath, currentPosition, screenShotResponse);

                // 下载设备网速
                UmengUtils.onEvent(getActivity(), UmengUtils.UMENG_SCREEN_SHOT, videoAdBean.adVideoName + " 截图时间：" + VideoAdUtils.computeTime(currentPosition));
//                LogCat.e("screenShot", "开始进行截图...........");
//                if (getActivity() == null || isDetached()) {
//                    return;
//                }
//                AdDetailResponse videoAdBean = getPlayingVideoInfo();
//                long currentPosition = videoView.getCurrentPosition();
//
//                Bundle bundle = new Bundle();
//                try {
//                    bundle.putString("uri", URLDecoder.decode(videoAdBean.videoPath, "UTF-8"));
//                    bundle.putLong("currentPosition", currentPosition);
//                    int width = 0;
//                    int height = 0;
//                    if (screenShotResponse == null) {
//                        // 获取状况栏高度
//                        width = getActivity().getWindowManager().getDefaultDisplay().getWidth();
//                        height = getActivity().getWindowManager().getDefaultDisplay().getHeight();
//                    } else {
//                        width = screenShotResponse.screenShotImgW;
//                        height = screenShotResponse.screenShotImgH;
//                    }
//                    bundle.putInt("width", width);
//                    bundle.putInt("height", height);
//                    bundle.putString("adName", videoAdBean.adVideoName);
//                    AdOneFragment.this.getLoaderManager().initLoader(mId, bundle, AdOneFragment.this);
//                    mId++;
//
//                } catch (UnsupportedEncodingException e) {
//                    e.printStackTrace();
//                }

            }
        }, delay, delay, TimeUnit.MILLISECONDS);
    }

    private void startPlayVideo() {
        String playingVideoPath = null;
        if (playVideoLists == null || playVideoLists.size() < 2) {
            // 如果播放列表视频数量不足2个，继续检查本地缓存列表
            if (cachePlayVideoLists == null || cachePlayVideoLists.size() < 2) {
                // 本地缓存列表数量也不足2个，播放预置片
                LogCat.e("video", "由于播放列表和缓存播放列表可以播放视频都不足2个，播放预置片.......");
                playingVideoInfo = getRawVideoInfo();
            } else {
                for (AdDetailResponse adDetailResponse : cachePlayVideoLists) {
                    if (!TextUtils.isEmpty(adDetailResponse.videoPath)) {
                        playingVideoInfo = adDetailResponse;
                        LogCat.e("video", "播放缓存播放列表......." + adDetailResponse.adVideoName);
                        break;
                    }
                }

            }
        } else {
            // 进行排播
            if (orderVideoList == null || orderVideoList.size() == 0) {
                // 此时没有排播列表，按下载视频进行播放
                for (AdDetailResponse adDetailResponse : playVideoLists) {
                    if (!TextUtils.isEmpty(adDetailResponse.videoPath)) {
                        playingVideoInfo = adDetailResponse;
                        LogCat.e("video", "播放播放列表......." + adDetailResponse.adVideoName);
                        break;
                    }
                }
            } else {
                int size = orderVideoList.size();
                // 处理播放位置
                if (playOrderPos >= size) {
                    LogCat.e("order", "重置playOrderPos.......");
                    playOrderPos = 0;
                }

                LogCat.e("order", "开始排播.......");
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
                        break;
                    }

                    LogCat.e("order", "找不到当前排播视频，继续便利下一个视频.......");

                }

                LogCat.e("order", "最终播放对视频的地址......." + playingVideoInfo.videoPath);
            }
        }
        playVideo(playingVideoInfo.videoPath);
    }


    private void removeTomorrowVideos(ArrayList<AdDetailResponse> tomorrowList) {
        // 获取明日需要用到的视频
        ArrayList<AdDetailResponse> preparePlayList = VideoAdUtils.getTodayPlayVideoList(localVideoList, tomorrowList);

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
                    LogCat.e("video", "在删除列表中明日需要用到的视频   " + deleteVideo.adVideoName);
                    break;
                }
            }
            if (isTomorrowUsed) {
                // 当前视频在在明日
                LogCat.e("video", "将当前视频从删除列表中剔除 ");
                deleteLists.remove(indexUsed);
            }

        }
    }


    private void executeDeleteVideos() {
        if (deleteLists != null && deleteLists.size() > 0) {
            LogCat.e("video", "删除列表还有内容，则进行删除操作.......");
            AdDetailResponse playingInfo = getPlayingVideoInfo();
            if (playingInfo == null) {
                return;
            }
            // 如果要删除的文件正在播放，那就等待稍后再进行删除
            for (int i = 0; i < deleteLists.size(); i++) {
                AdDetailResponse adDetailResponse = deleteLists.get(i);
                if (!TextUtils.isEmpty(playingInfo.adVideoName) && playingInfo.adVideoName.equals(adDetailResponse.adVideoName)) {
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
                if (!Constants.PRESET_PIECE.equals(adDetailResponse.adVideoName)) {
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
                    if (downloadLists.size() == 1) { // 此时表示已经没有下载任务，需要主动开启下载，否则表示下载还在进行中
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


    @Override
    public void onStop() {
        LogCat.e("video", "onStop...................");
        release();
        super.onStop();

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

        DLUtils.cancel();

        if (screenShotService != null) {
            screenShotService.shutdownNow();
            screenShotService = null;
        }

//        if (downloadingVideoResponse != null) {
//            DeleteFileUtils.getInstance().deleteFile(downloadingVideoResponse.videoPath);
//        }


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
                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvSpeed.setVisibility(View.GONE);
                        }
                    });
                }

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
            LogCat.e("video", "获取下载列表第一个视频，并开始下载。。。。。。。。 还剩余下载任务：" + downloadLists.size());
            downloadingVideoResponse = downloadLists.get(0);
            // 添加下载视频的文件路径
            String path = DataUtils.getVideoDirectory();
            downloadingVideoResponse.videoPath = path + downloadingVideoResponse.adVideoName + Constants.FILE_DOWNLOAD_EXTENSION;
            LogCat.e("video", "修改数据库视频地址信息........" + downloadingVideoResponse.adVideoId);
            // 保存下载文件的本地地址
            VideoAdUtils.updateVideoPath(!isDownloadPrepare, getActivity(), downloadingVideoResponse.adVideoName, downloadingVideoResponse.videoPath);
            // 开始获取文件地址
            if (TextUtils.isEmpty(downloadingVideoResponse.adVideoUrl)) {
                LogCat.e("video", "视频的playInfo数据出错，放弃当前视频，进行下一个.......");
                downloadLists.remove(0);
                prepareDownloading();
            } else {
                retryTimes = 0;
                videoUrl = downloadingVideoResponse.adVideoUrl;
                LogCat.e("video", "获取到当前视频的下载地址。。。。。。。。" + videoUrl);
                download(videoUrl);
            }

        }
    }

    private String logProgress(long progress, long fileLength) {
        if (fileLength == 0) {
            return "";
        }
        double size = (int) (progress / 1024);
        String sizeStr;
        int s = (int) (progress * 100 / fileLength);
        if (size > 1000) {
            size = (progress / 1024) / 1024f;
            BigDecimal b = new BigDecimal(size);
            double f1 = b.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
            sizeStr = String.valueOf(f1 + "MB，  ");
        } else {
            sizeStr = String.valueOf((int) size + "KB，  ");
        }
        return (sizeStr + s + "%");
    }

    private void showLogMsg(final long progress, final long fileLength) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (Constants.isTest && !isDetached()) {
                        // 当前下载视频
                        // 当前已下载视频的个数
                        StringBuilder logBuilder = new StringBuilder();
                        logBuilder.append("正在播放：" + getPlayingVideoInfo().adVideoName);
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
                            if (tvProgress.getVisibility() != View.VISIBLE) {
                                tvProgress.setVisibility(View.VISIBLE);
                                tvProgress.bringToFront();

                            }
                            tvProgress.setText(logBuilder.toString());
                        }

                    }
                }
            });
        }

    }

    private void download(String url) {
        // 一个视频一个视频的下载
        DownloadUtils.download(!isDownloadPrepare, getActivity(), DataUtils.getVideoDirectory(), downloadingVideoResponse.adVideoName + Constants.FILE_DOWNLOAD_EXTENSION, url, this);
    }

    private RelativeLayout getRelativeLayout(LayoutInflater inflater, ViewGroup container) {
        RelativeLayout layout = (RelativeLayout) inflater.inflate(R.layout.fragment_ad_video, container, false);

        String widthStr = null;
        String heightStr = null;
        String topStr = null;
        String leftStr = null;
        if (layoutResponse != null) {
            widthStr = TextUtils.isEmpty(layoutResponse.adWidth) ? "0.83125" : layoutResponse.adWidth;
            heightStr = TextUtils.isEmpty(layoutResponse.adHeight) ? "0.83125" : layoutResponse.adHeight;
            topStr = TextUtils.isEmpty(layoutResponse.adTop) ? "0.084375" : layoutResponse.adTop;
            leftStr = TextUtils.isEmpty(layoutResponse.adLeft) ? "0" : layoutResponse.adLeft;
        } else {
            //默认布局
            widthStr = "0.83125";
            heightStr = "0.83125";
            topStr = "0.084375";
            leftStr = "0";
        }
        //动态布局
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        double width = (float) (DataUtils.getDisplayMetricsWidth(getActivity()) * (Float.parseFloat(widthStr)));
        double height = (float) (DataUtils.getDisplayMetricsHeight(getActivity()) * (Float.parseFloat(heightStr)));
        double top = (float) (DataUtils.getDisplayMetricsHeight(getActivity()) * (Float.parseFloat(topStr)));
        double left = (float) (DataUtils.getDisplayMetricsWidth(getActivity()) * (Float.parseFloat(leftStr)));

        params.width = (int) Math.round(width);
        params.height = (int) Math.round(height);
        params.topMargin = (int) Math.round(top);
        params.leftMargin = (int) Math.round(left);
        layout.setLayoutParams(params);
        LogCat.e("video", " 广告一布局 width: " + params.width + " height: " + params.height + " top: " + params.topMargin + " left: " + params.leftMargin);
        return layout;
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
                    AdDetailResponse adDetailResponse = getRawVideoInfo();
                    if (adDetailResponse != null) {
                        playVideo(adDetailResponse.videoPath);
                    }

                }
            } else {
                LogCat.e("video", "正常进行播放下一个.......");
                if (cachePlayVideoLists == null || cachePlayVideoLists.size() < 2) {
                    AdDetailResponse adDetailResponse = getRawVideoInfo();
                    if (adDetailResponse != null) {
                        playVideo(adDetailResponse.videoPath);
                    }
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
            if (deleteLists != null && deleteLists.size() > 0) {
                LogCat.e("video", "删除列表还有内容，则进行删除操作.......");
                for (AdDetailResponse deleteResponse : deleteLists) {
                    LogCat.e("video", "删除文件......." + deleteResponse.adVideoName);
                    DeleteFileUtils.getInstance().deleteFile(deleteResponse.videoPath);
                }
                deleteLists.clear();
            }

            executeDeleteVideos();


            // 情况缓存文件列表，重置状态
            if (cachePlayVideoLists != null && cachePlayVideoLists.size() > 0) {
                LogCat.e("video", "清空缓存播放列表.......");
                cachePlayVideoLists.clear();
            }

//            AdDetailResponse videoAdBean = playVideoLists.get(playVideoIndex);
//            LogCat.e("video", "添加一次视频播放" + videoAdBean.adVideoName);
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
            String videoPath = null;
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
                // 添加友盟统计
                UmengUtils.onEvent(getActivity(), UmengUtils.UMENG_VIDEO_PLAY_TIMES, adDetailResponse.adVideoName);
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
        if (playVideoIndex <= size) {
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
    private void httpRequest() {
        LogCat.e("video", "正在检测是否联网。。。。。");
        if (isAdded()) {
            if (DataUtils.isNetworkConnected(getActivity())) {
                // 先去请求服务器，查看视频列表
                doHttpGetVideoList();
                LogCat.e("video", "已经联网。。。。。");
            } else {
                LogCat.e("video", "没有联网。。。。。继续检查");
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
        }
    }

    /**
     * 获取预置片的地址
     *
     * @return
     */
    private String getRawVideoUri() {
//            return "";
        return DataUtils.getRawVideoUri(getActivity(), R.raw.video_test);
    }

    private AdDetailResponse getRawVideoInfo() {
        AdDetailResponse adDetailResponse = new AdDetailResponse();
        adDetailResponse.videoPath = DataUtils.getRawVideoUri(getActivity(), R.raw.video_test);
        adDetailResponse.adVideoName = "预置片";
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


    /**
     * 获取正在播放的视频信息
     *
     * @return
     */
    private AdDetailResponse getPlayingVideoInfo() {
        return playingVideoInfo;
    }


    public void setIsDownloadAPK(boolean isDownloadAPK) {
        this.isDownloadAPK = isDownloadAPK;
    }

    /**
     * 开始下载视频
     */
    public void startDownloadVideo() {
        httpRequest();
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
        doHttpGetVideoList();
    }

    @Override
    public void removeFragment() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.remove(this);
        ft.commit();
    }
}
