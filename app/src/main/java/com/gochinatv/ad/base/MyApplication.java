package com.gochinatv.ad.base;

import android.app.Application;
import android.text.TextUtils;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.gochinatv.ad.tools.DataUtils;
import com.gochinatv.ad.tools.LogCat;
import com.umeng.analytics.AnalyticsConfig;

/**
 * Created by fq_mbp on 16/1/28.
 */
public class MyApplication extends Application{

    @Override
    public void onCreate() {
        super.onCreate();
        Fresco.initialize(this);
        try {
            String mac = TextUtils.isEmpty(DataUtils.getMacAddress(this)) ? "" : DataUtils.getMacAddress(this);
            mac = mac.replace(":", "");
            LogCat.e("mac: " + mac);
            AnalyticsConfig.setChannel(mac);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
