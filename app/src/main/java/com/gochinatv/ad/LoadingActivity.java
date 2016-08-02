package com.gochinatv.ad;

import android.app.Service;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;

import com.gochinatv.ad.base.BaseActivity;
import com.gochinatv.ad.thread.DeleteFileUtils;
import com.gochinatv.ad.tools.Constants;
import com.gochinatv.ad.tools.DataUtils;
import com.gochinatv.ad.tools.LogCat;
import com.gochinatv.statistics.SendStatisticsLog;
import com.gochinatv.statistics.request.RetryErrorRequest;
import com.gochinatv.statistics.request.UpgradeLogRequest;
import com.gochinatv.statistics.server.UpgradeServerLog;
import com.gochinatv.statistics.tools.Constant;
import com.okhtttp.response.ADDeviceDataResponse;
import com.okhtttp.response.UpdateResponse;

import java.util.ArrayList;

/**
 * Created by zfy on 2016/5/25.
 */
public class LoadingActivity extends BaseActivity {


    //loading框
    private LinearLayout loadingView;

    //定时器--在开机自启前网络没连上是用
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

    /**
     * 检查是否有版本更新
     */
    private int reTryTimes;

    /**
     * 请求广告体接口---布局大小
     */
    //布局数据
    //截屏数据
    private int reTryTimesTwo;


    //private int isNeedUpdate;//1：升级，0：不升级

    private int isGetUpdateInfo;//1：接口请求成功，0：接口请求不成
    private int isGetLayoutInfo;//1：接口请求成功，0：接口请求不成

    private Handler postHandler;
    private int reTryPostTimes;
    private RetryRunnable postRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
        loadingView = (LinearLayout) findViewById(R.id.loading);

        /**
         * 隐藏NavigationBar
         */
        DataUtils.hideNavigationBar(LoadingActivity.this);

        // 打开wifi
        //openWifi();

        /**
         * 打开安装未知应用设置
         * 如果要启动测试，需要注释此段代码，否则无法正常启动
         */
        if (!Constants.isTest) {
            //DataUtils.startAppServer(this);
        }

        // 请求网络
        doHttp();
    }



    @Override
    protected void onStop() {
        super.onStop();
        if(postHandler != null && postRunnable != null){ 
            postHandler.removeCallbacks(postRunnable);
        }
    }

    // 打开wifi
    private void openWifi() {
        WifiManager wifiManager = (WifiManager) getSystemService(Service.WIFI_SERVICE);
        // 如果wifi关闭,就打开wifi
        if(!wifiManager.isWifiEnabled()){
            wifiManager.setWifiEnabled(true);
        }
    }

    private void doHttp() {
        if (DataUtils.isNetworkConnected(this)) {
            LogCat.e("net", "网络已连接，请求接口");
            doHttpUpdate(LoadingActivity.this);
            doGetDeviceInfo(LoadingActivity.this);
            SendStatisticsLog.sendInitializeLog(this);//提交激活日志
        } else {
            LogCat.e("net", "没有联网。。。。。。。。。。");
            retryHttp();
        }
    }


    // 重新尝试请求http
    private void retryHttp() {
        if(postHandler == null){
            postHandler = new Handler();
        }
        if(postRunnable == null){
            postRunnable = new RetryRunnable();
        }
        postHandler.postDelayed(postRunnable,6000);
    }

    /**
     * 跳转main
     */
    private void goToMainActivity() {
        Intent intent = new Intent(LoadingActivity.this, MainActivity.class);
        intent.putExtra("device", adDeviceDataResponse);
        if (isHasUpgrade) {
            if (updateInfo != null && !TextUtils.isEmpty(updateInfo.fileUrl)) {
                intent.putExtra("apkUrl", updateInfo.fileUrl);
            }
        }
        intent.putExtra("isDoGetDevice", false);
        startActivity(intent);
        finish();
    }


    private void loadFragmentTwo(boolean isHasUpgrade) {
        if (isUpgradeSucceed && isGetDerviceSucceed) {
            sendLogHttpRequest();//上传接口日志
            if (loadingView != null) {
                loadingView.setVisibility(View.GONE);
            }
            //跳转到MainActivity
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("device", adDeviceDataResponse);
            if (isHasUpgrade) {
                if (updateInfo != null && !TextUtils.isEmpty(updateInfo.fileUrl)) {
                    intent.putExtra("apkUrl", updateInfo.fileUrl);
                }
            }
            intent.putExtra("isDoGetDevice", true);
            startActivity(intent);
            finish();
        }

    }

    @Override
    protected void onUpdateSuccess(UpdateResponse response) {
        super.onUpdateSuccess(response);
        if (checkUpdateInfo(response))
            return;

        // 重置retry标签
        reTryTimes = 0;
        updateInfo = response.resultForApk;
        isGetUpdateInfo = 1;//接口请求成功
        // 获取当前最新版本号
        if (!TextUtils.isEmpty(updateInfo.versionCode)) {
            // 获取本地和服务器的app版本
            int netVersionCode = 0;
            int appVersion = 0;
            try {
                netVersionCode = Integer.parseInt(updateInfo.versionCode);
                appVersion = DataUtils.getAppVersion(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
            LogCat.e("当前的最新版本：" + netVersionCode);
            LogCat.e("本地的版本：" + appVersion);
            // 检测是否要升级
            try {
                //升级接口成功
                isUpgradeSucceed = true;
                if (appVersion < netVersionCode) { // 升级
                    LogCat.e("需要升级。。。。。");
                    isHasUpgrade = true;
                } else {
                    // 不升级,加载布局
                    LogCat.e("无需升级。。。。。");
                    // 删除旧的安装文件
                    DeleteFileUtils.getInstance().deleteFile(DataUtils.getApkDirectory() + Constants.FILE_APK_NAME);
                }
                // 加载布局.但是不让AdOneFragment，下载视频
                loadFragmentTwo(isHasUpgrade);
            } catch (Exception e) {
                e.printStackTrace();
                doError("判断升级过程中出错" + e.getLocalizedMessage());
            }
        } else {
            // 不升级
            LogCat.e("升级版本为null。。。。。");
            doError("升级版本为null");
        }

    }

    private boolean checkUpdateInfo(UpdateResponse response) {
        if(response == null){
            LogCat.e("升级数据出错，无法正常升级2。。。。。");
            doError("\"3\".equals(response.status) = false");
            return true;
        }

        if (response.resultForApk == null) {
            if ("3".equals(response.status)) {
                isUpgradeSucceed = true;
                isGetUpdateInfo = 1;//接口请求成功
                loadFragmentTwo(isHasUpgrade);
                LogCat.e("没有升级包，不需要更新");
            } else {
                LogCat.e("升级数据出错，无法正常升级2。。。。。");
                doError("\"3\".equals(response.status) = false");
            }
            return true;
        }

        if (!"1".equals(response.status)) {
            LogCat.e("升级接口的status == 0。。。。。");
            doError("升级接口的status == 0");
            return true;
        }
        return false;
    }

    @Override
    protected void onUpdateError(String errorMsg) {
        super.onUpdateError(errorMsg);
        doError(errorMsg);

    }

    private void doError(String errorMsg) {
        if (!isFinishing()) {
            // 做不升级处理, 继续请求广告视频列表
            reTryTimes++;
            if (reTryTimes >= 4) {
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

            //存储错误日志
            saveUpgradeErrorReport(errorMsg);
        }
    }

    private void saveUpgradeErrorReport(String errorMsg) {
        if(upgradeLogList == null){
            upgradeLogList = new ArrayList<>();
        }
        RetryErrorRequest request = new RetryErrorRequest();
        request.retry = String.valueOf(reTryTimes);
        request.errorMsg = errorMsg;
        upgradeLogList.add(request);
    }


    @Override
    protected void onGetDeviceInfoError(String msg) {
        super.onGetDeviceInfoError(msg);
        doGetDeviceInfoError(msg);
    }

    @Override
    protected void onGetDeviceInfoSuccess(ADDeviceDataResponse response) {
        super.onGetDeviceInfoSuccess(response);
        if (!"0".equals(response.status)) {
            LogCat.e("请求广告体接口失败 status = 1");
            doGetDeviceInfoError("请求广告体接口失败 status = 1");
            return;
        }
        adDeviceDataResponse = response;
        //广告体接口成功
        isGetDerviceSucceed = true;
        isGetLayoutInfo = 1;
        //加载布局
        loadFragmentTwo(isHasUpgrade);
    }


    private void doGetDeviceInfoError(String errorMsg) {
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
                doGetDeviceInfo(this);
            }
            saveLayoutErrorReport(errorMsg);
        }
    }

    private void saveLayoutErrorReport(String errorMsg) {
        if(layoutLogList == null){
            layoutLogList = new ArrayList<>();
        }
        RetryErrorRequest request = new RetryErrorRequest();
        request.retry = String.valueOf(reTryTimesTwo);
        request.errorMsg = errorMsg;
        layoutLogList.add(request);
    }

    /**
     * 上报接口数据
     */
    private void sendLogHttpRequest(){
        //升级接口
        sendUpgradeLog();
        //布局接口
        sendLayoutLog();
    }

    /**
     * 升级接口日志
     */
    private void sendUpgradeLog(){
        UpgradeLogRequest request = UpgradeLogRequest.getInitInstance(this);
        request.isNeedUpdate = isHasUpgrade ? "1" : "0";
        request.isGetUpdateInfo = String.valueOf(isGetUpdateInfo);
        if(layoutLogList != null && layoutLogList.size()>0){
            request.interfaceError = layoutLogList;
        }
        UpgradeServerLog.doPostHttpUpgradeLog(this, Constant.LOG_TYPE_UPGRADE, request);
    }

    /**
     * 设备信息接口日志
     */
    private void sendLayoutLog(){
        UpgradeLogRequest request = UpgradeLogRequest.getInitInstance(this);
        request.isGetUpdateInfo = String.valueOf(isGetLayoutInfo);
        if(upgradeLogList != null && upgradeLogList.size()>0){
            request.interfaceError = upgradeLogList;
        }
        UpgradeServerLog.doPostHttpUpgradeLog(this, Constant.LOG_TYPE_LAYOUT, request);
    }


    private class RetryRunnable implements Runnable{

        @Override
        public void run() {
            if(DataUtils.isNetworkConnected(LoadingActivity.this)){
                doHttpUpdate(LoadingActivity.this);
                doGetDeviceInfo(LoadingActivity.this);
                SendStatisticsLog.sendInitializeLog(LoadingActivity.this);//提交激活日志
            }else{
                if(reTryPostTimes>= 4){
                    postHandler.removeCallbacks(postRunnable);
                    LogCat.e("已进行了4次重试，不再重试，直接进入main");
                    goToMainActivity();

                }else{
                    reTryPostTimes++;
                    LogCat.e("没有网络进行第："+reTryPostTimes +" 次重试");
                    if(postHandler != null && postRunnable != null){
                        postHandler.postDelayed(postRunnable,6000);
                    }
                }

            }
        }
    }



}
