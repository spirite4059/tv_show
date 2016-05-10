package com.gochinatv.ad.tools;

import android.content.Context;
import android.text.TextUtils;

import com.gochinatv.ad.interfaces.IUmengConstants;
import com.umeng.analytics.MobclickAgent;

/**
 * Created by fq_mbp on 16/5/6.
 */
public class UmengUtils implements IUmengConstants {



    public static void onEvent(Context context, String eventKey, String eventValues){
        if(context == null){
            return;
        }
        try {
            SharedPreference sharedPreference = SharedPreference.getSharedPreferenceUtils(context);
            if(sharedPreference == null){
                return;
            }
            boolean isHasMac = sharedPreference.getDate(Constants.SHARE_KEY_UMENG, false);
            if(isHasMac && !TextUtils.isEmpty(eventKey)){
                if(!TextUtils.isEmpty(eventValues)){
                    MobclickAgent.onEvent(context, eventKey, eventValues);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
