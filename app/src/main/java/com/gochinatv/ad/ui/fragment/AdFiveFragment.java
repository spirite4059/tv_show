package com.gochinatv.ad.ui.fragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.github.lzyzsd.jsbridge.BridgeHandler;
import com.github.lzyzsd.jsbridge.BridgeWebView;
import com.github.lzyzsd.jsbridge.CallBackFunction;
import com.gochinatv.ad.R;
import com.gochinatv.ad.base.BaseFragment;
import com.gochinatv.ad.tools.DataUtils;
import com.gochinatv.ad.tools.LogCat;
import com.google.gson.Gson;
import com.okhtttp.response.CommendResponse;
import com.okhtttp.response.LayoutResponse;

import java.util.ArrayList;

/**
 * Created by fq_mbp on 16/5/25.
 */
public class AdFiveFragment extends BaseFragment {

    private BridgeWebView webView;
    private Button btn;
    private CommendResponse commendInfo;
    private final String JS_METHOD_NAME = "functionInJs";
    private final String SUBMIT_FROM_WEB_NAME = "submitFromWeb";

    @Override
    protected View initLayout(LayoutInflater inflater, ViewGroup container) {
        View rootView = inflater.inflate(R.layout.fragment_ad_five, container, false);
        if (layoutResponse != null) {
            if (!TextUtils.isEmpty(layoutResponse.adWidth) && !TextUtils.isEmpty(layoutResponse.adHeight)
                    && !TextUtils.isEmpty(layoutResponse.adTop) && !TextUtils.isEmpty(layoutResponse.adLeft)) {
                String widthStr = layoutResponse.adWidth;
                String heightStr = layoutResponse.adHeight;
                String topStr = layoutResponse.adTop;
                String leftStr = layoutResponse.adLeft;
                //动态布局
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                double width = (DataUtils.getDisplayMetricsWidth(getActivity()) * (Float.parseFloat(widthStr)));
                double height = (DataUtils.getDisplayMetricsHeight(getActivity()) * (Float.parseFloat(heightStr)));
                double top = (DataUtils.getDisplayMetricsHeight(getActivity()) * (Float.parseFloat(topStr)));
                double left = (DataUtils.getDisplayMetricsWidth(getActivity()) * (Float.parseFloat(leftStr)));
                params.width = (int) Math.round(width);
                params.height = (int) Math.round(height);
                params.topMargin = (int) Math.round(top);
                params.leftMargin = (int) Math.round(left);
                rootView.setLayoutParams(params);
            }
        }
        return rootView;
    }

    @Override
    protected void initView(View rootView) {
        webView = (BridgeWebView) rootView.findViewById(R.id.root_fragment_web);
        btn = (Button) rootView.findViewById(R.id.btn);
    }

    @Override
    protected void init() {
        webView.setBackgroundColor(0);

        webView.setWebChromeClient(new WebChromeClient());



//        webView.loadUrl("file:///android_asset/demo.html");
        webView.loadUrl("http://192.168.2.210:8083/android");


        webView.registerHandler(SUBMIT_FROM_WEB_NAME, new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                LogCat.e("push", "handler = submitFromWeb, data from web = " + data);
                LogCat.e("push", "layoutJsStr(layoutResponses) = " + layoutJsStr(layoutResponses));
                function.onCallBack(layoutJsStr(layoutResponses));
            }
        });


//        layoutJs(layoutResponses);


    }

    @Override
    protected void bindEvent() {
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.loadUrl("http://192.168.2.210:8083/android");
            }
        });
    }


    @Override
    public void onStop() {
        super.onStop();

    }


    public void setCommendInfo(String commend) {
        if(TextUtils.isEmpty(commend)){
            return;
        }
        LogCat.e("push", "收到命令............" + commend);
        // 解析命令
        webView.callHandler(JS_METHOD_NAME, new Gson().toJson(commend), new CallBackFunction() {
            @Override
            public void onCallBack(String data) {

            }
        });

    }



    public String layoutJsStr(ArrayList<LayoutResponse> layoutResponses){
        String layoutJson = null;
        if(layoutResponses == null || layoutResponses.size() == 0){
            return layoutJson;
        }
        try {
            layoutJson = new Gson().toJson(layoutResponses);
            layoutJson = "{\"layout\":" + layoutJson + "}";
        }catch (Exception e){
            e.printStackTrace();
        }
        return layoutJson;
    }


    @Override
    public void setLayoutResponse(LayoutResponse layoutResponse) {
        super.setLayoutResponse(layoutResponse);
        this.layoutResponse = layoutResponse;
    }

    ArrayList<LayoutResponse> layoutResponses;
    public void setLayoutResponses(ArrayList<LayoutResponse> layoutResponses) {
        this.layoutResponses = layoutResponses;
    }
}
