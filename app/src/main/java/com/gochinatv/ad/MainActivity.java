package com.gochinatv.ad;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
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
import com.gochinatv.ad.tools.Constants;
import com.gochinatv.ad.tools.DataUtils;
import com.gochinatv.ad.tools.DownLoadAPKUtils;
import com.gochinatv.ad.tools.InstallUtils;
import com.gochinatv.ad.tools.LogCat;
import com.gochinatv.ad.tools.SharedPreference;
import com.gochinatv.ad.ui.fragment.AdOneFragment;
import com.gochinatv.ad.ui.view.AdWebView;
import com.gochinatv.statistics.server.ErrorHttpServer;
import com.gochinatv.statistics.tools.Constant;
import com.google.gson.Gson;
import com.okhtttp.OkHttpCallBack;
import com.okhtttp.response.ADDeviceDataResponse;
import com.okhtttp.response.AdVideoListResponse;
import com.okhtttp.response.CommendResponse;
import com.okhtttp.response.ErrorResponse;
import com.okhtttp.response.LayoutResponse;
import com.okhtttp.response.UpdateResponse;
import com.okhtttp.service.UpPushInfoService;
import com.tools.MacUtils;
import com.umeng.analytics.MobclickAgent;
import com.umeng.message.PushAgent;
import com.umeng.message.UmengMessageHandler;
import com.umeng.message.UmengRegistrar;
import com.umeng.message.entity.UMessage;

import java.io.File;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by fq_mbp on 16/3/17.
 */
public class MainActivity extends BaseActivity {
    //private Button testButton;
    //boolean isClick = true;
    private RelativeLayout rootLayout;
    private RelativeLayout titleLayout;
    private TextView textDeviceId;
    private final String FRAGMENT_TAG_AD_ONE = "ad_1";
    private static final int HANDLER_MSG_TOKEN = 1000;
    private static final int HANDLER_MSG_GET_TOKEN = 1001;

    //下载apk尝试的次数
    private int reTryTimes;
    private PushAgent mPushAgent;

    private MyHandler myHandler;
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
        adWebView = (AdWebView) findViewById(R.id.ad_web);
    }

    boolean isDoGetDevice;

    private void init() {

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


        //上报开机时间
        sendAPPStartTime();
    }


    // 清空fragment的状态
    private void cleanFragmentState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            String FRAGMENTS_TAG = "android:support:fragments";
            savedInstanceState.remove(FRAGMENTS_TAG);
        }
    }


    private static String pushToken;

    private void initPush(String mac) {
        mPushAgent = PushAgent.getInstance(this);
        LogCat.e("device_token", "start............");
        mPushAgent.enable();
        mPushAgent.onAppStart();
        mPushAgent.setMessageChannel(mac);
        mPushAgent.setDebugMode(false);

        myHandler = new MyHandler(this);
        myHandler.sendEmptyMessage(HANDLER_MSG_GET_TOKEN);
        myHandler.setAdDeviceDataResponse(adDeviceDataResponse);

        mPushAgent.setMessageHandler(new UmengMessageHandler() {

            @Override
            public void dealWithCustomMessage(Context context, UMessage uMessage) {
                super.dealWithCustomMessage(context, uMessage);
                LogCat.e("push: " + uMessage.custom);
                if (TextUtils.isEmpty(uMessage.custom)) {
                    LogCat.e("push...........收到的命令为null");
                    return;
                }
                LogCat.e("push", "commend -> json: " + uMessage.custom);
                dispatchCommend(uMessage);
            }
        });
    }

    private void dispatchCommend(UMessage uMessage) {
        CommendResponse commendResponse = null;
        try {
            commendResponse = new Gson().fromJson(uMessage.custom, CommendResponse.class);
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

    private void sendCmd(UMessage uMessage, CommendResponse commendResponse) {
        if ("1".equals(commendResponse.getIsJsCommand())) {   // 对js的命令
            LogCat.e("push", "push...........commend  code ......" + commendResponse.getIsJsCommand());
            cmdJs(uMessage);
        } else { // 本地的命令
            cmdLocal(commendResponse);
        }
    }

    private void cmdJs(UMessage uMessage) {
        if (adWebView != null) {
            LogCat.e("push", "push...........adWebView != null");
            adWebView.setCommendInfo(uMessage.custom);
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
                initPush(mac);
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
        DLUtils.cancel();
        if (downLoadAPKUtils != null) {
            downLoadAPKUtils.stopDownLoad();
        }
        if (myHandler != null) {
            myHandler = null;
        }
        if (mPushAgent != null) {
            mPushAgent.disable();
        }
        super.onStop();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        //卸载广播
        if (networkBroadcastReceiver != null) {
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
            oneFT.add(R.id.root_main, adOneFragment, Constants.FRAGMENT_TAG_PRE + 1);
            oneFT.commit();
        }
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

    private void loadFragment(boolean isDownload, ADDeviceDataResponse adDeviceDataResponse) {
        //显示title栏
        if (adDeviceDataResponse == null) {
            titleLayout.setVisibility(View.VISIBLE);
        } else {
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
                try {
                    int type = Integer.parseInt(adType);
                    if (i == 4) {
                        initWebView(adDeviceDataResponse, i);
                    } else {
                        initAdFragment(isDownload, adDeviceDataResponse, fm, i, type);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        }
    }

    private void initWebView(ADDeviceDataResponse adDeviceDataResponse, int i) {
        if(adWebView != null){
            adWebView.setWebViewClient( new WebViewClient(){
                @Override
                public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                    super.onReceivedError(view, request, error);
                    //加载失败监听
                    LogCat.e("MainActivity", "webview加载失败！！！！！！！！！！！！");
                    adWebView.setVisibility(View.GONE);
                    //view.stopLoading();
                    //view.clearView();
                }
            });
        }
        adWebView.init();
        adWebView.setLayoutResponse(adDeviceDataResponse.layout.get(i));
        adWebView.setLayoutResponses(adDeviceDataResponse.layout);
        adWebView.setDeviceId(adDeviceDataResponse.code);



    }

    private void initAdFragment(boolean isDownload, ADDeviceDataResponse adDeviceDataResponse, FragmentManager fm, int i, int type) {
        FragmentTransaction ft = fm.beginTransaction();
        BaseFragment fragment = BaseFragment.getInstance(type);
        if(type == 1){
            AdOneFragment adOneFragment = (AdOneFragment) fm.findFragmentByTag(Constants.FRAGMENT_TAG_PRE + 1);
            if(adOneFragment == null){
                //广告一没有创建
                //BaseFragment fragment = BaseFragment.getInstance(type);
                initAdOne((AdOneFragment) fragment, isDownload, adDeviceDataResponse);
                fragment.setLayoutResponse(adDeviceDataResponse.layout.get(i));
                ft.add(R.id.root_main, fragment, Constants.FRAGMENT_TAG_PRE + type);
                ft.commit();
            }else{
                //广告一已经创建
                initAdOneLayout(adOneFragment, isDownload, adDeviceDataResponse,i);

            }
        }else{
            // 添加布局参数
            fragment.setLayoutResponse(adDeviceDataResponse.layout.get(i));
            if (adDeviceDataResponse.pollInterval > 0) {
                fragment.setHttpIntervalTime((int)adDeviceDataResponse.pollInterval);
            }
            ft.add(R.id.root_main, fragment, Constants.FRAGMENT_TAG_PRE + type);
            ft.commit();
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
            fragment.setPollInterval(adDeviceDataResponse.pollInterval);
        }
    }


    private void initAdOneLayout(AdOneFragment fragment, boolean isDownload, ADDeviceDataResponse adDeviceDataResponse,int i) {
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
            fragment.setPollInterval(adDeviceDataResponse.pollInterval);
        }

        if(adDeviceDataResponse.layout != null){
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

                if(isInitNetState){
                    isInitNetState = false;
                    return;
                }

                if (hasNetwork) {
                    //当有网络时执行
                    reLoadHttpRequest();
                    //请求升级接口
                    doHttpUpdate(MainActivity.this);
                } else {
                    // 显示当前的网络状态
                    AdOneFragment adOneFragment = (AdOneFragment) getFragmentManager().findFragmentByTag(FRAGMENT_TAG_AD_ONE);
                    if (adOneFragment != null) {
                        adOneFragment.showNetSpeed(false, false, 0);
                    }
                }
            }
        });
    }

    /**
     * 当网络状态改变时，加载数据
     */
    private void reLoadHttpRequest() {
        if (isDoGetDevice) {
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

    private static class MyHandler extends Handler {

        private Context context;

        private ADDeviceDataResponse adDeviceDataResponse;

        public MyHandler(Context context) {
            this.context = context;
        }

        public void setAdDeviceDataResponse(ADDeviceDataResponse adDeviceDataResponse) {
            this.adDeviceDataResponse = adDeviceDataResponse;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case HANDLER_MSG_TOKEN:
                    if (TextUtils.isEmpty(pushToken)) {
                        LogCat.e("push", "仍未获取到token，继续延迟请求............");
                        sendEmptyMessage(HANDLER_MSG_GET_TOKEN);
                        return;
                    }
                    LogCat.e("push", "获取到token............" + pushToken);
                    String deviceId = null;
                    if (adDeviceDataResponse != null) {
                        deviceId = adDeviceDataResponse.code;
                    }
                    doHttpUpdateUserInfo(context, deviceId);
                    break;
                case HANDLER_MSG_GET_TOKEN:
                    if(((Activity)context).isFinishing()){
                        return;
                    }
                    pushToken = getPushToken(context);
                    sendEmptyMessageDelayed(HANDLER_MSG_TOKEN, 2000);
                    break;
            }
        }
    }

    private static String getPushToken(Context context) {
        LogCat.e("push", "请求token............");
        return UmengRegistrar.getRegistrationId(context);
    }

    private static void doHttpUpdateUserInfo(Context context, String deviceId) {
        Map<String, String> params = new HashMap<>();
        params.put("mac", MacUtils.getMacAddress(context));
        params.put("deviceId", deviceId);

        params.put("token", pushToken);
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
                    if(!TextUtils.isEmpty(updateInfo.fileUrl)){
                        downloadAPKNew(updateInfo.fileUrl);
                    }

                } else {
                    // 不升级,加载布局
                    LogCat.e("无需升级。。。。。");
                    // 5.清空所有升级包，为了节省空间
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
            } else {
                LogCat.e("进行第 " + retryUpgradeTimes + " 次重试请求。。。。。。。");
                doHttpUpdate(MainActivity.this);
            }
        }
    }

    /**
     * 上报开始时间
     */
    private void sendAPPStartTime(){
        if(!TextUtils.isEmpty(DataUtils.getMacAddress(this)) && DataUtils.isNetworkConnected(this)){
            ErrorHttpServer.doStatisticsHttp(this, Constant.APP_START_TIME, "开机时间", new OkHttpCallBack<ErrorResponse>() {
                @Override
                public void onSuccess(String url, ErrorResponse response) {
                    LogCat.e("MainActivity","上传开机时间成功");
                }

                @Override
                public void onError(String url, String errorMsg) {
                    LogCat.e("MainActivity","上传开机时间失败");
                }
            });
        }
    }
}
