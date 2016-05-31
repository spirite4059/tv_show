package com.gochinatv.ad.ui.view;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.webkit.WebChromeClient;
import android.widget.RelativeLayout;

import com.github.lzyzsd.jsbridge.BridgeHandler;
import com.github.lzyzsd.jsbridge.BridgeWebView;
import com.github.lzyzsd.jsbridge.CallBackFunction;
import com.gochinatv.ad.tools.DataUtils;
import com.gochinatv.ad.tools.LogCat;
import com.google.gson.Gson;
import com.okhtttp.response.CommendResponse;
import com.okhtttp.response.LayoutResponse;

import java.util.ArrayList;

/**
 * Created by fq_mbp on 16/5/27.
 */
public class AdWebView extends BridgeWebView {

    private CommendResponse commendInfo;
    private final String JS_METHOD_NAME = "functionInJs";
    private final String SUBMIT_FROM_WEB_NAME = "submitFromWeb";
    private ArrayList<LayoutResponse> layoutResponses;
    private String deviceId;

    public AdWebView(Context context) {
        this(context, null);
    }

    public AdWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setBackgroundColor(0);
        this.setWebChromeClient(new WebChromeClient());

    }



    private String layoutJsStr(ArrayList<LayoutResponse> layoutResponses){
        LogCat.e("push", "layoutJsStr.............");
        String layoutJson = null;
        if(layoutResponses == null || layoutResponses.size() == 0){
            return layoutJson;
        }
        try {
            layoutJson = new Gson().toJson(layoutResponses);
            layoutJson = "{" + "\"deviceId\": \"" + deviceId + "\"," +
                    "\"layout\":" + layoutJson + "}";
        }catch (Exception e){
            e.printStackTrace();
        }
        return layoutJson;
    }



    public void setLayoutResponses(ArrayList<LayoutResponse> layoutResponses) {
        this.layoutResponses = layoutResponses;

    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }


    public void setCommendInfo(String commend) {
        if(TextUtils.isEmpty(commend)){
            return;
        }
        LogCat.e("push", "收到命令............" + commend);
        // 解析命令
        callHandler(JS_METHOD_NAME, new Gson().toJson(commend), new CallBackFunction() {
            @Override
            public void onCallBack(String data) {

            }
        });

    }

    public void init(){
        loadUrl();

        registerHandler(SUBMIT_FROM_WEB_NAME, new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                LogCat.e("push", "handler = submitFromWeb, data from web = " + data);
                String layoutCmd = layoutJsStr(layoutResponses);
                LogCat.e("push", "layoutJsStr(layoutResponses) = " + layoutCmd);
                function.onCallBack(layoutCmd);
            }
        });
    }

    public void loadUrl(){
        loadUrl("http://192.168.2.210:8083/android");
    }


    public void setLayoutResponse(LayoutResponse layoutResponse) {
        String widthStr = "1";
        String heightStr = "1";
        String topStr = "0";
        String leftStr = "0";
        if (layoutResponse != null) {
            if (!TextUtils.isEmpty(layoutResponse.adWidth) && !TextUtils.isEmpty(layoutResponse.adHeight)
                    && !TextUtils.isEmpty(layoutResponse.adTop) && !TextUtils.isEmpty(layoutResponse.adLeft)) {
                widthStr = layoutResponse.adWidth;
                heightStr = layoutResponse.adHeight;
                topStr = layoutResponse.adTop;
                leftStr = layoutResponse.adLeft;
            }
        }
        //动态布局
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        double width = (DataUtils.getDisplayMetricsWidth((Activity) getContext()) * (Float.parseFloat(widthStr)));
        double height = (DataUtils.getDisplayMetricsHeight((Activity) getContext()) * (Float.parseFloat(heightStr)));
        double top = (DataUtils.getDisplayMetricsHeight((Activity) getContext()) * (Float.parseFloat(topStr)));
        double left = (DataUtils.getDisplayMetricsWidth((Activity) getContext()) * (Float.parseFloat(leftStr)));
        params.width = (int) Math.round(width);
        params.height = (int) Math.round(height);
        params.topMargin = (int) Math.round(top);
        params.leftMargin = (int) Math.round(left);
        setLayoutParams(params);

    }


}
