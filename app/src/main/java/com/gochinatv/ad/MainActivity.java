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
import com.gochinatv.ad.ui.fragment.AdOneFragment;
import com.httputils.http.response.UpdateResponse;
import com.okhtttp.OkHttpCallBack;
import com.okhtttp.OkHttpUtils;
import com.tools.HttpUrls;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        view = findViewById(R.id.root_main);
        loadingView = (LinearLayout) findViewById(R.id.loading);
        doHttpUpdate(this);

//        //        //新包下载完成得安装
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
                                if (DataUtils.getAppVersion(context) < netVersonCode) { // 升级
                                    // 升级
                                    // 下载最新安装包，下载完成后，提示安装
                                    LogCat.e("需要升级。。。。。");
                                    // 去下载当前的apk
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
        AdOneFragment adOneFragment = new AdOneFragment();
        if(isDownload){
            adOneFragment.setIsDownloadAPK(true);
        }
        ft.add(R.id.root_main, adOneFragment);
        //ft.add(R.id.root_main, new ADTwoFragment());
        //ft.add(R.id.root_main, new ADThreeFragment());
        //ft.add(R.id.root_main, new AdFiveFragment());
        //ft.add(R.id.root_main, new ADFourFragment());
        ft.commit();
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
                MainActivity.this.finish();
            }

            @Override
            public void onDownloadFileError(int errorCode, String errorMsg) {
                loadFragment(false);
            }
        });


    }

}
