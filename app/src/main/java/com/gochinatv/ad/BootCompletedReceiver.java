package com.gochinatv.ad;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by fq_mbp on 15/12/31.
 */
public class BootCompletedReceiver extends BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction() == Intent.ACTION_BOOT_COMPLETED){
            Log.d("LibraryTestActivity", "recevie boot completed ... ");
            Intent activityIntent = new Intent(context, TestActivity.class);
            activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(activityIntent);
//            AlertUtils.alert(context, "开机自启动。。。。。。。。。。");
        }

    }
}

