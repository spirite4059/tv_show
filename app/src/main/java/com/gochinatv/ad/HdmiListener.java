package com.gochinatv.ad;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.gochinatv.ad.tools.AlertUtils;
import com.gochinatv.ad.tools.LogCat;

import java.util.List;

/**
 * Created by fq_mbp on 16/5/6.
 */
public class HdmiListener extends BroadcastReceiver {

    private static String INTENT_HDMI = "android.intent.action.HDMI_PLUGGED";

    @Override
    public void onReceive(Context ctxt, Intent receivedIt) {
        String action = receivedIt.getAction();
        if (action != null && action.equals(INTENT_HDMI)) {
            boolean state = receivedIt.getBooleanExtra("state", false);
            ActivityManager activityManager = (ActivityManager) ctxt.getSystemService(Context.ACTIVITY_SERVICE);
            if (state) {
                LogCat.e("hdmi", "HDMI_ON。。。。。。。已经连接");

                String runningProgress = activityManager.getRunningAppProcesses().get(0).processName;
                com.gochinatv.ad.tools.LogCat.e("hdmi", "runningActivity : " + runningProgress);
                AlertUtils.alert(ctxt, "runningActivity : " + runningProgress);
                if(!"com.gochinatv.ad".equals(runningProgress)){
                    //DataUtils.saveToSDCardHDMI('\n'+ " app start &&&&&&&&&&&&  " + DataUtils.getFormatTime(System.currentTimeMillis()));
                    LogCat.e("hdmi", "重启app");
                    AlertUtils.alert(ctxt, "重启app ");
                    Intent restartIntent = new Intent(ctxt, LoadingActivity.class);
                    restartIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    ctxt.startActivity(restartIntent);
                }else {
                    AlertUtils.alert(ctxt, "当前程序运行中，无需重启app ");
                    LogCat.e("hdmi", "当前程序运行中，无需重启app");
                }
            } else {
                LogCat.e("hdmi", "HDMI_OFF。。。。。。。失去连接");
                //获得系统运行的进程
                List<ActivityManager.RunningAppProcessInfo> appList1 = activityManager
                        .getRunningAppProcesses();
                for (ActivityManager.RunningAppProcessInfo running : appList1) {
                    System.out.println(running.processName);
                    if("com.gochinatv.ad".equals(running.processName)){
                        //DataUtils.saveToSDCardHDMI('\n'+ " app killed &&&&&&&&&&&&  " + DataUtils.getFormatTime(System.currentTimeMillis()));
                        LogCat.e("hdmi", "找到正在执行的app " + running.processName + ", 并关闭app");
                        android.os.Process.killProcess(running.pid);
                        break;
                    }
                }


//                System.exit(0);
            }
        }
    }
}
