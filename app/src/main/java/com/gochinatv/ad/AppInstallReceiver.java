package com.gochinatv.ad;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import com.gochinatv.ad.tools.RootUtils;
import com.gochinatv.ad.tools.SharedPreference;

/**
 * Created by zfy on 2016/3/25.
 */
public class AppInstallReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        PackageManager manager = context.getPackageManager();
        if (intent.getAction().equals(Intent.ACTION_PACKAGE_ADDED)) {
            String packageName = intent.getDataString();
            boolean is = SharedPreference.getSharedPreferenceUtils(context).getDate("isClientInstall", false);
            if("package:com.gochinatv.ad".equals(packageName) && is){
                RootUtils.startApp("com.gochinatv.ad", "com.gochinatv.ad.MainActivity");
            }


        }
        if (intent.getAction().equals(Intent.ACTION_PACKAGE_REMOVED)) {
            String packageName = intent.getDataString();



        }
        if (intent.getAction().equals(Intent.ACTION_PACKAGE_REPLACED)) {
            String packageName = intent.getDataString();

            boolean is = SharedPreference.getSharedPreferenceUtils(context).getDate("isClientInstall",false);
            if("package:com.gochinatv.ad".equals(packageName) && is){
                  RootUtils.startApp("com.gochinatv.ad", "com.gochinatv.ad.MainActivity");

            }


        }


    }

}
