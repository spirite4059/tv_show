package com.gochinatv.ad;

import android.app.DownloadManager;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;

import com.gochinatv.ad.base.BaseActivity;
import com.gochinatv.ad.db.AdDao;
import com.gochinatv.ad.db.VideoAdBean;
import com.gochinatv.ad.download.DLUtils;
import com.gochinatv.ad.interfaces.DownloadListener;
import com.gochinatv.ad.tools.DataUtils;
import com.gochinatv.ad.tools.LogCat;
import com.gochinatv.ad.tools.SharedPreference;
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
public class ChinaRestaurantActivity extends BaseActivity {


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

    private static long startLong;
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

    private DownloadManager downloadManager;
    private SharedPreferences prefs;
    private static final String DL_ID = "222";
    DownloadDefine downloadDefine;
    private MyHandler handler;
    private boolean isDownLoadApk;

    private final String SHARE_KEY_DURATION = "SHARE_KEY_DURATION";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MobclickAgent.setDebugMode(false);
        startLong = System.currentTimeMillis();
        /** 设置是否对日志信息进行加密, 默认false(不加密). */
        AnalyticsConfig.enableEncrypt(true);
        MobclickAgent.openActivityDurationTrack(false);
        setContentView(R.layout.activity_main);
        initView();

        startLong = System.currentTimeMillis();
        SharedPreference sharedPreference = SharedPreference.getSharedPreferenceUtils(this);
        sharedPreference.saveDate(SHARE_KEY_DURATION, System.currentTimeMillis());


//        LogCat.e("getDeviceInfo: " + getDeviceInfo(this));
    }


    public void onResume() {
        super.onResume();
        startLong = System.currentTimeMillis();


        init();
        bindEvent();

        MobclickAgent.onResume(this);
        MobclickAgent.onPageStart("ChinaRestaurantActivity"); // 统计页面
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
//        downloadManagerPro = new DownloadManagerPro(downloadManager);


        handler = new MyHandler();
        downloadDefine = new DownloadDefine();

        // 创建播放列表
        playVideoTable = new ArrayList<>();
        // 查询数据库，看是否有播放记录
        localVideoTable = AdDao.getInstance(this).queryVideoAds();

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
        new DeleteApkThread(saveDir + "chinaRestaurant").start();

    }


    private void checkNet() {
        refrushTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                LogCat.e("正在检测是否联网。。。。。");
                if (DataUtils.isNetworkConnected(ChinaRestaurantActivity.this)) {
                    // 先去请求服务器，查看视频列表
                    doHttpUpdate(ChinaRestaurantActivity.this);
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
                    if (!DataUtils.getRawVideoUri(ChinaRestaurantActivity.this, R.raw.video_test).equals(videoAdBean.videoPath)) {
                        LogCat.e("添加一次视频播放" + videoAdBean.videoName);
                        MobclickAgent.onEvent(ChinaRestaurantActivity.this, "video_play_times", videoAdBean.videoName);
                    }


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
                    AdDao.getInstance(ChinaRestaurantActivity.this).delete(videoAdBean.videoId);

                    LogCat.e("从本地表中删除该记录。。。。。。" + localVideoTable.size());
                    for (VideoAdBean videoAdBean1 : localVideoTable) {
                        if (videoAdBean1.videoId.equals(videoAdBean.videoId)) {
                            LogCat.e("执行删除本地表记录操作。。。。。。");
                            localVideoTable.remove(videoAdBean1);
                            break;
                        }
                    }

                    LogCat.e("从当前播放列表中删除该视频。。。。。。" + playVideoTable.size());
                    playVideoTable.remove(position);
                    position--;
                    if (position < 0) {
                        position = 0;

                    }
                    LogCat.e("从当前播放列表中删除该视频后的大小。。。。。。" + playVideoTable.size());
                    VideoDetailResponse videoDetailResponseDown = null;
                    if (videoDetailResponses != null) {
                        for (VideoDetailResponse videoDetailResponse : videoDetailResponses) {
                            if (videoAdBean.videoId.equals(videoDetailResponse.vid)) {
                                videoDetailResponseDown = videoDetailResponse;
                                break;
                            }
                        }

                        if (videoDetailResponseDown != null) {
                            videoDetailResponseDown.isDownloading = false;
                            // 当前还有下载就添加到下载列表中
                            if (downloadViews == null) {
                                downloadViews = new ArrayList<VideoDetailResponse>();
                            }

                            addDownloadList(videoDetailResponseDown);
                            // 添加到下载列表
                            if (downloadViews.size() <= 1) {
                                // 已经下载完成,需要重新启动
                                initDownloadInfo();
                            }
                        } else {
                            LogCat.e("当前视频播放失败，服务器也没有当前视频，所以直接做删除操作，无需下载");
                        }

                    }

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
        this.updateInfo = updateInfo;
        downloadApk();
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
            }, 1000 * 10, 1000 * 10);
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
        downloadUrl = path;
        download();


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

        if (playVideoTable == null || playVideoTable.size() == 0) {
            LogCat.e("playNext。。。。。");
            position = 0;
            VideoAdBean videoAdBean = new VideoAdBean();
            videoAdBean.videoName = "预置片";
            videoAdBean.videoPath = DataUtils.getRawVideoUri(this, R.raw.video_test);
            videoAdBean.videoId = "0";
            playVideoTable.add(0, videoAdBean);
            playVideo(DataUtils.getRawVideoUri(this, R.raw.video_test));
        } else if (playVideoTable != null && playVideoTable.size() == 2) {
            VideoAdBean videoAdBean = playVideoTable.get(0);
            if (DataUtils.getRawVideoUri(this, R.raw.video_test).equals(videoAdBean.videoPath)) {
                playVideoTable.remove(0);
            }
            position = 0;

            playVideo(videoAdBean.videoPath);
        } else {
            position++;
            if (position >= playVideoTable.size()) {
                position = 0;
            }
            VideoAdBean videoAdBean = playVideoTable.get(position);
            if (TextUtils.isEmpty(videoAdBean.videoPath)) {

                // 添加下载
                LogCat.e("onError...........视频播放失败了: " + videoAdBean.videoName);
                LogCat.e("删除当前视频的本地文件。。。。。。");
                new DeleteFileThread(saveDir, videoAdBean.videoName).start();

                LogCat.e("从本地数据库中删除该记录。。。。。。");
                AdDao.getInstance(ChinaRestaurantActivity.this).delete(videoAdBean.videoId);

                LogCat.e("从本地表中删除该记录。。。。。。" + localVideoTable.size());
                for (VideoAdBean videoAdBean1 : localVideoTable) {
                    if (videoAdBean1.videoId.equals(videoAdBean.videoId)) {
                        LogCat.e("执行删除本地表记录操作。。。。。。");
                        localVideoTable.remove(videoAdBean1);
                        break;
                    }
                }

                LogCat.e("从当前播放列表中删除该视频。。。。。。" + playVideoTable.size());
                playVideoTable.remove(position);
                LogCat.e("从当前播放列表中删除该视频后的大小。。。。。。" + playVideoTable.size());
                VideoDetailResponse videoDetailResponseDown = null;
                if (videoDetailResponses != null) {
                    for (VideoDetailResponse videoDetailResponse : videoDetailResponses) {
                        if (videoAdBean.videoId.equals(videoDetailResponse.vid)) {
                            videoDetailResponseDown = videoDetailResponse;
                            break;
                        }
                    }
                    videoDetailResponseDown.isDownloading = false;
                    // 当前还有下载就添加到下载列表中
                    if (downloadViews == null) {
                        downloadViews = new ArrayList<VideoDetailResponse>();
                    }

                    addDownloadList(videoDetailResponseDown);
                    // 添加到下载列表
                    if (downloadViews.size() <= 1) {
                        // 已经下载完成,需要重新启动
                        initDownloadInfo();
                    }

                }
                position--;
                playNext();
            } else {
                LogCat.e("即将播放。。。。。" + videoAdBean.videoName);
                playVideo(videoAdBean.videoPath);
            }
        }


//        if (playVideoTable != null && playVideoTable.size() > 0) {
//            position++;
//            if (position >= playVideoTable.size()) {
//                position = 0;
////                LogCat.e("此时表示已经将所有视频循环一遍了。。。。。");
//            }
//            LogCat.e("当前播放第   " + position + "   个视频");
//
//            VideoAdBean videoAdBean = playVideoTable.get(position);
//            if (TextUtils.isEmpty(videoAdBean.videoPath)) {
//                playVideoTable.remove(videoAdBean);
//                playNext();
//                LogCat.e("当前视频的path为 null。。。。。");
//                return;
//            }
//
//            if (!DataUtils.getRawVideoUri(this, R.raw.video_test).equals(videoAdBean.videoPath)) {
//                File file = new File(saveDir, videoAdBean.videoName);
//                if (!file.exists()) {
//                    playVideoTable.remove(videoAdBean);
//                    playNext();
//                    LogCat.e("当前视频的视频文件不存在。。。。。" + file.getAbsolutePath());
//                    return;
//                }
//            }
//            playVideo(videoAdBean.videoPath);
////            LogCat.e(videoAdBean.videoName + "视频地址。。。。。" + videoAdBean.videoName);
//
//        } else {
//            position = 0;
//            VideoAdBean videoAdBean = new VideoAdBean();
//            videoAdBean.videoName = "预置片";
//            videoAdBean.videoPath = DataUtils.getRawVideoUri(this, R.raw.video_test);
//            videoAdBean.videoId = "0";
//            playVideoTable.add(0, videoAdBean);
//
//
//            playVideo(DataUtils.getRawVideoUri(this, R.raw.video_test));
//        }
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
        } else {
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

    UpdateResponse.UpdateInfoResponse updateInfo;

    private void downloadApk() {
        if (dlManager == null) {
            dlManager = DLManager.getInstance(this);
        }
        delAllFile(saveDir + "chinaRestaurant");

        downloadUrl = updateInfo.fileUrl;
        isDownLoadApk = true;
        dlManager.dlStop(downloadUrl);
        dlManager.dlCancel(downloadUrl);
        LogCat.e("开启保护线程，防止下载中断。。。。。。。");
        handler.postDelayed(downloadDefine, reLoadTime);

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
                handler.removeCallbacks(downloadDefine);

                handler.postDelayed(downloadDefine, reLoadTime);
            }

            @Override
            public void onStop(int progress) {
                LogCat.e("onStop............");
                doHttpGetEpisode();
            }

            @Override
            public void onFinish(File file) {
                LogCat.e("onFinish............");
                handler.removeCallbacks(downloadDefine);
                // 提示安装
                if (dlManager == null) {
                    dlManager = DLManager.getInstance(ChinaRestaurantActivity.this);
                }
                dlManager.dlCancel(saveDir + "chinaRestaurant.apk");
                installApk(ChinaRestaurantActivity.this, file.getAbsolutePath());
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


    // 播放视频
    private void playVideo(String url) {
        if (!TextUtils.isEmpty(url) && videoView != null) {
//            videoView.setVideoPath(url);


        }
    }


    String downloadUrl;

    long downloadingId;

    private int reLoadTime = 1000 * 60;
    DLUtils dlUtils;

    private void download() {
        isDownLoadApk = false;

        LogCat.e("本地文件目录：" + saveDir + downLoadingVideo.videoName);

//        dlUtils = DLUtils.init();
//        dlUtils.download(saveDir, downLoadingVideo.videoName, downloadUrl, 1, new OnDownloadStatusListener() {
//
//            private long fileLength;
//
//            @Override
//            public void onError(int errorCode, String errorMsg) {
//                LogCat.e("onError............. " + errorCode + ",  " + errorMsg);
//            }
//
//            @Override
//            public void onPrepare(long fileSize) {
//                LogCat.e("fileSize............. " + fileSize);
//                fileLength = fileSize;
//            }
//
//            @Override
//            public void onProgress(long progress) {
//                if(fileLength == 0){
//                    return;
//                }
//                double size = (int) (progress / 1024);
//                String sizeStr;
//                int s = (int) (progress * 100 / fileLength);
//                if (size > 1000) {
//                    size = (progress / 1024) / 1024f;
//                    BigDecimal b = new BigDecimal(size);
//                    double f1 = b.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
//                    sizeStr = String.valueOf(f1 + "MB，  ");
//                } else {
//                    sizeStr = String.valueOf((int)size + "KB，  ");
//                }
//                LogCat.e("progress............. " + sizeStr + s + "%");
//
//
//            }
//
//            @Override
//            public void onFinish() {
//                LogCat.e("onFinish............. ");
//            }
//        });

        if (dlManager == null) {
            dlManager = DLManager.getInstance(this);
        }
        dlManager.dlStop(downloadUrl);
        dlManager.dlCancel(downloadUrl);
        LogCat.e("开启保护线程，防止下载中断。。。。。。。");
        handler.postDelayed(downloadDefine, reLoadTime);

        dlManager.dlStart(downloadUrl, saveDir, downLoadingVideo.videoName, new DownloadListener() {

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
                handler.removeCallbacks(downloadDefine);

                handler.postDelayed(downloadDefine, reLoadTime);
            }

            @Override
            public void onStop(int progress) {
                LogCat.e("onStop............");


                downloadFinish(downloadUrl, AdDao.FLAG_DOWNLOAD_UNFINISH);
            }

            @Override
            public void onFinish(File file) {
                if (file.exists()) {
                    LogCat.e("文件创建成功，file:®" + file.getName());
                    LogCat.e("onFinish............");
                }

                // 重置重试下载的次数
                retryDownLoadTimes = 0;
                downloadFinish(downloadUrl, AdDao.FLAG_DOWNLOAD_FINISHED);
            }

            @Override
            public void onError(int status, String error) {
                LogCat.e("onError............" + error);
                downloadFinish(downloadUrl, AdDao.FLAG_DOWNLOAD_UNFINISH);
            }
        });
    }


    private int retryDownLoadTimes;

    private void downloadFinish(String url, String state) {
//        dlManager.dlCancel(url);

        // 将视频信息插入数据库
        insertVideoAdState(state, url);
        // 添加到播放列表
        if (state.equals(AdDao.FLAG_DOWNLOAD_FINISHED)) {
            // 如果当第一个视频是默认视频的时候，删除
//            if (playVideoTable != null && playVideoTable.size() == 1) {
//                VideoAdBean videoAdBean = playVideoTable.get(0);
//                LogCat.e("下载完成视频。。。。。。。" + videoAdBean.videoName);
//                if (DataUtils.getRawVideoUri(this, R.raw.video_test).equals(videoAdBean.videoPath)) {
//                    playVideoTable.remove(0);
//                }
//            }

            if (!downLoadingVideo.videoPath.equals(DataUtils.getRawVideoUri(this, R.raw.video_test))) {
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
        } else {
            // 查询是否还有未下载的任务，如果有 继续下载
            Object object = new Object();
            synchronized (object) {
                retryDownLoadTimes++;
                if (retryDownLoadTimes > 4) {
                    retryDownLoadTimes = 0;
                    Object object1 = new Object();
                    synchronized (object1) {
                        if (downloadViews != null && downloadViews.size() > 0) {
                            downloadViews.remove(0);
                        }
                    }
                    LogCat.e("已经重试4次了，当前视频没救了，死活无法下载了，不管他了，等待全部下载完成后欧的校验吧");
                } else {
                    LogCat.e("下载失败，将当前下载任务添加到下载列末尾，后续下载，进行第 " + retryDownLoadTimes + " 次重试");
                    if (downloadViews != null && downloadViews.size() > 0) {
                        int count = downloadViews.size() - 1;
                        VideoDetailResponse videoDetailResponse = downloadViews.get(0);
                        videoDetailResponse.isDownloading = false;
                        downloadViews.add(count, videoDetailResponse);
                        downloadViews.remove(0);
                        new DeleteFileThread(saveDir, videoDetailResponse.name).start();
                    }
                }


            }
        }


        if (downloadViews.size() != 0) {
            // 还有需要下载的视频，要继续下载
            LogCat.e("还有下载任务。。。。。。" + downloadViews.size());
            initDownloadInfo();
        } else {
            LogCat.e("已经全部下载完成了、、、、、、、");
            downloadUrl = "-1";
            downloadingId = -1;
            handler.removeCallbacks(downloadDefine);
            doHttpGetEpisode();


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

        // 停止当前的下载
        if (dlManager != null && !"-1".equals(downloadUrl)) {
            LogCat.e(downloadUrl + " is not null, 删除对应的文件");
            dlManager.dlCancel(downloadUrl);
        }

        if(dlUtils != null){
            dlUtils.cancel();
        }

//        unregisterReceiver(receiver);
//        if (downloadManager != null && downloadingId != -1) {
//            downloadManager.remove(downloadingId);
//        }


        if (handler != null) {
            handler.removeCallbacks(downloadDefine);
        }


        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        SharedPreference sharedPreference = SharedPreference.getSharedPreferenceUtils(this);
        // 计算离开的时候总时长
        try {


            startLong = sharedPreference.getDate(SHARE_KEY_DURATION, startLong);

            if (startLong != 0) {
                long duration = System.currentTimeMillis() - startLong;
                if (duration > 0) {
                    long day = duration / (24 * 60 * 60 * 1000);
                    long hour = (duration / (60 * 60 * 1000) - day * 24);
                    long min = ((duration / (60 * 1000)) - day * 24 * 60 - hour * 60);
                    long s = (duration / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - min * 60);
                    String str = day + "天  " + hour + "时" + min + "分" + s + "秒";

                    LogCat.e(str);

                    MobclickAgent.onEvent(this, "duration", str);
                    LogCat.e("上报开机时长。。。。。。。。");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            sharedPreference.saveDate(SHARE_KEY_DURATION, 0);
        }

        super.onStop();
//        getContentResolver().unregisterContentObserver(downloadObserver);

    }


    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
        MobclickAgent.onPageEnd("ChinaRestaurantActivity"); // 保证 onPageEnd 在onPause
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
//                    doDownLoad();
//
//                    break;
//            }
//        }
//        return downloadingId;
//    }
//
//
//
//    private void doDownLoad() {
//        LogCat.e("doDownLoad......" + downLoadingVideo.videoName);
//        LogCat.e("下载地址：" + downloadUrl);
//        Uri resource = Uri.parse(encodeGB(downloadUrl));
//        DownloadManager.Request request = new DownloadManager.Request(resource);
//        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
//        request.setAllowedOverRoaming(false);
//        //在通知栏中显示
//        request.setShowRunningNotification(false);
//        request.setVisibleInDownloadsUi(false);
//        //sdcard的目录下的download文件夹
//        request.setDestinationInExternalPublicDir(FILE_DIRECTROY, downLoadingVideo.videoName);
//        downloadingId = downloadManager.enqueue(request);
//        //保存id
//        LogCat.e("开始下载...........下载id: " + downloadingId);
//        handler.postDelayed(downloadDefine, 1000 * 30);
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
//
//
//    public void updateView() {
//        int[] bytesAndStatus = downloadManagerPro.getBytesAndStatus(downloadingId);
//        handler.sendMessage(handler.obtainMessage(0, bytesAndStatus[0], bytesAndStatus[1],
//                bytesAndStatus[2]));
//    }


    private class MyHandler extends Handler {


        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (isFinishing()) {
                return;
            }
            switch (msg.what) {
                case 0:
//                    if (msg.arg2 >= 0) {
//                        int percent = (int) (msg.arg1 * 100 / (float) msg.arg2);
//                        LogCat.e("download_progress..........." + percent + "%");
//                    }
                    removeCallbacks(downloadDefine);
                    downloadApk();
                    break;
                case 1:
                    removeCallbacks(downloadDefine);
                    LogCat.e("已经30秒没有下载信息了。。。。。保护线程要起作用了");
                    // 取消当前下载
//                    downloadManager.remove(downloadingId);
                    // 重新下载
//                    doDownLoad();
                    download();
                    break;
            }
        }
    }

    private class DownloadDefine implements Runnable {


        @Override
        public void run() {
            if (isDownLoadApk) {
                handler.sendMessage(handler.obtainMessage(0));
            } else {
                handler.sendMessage(handler.obtainMessage(1));
            }

        }
    }


    @Override
    public void showLoading() {

        if (loading != null && loading.getVisibility() != View.VISIBLE) {
            loading.setVisibility(View.VISIBLE);
        }

    }


    @Override
    public void hideLoading() {
        if (loading != null && loading.getVisibility() != View.GONE) {
            loading.setVisibility(View.GONE);
        }


    }


    boolean isUpdate;
    boolean isUpdateFinish;


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
//                    doDownLoad();
//
//                    break;
//            }
//        }
//        return downloadingId;
//    }
//
//
//
//    private void doDownLoad() {
//        LogCat.e("doDownLoad......" + downLoadingVideo.videoName);
//        LogCat.e("下载地址：" + downloadUrl);
//        Uri resource = Uri.parse(encodeGB(downloadUrl));
//        DownloadManager.Request request = new DownloadManager.Request(resource);
//        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
//        request.setAllowedOverRoaming(false);
//        //在通知栏中显示
//        request.setShowRunningNotification(false);
//        request.setVisibleInDownloadsUi(false);
//        //sdcard的目录下的download文件夹
//        request.setDestinationInExternalPublicDir(FILE_DIRECTROY, downLoadingVideo.videoName);
//        downloadingId = downloadManager.enqueue(request);
//        //保存id
//        LogCat.e("开始下载...........下载id: " + downloadingId);
//        handler.postDelayed(downloadDefine, 1000 * 30);
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
//
//
//    public void updateView() {
//        int[] bytesAndStatus = downloadManagerPro.getBytesAndStatus(downloadingId);
//        handler.sendMessage(handler.obtainMessage(0, bytesAndStatus[0], bytesAndStatus[1],
//                bytesAndStatus[2]));
//    }


}
