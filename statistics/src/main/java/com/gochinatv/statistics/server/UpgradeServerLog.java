package com.gochinatv.statistics.server;

import android.app.Activity;
import android.text.TextUtils;

import com.gochinatv.statistics.request.ContentLogRequest;
import com.gochinatv.statistics.request.UpgradeLogRequest;
import com.gochinatv.statistics.tools.MacUtils;
import com.okhtttp.OkHttpCallBack;
import com.okhtttp.OkHttpUtils;
import com.okhtttp.request.ErrorMsgRequest;

/**
 * 升级
 * Created by zfy on 2016/5/4.
 */
public class UpgradeServerLog {

    public static void doPostHttpUpgradeLog(Activity context,String tpye, UpgradeLogRequest request, OkHttpCallBack<ErrorMsgRequest> listener){
        if(request == null){
            return;
        }
        if(TextUtils.isEmpty(MacUtils.getMacAddress(context))){
            return;
        }
        String url = "http://api.bm.gochinatv.com/device_v1/uploadLog";
        ContentLogRequest contentLogRequest = new ContentLogRequest();
        contentLogRequest.mac = MacUtils.getMacAddress(context);
        contentLogRequest.type = tpye;//
        contentLogRequest.content = MacUtils.getJsonStringByEntity(request);
        OkHttpUtils.getInstance().doHttpPost(url,contentLogRequest,listener);

    }



}
