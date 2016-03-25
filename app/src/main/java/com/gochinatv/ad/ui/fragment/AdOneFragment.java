package com.gochinatv.ad.ui.fragment;

import android.media.MediaPlayer;
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
import com.gochinatv.ad.thread.DeleteFileUtils;
import com.gochinatv.ad.tools.Constants;
import com.gochinatv.ad.tools.DataUtils;
import com.gochinatv.ad.tools.DownloadUtils;
import com.gochinatv.ad.tools.LogCat;
import com.gochinatv.ad.video.MeasureVideoView;
import com.google.gson.Gson;
import com.httputils.http.response.PlayInfoResponse;
import com.httputils.http.response.UpdateResponse;
import com.httputils.http.response.VideoDetailListResponse;
import com.httputils.http.response.VideoDetailResponse;
import com.umeng.analytics.MobclickAgent;

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
    private ArrayList<VideoDetailResponse> localVideoList;

    /**
     * 重复请求的次数
     */
    private Timer httpTimer;
    /**
     * 当前下载视频的info
     */
    private VideoDetailResponse downloadingVideoResponse;
    /**
     * 下载重试次数
     */
    private int retryGetVIdeoListTimes;

    /**
     * 下载info
     */
    private UpdateResponse.UpdateInfoResponse updateInfo;

    /**
     * 当前播放视频的序号
     */
    private int playVideoIndex;
    private ArrayList<VideoDetailResponse> playVideoLists = null;
    private ArrayList<VideoDetailResponse> deleteLists = null;
    private ArrayList<VideoDetailResponse> downloadLists = null;

    private boolean isDownloadVideo;

    private String videoUrl;

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
            playVideo(getRawVideoUri());
            return;
        }
        // 初始化本地缓存表
        localVideoList = new ArrayList<>();
        // 先开始播放视频
        // 优先查看是否有缓存的视频可以播放，有就播放，没有则播放本地视频
        initLocalBufferList();
        // 播放本地缓存列表第一个视频
        playVideo(localVideoList.get(0).videoPath);

        // 清空所有升级包
        DeleteFileUtils.getInstance().deleteFile(DataUtils.getSdCardFileDirectory() + Constants.FILE_DIRECTORY_APK);
        // 请求视频列表
        httpRequest();
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

                if (what == 38) {
                    return false;
                }


                videoView.stopPlayback();

                // 继续播放下一个
                // 删除当前的视频
                // 继续下载播放失败的

                if (playVideoLists == null || playVideoLists.size() == 0) {
                    int size = localVideoList.size();
                    if (localVideoList != null && size > 0 && playVideoIndex < size) {
                        VideoDetailResponse videoDetailResponse = localVideoList.get(playVideoIndex);
                        if (!videoDetailResponse.isPresetPiece) {
                            localVideoList.remove(videoDetailResponse);
                            // 如果是缓存文件出错，直接删除当前文件，并播放下一个
                            DeleteFileUtils.getInstance().deleteFile(videoDetailResponse.videoPath);
                        } else {
                            // TODO 上报预置片出错
                        }
                    }

                } else {
                    int size = playVideoLists.size();
                    if (playVideoLists != null && size > 0 && playVideoIndex < size) {
                        VideoDetailResponse videoDetailResponse = playVideoLists.get(playVideoIndex);
                        // 从播放列表将当前视频删除
                        playVideoLists.remove(videoDetailResponse);
                        // 删除当前的视频文件
                        DeleteFileUtils.getInstance().deleteFile(videoDetailResponse.videoPath);
                        // 添加重新下载当前文件
                        if (downloadLists != null) {
                            // 仍有任务没有下载完成，将当前任务添加到最后一个
                            downloadLists.add(videoDetailResponse);
                            if (downloadLists.size() == 1) { // 此时表示已经没有下载任务，需要主动开启下载，否则表示下载还在进行中
                                prepareDownloading();
                            }
                        } else {
                            downloadLists = new ArrayList<VideoDetailResponse>();
                            downloadLists.add(videoDetailResponse);
                            prepareDownloading();
                        }
                    }
                }

                playNext();

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
                playNext();
            }
        });
    }


    @Override
    public void onStop() {
        super.onStop();

        if(httpTimer != null){
            httpTimer.cancel();
            httpTimer = null;
        }

        DLUtils.init().cancel();


    }

    @Override
    protected void onUpgradeSuccessful(UpdateResponse.UpdateInfoResponse updateInfo) {
        this.updateInfo = updateInfo;
        isDownloadVideo = false;
        DownloadUtils.download(Constants.FILE_DIRECTORY_APK, Constants.FILE_APK_NAME, updateInfo.fileUrl, this);

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

        ArrayList<VideoDetailResponse> videoDetailResponses = response.data;

        if (videoDetailResponses == null || videoDetailResponses.size() == 0) {
            // TODO 默认继续播放之前的缓存文件
            return;
        }

        //
        // servierList 去除本地缓存中需要用到的就是都要下载的
        downloadLists = new ArrayList<>();
        // 本地缓存中还有用的视频，或者预置片
        playVideoLists = new ArrayList<>();
        // 本地缓存中已经无用的视频
        deleteLists = new ArrayList<>();



        // 拿到2天的列表，先处理当天的视频列表

        // 拿到当天的列表去匹对prepareVideo的文件列表内容

        // 匹对成功的添加到playVideo列表，

        // 匹对大于等于2个，删除video目录所有视频和preVideo目录多余视频，并将匹对的视频移动到video目录

        // 匹对小于2个，判断当前prepareVideo视频个数，如果超过2个，删除video的内容，如果不足2个，删除不匹对的prepareVideo视频



        downloadLists.addAll(videoDetailResponses);
        // 判断当前缓存视频是否是
        if (localVideoList.size() == 1) {
            VideoDetailResponse videoDetailResponse = localVideoList.get(0);
            if (videoDetailResponse.isPresetPiece) { // 预置片
                // 此时说明所有的视频都要下载没有预置片
            } else {
                // 此时只有一个缓存视频，判断当前视频是否需要下载
                boolean isUsefully = false;
                for (VideoDetailResponse detailResponse : videoDetailResponses) {
                    if (videoDetailResponse.name.equals(detailResponse.name)) {
                        isUsefully = true;
                        // 从下载列表中删除当前视频
                        downloadLists.remove(detailResponse);
                        // 将当前视频加入播放列表
                        detailResponse.videoPath = videoDetailResponse.videoPath;
                        playVideoLists.add(detailResponse);
                    }
                }
                // 不在使用了，需要将当前视频删除
                if (!isUsefully) {
                    deleteLists.add(videoDetailResponse);
                }
            }

        } else { // 有缓存视频的存在
            for (VideoDetailResponse localVideoResponse : localVideoList) {
                boolean isUsefully = false;
                for (VideoDetailResponse videoDetailResponse : videoDetailResponses) {
                    // 当前视频需要继续使用
                    if (videoDetailResponse.name.equals(localVideoResponse.name)) {
                        isUsefully = true;
                        // 从下载列表中删除当前视频
                        downloadLists.remove(videoDetailResponse);
                        // 当前是否仍然需要使用
                        videoDetailResponse.videoPath = localVideoResponse.videoPath;
                        playVideoLists.add(videoDetailResponse);
                        break;
                    }
                }
                // 当前视频不在缓存列表中，需要下载
                if (!isUsefully) {
                    deleteLists.add(localVideoResponse);
                }
            }
        }


        // 删除所有不在需要的缓存视频，除了当前正在播放的视频
        // 从缓存中查找需要删除的视频  然后进行删除
        if (deleteLists.size() != 0) {
            VideoDetailResponse playingVideo = localVideoList.get(playVideoIndex);
            for (VideoDetailResponse videoDetailResponse : playVideoLists) {
                if (!playingVideo.name.equals(videoDetailResponse.name)) {
                    DeleteFileUtils.getInstance().deleteFile(playingVideo.videoPath);
                }
            }
        } else {
            deleteLists = null;
        }


        // 开启下载
        for (VideoDetailResponse videoDetailResponse : downloadLists) {
            LogCat.e("需要下载的视频..." + videoDetailResponse.name);
        }
        prepareDownloading();

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
        retryGetVIdeoListTimes = 0;
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
            if (retryGetVIdeoListTimes < 2) {
                retryGetVIdeoListTimes++;
                LogCat.e("第 " + retryGetVIdeoListTimes + " 次重新尝试获取下载地址");
                prepareDownloading();
            } else {
                retryGetVIdeoListTimes = 0;
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
            LogCat.e("onDownloadFileError............. " + errorCode + ",  " + errorMsg);
            if(errorCode == ErrorCodes.ERROR_DOWNLOAD_SDCARD_SPACE){ // 如果是空间不足的错误，就不在进行下载
                // TODO 上报情况
            }else {
                downVideoError();
            }


        } else {
            downloadApkError();
        }

    }

    private void downVideoError() {
        // 出错就放弃当前下载任务，继续下载下一个任务，并将当前任务放到最后一个，如果已经是最后一个，再重试2边
        final int size = downloadLists.size();
        if (size > 1) {
            videoView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    LogCat.e("5秒后继续尝试，如此循环。。。。");
                    if (retryGetVIdeoListTimes < 3) {
                        retryGetVIdeoListTimes++;
                        LogCat.e("继续重试3次下载，此时是第" + retryGetVIdeoListTimes + "次尝试。。。。");
                        download(videoUrl);
                    } else {
                        retryGetVIdeoListTimes = 0;
                        LogCat.e("将当前下载失败的视频放到最后一个，继续下载后续的视频。。。。");
                        downloadLists.add(size, downloadingVideoResponse);
                        downloadLists.remove(0);
                        prepareDownloading();
                    }
                }
            }, 5000);

        } else {
            LogCat.e("只剩最后一个视频。。。。");
            videoView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    LogCat.e("5秒后继续尝试，如此循环。。。。");
                    download(videoUrl);

                }
            }, 5000);
        }
    }


    private void downloadApkSuccess(String filePath) {
        updateInfo = null;
        DataUtils.installApk(getActivity(), filePath);
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
                    DownloadUtils.download(Constants.FILE_DIRECTORY_APK, Constants.FILE_APK_NAME, updateInfo.fileUrl, AdOneFragment.this);
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
            downloadLists = null;
        } else {
            LogCat.e("获取下载列表第一个视频，并开始下载。。。。。。。。 还剩余下载任务：" + downloadLists.size());
            downloadingVideoResponse = downloadLists.get(0);
            String path = DataUtils.getSdCardFileDirectory() + Constants.FILE_DIRECTORY_VIDEO;
            downloadingVideoResponse.videoPath = path + downloadingVideoResponse.name + Constants.FILE_DOWNLOAD_EXTENSION;
            if (downloadingVideoResponse.playInfo != null && downloadingVideoResponse.playInfo.size() != 0) {
                PlayInfoResponse playInfoResponse = downloadingVideoResponse.playInfo.get(0);
                doHttpGetCdnPath(playInfoResponse.remotevid);
            }
        }
    }

    private void download(String url) {
        isDownloadVideo = true;
        // 一个视频一个视频的下载
        DownloadUtils.download(Constants.FILE_DIRECTORY_VIDEO, downloadingVideoResponse.name + Constants.FILE_DOWNLOAD_EXTENSION, url, this);
    }


    private void playNext() {
        // 此时说明还没有可以播放的视频，播放预置片或者缓存文件
        if (playVideoLists == null || playVideoLists.size() == 0) {
            VideoDetailResponse videoAdBean = localVideoList.get(playVideoIndex);

            MobclickAgent.onEvent(getActivity(), "video_play_times", videoAdBean.name);

            LogCat.e("添加一次视频播放" + videoAdBean.name);

            // 播放缓存列表文件
            int length = localVideoList.size();
            if (playVideoIndex >= length) {
                playVideoIndex = 0;
            }
            VideoDetailResponse videoDetailResponse = localVideoList.get(playVideoIndex);
            LogCat.e("即将播放视频。。。" + videoDetailResponse.name + "  " + playVideoIndex);
            playVideo(videoDetailResponse.videoPath);

        } else {
            // 删除所有需要删除的缓存文件
            if (deleteLists != null) {
                if (deleteLists.size() != 0) {
                    for (VideoDetailResponse deleteResponse : deleteLists) {
                        DeleteFileUtils.getInstance().deleteFile(deleteResponse.videoPath);
                    }
                    deleteLists.clear();
                }
                deleteLists = null;
            }


            // 情况缓存文件列表，重置状态
            if (localVideoList != null) {
                playVideoIndex = 0;
                localVideoList.clear();
                localVideoList = null;
            }

            VideoDetailResponse videoAdBean = playVideoLists.get(playVideoIndex);

            MobclickAgent.onEvent(getActivity(), "video_play_times", videoAdBean.name);

            LogCat.e("添加一次视频播放" + videoAdBean.name);

            // 播放缓存列表文件
            int length = playVideoLists.size();
            if (playVideoIndex >= length) {
                playVideoIndex = 0;
            }
            VideoDetailResponse videoDetailResponse = playVideoLists.get(playVideoIndex);
            LogCat.e("即将播放视频。。。" + videoDetailResponse.name + "  " + playVideoIndex);
            playVideo(videoDetailResponse.videoPath);


        }
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
        if(isPlayPrepareVideos()){
            // 播放第一个视频
            for(VideoDetailResponse videoDetailResponse : localVideoList){
                if(!TextUtils.isEmpty(videoDetailResponse.videoPath)){
                    playVideo(videoDetailResponse.videoPath);
                    break;
                }
            }
        } else {
            String videoPath = DataUtils.getSdCardFileDirectory() + Constants.FILE_DIRECTORY_VIDEO;
            LogCat.e("当前视频缓存的路径：" + videoPath);
            File videoFiles = new File(videoPath);
            if (videoFiles.exists()) { // 有就要查询当前目录下的所有文件
                if (videoFiles.isDirectory()) {
                    localVideoList.addAll(getLocalList(videoFiles));
                } else {
                    reBuildDirectory(videoPath);
                }
            } else { // 没有当前目录，要进行创建
                if (videoFiles.mkdirs()) {
                    LogCat.e("sdcard文件目录创建成功......");

                } else {
                    // TODO 上报问题，sdcard无法创建文件目录
                    LogCat.e("sdcard文件目录创建失败......");
                }
            }

            // 文件数量少于2就播放预置片
            if (localVideoList.size() < 2) {
                localVideoList.clear();

                VideoDetailResponse videoAdBean = new VideoDetailResponse();
                videoAdBean.name = "预置片";
                videoAdBean.videoPath = getRawVideoUri();
                videoAdBean.isPresetPiece = true;
                videoAdBean.index = 0;
                localVideoList.add(videoAdBean);
            }


        }


    }


    /**
     * 检查是否要播放预下载视频内容，如果有预下载视频内容将其加入local表中
     * @return
     */
    private boolean isPlayPrepareVideos() {
        boolean isPlayPrepareVideo = false;
        try {
            // 优先检测prepareVideo缓存视频数量
            File filePrepare = new File(DataUtils.getPrepareVideoDirectory());
            if(filePrepare.exists()){
                LogCat.e("prepareVideo文件目录存在.....");
                File[] fileVideos = filePrepare.listFiles();
                if(fileVideos.length < 2){
                    LogCat.e("prepareVideo文件内部视频缓存文件不足2个.....");
                    // 检测video视频目录。如果视频多余2个，加入local，少于2个播放预置片
                }else {
                    LogCat.e("prepareVideo文件内部视频缓存文件超过2个.....");
                    // 检测cache中的视频列表，获取缓存文件，如果有，读取列表，如果没有，直接去检测video视频目录
                    String cachePath = DataUtils.getCacheDirectory();
                    File fileCache = new File(cachePath);
                    if(!fileCache.exists()){
                        LogCat.e("cache文件目录不存在，进行创建.....");
                        fileCache.mkdirs();
                    }
                    File fileCacheVideo = new File(cachePath, Constants.FILE_CACHE_NAME);
                    if(fileCacheVideo.exists() && fileCacheVideo.isFile()){
                        LogCat.e("准备读取cache的内容.....");
                        // 读取缓存文件
                        String json = DataUtils.readFileFromSdCard(fileCacheVideo);
                        if(TextUtils.isEmpty(json)){
                            LogCat.e("cache文件内容为null.....");
                            // 检测video视频目录。如果视频多余2个，加入local，少于2个播放预置片
                        }else {
                            // 将其转换成实体类
                            Gson gson = new Gson();
                            VideoDetailListResponse videoDetailListResponse = gson.fromJson(json, VideoDetailListResponse.class);
                            if(videoDetailListResponse == null || videoDetailListResponse.data == null || videoDetailListResponse.data.size() < 2){
                                // 检测video视频目录。如果视频多余2个，加入local，少于2个播放预置片
                                LogCat.e("cache文件内视频列表的数量小于2.....");
                            }else {
                                LogCat.e("开始匹配cache的列表和prepareVideo的文件列表.....");
                                // 检测prepareVideo目录下的视频，跟上面的列表进行配对，如果有则加入Local列表
                                ArrayList<VideoDetailResponse> fileVideoList = getLocalList(fileCacheVideo);
                                // 匹配视频列表，获取有用的视频
                                int fileVideoSize = fileVideoList.size();

                                int prepareVideoSize = videoDetailListResponse.data.size();
                                for(int i = 0; i < fileVideoSize; i++){
                                    VideoDetailResponse fileVideoResponse = fileVideoList.get(i);
                                    for(int j = 0; j < prepareVideoSize; j ++){
                                        VideoDetailResponse prepareVideoResponse = videoDetailListResponse.data.get(j);
                                        if(prepareVideoResponse.name.equals(fileVideoResponse.name)){
                                            // 当前视频可以使用
                                            prepareVideoResponse.videoPath = fileVideoResponse.videoPath;
                                            // 加入本地视频列表
                                            localVideoList.add(prepareVideoResponse.index, prepareVideoResponse);
                                            break;
                                        }
                                    }
                                }

                                if(localVideoList.size() < 2){
                                    localVideoList.clear();
                                    // 检测video视频目录。如果视频多余2个，加入local，少于2个播放预置片
                                    LogCat.e("匹配后的结果集数量不足2个，情况local，准备播放昨天的内容.....");
                                }else {
                                    // 播放缓存列表的视频内容
                                    isPlayPrepareVideo = true;
                                    LogCat.e("匹配到合适的结果，准备播放.....");
                                }
                            }
                        }
                    }else {
                        // 检测video视频目录。如果视频多余2个，加入local，少于2个播放预置片
                        LogCat.e("cache文件不存在或不是个文件.....");
                    }
                }
            }else {
                filePrepare.mkdirs();
                LogCat.e("prepareVideo目录不存在.....");
                // 检测video视频目录。如果视频多余2个，加入local，少于2个播放预置片
            }
        }catch (Exception e){
            e.printStackTrace();
        }


        return isPlayPrepareVideo;
    }

    private void reBuildDirectory(String videoPath) {
        File videoFiles;
        LogCat.e("文件目录出错，并不是目录，需要删除再创建......");
        // 删除原来的文件结构
        DeleteFileUtils.getInstance().deleteDir(new File(DataUtils.getSdCardFileDirectory()));
        // 重新创建，避免冲突，延迟重新创建
        File fileParent = new File(DataUtils.getSdCardFileDirectory());
        if (!fileParent.exists()) {
            fileParent.mkdirs();
        }
        videoFiles = new File(videoPath);
        if (!videoFiles.exists()) {
            videoFiles.mkdirs();
            LogCat.e("sdcard文件目录创建成功......");
        } else {
            // TODO 上报问题，sdcard无法创建文件目录
            LogCat.e("sdcard文件目录创建失败......");
        }
    }

//    private void addLocalList(File videoFiles) {
//        File[] files = videoFiles.listFiles();
//        for (File file : files) {
//            if (file.isFile()) {
//                VideoDetailResponse videoAdBean = new VideoDetailResponse();
//                String name = file.getName();
//                int index = name.lastIndexOf(Constants.FILE_DOWNLOAD_EXTENSION);
//                name = name.substring(0, index);
//                videoAdBean.name = name;
//                videoAdBean.videoPath = file.getAbsolutePath();
//                localVideoList.add(videoAdBean);
//                LogCat.e("找到本地缓存文件：" + videoAdBean.name);
//            } else {
//                DeleteFileUtils.getInstance().deleteFile(file.getAbsolutePath());
//            }
//        }
//    }

    private ArrayList<VideoDetailResponse> getLocalList(File videoFiles) {
        ArrayList<VideoDetailResponse> videoDetailResponses = new ArrayList<>();
        File[] files = videoFiles.listFiles();
        for (File file : files) {
            if (file.isFile()) {
                VideoDetailResponse videoAdBean = new VideoDetailResponse();
                String name = file.getName();
                int index = name.lastIndexOf(Constants.FILE_DOWNLOAD_EXTENSION);
                name = name.substring(0, index);
                videoAdBean.name = name;
                videoAdBean.videoPath = file.getAbsolutePath();
                videoDetailResponses.add(videoAdBean);
                LogCat.e("找到本地缓存文件：" + videoAdBean.name);
            } else {
                DeleteFileUtils.getInstance().deleteFile(file.getAbsolutePath());
            }
        }
        return videoDetailResponses;
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


}
