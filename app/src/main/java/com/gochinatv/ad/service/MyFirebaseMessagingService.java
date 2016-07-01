package com.gochinatv.ad.service;

import android.content.Intent;
import android.text.TextUtils;

import com.gochinatv.ad.tools.Constants;
import com.gochinatv.ad.tools.LogCat;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

/**
 * Created by ulplanet on 2016/6/21.
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        if(remoteMessage != null){
            LogCat.e("service", "remoteMessage.toString() : "+ remoteMessage.toString());
            LogCat.e("service", "remoteMessage.getMessageType() : "+ remoteMessage.getMessageType());
            LogCat.e("service", "remoteMessage.getMessageId() : "+ remoteMessage.getMessageId());
            LogCat.e("service", "remoteMessage.getData() : "+ remoteMessage.getData());
            LogCat.e("service", "remoteMessage.getCollapseKey() : "+ remoteMessage.getCollapseKey());

            //LogCat.e("service", "remoteMessage.getBody : "+ remoteMessage.getNotification().getBody());
            //remoteMessage.getMessageType();
            //remoteMessage.toString()
            Map<String,String> data = remoteMessage.getData();
            if(data != null){
                for (String key : data.keySet()) {
                    LogCat.e("service", "key : "+ key);
                    LogCat.e("service", "data.get(key) : "+ data.get(key));
                }
            }
            try{
                if(remoteMessage.getNotification() != null){
                    String meg = remoteMessage.getNotification().getBody();
                    if(!TextUtils.isEmpty(meg)){
                        //得到消息。用广播发出去
                        Intent intent = new Intent();
                        intent.putExtra("msg",meg);
                        intent.setAction(Constants.FIREBASE_INTENT_FILTER);
                        sendBroadcast(intent);
                    }
                }

            }catch (Exception e){
                e.printStackTrace();
            }


        }
    }
}
