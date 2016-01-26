package com.gochinatv.ad;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;

import com.gochinatv.ad.base.BaseActivity;
import com.gochinatv.ad.db.AdDao;
import com.gochinatv.ad.db.VideoAdBean;
import com.gochinatv.ad.interfaces.DownloadListener;
import com.gochinatv.ad.tools.DataUtils;
import com.gochinatv.ad.tools.LogCat;
import com.httputils.http.response.PlayInfoResponse;
import com.httputils.http.response.UpdateResponse;
import com.httputils.http.response.VideoDetailListResponse;
import com.httputils.http.response.VideoDetailResponse;
import com.umeng.analytics.AnalyticsConfig;
import com.umeng.analytics.MobclickAgent;
import com.vego.player.MeasureVideoView;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import cn.aigestudio.downloader.bizs.DLManager;

/**
 * Created by fq_mbp on 16/1/8.
 */
public class TestActivity extends BaseActivity {

    private MeasureVideoView videoView;


    private int position;
    private int downloadPosition;
    private String saveDir;

    private VideoHandler videoHandler;

    private static final String FORMAT_VIDEO_AD_TIME = "yyyyMMddHHmmss";
    private static final String FILE_DIRECTROY = "gochinatv_ad";

//    private static final int REFRUSH_DURATION = 5 * 60 * 60 * 1000;

    private static final int REFRUSH_DURATION = 5 * 60 * 60 * 1000;


    private Timer timer;

    private long startLong;
    private DLManager dlManager;

    /**
     * 本地数据表
     */
    private ArrayList<VideoAdBean> localVideoTable;

    private ArrayList<VideoAdBean> playVideoTable;
    /**
     * 服务器数据表
     */
    private ArrayList<VideoDetailResponse> videoDetailResponses;
    /**
     * 下载数据表
     */
    private ArrayList<VideoDetailResponse> downloadViews;

    private VideoAdBean downLoadingVideo;

    private Timer refrushTimer;

    private LinearLayout loading;

//    private DownloadManager downloadManager;
//    private SharedPreferences prefs;
//    private static final String DL_ID = "222";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MobclickAgent.setDebugMode(false);
        /** 设置是否对日志信息进行加密, 默认false(不加密). */
        AnalyticsConfig.enableEncrypt(true);
        MobclickAgent.openActivityDurationTrack(false);
        setContentView(R.layout.activity_main);
        initView();
        init();
        bindEvent();

//        LogCat.e("getDeviceInfo: " + getDeviceInfo(this));
    }


//    public static String getDeviceInfo(Context context) {
//        try{
//            org.json.JSONObject json = new org.json.JSONObject();
//            android.telephony.TelephonyManager tm = (android.telephony.TelephonyManager) context
//                    .getSystemService(Context.TELEPHONY_SERVICE);
//
//            String device_id = tm.getDeviceId();
//
//            android.net.wifi.WifiManager wifi = (android.net.wifi.WifiManager) context.getSystemService(Context.WIFI_SERVICE);
//
//            String mac = wifi.getConnectionInfo().getMacAddress();
//            json.put("mac", mac);
//
//            if( TextUtils.isEmpty(device_id) ){
//                device_id = mac;
//            }
//
//            if( TextUtils.isEmpty(device_id) ){
//                device_id = android.provider.Settings.Secure.getString(context.getContentResolver(),android.provider.Settings.Secure.ANDROID_ID);
//            }
//
//            json.put("device_id", device_id);
//
//            return json.toString();
//        }catch(Exception e){
//            e.printStackTrace();
//        }
//        return null;
//    }


    private void initView() {
        videoView = (MeasureVideoView) findViewById(R.id.videoView);

        loading = (LinearLayout) findViewById(R.id.loading);
    }

    private void init() {
        // 以上就显示加载状态，只有开始播放的时候隐藏
        showLoading();
        startLong = System.currentTimeMillis();
        if (android.os.Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            LogCat.e("sd卡状态可用。。。。。。。");
        }
        // 实力化本地下载目录
        File file = Environment.getExternalStoragePublicDirectory(FILE_DIRECTROY);

        if (!(file.exists() && file.isDirectory())) {
            LogCat.e("当前文件路径不存在，主动创建");
            file.mkdirs();
        }
        saveDir = file.getAbsolutePath() + "/";
        LogCat.e("现在的下载地址是： " + saveDir);


//        downloadObserver = new DownloadChangeObserver();
//        getContentResolver().registerContentObserver(DownloadManagerPro.CONTENT_URI, true,
//                downloadObserver);
//        downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
//        prefs = PreferenceManager.getDefaultSharedPreferences(this);
//        registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
//
//        downloadManagerPro = new DownloadManagerPro(downloadManager);
//        handler = new MyHandler();


        // 创建播放列表
        playVideoTable = new ArrayList<>();
        // 查询数据库，看是否有播放记录
        localVideoTable = AdDao.getInstance(this).queryVideoAds();

//        if(localVideoTable ==null || localVideoTable.size() == 0){
//            localVideoTable = new ArrayList<>();
//            VideoAdBean videoAdBean = new VideoAdBean();
//            videoAdBean.videoName = "111111";
//            videoAdBean.videoPath = "111111";
//            videoAdBean.videoEndTime = "20160202235959";
//            videoAdBean.videoId = "111111";
//            localVideoTable.add(videoAdBean);
//
//            VideoAdBean videoAdBean1 = new VideoAdBean();
//            videoAdBean1.videoName = "2222222";
//            videoAdBean1.videoPath = "2222222";
//            videoAdBean1.videoEndTime = "20160102235959";
//            videoAdBean1.videoId = "2222222";
//            localVideoTable.add(videoAdBean1);
//        }


        // 此时需要先播放视频
        if (localVideoTable == null || localVideoTable.size() == 0) {
            // 此时表示没有本地数据，所有服务器视频都要下载
            // 先播放本地的预置视频
            LogCat.e("当前没有缓存视频，播放本地的。。。。。");
            VideoAdBean videoAdBean = new VideoAdBean();
            videoAdBean.videoName = "预置片";
            videoAdBean.videoPath = DataUtils.getRawVideoUri(this, R.raw.video_test);
            videoAdBean.videoId = "0";
//            playVideoTable.add(0, new VideoInfo("预置片", DataUtils.getRawVideoUri(this, R.raw.video_test)));
            playVideoTable.add(0, videoAdBean);
        } else {
            // 删除本地已经过期的视频，或者不合格的视频
            LogCat.e("有缓存文件，开始查找本地可播放文件。。。。。");
            checkLocalData();
            // 添加播放列表
            if (localVideoTable.size() == 0) {
                LogCat.e("经过处理，所有文件都被删除了。。。。。");
//                playVideoTable.add(0, new VideoInfo("预置片", DataUtils.getRawVideoUri(this, R.raw.video_test)));
                VideoAdBean videoAdBean = new VideoAdBean();
                videoAdBean.videoName = "预置片";
                videoAdBean.videoPath = DataUtils.getRawVideoUri(this, R.raw.video_test);
                videoAdBean.videoId = "0";
                playVideoTable.add(0, videoAdBean);
            } else {
                for (VideoAdBean videoAdBeanq : localVideoTable) {
//                    VideoInfo videoInfo = new VideoInfo(videoAdBean.videoName, videoAdBean.videoPath);
//                    playVideoTable.add(videoInfo);
                    playVideoTable.add(videoAdBeanq);
                }
            }

        }
        // 播放第一个视频
        LogCat.e("默认开始播放的视频名称：" + playVideoTable.get(0).videoName);
        playVideo(playVideoTable.get(0).videoPath);

        refrushTimer = new Timer();

        checkNet();

        // 删除目录下所有的安装包
//        new DeleteApkThread(saveDir + "chinaRestaurant").start();

    }

    class VideoInfo {
        public String videoUrl;
        public String videoName;

        public VideoInfo(String videoName, String videoUrl) {
            this.videoName = videoName;
            this.videoUrl = videoUrl;
        }
    }

    private void checkNet() {
        refrushTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                LogCat.e("正在检测是否联网。。。。。");
                if (DataUtils.isNetworkConnected(TestActivity.this)) {
                    // 先去请求服务器，查看视频列表
                    doHttpUpdate(TestActivity.this);
                    refrushTimer.cancel();
                    refrushTimer = null;
                    LogCat.e("已经联网。。。。。");
                } else {
                    LogCat.e("没有联网。。。。。继续检查");
                }
            }
        }, 1000, 10 * 1100);
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
                if (isFinishing()) {
                    return;
                }
                LogCat.e("视频播放完成。。。。。。");
                showLoading();
                try {
                    VideoAdBean videoAdBean = playVideoTable.get(position);
                    if (!DataUtils.getRawVideoUri(TestActivity.this, R.raw.video_test).equals(videoAdBean.videoPath)) {
                        LogCat.e("添加一次视频播放" + videoAdBean.videoName);
                        MobclickAgent.onEvent(TestActivity.this, "video_loop_times", videoAdBean.videoName);
                    }


//                    VideoInfo videoInfo = playVideoTable.get(position);
//                    if(!DataUtils.getRawVideoUri(TestActivity.this, R.raw.video_test).equals(videoInfo.videoUrl)){
//                        LogCat.e("添加一次视频播放" + videoInfo.videoName);
//                        MobclickAgent.onEvent(TestActivity.this, "video_loop_times", videoInfo.videoName);
//                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // 播放下一个视频
                playNext();
            }
        });

        videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {

//                VideoInfo videoInfo = playVideoTable.get(position);
                LogCat.e("onError...........");
//                LogCat.e("从当前播放列表中删除该视频。。。。。。");


                VideoAdBean videoAdBean = playVideoTable.get(position);
                if (videoAdBean != null) {
                    LogCat.e("onError...........视频播放失败了: " + videoAdBean.videoName);
                    LogCat.e("删除当前视频的本地文件。。。。。。");
                    new DeleteFileThread(saveDir, videoAdBean.videoName).start();

                    LogCat.e("从本地数据库中删除该记录。。。。。。");
                    AdDao.getInstance(TestActivity.this).delete(videoAdBean.videoId);

                    LogCat.e("从本地表中删除该记录。。。。。。");
                    for (VideoAdBean videoAdBean1 : localVideoTable) {
                        if (videoAdBean1.videoId.equals(videoAdBean.videoId)) {
                            LogCat.e("执行删除本地表记录操作。。。。。。");
                            localVideoTable.remove(videoAdBean1);
                            break;
                        }
                    }

                    LogCat.e("从当前播放列表中删除该视频。。。。。。");
                    playVideoTable.remove(position);
                }


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

    @Override
    protected void onUpdateSuccess(UpdateResponse.UpdateInfoResponse updateInfo) {
        isUpdate = true;
        downloadApk(updateInfo);
    }

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
        // 创建下载列表
        if (downloadViews == null) {
            downloadViews = new ArrayList<>(videoDetailResponses.size());
        }

        int length = videoDetailResponses.size();
        if (localVideoTable == null || localVideoTable.size() == 0) {
            // 本地表不存在，此时需要新创建
            for (int i = 0; i < length; i++) {
                VideoDetailResponse videoDetailResponse = videoDetailResponses.get(i);
//                if(i == 0){
//                    videoDetailResponse.tag = "20160131235959";
//                }
                if (!isExpired(this, videoDetailResponse.tag)) {
                    // 没有过期，需要加入到下载列表
                    LogCat.e("需要下载的视频 " + videoDetailResponse.name);
                    addDownloadList(videoDetailResponse);
                } else {
                    LogCat.e(videoDetailResponse.name + "  已经过期，不再处理。。。。。。。");

                }

            }

            LogCat.e("初次安装。。。。。。。。。" + downloadViews.size());
            // 此时表示没有缓存视频内容，需要下载
        } else {
            LogCat.e("本地有记录,开始匹配服务器视频信息。。。。。。。。。" + AdDao.getInstance(this).queryVideoAds().size());
            ArrayList<Integer> serverNoRecords = new ArrayList<>();
            // 删除本地有的视频信息而服务器没有的视频
            int localSize = localVideoTable.size();
            for (int i = 0; i < localSize; i++) {
                VideoAdBean videoAdBean = localVideoTable.get(i);
                boolean isServerHas = false;
//                LogCat.e("继续查找本地记录与服务器匹配");
                for (int j = 0; j < length; j++) {
                    VideoDetailResponse videoDetailResponse = videoDetailResponses.get(j);
                    if (videoDetailResponse.vid.equals(videoAdBean.videoId)) {
                        isServerHas = true;
                        break;
                    }
                }
                // 此时说明，本地记录的表已经不再服务器了，需要删除当前记录
                if (!isServerHas) {
                    serverNoRecords.add(i);
                }

            }


            if (localVideoTable.size() != 0) {
                int sizeD = serverNoRecords.size();
                LogCat.e("服务器已经不存在的本地记录的个数：" + sizeD);
                for (int i = sizeD - 1; i >= 0; i--) {
                    if (i <= sizeD) {
                        int index = serverNoRecords.get(i);
                        if (index < localVideoTable.size()) {
                            VideoAdBean videoAdBean = localVideoTable.get(index);
                            // 删除本地记录表和本地的文件
                            LogCat.e("删除本地记录表和本地的文件");
                            AdDao.getInstance(this).delete(videoAdBean.videoId);
                            new DeleteFileThread(saveDir, videoAdBean.videoName).start();
                            localVideoTable.remove(index);
                        }
                    }
                }
            }


            // 匹配数据
            int size = localVideoTable.size();
            //
            for (int i = 0; i < length; i++) {
                //
                VideoDetailResponse videoDetailResponse = videoDetailResponses.get(i);
                boolean isContain = false;
                for (int j = 0; j < size; j++) {
                    VideoAdBean videoAdBean = localVideoTable.get(j);
                    if (videoAdBean.videoId.equals(videoDetailResponse.vid)) {
                        isContain = true;
//                        LogCat.e("不需要下载的视频 。。。。。。。。。" + videoAdBean.videoName);
                        if (isExpired(this, videoDetailResponse.tag)) {
                            LogCat.e("过期的视频的名称 。。。。。。。。。" + videoDetailResponse.name);
                            LogCat.e("过期的视频的vid 。。。。。。。。。" + videoDetailResponse.vid);
                            // 从播放列表删除记录
                            LogCat.e("vid : " + videoAdBean.videoId);
                            playVideoTable.remove(videoAdBean);
                            LogCat.e("从播放列表中删除过期视频。。。。。。。。。" + videoAdBean.videoName);
                            // 提出本地数据记录
                            AdDao.getInstance(this).delete(videoDetailResponse.vid);
                            new DeleteFileThread(saveDir, videoAdBean.videoName).start();
                        }


                        break;
                    }
                }
                if (!isContain) {
                    // 提出过期的视频
                    if (!isExpired(this, videoDetailResponse.tag)) {
                        // 此时表示当前视频需要下载，添加到下载列表
                        addDownloadList(videoDetailResponse);
                        LogCat.e("需要下载的视频 。。。。。。。。。" + videoDetailResponse.name);
                    } else {
                        LogCat.e("过期的视频的名称 。。。。。。。。。" + videoDetailResponse.name);
                        LogCat.e("过期的视频的vid 。。。。。。。。。" + videoDetailResponse.vid);
                        // 从播放列表删除记录
                        for (VideoAdBean videoAdBean : localVideoTable) {
                            LogCat.e("vid : " + videoAdBean.videoId);
                            if (videoAdBean.videoId.equals(videoDetailResponse.vid)) {
                                playVideoTable.remove(videoAdBean);
                                LogCat.e("从播放列表中删除过期视频。。。。。。。。。" + videoAdBean.videoName);
                            }
                        }
                        // 提出本地数据记录
                        AdDao.getInstance(this).delete(videoDetailResponse.vid);


                    }
                }
            }
        }

        // 开始下载
        initDownloadInfo();

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
    protected void getVideoCdnPath(String path) {
        super.getVideoCdnPath(path);
        if (isFinishing()) {
            return;
        }
        LogCat.e("开始下载。。。。。。。。" + path);
        // 一个视频一个视频的下载

        download(path);


    }

    private void checkLocalData() {
        int size = localVideoTable.size();
        ArrayList<Integer> deleteVideos = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            VideoAdBean videoAdBean = localVideoTable.get(i);
            // 本地文件路径不存在或者已经过期
            if (TextUtils.isEmpty(videoAdBean.videoPath)) {
                AdDao.getInstance(this).delete(videoAdBean.videoId);
                deleteVideos.add(i);
                DLManager.getInstance(this).dlCancel(videoAdBean.videoUrl);
                LogCat.e("文件路径不存在，。。。。。");
                continue;
            }
            // 本地文件已经删除的
//            File fileD = new File(saveDir);
//            if(fileD.exists()){
//                for(File file : fileD.listFiles()){
//                    LogCat.e("fileName : " + file.getAbsolutePath());
//                }
//            }


            File file = new File(saveDir, videoAdBean.videoName);
            if (!file.exists()) {
                LogCat.e("不存在的文件的路径： " + file.getAbsolutePath());
                AdDao.getInstance(this).delete(videoAdBean.videoId);
                deleteVideos.add(i);
                DLManager.getInstance(this).dlCancel(videoAdBean.videoUrl);
                LogCat.e("文件不存在，。。。。。");
                continue;
            }
            // 已经过期的视频
            if (isExpired(this, videoAdBean.videoEndTime)) {
                AdDao.getInstance(this).delete(videoAdBean.videoId);
                new DeleteFileThread(saveDir, videoAdBean.videoName).start();
                deleteVideos.add(i);
                DLManager.getInstance(this).dlCancel(videoAdBean.videoUrl);
                LogCat.e("数据已经过期，。。。。。");
                continue;
            }

            // 没有下载完成的
            if (videoAdBean.isDownloadFinish == AdDao.FLAG_DOWNLOAD_UNFINISH) {
                AdDao.getInstance(this).delete(videoAdBean.videoId);
                new DeleteFileThread(saveDir, videoAdBean.videoName).start();
                deleteVideos.add(i);
                DLManager.getInstance(this).dlCancel(videoAdBean.videoUrl);
            }

        }
        LogCat.e("没有处理前的数据count：" + localVideoTable.size());


        // 删除本地记录
        if (localVideoTable.size() != 0) {
            int sizeD = deleteVideos.size();
            for (int i = sizeD - 1; i >= 0; i--) {
                if (i <= sizeD) {
                    int index = deleteVideos.get(i);
                    if (index < localVideoTable.size()) {
                        localVideoTable.remove(index);
                    }
                }
            }
        }


        LogCat.e("处理后的数据count：" + localVideoTable.size());

    }


    private void playNext() {
        LogCat.e("playNext。。。。。");
        if (playVideoTable != null && playVideoTable.size() > 0) {
            position++;
            if (position >= playVideoTable.size()) {
                position = 0;
                LogCat.e("此时表示已经将所有视频循环一遍了。。。。。");
            }
            LogCat.e("当前播放第   " + position + "   个视频");

            VideoAdBean videoAdBean = playVideoTable.get(position);
            if (TextUtils.isEmpty(videoAdBean.videoPath)) {
                playVideoTable.remove(videoAdBean);
                playNext();
                LogCat.e("当前视频的path为 null。。。。。");
                return;
            }

            if (!DataUtils.getRawVideoUri(this, R.raw.video_test).equals(videoAdBean.videoPath)) {
                File file = new File(saveDir, videoAdBean.videoName);
                if (!file.exists()) {
                    playVideoTable.remove(videoAdBean);
                    playNext();
                    LogCat.e("当前视频的视频文件不存在。。。。。" + file.getAbsolutePath());
                    return;
                }
            }
            playVideo(videoAdBean.videoPath);
            LogCat.e(videoAdBean.videoName + "视频地址。。。。。" + videoAdBean.videoName);


//            VideoInfo videoInfo = playVideoTable.get(position);
//            if (TextUtils.isEmpty(videoInfo.videoUrl)) {
//                playVideoTable.remove(videoInfo);
//                playNext();
//                LogCat.e("当前视频的path为 null。。。。。");
//                return;
//            }
//
//            if(!DataUtils.getRawVideoUri(this, R.raw.video_test).equals(videoInfo.videoUrl)){
//                File file = new File(videoInfo.videoUrl);
//                if (!file.exists()) {
//                    playVideoTable.remove(videoInfo);
//                    playNext();
//                    LogCat.e("当前视频的视频文件不存在。。。。。");
//                    return;
//                }
//            }
//            playVideo(videoInfo.videoUrl);
//            LogCat.e(videoInfo.videoName + "视频地址。。。。。" + videoInfo.videoName);
        } else {
            position = 0;
//            VideoInfo videoInfo = new VideoInfo("预置片", DataUtils.getRawVideoUri(this, R.raw.video_test));
//            playVideoTable.add(videoInfo);

            VideoAdBean videoAdBean = new VideoAdBean();
            videoAdBean.videoName = "预置片";
            videoAdBean.videoPath = DataUtils.getRawVideoUri(this, R.raw.video_test);
            videoAdBean.videoId = "0";
            playVideoTable.add(0, videoAdBean);


            playVideo(DataUtils.getRawVideoUri(this, R.raw.video_test));
        }
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


    private void initDownloadInfo() {
        if (downloadViews == null || downloadViews.size() == 0) {
            LogCat.e("已经没有下载任务了........");
            return;
        }
        VideoDetailResponse videoDetailResponse = downloadViews.get(0);

        if (videoDetailResponse.isDownloading) {
            LogCat.e("当前任务正在下载中。。。。。。。");
            return;
        }
        // 表明当前的视频正在下载
        videoDetailResponse.isDownloading = true;
        downLoadingVideo = new VideoAdBean();
        downLoadingVideo.videoId = videoDetailResponse.vid;
        downLoadingVideo.videoName = videoDetailResponse.name + ".mp4";
        downLoadingVideo.videoStartTime = videoDetailResponse.vid;
        downLoadingVideo.videoEndTime = videoDetailResponse.tag;
        downLoadingVideo.isDownloadFinish = AdDao.FLAG_DOWNLOAD_UNFINISH;
        downLoadingVideo.videoPath = saveDir + videoDetailResponse.name + ".mp4";


        // 判断是否是继续下载，如果是继续下载使用本地数据库的存放地址
        if (videoDetailResponse.playInfo != null) {
            PlayInfoResponse playInfoResponse = videoDetailResponse.playInfo.get(0);
            // 去获取视频在实地址
            LogCat.e("获取新的播放地址。。。。。。。。");
            // 格式化当前的过期时间
            try {
                LogCat.e("格式化时间。。。。。。。。" + videoDetailResponse.tag);
                if (!videoDetailResponse.tag.startsWith("201")) {
                    throw new NullPointerException();
                }
                Date date = DataUtils.getFormatTimeDate(FORMAT_VIDEO_AD_TIME, videoDetailResponse.tag);
                doHttpGetCdnPath(this, playInfoResponse.remotevid, date);
//                    doHttpGetCdnPath(this, "/2015/08/26/8496868D000070DA.mp4", date);
            } catch (Exception e) {
                e.printStackTrace();
                LogCat.e("当前的视频下载初始化失败，删除该记录继续下载");
                downloadViews.remove(videoDetailResponse);
                initDownloadInfo();
            }
        }else {
            LogCat.e("playinfo null 无法执行下载");
        }


    }

    @Override
    protected void onVideoCdnError(String path) {
        super.onVideoCdnError(path);
        LogCat.e("当前视频下载失败，删除当前视频下载信息");
        if (downloadViews != null && downloadViews.size() > 0) {
            downloadViews.remove(0);
        }

        if (downloadViews.size() > 0) {
            LogCat.e("当前视频下载失败，继续下一个视频");
            initDownloadInfo();
        }


    }

    boolean isUpdate;
    boolean isUpdateFinish;


    private void downloadApk(final UpdateResponse.UpdateInfoResponse updateInfo) {
        if (dlManager == null) {
            dlManager = DLManager.getInstance(this);
        }
        delAllFile(saveDir + "chinaRestaurant");

        downloadUrl = updateInfo.fileUrl;

        dlManager.dlCancel(updateInfo.fileUrl);

        dlManager.dlStart(updateInfo.fileUrl, saveDir + "chinaRestaurant", new DownloadListener() {
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
                    dlManager = DLManager.getInstance(TestActivity.this);
                }
                dlManager.dlCancel(saveDir + "chinaRestaurant.apk");
                installApk(TestActivity.this, file.getAbsolutePath());
                isUpdateFinish = true;
                // 关闭当前app
                finish();
            }

            @Override
            public void onError(int status, String error) {
                doHttpGetEpisode();

            }
        });

    }


    private void playLocalVideo() {
        if (videoView != null && !isFinishing()) {
            LogCat.e("开始播放本地视频........");
            playVideo(DataUtils.getRawVideoUri(this, R.raw.video_test));
        }
    }

    // 播放视频
    private void playVideo(String url) {
        if (!TextUtils.isEmpty(url) && videoView != null) {
            videoView.setVideoPath(url);


        }
    }


    String downloadUrl;

    long downloadingId;

    private void download(final String url) {

        downloadUrl = url;
//        downloadingId = doDownLoad();

        if (dlManager == null) {
            dlManager = DLManager.getInstance(this);
        }
        downloadUrl = url;
        LogCat.e("本地文件目录：" + saveDir + downLoadingVideo.videoName);
        dlManager.dlCancel(url);

        dlManager.dlStart(url, saveDir, downLoadingVideo.videoName, new DownloadListener() {

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
                if(file.exists()){
                    LogCat.e("文件创建成功，file:®"+file.getName());
                    LogCat.e("onFinish............");
                }

                downloadFinish(url, AdDao.FLAG_DOWNLOAD_FINISHED);
            }

            @Override
            public void onError(int status, String error) {
                LogCat.e("onError............" + error);
                downloadFinish(url, AdDao.FLAG_DOWNLOAD_UNFINISH);
            }
        });
    }

    private void downloadFinish(String url, String state) {
//        dlManager.dlCancel(url);
        // 将视频信息插入数据库
        insertVideoAdState(state, url);
        // 添加到播放列表
        if (state.equals(AdDao.FLAG_DOWNLOAD_FINISHED)) {
            // 如果当第一个视频是默认视频的时候，删除
            if (playVideoTable != null && playVideoTable.size() == 1) {
                VideoAdBean videoAdBean = playVideoTable.get(0);
                LogCat.e("下载完成视频。。。。。。。" + videoAdBean.videoName);
                if (DataUtils.getRawVideoUri(this, R.raw.video_test).equals(videoAdBean.videoPath)) {
                    playVideoTable.remove(0);
                }
//                VideoInfo videoInfo = playVideoTable.get(0);
//                if(DataUtils.getRawVideoUri(this, R.raw.video_test).equals(videoInfo.videoUrl)){
//                    playVideoTable.remove(0);
//                }
            }

            if (!downLoadingVideo.videoPath.equals(DataUtils.getRawVideoUri(this, R.raw.video_test))) {
//                VideoInfo videoInfo = new VideoInfo(downLoadingVideo.videoName , downLoadingVideo.videoPath);
//                playVideoTable.add(videoInfo);


                playVideoTable.add(downLoadingVideo);


                localVideoTable.add(downLoadingVideo);
            }
            // 查询是否还有未下载的任务，如果有 继续下载
            Object object = new Object();
            synchronized (object) {
                if (downloadViews != null && downloadViews.size() > 0) {
                    downloadViews.remove(0);
                }

            }
        }else {
            // 查询是否还有未下载的任务，如果有 继续下载
            Object object = new Object();
            synchronized (object) {
                if (downloadViews != null && downloadViews.size() > 0) {
                    int count = downloadViews.size() - 1;
                    VideoDetailResponse videoDetailResponse = downloadViews.get(0);
                    videoDetailResponse.isDownloading = false;
                    downloadViews.add(count, videoDetailResponse);
                    downloadViews.remove(0);
                    new DeleteFileThread(saveDir, videoDetailResponse.name + ".mp4").start();
                }

            }
        }



        if (downloadViews.size() != 0) {
            // 还有需要下载的视频，要继续下载
            LogCat.e("还有下载任务。。。。。。" + downloadViews.size());
            initDownloadInfo();
        } else {
            LogCat.e("已经全部下载完成了、、、、、、、");
        }

    }

    private void insertVideoAdState(String state, String url) {
        LogCat.e("准备插入数据。。。。。。。。");
        if (downLoadingVideo != null && state.equals(AdDao.FLAG_DOWNLOAD_FINISHED)) {
            downLoadingVideo.videoUrl = url;
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
        super.onStop();
        // 停止当前的下载
        if (dlManager != null && !TextUtils.isEmpty(downloadUrl)) {
            dlManager.dlCancel(downloadUrl);
        }


//        unregisterReceiver(receiver);
//        if (downloadManager != null) {
//            downloadManager.remove(downloadingId);
//        }


        if (timer != null) {
            timer.cancel();
            timer = null;
        }

        // 计算离开的时候总时长
        try {
            long duration = System.currentTimeMillis() - startLong;
            long day = duration / (24 * 60 * 60 * 1000);
            long hour = (duration / (60 * 60 * 1000) - day * 24);
            long min = ((duration / (60 * 1000)) - day * 24 * 60 - hour * 60);
            long s = (duration / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - min * 60);
            String str = day + "天" + hour + "小时" + min + "分" + s + "秒";
            MobclickAgent.onEvent(this, "video_duration", str);
            LogCat.e("上报开机时长。。。。。。。。");
        } catch (Exception e) {
            e.printStackTrace();
        }


//        getContentResolver().unregisterContentObserver(downloadObserver);

    }

    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
        MobclickAgent.onPageStart("TestActivity"); // 统计页面
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
        MobclickAgent.onPageEnd("TestActivity"); // 保证 onPageEnd 在onPause
    }


//    private BroadcastReceiver receiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            //这里可以取得下载的id，这样就可以知道哪个文件下载完成了。适用与多个下载任务的监听
//            Log.e("intent", "" + intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0));
//            queryDownloadStatus();
//        }
//    };
//
//
//    private long queryDownloadStatus() {
//        LogCat.e("queryDownloadStatus........");
//        DownloadManager.Query query = new DownloadManager.Query();
//        query.setFilterById(downloadingId);
//        Cursor c = downloadManager.query(query);
//
//        if (c.moveToFirst()) {
//            int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
//            LogCat.e("download status................... " + status);
//            switch (status) {
//                case DownloadManager.STATUS_PAUSED:
//                    LogCat.e("STATUS_PAUSED");
//                case DownloadManager.STATUS_PENDING:
//                    LogCat.e("STATUS_PENDING");
//                case DownloadManager.STATUS_RUNNING:
//                    //正在下载，不做任何事情
//                    LogCat.e("STATUS_RUNNING");
//                    break;
//                case DownloadManager.STATUS_SUCCESSFUL:
//                    //完成
//                    LogCat.e("下载完成");
//
//                    downloadFinish("", AdDao.FLAG_DOWNLOAD_FINISHED);
//
//                    break;
//                case DownloadManager.STATUS_FAILED:
//                    //清除已下载的内容，重新下载
//                    LogCat.e("STATUS_FAILED");
//                    downloadManager.remove(downloadingId);
//                    downloadingId = doDownLoad();
//
//                    break;
//            }
//        }
//        return downloadingId;
//    }
//
//
//
//    private long doDownLoad() {
//        LogCat.e("doDownLoad......" + downLoadingVideo.videoName);
////        if(!prefs.contains(DL_ID)) {
////        downloadUrl = "http://developer.android.com/shareables/icon_templates-v4.0.zip";
//        //®
////        downloadUrl = downloadUrl.split("\\u003F")[0];
//        LogCat.e("下载地址：" + downloadUrl);
//
//
//
//        Uri resource = Uri.parse(encodeGB(downloadUrl+ "tttt.mp4"));
//        DownloadManager.Request request = new DownloadManager.Request(resource);
//        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
//        request.setAllowedOverRoaming(false);
//        //设置文件类型
////        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
////        String mimeString = mimeTypeMap.getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(downloadUrl));
////         LogCat.e("mimeString.............." + mimeString);
////        request.setMimeType(mimeString);
//
//
//        //在通知栏中显示
//        request.setShowRunningNotification(false);
//        request.setVisibleInDownloadsUi(false);
//        //sdcard的目录下的download文件夹
//
////                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_MOVIES, fileName);
////        LogCat.e("file 处理中文： " + encodeGB(downLoadingVideo.videoName));
//        request.setDestinationInExternalPublicDir(FILE_DIRECTROY, downLoadingVideo.videoName);
//        long id = downloadManager.enqueue(request);
//        //保存id
////        prefs.edit().putLong(DL_ID, id).commit();
//        LogCat.e("开始下载...........141414: " + id);
//        return id;
////        } else {
////            //下载已经开始，检查状态
////            return queryDownloadStatus();
////        }
//
//    }
//
//
//    class DownloadChangeObserver extends ContentObserver {
//
//        public DownloadChangeObserver() {
//            super(handler);
//        }
//
//        @Override
//        public void onChange(boolean selfChange) {
//            updateView();
//        }
//
//    }
//
//    private DownloadChangeObserver downloadObserver;
//    private DownloadManagerPro downloadManagerPro;
//    private MyHandler handler;
//
//    public void updateView() {
//        int[] bytesAndStatus = downloadManagerPro.getBytesAndStatus(downloadingId);
//        handler.sendMessage(handler.obtainMessage(0, bytesAndStatus[0], bytesAndStatus[1],
//                bytesAndStatus[2]));
//    }
//
//
//    private static class MyHandler extends Handler {
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//
//            if (msg.arg2 >= 0) {
//                int percent = (int) (msg.arg1 * 100 / (float) msg.arg2);
//                LogCat.e("download_progress..........." + percent + "%");
//            }
//
////            LogCat.e("temp2: " + temp2);
//        }
//    }


    @Override
    public void showLoading() {

        if(loading != null && loading.getVisibility() != View.VISIBLE){
            loading.setVisibility(View.VISIBLE);
        }

    }


    @Override
    public void hideLoading() {
        if(loading != null && loading.getVisibility() != View.GONE){
            loading.setVisibility(View.GONE);
        }


    }
}
