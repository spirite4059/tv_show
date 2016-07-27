package com.gochinatv.ad;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.gochinatv.ad.tools.Constants;
import com.gochinatv.ad.tools.LogCat;
import com.gochinatv.ad.tools.RootUtils;
import com.gochinatv.ad.tools.SharedPreference;

/**
 * Created by zfy on 2016/3/25.
 */
public class AppInstallReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_PACKAGE_ADDED)) {
            String packageName = intent.getDataString();
            boolean is = SharedPreference.getSharedPreferenceUtils(context).getDate(Constants.SILENCE_UPGRADE_FLAG, false);
            LogCat.e("静默安装完成，广播启动 packageName " + "package:com.gochinatv.ad".equals(packageName));
            LogCat.e("静默安装完成，广播启动  is  "+ is);
            if("package:com.gochinatv.ad".equals(packageName) && is){
                RootUtils.startApp("com.gochinatv.ad", "com.gochinatv.ad.LoadingActivity");
                LogCat.e("静默安装完成，广播启动");
            }


        }
        if (intent.getAction().equals(Intent.ACTION_PACKAGE_REMOVED)) {
            String packageName = intent.getDataString();



        }
        if (intent.getAction().equals(Intent.ACTION_PACKAGE_REPLACED)) {
            String packageName = intent.getDataString();
            boolean is = SharedPreference.getSharedPreferenceUtils(context).getDate(Constants.SILENCE_UPGRADE_FLAG,false);
            LogCat.e("静默安装完成，广播启动 packageName " + "package:com.gochinatv.ad".equals(packageName));
            LogCat.e("静默安装完成，广播启动  is  "+ is);
            if("package:com.gochinatv.ad".equals(packageName) && is){
                  RootUtils.startApp("com.gochinatv.ad", "com.gochinatv.ad.LoadingActivity");
                  LogCat.e("静默安装完成，广播启动");
            }


        }


    }

}
