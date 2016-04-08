package com.gochinatv.ad;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;

import com.gochinatv.ad.interfaces.OnUpgradeStatusListener;
import com.gochinatv.ad.thread.DeleteFileUtils;
import com.gochinatv.ad.tools.Constants;
import com.gochinatv.ad.tools.DataUtils;
import com.gochinatv.ad.tools.DownloadUtils;
import com.gochinatv.ad.tools.LogCat;
import com.gochinatv.ad.tools.RootUtils;
import com.gochinatv.ad.tools.SharedPreference;
import com.gochinatv.ad.ui.fragment.ADFourFragment;
import com.gochinatv.ad.ui.fragment.ADThreeFragment;
import com.gochinatv.ad.ui.fragment.ADTwoFragment;
import com.gochinatv.ad.ui.fragment.AdOneFragment;
import com.httputils.http.response.UpdateResponse;
import com.okhtttp.OkHttpCallBack;
import com.okhtttp.OkHttpUtils;
import com.okhtttp.response.ADDeviceDataResponse;
import com.okhtttp.response.LayoutResponse;
import com.okhtttp.response.ScreenShotResponse;
import com.okhtttp.service.ADHttpService;
import com.tools.HttpUrls;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by fq_mbp on 16/3/17.
 */
public class MainActivity extends Activity {


    private View view;
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



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        view = findViewById(R.id.root_main);
        loadingView = (LinearLayout) findViewById(R.id.loading);
        RootUtils.hasRootPerssion();//一开始就请求root
        doHttpUpdate(this);

        //        //新包下载完成得安装
//        if(RootUtils.hasRootPerssion()){
//            //RootUtils.clientInstall("/sdcard/Music/test.apk");
//            SharedPreference.getSharedPreferenceUtils(this).saveDate("isClientInstall", true);
//            LogCat.e("有root权限：RootUtils.hasRootPerssion()");
//            Toast.makeText(MainActivity.this, "有root权限，静默安装方式", Toast.LENGTH_LONG).show();
//            RootUtils.clientInstall("/mnt/internal_sd/Movies/VegoPlus.apk");
//        }else{
//            Toast.makeText(MainActivity.this,"没有root权限，普通安装方式",Toast.LENGTH_LONG).show();
//            //RootUtils.installApk(CustomActivity.this,"/sdcard/Music/test.apk");
//            LogCat.e("没有root权限：RootUtils.hasRootPerssion()");
//            //RootUtils.installApk();
//
//        }

    }


    @Override
    protected void onStop() {
        super.onStop();
        LogCat.e("当弹出弹出root框时，MainActivity是否 onStop");
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

                        if (response == null || !(response instanceof UpdateResponse)) {
                            LogCat.e("升级数据出错，无法正常升级1。。。。。");
                            doError();
                            return;
                        }

                        if (response.resultForApk == null || !(response.resultForApk instanceof UpdateResponse.UpdateInfoResponse)) {
                            LogCat.e("升级数据出错，无法正常升级2。。。。。");
                            doError();
                            return;
                        }

                        if ("1".equals(response.status) == false) {
                            LogCat.e("升级接口的status == 0。。。。。");
                            doError();
                            return;
                        }

                        updateInfo = response.resultForApk;
                        // 获取当前最新版本号
                        if (TextUtils.isEmpty(updateInfo.versionCode) == false) {
                            double netVersonCode = Integer.parseInt(updateInfo.versionCode);
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
                                    downloadAPK();
                                    // 加载布局.但是不让AdOneFragment，下载视频
                                    loadFragment(true);

                                } else {
                                    // 不升级,加载布局
                                    LogCat.e("无需升级。。。。。");
                                    // 5.清空所有升级包，为了节省空间
                                    DeleteFileUtils.getInstance().deleteFile(DataUtils.getSdCardFileDirectory() + Constants.FILE_DIRECTORY_APK);
                                    LogCat.e("清空升级apk.....");
                                    loadFragment(false);
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
                            if (reTryTimes > 4) {
                                reTryTimes = 0;
                                LogCat.e("升级接口已连续请求3次，不在请求");
                                //升级接口成功
                                isUpgradeSucceed = true;
                                loadFragment(false);
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
                    }
                });

    }

    /**
     * 加载fragment
     * @param isDownload
     */
    private void loadFragment(boolean isDownload){
        loadingView.setVisibility(View.GONE);
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        adOneFragment = new AdOneFragment();
        if(isDownload){
            adOneFragment.setIsDownloadAPK(true);
        }
        ft.add(R.id.root_main, adOneFragment);
        //ft.add(R.id.root_main, new ADTwoFragment());
        //ft.add(R.id.root_main, new ADThreeFragment());
        //ft.add(R.id.root_main, new AdFiveFragment());

//        ADFourFragment adFourFragment = new ADFourFragment();
//        LayoutResponse fourLayout = new LayoutResponse();
//        fourLayout.adType = "4";
//        fourLayout.adWidth = "0.83125";
//        fourLayout.adHeight = "0.084375";
//        fourLayout.adTop = "0.915625";
//        fourLayout.adLeft = "0.0";
//        adFourFragment.setLayoutResponse(fourLayout);
//        ft.add(R.id.root_main, adFourFragment);
        ft.commit();
    }


    /**
     * 加载fragment
     * @param isDownload
     */
    private LayoutResponse oneLayout;//广告一布局
    private LayoutResponse twoLayout;//广告二布局
    private LayoutResponse threeLayout;//广告三布局
    private LayoutResponse fourLayout;//广告四布局
    private void loadFragmentTwo(boolean isDownload) {
        //当升级和广告体接口都完成后，才加载布局
        if (isUpgradeSucceed && isGetDerviceSucceed) {
            loadingView.setVisibility(View.GONE);
            FragmentManager fm = getFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();

            adOneFragment = new AdOneFragment();
            if (isDownload) {
                adOneFragment.setIsDownloadAPK(true);
            }
            if (screenShot != null) {
                //截屏的参数
                //adOneFragment.
            }
            //设置布局参数
            if (LayoutResponses != null && LayoutResponses.size() > 0) {
                int size = LayoutResponses.size();
                for (int i = 0; i < size; i++) {
                    if ("1".equals(LayoutResponses.get(i).adType)) {
                        oneLayout = LayoutResponses.get(i);
                    } else if ("2".equals(LayoutResponses.get(i).adType)) {
                        twoLayout = LayoutResponses.get(i);
                    } else if ("3".equals(LayoutResponses.get(i).adType)) {
                        threeLayout = LayoutResponses.get(i);
                    } else if ("4".equals(LayoutResponses.get(i).adType)) {
                        fourLayout = LayoutResponses.get(i);
                    }
                }
            }

            //广告一
            if (oneLayout != null) {
                if (!TextUtils.isEmpty(oneLayout.adWidth) && !TextUtils.isEmpty(oneLayout.adHeight)
                        && !TextUtils.isEmpty(oneLayout.adTop) && !TextUtils.isEmpty(oneLayout.adLeft)) {
                    //此时加载广告一
                    //adOneFragment.setLayoutResponse(oneLayout);
                    ft.add(R.id.root_main, adOneFragment);
                }
            }

            //广告二
            if (twoLayout != null) {
                if (!TextUtils.isEmpty(twoLayout.adWidth) && !TextUtils.isEmpty(twoLayout.adHeight)
                        && !TextUtils.isEmpty(twoLayout.adTop) && !TextUtils.isEmpty(twoLayout.adLeft)) {
                    //此时加载广告二
                    ADTwoFragment adTwoFragment = new ADTwoFragment();
                    adTwoFragment.setLayoutResponse(twoLayout);
                    ft.add(R.id.root_main, adTwoFragment);
                }
            }

            //广告三
            if (threeLayout != null) {
                if (!TextUtils.isEmpty(threeLayout.adWidth) && !TextUtils.isEmpty(threeLayout.adHeight)
                        && !TextUtils.isEmpty(threeLayout.adTop) && !TextUtils.isEmpty(threeLayout.adLeft)) {
                    //此时加载广告三
                    ADThreeFragment adThreeFragment = new ADThreeFragment();
                    // adThreeFragment.setLayoutResponse(threeLayout);
                    ft.add(R.id.root_main, adThreeFragment);
                }
            }

            //广告四
            if (fourLayout != null) {
                if (!TextUtils.isEmpty(fourLayout.adWidth) && !TextUtils.isEmpty(fourLayout.adHeight)
                        && !TextUtils.isEmpty(fourLayout.adTop) && !TextUtils.isEmpty(fourLayout.adLeft)) {
                    //此时加载广告四
                    ADFourFragment adFourFragment = new ADFourFragment();
                    adFourFragment.setLayoutResponse(fourLayout);
                    ft.add(R.id.root_main, adFourFragment);
                }
            }

            ft.commit();
        }
    }


    /**
     * 下载apk
     */
    private void downloadAPK(){
        DownloadUtils.download(MainActivity.this, Constants.FILE_DIRECTORY_APK, Constants.FILE_APK_NAME, updateInfo.fileUrl, new OnUpgradeStatusListener() {
            @Override
            public void onDownloadFileSuccess(String filePath) {
//        //新包下载完成得安装
                if (RootUtils.hasRootPerssion()) {
                    SharedPreference.getSharedPreferenceUtils(MainActivity.this).saveDate("isClientInstall", true);
                    RootUtils.clientInstall(DataUtils.getApkDirectory() + Constants.FILE_APK_NAME);
                    LogCat.e("有root权限，静默安装方式");
                } else {
                    LogCat.e("没有root权限，普通安装方式");
                    DataUtils.installApk(MainActivity.this, filePath);
                }
                //MainActivity.this.finish();
            }

            @Override
            public void onDownloadFileError(int errorCode, String errorMsg) {
                //通知AdOneFragment去下载视频
                LogCat.e("下载apk出现错误");
                if (adOneFragment != null) {
                    adOneFragment.startDownloadVideo();
                }

            }
        });

    }


    /**
     * 请求广告体接口---布局大小
     */

    //布局数据
    private  ArrayList<LayoutResponse> LayoutResponses;
    //截屏数据
    private ScreenShotResponse screenShot;
    private int reTryTimesTwo;
    private void doGetDerviceInfo(){
        ADHttpService.doHttpGetDeviceInfo(this, new OkHttpCallBack<ADDeviceDataResponse>() {
            @Override
            public void onSuccess(String url, ADDeviceDataResponse response) {
                LogCat.e("doGetDerviceInfo url:  " + url);
                if (isFinishing()) {
                    return;
                }

                if (response == null || !(response instanceof ADDeviceDataResponse)) {
                    LogCat.e("请求广告体接口失败");
                    doError();
                    return;
                }

                if ("1".equals(response.status) == false) {
                    LogCat.e("请求广告体接口失败 status = 1");
                    doError();
                    return;
                }

                if(response.screenShot != null){
                    screenShot = response.screenShot;
                }

                if(response.layout != null){
                    LayoutResponses = response.layout;
                }

                //广告体接口成功
                isGetDerviceSucceed = true;
                //加载布局
                loadFragmentTwo(isHasUpgrade);

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
                        doGetDerviceInfo();
                    }
                }
            }


            @Override
            public void onError(String url, String errorMsg) {

                LogCat.e("请求接口出错，无法升级。。。。。" + url);
                doError();


            }
        });


    }



}
