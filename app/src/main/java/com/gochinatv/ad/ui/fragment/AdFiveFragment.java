package com.gochinatv.ad.ui.fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.gochinatv.ad.R;
import com.gochinatv.ad.base.BaseFragment;

/**
 * Created by fq_mbp on 16/5/25.
 */
public class AdFiveFragment extends BaseFragment{

    private WebView webView;

    @Override
    protected View initLayout(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.fragment_ad_five, container, false);
    }

    @Override
    protected void initView(View rootView) {
        webView = (WebView) rootView;
    }

    @Override
    protected void init() {



    }

    @Override
    protected void bindEvent() {

    }


    @Override
    public void onStop() {
        super.onStop();

    }
}
