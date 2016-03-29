package com.gochinatv.ad;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;

import com.gochinatv.ad.ui.fragment.ADFourFragment;
import com.gochinatv.ad.ui.fragment.ADThreeFragment;
import com.gochinatv.ad.ui.fragment.ADTwoFragment;

/**
 * Created by fq_mbp on 16/3/17.
 */
public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FragmentManager fm = getFragmentManager();

        FragmentTransaction ft = fm.beginTransaction();

//        ft.add(R.id.root_main, new AdOneFragment());
        ft.add(R.id.root_main, new ADTwoFragment());
        ft.add(R.id.root_main, new ADThreeFragment());
        ft.add(R.id.root_main, new ADFourFragment());
        ft.commit();
//        //添加图片广告
//        ADThreeFragment fragmentThree = new  ADThreeFragment();
//        FragmentManager fm3 = getFragmentManager();
//        FragmentTransaction ft3 = fm3.beginTransaction();
//        ft3.add(R.id.ad_three,fragmentThree);
//        ft3.commit();
//
//
//        //添加文本广告
//        ADFourFragment fragmentFour = new  ADFourFragment();
//        FragmentManager fm4 = getFragmentManager();
//        FragmentTransaction ft4 = fm4.beginTransaction();
//        ft4.add(R.id.ad_four,fragmentFour);
//        ft4.commit();

//      Timer timer = new Timer();
//        timer.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                new ScreenShotThread(MainActivity.this, 60000000, 0.5f, 0.3f, "【二轮】春节轮播e23美丽中国一【1'04''】.mp4").start();
//            }
//        }, 1000, 1 * 60 * 1000);


        //新包下载完成得安装
//        if(RootUtils.hasRootPerssion()){
//            //RootUtils.clientInstall("/sdcard/Music/test.apk");
//            SharedPreference.getSharedPreferenceUtils(this).saveDate("isClientInstall",true);
//            RootUtils.clientInstall(DataUtils.getApkDirectory()+ Constants.FILE_APK_NAME);
//
//            Toast.makeText(MainActivity.this, "有root权限，静默安装方式", Toast.LENGTH_LONG).show();
//        }else{
//            Toast.makeText(MainActivity.this,"没有root权限，普通安装方式",Toast.LENGTH_LONG).show();
//            //RootUtils.installApk(CustomActivity.this,"/sdcard/Music/test.apk");
//            //DataUtils.installApk(MainActivity.this, filePath);
//            RootUtils.installApk(MainActivity.this,DataUtils.getApkDirectory()+Constants.FILE_APK_NAME);
//        }
//        //RootUtils.installApk(MainActivity.this, DataUtils.getApkDirectory() + Constants.FILE_APK_NAME);
//        MainActivity.this.finish();

    }



}
