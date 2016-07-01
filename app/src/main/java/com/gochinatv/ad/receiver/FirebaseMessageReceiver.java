package com.gochinatv.ad.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.gochinatv.ad.tools.Constants;

/**
 * Created by ulplanet on 2016/6/22.
 */
public class FirebaseMessageReceiver extends BroadcastReceiver {

    public FirebaseMessageListener getFirebaseMessageListener() {
        return firebaseMessageListener;
    }

    public void setFirebaseMessageListener(FirebaseMessageListener firebaseMessageListener) {
        this.firebaseMessageListener = firebaseMessageListener;
    }

    private FirebaseMessageListener firebaseMessageListener;

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent != null){
            String action = intent.getAction();
            if(!TextUtils.isEmpty(action) && action.equals(Constants.FIREBASE_INTENT_FILTER)){
                String msg = intent.getStringExtra("msg");
                if(!TextUtils.isEmpty(msg)){
                    if(firebaseMessageListener != null){
                        //
                        firebaseMessageListener.sendFirebaseMessage(msg);
                    }
                }
            }
        }
    }

    public interface FirebaseMessageListener{

        public void sendFirebaseMessage(String msg);
    }



}
