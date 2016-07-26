package com.gochinatv.ad;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.download.DLUtils;
import com.gochinatv.ad.base.BaseActivity;
import com.gochinatv.ad.base.BaseFragment;
import com.gochinatv.ad.cmd.CloseCommend;
import com.gochinatv.ad.cmd.CmdReceiver;
import com.gochinatv.ad.cmd.FreshCommend;
import com.gochinatv.ad.cmd.ICommend;
import com.gochinatv.ad.cmd.Invoker;
import com.gochinatv.ad.cmd.OpenCommend;
import com.gochinatv.ad.cmd.RefreshWebCommend;
import com.gochinatv.ad.interfaces.OnUpgradeStatusListener;
import com.gochinatv.ad.receiver.FirebaseMessageReceiver;
import com.gochinatv.ad.tools.Constants;
import com.gochinatv.ad.tools.DataUtils;
import com.gochinatv.ad.tools.DownLoadAPKUtils;
import com.gochinatv.ad.tools.DownloadUtils;
import com.gochinatv.ad.tools.InstallUtils;
import com.gochinatv.ad.tools.LogCat;
import com.gochinatv.ad.tools.SharedPreference;
import com.gochinatv.ad.tools.WifiAutoConnectManager;
import com.gochinatv.ad.ui.dialog.WifiDialog;
import com.gochinatv.ad.ui.fragment.AdOneFragment;
import com.gochinatv.ad.ui.view.AdWebView;
import com.gochinatv.statistics.SendStatisticsLog;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.okhtttp.OkHttpCallBack;
import com.okhtttp.response.ADDeviceDataResponse;
import com.okhtttp.response.AdVideoListResponse;
import com.okhtttp.response.CommendResponse;
import com.okhtttp.response.LayoutResponse;
import com.okhtttp.response.ScreenShotResponse;
import com.okhtttp.response.UpdateResponse;
import com.okhtttp.service.UpPushInfoService;
import com.tools.MacUtils;
import com.umeng.analytics.MobclickAgent;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by fq_mbp on 16/3/17.
 */
public class MainActivity extends BaseActivity {
    //private Button testButton;
    //boolean isClick = true;
    private RelativeLayout rootLayout;
    private RelativeLayout titleLayout;
    private TextView textDeviceId;//设备id
    private TextView textDownloadInfo;//下载信息
    private TextView textSpeedInfo;//下载速度
    private final String FRAGMENT_TAG_AD_ONE = "ad_1";
    private static final int HANDLER_MSG_TOKEN = 1000;
    private static final int HANDLER_MSG_GET_TOKEN = 1001;

    //下载apk尝试的次数
    private int reTryTimes;

    //private MyHandler myHandler;
    private AdWebView adWebView;

    //网络广播
    private NetworkBroadcastReceiver networkBroadcastReceiver;

    private ADDeviceDataResponse adDeviceDataResponse;

    /**
     * 请求广告体接口---布局大小
     */
    private int reTryTimesTwo;

    /**
     * 新下载apk方法
     */
    private DownLoadAPKUtils downLoadAPKUtils;

    private boolean isInitNetState = true;

    /**
     * 每隔4个小时
     */
    private int intervalTime = 4 * 60 * 60 * 1000;//默认4个小时;
    private Timer intervalTimer;//定时器

    //firebase消息广播
    private FirebaseMessageReceiver firebaseMessageReceiver;

    //是否中途app由无网络变为有网络
    //private boolean isReloadFlag = false;
    WifiReceiver receiverWifi;
    WifiAutoConnectManager wifiAutoConnectManager;
    boolean isHasRegisterWifiReceiver;
    //网络是否连接
    private boolean isNetConnect;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //保持屏幕不变黑
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
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
        adWebView = (AdWebView) findViewById(R.id.ad_web);
        textDownloadInfo = (TextView) findViewById(R.id.text_download_info);
        textSpeedInfo = (TextView) findViewById(R.id.text_speed_info);
    }

    boolean isDoGetDevice;
    WifiDialog dialog;

    private void init() {

        if(DataUtils.isNetworkConnected(this)){
            textSpeedInfo.setText("wifi-on:0kb/s");
            isNetConnect = true;
        }else{
            isNetConnect = false;
        }

        registerNetworkReceiver();

        Intent intent = getIntent();

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


        isDoGetDevice = intent.getBooleanExtra("isDoGetDevice", false);
        if (isDoGetDevice) {
            //动态布局部分
            LogCat.e("push", "获取到设备信息...........");
            adDeviceDataResponse = (ADDeviceDataResponse) getIntent().getSerializableExtra("device");
        }

        // 初始化友盟统计
        initUmeng();

        loadFragment(hasApkDownload, adDeviceDataResponse);

        //得到firebase-token
        refreshGetFirebaseToken();

        //每4个小时上报开机时间和升级请求
        intervalUpdate();


        // wifi没有打开或者没有信号,显示wifi引导页面
        showWifiDialog();

//        if (DataUtils.isNetworkConnected(this)) {
//            textSpeedInfo.setText("wifi-on");
//        } else {
//            LogCat.e("net", "off..............");
//            textSpeedInfo.setText("wifi-off:0kb/s");
//        }



    }

    private void showWifiDialog() {
        if(!DataUtils.isNetworkConnected(this)){
            if(wifiAutoConnectManager == null){
                wifiAutoConnectManager = new WifiAutoConnectManager(this);
            }

            if(!wifiAutoConnectManager.wifiManager.isWifiEnabled()){
                wifiAutoConnectManager.wifiManager.setWifiEnabled(true);
            }


            wifiAutoConnectManager.wifiManager.startScan();
            if(receiverWifi == null){
                receiverWifi = new WifiReceiver();
            }
            isHasRegisterWifiReceiver = true;
            registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        }
    }


    /**
     * 每4隔小时请求升级和上报开机
     */
    private void intervalUpdate() {
        if(isFinishing()){
            return;
        }
        if (intervalTimer == null) {
            intervalTimer = new Timer();
        }

        intervalTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                //升级接口
                doHttpUpdate(MainActivity.this);
                //上报开机时间
                SendStatisticsLog.sendInitializeLog(MainActivity.this);//提交激活日志
                fragmentDoHttpRequest();
                refreshGetFirebaseToken();//获取tooken
            }
        }, intervalTime, intervalTime);//intervalTime
    }


    /**
     * 各个fragment的4个小时后的重新请求接口
     */
    private void fragmentDoHttpRequest(){
        FragmentManager fm = getFragmentManager();
        for(int i= 1;i<5;i++){
            if(i == 3){
                continue;
            }
            if(i == 1){
                LogCat.e("MainActivity", "baseFragment :  "+ i);
                AdOneFragment adOneFragment = (AdOneFragment) fm.findFragmentByTag(Constants.FRAGMENT_TAG_PRE + i);
                if(adOneFragment != null){
                    adOneFragment.httpRequest(false);
                }
            }else{
                LogCat.e("MainActivity", "baseFragment :  "+ i);
                BaseFragment baseFragment = (BaseFragment) fm.findFragmentByTag(Constants.FRAGMENT_TAG_PRE + i);
                if(baseFragment != null){
                    baseFragment.doHttpRequest();
                }
            }
        }
    }


    /**
     *  获取token的
     */
    private int tokenTimes;
    private Runnable firebaseTokenRunnable = new Runnable() {
        @Override
        public void run() {
            tokenTimes++;
            String refreshedToken = FirebaseInstanceId.getInstance().getToken();
            if(!TextUtils.isEmpty(refreshedToken)){
                LogCat.e("service", "refreshedToken : "+ refreshedToken);
                //上传refreshedToken
                String deviceId = null;
                if (adDeviceDataResponse != null) {
                    deviceId = adDeviceDataResponse.code;
                }
                doHttpUpdateToken(MainActivity.this, deviceId,refreshedToken);
                if(rootLayout != null && firebaseTokenRunnable != null){
                    rootLayout.removeCallbacks(firebaseTokenRunnable);
                }
            }else{
                //当请求20次后不再请求
                if(tokenTimes <20){
                    if(rootLayout != null && firebaseTokenRunnable != null){
                        if(DataUtils.isNetworkConnected(MainActivity.this)){
                            LogCat.e("service", "再次获取refreshedToken！！！");
                            rootLayout.postDelayed(firebaseTokenRunnable,5000);
                        }
                    }else{
                        LogCat.e("service", "再次获取refreshedToken失败2222222");
                    }
                }else{
                    LogCat.e("service", "refreshedToken，当请求20次后不再请求");
                }

            }
        }
    };

    /**
     * 获取firebase-token并上传服务器
     */
    private void refreshGetFirebaseToken(){
        tokenTimes = 0;
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        if(!TextUtils.isEmpty(refreshedToken)){
            LogCat.e("service", "refreshedToken : "+ refreshedToken);
            //上传refreshedToken
            String deviceId = null;
            if (adDeviceDataResponse != null) {
                deviceId = adDeviceDataResponse.code;
            }
            doHttpUpdateToken(MainActivity.this, deviceId,refreshedToken);
        }else{
            LogCat.e("service", "refreshedToken  = null");
            if(rootLayout != null && firebaseTokenRunnable != null){
                if(DataUtils.isNetworkConnected(MainActivity.this)){
                    LogCat.e("service", "再次获取refreshedToken！！！");
                    rootLayout.postDelayed(firebaseTokenRunnable,5000);
                }
            }else{
                LogCat.e("service", "再次获取refreshedToken失败1111111111");
            }
        }

    }




    // 清空fragment的状态
    private void cleanFragmentState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            String FRAGMENTS_TAG = "android:support:fragments";
            savedInstanceState.remove(FRAGMENTS_TAG);
        }
    }



    /**
     * google-firebase推送
     */
    private void initFirebaseMessage(){
        firebaseMessageReceiver =new FirebaseMessageReceiver();
        IntentFilter intentFilter = new IntentFilter(Constants.FIREBASE_INTENT_FILTER);
        registerReceiver(firebaseMessageReceiver,intentFilter);

        firebaseMessageReceiver.setFirebaseMessageListener(new FirebaseMessageReceiver.FirebaseMessageListener() {
            @Override
            public void sendFirebaseMessage(String msg) {
                if(!TextUtils.isEmpty(msg)){
                    //DataUtils.saveToSDCard("\n" + "来自firebase的消息： "+ msg +" ---- "+ DataUtils.getFormatTime(System.currentTimeMillis()));
                    LogCat.e("push", "commend -> json: " + msg);
                    LogCat.e("Message", "google-firebase的消息: " + msg);
                    dispatchCommend(msg);
                }else{
                    LogCat.e("push...........收到的命令为null");
                }
            }
        });
    }



    private void dispatchCommend(String uMessage) {
        CommendResponse commendResponse = null;
        try {
            commendResponse = new Gson().fromJson(uMessage, CommendResponse.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (commendResponse == null) {
            LogCat.e("push", "push...........commendResponse == null");
            return;
        }
        // 发送命令
        sendCmd(uMessage, commendResponse);
    }

    private void sendCmd(String uMessage, CommendResponse commendResponse) {
        if ("1".equals(commendResponse.getIsJsCommand())) {   // 对js的命令
            LogCat.e("push", "push...........commend  code ......" + commendResponse.getIsJsCommand());
            cmdJs(uMessage);
        } else { // 本地的命令
            cmdLocal(commendResponse);
        }
    }

    private void cmdJs(String uMessage) {
        if (adWebView != null) {
            LogCat.e("push", "push...........adWebView != null");
            adWebView.setCommendInfo(uMessage);
        }
    }

    private void cmdLocal(CommendResponse commendResponse) {
        if (commendResponse == null) {
            return;
        }

        LogCat.e("push", "执行本地命令.............");
        Invoker invoker = new Invoker();

        CmdReceiver receiver = new CmdReceiver(this);

        for (CommendResponse.CmdResponse cmdResponse : commendResponse.getCmd()) {
            String cmd = cmdResponse.getCmdInfo();
            if (TextUtils.isEmpty(cmd)) {
                continue;
            }

            if (ICommend.COMMEND_OPEN.equals(cmd)) {
                invoker.setCommend(new OpenCommend(cmdResponse.getAd(), receiver, adDeviceDataResponse));
            } else if (ICommend.COMMEND_CLOSE.equals(cmd)) {
                invoker.setCommend(new CloseCommend(cmdResponse.getAd(), receiver));
            } else if (ICommend.COMMEND_FRESH.equals(cmd)) {
                invoker.setCommend(new FreshCommend(cmdResponse.getAd(), receiver));
            } else if (ICommend.COMMEND_FRESH_WEB.equals(cmd)) {
                invoker.setCommend(new RefreshWebCommend(receiver));
            }
            invoker.execute();
        }


    }


    private void initUmeng() {
        if (isFinishing()) {
            return;
        }

        if (DataUtils.isNetworkConnected(this)) {
            // 先去请求服务器，查看视频列表
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
                initFirebaseMessage();//firebase推送
            } else {
                if (sharedPreference != null) {
                    sharedPreference.saveDate(Constants.SHARE_KEY_UMENG, false);
                }
            }
        }


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
        super.onStop();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        DLUtils.cancel();
        //卸载广播
        if (networkBroadcastReceiver != null) {
            unregisterReceiver(networkBroadcastReceiver);
        }
        if (downLoadAPKUtils != null) {
            downLoadAPKUtils.stopDownLoad();
        }

        if (adWebView != null) {
            adWebView.cancelReloadRunnable();
        }

        if (intervalTimer != null) {
            intervalTimer.cancel();
        }

        if(rootLayout != null && firebaseTokenRunnable != null){
            rootLayout.removeCallbacks(firebaseTokenRunnable);
        }

        //卸载firebase广播
        if(firebaseMessageReceiver != null){
            unregisterReceiver(firebaseMessageReceiver);
        }

        if(receiverWifi != null && isHasRegisterWifiReceiver){
            unregisterReceiver(receiverWifi);
        }

        cancelDialogCountTimer();
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
            oneFT.add(R.id.root_main, adOneFragment, Constants.FRAGMENT_TAG_PRE + 1);
            oneFT.commit();
        }
    }


    /**
     * 之前的下载apk方法
     */
//    private void downloadAPKOld() {
//        DownloadUtils.start(true, getApplication(), DataUtils.getApkDirectory(), Constants.FILE_APK_NAME, updateInfo.fileUrl, new OnUpgradeStatusListener() {
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

    private void loadFragment(boolean isDownload, ADDeviceDataResponse adDeviceDataResponse) {
        //显示title栏
        showTitleLayout(adDeviceDataResponse);

        if (adDeviceDataResponse == null) {
            //如果布局数据为空，就手动创造数据
            adDeviceDataResponse = createDeviceDataResponse();
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
                try {
                    int type = Integer.parseInt(adType);
                    if (i == 4) {
                        initWebView(adDeviceDataResponse, i);
                    } else {
                        initAdFragment(isDownload, adDeviceDataResponse, fm, i, type);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
    }

    /**
     * 手动创建布局参数
     *
     * @return ADDeviceDataResponse
     */
    private ADDeviceDataResponse createDeviceDataResponse() {
        ADDeviceDataResponse adDeviceDataResponse = new ADDeviceDataResponse();
        adDeviceDataResponse.pollInterval = 4 * 60 * 60 * 1000;//默认4个小时
        ScreenShotResponse screenShotResponse = new ScreenShotResponse();
        screenShotResponse.screenShotInterval = 15 * 60 * 1000;//默认15分钟
        screenShotResponse.screenShotImgW = 320;
        screenShotResponse.screenShotImgH = 180;
        adDeviceDataResponse.screenShot = screenShotResponse;
        ArrayList<LayoutResponse> layoutList = new ArrayList<>();
        //广告一
        LayoutResponse oneLayout = new LayoutResponse();
        oneLayout.adType = "1";
        oneLayout.adWidth = "0.83125";
        oneLayout.adHeight = "0.83125";
        oneLayout.adTop = "0.084375";
        oneLayout.adLeft = "0";
        layoutList.add(oneLayout);
        //广告二
        LayoutResponse twoLayout = new LayoutResponse();
        twoLayout.adType = "2";
        twoLayout.adWidth = "0.16689";
        twoLayout.adHeight = "0.4";
        twoLayout.adTop = "0";
        twoLayout.adLeft = "0.83315";
        layoutList.add(twoLayout);
        //广告三
        LayoutResponse threeLayout = new LayoutResponse();
        threeLayout.adType = "3";
        threeLayout.adWidth = "0.16689";
        threeLayout.adHeight = "0.6";
        threeLayout.adTop = "0.4";
        threeLayout.adLeft = "0.83315";
        layoutList.add(threeLayout);
        //广告四
        LayoutResponse fourLayout = new LayoutResponse();
        fourLayout.adType = "4";
        fourLayout.adWidth = "0.83125";
        fourLayout.adHeight = "0.084375";
        fourLayout.adTop = "0.915625";
        fourLayout.adLeft = "0";
        layoutList.add(fourLayout);
        //广告五
        LayoutResponse fiveLayout = new LayoutResponse();
        fiveLayout.adType = "5";
        fiveLayout.adWidth = "1";
        fiveLayout.adHeight = "1";
        fiveLayout.adTop = "0";
        fiveLayout.adLeft = "0";
        layoutList.add(fiveLayout);

        adDeviceDataResponse.layout = layoutList;

        return adDeviceDataResponse;
    }


    /**
     *
     */

    private void initWebView(ADDeviceDataResponse adDeviceDataResponse, int i) {

        adWebView.setLayoutResponse(adDeviceDataResponse.layout.get(i));
        adWebView.setLayoutResponses(adDeviceDataResponse.layout);
        adWebView.setDeviceId(adDeviceDataResponse.code);
        adWebView.init();


    }

    private void initAdFragment(boolean isDownload, ADDeviceDataResponse adDeviceDataResponse, FragmentManager fm, int i, int type) {
        FragmentTransaction ft = fm.beginTransaction();
        BaseFragment fragment = BaseFragment.getInstance(type);
        if (type == 1) {
            AdOneFragment adOneFragment = (AdOneFragment) fm.findFragmentByTag(Constants.FRAGMENT_TAG_PRE + 1);
            if (adOneFragment == null) {
                //广告一没有创建
                //BaseFragment fragment = BaseFragment.getInstance(type);
                initAdOne((AdOneFragment) fragment, isDownload, adDeviceDataResponse);
                fragment.setLayoutResponse(adDeviceDataResponse.layout.get(i));
                ft.add(R.id.root_main, fragment, Constants.FRAGMENT_TAG_PRE + type);
                ft.commit();
            } else {
                //广告一已经创建
                initAdOneLayout(adOneFragment, isDownload, adDeviceDataResponse, i);

            }
        } else {
            // 添加布局参数
            BaseFragment baseFragment = (BaseFragment) fm.findFragmentByTag(Constants.FRAGMENT_TAG_PRE + type);
            if (baseFragment == null) {
                //当fragment没有创建时，才创建
                fragment.setLayoutResponse(adDeviceDataResponse.layout.get(i));
                if (adDeviceDataResponse.pollInterval > 0) {
                    intervalTime = (int) adDeviceDataResponse.pollInterval;
                    fragment.setHttpIntervalTime((int) adDeviceDataResponse.pollInterval);
                }
                ft.add(R.id.root_main, fragment, Constants.FRAGMENT_TAG_PRE + type);
                ft.commit();
            } else {
                //如果fragment已经创建了就重新请求数据
                baseFragment.doHttpRequest();
            }

        }


    }

    private void initAdOne(AdOneFragment fragment, boolean isDownload, ADDeviceDataResponse adDeviceDataResponse) {
        if (isDownload) {
            fragment.setIsDownloadAPK(true);
        }

        if (adDeviceDataResponse == null) {
            return;
        }

        if (adDeviceDataResponse.screenShot != null) {
            fragment.setScreenShotResponse(adDeviceDataResponse.screenShot);
        }
        //截屏的参数
        if (adDeviceDataResponse.pollInterval > 0) {
            intervalTime = (int) adDeviceDataResponse.pollInterval;
            fragment.setPollInterval(adDeviceDataResponse.pollInterval);
        }
    }


    private void initAdOneLayout(AdOneFragment fragment, boolean isDownload, ADDeviceDataResponse adDeviceDataResponse, int i) {
        if(fragment != null){
            fragment.doHttpRequest();
        }

        if (isDownload) {
            fragment.setIsDownloadAPK(true);
        }

        if (adDeviceDataResponse == null) {
            return;
        }

        if (adDeviceDataResponse.screenShot != null) {
            fragment.setScreenShotResponse(adDeviceDataResponse.screenShot);
        }
        //截屏的参数
        if (adDeviceDataResponse.pollInterval > 0) {
            intervalTime = (int) adDeviceDataResponse.pollInterval;
            fragment.setPollInterval(adDeviceDataResponse.pollInterval);
        }

        if (adDeviceDataResponse.layout != null) {
            fragment.intLayoutParams(adDeviceDataResponse.layout.get(i));
        }


    }


    @Override
    protected void onResume() {
        super.onResume();

        /**
         * 隐藏NavigationBar
         */
        DataUtils.hideNavigationBar(this);

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
    private void showTitleLayout(ADDeviceDataResponse adDeviceDataResponse) {

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

        if(adDeviceDataResponse != null){
            if (!TextUtils.isEmpty(adDeviceDataResponse.code)) {
                textDeviceId.setText("DEVICE ID: " + adDeviceDataResponse.code);
            }
        }
        LogCat.e(" DataUtils.getDisplayMetricsWidth: " + DataUtils.getDisplayMetricsWidth(this) + "   DataUtils.getDisplayMetricsHeight: " + DataUtils.getDisplayMetricsHeight(this));

    }


    /**
     * 动态注册网络状态广播,并回调
     */
    private void registerNetworkReceiver() {
        networkBroadcastReceiver = new NetworkBroadcastReceiver();
        final IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkBroadcastReceiver, filter);

        networkBroadcastReceiver.setNetworkChangeLinstener(new NetworkBroadcastReceiver.NetworkChangeLinstener() {
            @Override
            public void networkChange(boolean hasNetwork) {
                if (isFinishing()) {
                    return;
                }
                LogCat.e("net","networkChange..............");
                //屏蔽第一次网络改变监听
                if (isInitNetState) {
                    isInitNetState = false;
                    LogCat.e("net","屏蔽第一次registerNetworkReceiver");
                    return;
                }

                if(hasNetwork){
                    isNetConnect = true;
                    if(dialog != null && dialog.isShowing()){
                        /**
                         * 隐藏NavigationBar
                         */
                        DataUtils.hideNavigationBar(MainActivity.this);
                        cancelDialogCountTimer();
                        dialog.dismiss();
                        if(receiverWifi != null && isHasRegisterWifiReceiver){
                            unregisterReceiver(receiverWifi);
                        }
                    }
                }else {
                    isNetConnect = false;
                    showWifiDialog();
                }

                AdOneFragment adOneFragment = (AdOneFragment) getFragmentManager().findFragmentByTag(FRAGMENT_TAG_AD_ONE);
                if(adOneFragment != null){
                    if(hasNetwork){

                        //有网络
                        adOneFragment.showBeforeRequestCompleted();
                        //从新加载webview
                        if(adWebView != null){
                            LogCat.e("BridgeWebView","网络重新连接，URL重新加载");
                            adWebView.reload();
                        }

                    }else{
                        //无网络
                        adOneFragment.showCompletedNotNetwork(false);
                        //隐藏webview
                        if(adWebView != null){
                            LogCat.e("BridgeWebView","网络断开，隐藏webview");
                            adWebView.setVisibility(View.GONE);
                        }
                    }
                }

                showNetStatus(hasNetwork);

            }

            private void showNetStatus(boolean hasNetwork) {
                //loading没有请求成功
                if (hasNetwork) {
                    refreshGetFirebaseToken();//获取tooken
                    //请求升级接口
                    doHttpUpdate(MainActivity.this);
                    textSpeedInfo.setText("wifi-on:0kb/s");
                } else {
                    // 显示当前的网络状态
                    textSpeedInfo.setText("wifi-off:0kb/s");
                    LogCat.e("net", "networkChange    off..............");
                    //停止下载视频和apk
                    DLUtils.cancel();
                }
            }
        });
    }

    /**
     * 当网络状态改变时，加载数据
     */
    private void reLoadHttpRequest() {
        isInitNetState = false;//此时不用屏蔽第一次网络状态监听了
        if (isDoGetDevice) {
            isDoGetDevice = true;
            FragmentManager fm = getFragmentManager();
            for (int i = 1; i < 5; i++) {
                BaseFragment baseFragment = (BaseFragment) fm.findFragmentByTag(Constants.FRAGMENT_TAG_PRE + i);
                if (baseFragment != null) {
                    baseFragment.doHttpRequest();
                }
            }
            adWebView.loadUrl();
        } else {
            doGetDeviceInfo(MainActivity.this);
        }

    }

    @Override
    protected void onGetDeviceInfoSuccess(ADDeviceDataResponse response) {
        super.onGetDeviceInfoSuccess(response);
        if (!"0".equals(response.status)) {
            LogCat.e("请求广告体接口失败 status = 1");
            doGetDeviceInfoError();
            return;
        }
        isDoGetDevice = true;
        adDeviceDataResponse = response;
        initUmeng();
        //广告体接口成功
        //加载布局
        loadFragment(false, adDeviceDataResponse);
    }

    @Override
    protected void onGetDeviceInfoError(String msg) {
        super.onGetDeviceInfoError(msg);
        doGetDeviceInfoError();
    }

    private void doGetDeviceInfoError() {
        if (!isFinishing()) {
            // 做不升级处理, 继续请求广告视频列表
            reTryTimesTwo++;
            if (reTryTimesTwo > 4) {
                reTryTimesTwo = 0;
                LogCat.e("升级接口已连续请求3次，不在请求");
                //广告体接口成功
                //加载布局
                loadFragment(false, adDeviceDataResponse);
            } else {
                LogCat.e("进行第 " + reTryTimesTwo + " 次重试请求。。。。。。。");
                doGetDeviceInfo(MainActivity.this);
            }
        }
    }



    //private int progressTest;
    private void downloadAPKNew(final String url) {
//        if (downLoadAPKUtils == null) {
//            downLoadAPKUtils = new DownLoadAPKUtils();
//        }
//
//
//        if (adOneFragment != null) {
//            //停止下载视频
//
//        }
        DLUtils.cancel();
//
//        downLoadAPKUtils.downLoad(this, turl);
//        //下载失败监听
//        downLoadAPKUtils.setOnDownLoadErrorListener(new DownLoadAPKUtils.OnDownLoadErrorListener() {
//            @Override
//            public void onDownLoadFinish(Exception e) {
//                //通知AdOneFragment去下载视频
//                LogCat.e("APKdownload", "下载apk出现错误: " + e.toString());
//                if (reTryTimes < 5) {
//                    LogCat.e("APKdownload", "下载apk文件失败，进行第 " + reTryTimes + " 次尝试,........");
//                    reTryTimes += 1;
//                    //延迟2秒再去下载
//                    rootLayout.postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            downloadAPKNew(turl);
//                        }
//                    }, 2000);
//
//                } else {
//                    LogCat.e("APKdownload", "下载apk出现错误,重试5次不再重试 ");
//                    if (adOneFragment != null) {
//                        adOneFragment.startDownloadVideo();
//                    }
//                }
//            }
//        });
//
//        //下载进度监听
//        downLoadAPKUtils.setOnDownLoadProgressListener(new DownLoadAPKUtils.OnDownLoadProgressListener() {
//
//            private int oldProgress;
//            private final int K_SIZE = 1024 * 1024;
//
//            @Override
//            public void onDownLoadProgress(int progress, long fileSize, String fileName) {
//                showNetSpeed(progress);
//            }
//            private void showNetSpeed(int progress) {
//                try {
//                    if (oldProgress <= 0) {
//                        oldProgress = progress;
//                        return;
//                    }
//                    long current = progress - oldProgress;
//                    String speed;
//                    if (current >= 0 && current < 1024) {
//                        speed = current + "B/s";
//                    } else if (current >= 1024 && current < K_SIZE) {
//                        speed = current / 1024 + "KB/s";
//                    } else {
//                        speed = current / K_SIZE + "MB/s";
//                    }
//                    LogCat.e("net_speed", "speed: " + speed);
//                    if (current > 0) {
//                        String msg = "wifi:on -" + speed + "-upgrading";
//
//                        textSpeedInfo.setText(msg);
//                        //tvSpeed.setText(msg);
////                        setSpeedInfo(msg);
//                        // 下载设备网速
//                    }
//                    oldProgress = progress;
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//
//        });
//
//        // 下载成功监听
//        downLoadAPKUtils.setOnDownLoadFinishListener(new DownLoadAPKUtils.OnDownLoadFinishListener() {
//            @Override
//            public void onDownLoadFinish(String fileName) {
//                //新包下载完成得安装
//                LogCat.e("APKdownload", "下载升级成功，开始正式升级.......");
//                File file = new File(DataUtils.getApkDirectory() + Constants.FILE_APK_NAME);
//                InstallUtils.installAuto(MainActivity.this, file, true);
//            }
//        });


        DownloadUtils.downloadApk(this, url, new OnUpgradeStatusListener() {

            private long oldProgress;
            private final int K_SIZE = 1024 * 1024;

            @Override
            public void onDownloadFileSuccess(String filePath) {
                //新包下载完成得安装
                LogCat.e("APKdownload", "下载升级成功，开始正式升级.......");
                File file = new File(DataUtils.getApkDirectory() + Constants.FILE_APK_NAME);
                InstallUtils.installAuto(MainActivity.this, file, true);
            }

            @Override
            public void onDownloadFileError(int errorCode, String errorMsg) {
                //通知AdOneFragment去下载视频
                LogCat.e("APKdownload", "下载apk出现错误: " + errorMsg);
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
                    final AdOneFragment adOneFragment = (AdOneFragment) getFragmentManager().findFragmentByTag(FRAGMENT_TAG_AD_ONE);
                    if (adOneFragment != null) {
                        adOneFragment.startDownloadVideo();
                    }
                }
            }

            @Override
            public void onDownloadProgress(long progress, long size) {
                showNetSpeed(progress, size);
            }
            String msg;
            private void showNetSpeed(final long progress, final long fileLength) {
                try {
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

                    if (current >= 0) {
                        msg = "wifi-on :" + speed + "-upgrading";
                        LogCat.e("net_speed", "speed: " + speed);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                textSpeedInfo.setText(msg);
                                String progressStr = logProgress(progress, fileLength);
                                LogCat.e("net_speed", "progressStr: " + progressStr);
                                textDownloadInfo.setText("upgrading: " + progressStr);
                            }
                        });

//                        tvSpeed.setText(msg);
//                        setSpeedInfo(msg);
                        // 下载设备网速
                    }
                    oldProgress = progress;
                } catch (Exception e) {
                    e.printStackTrace();
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
        });

    }


    @Override
    protected void onUpdateError(String errorMsg) {
        super.onUpdateError(errorMsg);
        doError(errorMsg);
    }

    private int retryUpgradeTimes;

    @Override
    protected void onUpdateSuccess(UpdateResponse response) {
        super.onUpdateSuccess(response);
        if (response.resultForApk == null) {
            if ("3".equals(response.status)) {
                LogCat.e("没有升级包，不需要更新");

                //没有升级，请求其他接口
                reLoadHttpRequest();
            } else {
                LogCat.e("升级数据出错，无法正常升级2。。。。。");
                doError("\"3\".equals(response.status) = false");
            }
            return;
        }

        if (!"1".equals(response.status)) {
            LogCat.e("升级接口的status == 0。。。。。");
            doError("升级接口的status == 0");
            return;
        }
        retryUpgradeTimes = 0;
        UpdateResponse.UpdateInfoResponse updateInfo = response.resultForApk;
        // 获取当前最新版本号
        if (!TextUtils.isEmpty(updateInfo.versionCode)) {
            double netVersonCode = Integer.parseInt(updateInfo.versionCode);
            try {
                LogCat.e("当前的app版本：" + DataUtils.getAppVersion(this));
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            LogCat.e("当前的最新版本：" + netVersonCode);

            // 检测是否要升级
            try {
                //升级接口成功
                if (DataUtils.getAppVersion(this) < netVersonCode) { // 升级
                    // 升级
                    // 下载最新安装包，下载完成后，提示安装
                    LogCat.e("需要升级。。。。。");
                    // 去下载当前的apk
                    if (!TextUtils.isEmpty(updateInfo.fileUrl)) {
                        downloadAPKNew(updateInfo.fileUrl);
                    }

                } else {
                    // 不升级,加载布局
                    LogCat.e("无需升级。。。。。");
                    // 5.清空所有升级包，为了节省空间
                    //没有升级，请求其他接口
                    reLoadHttpRequest();
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                LogCat.e("判断升级过程中出错。。。。。");
                doError("判断升级过程中出错" + e.getLocalizedMessage());
            }
        } else {
            // 不升级
            LogCat.e("升级版本为null。。。。。");
            doError("升级版本为null");
        }

    }

    private void doError(String errorMsg) {
        if (!isFinishing()) {
            // 做不升级处理, 继续请求广告视频列表
            retryUpgradeTimes++;
            if (retryUpgradeTimes >= 3) {
                retryUpgradeTimes = 0;
                LogCat.e("升级接口已连续请求3次，不在请求");
                //没有升级，请求其他接口
                reLoadHttpRequest();
            } else {
                LogCat.e("进行第 " + retryUpgradeTimes + " 次重试请求。。。。。。。");
                doHttpUpdate(MainActivity.this);
            }
        }
    }

    /**
     * 上报开始时间
     */
//    private void sendAPPStartTime() {
//        if (!TextUtils.isEmpty(DataUtils.getMacAddress(this)) && DataUtils.isNetworkConnected(this)) {
//            String msg = "{\"time\"" + ":}";
//            ErrorHttpServer.doStatisticsHttp(this, Constant.APP_START_TIME, msg, new OkHttpCallBack<ErrorResponse>() {
//                @Override
//                public void onSuccess(String turl, ErrorResponse response) {
//                    LogCat.e("MainActivity", "上传开机时间成功");
//                }
//
//                @Override
//                public void onError(String turl, String errorMsg) {
//                    LogCat.e("MainActivity", "上传开机时间失败");
//                }
//            });
//        }
//    }

    /**
     * 设置下载信息
     *
     * @param info
     */
    public void setDownloadInfo(String info) {
        if (!TextUtils.isEmpty(info) && textDownloadInfo != null) {
            textDownloadInfo.setVisibility(View.VISIBLE);
            textDownloadInfo.setText(info);
        } else {
            if (textDownloadInfo != null) {
                textDownloadInfo.setVisibility(View.GONE);
            }
        }
    }

    /**
     * 设置下载速度
     *
     * @param info
     */
    public void setSpeedInfo(String info) {
        if (!TextUtils.isEmpty(info)) {
            if (View.VISIBLE != textSpeedInfo.getVisibility()) {
                textSpeedInfo.setVisibility(View.VISIBLE);
            }
            LogCat.e("net","show msg " + info);
            textSpeedInfo.setText(info);
        }
    }


    /**
     *
     * @param context
     * @param deviceId
     */
    private static void doHttpUpdateToken(Context context, String deviceId,String token) {
        Map<String, String> params = new HashMap<>();
        params.put("mac", MacUtils.getMacAddress(context));
        params.put("deviceId", deviceId);
        params.put("token", token);
//        LogCat.e("push", "上传信息成功...........deviceId： " + deviceId);
        UpPushInfoService.doHttpUpPushInfo(params, new OkHttpCallBack<AdVideoListResponse>() {
            @Override
            public void onSuccess(String url, AdVideoListResponse response) {
                LogCat.e("push", "上传信息成功...........");
            }

            @Override
            public void onError(String url, String errorMsg) {
                LogCat.e("push", "上传信息失败...........");
            }
        });
    }




    class WifiReceiver extends BroadcastReceiver {



        public void onReceive(Context c, Intent intent) {
            Log.e("TAG", "onReceive..........");

//            ArrayList<WifiInfos> wifiInfoses = new ArrayList<>();
//            for (int i = 0; i < wifiList.size(); i++) {
//                ScanResult scanResult = wifiList.get(i);
//                Log.e("TAG", "wifi_name: " + scanResult.SSID + " 加密: " + scanResult.capabilities + "  ");
//            }

            unregisterReceiver(receiverWifi);
            Log.e("TAG", ".........................");
//            String wifiProperty = "当前连接Wifi信息如下："+wifiInfo.getSSID()+'\n'+
//                    "ip:"     +     FormatString(dhcpInfo.ipAddress)   +'\n'+
//                    "mask:"   +     FormatString(dhcpInfo.netmask)     +'\n'+
//                    "netgate:"+     FormatString(dhcpInfo.gateway)     +'\n'+
//                    "dns:"    +     FormatString(dhcpInfo.dns1)  ;
//            Log.e("TAG", wifiProperty);
//            Log.e("TAG", sb.toString());
            isHasRegisterWifiReceiver = false;
            dialog = new WifiDialog(MainActivity.this, wifiAutoConnectManager, (ArrayList<ScanResult>) wifiAutoConnectManager.wifiManager.getScanResults());
            if(!isNetConnect){
                //没有联网是才show
                dialog.show();
                startDialogCountTimer();
            }
        }

    }


    /**
     * 定时器
     */
    private  DialogCountDownTimer dialogCountDownTimer;
    class DialogCountDownTimer extends CountDownTimer{


        public DialogCountDownTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long l) {
            if(dialog != null){
                dialog.showCountTime(String.valueOf(l/1000));
            }
        }

        @Override
        public void onFinish() {
            //倒计时完成，隐藏dialog
            hideDialog();
        }
    }


    /**
     * 开始定时器
     */
    public void startDialogCountTimer(){
        if(dialogCountDownTimer == null){
            dialogCountDownTimer = new DialogCountDownTimer(30*1000,1000);
        }
        dialogCountDownTimer.start();
    }



    /**
     * 隐藏dialog
     */
    private void hideDialog(){
        if(dialog != null && dialog.isShowing()){
            /**
             * 隐藏NavigationBar
             */
            DataUtils.hideNavigationBar(this);
            dialog.dismiss();
        }
    }

    /**
     * 取消定时器
     */
    public void cancelDialogCountTimer(){
        if(dialogCountDownTimer != null){
            dialogCountDownTimer.cancel();
        }

    }



}
