package com.gochinatv.ad.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.igexin.sdk.PushConsts;

/**
 * 个推推送
 * Created by ulplanet on 2016/6/20.
 */
public class PushGetuiReceiver extends BroadcastReceiver{


    public PushGetuiListener getPushGetuiListener() {
        return pushGetuiListener;
    }

    public void setPushGetuiListener(PushGetuiListener pushGetuiListener) {
        this.pushGetuiListener = pushGetuiListener;
    }

    private PushGetuiListener pushGetuiListener;




    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();

        switch (bundle.getInt(PushConsts.CMD_ACTION)) {
            case PushConsts.GET_CLIENTID:

                String cid = bundle.getString("clientid");
                // TODO:处理cid返回
                if(!TextUtils.isEmpty(cid) && pushGetuiListener != null){
                    pushGetuiListener.hasPushDataListener(1,cid);
                }

                break;
            case PushConsts.GET_MSG_DATA:
                String taskid = bundle.getString("taskid");
                String messageid = bundle.getString("messageid");
                byte[] payload = bundle.getByteArray("payload");
                if (payload != null) {
                    String data = new String(payload);
                    // TODO:接收处理透传（payload）数据
                    if(!TextUtils.isEmpty(data) && pushGetuiListener != null){
                        pushGetuiListener.hasPushDataListener(2,data);
                    }
                }
                break;
            default:
                break;
        }
    }


    public interface PushGetuiListener{

        public void hasPushDataListener(int type,String msg);

    }



}
