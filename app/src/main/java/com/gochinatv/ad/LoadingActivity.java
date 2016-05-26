package com.gochinatv.ad;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;

import com.gochinatv.ad.tools.Constants;
import com.gochinatv.ad.tools.DataUtils;
import com.gochinatv.ad.tools.LogCat;
import com.gochinatv.ad.tools.UmengUtils;
import com.gochinatv.statistics.SendStatisticsLog;
import com.gochinatv.statistics.request.RetryErrorRequest;
import com.okhtttp.OkHttpCallBack;
import com.okhtttp.OkHttpUtils;
import com.okhtttp.response.ADDeviceDataResponse;
import com.okhtttp.response.UpdateResponse;
import com.okhtttp.service.ADHttpService;
import com.tools.HttpUrls;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by zfy on 2016/5/25.
 */
public class LoadingActivity extends Activity {


    //loading框
    private LinearLayout loadingView;

    //定时器--在开机自启前网络没连上是用
    private Handler handler;
    private int runnableTimes;//post-Runnable的次数


    /**
     * 下载info
     */
    private UpdateResponse.UpdateInfoResponse updateInfo;
    //private AdOneFragment adOneFragment;

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
        setContentView(R.layout.activity_loading);
        loadingView = (LinearLayout) findViewById(R.id.loading);

        handler = new Handler(Looper.getMainLooper());
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
            //DataUtils.startAppServer(this);
        }
    }


    @Override
    protected void onStop() {

        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
        }
        super.onStop();
    }





    private void doHttp() {
        if (DataUtils.isNetworkConnected(this)) {
            LogCat.e("网络已连接，请求接口");
            doHttpUpdate(LoadingActivity.this);
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
            if (DataUtils.isNetworkConnected(LoadingActivity.this)) {
                LogCat.e("网络已连接，请求接口，并且移除runnable");
                handler.removeCallbacks(runnable);
                doHttpUpdate(LoadingActivity.this);
                doGetDeviceInfo();
                SendStatisticsLog.sendInitializeLog(LoadingActivity.this);//提交激活日志
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

    private void loadFragmentTwo(boolean isHasUpgrade) {

        if (isUpgradeSucceed && isGetDerviceSucceed ){
            if(loadingView != null){
                loadingView.setVisibility(View.GONE);
            }
            //跳转到MainActivity
            Intent intent = new Intent(this,MainActivity.class);
            intent.putExtra("device",adDeviceDataResponse);
            if(isHasUpgrade){
                if(updateInfo != null && !TextUtils.isEmpty(updateInfo.fileUrl)){
                    intent.putExtra("apkUrl",updateInfo.fileUrl);
                }
            }
            startActivity(intent);
            finish();

        }

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
                                    //downloadAPKNew();
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
                                doHttpUpdate(LoadingActivity.this);
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

                UmengUtils.onEvent(LoadingActivity.this, UmengUtils.UMENG_APP_START_TIME, DataUtils.getFormatTime(adDeviceDataResponse.currentTime));

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






}
