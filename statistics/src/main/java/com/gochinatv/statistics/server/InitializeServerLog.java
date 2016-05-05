package com.gochinatv.statistics.server;

import com.okhtttp.OkHttpCallBack;
import com.okhtttp.OkHttpUtils;
import com.okhtttp.request.ErrorMsgRequest;

import java.util.Map;

/**
 * 初始化
 * Created by zfy on 2016/5/4.
 */
public class InitializeServerLog {

    public static void doPostHttpInitializeLog(Map<String,String> map, OkHttpCallBack<ErrorMsgRequest> listener){
        if(map == null){
            return;
        }
        String url = "http://47.89.179.118:9090/app-api2/device_v1/updateDevice";
        OkHttpUtils.getInstance().doHttpGet(url,map,listener);

    }

}
