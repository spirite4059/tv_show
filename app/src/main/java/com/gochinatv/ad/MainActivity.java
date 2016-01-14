package com.gochinatv.ad;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.text.TextUtils;

import com.gochinatv.ad.base.BaseActivity;
import com.gochinatv.ad.db.AdDao;
import com.gochinatv.ad.db.VideoAdBean;
import com.gochinatv.ad.interfaces.DownloadListener;
import com.gochinatv.ad.tools.DataUtils;
import com.gochinatv.ad.tools.LogCat;
import com.httputils.http.OnRequestListener;
import com.httputils.http.response.PlayInfoResponse;
import com.httputils.http.response.UpdateResponse;
import com.httputils.http.response.VideoDetailListResponse;
import com.httputils.http.response.VideoDetailResponse;
import com.httputils.http.service.AlbnumHttpService;
import com.umeng.analytics.MobclickAgent;
import com.vego.player.MeasureVideoView;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import cn.aigestudio.downloader.bizs.DLManager;

public class MainActivity extends BaseActivity {

    private MeasureVideoView videoView;

    private ArrayList<VideoAdBean> videoAdBeans;

    private int position;

    private String saveDir;

    private VideoHandler videoHandler;

    private static final String FORMAT_VIDEO_AD_TIME = "yyyyMMddHHmmss";

//    private static final int REFRUSH_DURATION = 5 * 60 * 60 * 1000;

    private static final int REFRUSH_DURATION = 200 * 1000;

    private Timer timer;

    private long startLong;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        videoView = (MeasureVideoView) findViewById(R.id.videoView);
        init();
        bindEvent();
        startLong = System.currentTimeMillis();

    }

    private void init() {
        showLoading();

        // 实力化本地下载目录
        if (android.os.Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            LogCat.e("sd卡状态可用。。。。。。。");
        }

        saveDir = Environment.getExternalStorageDirectory() + "/gochinatv_ad/";

        // 查询数据库，看是否有播放记录
        videoAdBeans = AdDao.getInstance(this).queryVideoAds();
        if (videoAdBeans == null || videoAdBeans.size() == 0) {
            // 此时没有视频记录，需要去播放本地视频
            LogCat.e("本地没有播放记录。。。。。。。");
            playLocalVideo();
            // 等待请求完服务器后，准备下载视频
        } else {
            // 有视频数据，开始播放第一个视频
            LogCat.e("本地有播放记录。。。。。。。");
            int length = videoAdBeans.size();
            String url = null;
            ArrayList<Integer> deleteFilesVideos = new ArrayList<>();
            for (int i = 0; i < length; i++) {
                VideoAdBean videoAdBean = videoAdBeans.get(i);
                // 已经下载完成
                if (AdDao.FLAG_DOWNLOAD_FINISHED.equals(videoAdBean.isDownloadFinish)) {
                    if (TextUtils.isEmpty(videoAdBean.videoPath)) {
                        continue;
                    }
                    // 开始播放第一个已经下载完成的视频
                    File file = new File(videoAdBean.videoPath);
                    if (file.exists()) {
                        url = videoAdBean.videoPath;
                    } else {
                        LogCat.e("本地视频已经删除了，需要重新下载，删除本地数据表记录，继续下载");
                        AdDao.getInstance(this).delete(videoAdBean.videoId);
                        deleteFilesVideos.add(i);
                    }
                    break;

                }
            }

            for (Integer i : deleteFilesVideos) {
                VideoAdBean videoAdBean = videoAdBeans.get(i);
                LogCat.e("需要删除的视频" + videoAdBean.videoName);
                if (videoAdBean != null) {
                    videoAdBeans.remove(videoAdBean);
                    LogCat.e("成功删除本地没缓存的视频");
                }
            }
            if (TextUtils.isEmpty(url)) {
                playVideo(DataUtils.getRawVideoUri(this, R.raw.video_test));
            } else {
                playVideo(url);
            }

        }


        // 先去请求服务器，查看视频列表
        doHttpUpdate();
    }


    private void bindEvent() {
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                if (isFinishing()) {
                    return;
                }
                hideLoading();
                LogCat.e("video_onPrepared....");
                videoView.start();
            }
        });

        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                // 播放下一个视频
                playNext();
            }
        });

        videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                LogCat.e("onError...........视频播放失败了");
                playNext();
                return true;
            }
        });

        videoView.setOnVideoBufferStateListener(new MeasureVideoView.OnVideoBufferStateListener() {
            @Override
            public void OnVideoBufferState(boolean isBuffering) {
                if (isBuffering) {
                    showLoading();
                } else {
                    hideLoading();
                }
            }
        });
    }

    private void playVideo(String path) {
        if (!TextUtils.isEmpty(path) && videoView != null) {
            videoView.setVideoPath(path);
        }
    }

    private void playLocalVideo() {
        if (videoView != null && !isFinishing()) {
            LogCat.e("开始播放本地视频........");
            playVideo(DataUtils.getRawVideoUri(this, R.raw.video_test));
        }
    }


    private void playNext() {
        LogCat.e("playNext() " + videoAdBeans.size());
        if (videoAdBeans != null && videoAdBeans.size() != 0) {
            for(VideoAdBean videoAdBean : videoAdBeans){
                LogCat.e("还在播放列表的视频。。。。。" + videoAdBean.videoName);
            }


            position++;
            if (position >= videoAdBeans.size()) {
                position = 0;
                LogCat.e("此时表示已经将所有视频循环一遍了。。。。。");

                MobclickAgent.onEvent(this, "loop_times");

            }
            VideoAdBean videoAdBean = videoAdBeans.get(position);
            LogCat.e("当前视频名字........ " + videoAdBean.videoName);
            LogCat.e("当前视频videoPath........ " + videoAdBean.videoPath);
            if(TextUtils.isEmpty(videoAdBean.videoPath)){
                LogCat.e("当前播放的视频文件路径已经不存在，将其从播放列表删除");

                dealWithFileError(videoAdBean);
            }else {
                // 判断当前要播放的文件是否存在
                File file = new File(videoAdBean.videoPath);
                if(file.exists()){
                    playVideo(videoAdBean.videoPath);
                }else {
                    // 当前要播放的文件已经删除了,需要重新下载
                    LogCat.e("当前文件不存在，或被删除，需要重新下载");
                    dealWithFileError(videoAdBean);
                }
            }
        } else {
            LogCat.e("继续播放本地视频........ ");
            playLocalVideo();
        }
        LogCat.e("当前的序列........ " + position);
    }


    private void dealWithFileError(VideoAdBean videoAdBean){
        VideoDetailResponse videoDetailResponse = new VideoDetailResponse();
        videoDetailResponse.vid = videoAdBean.videoId;
        videoDetailResponse.name = videoAdBean.videoName;
        videoDetailResponse.vid = videoAdBean.videoEndTime;
        videoDetailResponse.tag = videoAdBean.videoId;
        LogCat.e("重新下载当前文件");
        if(downloadViews == null || downloadViews.size() == 0){
            addDownloadList(videoDetailResponse);
            LogCat.e("已经全部下载完成，此时需要继续开启下载任务");
            initDownloadInfo();
        }else {
            LogCat.e("当前下载任务还没完成，将要下载的任务添加到下载列表");
            addDownloadList(videoDetailResponse);
        }
        videoAdBeans.remove(videoAdBean);
        playNext();
    }


    @Override
    protected void doHandlerMessage(Message msg) {
        super.doHandlerMessage(msg);

        switch (msg.what) {
            case 1:
                LogCat.e("doHandlerMessage添加新的数据。。。。。。。");
//                playVideo(String.valueOf(msg.obj));
                if (videoAdBeans == null) {
                    videoAdBeans = new ArrayList<>();
                }
                // 添加到播放列表
                videoAdBeans.add(downLoadingVideo);


                // 查询是否还有未下载的任务，如果有 继续下载
                Object object = new Object();
                synchronized (object) {
                    if (downloadViews != null && downloadViews.size() > 0) {
                        downloadViews.remove(0);
                    }

                }

                // 下载的index序号递增
                synchronized (object) {
                    downloadPosition++;
                }

                if (downloadViews.size() != 0) {
                    // 还有需要下载的视频，要继续下载
                    LogCat.e("还有下载任务。。。。。。" + downloadViews.size());
                    initDownloadInfo();
                }

                break;
        }


    }


    private ArrayList<VideoDetailResponse> videoDetailResponses;
    // 需要下载的视频集合
    private ArrayList<VideoDetailResponse> downloadViews;
    private int downloadPosition;
    private DLManager dlManager;
    private String videoName;
    private VideoAdBean downLoadingVideo;
    private String downloadUrl;

    @Override
    protected void onSuccessFul(VideoDetailListResponse response, String url) {
        LogCat.e("onSuccessFul....");
        if (isFinishing()) {
            return;
        }

        if (response == null || response.data == null || response.data.size() == 0) {
            return;
        }
        videoDetailResponses = response.data;
        if (downloadViews == null) {
            downloadViews = new ArrayList<>(videoDetailResponses.size());
        }

        /****
         * ******************************************************
         */

        // 对比当前的数据集合，判断如果已经过期或者还没有的视频，就进行下载
        if (videoAdBeans == null || videoAdBeans.size() == 0) {
            // 添加下载视频集合
            // 检测没有过期的视频，将其加入下载列表
            int length = videoDetailResponses.size();
            for (int i = 0; i < length; i++) {
                VideoDetailResponse videoDetailResponse = videoDetailResponses.get(i);
                if (!isExpired(this, videoDetailResponse.tag)) {
                    addDownloadList(videoDetailResponse);
                } else {
                    LogCat.e("当前视频已经过期，不再处理。。。。。。。");
                }
            }

            LogCat.e("初次安装。。。。。。。。。" + downloadViews.size());
            // 此时表示没有缓存视频内容，需要下载
            initDownloadInfo();
        } else {
            LogCat.e("本地有记录,开始匹配服务器视频信息。。。。。。。。。");
            // 匹配数据
            matchingVideos();
        }

        // 开始计时，时间间隔5小时
        if (timer == null) {
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            doHttpGetEpisode();
                        }
                    });
                }
            }, REFRUSH_DURATION, REFRUSH_DURATION);
        }

    }

    @Override
    protected void onFailed(String errorMsg, String url) {
        LogCat.e("onFailed....");
//        if(!isFinishing()){
//            doHttpGetEpisode();
//        }
        if (timer == null) {
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            doHttpGetEpisode();
                        }
                    });
                }
            }, REFRUSH_DURATION, REFRUSH_DURATION);
        }
    }


    private void matchingVideos() {
        // 有数据的时候，需要判断数据是否过期
        // 根据日期来进行判断
        int length = videoDetailResponses.size();
        int size = videoAdBeans.size();
        ArrayList<Integer> outOfDateVideo = new ArrayList<>();
        // 对比数据
        for (int i = 0; i < length; i++) {
            VideoDetailResponse videoDetailResponse = videoDetailResponses.get(i);
            // 本地数据表中是否有该视频记录，如果没有需要添加到下载列表中
            boolean isLocal = false;
            // 开始匹配数据内容
            for (int j = 0; j < size; j++) {
                VideoAdBean videoAdBean = videoAdBeans.get(j);
                // 判断vid是否相同
                if (videoDetailResponse.vid.equals(videoAdBean.videoId)) {
                    LogCat.e("存在已经记录的视频。。。。。。。。。" + videoDetailResponse.name);
                    isLocal = true;
                    // 继续判断是否过期
                    if (isExpired(this, videoDetailResponse.tag)) {
                        // 删除数据表信息
                        AdDao.getInstance(MainActivity.this).delete(videoAdBean.videoId);
                        // 删除本地文件
                        new DeleteFileThread(videoAdBean.videoPath).run();
                        // 删除本地数据
                        outOfDateVideo.add(j);
                        LogCat.e("当前视频已经过期，需要删除。。。。。。。。。" + videoDetailResponse.name);
                        continue;
                    }


                    // 如果本地文件中不存在，则删除改记录，并添加到下载列表
                    if (TextUtils.isEmpty(videoAdBean.videoPath)) {
                        // 删除数据表信息
                        AdDao.getInstance(MainActivity.this).delete(videoAdBean.videoId);
                        // 删除本地数据
                        outOfDateVideo.add(j);
                        // 添加到下载列表
                        addDownloadList(videoDetailResponse);
                        LogCat.e("本地文件存放地址为空，需要重新下载");
                    } else {
                        File file = new File(videoAdBean.videoPath);
                        if (!file.exists()) {
                            LogCat.e("本地缓存视频已经删除，需要重新下载");
                            // 删除数据表信息
                            AdDao.getInstance(MainActivity.this).delete(videoAdBean.videoId);
                            // 删除本地数据
                            outOfDateVideo.add(j);
                            // 添加到下载列表
                            addDownloadList(videoDetailResponse);
                        }
                    }


                    // 判断是否下载完成,如果没有下载完成，需要继续下载
                    if (!AdDao.FLAG_DOWNLOAD_FINISHED.equals(videoAdBean.isDownloadFinish)) {
                        // 尚未下载完成
                        // 从播放列表删除
                        outOfDateVideo.add(j);
                        // 添加到下载列表
                        videoDetailResponse.isUseLocalUrl = true;
                        videoDetailResponse.localUrl = videoAdBean.videoUrl;
                        addDownloadList(videoDetailResponse);
                        LogCat.e("当前视频没有下载完成，需要继续下载。。。。。。。。。" + videoDetailResponse.name);
                    }


                }
            }
            // 如果没有当前视频，则也要添加到下载列表中去
            if (!isLocal) {
                LogCat.e("匹配结果--> 本地没有次视频，需要添加到下载列表。。。。。。。。。" + videoDetailResponse.name);
                // 如果没有过期，新的视频需要添加到下载列表中
                if (!isExpired(this, videoDetailResponse.tag)) {
                    addDownloadList(videoDetailResponse);
                }else {

                }

            }
        }


        // 删除本地过期数据
        if (videoAdBeans != null && size > 0) {
            int offsetPosition = 0;
            for (Integer i : outOfDateVideo) {
                if(i - offsetPosition < 0){
                    continue;
                }
                VideoAdBean videoAdBean = videoAdBeans.get(i - offsetPosition);
                offsetPosition++;
                LogCat.e("需要删除的视频" + videoAdBean.videoName);
                if (videoAdBean != null) {
                    videoAdBeans.remove(videoAdBean);
                    LogCat.e("成功删除本地没缓存的视频");
                }

            }
        }


        // 开始下载
        initDownloadInfo();

        // 开始播放
//        if (videoAdBeans == null || videoAdBeans.size() == 0) {
//            playVideo(DataUtils.getRawVideoUri(this, R.raw.video_test));
//        } else {
//            playVideo(videoAdBeans.get(0).videoPath);
//        }
    }





    private void initDownloadInfo() {
        if (downloadViews == null || downloadViews.size() == 0) {
            LogCat.e("已经没有下载任务了........");
            return;
        }
        VideoDetailResponse videoDetailResponse = downloadViews.get(0);

        if (videoDetailResponse.isDownloading) {
            return;
        }
        // 表明当前的视频正在下载
        videoDetailResponse.isDownloading = true;
        downLoadingVideo = new VideoAdBean();
        downLoadingVideo.videoId = videoDetailResponse.vid;
        downLoadingVideo.videoName = videoDetailResponse.name;
        downLoadingVideo.videoIndex = String.valueOf(downloadPosition);
        downLoadingVideo.videoStartTime = videoDetailResponse.vid;
        downLoadingVideo.videoEndTime = videoDetailResponse.tag;
        downLoadingVideo.isDownloadFinish = AdDao.FLAG_DOWNLOAD_UNFINISH;
        videoName = videoDetailResponse.name + ".mp4";
        downLoadingVideo.videoPath = saveDir + videoName;


        // 判断是否是继续下载，如果是继续下载使用本地数据库的存放地址
        if (videoDetailResponse.isUseLocalUrl) {
            LogCat.e("使用存储的url继续上次下载    " + videoDetailResponse.localUrl);
            downloadUrl = videoDetailResponse.localUrl;
            download();

        } else {
            if (videoDetailResponse.playInfo != null) {
                PlayInfoResponse playInfoResponse = videoDetailResponse.playInfo.get(0);
                // 去获取视频在实地址
                LogCat.e("获取新的播放地址。。。。。。。。");

                // 格式化当前的过期时间
                try {
                    LogCat.e("格式化时间。。。。。。。。" + videoDetailResponse.tag);
                    if(!videoDetailResponse.tag.startsWith("201")){
                        throw new NullPointerException();
                    }
                    Date date = DataUtils.getFormatTimeDate(FORMAT_VIDEO_AD_TIME, videoDetailResponse.tag);
                    doHttpGetCdnPath(this, playInfoResponse.remotevid, date);

//                    doHttpGetCdnPath(this, "/vlive/ws_zhejiang/SD/stream.m3u8", date);
                } catch (Exception e) {
                    e.printStackTrace();
                    LogCat.e("当前的视频下载初始化失败，删除该记录继续下载");
                    downloadViews.remove(videoDetailResponse);
                    initDownloadInfo();
                }
            }
        }


    }

    @Override
    protected void getVideoCdnPath(final String path) {
        super.getVideoCdnPath(path);
        if (isFinishing()) {
            return;
        }
        downloadUrl = path;

//        File file = new File(downLoadingVideo.videoPath);
//        if(file.exists()){
//            // 文件已经存在，添加到播放列表
//            // 判断文件大小
//            if(file.length() == ){
//
//            }
//
//            LogCat.e("已经存在文件了，直接播放。。。。。。。。");
//            downloadFinish();
//        }else {
        LogCat.e("开始下载。。。。。。。。" + path);
        // 一个视频一个视频的下载
        if (videoHandler == null) {
            videoHandler = new VideoHandler(this);
        }
        download();
//        }


    }

    @Override
    protected void onUpdateSuccess(UpdateResponse.UpdateInfoResponse updateInfo) {

    }

    private void download() {
        if (dlManager == null) {
            dlManager = DLManager.getInstance(this);
        }
        LogCat.e("本地文件目录：" + saveDir +  videoName);

        dlManager.dlStart(downloadUrl, saveDir, videoName, new DownloadListener() {

            @Override
            public void onPrepare() {
                LogCat.e("onPrepare............");
            }

            @Override
            public void onStart(String fileName, String realUrl, int fileLength) {
                LogCat.e("fileSize............" + ((fileLength / 1024) / 1024) + "M");
            }

            @Override
            public void onProgress(int progress) {
                super.onProgress(progress);
                LogCat.e("onProgress............" + ((progress / 1024) / 1024) + "M");
            }

            @Override
            public void onStop(int progress) {
                LogCat.e("onStop............");
            }

            @Override
            public void onFinish(File file) {
                LogCat.e("onFinish............");

                downloadFinish();


//                if (videoHandler != null && isFinishing() == false) {
//                    Message msg = videoHandler.obtainMessage(1);
//                    msg.obj = file.getAbsolutePath();
//                    videoHandler.sendMessage(msg);
//                }

            }

            @Override
            public void onError(int status, String error) {
                LogCat.e("onError............" + error);
                downloadFinish();

//                dlManager.dlCancel(downloadUrl);
//
//                download();
            }
        });
    }

    private void downloadFinish() {
        dlManager.dlCancel(downloadUrl);
        // 将视频信息插入数据库
        insertVideoAdState(AdDao.FLAG_DOWNLOAD_FINISHED);


        LogCat.e("doHandlerMessage添加新的数据。。。。。。。");
//                playVideo(String.valueOf(msg.obj));
        if (videoAdBeans == null) {
            videoAdBeans = new ArrayList<>();
        }
        // 添加到播放列表
        videoAdBeans.add(downLoadingVideo);


        // 查询是否还有未下载的任务，如果有 继续下载
        Object object = new Object();
        synchronized (object) {
            if (downloadViews != null && downloadViews.size() > 0) {
                downloadViews.remove(0);
            }

        }

        // 下载的index序号递增
        synchronized (object) {
            downloadPosition++;
        }

        if (downloadViews.size() != 0) {
            // 还有需要下载的视频，要继续下载
            LogCat.e("还有下载任务。。。。。。" + downloadViews.size());
            initDownloadInfo();
        } else {
            LogCat.e("已经全部下载完成了、、、、、、、");
        }

        downloadUrl = null;
    }


    private void addDownloadList(VideoDetailResponse videoDetailResponse) {
        if (downloadViews != null) {
            boolean isContains = false;
            for (VideoDetailResponse videoDetailResponse1 : downloadViews) {
                if (videoDetailResponse1.vid.equals(videoDetailResponse.vid)) {
                    isContains = true;
                    LogCat.e("已经在下载列表的视频不再重复添加" + videoDetailResponse.name);
                    break;
                }
            }
            if (!isContains) {
                downloadViews.add(videoDetailResponse);
            }

        }

    }

    private void insertVideoAdState(String state) {
        LogCat.e("准备插入数据。。。。。。。。");
        if (downLoadingVideo != null) {
            downLoadingVideo.videoUrl = downloadUrl;
            downLoadingVideo.isDownloadFinish = state;
            // 添加到数据表
            if (AdDao.getInstance(this).isAdded(downLoadingVideo.videoId)) {
                // 更新数据表
                LogCat.e("已经存在数据，需要更新。。。。。。。。");
                AdDao.getInstance(this).updateVideoAds(downLoadingVideo);
            } else {
                LogCat.e("开始插入数据。。。。。。。。");
                AdDao.getInstance(this).insertAd(downLoadingVideo);
            }
        }
    }


    @Override
    protected void onStop() {
        // 停止当前的下载
        if (dlManager != null && !TextUtils.isEmpty(downloadUrl)) {
            dlManager.dlStop(downloadUrl);

            if (isUpdate && !isUpdateFinish && updateInfo != null) {
                dlManager.dlStop(updateInfo.fileUrl);
            }
        }
        // 更新表
        if (downloadViews != null && downloadViews.size() != 0) {
            // 此时还有尚未下载的视频
            insertVideoAdState(AdDao.FLAG_DOWNLOAD_UNFINISH);
        }

        if (timer != null) {
            timer.cancel();
            timer = null;
        }

        // 计算离开的时候总时长
        try{
            long duration = System.currentTimeMillis() - startLong;
            long day = duration / (24 * 60 * 60 * 1000);
            long hour = (duration / (60 * 60 * 1000) - day * 24);
            long min = ((duration / (60 * 1000)) - day * 24 * 60 - hour * 60);
            long s = (duration / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - min * 60);
            String str = day + "天" + hour + "小时" + min + "分" + s + "秒";
            MobclickAgent.onEvent(this, "duration", str);
        }catch (Exception e){
            e.printStackTrace();
        }


        super.onStop();
    }


    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }


    UpdateResponse.UpdateInfoResponse updateInfo;
    boolean isUpdate;
    boolean isUpdateFinish;

    /**
     * 检查是否有版本更新
     */
    private void doHttpUpdate() {
        Map<String, String> map = new HashMap<>();
        map.put("platformId", String.valueOf("22"));
        if (!TextUtils.isEmpty(android.os.Build.MODEL)) {
            // 不为空
            try {
                map.put("modelNumber", URLEncoder.encode(android.os.Build.MODEL, "utf-8")); // 型号
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                map.put("modelNumber", android.os.Build.MODEL); // 型号
            }
        }
        try {
            ApplicationInfo appInfo = this.getPackageManager().getApplicationInfo(getPackageName(),
                    PackageManager.GET_META_DATA);
            if (appInfo != null) {
                map.put("brandNumber", appInfo.metaData.getString("UMENG_CHANNEL")); // 品牌
            }
        } catch (PackageManager.NameNotFoundException e1) {
            e1.printStackTrace();
            map.put("brandNumber", "unkown"); // 品牌
        }

        AlbnumHttpService.doHttpUpdateApk(this, map, new OnRequestListener<UpdateResponse>() {
            @Override
            public void onSuccess(UpdateResponse response, String url) {
                if (isFinishing()) {
                    return;
                }


                if (response == null || !(response instanceof UpdateResponse)) {
                    LogCat.e("升级数据出错，无法正常升级1。。。。。");
                    doError();
                    return;
                }

                if (response.resultForApk == null || !(response.resultForApk instanceof UpdateResponse.UpdateInfoResponse)) {
                    LogCat.e("升级数据出错，无法正常升级2。。。。。");
                    doError();
                    return;
                }

                if ("1".equals(response.status) == false) {
                    LogCat.e("升级接口的status == 0。。。。。");
                    doError();
                    return;
                }
                updateInfo = response.resultForApk;
                // 获取当前最新版本号
                if (TextUtils.isEmpty(updateInfo.versionCode) == false) {
                    double netVersonCode = Integer.parseInt(updateInfo.versionCode);
                    // 检测是否要升级
                    try {
                        if (DataUtils.getAppVersion(MainActivity.this) < netVersonCode) { // 升级
                            // 升级
                            // 下载最新安装包，下载完成后，提示安装
                            LogCat.e("需要升级。。。。。");

                            // 去下载当前的apk
                            downloadApk();
                            isUpdate = true;
                        } else {
                            // 不升级
                            LogCat.e("无需升级。。。。。");
                            doError();
                        }
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                        LogCat.e("判断升级过程中出错。。。。。");
                        doError();
                    }
                } else {
                    // 不升级
                    LogCat.e("升级版本为null。。。。。");
                    doError();
                }

            }

            private void doError() {
                if (!isFinishing()) {
                    // 做不升级处理, 继续请求广告视频列表
                    doHttpGetEpisode();
                }

            }

            @Override
            public void onError(String errorMsg, String url) {
                // 升级失败
                LogCat.e("请求接口出错，无法升级。。。。。");
                doError();
            }
        });
    }


    private void downloadApk() {
        if (dlManager == null) {
            dlManager = DLManager.getInstance(this);
        }


        dlManager.dlStart(updateInfo.fileUrl, saveDir + "chinaRestaurant.apk", new DownloadListener() {
            @Override
            public void onPrepare() {
                LogCat.e("开始下载升级包。。。。。");
            }

            @Override
            public void onStart(String fileName, String realUrl, int fileLength) {
                LogCat.e("fileSize............" + ((fileLength / 1024) / 1024) + "M");
            }

            @Override
            public void onProgress(int progress) {
                super.onProgress(progress);
                LogCat.e("onProgress............" + ((progress / 1024) / 1024) + "M");
            }

            @Override
            public void onStop(int progress) {
                LogCat.e("onStop............");
            }

            @Override
            public void onFinish(File file) {
                LogCat.e("onFinish............");
                // 提示安装
                if (dlManager == null) {
                    dlManager = DLManager.getInstance(MainActivity.this);
                }
                dlManager.dlCancel(saveDir + "chinaRestaurant.apk");
                installApk(MainActivity.this, file.getAbsolutePath());
                isUpdateFinish = true;
                // 关闭当前app
                finish();
            }

            @Override
            public void onError(int status, String error) {
                DLManager.getInstance(MainActivity.this).dlCancel(updateInfo.fileUrl);
                downloadApk();

            }
        });

    }



}
