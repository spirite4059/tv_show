package com.gochinatv.ad;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.download.DLUtils;
import com.gochinatv.ad.base.BaseFragment;
import com.gochinatv.ad.thread.DeleteFileUtils;
import com.gochinatv.ad.tools.AlertUtils;
import com.gochinatv.ad.tools.Constants;
import com.gochinatv.ad.tools.DataUtils;
import com.gochinatv.ad.tools.DownLoadAPKUtils;
import com.gochinatv.ad.tools.InstallUtils;
import com.gochinatv.ad.tools.LogCat;
import com.gochinatv.ad.tools.SharedPreference;
import com.gochinatv.ad.ui.fragment.AdFiveFragment;
import com.gochinatv.ad.ui.fragment.AdOneFragment;
import com.google.gson.Gson;
import com.okhtttp.OkHttpCallBack;
import com.okhtttp.response.ADDeviceDataResponse;
import com.okhtttp.response.CommendResponse;
import com.okhtttp.response.LayoutResponse;
import com.okhtttp.service.ADHttpService;
import com.umeng.analytics.MobclickAgent;
import com.umeng.message.IUmengRegisterCallback;
import com.umeng.message.PushAgent;
import com.umeng.message.UmengMessageHandler;
import com.umeng.message.entity.UMessage;

import java.io.File;


/**
 * Created by fq_mbp on 16/3/17.
 */
public class MainActivity extends Activity {
    private Button testButton;
    private RelativeLayout rootLayout;
    private RelativeLayout titleLayout;
    private TextView textDeviceId;
    private final String FRAGMENT_TAG_AD_ONE = "ad_1";


    //下载apk尝试的次数
    private int reTryTimes;
    private PushAgent mPushAgent;

    //网络广播
    private NetworkBroadcastReceiver networkBroadcastReceiver;

    private ADDeviceDataResponse adDeviceDataResponse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 情况fragment的状态，保证getActivity不为null
        cleanFragmentState(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();

        init();
    }

    private void initView() {
        rootLayout = (RelativeLayout) findViewById(R.id.root_main);
        textDeviceId = (TextView) findViewById(R.id.text_device_id);
        titleLayout = (RelativeLayout) findViewById(R.id.rel_title);
        testButton = (Button) findViewById(R.id.test);
    }


    private void init() {
        registerNetworkReceiver();
        /**
         * 隐藏NavigationBar
         */
        DataUtils.hideNavigationBar(this);
        // 删除升级安装包
        deleteUpdateApk();
        // 初始化友盟统计
        initUmeng();

        //apk下载部分
        boolean hasApkDownload;//是否有apk下载
        String apkUrl = getIntent().getStringExtra("apkUrl");
        if (!TextUtils.isEmpty(apkUrl)) {
            //开始下载apk
            downloadAPKNew(apkUrl);
            hasApkDownload = true;
        } else {
            //没有apk下载
            hasApkDownload = false;

        }
        //动态布局部分
        adDeviceDataResponse = (ADDeviceDataResponse) getIntent().getSerializableExtra("device");
        loadFragmentTwo(hasApkDownload, adDeviceDataResponse);

        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isClick = false;
                if(isClick){


                    testButton.setText("恢复");
                    isClick = true;
                }else{
                    recoveryLoadFragment();
                    isClick = false;
                    testButton.setText("删除");
                }
            }
        });
    }

    /**
     * 动态注册网络状态广播,并回调
     */
    private void registerNetworkReceiver() {
        networkBroadcastReceiver = new NetworkBroadcastReceiver();
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkBroadcastReceiver,filter);

        networkBroadcastReceiver.setNetworkChangeLinstener(new NetworkBroadcastReceiver.NetworkChangeLinstener() {
            @Override
            public void networkChange(boolean hasNetwork) {
                if(hasNetwork){
                    AlertUtils.alert(MainActivity.this,"当前有网络连接!!!!!!!!!!!!!");
                }else{
                    AlertUtils.alert(MainActivity.this,"当前无网络连接xxxxxxxxxxxxxx");
                }
            }
        });

    }

    // 清空fragment的状态
    private void cleanFragmentState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            String FRAGMENTS_TAG = "android:support:fragments";
            savedInstanceState.remove(FRAGMENTS_TAG);
        }
    }


    private void initPush(String mac) {
        mPushAgent = PushAgent.getInstance(this);
        LogCat.e("device_token", "start............");
        mPushAgent.enable(new IUmengRegisterCallback() {

            @Override
            public void onRegistered(final String registrationId) {
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        //onRegistered方法的参数registrationId即是device_token
                        LogCat.e("device_token", registrationId);

                    }
                });
            }
        });
        mPushAgent.onAppStart();
        mPushAgent.setMessageChannel(mac);
        mPushAgent.setDebugMode(true);


        mPushAgent.setMessageHandler(new UmengMessageHandler() {

            @Override
            public void dealWithCustomMessage(Context context, UMessage uMessage) {
                super.dealWithCustomMessage(context, uMessage);
                LogCat.e("push: " + uMessage.custom);
                if(TextUtils.isEmpty(uMessage.custom)){
                    LogCat.e("push...........收到的命令为null");
                    return;
                }

                String json = "{" +
                                    "\"isJsCommend\": 0," +
                                    "\"cmd\":[" +
                                        "{" +
                                            "\"cmdInfo\": \"open\"," +
                                            "\"ad\":\"ad1\"" +
                                        "}," +
                                        "{" +
                                            "\"cmdInfo\":\"close\"," +
                                            "\"ad\":\"ad2\"" +
                                        "}," +
                                        "{" +
                                        "\"cmdInfo\":\"fresh\"," +
                                        "\"ad\":\"ad3\"" +
                                        "}," +
                                    "]" +
                                "}";

                CommendResponse commendResponse = new Gson().fromJson(json, CommendResponse.class);
                if(commendResponse == null){
                    LogCat.e("push...........commendResponse == null");
                    return;
                }
                // js的命令
                if(0  == commendResponse.getIsJsCommend()){
                    AdFiveFragment adFiveFragment = (AdFiveFragment) getFragmentManager().findFragmentByTag("ad_5");
                    adFiveFragment.setCommendInfo(json);
                } else {



                }
            }
        });
    }


    private void initUmeng() {
        if (isFinishing()) {
            return;
        }
        String mac = DataUtils.getMacAddress(MainActivity.this);
        SharedPreference sharedPreference = SharedPreference.getSharedPreferenceUtils(MainActivity.this);
        if (!TextUtils.isEmpty(mac)) {
            final String macAddress = mac.replaceAll(":", "");
            LogCat.e("mac: " + macAddress);
            MobclickAgent.startWithConfigure(new MobclickAgent.UMAnalyticsConfig(this, "572c1246e0f55aa6c5001533", mac));
            MobclickAgent.openActivityDurationTrack(false);
            MobclickAgent.setCatchUncaughtExceptions(true);
            MobclickAgent.setDebugMode(false);
            if (sharedPreference != null) {
                sharedPreference.saveDate(Constants.SHARE_KEY_UMENG, true);
            }
            initPush(mac);
        } else {
            if (sharedPreference != null) {
                sharedPreference.saveDate(Constants.SHARE_KEY_UMENG, false);
            }
        }
    }


    private void deleteUpdateApk() {
        DeleteFileUtils.getInstance().deleteFile(DataUtils.getApkDirectory() + Constants.FILE_APK_NAME);
    }

//    private void testInstall() {
//        File file = Environment.getExternalStorageDirectory();
////
//        File fileApk = new File(file.getAbsolutePath() + "/Music/test.apk");
//
//        LogCat.e("fileApk: " + fileApk.getAbsolutePath());
////
//        PackageInfo pInfo = null;
//        try {
//            pInfo = getPackageManager().getPackageInfo("com.gochinatv.ad", 0);
//        } catch (PackageManager.NameNotFoundException e) {
//            e.printStackTrace();
//        }
//        if (InstallUtils.hasRootPermission()) {
//            //  have  root
//            InstallUtils.installSilent(this, fileApk.getAbsolutePath(), true);
//            Toast.makeText(this, "提醒：获取到root权限，可以静默升级！", Toast.LENGTH_LONG).show();
//            // rootClientInstall(apkFile.getAbsolutePath());
//        } else if (InstallUtils.isSystemApp(pInfo) || InstallUtils.isSystemUpdateApp(pInfo)) {
////                Toast.makeText(context,"正在更新软件！",Toast.LENGTH_SHORT).show();
//            InstallUtils.installSilent(this, fileApk.getAbsolutePath(), false);
//            Toast.makeText(this, "提醒：获取到系统权限，可以静默升级！", Toast.LENGTH_LONG).show();
//        } else {
//            Toast.makeText(this, "提醒：没有获取到系统权限和root权限，请选择普通安装！", Toast.LENGTH_LONG).show();
//        }
//    }


    @Override
    protected void onStop() {

        DLUtils.cancel();
        if (downLoadAPKUtils != null) {
            downLoadAPKUtils.stopDownLoad();
        }
//        if (handler != null && progressRunnable != null) {
//            handler.removeCallbacks(progressRunnable);
//        }
        if (mPushAgent != null) {
            mPushAgent.disable();
        }
        super.onStop();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        //卸载广播
        if(networkBroadcastReceiver != null){
            unregisterReceiver(networkBroadcastReceiver);
        }
    }

    /**
     * 只有视频广告
     *
     * @param
     */
    private void showOneAD(boolean isDownload, ADDeviceDataResponse adDeviceDataResponse) {
        FragmentManager oneFM = getFragmentManager();
        FragmentTransaction oneFT = oneFM.beginTransaction();
        LayoutResponse oneLayout = new LayoutResponse();
        oneLayout.adWidth = "1.0";
        oneLayout.adHeight = "1.0";
        oneLayout.adTop = "0.0";
        oneLayout.adLeft = "0.0";
        //广告一
        if (!TextUtils.isEmpty(oneLayout.adWidth) && !TextUtils.isEmpty(oneLayout.adHeight)
                && !TextUtils.isEmpty(oneLayout.adTop) && !TextUtils.isEmpty(oneLayout.adLeft)) {
            //此时加载广告一
            AdOneFragment adOneFragment = (AdOneFragment) BaseFragment.getInstance(1);
            adOneFragment.setLayoutResponse(oneLayout);
            initAdOne(adOneFragment, isDownload, adDeviceDataResponse);
            oneFT.add(R.id.root_main, adOneFragment);
            oneFT.commit();
        }
    }

    /**
     * 新下载apk方法
     */
    private DownLoadAPKUtils downLoadAPKUtils;

    //private int progressTest;
    private void downloadAPKNew(final String url) {
        if (downLoadAPKUtils == null) {
            downLoadAPKUtils = new DownLoadAPKUtils();
        }
        downLoadAPKUtils.downLoad(this, url);
        final AdOneFragment adOneFragment = (AdOneFragment) getFragmentManager().findFragmentByTag(FRAGMENT_TAG_AD_ONE);
        //下载失败监听
        downLoadAPKUtils.setOnDownLoadErrorListener(new DownLoadAPKUtils.OnDownLoadErrorListener() {
            @Override
            public void onDownLoadFinish(Exception e) {
                //通知AdOneFragment去下载视频
                LogCat.e("APKdownload", "下载apk出现错误: " + e.toString());
                if (reTryTimes < 5) {
                    LogCat.e("APKdownload", "下载apk文件失败，进行第 " + reTryTimes + " 次尝试,........");
                    reTryTimes += 1;
                    //延迟2秒再去下载
                    rootLayout.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            downloadAPKNew(url);
                        }
                    }, 2000);

                } else {
                    LogCat.e("APKdownload", "下载apk出现错误,重试5次不再重试 ");
                    if (adOneFragment != null) {
                        adOneFragment.startDownloadVideo();
                    }
                }
            }
        });

        //下载进度监听
        downLoadAPKUtils.setOnDownLoadProgressListener(new DownLoadAPKUtils.OnDownLoadProgressListener() {
            @Override
            public void onDownLoadProgress(int progress, String fileName) {
                LogCat.e("APKdownload", "APK已经下载了 progress: " + progress + "%");
                AdOneFragment adOneFragment = (AdOneFragment) getFragmentManager().findFragmentByTag(FRAGMENT_TAG_AD_ONE);
                if (adOneFragment != null) {
                    adOneFragment.startDownloadVideo();
                }
                if (adOneFragment != null) {
                    adOneFragment.showNetSpeed(true, true, progress);
                }
            }
        });

        // 下载成功监听
        downLoadAPKUtils.setOnDownLoadFinishListener(new DownLoadAPKUtils.OnDownLoadFinishListener() {
            @Override
            public void onDownLoadFinish(String fileName) {
                //新包下载完成得安装
                LogCat.e("APKdownload", "下载升级成功，开始正式升级.......");
                File file = new File(DataUtils.getApkDirectory() + Constants.FILE_APK_NAME);
                InstallUtils.installAuto(MainActivity.this, file, true);
                if (adOneFragment != null) {
                    adOneFragment.hideNetSpeed();
                }
            }
        });

    }

    /**
     * 之前的下载apk方法
     */
//    private void downloadAPKOld() {
//        DownloadUtils.download(true, getApplication(), DataUtils.getApkDirectory(), Constants.FILE_APK_NAME, updateInfo.fileUrl, new OnUpgradeStatusListener() {
//            @Override
//            public void onDownloadFileSuccess(String filePath) {
//                //新包下载完成得安装
//                LogCat.e("下载升级成功，开始正式升级.......");
//                File file = new File(DataUtils.getApkDirectory() + Constants.FILE_APK_NAME);
//                InstallUtils.installAuto(MainActivity.this, file, true);
//                //MainActivity.this.finish();
//            }
//
//            @Override
//            public void onDownloadFileError(int errorCode, String errorMsg) {
//                //通知AdOneFragment去下载视频
//                if (reTryTimes < 3) {
//                    LogCat.e("下载apk文件失败，进行第 " + reTryTimes + " 次尝试,........");
//                    reTryTimes += 1;
//                    downloadAPKOld();
//                } else {
//                    LogCat.e("下载apk出现错误");
//                    if (adOneFragment != null) {
//                        adOneFragment.startDownloadVideo();
//                    }
//                }
//
//            }
//
//            @Override
//            public void onDownloadProgress(long progress, long fileSize) {
//
//            }
//        });
//    }


    /**
     * 加载fragment
     *
     * @param isDownload
     */

    private void loadFragmentTwo(boolean isDownload, ADDeviceDataResponse adDeviceDataResponse) {
        //显示title栏
        if(adDeviceDataResponse == null){
            titleLayout.setVisibility(View.VISIBLE);
        }else{
            showTitleLayout(adDeviceDataResponse.code);
        }

        // 显示大屏广告
        if (adDeviceDataResponse == null || TextUtils.isEmpty(adDeviceDataResponse.adStruct) || !"4".equals(adDeviceDataResponse.adStruct)) {
            showOneAD(isDownload, adDeviceDataResponse);
            return;
        }

        // 显示4屏广告
        if (adDeviceDataResponse.layout != null && adDeviceDataResponse.layout.size() > 0) {
            int size = adDeviceDataResponse.layout.size();
            FragmentManager fm = getFragmentManager();
            for (int i = 0; i < size; i++) {
                String adType = adDeviceDataResponse.layout.get(i).adType;
                if (TextUtils.isEmpty(adType)) {
                    continue;
                }
                int type = Integer.parseInt(adType);
                initAdFragment(isDownload, adDeviceDataResponse, fm, i, type);
            }
        }
    }

    private void initAdFragment(boolean isDownload, ADDeviceDataResponse adDeviceDataResponse, FragmentManager fm, int i, int type) {
        FragmentTransaction ft = fm.beginTransaction();
        BaseFragment fragment = BaseFragment.getInstance(type);
        // 1号广告位
        if (type == 1) {
            initAdOne((AdOneFragment) fragment, isDownload, adDeviceDataResponse);
        }

        if (type == 5) {
            ((AdFiveFragment)fragment).setLayoutResponses(adDeviceDataResponse.layout);
        }
        // 添加布局参数
        fragment.setLayoutResponse(adDeviceDataResponse.layout.get(i));
        ft.add(R.id.root_main, fragment, "ad_" + type);
        ft.commit();
    }

    private void initAdOne(AdOneFragment fragment, boolean isDownload, ADDeviceDataResponse adDeviceDataResponse) {
        if (isDownload) {
            fragment.setIsDownloadAPK(true);
        }

        if (adDeviceDataResponse.screenShot != null) {
            fragment.setScreenShotResponse(adDeviceDataResponse.screenShot);
        }
        //截屏的参数
        if (adDeviceDataResponse.pollInterval > 0) {
            fragment.setPollInterval(adDeviceDataResponse.pollInterval);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

        SharedPreference sharedPreference = SharedPreference.getSharedPreferenceUtils(MainActivity.this);
        boolean isHasMac = sharedPreference.getDate(Constants.SHARE_KEY_UMENG, false);
        if (isHasMac) {
            LogCat.e("mac", "umeng可以使用。。。。。");
            MobclickAgent.onResume(this);
        }
//        LogCat.e("debug", "info: " + getDeviceInfo(this));

    }


    @Override
    protected void onPause() {
        super.onPause();
        SharedPreference sharedPreference = SharedPreference.getSharedPreferenceUtils(MainActivity.this);
        boolean isHasMac = sharedPreference.getDate(Constants.SHARE_KEY_UMENG, false);
        if (isHasMac) {
            LogCat.e("mac", "umeng可以使用。。。。。");
            MobclickAgent.onPause(this);
        }
    }


    /**
     * 加载title栏-logo图和设备id
     */
    private void showTitleLayout(String text) {

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) titleLayout.getLayoutParams();

        String widthStr = "0.83125";
        String heightStr = "0.0833";
        String topStr = "0.0";
        String leftStr = "0.0";

        double width = (DataUtils.getDisplayMetricsWidth(this) * (Float.parseFloat(widthStr)));
        double height = (DataUtils.getDisplayMetricsHeight(this) * (Float.parseFloat(heightStr)));
        double top = (DataUtils.getDisplayMetricsHeight(this) * (Float.parseFloat(topStr)));
        double left = (DataUtils.getDisplayMetricsWidth(this) * (Float.parseFloat(leftStr)));

        params.width = (int) Math.round(width);
        params.height = (int) Math.round(height);
        params.topMargin = (int) Math.round(top);
        params.leftMargin = (int) Math.round(left);
        titleLayout.setLayoutParams(params);
        titleLayout.setVisibility(View.VISIBLE);

        if (!TextUtils.isEmpty(text)) {
            textDeviceId.setText("DEVICE ID: " + text);
        }
        LogCat.e(" DataUtils.getDisplayMetricsWidth: " + DataUtils.getDisplayMetricsWidth(this) + "   DataUtils.getDisplayMetricsHeight: " + DataUtils.getDisplayMetricsHeight(this));

    }

    /**
     * 重新加载fragment
     */
    private void recoveryLoadFragment(){
        if(adDeviceDataResponse != null){
            loadFragmentTwo(false, adDeviceDataResponse);
        }else{
            //请求设备信息接口
            doGetDeviceInfo();
        }
    }


    /**
     * 请求广告体接口---布局大小
     */
    private int reTryTimesTwo;
    //布局形式——1：一屏；4：4屏
    private void doGetDeviceInfo() {
        ADHttpService.doHttpGetDeviceInfo(this, new OkHttpCallBack<ADDeviceDataResponse>() {
            @Override
            public void onSuccess(String url, ADDeviceDataResponse response) {
                LogCat.e("doGetDeviceInfo url:  " + url);
                if (isFinishing()) {
                    return;
                }
                if (response == null) {
                    LogCat.e("请求广告体接口失败");
                    doError();
                    return;
                }

                if (!"0".equals(response.status)) {
                    LogCat.e("请求广告体接口失败 status = 1");
                    doError();
                    return;
                }
                adDeviceDataResponse = response;
                //广告体接口成功
                //加载布局
                loadFragmentTwo(false,adDeviceDataResponse);
                //UmengUtils.onEvent(LoadingActivity.this, UmengUtils.UMENG_APP_START_TIME, DataUtils.getFormatTime(adDeviceDataResponse.currentTime));
            }
            private void doError() {
                if (!isFinishing()) {
                    // 做不升级处理, 继续请求广告视频列表
                    reTryTimesTwo++;
                    if (reTryTimesTwo > 4) {
                        reTryTimesTwo = 0;
                        LogCat.e("升级接口已连续请求3次，不在请求");
                        //广告体接口成功
                        //加载布局
                        loadFragmentTwo(false,adDeviceDataResponse);
                    } else {
                        LogCat.e("进行第 " + reTryTimesTwo + " 次重试请求。。。。。。。");
                        doGetDeviceInfo();
                    }
                }
            }

            @Override
            public void onError(String url, String errorMsg) {
                LogCat.e("请求广告体接口失败。。。。。" + url);
                doError();
                //存储布局接口失败信息
//                layoutLogList = new ArrayList<RetryErrorRequest>();
//                RetryErrorRequest request = new RetryErrorRequest();
//                request.retry = String.valueOf(reTryTimesTwo);
//                request.errorMsg = errorMsg;
//                layoutLogList.add(request);
            }
        });
    }





}
