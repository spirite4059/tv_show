package com.gochinatv.ad.ui.fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.lzyzsd.jsbridge.BridgeWebView;
import com.gochinatv.ad.R;
import com.gochinatv.ad.base.BaseFragment;
import com.okhtttp.response.LayoutResponse;

/**
 * Created by fq_mbp on 16/5/25.
 */
public class AdFiveFragment extends BaseFragment{

    private BridgeWebView webView;
    LayoutResponse layoutResponse;

    @Override
    protected View initLayout(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.fragment_ad_five, container, false);
    }

    @Override
    protected void initView(View rootView) {
        webView = (BridgeWebView) rootView;
    }

    @Override
    protected void init() {
        webView.setBackgroundColor(0);
//        webView.loadUrl("http://192.168.2.210:8083/android");

        webView.loadUrl("file:///android_asset/demo.html");


    }

    @Override
    protected void bindEvent() {

    }


    @Override
    public void onStop() {
        super.onStop();

    }

    /**
     * 设置布局参数
     * @param layoutResponse
     */
    public void setLayoutResponse(LayoutResponse layoutResponse) {
        this.layoutResponse = layoutResponse;
    }
}
