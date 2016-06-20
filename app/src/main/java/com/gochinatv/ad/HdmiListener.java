package com.gochinatv.ad;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.tools.LogCat;

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
            if (state == true) {
                LogCat.e("hdmi", "HDMI_ON。。。。。。。已经连接");

            } else {
                LogCat.e("hdmi", "HDMI_OFF。。。。。。。失去连接");
            }
        }
    }
}
