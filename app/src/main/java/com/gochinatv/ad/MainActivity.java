package com.gochinatv.ad;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;

import com.gochinatv.ad.ui.fragment.ADFourFragment;
import com.gochinatv.ad.ui.fragment.ADTwoFragment;
import com.gochinatv.ad.ui.fragment.AdFiveFragment;
import com.gochinatv.ad.ui.fragment.AdOneFragment;


/**
 * Created by fq_mbp on 16/3/17.
 */
public class MainActivity extends Activity {

    //SimpleDraweeView mSimpleDraweeView;

    private View view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        view = findViewById(R.id.root_main);
        FragmentManager fm = getFragmentManager();

        FragmentTransaction ft = fm.beginTransaction();
        ft.add(R.id.root_main, new AdOneFragment());
        ft.add(R.id.root_main, new ADTwoFragment());
////        ft.add(R.id.root_main, new ADThreeFragment());
       ft.add(R.id.root_main, new AdFiveFragment());
        ft.add(R.id.root_main, new ADFourFragment());
        ft.commit();



        //        //新包下载完成得安装
//        if(RootUtils.hasRootPerssion()){
//            //RootUtils.clientInstall("/sdcard/Music/test.apk");
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


//        //新包下载完成得安装
//        if(RootUtils.hasRootPerssion()){
//            //RootUtils.clientInstall("/sdcard/Music/test.apk");
//            LogCat.e("有root权限：RootUtils.hasRootPerssion()");
//            Toast.makeText(MainActivity.this, "有root权限，静默安装方式", Toast.LENGTH_LONG).show();
//        }else{
//            Toast.makeText(MainActivity.this,"没有root权限，普通安装方式",Toast.LENGTH_LONG).show();
//            //RootUtils.installApk(CustomActivity.this,"/sdcard/Music/test.apk");
//            LogCat.e("没有root权限：RootUtils.hasRootPerssion()");
//        }
//
//        if (RootManager.getInstance().obtainPermission()) {
//
//            LogCat.e("获取root权限成功 RootManager" );
//
//            LogCat.e("有root权限：RootManager.getInstance().hasRooted() ");
//            //RootManager.getInstance().installPackage();
//
//            view.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    final String apkPath = "/mnt/internal_sd/Movies/VegoPlus.apk";
//                    runAsyncTask(new AsyncTask<Void, Void, Result>() {
//
//                        @Override
//                        protected void onPreExecute() {
//                            LogCat.e("Installing package " + apkPath + " ...........");
//                            super.onPreExecute();
//                        }
//
//                        @Override
//                        protected Result doInBackground(Void... params) {
//                            return RootManager.getInstance().installPackage(apkPath);
//                        }
//
//                        @Override
//                        protected void onPostExecute(Result result) {
//                            LogCat.e("Install " + apkPath + " " + result.getResult()
//                                    + " with the message " + result.getMessage());
//                            super.onPostExecute(result);
//                        }
//
//                    });
//                }
//            }, 5000);
//
//
//        }else if(RootManager.getInstance().obtainPermission()){
//            LogCat.e("获取root权限成功 RootManager" );
//        }else{
//            LogCat.e("获取root权限失败 RootManager " );
//        }

    }



    private static final <T> void runAsyncTask(AsyncTask<T, ?, ?> asyncTask, T... params) {
        asyncTask.execute(params);
    }

}
