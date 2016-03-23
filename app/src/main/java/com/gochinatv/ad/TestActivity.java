package com.gochinatv.ad;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import com.download.DLUtils;
import com.download.dllistener.OnDownloadStatusListener;
import com.gochinatv.ad.base.BaseActivity;
import com.gochinatv.ad.tools.AlertUtils;
import com.gochinatv.ad.tools.DataUtils;
import com.gochinatv.ad.tools.LogCat;
import com.gochinatv.ad.tools.SharedPreference;
import com.gochinatv.ad.video.MeasureVideoView;
import com.httputils.http.response.PlayInfoResponse;
import com.httputils.http.response.UpdateResponse;
import com.httputils.http.response.VideoDetailListResponse;
import com.httputils.http.response.VideoDetailResponse;
import com.okhtttp.OkHttpUtils;
import com.umeng.analytics.AnalyticsConfig;
import com.umeng.analytics.MobclickAgent;
import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;
/**
 * Created by fq_mbp on 16/3/8.
 */
public class TestActivity extends BaseActivity {
    private MeasureVideoView videoView;
    private int playVideoPosition;
    private String saveDir;
    private static final String FILE_DIRECTORY = "gochinatv_ad";
    private static final String DOWNLOAD_FILE_EXTENSION = ".mp4";
    private Timer timer;
    private long startLong;
    /**
     * 本地数据表
     */
    private ArrayList<VideoDetailResponse> localVideoTable;
    private ArrayList<VideoDetailResponse> playVideoTable;
    private ArrayList<VideoDetailResponse> deleteVideoTable;
    /**
     * 服务器数据表
     */
    private ArrayList<VideoDetailResponse> videoDetailResponses;
    /**
     * 下载数据表
     */
    private ArrayList<VideoDetailResponse> downloadVideoTable;
    private Timer netStatusTimer;
    private LinearLayout loading;
    private final String SHARE_KEY_DURATION = "SHARE_KEY_DURATION";
    private boolean isDeleteVideo;
    private boolean isHttpFinish;
    private DLUtils dlUtils;
    private int localPlayPosition;
    private VideoDetailResponse downloadResponse;
    private int retryTimes;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initUmeng();
        setContentView(R.layout.activity_test);
        initView();
    }
    public void onResume() {
        super.onResume();
        // 记录开始时间
        setStartTime();
        init();
        bindEvent();
        MobclickAgent.onResume(this);
        MobclickAgent.onPageStart("ChinaRestaurantActivity"); // 统计页面
    }
    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
        MobclickAgent.onPageEnd("ChinaRestaurantActivity"); // 保证 onPageEnd 在onPause
    }
    @Override
    protected void onStop() {
        // 删除正在下载的任务
        if (dlUtils != null) {
            dlUtils.cancel();
        }
        // 删除正在下载的文件
        if (downloadResponse != null) {
            deleteFiles(saveDir, downloadResponse.name + DOWNLOAD_FILE_EXTENSION);
        }
        // 停止所有的timer
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
//        OkHttpUtils.getInstance().cancelFileDownloading();

        if (netStatusTimer != null) {
            netStatusTimer.cancel();
            netStatusTimer = null;
        }
        // 计算app启动的时间
        computeTime();
        super.onStop();
    }
    private void initView() {
        videoView = (MeasureVideoView) findViewById(R.id.videoView);
        loading = (LinearLayout) findViewById(R.id.loading);
    }
    private void init() {
        // 以上就显示加载状态，只有开始播放的时候隐藏
        showLoading();
        if (!isSdCardUsefully()) {
            LogCat.e("sd卡状态不可用。。。。。。。");
            hideLoading();
            AlertUtils.alert(this, "sd卡状态不可用");
            playVideo(getRawVideoUri());
            return;
        }
        dlUtils = DLUtils.init();
        // 实例化本地视频集合
        localVideoTable = new ArrayList<>();
        // 实例化播放列表集合
        playVideoTable = new ArrayList<>();
        // 实例化本地视频集合
        downloadVideoTable = new ArrayList<>();
        // 实例化需要删除视频视频集合
        deleteVideoTable = new ArrayList<>();
        // 实力化本地下载目录
        File file = Environment.getExternalStoragePublicDirectory(FILE_DIRECTORY);
        saveDir = file.getAbsolutePath() + "/";
        LogCat.e("现在的下载地址是： " + saveDir);
        // 文件目录存在，查询当前文件夹下的视频文件
        String firstVideoPath = getFirstVideoPath(file);
        if (TextUtils.isEmpty(firstVideoPath)) {
            playVideo(getRawVideoUri());
        } else {
            playVideo(firstVideoPath);
        }
        // 请求接口
        doHttpRequest();
        // 删除目录下所有的安装包
        new DeleteApkThread(saveDir + "chinaRestaurant").start();
    }
    /**
     * 获取初始播放的视频地址
     */
    private String getFirstVideoPath(File file) {
        String firstVideoPath = null;
        if ((file.exists() && file.isDirectory())) {
            File[] files = file.listFiles();
            if (files.length == 0) {
            } else {
                for (File fileItem : files) {
                    if (fileItem.isFile()) {
                        // 添加到本地视频集合
                        VideoDetailResponse videoAdBean = new VideoDetailResponse();
                        String name = fileItem.getName();
                        int index = name.lastIndexOf(DOWNLOAD_FILE_EXTENSION);
                        name = name.substring(0, index);
                        videoAdBean.name = name;
                        videoAdBean.videoPath = fileItem.getAbsolutePath();
                        localVideoTable.add(videoAdBean);
                        LogCat.e("找到本地缓存文件：" + videoAdBean.name);
                    }
                }
                // 开始播放视频
                if (localVideoTable.size() == 0) {
                    VideoDetailResponse videoAdBean = new VideoDetailResponse();
                    videoAdBean.name = "预置片";
                    videoAdBean.videoPath = getRawVideoUri();
                    videoAdBean.isPresetPiece = true;
                    localVideoTable.add(videoAdBean);
                    firstVideoPath = videoAdBean.videoPath;
                } else {
                    firstVideoPath = localVideoTable.get(0).videoPath;
                }
            }

        } else {
            LogCat.e("当前文件路径不存在，主动创建");
            file.mkdirs();
        }
        return firstVideoPath;
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
        videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                if (what == 1) {
                    // 继续播放下一个
                    // 删除当前的视频
                    // 继续下载播放失败的
                }
                return false;
            }
        });
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (isFinishing()) {
                    return;
                }
                LogCat.e("视频播放完成。。。。。。");
                // 播放下一个视频
                playNext();
            }
        });
    }
    private void initUmeng() {
        MobclickAgent.setDebugMode(false);
        /** 设置是否对日志信息进行加密, 默认false(不加密). */
        AnalyticsConfig.enableEncrypt(true);
        MobclickAgent.openActivityDurationTrack(false);
    }
    private void setStartTime() {
        startLong = System.currentTimeMillis();
        SharedPreference sharedPreference = SharedPreference.getSharedPreferenceUtils(this);
        sharedPreference.saveDate(SHARE_KEY_DURATION, System.currentTimeMillis());
        LogCat.e("记录启动app时间。。。。。。。" + startLong);
    }
    private void computeTime() {
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
    }
    private void playNext() {
        showLoading();
        // http请求没有结束的时候播放本地视频列表
        if (!isHttpFinish) {
            int lengthL = localVideoTable.size();
            if (lengthL <= 0) {
                VideoDetailResponse videoAdBean = new VideoDetailResponse();
                videoAdBean.name = "预置片";
                videoAdBean.videoPath = getRawVideoUri();
                videoAdBean.isPresetPiece = true;
                localVideoTable.add(videoAdBean);
            }
            localPlayPosition++;
            if (localPlayPosition >= lengthL) {
                localPlayPosition = 0;
            }
            VideoDetailResponse videoDetailResponse = localVideoTable.get(localPlayPosition);
            playVideo(videoDetailResponse.videoPath);
            return;
        }
        // 需要对视频进行删除操作
        if (isDeleteVideo) {
            LogCat.e("需要对本地缓存视频进行处理，删除需要删除的视频。。。");
            isDeleteVideo = false;
            // 删除所有的需要删除的视频文件
            deleteFiles(deleteVideoTable);
        }
        int length = playVideoTable.size();
        if (length == 0) {
            LogCat.e("当前播放列表仍然没有视频内容，继续播放预置片。。。");
            playVideo(getRawVideoUri());
        } else {
            VideoDetailResponse videoAdBean = playVideoTable.get(playVideoPosition);

            MobclickAgent.onEvent(this, "video_play_times", videoAdBean.name);

            LogCat.e("添加一次视频播放" + videoAdBean.name);

            playVideoPosition++;

            if (playVideoPosition >= length) {

                playVideoPosition = 0;

            }

            VideoDetailResponse videoDetailResponse = playVideoTable.get(playVideoPosition);
            LogCat.e("即将播放视频。。。" + videoDetailResponse.name + "  " + playVideoPosition);
            playVideo(videoDetailResponse.videoPath);
        }


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


        /**
         * -----------------------测试代码-------------------------
         */
//        ArrayList<String> deletes = new ArrayList<>();
//        deletes.add("683817");
//        deletes.add("683816");
//        deletes.add("679615");
//        int size1 =  videoDetailResponses.size();
//        for(int i = size1 - 1; i >=0; i--){
//            VideoDetailResponse videoDetailResponse = videoDetailResponses.get(i);
//            for(String vid : deletes){
//                if(vid.equals(videoDetailResponse.vid)){
//                    videoDetailResponses.remove(videoDetailResponse);
//                }
//            }
//        }
        /**
         * -----------------------测试代码-------------------------
         */


        downloadVideoTable.addAll(videoDetailResponses);

        int length = videoDetailResponses.size();

        // 判断是否有本地缓存视频
        int localVideosSize = localVideoTable.size();

        isHttpFinish = true;

        LogCat.e("开始匹配服务器视频列表。。。。。。。");
        // 处理本地视频与服务器视频列表
        if (localVideosSize == 1) { // 如果有，则需要先判断
            VideoDetailResponse videoAdBean = localVideoTable.get(0);
            if (videoAdBean.isPresetPiece) {
                // 当前的视频是预置片，此时表示没有本地缓存文件，所有视频都需要下载
                LogCat.e("当前的视频是预置片，此时表示没有本地缓存文件，所有视频都需要下载");
            } else {
                // 此时有本地视频了，要检测当前视频是否仍在服务器列表中
                LogCat.e("此时有本地视频了，要检测当前视频是否仍在服务器列表中");
                boolean isServerUse = false;
                VideoDetailResponse downloadDeleteResponse = null;
                for (VideoDetailResponse videoDetailResponse : videoDetailResponses) {
                    if (videoAdBean.name.equals(videoDetailResponse.name)) {
                        isServerUse = true;

                        videoDetailResponse.videoPath = videoAdBean.videoPath;
                        downloadDeleteResponse = videoDetailResponse;
                        break;
                    }
                }

                if (isServerUse) { // 仍需要继续使用此视频
                    // 直接添加到播放列表
                    LogCat.e("仍需要继续使用此视频，将该视频添加到播放列表，并从下载列表中删除..." + videoAdBean.name);
                    playVideoTable.add(videoAdBean);
                    // 此视频无需下载
                    downloadVideoTable.remove(downloadDeleteResponse);
                } else { // 不需要此视频
                    // 当前一定在播放此视频，需要等到视频播放完后进行删除
                    LogCat.e(" 不需要此视频，等当前视频播放结束后，将其删除..." + videoAdBean.name);
                    isDeleteVideo = true;
                    deleteVideoTable.add(videoAdBean);
                }
            }
        } else {
            ArrayList<Integer> deleteIndexs = new ArrayList<>();
            for (int i = 0; i < localVideosSize; i++) {
                VideoDetailResponse videoDetailResponse = localVideoTable.get(i);
                boolean isServerUse = false;
                // 检测服务器还用到的视频
                for (int j = 0; j < length; j++) {
                    VideoDetailResponse playVideoResponse = videoDetailResponses.get(j);
                    if (videoDetailResponse.name.equals(playVideoResponse.name)) {
                        playVideoResponse.videoPath = videoDetailResponse.videoPath;
                        isServerUse = true;
                        // 将不需要下载的视频删除掉
                        deleteIndexs.add(j);
                        LogCat.e("当前视频仍然需要使用。。。。。。。" + videoDetailResponse.name + "  " + videoDetailResponse.videoPath);
                        playVideoTable.add(videoDetailResponse);
                        break;
                    }
                }

                if (!isServerUse) { // 服务器不在使用的视频需要进行清除
                    // 判断是否正在播放当前视频
                    isDeleteVideo = true;
                    LogCat.e("需要从本地进行删除的视频..." + videoDetailResponse.name);
                    deleteVideoTable.add(videoDetailResponse);
                }
            }

            LogCat.e("不需要此视频，等当前视频播放结束后，将其删除..." + downloadVideoTable.size());
            // 删除无需下载的视频

            Collections.sort(deleteIndexs);
            int size = deleteIndexs.size();
            LogCat.e("deleteIndexs..." + deleteIndexs);
            for (int i = size - 1; i >= 0; i--) {
                int index = deleteIndexs.get(i);
                downloadVideoTable.remove(index);
            }
            LogCat.e("删除后的下载列表数量..." + downloadVideoTable.size());
        }

        // 开启下载
        for (VideoDetailResponse videoDetailResponse : downloadVideoTable) {
            LogCat.e("需要下载的视频..." + videoDetailResponse.name);
        }


        getVideoUrl();

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
    protected void onUpdateSuccess(UpdateResponse.UpdateInfoResponse updateInfo) {
        downloadApk(updateInfo);
    }


    @Override
    protected void getVideoCdnPath(String url) {
        if (isFinishing()) {
            return;
        }
        LogCat.e("获取到当前视频的下载地址。。。。。。。。" + url);
        // 一个视频一个视频的下载
        download(url);
    }

    @Override
    protected void onVideoCdnError(String path) {
        super.onVideoCdnError(path);
        LogCat.e("当前视频的下载地址获取失败。。。。");


        final int size = downloadVideoTable.size();
        if (size > 1) {
            LogCat.e("将当前下载地址获取失败的视频放到最后一个，继续下载后续的视频。。。。");

            videoView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    downloadVideoTable.add(size, downloadResponse);
                    downloadVideoTable.remove(0);
                    getVideoUrl();
                }
            }, 50000);


        } else {
            LogCat.e("只剩最后一个视频。。。。");
            if (retryTimes < 2) {
                retryTimes++;
                LogCat.e("第 " + retryTimes + " 次重新尝试获取下载地址");
                getVideoUrl();
            } else {
                retryTimes = 0;
                LogCat.e("经过尝试后，仍无法获取到当前视频的下载地址，放弃此次下载");
            }
        }


    }


    private void getVideoUrl() {
        if (downloadVideoTable.size() == 0) {
            LogCat.e("所有视频下载完成。。。。。。。。");
            downloadResponse = null;
        } else {
            LogCat.e("获取下载列表第一个视频，并开始下载。。。。。。。。 还剩余下载任务：" + downloadVideoTable.size());
            downloadResponse = downloadVideoTable.get(0);
            downloadResponse.videoPath = saveDir + downloadResponse.name + DOWNLOAD_FILE_EXTENSION;
            if (downloadResponse.playInfo != null && downloadResponse.playInfo.size() != 0) {
                PlayInfoResponse playInfoResponse = downloadResponse.playInfo.get(0);
                doHttpGetCdnPath(this, playInfoResponse.remotevid, null);
            }
        }
    }

    private int retryDownloadTimes;

    private void download(final String url) {
        LogCat.e("开始下载。。。。");

        // okDownload(url);
        // 每次开始前都取消其他下载，保证只有一个下载
        dlUtils.cancel();

        dlUtils.download(saveDir, downloadResponse.name + DOWNLOAD_FILE_EXTENSION, url, 1, new OnDownloadStatusListener() {

            private long fileLength;

            @Override
            public void onError(int errorCode, String errorMsg) {
                LogCat.e("onDownloadFileError............. " + errorCode + ",  " + errorMsg);
                // 出错就放弃当前下载任务，继续下载下一个任务，并将当前任务放到最后一个，如果已经是最后一个，再重试2边
                final int size = downloadVideoTable.size();
                if (size > 1) {
                    videoView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            LogCat.e("5秒后继续尝试，如此循环。。。。");
                            if (retryDownloadTimes < 3) {
                                retryDownloadTimes++;
                                LogCat.e("继续重试3次下载，此时是第" + retryDownloadTimes + "次尝试。。。。");
                                download(url);
                            } else {
                                retryDownloadTimes = 0;
                                LogCat.e("将当前下载失败的视频放到最后一个，继续下载后续的视频。。。。");
                                downloadVideoTable.add(size, downloadResponse);
                                downloadVideoTable.remove(0);
                                getVideoUrl();
                            }
                        }
                    }, 5000);

                } else {
                    LogCat.e("只剩最后一个视频。。。。");
                    videoView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            LogCat.e("5秒后继续尝试，如此循环。。。。");
                            download(url);

                        }
                    }, 5000);
                }
            }


            @Override
            public void onPrepare(long fileSize) {
                LogCat.e("fileSize............. " + fileSize);
                fileLength = fileSize;
            }

            @Override
            public void onProgress(long progress) {
                if (fileLength == 0) {
                    return;
                }
                logProgress(progress);


            }

            @Override
            public void onFinish(String filePath) {
                LogCat.e("onFinish............. " + filePath);
                // 把下载成功的视频添加到播放列表中
                playVideoTable.add(downloadResponse);

                // 把当前下载的任务从播放列删除
                downloadVideoTable.remove(downloadResponse);

                // 继续进行下载任务
                getVideoUrl();


            }

            @Override
            public void onCancel() {
                LogCat.e("onCancel............. ");
            }

            private void logProgress(long progress){
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
                LogCat.e("progress............. " + sizeStr + s + "%");
            }
        });
    }


    private void okDownload(final String url) {
        LogCat.e("okDownload开始下载。。。。");
        OkHttpUtils.getInstance().doFileDownload(url, saveDir, downloadResponse.name + DOWNLOAD_FILE_EXTENSION, new com.okhtttp.OnDownloadStatusListener() {

            private long fileLength;

            @Override
            public void onError(int errorCode, String errorMsg) {

            }

            @Override
            public void onPrepare(long fileSize) {
                LogCat.e("fileSize............. " + fileSize);
                fileLength = fileSize;
            }

            @Override
            public void onProgress(long progress) {
                if (fileLength == 0) {
                    return;
                }
                logProgress(progress);
            }

            @Override
            public void onFinish(String filePath) {
                LogCat.e("filePath............. " + filePath);
            }

            @Override
            public void onCancel() {

            }

            private void logProgress(long progress) {
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
                LogCat.e("progress............. " + sizeStr + s + "%");
            }
        });
    }


    private void downloadApk(final UpdateResponse.UpdateInfoResponse updateInfo) {
        LogCat.e("开始下载升级安装包。。。。");
        dlUtils.download(saveDir + "chinaRestaurant", downloadResponse.name, updateInfo.fileUrl, 1, new OnDownloadStatusListener() {

            private long fileLength;

            @Override
            public void onError(int errorCode, String errorMsg) {
                LogCat.e("onDownloadFileError............. " + errorCode + ",  " + errorMsg);
                // 此时出错，需要判断是否是强制升级，如果是强制升级，说明是接口等重大功能改变，必须优先升级
                // 强制升级：如果出错，就要循环去做升级操作，直至升级成
                // 普通升级：如果出错，不再请求，去请求视频接口
                if ("1".equals(updateInfo.type)) {
                    // 强制更新
                    videoView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            LogCat.e("5秒后继续尝试，如此循环。。。。");
                            downloadApk(updateInfo);
                        }
                    }, 5000);

                } else {
                    doHttpGetEpisode();
                }
            }


            @Override
            public void onPrepare(long fileSize) {
                LogCat.e("fileSize............. " + fileSize);
                fileLength = fileSize;

            }

            @Override
            public void onProgress(long progress) {
                if (fileLength == 0) {
                    return;
                }
                logProgress(progress);

            }

            @Override
            public void onFinish(String filePath) {
                LogCat.e("onFinish............. " + filePath);
                // 把下载成功的视频添加到播放列表中
                installApk(TestActivity.this, filePath);
                finish();
            }

            @Override
            public void onCancel() {
                LogCat.e("onCancel............. ");


            }

            private void logProgress(long progress) {
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
                LogCat.e("progress............. " + sizeStr + s + "%");
            }
        });


    }


    /**
     * 检测sd卡状态是否可用
     */
    private boolean isSdCardUsefully() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    /**
     * 获取预置片的地址
     *
     * @return
     */
    private String getRawVideoUri() {
        return DataUtils.getRawVideoUri(this, R.raw.video_test);
    }


    // 播放视频
    private void playVideo(String url) {
        if (!TextUtils.isEmpty(url) && videoView != null) {
            videoView.setVideoPath(url);
        }
    }

    /**
     * 检测网络，如果链接正常则请求接口
     */
    private void doHttpRequest() {
        netStatusTimer = new Timer();
        netStatusTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                LogCat.e("正在检测是否联网。。。。。");
                if (DataUtils.isNetworkConnected(TestActivity.this)) {
                    // 先去请求服务器，查看视频列表
                    doHttpUpdate(TestActivity.this);
                    netStatusTimer.cancel();
                    netStatusTimer = null;
                    LogCat.e("已经联网。。。。。");
                } else {
                    LogCat.e("没有联网。。。。。继续检查");
                }
            }
        }, 100, 10 * 1100);
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


}
