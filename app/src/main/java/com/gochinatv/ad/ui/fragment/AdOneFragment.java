package com.gochinatv.ad.ui.fragment;

import android.media.MediaPlayer;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

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
import com.gochinatv.ad.tools.ScreenShotUtils;
import com.gochinatv.ad.video.MeasureVideoView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.httputils.http.response.AdDetailResponse;
import com.httputils.http.response.PlayInfoResponse;
import com.httputils.http.response.VideoDetailListResponse;
import com.okhtttp.response.LayoutResponse;

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
     * 当前播放视频的序号
     */
    private int playVideoIndex;
    private ArrayList<AdDetailResponse> playVideoLists = null;
    private ArrayList<AdDetailResponse> cachePlayVideoLists = null;
    private ArrayList<AdDetailResponse> deleteLists = null;
    private ArrayList<AdDetailResponse> downloadLists = null;
//    private ArrayList<AdDetailResponse> prepareDownloadLists = null;


    private boolean isDownloadVideo;


    /**
     * 是否用更新的apk在下载
     */
    private boolean isDownloadAPK;

    private String videoUrl;

    private Timer screenShotTimer;

    private Handler handler;

    private boolean isDownloadPrepare;

    /**
     * 进行重试的时间间隔
     */
    private final int TIME_RETRY_DURATION = 1000 * 10;
    /**
     * 最多进行重试的次数
     */
    private final int MAX_RETRY_TIMES = 3;

    public LayoutResponse getLayoutResponse() {
        return layoutResponse;
    }

    public void setLayoutResponse(LayoutResponse layoutResponse) {
        this.layoutResponse = layoutResponse;
    }

    //布局参数
    private LayoutResponse layoutResponse;


    @Override
    protected View initLayout(LayoutInflater inflater, ViewGroup container) {
        RelativeLayout layout = (RelativeLayout) inflater.inflate(R.layout.fragment_ad_video, container, false);
        if(layoutResponse != null){

            if(!TextUtils.isEmpty(layoutResponse.adWidth) && !TextUtils.isEmpty(layoutResponse.adHeight)
                    && !TextUtils.isEmpty(layoutResponse.adTop) && !TextUtils.isEmpty(layoutResponse.adLeft)){

                String widthStr = layoutResponse.adWidth;
                String heightStr = layoutResponse.adHeight;
                String topStr = layoutResponse.adTop;
                String leftStr = layoutResponse.adLeft;

                //动态布局
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
                double width = (float) (DataUtils.getDisplayMetricsWidth(getActivity())*(Float.parseFloat(widthStr)));
                double height = (float) (DataUtils.getDisplayMetricsHeight(getActivity())*(Float.parseFloat(heightStr)));
                double top = (float) (DataUtils.getDisplayMetricsHeight(getActivity())*(Float.parseFloat(topStr)));
                double left = (float) (DataUtils.getDisplayMetricsWidth(getActivity())*(Float.parseFloat(leftStr)));

                params.width = (int) Math.floor(width);
                params.height = (int) Math.floor(height);
                params.topMargin = (int) Math.floor(top);

                params.leftMargin = (int) Math.floor(left);
                layout.setLayoutParams(params);
                LogCat.e(" 广告二布局 width: "+params.width+" height: "+params.height+" top: "+params.topMargin+" left: "+params.leftMargin);

            }
        }
        return layout;
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
        if (localVideoList.size() != 0) {
            // 2.获取今日播放的视频完整性
            // ArrayList<AdDetailResponse> cacheTodayList = getCacheList(Constants.FILE_CACHE_TD_NAME);

            // 3.取明日缓存列表
            // LogCat.e("获取缓存的明日播放列表.......");
            // ArrayList<AdDetailResponse> cacheTomorrowList = getCacheList(Constants.FILE_CACHE_NAME);
            // LogCat.e("------------------------------");

            // 4.根据今明两日的文件列表检查localVideoList列表文件的完整性
            // checkFileLength(cacheTodayList);
            // checkFileLength(cacheTomorrowList);


            // 5.根据本地缓存视频列表和缓存明日列表，得出当前的播放列表
            // LogCat.e("根据本地缓存视频列表和缓存明日列表，得出当前的播放列表.....");
            // cachePlayVideoLists = getPlayVideoList(localVideoList, cacheTomorrowList);

            // TODO 临时
            cachePlayVideoLists = localVideoList;
        } else {
            cachePlayVideoLists = localVideoList;
        }

        // 4.播放缓存列表
        LogCat.e("查找可以播放的视频.....");
        startPlayVideo();


        String oldPath = DataUtils.getSdCardOldFileDirectory();
        LogCat.e("清空旧文件目录(gochinatv)....." + oldPath);
        DeleteFileUtils.getInstance().deleteDir(new File(DataUtils.getSdCardOldFileDirectory()));

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


//        VideoHttpService.doHttpGetVideoList(getActivity(), new OkHttpCallBack<AdVideoListResponse>() {
//            @Override
//            public void onSuccess(String url, AdVideoListResponse response) {
//                LogCat.e("新街口成功了........*******************.");
//                if (response != null) {
//
//                }
//
//            }
//
//            @Override
//            public void onError(String url, String errorMsg) {
//                LogCat.e("新街口onError了.........********************");
//            }
//        });

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
    private synchronized ArrayList<AdDetailResponse> getCacheList(String fileName) {
        ArrayList<AdDetailResponse> cacheTomorrowList = new ArrayList<>();
        File cacheFile = new File(DataUtils.getCacheDirectory() + fileName);
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
                if (!isAdded()) {
                    return;
                }
                LogCat.e("视频播放完成。。。。。。");
                // 播放下一个视频
                playNext(false);
            }
        });
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
        super.onStop();
        LogCat.e("onStop...................");
        if (httpTimer != null) {
            httpTimer.cancel();
            httpTimer = null;
        }

        DLUtils.init(getActivity()).cancel();

        ScreenShotUtils.shutdown();


        if (screenShotTimer != null) {
            screenShotTimer.cancel();
        }

        if (downloadingVideoResponse != null) {
            DeleteFileUtils.getInstance().deleteFile(downloadingVideoResponse.videoPath);
        }


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

        // 将今日列表缓存到本地
//        cacheVideoList(Constants.FILE_CACHE_TD_NAME, adDetailResponses);

        // 1.更新数据内容
        // 重新获取本地缓存列表
        LogCat.e("重新获取本地缓存列表......");
        localVideoList = getLocalVideoList();
        LogCat.e("------------------------------");

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

    private void reconnectedPrepare() {
//        int downloadsSize = downloadLists.size();
//        for(int i = 0; i < prepareDownloadLists.size(); i++){
//            AdDetailResponse prepareVideo = prepareDownloadLists.get(i);
//            for(int j = 0; j < downloadsSize; j++){
//                AdDetailResponse downloadVideo = downloadLists.get(j);
//                if(!TextUtils.isEmpty(prepareVideo.adVideoName) && prepareVideo.adVideoName.equals(downloadVideo.adVideoName)){
//                    prepareDownloadLists.remove(i);
//                    LogCat.e("剔除重复下载的视频：" + prepareVideo.adVideoName);
//                    --i;
//                    break;
//                }
//            }
//        }
//        LogCat.e("------------------------------");
//        LogCat.e("最终的明日下载列表.......");
//        for(AdDetailResponse adDetailResponse : prepareDownloadLists){
//            LogCat.e("明日下载视频：" + adDetailResponse.adVideoName);
//        }
//        LogCat.e("------------------------------");
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
                    playList.add(todayResponse.adVideoIndex, todayResponse);
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
        }, TIME_RETRY_DURATION);
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
            }, TIME_RETRY_DURATION);


        } else {
            LogCat.e("只剩最后一个视频。。。。");
            if (retryGetVideoListTimes < MAX_RETRY_TIMES) {
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
        retryGetVideoListTimes = 0;

    }


    @Override
    public void onDownloadFileError(int errorCode, String errorMsg) {
        if (errorCode == ErrorCodes.ERROR_DOWNLOAD_SDCARD_SPACE) { // 如果是空间不足的错误，就不在进行下载
            // TODO 上报情况
        } else {
            downVideoError();
        }
    }

    private void downVideoError() {
        // 出错就放弃当前下载任务，继续下载下一个任务，并将当前任务放到最后一个，如果已经是最后一个，再重试2边
        final int size = downloadLists.size();
        videoView.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isAdded()) {
                    return;
                }
                if (retryGetVideoListTimes < MAX_RETRY_TIMES) {
                    retryGetVideoListTimes++;
                    LogCat.e("继续重试3次下载，此时是第" + retryGetVideoListTimes + "次尝试。。。。");
                    download(videoUrl);
                } else {
                    retryGetVideoListTimes = 0;
                    if (size != 1) { // 如果不是最后一个视频，就继续下载下一个，如果是就放弃下载
                        retryGetVideoListTimes = 0;
                        LogCat.e("将当前下载失败的视频放到最后一个，继续下载后续的视频。。。。");
                        downloadLists.add(size, downloadingVideoResponse);
                        downloadLists.remove(0);
                        prepareDownloading();
                    }

                }
            }
        }, TIME_RETRY_DURATION);

    }


    /**
     * 下载准备工作
     */
    private void prepareDownloading() {
        if (downloadLists.size() == 0) {
//            if (prepareDownloadLists.size() == 0) {
            LogCat.e("所有视频下载完成。。。。。。。。");
            downloadingVideoResponse = null;
            isDownloadPrepare = false;
//            } else {
//                LogCat.e("当日的下载列表下载完成，继续下载明日与播放的视频列表");
//                downloadLists.addAll(prepareDownloadLists);
//                prepareDownloadLists.clear();
//                isDownloadPrepare = true;
//                prepareDownloading();
//            }
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


    /**
     * 检测localVideoList列表文件完整性
     * @param cacheTodayList
     */
    private void checkFileLength(ArrayList<AdDetailResponse> cacheTodayList) {
        if (cacheTodayList != null && cacheTodayList.size() != 0) {
            LogCat.e("检测到昨日播放列表, 开始检测文件完整性......");
            for(int i = 0; i < localVideoList.size(); i++){
                AdDetailResponse localVideo = localVideoList.get(i);
                for(AdDetailResponse cacheVideo : cacheTodayList){
                    if(cacheVideo.adVideoLength != 0 && cacheVideo.adVideoLength != localVideo.adVideoLength){
                        --i;
                        localVideoList.remove(localVideo);
                        DeleteFileUtils.getInstance().deleteFile(localVideo.videoPath);
                        LogCat.e("由于文件不完整，需要删除的文件是......." + localVideo.adVideoName);
                    }
                }
            }
        }else {
            for(int i = 0; i < localVideoList.size(); i++){
                AdDetailResponse localVideo = localVideoList.get(i);
                if(localVideo.adVideoLength == 0){
                    --i;
                    localVideoList.remove(localVideo);
                    DeleteFileUtils.getInstance().deleteFile(localVideo.videoPath);
                    LogCat.e("由于文件大小==0，需要删除的文件是......." + localVideo.adVideoName);
                }
            }
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
                if(isAdded()){
                    if (DataUtils.isNetworkConnected(getActivity())) {
                        // 先去请求服务器，查看视频列表
                        doHttpGetEpisode();
                        httpTimer.cancel();
                        httpTimer = null;
                        LogCat.e("已经联网。。。。。");
                    } else {
                        LogCat.e("没有联网。。。。。继续检查");
                    }
                }else {
                    httpTimer.cancel();
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
     * 返回当前的本地video视频列表信息集合
     *
     * @param videoFiles
     * @return
     */
    private ArrayList<AdDetailResponse> getLocalList(File videoFiles) {
        ArrayList<AdDetailResponse> adDetailResponses = new ArrayList<>();
        File[] files = videoFiles.listFiles();
        for (File file : files) {
            if (file.isFile()) {
                String name = file.getName();
                // 正在下载的文件不能算到本地缓存列表中
                if (DLUtils.init(getActivity()).downloading(name)) {
                    LogCat.e("当前文件正在下载。。。。。");
                    continue;
                }
                // 文件下载失败
                if(file.length() == 0){
                    DeleteFileUtils.getInstance().deleteFile(file.getAbsolutePath());
                    continue;
                }
                AdDetailResponse videoAdBean = new AdDetailResponse();
                int index = name.lastIndexOf(Constants.FILE_DOWNLOAD_EXTENSION);
                name = name.substring(0, index);
                // TODO 以后要删除的
                videoAdBean.adVideoName = name;
                videoAdBean.videoPath = file.getAbsolutePath();
                videoAdBean.adVideoLength = file.length();
                adDetailResponses.add(videoAdBean);
                LogCat.e("本地缓存视频：" + videoAdBean.adVideoName + "  " + videoAdBean.adVideoLength);
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
//        return DataUtils.getRawVideoUri(getActivity(), R.raw.video_test);
        return "";
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

    private synchronized void cacheVideoList(String fileName, ArrayList<AdDetailResponse> cachePlayVideoLists) {
        new CacheVideoListThread(cachePlayVideoLists, DataUtils.getCacheDirectory(), fileName).start();
        LogCat.e("文件缓存成功........." + fileName);
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
