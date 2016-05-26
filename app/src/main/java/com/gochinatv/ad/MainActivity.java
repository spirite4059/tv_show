package com.gochinatv.ad;

import android.Manifest.permission;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.download.DLUtils;
import com.gochinatv.ad.base.BaseFragment;
import com.gochinatv.ad.thread.DeleteFileUtils;
import com.gochinatv.ad.tools.Constants;
import com.gochinatv.ad.tools.DataUtils;
import com.gochinatv.ad.tools.DownLoadAPKUtils;
import com.gochinatv.ad.tools.InstallUtils;
import com.gochinatv.ad.tools.LogCat;
import com.gochinatv.ad.tools.SharedPreference;
import com.gochinatv.ad.ui.fragment.ADTwoFragment;
import com.gochinatv.ad.ui.fragment.AdFiveFragment;
import com.gochinatv.ad.ui.fragment.AdOneFragment;
import com.gochinatv.statistics.request.RetryErrorRequest;
import com.okhtttp.response.ADDeviceDataResponse;
import com.okhtttp.response.LayoutResponse;
import com.umeng.analytics.MobclickAgent;
import com.umeng.message.IUmengRegisterCallback;
import com.umeng.message.PushAgent;
import com.umeng.message.UmengMessageHandler;
import com.umeng.message.entity.UMessage;

import java.io.File;
import java.util.ArrayList;


/**
 * Created by fq_mbp on 16/3/17.
 */
public class MainActivity extends Activity {

    private RelativeLayout rootLayout;
    private RelativeLayout titleLayout;
    private TextView textDeviceId;
    /**
     * 下载info
     */
    //private UpdateResponse.UpdateInfoResponse updateInfo;
    private AdOneFragment adOneFragment;
    private ADTwoFragment adTwoFragment;
    private AdFiveFragment adFiveFragment;


    //private ADDeviceDataResponse adDeviceDataResponse;//


    /**
     * 升级接口日志
     */
    private ArrayList<RetryErrorRequest> upgradeLogList;

    /**
     * 布局接口日志
     */
    private ArrayList<RetryErrorRequest> layoutLogList;


    //下载apk尝试的次数
    private int reTryTimes;


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
    }


    private void init() {
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
        ADDeviceDataResponse adDeviceDataResponse = (ADDeviceDataResponse) getIntent().getSerializableExtra("device");
        loadFragmentTwo(hasApkDownload, adDeviceDataResponse);

    }

    // 清空fragment的状态
    private void cleanFragmentState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            String FRAGMENTS_TAG = "android:support:fragments";
            savedInstanceState.remove(FRAGMENTS_TAG);
        }
    }

    private PushAgent mPushAgent;

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
                LogCat.e("tokenID: " + uMessage.custom);
                // 执行命令

                // 加载内容

            }
        });


    }

    private void initAdFive() {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        LogCat.e("成功加载了广告5");
        adFiveFragment = new AdFiveFragment();
        ft.add(R.id.root_main, adFiveFragment);
        ft.commit();
    }

    public PushAgent getPushAgent() {
        return mPushAgent;
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
        if (InstallUtils.hasRootPermission()) {
            //  have  root
            InstallUtils.installSilent(this, fileApk.getAbsolutePath(), true);
            Toast.makeText(this, "提醒：获取到root权限，可以静默升级！", Toast.LENGTH_LONG).show();
            // rootClientInstall(apkFile.getAbsolutePath());
        } else if (InstallUtils.isSystemApp(pInfo) || InstallUtils.isSystemUpdateApp(pInfo)) {
//                Toast.makeText(context,"正在更新软件！",Toast.LENGTH_SHORT).show();
            InstallUtils.installSilent(this, fileApk.getAbsolutePath(), false);
            Toast.makeText(this, "提醒：获取到系统权限，可以静默升级！", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "提醒：没有获取到系统权限和root权限，请选择普通安装！", Toast.LENGTH_LONG).show();
        }
    }


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
        showTitleLayout(adDeviceDataResponse.code);
        //当升级和广告体接口都完成后，才加载布局
        if (adDeviceDataResponse == null || TextUtils.isEmpty(adDeviceDataResponse.adStruct) || !"4".equals(adDeviceDataResponse.adStruct)) {
            showOneAD(isDownload, adDeviceDataResponse);
            return;
        }

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
            initAdOne((AdOneFragment)fragment, isDownload, adDeviceDataResponse);
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


}
