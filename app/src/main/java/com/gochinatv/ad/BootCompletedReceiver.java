package com.gochinatv.ad;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.gochinatv.ad.tools.LogCat;

/**
 * Created by fq_mbp on 15/12/31.
 */
public class BootCompletedReceiver extends BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {
        LogCat.e("onReceive..........");

        if(intent.getAction() == Intent.ACTION_BOOT_COMPLETED){
            LogCat.e("recevie boot completed ... ");
            Intent activityIntent = new Intent(context, TestActivity.class);
            activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(activityIntent);
        }

    }
}

