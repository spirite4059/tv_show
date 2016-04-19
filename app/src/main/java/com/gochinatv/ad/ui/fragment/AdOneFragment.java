package com.gochinatv.ad.ui.fragment;

import android.media.MediaPlayer;
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
import com.gochinatv.ad.base.VideoHttpBaseFragment;
import com.gochinatv.ad.interfaces.OnUpgradeStatusListener;
import com.gochinatv.ad.thread.DeleteFileUtils;
import com.gochinatv.ad.tools.Constants;
import com.gochinatv.ad.tools.DataUtils;
import com.gochinatv.ad.tools.DownloadUtils;
import com.gochinatv.ad.tools.LogCat;
import com.gochinatv.ad.tools.ScreenShotUtils;
import com.gochinatv.ad.tools.VideoAdUtils;
import com.gochinatv.ad.video.MeasureVideoView;
import com.httputils.http.response.AdDetailResponse;
import com.httputils.http.response.PlayInfoResponse;
import com.httputils.http.response.VideoDetailListResponse;
import com.okhtttp.response.AdVideoListResponse;
import com.okhtttp.response.LayoutResponse;

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

    private Timer screenShotTimer;

    private boolean isDownloadPrepare;


    /**
     * 进行重试的时间间隔
     */
    private final int TIME_RETRY_DURATION = 1000 * 10;
    /**
     * 最多进行重试的次数
     */
    private final int MAX_RETRY_TIMES = 3;
    //布局参数
    private LayoutResponse layoutResponse;

    private TextView tvProgress;

    private boolean isTest = false;


    @Override
    protected View initLayout(LayoutInflater inflater, ViewGroup container) {
        return getRelativeLayout(inflater, container);
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
        // 记录开始时间
        VideoAdUtils.recordStartTime(getActivity());
        // 先判断sdcard状态是否可用，如果不可用，直接播放本地视频
        if (!DataUtils.isExistSDCard()) {
            LogCat.e("sd卡状态不可用......");
            playPresetVideo();
            return;
        }

        // 1.初始化本地缓存表
        LogCat.e("获取本地缓存视频列表.......");
        localVideoList = VideoAdUtils.getLocalVideoList();
        LogCat.e("------------------------------");
        if (localVideoList.size() != 0) {
            // 2.获取今日播放的缓存列表
            LogCat.e("获取今日播放的缓存列表.......");
            ArrayList<AdDetailResponse> cacheTodayList = VideoAdUtils.getCacheList(Constants.FILE_CACHE_TD_NAME);

            ArrayList<AdDetailResponse> cacheTomorrowList = null;
            if(isTest){
                LogCat.e("获取缓存的明日播放列表.......");
                cacheTomorrowList = VideoAdUtils.getCacheList(Constants.FILE_CACHE_NAME);
            }




            LogCat.e("开始根据今日播放列表检测文件的完整性......." + localVideoList.size());
            VideoAdUtils.checkFileLength(localVideoList, cacheTodayList);
            if(isTest){
                // 3.取明日缓存列表
                LogCat.e("------------------------------");
                // 4.根据今明两日的文件列表检查localVideoList列表文件的完整性
                if (cacheTomorrowList != null && cacheTomorrowList.size() != 0) {
                    LogCat.e("开始根据明日播放列表检测文件的完整性......." + localVideoList.size());

                    VideoAdUtils.checkFileLength(localVideoList, cacheTomorrowList);
                }
                LogCat.e("检测完后的视频列表个数......." + localVideoList.size());

                // 5.根据本地缓存视频列表和缓存明日列表，得出当前的播放列表
                LogCat.e("根据本地缓存视频列表和缓存明日列表，得出当前的播放列表.....");
                cachePlayVideoLists = VideoAdUtils.getPlayVideoList(localVideoList, cacheTomorrowList);
                LogCat.e("------------------------------");
            }else {
                cachePlayVideoLists = localVideoList;
            }



        } else {
            cachePlayVideoLists = localVideoList;
        }

        // 4.播放缓存列表
        LogCat.e("查找可以播放的视频.....");
        startPlayVideo();

        // 删除旧的文件目录
        VideoAdUtils.deleteOldDir();

        LogCat.e("请求接口.....");
        // 6.请求视频列表
        if (!isDownloadAPK) { // 当有下载任务的时候，就不会再去请求视频列表，全部资源给下载apk
            httpRequest();
        }


//        LogCat.e("开始上传截屏文件timer.....");
        // 7.开启上传截图
//        startScreenShot();


        //  开启轮询接口
//        handler = new Handler(Looper.getMainLooper());


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
                LogCat.e("video_onPrepared....");
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
                LogCat.e("视频播放出错......");
                videoView.stopPlayback();
                fixVideoError();
                LogCat.e("开始播放下一个视频......");
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
                LogCat.e("视频播放完成。。。。。。");
                // 播放下一个视频
                playNext(false);
            }
        });
    }


    @Override
    protected void onGetVideoListSuccessful(VideoDetailListResponse response, String url) {
        if (!isAdded()) {
            return;
        }

        if (response == null) {
            // 默认继续播放之前的缓存文件
            return;
        }
        ArrayList<AdDetailResponse> adDetailResponses = response.data;

        if (adDetailResponses == null || adDetailResponses.size() == 0) {
            // 默认继续播放之前的缓存文件
            return;
        }

        // TODO 以后要删除的
        for (AdDetailResponse adDetailResponse : response.data) {
            adDetailResponse.adVideoName = adDetailResponse.name;
        }

        // 将今日列表缓存到本地
        VideoAdUtils.cacheVideoList(Constants.FILE_CACHE_TD_NAME, adDetailResponses);

        // 2.匹配今天要下载的视频
        LogCat.e("根据今日播放列表，获取下载列表......");
        downloadLists = VideoAdUtils.getDownloadList(localVideoList, adDetailResponses);
        LogCat.e("------------------------------");


        // 3.匹配要删除的视频
        LogCat.e("根据今日播放列表，获取删除列表......");
        deleteLists = VideoAdUtils.getDeleteList(localVideoList, adDetailResponses);
        LogCat.e("------------------------------");

        // 4.匹配要播放的视频
        LogCat.e("根据今日播放列表，获取播放列表......");
        playVideoLists = VideoAdUtils.getTodayPlayVideoList(localVideoList, adDetailResponses);
        LogCat.e("------------------------------");

        // 5.匹配明天要下载的视频
//        LogCat.e("根据明日播放列表，获取下载列表......");
//        prepareDownloadLists = getDownloadList(localVideoList, adDetailResponses);

//        LogCat.e("------------------------------");
        // 匹对今天的下载列表，提出重复下载的视频
//        LogCat.e("开始处理重复的下载任务......");
//        reconnectedPrepare();

        // 6.再次匹配要删除的视频列表,去除明日需要用到的视频，然后得到最终的删除列表
//        LogCat.e("再次匹配要删除的视频列表,去除明日需要用到的视频，然后得到最终的删除列表......");
//        removeTomorrowVideos(localVideoList, adDetailResponses);
//        for (AdDetailResponse adDetailResponse : deleteLists) {
//            LogCat.e("删除列表视频：" + adDetailResponse.adVideoName);
//        }


        // 9.进行删除控制
        if (deleteLists.size() > 0) {
            LogCat.e("开始执行删除操作......");
            if (playVideoLists.size() >= 2) {
                // 立即执行删除文件操作
                executeDeleteVideos(false);
            } else {
                // 否则就等待播放列表个数多余2个再进行删除
                LogCat.e("等待播放列表个数多余2个再进行删除......");
            }
        }


        // 10.开始下载
        LogCat.e("开始下载......");
        for (AdDetailResponse adDetailResponse : downloadLists) {
            LogCat.e("需要下载的视频..." + adDetailResponse.adVideoName);
        }
        prepareDownloading();

        // 11.将明日播放列表缓存到本地
//        LogCat.e("将明日播放列表缓存到本地......");
//        cacheVideoList(Constants.FILE_CACHE_NAME, adDetailResponses);

        showLogMsg("");
        // 显示开发下载模式，主要是为了显示日志
        LogCat.e("++++++++++++++++++++++++++++++++++++++++++++++++");
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                if (isAdded()) {
//                    doHttpGetEpisode();
//                }
//
//            }
//        }, TIME_RETRY_DURATION);


    }


    @Override
    protected void onGetVideoListFailed(String errorMsg, String url) {
        videoView.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isDetached()) {
                    return;
                }
                doHttpGetEpisode();
            }
        }, TIME_RETRY_DURATION);
    }

    @Override
    protected void onGetVideoPathSuccessful(String url) {
        this.videoUrl = url;
        retryTimes = 0;
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
                    if (isDetached()) {
                        return;
                    }
                    // 移动当前正在下载的视频的index
                    downloadLists.add(size, downloadingVideoResponse);
                    downloadLists.remove(0);
                    prepareDownloading();
                }
            }, TIME_RETRY_DURATION);


        } else {
            LogCat.e("只剩最后一个视频。。。。");
            if (retryTimes < MAX_RETRY_TIMES) {
                retryTimes++;
                LogCat.e("第 " + retryTimes + " 次重新尝试获取下载地址");
                prepareDownloading();
            } else {
                retryTimes = 0;
                LogCat.e("经过尝试后，仍无法获取到当前视频的下载地址，放弃此次下载");
            }
        }
    }

    @Override
    protected void onGetVideoListSuccess(AdVideoListResponse response, String url) {
        if (!isAdded()) {
            return;
        }
        LogCat.e("url: " + url);
        if (response == null) {
            // 默认继续播放之前的缓存文件
            return;
        }

        if (response == null || response.current == null || response.current.size() == 0) {
            // 默认继续播放之前的缓存文件
            return;
        }

        // 去除重复
        LogCat.e("去除今日播放列表重复的视频.......");
        ArrayList<AdDetailResponse> currentVideoList = getDistinctVideoList(response.current);
        LogCat.e("去除明日播放列表重复的视频.......");
        ArrayList<AdDetailResponse> nextVideoList = getDistinctVideoList(response.next);


        // 将今日列表缓存到本地
        LogCat.e("将今日列表缓存到本地.......");
        VideoAdUtils.cacheVideoList(Constants.FILE_CACHE_TD_NAME, nextVideoList);
        // 2.匹配今天要下载的视频
        LogCat.e("根据今日播放列表，获取下载列表......");
        downloadLists = VideoAdUtils.getDownloadList(localVideoList, currentVideoList);
        LogCat.e("------------------------------");


        // 3.匹配要删除的视频
        LogCat.e("根据今日播放列表，获取删除列表......");
        deleteLists = VideoAdUtils.getDeleteList(localVideoList, currentVideoList);
        LogCat.e("------------------------------");

        // 4.匹配要播放的视频
        LogCat.e("根据今日播放列表，获取播放列表......");
        playVideoLists = VideoAdUtils.getTodayPlayVideoList(localVideoList, currentVideoList);
        LogCat.e("------------------------------");

        if (response.next != null && response.next.size() != 0) {
            // 11.将明日播放列表缓存到本地
            LogCat.e("将明日播放列表缓存到本地......");
            VideoAdUtils.cacheVideoList(Constants.FILE_CACHE_NAME, nextVideoList);
            // 5.匹配明天要下载的视频
            LogCat.e("根据明日播放列表，获取下载列表......");
            prepareDownloadLists = VideoAdUtils.getDownloadList(localVideoList, nextVideoList);
            LogCat.e("------------------------------");
            // 匹对今天的下载列表，提出重复下载的视频
            LogCat.e("开始处理重复的下载任务......");
            VideoAdUtils.reconnectedPrepare(downloadLists, prepareDownloadLists);
            // 6.再次匹配要删除的视频列表,去除明日需要用到的视频，然后得到最终的删除列表
            LogCat.e("再次匹配要删除的视频列表,去除明日需要用到的视频，然后得到最终的删除列表......");
            removeTomorrowVideos(deleteLists, nextVideoList);
            LogCat.e("最终的删除列表......");
            for (AdDetailResponse adDetailResponse : deleteLists) {
                LogCat.e("删除列表视频：" + adDetailResponse.adVideoName);
            }
            LogCat.e("------------------------------");
        }


        // 9.进行删除控制
        if (deleteLists.size() > 0) {
            LogCat.e("开始执行删除操作......");
            if (playVideoLists.size() >= 2) {
                // 立即执行删除文件操作
                executeDeleteVideos(false);
            } else {
                // 否则就等待播放列表个数多余2个再进行删除
                LogCat.e("等待播放列表个数多余2个再进行删除......");
            }
        }


        // 10.开始下载
        LogCat.e("开始下载......");
        for (AdDetailResponse adDetailResponse : downloadLists) {
            LogCat.e("需要下载的视频..." + adDetailResponse.adVideoName);
        }

        prepareDownloading();

        // 显示开发下载模式，主要是为了显示日志
        showLogMsg("");
        LogCat.e("++++++++++++++++++++++++++++++++++++++++++++++++");
    }

    private ArrayList<AdDetailResponse> getDistinctVideoList(ArrayList<AdDetailResponse> videos) {
        ArrayList<AdDetailResponse> videoList = new ArrayList<>();
        for(AdDetailResponse adDetailResponse : videos){
            int length = videoList.size();
            if(length == 0){
                videoList.add(adDetailResponse);
            }else {
                boolean isHasVideo = false;
                for(int i = 0; i < length; i++){
                    AdDetailResponse currentVideo = videoList.get(i);
                    if(currentVideo.adVideoName.equals(adDetailResponse.adVideoName)){
                        isHasVideo = true;
                        break;
                    }
                }
                if(!isHasVideo){
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
        LogCat.e("onFinish..../*/*/*/*/*/**/......... " + filePath);

        if (!isDownloadPrepare) { // 如果是下载预下载视频，则不会加入到播放列表
            // 把下载成功的视频添加到播放列表中
            LogCat.e("将视频添加到播放列表.......");
            playVideoLists.add(downloadingVideoResponse);
        } else {
            LogCat.e("此时是下载明日视频，无需添加到播放列表");
        }

        // 把当前下载的任务从播放列删除
        downloadLists.remove(downloadingVideoResponse);
        // 继续进行下载任务
        prepareDownloading();
        // 还原状态
        retryTimes = 0;


        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showLogMsg("");
                }
            });
        }

    }


    @Override
    public void onDownloadFileError(int errorCode, String errorMsg) {
        if (errorCode == ErrorCodes.ERROR_DOWNLOAD_SDCARD_SPACE) { // 如果是空间不足的错误，就不在进行下载
            // TODO 上报情况
        } else {
            downVideoError();
        }
    }

    @Override
    public void onDownloadProgress(final String progress) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showLogMsg(progress);
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
                    LogCat.e("继续重试3次下载，此时是第" + (retryTimes + 1) + "次尝试。。。。");
                    download(videoUrl);
                    retryTimes++;
                } else {
                    retryTimes = 0;
                    if (size != 1) { // 如果不是最后一个视频，就继续下载下一个，如果是就放弃下载
                        retryTimes = 0;
                        LogCat.e("将当前下载失败的视频放到最后一个，继续下载后续的视频。。。。");
                        downloadLists.add(size, downloadingVideoResponse);
                        downloadLists.remove(0);
                        prepareDownloading();
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
        String playingVideoName = null;
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


    private void removeTomorrowVideos(ArrayList<AdDetailResponse> localVideoList, ArrayList<AdDetailResponse> tomorrowList) {
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
                    LogCat.e("在删除列表中明日需要用到的视频   " + deleteVideo.adVideoName);
                    break;
                }
            }
            if (isTomorrowUsed) {
                // 当前视频在在明日
                LogCat.e("将当前视频从删除列表中剔除 ");
                deleteLists.remove(indexUsed);
            }

        }
    }


    private void executeDeleteVideos(boolean isAllFile) {
        if (deleteLists != null && deleteLists.size() > 0) {
            AdDetailResponse playingInfo = getPlayingVideoInfo();
            AdDetailResponse playingVideo = null;
            for (AdDetailResponse adDetailResponse : deleteLists) {

                if (!isAllFile && !TextUtils.isEmpty(playingInfo.adVideoName) && playingInfo.adVideoName.equals(adDetailResponse.adVideoName)) {
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

    /**
     * 处理视频播放错
     */
    private void fixVideoError() {
        if (playVideoLists == null || playVideoLists.size() < 2) {
            LogCat.e("当前视频在本地缓存列表中......");
            int size = localVideoList.size();
            if (localVideoList != null && size > 0 && playVideoIndex < size) {
                AdDetailResponse adDetailResponse = searchPlayVideo(playVideoIndex, localVideoList);
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
                AdDetailResponse adDetailResponse = searchPlayVideo(playVideoIndex, playVideoLists);
                LogCat.e("当前视频在播放列表中......" + adDetailResponse.adVideoName);
                // 从播放列表将当前视频删除
                playVideoLists.remove(adDetailResponse);
                // 删除当前的视频文件
                DeleteFileUtils.getInstance().deleteFile(adDetailResponse.videoPath);
                LogCat.e("从播放列表中移除，并删除文件......");
                // 添加重新下载当前文件
                if (downloadingVideoResponse != null) {

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
    }


    @Override
    public void onStop() {
        LogCat.e("onStop...................");
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
        if (httpTimer != null) {
            httpTimer.cancel();
            httpTimer = null;
        }

        DLUtils.init().cancel();

        ScreenShotUtils.shutdown();


        if (screenShotTimer != null) {
            screenShotTimer.cancel();
        }

        if (downloadingVideoResponse != null) {
            DeleteFileUtils.getInstance().deleteFile(downloadingVideoResponse.videoPath);
        }
    }

    /**
     * 下载准备工作
     */
    private void prepareDownloading() {
        if (downloadLists.size() == 0) {
            if(isTest){
                if (prepareDownloadLists.size() == 0) {
                    LogCat.e("所有视频下载完成。。。。。。。。");
                    downloadingVideoResponse = null;
                    isDownloadPrepare = false;
                } else {
                    LogCat.e("当日的下载列表下载完成，继续下载明日与播放的视频列表");
                    downloadLists.addAll(prepareDownloadLists);
                    prepareDownloadLists.clear();
                    isDownloadPrepare = true;
                    prepareDownloading();
                }
            }else {
                LogCat.e("所有视频下载完成。。。。。。。。");
                downloadingVideoResponse = null;
            }
        } else {
            LogCat.e("获取下载列表第一个视频，并开始下载。。。。。。。。 还剩余下载任务：" + downloadLists.size());
            downloadingVideoResponse = downloadLists.get(0);
            // 添加下载视频的文件路径
            String path = DataUtils.getSdCardFileDirectory() + Constants.FILE_DIRECTORY_VIDEO;
            downloadingVideoResponse.videoPath = path + downloadingVideoResponse.adVideoName + Constants.FILE_DOWNLOAD_EXTENSION;
            // 开始获取文件地址
            if(isTest){
                doHttpGetCdnPath(downloadingVideoResponse.adVideoUrl);
            }else {
                if (downloadingVideoResponse.playInfo != null && downloadingVideoResponse.playInfo.size() != 0) {
                    PlayInfoResponse playInfoResponse = downloadingVideoResponse.playInfo.get(0);
                    doHttpGetCdnPath(playInfoResponse.remotevid);
                } else {
                    LogCat.e("视频的playInfo数据出错，放弃当前视频，进行下一个.......");
                    downloadLists.remove(0);
                    prepareDownloading();
                }
            }




        }
    }

    private void showLogMsg(String progress) {
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
            logBuilder.append(progress);
            logBuilder.append('\n');

            logBuilder.append("已下载视频列表：" + '\n');
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


    private void download(String url) {
        // 一个视频一个视频的下载
        DownloadUtils.download(Constants.FILE_DIRECTORY_VIDEO, downloadingVideoResponse.adVideoName + Constants.FILE_DOWNLOAD_EXTENSION, url, this);
    }

    private RelativeLayout getRelativeLayout(LayoutInflater inflater, ViewGroup container) {
        RelativeLayout layout = (RelativeLayout) inflater.inflate(R.layout.fragment_ad_video, container, false);

        if (layoutResponse != null) {

            if (!TextUtils.isEmpty(layoutResponse.adWidth) && !TextUtils.isEmpty(layoutResponse.adHeight)
                    && !TextUtils.isEmpty(layoutResponse.adTop) && !TextUtils.isEmpty(layoutResponse.adLeft)) {

                String widthStr = layoutResponse.adWidth;
                String heightStr = layoutResponse.adHeight;
                String topStr = layoutResponse.adTop;
                String leftStr = layoutResponse.adLeft;

                //动态布局
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                double width = (float) (DataUtils.getDisplayMetricsWidth(getActivity()) * (Float.parseFloat(widthStr)));
                double height = (float) (DataUtils.getDisplayMetricsHeight(getActivity()) * (Float.parseFloat(heightStr)));
                double top = (float) (DataUtils.getDisplayMetricsHeight(getActivity()) * (Float.parseFloat(topStr)));
                double left = (float) (DataUtils.getDisplayMetricsWidth(getActivity()) * (Float.parseFloat(leftStr)));

                params.width = (int) Math.floor(width);
                params.height = (int) Math.floor(height);
                params.topMargin = (int) Math.floor(top);

                params.leftMargin = (int) Math.floor(left);
                layout.setLayoutParams(params);
                LogCat.e(" 广告二布局 width: " + params.width + " height: " + params.height + " top: " + params.topMargin + " left: " + params.leftMargin);

            }
        }


        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        layout.setLayoutParams(params);
        return layout;
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
                    LogCat.e("缓存播放列表无数据了，播放预置片.......");

                } else {
                    LogCat.e("缓存播放列表还有视频可以播放.......");
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

            executeDeleteVideos(true);


            // 情况缓存文件列表，重置状态
            if (cachePlayVideoLists != null && cachePlayVideoLists.size() > 0) {
                LogCat.e("清空缓存播放列表.......");
                cachePlayVideoLists.clear();
            }

//            AdDetailResponse videoAdBean = playVideoLists.get(playVideoIndex);
//            LogCat.e("添加一次视频播放" + videoAdBean.adVideoName);
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

            AdDetailResponse adDetailResponse = searchPlayVideo(playVideoIndex, playVideoLists);
            LogCat.e("当前正在播放结束的是：" + adDetailResponse.adVideoName);
        }

        LogCat.e("当前播放列表数量：" + length);
        playVideoIndex++;
        if (playVideoIndex >= length) {
            playVideoIndex = 0;
        }
        AdDetailResponse adDetailResponse = playVideoLists.get(playVideoIndex);
        playVideo(adDetailResponse.videoPath);
        LogCat.e("即将播放视频。。。" + adDetailResponse.adVideoName + "  " + playVideoIndex);
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
        httpTimer = new Timer();
        httpTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                LogCat.e("正在检测是否联网。。。。。");
                if (isAdded()) {
                    if (DataUtils.isNetworkConnected(getActivity())) {
                        // 先去请求服务器，查看视频列表
                        if(isTest){
                            doHttpGetVideoList();
                        }else {
                            doHttpGetEpisode();
                        }


                        httpTimer.cancel();
                        httpTimer = null;
                        LogCat.e("已经联网。。。。。");
                    } else {
                        LogCat.e("没有联网。。。。。继续检查");
                    }
                } else {
                    if (httpTimer != null) {
                        httpTimer.cancel();
                    }

                }

            }
        }, 0, 10 * 1000);
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
        return DataUtils.getRawVideoUri(getActivity(), R.raw.video_test);
//        return "";
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
        AdDetailResponse videoAdBean = null;
        if (playVideoLists == null || playVideoLists.size() < 2) {
            if (localVideoList != null && localVideoList.size() > 0) {
                int index = 0;
                if (playVideoIndex < localVideoList.size()) {
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
            if (playVideoIndex < playVideoLists.size()) {
                index = playVideoIndex;
            }
            videoAdBean = playVideoLists.get(index);
        }
        return videoAdBean;
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


}
