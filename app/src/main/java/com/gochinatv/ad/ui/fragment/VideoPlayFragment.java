package com.gochinatv.ad.ui.fragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.gochinatv.ad.R;
import com.gochinatv.ad.base.VideoHttpBaseFragment;
import com.gochinatv.ad.interfaces.OnUpgradeStatusListener;
import com.gochinatv.ad.thread.DeleteFileUtils;
import com.gochinatv.ad.tools.Constants;
import com.gochinatv.ad.tools.DataUtils;
import com.gochinatv.ad.tools.DownloadApk;
import com.gochinatv.ad.tools.LogCat;
import com.gochinatv.ad.video.MeasureVideoView;
import com.httputils.http.response.UpdateResponse;
import com.httputils.http.response.VideoDetailListResponse;
import com.httputils.http.response.VideoDetailResponse;

import java.io.File;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by zfy on 2016/3/16.
 */
public class VideoPlayFragment extends VideoHttpBaseFragment implements OnUpgradeStatusListener{

    private MeasureVideoView videoView;
    private LinearLayout loading;
    /** 本地数据表 */
    private ArrayList<VideoDetailResponse> localVideoTable;

    /** 重复请求的次数 */
    private Timer httpTimer;

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
        if(!DataUtils.isExistSDCard()){
            LogCat.e("sd卡状态不可用......");
            playVideo(getRawVideoUri());
            return;
        }
        // 初始化本地缓存表
        localVideoTable = new ArrayList<>();
        // 先开始播放视频
        // 优先查看是否有缓存的视频可以播放，有就播放，没有则播放本地视频
        initLocalBufferList();
        // 播放本地缓存列表第一个视频
        playVideo(localVideoTable.get(0).videoPath);

        // 清空所有升级包
        DeleteFileUtils.getInstance().deleteFile(DataUtils.getSdCardFileDirectory() + Constants.FILE_DIRECTORY_APK);
        // 请求视频列表
        httpRequest();
    }



    @Override
    protected void bindEvent() {

    }

    @Override
    protected void onGetVideoListSuccessful(VideoDetailListResponse response, String url) {
        if (!isAdded()) {
            return;
        }

        if(response == null){
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
        ArrayList<VideoDetailResponse> downloadLists = null;
        // 本地缓存中还有用的视频，或者预置片
        ArrayList<VideoDetailResponse> playLists = null;
        // 本地缓存中已经无用的视频
        ArrayList<VideoDetailResponse> deteleLists = null;

        ArrayList<VideoDetailResponse> bufferUserdLists = new ArrayList<>();
        ArrayList<VideoDetailResponse> bufferUserlessLists = new ArrayList<>();



        // 判断当前缓存视频是否是






        for(VideoDetailResponse localVideoResponse : localVideoTable){

            for(VideoDetailResponse videoDetailResponse : videoDetailResponses){
                if(videoDetailResponse.name.equals(localVideoResponse.name)){
                    bufferUserdLists.add(videoDetailResponse);
                    continue;
                }
            }

            // 当前视频不在缓存列表中，需要下载

        }



    }

    @Override
    protected void onGetVideoListFailed(String errorMsg, String url) {

    }

    @Override
    protected void onGetVideoPathSuccessful(String path) {

    }

    @Override
    protected void onGetVideoPathFailed(String path) {

    }

    @Override
    public void onDownloadApkSuccess(String filePath) {
        // 把下载成功的视频添加到播放列表中
        DataUtils.installApk(getActivity(), filePath);
        getActivity().finish();
    }

    @Override
    public void onDownloadApkError(final UpdateResponse.UpdateInfoResponse updateInfo) {
        // 此时出错，需要判断是否是强制升级，如果是强制升级，说明是接口等重大功能改变，必须优先升级
        // 强制升级：如果出错，就要循环去做升级操作，直至升级成
        // 普通升级：如果出错，不再请求，去请求视频接口
        if ("1".equals(updateInfo.type)) {
            // 强制更新
            videoView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    LogCat.e("5秒后继续尝试，如此循环。。。。");
                    DownloadApk.download(updateInfo, VideoPlayFragment.this);
                }
            }, 5000);

        } else {
            doHttpGetEpisode();
        }
    }

    @Override
    protected void onUpgradeSuccessful(UpdateResponse.UpdateInfoResponse updateInfo) {
        DownloadApk.download(updateInfo, this);
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
        String videoPath = DataUtils.getSdCardFileDirectory() + Constants.FILE_DIRECTORY_VIDEO;
        LogCat.e("当前视频缓存的路径：" + videoPath);
        File videoFiles = new File(videoPath);
        if(videoFiles.exists()){ // 有就要查询当前目录下的所有文件
            if(videoFiles.isDirectory()){
                addLocalList(videoFiles);
            } else {
                reBuildDirectory(videoPath);
            }
        }else { // 没有当前目录，要进行创建
            if(videoFiles.mkdirs()){
                LogCat.e("sdcard文件目录创建成功......");

            } else {
                // TODO 上报问题，sdcard无法创建文件目录
                LogCat.e("sdcard文件目录创建失败......");
            }
        }

        // 如果没有缓存文件，添加本地视频
        if(localVideoTable.size() == 0){
            VideoDetailResponse videoAdBean = new VideoDetailResponse();
            videoAdBean.name = "预置片";
            videoAdBean.videoPath = getRawVideoUri();
            videoAdBean.isPresetPiece = true;
            localVideoTable.add(videoAdBean);
        }

    }

    private void reBuildDirectory(String videoPath) {
        File videoFiles;
        LogCat.e("文件目录出错，并不是目录，需要删除再创建......");
        // 删除原来的文件结构
        DeleteFileUtils.getInstance().deleteDir(new File(DataUtils.getSdCardFileDirectory()));
        // 重新创建，避免冲突，延迟重新创建
        File fileParent = new File(DataUtils.getSdCardFileDirectory());
        if(!fileParent.exists()){
            fileParent.mkdirs();
        }
        videoFiles = new File(videoPath);
        if(!videoFiles.exists()){
            videoFiles.mkdirs();
            LogCat.e("sdcard文件目录创建成功......");
        }else {
            // TODO 上报问题，sdcard无法创建文件目录
            LogCat.e("sdcard文件目录创建失败......");
        }
    }

    private void addLocalList(File videoFiles) {
        File[] files = videoFiles.listFiles();
        for(File file : files){
            if(file.isFile()){
                VideoDetailResponse videoAdBean = new VideoDetailResponse();
                String name = file.getName();
                int index = name.lastIndexOf(Constants.FILE_DOWNLOAD_EXTENSION);
                name = name.substring(0, index);
                videoAdBean.name = name;
                videoAdBean.videoPath = file.getAbsolutePath();
                localVideoTable.add(videoAdBean);
                LogCat.e("找到本地缓存文件：" + videoAdBean.name);
            }else {
                DeleteFileUtils.getInstance().deleteFile(file.getAbsolutePath());
            }
        }
    }

    /**
     * 播放本地视频
     * @param videoPath
     */
    private void playVideo(String videoPath){
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
