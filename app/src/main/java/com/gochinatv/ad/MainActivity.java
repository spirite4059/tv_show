package com.gochinatv.ad;

import android.Manifest.permission;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.download.DLUtils;
import com.gochinatv.ad.interfaces.OnUpgradeStatusListener;
import com.gochinatv.ad.tools.Constants;
import com.gochinatv.ad.tools.DataUtils;
import com.gochinatv.ad.tools.DownLoadAPKUtils;
import com.gochinatv.ad.tools.DownloadUtils;
import com.gochinatv.ad.tools.InstallUtils;
import com.gochinatv.ad.tools.LogCat;
import com.gochinatv.ad.tools.SharedPreference;
import com.gochinatv.ad.tools.UmengUtils;
import com.gochinatv.ad.ui.fragment.ADFourFragment;
import com.gochinatv.ad.ui.fragment.ADThreeOtherFragment;
import com.gochinatv.ad.ui.fragment.ADTwoFragment;
import com.gochinatv.ad.ui.fragment.AdOneFragment;
import com.gochinatv.statistics.SendStatisticsLog;
import com.gochinatv.statistics.request.RetryErrorRequest;
import com.okhtttp.OkHttpCallBack;
import com.okhtttp.OkHttpUtils;
import com.okhtttp.response.ADDeviceDataResponse;
import com.okhtttp.response.LayoutResponse;
import com.okhtttp.response.UpdateResponse;
import com.okhtttp.service.ADHttpService;
import com.tools.HttpUrls;
import com.umeng.analytics.MobclickAgent;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by fq_mbp on 16/3/17.
 */
public class MainActivity extends Activity {

    private RelativeLayout rootLayout;
    private ImageView imgLoge;//LOGE图

    private LinearLayout loadingView;
    /**
     * 下载info
     */
    private UpdateResponse.UpdateInfoResponse updateInfo;
    private AdOneFragment adOneFragment;

    /**
     * 是否升级接口成功
     */
    private boolean isUpgradeSucceed;
    /**
     * 是否有新版本升级
     */
    private boolean isHasUpgrade;


    /**
     * 是否请求广告体接口成功
     */
    private boolean isGetDerviceSucceed;

    private ADDeviceDataResponse adDeviceDataResponse;//

    //定时器--在开机自启前网络没连上是用
    private Handler handler;
    private int runnableTimes;//post-Runnable的次数
    //private Runnable runnable;


    /**
     * 升级接口日志
     */
    private ArrayList<RetryErrorRequest> upgradeLogList;

    /**
     * 布局接口日志
     */
    private ArrayList<RetryErrorRequest> layoutLogList;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 情况fragment的状态，保证getActivity不为null
        cleanFragmentState(savedInstanceState);
        setContentView(R.layout.activity_main);
        rootLayout = (RelativeLayout) findViewById(R.id.root_main);
        loadingView = (LinearLayout) findViewById(R.id.loading);
        imgLoge = (ImageView) findViewById(R.id.img_loge);
        init();
    }

    // 清空fragment的状态
    private void cleanFragmentState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            String FRAGMENTS_TAG = "android:support:fragments";
            savedInstanceState.remove(FRAGMENTS_TAG);
        }
    }


    private void init() {
        // 删除升级安装包
        deleteUpdateApk();

        handler = new Handler(Looper.getMainLooper());

        initUmeng();

        /**
         * 隐藏NavigationBar
         */
        DataUtils.hideNavigationBar(this);

        // 请求网络
        doHttp();

        /**
         * 如果要启动测试，需要注释此段代码，否则无法正常启动
         */
        if (!Constants.isTest) {
            DataUtils.startAppServer(this);
        }



    }

    private void doHttp() {
        if (DataUtils.isNetworkConnected(this)) {
            LogCat.e("网络已连接，请求接口");
            doHttpUpdate(MainActivity.this);
            doGetDeviceInfo();
            SendStatisticsLog.sendInitializeLog(this);//提交激活日志
        } else {
            LogCat.e("网络未连接，继续判断网络是否连接");
            handler.postDelayed(runnable, 10000);
        }
    }


    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            runnableTimes++;
            if (DataUtils.isNetworkConnected(MainActivity.this)) {
                LogCat.e("网络已连接，请求接口，并且移除runnable");
                handler.removeCallbacks(runnable);
                doHttpUpdate(MainActivity.this);
                doGetDeviceInfo();
                SendStatisticsLog.sendInitializeLog(MainActivity.this);//提交激活日志
            } else {
                if (runnableTimes > 4) {
                    LogCat.e("请求5次后不再请求，进入广告一");
                    isUpgradeSucceed = true;
                    isGetDerviceSucceed = true;
                    loadFragmentTwo(isHasUpgrade);
                } else {
                    LogCat.e("网络未连接，继续判断网络是否连接");
                    handler.postDelayed(runnable, 10000);
                }
            }
        }
    };

    private void initUmeng() {
        if(isFinishing()){
            return;
        }

        String mac = DataUtils.getMacAddress(MainActivity.this);
        SharedPreference sharedPreference = SharedPreference.getSharedPreferenceUtils(MainActivity.this);
        if (!TextUtils.isEmpty(mac)) {
            final String macAddress = mac.replaceAll(":", "");
            LogCat.e("mac: " + macAddress);
            MobclickAgent. startWithConfigure(new MobclickAgent.UMAnalyticsConfig(this, "572c1246e0f55aa6c5001533", mac, MobclickAgent.EScenarioType.E_UM_NORMAL));
            MobclickAgent.openActivityDurationTrack(false);
            MobclickAgent.setCatchUncaughtExceptions(true);
            MobclickAgent.setDebugMode(false);
            if(sharedPreference != null){
                sharedPreference.saveDate(Constants.SHARE_KEY_UMENG, true);
            }
        }else {
            if(sharedPreference != null){
                sharedPreference.saveDate(Constants.SHARE_KEY_UMENG, false);
            }
        }
    }






    private void deleteUpdateApk() {
        File file = new File(DataUtils.getApkDirectory() + Constants.FILE_APK_NAME);
        if (file.exists()) {
            file.delete();
        }
    }

    private void testInstall() {
        File file = Environment.getExternalStorageDirectory();
//
        File fileApk = new File(file.getAbsolutePath() + "/Music/test.apk");

        LogCat.e("fileApk: " + fileApk.getAbsolutePath());
//
        PackageInfo pInfo = null;
        try {
            pInfo = getPackageManager().getPackageInfo("com.gochinatv.ad", 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if(InstallUtils.hasRootPermission()){
            //  have  root
            InstallUtils.installSilent(this, fileApk.getAbsolutePath(), true);
            Toast.makeText(this, "提醒：获取到root权限，可以静默升级！", Toast.LENGTH_LONG).show();
            // rootClientInstall(apkFile.getAbsolutePath());
        }else if (InstallUtils.isSystemApp(pInfo) || InstallUtils.isSystemUpdateApp(pInfo)){
//                Toast.makeText(context,"正在更新软件！",Toast.LENGTH_SHORT).show();
            InstallUtils.installSilent(this, fileApk.getAbsolutePath(), false);
            Toast.makeText(this,"提醒：获取到系统权限，可以静默升级！",Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(this,"提醒：没有获取到系统权限和root权限，请选择普通安装！",Toast.LENGTH_LONG).show();
        }
    }


    @Override
    protected void onStop() {

        DLUtils.cancel();
        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
        }

        if(downLoadAPKUtils != null ){
            downLoadAPKUtils.stopDownLoad();
        }

//        if (handler != null && progressRunnable != null) {
//            handler.removeCallbacks(progressRunnable);
//        }

        super.onStop();
    }

    /**
     * 检查是否有版本更新
     */
    private int reTryTimes;

    protected void doHttpUpdate(final Context context) {
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
            ApplicationInfo appInfo = this.getPackageManager().getApplicationInfo(this.getPackageName(),
                    PackageManager.GET_META_DATA);
            if (appInfo != null) {
                String brand = appInfo.metaData.getString("UMENG_CHANNEL");
                if (TextUtils.isEmpty(brand)) {
                    String name = Constants.isTest ? "ctTest" : "chinarestaurant";//如果是测试：ctTest；否则：chinarestaurant
                    map.put("brandNumber", name); // 品牌
                } else {
                    map.put("brandNumber", brand); // 品牌
                }
            }
        } catch (PackageManager.NameNotFoundException e1) {
            e1.printStackTrace();
            String name = Constants.isTest ? "ctTest" : "chinarestaurant";//如果是测试：ctTest；否则：chinarestaurant
            map.put("brandNumber", name); // 品牌
        }

        OkHttpUtils.getInstance().
                doHttpGet(HttpUrls.URL_CHECK_UPDATE, map, new OkHttpCallBack<UpdateResponse>() {
                    @Override
                    public void onSuccess(String url, UpdateResponse response) {
                        LogCat.e("onSuccess adVideoUrl: " + url);
                        if (isFinishing()) {
                            return;
                        }

                        if (response == null) {
                            LogCat.e("升级数据出错，无法正常升级1。。。。。");
                            doError();
                            return;
                        }

                        if (response.resultForApk == null) {
                            if ("3".equals(response.status)) {
                                isUpgradeSucceed = true;
                                //loadFragment(false);
                                loadFragmentTwo(isHasUpgrade);
                                LogCat.e("没有升级包，不需要更新");
                            } else {
                                LogCat.e("升级数据出错，无法正常升级2。。。。。");
                                doError();
                            }
                            return;
                        }

                        if (!"1".equals(response.status)) {
                            LogCat.e("升级接口的status == 0。。。。。");
                            doError();
                            return;
                        }
                        reTryTimes = 0;
                        updateInfo = response.resultForApk;
                        // 获取当前最新版本号
                        if (!TextUtils.isEmpty(updateInfo.versionCode)) {
                            double netVersonCode = Integer.parseInt(updateInfo.versionCode);
                            try {
                                LogCat.e("当前的app版本：" + DataUtils.getAppVersion(context));
                            } catch (PackageManager.NameNotFoundException e) {
                                e.printStackTrace();
                            }
                            LogCat.e("当前的最新版本：" + netVersonCode);

                            // 检测是否要升级
                            try {
                                //升级接口成功
                                isUpgradeSucceed = true;
                                if (DataUtils.getAppVersion(context) < netVersonCode) { // 升级
                                    // 升级
                                    // 下载最新安装包，下载完成后，提示安装
                                    LogCat.e("需要升级。。。。。");
                                    // 去下载当前的apk
                                    isHasUpgrade = true;


//                                    testInstall();

                                    downloadAPKNew();

                                    //downloadAPKOld();
                                    // 加载布局.但是不让AdOneFragment，下载视频
                                    //loadFragment(true);
                                    loadFragmentTwo(isHasUpgrade);
                                } else {
                                    // 不升级,加载布局
                                    LogCat.e("无需升级。。。。。");
                                    // 5.清空所有升级包，为了节省空间

                                    LogCat.e("清空升级apk.....");
                                    //loadFragment(false);
                                    loadFragmentTwo(isHasUpgrade);
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
                            reTryTimes++;
                            if (reTryTimes >= 3) {
                                reTryTimes = 0;
                                LogCat.e("升级接口已连续请求3次，不在请求");
                                //升级接口成功
                                isUpgradeSucceed = true;
                                //loadFragment(false);
                                loadFragmentTwo(isHasUpgrade);
                            } else {
                                LogCat.e("进行第 " + reTryTimes + " 次重试请求。。。。。。。");
                                doHttpUpdate(MainActivity.this);
                            }
                        }
                    }

                    @Override
                    public void onError(String url, String errorMsg) {
                        LogCat.e("请求接口出错，无法升级。。。。。" + url);
                        doError();
                        //存储错误日志
                        upgradeLogList = new ArrayList<RetryErrorRequest>();
                        RetryErrorRequest request = new RetryErrorRequest();
                        request.retry = String.valueOf(reTryTimes);
                        request.errorMsg = errorMsg;
                        upgradeLogList.add(request);
                    }
                });

    }

    /**
     * 加载fragment
     *
     * @param isDownload
     */
    private void loadFragment(boolean isDownload) {
        if (isFinishing()) {
            return;
        }
        rootLayout.setBackground(null);
        rootLayout.setBackgroundColor(Color.BLACK);
        imgLoge.setVisibility(View.VISIBLE);
        loadingView.setVisibility(View.GONE);
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        adOneFragment = new AdOneFragment();
        if (isDownload) {
            adOneFragment.setIsDownloadAPK(true);
        }
        ft.add(R.id.root_main, adOneFragment);
//        ft.add(R.id.root_main, new ADTwoFragment());
//        //ft.add(R.id.root_main, new ADThreeFragment());
//        ft.add(R.id.root_main, new AdFiveFragment());
//        ft.add(R.id.root_main, new ADFourFragment());

        //ft.add(R.id.root_main, new TestFragment());
        ft.commit();
    }


    /**
     * 只有视频广告
     *
     * @param
     */
    private void showOneAD() {
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
            adOneFragment.setLayoutResponse(oneLayout);
            oneFT.add(R.id.root_main, adOneFragment);
            oneFT.commit();
        }
    }

    /**
     * 新下载apk方法
     */
    private DownLoadAPKUtils downLoadAPKUtils;
    //private int progressTest;
    private void downloadAPKNew() {
        if(downLoadAPKUtils == null) {
            downLoadAPKUtils = new DownLoadAPKUtils();
        }
        downLoadAPKUtils.downLoad(this,updateInfo.fileUrl);

        //下载失败监听
        downLoadAPKUtils.setOnDownLoadErrorListener(new DownLoadAPKUtils.OnDownLoadErrorListener() {
            @Override
            public void onDownLoadFinish(Exception e) {
                //通知AdOneFragment去下载视频
                LogCat.e("APKdownload","下载apk出现错误: "+e.toString());
                if (reTryTimes < 5) {
                    LogCat.e("APKdownload","下载apk文件失败，进行第 " + reTryTimes + " 次尝试,........");
                    reTryTimes += 1;
                    //延迟2秒再去下载
                    rootLayout.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            downloadAPKNew();
                        }
                    },2000);

                } else {
                    LogCat.e("APKdownload","下载apk出现错误,重试5次不再重试 ");
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
                LogCat.e("APKdownload","APK已经下载了 progress: " + progress +"%");
            }
        });

        // 下载成功监听
        downLoadAPKUtils.setOnDownLoadFinishListener(new DownLoadAPKUtils.OnDownLoadFinishListener() {
            @Override
            public void onDownLoadFinish(String fileName) {
                //新包下载完成得安装
                LogCat.e("APKdownload","下载升级成功，开始正式升级.......");
                File file = new File(DataUtils.getApkDirectory() + Constants.FILE_APK_NAME);
                InstallUtils.installAuto(MainActivity.this, file, true);
            }
        });

    }

    /**
     * 之前的下载apk方法
     */
    private void downloadAPKOld() {
        DownloadUtils.download(true, getApplication(), DataUtils.getApkDirectory(), Constants.FILE_APK_NAME, updateInfo.fileUrl, new OnUpgradeStatusListener() {
            @Override
            public void onDownloadFileSuccess(String filePath) {
                //新包下载完成得安装
                LogCat.e("下载升级成功，开始正式升级.......");
                File file = new File(DataUtils.getApkDirectory() + Constants.FILE_APK_NAME);
                InstallUtils.installAuto(MainActivity.this, file, true);
                //MainActivity.this.finish();
            }

            @Override
            public void onDownloadFileError(int errorCode, String errorMsg) {
                //通知AdOneFragment去下载视频
                if (reTryTimes < 3) {
                    LogCat.e("下载apk文件失败，进行第 " + reTryTimes + " 次尝试,........");
                    reTryTimes += 1;
                    downloadAPKOld();
                } else {
                    LogCat.e("下载apk出现错误");
                    if (adOneFragment != null) {
                        adOneFragment.startDownloadVideo();
                    }
                }

            }

            @Override
            public void onDownloadProgress(long progress, long fileSize) {

            }
        });
    }


    /**
     * 请求广告体接口---布局大小
     */

    //布局数据
    //截屏数据
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
                isGetDerviceSucceed = true;
                //加载布局
                loadFragmentTwo(isHasUpgrade);

                UmengUtils.onEvent(MainActivity.this, UmengUtils.UMENG_APP_START_TIME, DataUtils.getFormatTime(adDeviceDataResponse.currentTime));

            }


            private void doError() {
                if (!isFinishing()) {
                    // 做不升级处理, 继续请求广告视频列表
                    reTryTimesTwo++;
                    if (reTryTimesTwo > 4) {
                        reTryTimesTwo = 0;
                        LogCat.e("升级接口已连续请求3次，不在请求");
                        //广告体接口成功
                        isGetDerviceSucceed = true;
                        //加载布局
                        loadFragmentTwo(isHasUpgrade);
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
                layoutLogList = new ArrayList<RetryErrorRequest>();
                RetryErrorRequest request = new RetryErrorRequest();
                request.retry = String.valueOf(reTryTimesTwo);
                request.errorMsg = errorMsg;
                layoutLogList.add(request);
            }
        });


    }


    /**
     * 加载fragment
     *
     * @param isDownload
     */
    private LayoutResponse oneLayout;//广告一布局
    private LayoutResponse twoLayout;//广告二布局
    private LayoutResponse threeLayout;//广告三布局
    private LayoutResponse fourLayout;//广告四布局

    private void loadFragmentTwo(boolean isDownload) {
        LogCat.e(" isUpgradeSucceed " + isUpgradeSucceed + "  isGetDerviceSucceed    " + isGetDerviceSucceed);
        //当升级和广告体接口都完成后，才加载布局
        if (isUpgradeSucceed && isGetDerviceSucceed) {
            rootLayout.setBackground(null);
            rootLayout.setBackgroundColor(Color.BLACK);
            loadingView.setVisibility(View.GONE);
            imgLoge.setVisibility(View.VISIBLE);

            adOneFragment = new AdOneFragment();
            if (isDownload) {
                adOneFragment.setIsDownloadAPK(true);
            }
            if (adDeviceDataResponse == null) {
                showOneAD();
            } else {
                if (adDeviceDataResponse.screenShot != null) {
                    //截屏的参数
                    //adOneFragment.
                    adOneFragment.setScreenShotResponse(adDeviceDataResponse.screenShot);
                }

                if (adDeviceDataResponse.pollInterval > 0) {
                    //截屏的参数
                    //adOneFragment.
                    adOneFragment.setPollInterval(adDeviceDataResponse.pollInterval);
                }

                if (!TextUtils.isEmpty(adDeviceDataResponse.adStruct)) {
                    if ("1".equals(adDeviceDataResponse.adStruct)) {
                        //一个广告位
                        showOneAD();
                    } else if ("4".equals(adDeviceDataResponse.adStruct)) {
                        //四个广告位
                        //遍历获取布局参数
                        if (adDeviceDataResponse.layout != null && adDeviceDataResponse.layout.size() > 0) {
                            int size = adDeviceDataResponse.layout.size();
                            for (int i = 0; i < size; i++) {
                                if ("1".equals(adDeviceDataResponse.layout.get(i).adType)) {
                                    oneLayout = adDeviceDataResponse.layout.get(i);
                                } else if ("2".equals(adDeviceDataResponse.layout.get(i).adType)) {
                                    twoLayout = adDeviceDataResponse.layout.get(i);
                                } else if ("3".equals(adDeviceDataResponse.layout.get(i).adType)) {
                                    threeLayout = adDeviceDataResponse.layout.get(i);
                                } else if ("4".equals(adDeviceDataResponse.layout.get(i).adType)) {
                                    fourLayout = adDeviceDataResponse.layout.get(i);
                                }
                            }
                        }

                        FragmentManager fm = getFragmentManager();
                        FragmentTransaction ft = fm.beginTransaction();
                        //广告一
                        if (oneLayout != null) {
                            if (!TextUtils.isEmpty(oneLayout.adWidth) && !TextUtils.isEmpty(oneLayout.adHeight)
                                    && !TextUtils.isEmpty(oneLayout.adTop) && !TextUtils.isEmpty(oneLayout.adLeft)) {
                                //此时加载广告一
                                LogCat.e("成功加载了广告一");
                                adOneFragment.setLayoutResponse(oneLayout);
                                ft.add(R.id.root_main, adOneFragment);
                            }
                        }
                        //广告二
                        if (twoLayout != null) {
                            if (!TextUtils.isEmpty(twoLayout.adWidth) && !TextUtils.isEmpty(twoLayout.adHeight)
                                    && !TextUtils.isEmpty(twoLayout.adTop) && !TextUtils.isEmpty(twoLayout.adLeft)) {
                                //此时加载广告二
                                LogCat.e("成功加载了广告二");
                                ADTwoFragment adTwoFragment = new ADTwoFragment();
                                adTwoFragment.setLayoutResponse(twoLayout);
                                if (adDeviceDataResponse.pollInterval > 0) {
                                    adTwoFragment.setGetWebADTime((int)adDeviceDataResponse.pollInterval);
                                }
                                ft.add(R.id.root_main, adTwoFragment);
                            }
                        }
                        //广告三
                        if (threeLayout != null) {
                            if (!TextUtils.isEmpty(threeLayout.adWidth) && !TextUtils.isEmpty(threeLayout.adHeight)
                                    && !TextUtils.isEmpty(threeLayout.adTop) && !TextUtils.isEmpty(threeLayout.adLeft)) {
                                //此时加载广告三
                                LogCat.e("成功加载了广告三");
                                ADThreeOtherFragment adThreeFragment = new ADThreeOtherFragment();
                                adThreeFragment.setLayoutResponse(threeLayout);
                                ft.add(R.id.root_main, adThreeFragment);
                            }
                        }
                        //广告四
                        if (fourLayout != null) {
                            if (!TextUtils.isEmpty(fourLayout.adWidth) && !TextUtils.isEmpty(fourLayout.adHeight)
                                    && !TextUtils.isEmpty(fourLayout.adTop) && !TextUtils.isEmpty(fourLayout.adLeft)) {
                                //此时加载广告四
                                LogCat.e("成功加载了广告四");
                                ADFourFragment adFourFragment = new ADFourFragment();
                                adFourFragment.setLayoutResponse(fourLayout);
                                if (adDeviceDataResponse.pollInterval > 0) {
                                    adFourFragment.setGetTextADTime((int) adDeviceDataResponse.pollInterval);
                                }
                                ft.add(R.id.root_main, adFourFragment);
                            }
                        }
                        ft.commit();
                    } else {
                        //一个广告位
                        showOneAD();
                    }
                } else {
                    //一个广告位
                    showOneAD();
                }

            }

        }
    }


    @Override
    protected void onResume() {
        super.onResume();

        SharedPreference sharedPreference = SharedPreference.getSharedPreferenceUtils(MainActivity.this);
        boolean isHasMac = sharedPreference.getDate(Constants.SHARE_KEY_UMENG, false);
        if(isHasMac){
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
        if(isHasMac){
            LogCat.e("mac", "umeng可以使用。。。。。");
            MobclickAgent.onPause(this);
        }
    }

    /**
     * 从res得到bitmap
     */
//    private Bitmap  getBitmapFromRes(){
//      return BitmapFactory.decodeResource(this.getResources(),R.drawable.loging_bg);
//    }





    @SuppressLint("NewApi")

    public static boolean checkPermission(Context context, String permission) {
        boolean result = false;

        if (Build.VERSION.SDK_INT >= 23) {
            if (context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) {
                result = true;
            }
        } else {
            PackageManager pm = context.getPackageManager();

            if (pm.checkPermission(permission, context.getPackageName()) == PackageManager.PERMISSION_GRANTED) {
                result = true;
            }
        }

        return result;
    }


    public static String getDeviceInfo(Context context) {
        try {
            org.json.JSONObject json = new org.json.JSONObject();
            android.telephony.TelephonyManager tm = (android.telephony.TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

            String device_id = null;

            if (checkPermission(context, permission.READ_PHONE_STATE)) {
                device_id = tm.getDeviceId();
            }

            android.net.wifi.WifiManager wifi = (android.net.wifi.WifiManager) context.getSystemService(Context.WIFI_SERVICE);

            String mac = wifi.getConnectionInfo().getMacAddress();

            json.put("mac", mac);

            if (TextUtils.isEmpty(device_id)) {
                device_id = mac;
            }


            if (TextUtils.isEmpty(device_id)) {
                device_id = android.provider.Settings.Secure.getString(context.getContentResolver(),
                        android.provider.Settings.Secure.ANDROID_ID);
            }

            json.put("device_id", device_id);

            return json.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }


    /**
     * 上报
     */




}
