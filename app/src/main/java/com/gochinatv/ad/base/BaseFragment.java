package com.gochinatv.ad.base;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gochinatv.ad.ui.fragment.ADFourFragment;
import com.gochinatv.ad.ui.fragment.ADThreeLocalFragment;
import com.gochinatv.ad.ui.fragment.ADTwoFragment;
import com.gochinatv.ad.ui.fragment.AdOneFragment;
import com.okhtttp.response.LayoutResponse;


public abstract class BaseFragment extends Fragment {

    protected View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = initLayout(inflater, container);
        initView(rootView);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        init();
        bindEvent();
    }

    public static BaseFragment getInstance(int type){
        BaseFragment baseFragment = null;
        switch (type){
            case 1:
                baseFragment = new AdOneFragment();
                break;
            case 2:
                baseFragment = new ADTwoFragment();
                break;
            case 3:
                baseFragment = new ADThreeLocalFragment();
                break;
            case 4:
                baseFragment = new ADFourFragment();
                break;
            case 5:
//                baseFragment = new AdFiveFragment();
                break;

        }
        return baseFragment;
    }


    /**
     * 初始化控件，比如findViewById
     */
    protected abstract View initLayout(LayoutInflater inflater, ViewGroup container);

    /**
     * 初始化控件，比如findViewById
     */
    protected abstract void initView(View rootView);

    /**
     * 初始化参数，比如一次必要的数据设置
     */
    protected abstract void init();

    /**
     * 绑定控件的事件，比如button的onclick事件
     */
    protected abstract void bindEvent();


    protected LayoutResponse layoutResponse;
    public void setLayoutResponse(LayoutResponse layoutResponse) {
        this.layoutResponse = layoutResponse;
    }

    /**
     * 移除fragment
     */
    public void removeFragment(){}


    /**
     * 网络请求
     */
    public void doHttpRequest(){}



    //接口请求的时间间隔
    protected int httpIntervalTime = 14400000;//每隔多长去请求接口，默认：4 （小时）== 14400000 毫秒

    public void setHttpIntervalTime(int httpIntervalTime) {
        this.httpIntervalTime = httpIntervalTime;
    }



}
