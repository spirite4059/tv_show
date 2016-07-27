package com.gochinatv.statistics.server;

import android.app.Activity;
import android.text.TextUtils;

import com.gochinatv.statistics.request.ContentLogRequest;
import com.gochinatv.statistics.request.UpgradeLogRequest;
import com.gochinatv.statistics.tools.Constant;
import com.gochinatv.statistics.tools.MacUtils;
import com.okhtttp.OkHttpCallBack;
import com.okhtttp.OkHttpUtils;
import com.okhtttp.request.ErrorMsgRequest;
import com.tools.LogCat;

/**
 * 升级
 * Created by zfy on 2016/5/4.
 */
public class UpgradeServerLog {

    public static void doPostHttpUpgradeLog(Activity context,String tpye, UpgradeLogRequest request){
        if(request == null){
            return;
        }
        if(TextUtils.isEmpty(MacUtils.getMacAddress(context))){
            return;
        }
        ContentLogRequest contentLogRequest = new ContentLogRequest();
        contentLogRequest.mac = MacUtils.getMacAddress(context);
        contentLogRequest.type = tpye;//
        contentLogRequest.content = MacUtils.getJsonStringByEntity(request);
        OkHttpUtils.getInstance().doHttpPost(Constant.LOG_HTTP_URL, contentLogRequest, new OkHttpCallBack<ErrorMsgRequest>() {
            @Override
            public void onSuccess(String url, ErrorMsgRequest response) {
                LogCat.e("升级接口日志上传成功");
            }

            @Override
            public void onError(String url, String errorMsg) {
                LogCat.e("升级接口日志上传失败");
            }
        });

    }

}
