package com.gochinatv.ad.base;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public abstract class BaseFragment extends Fragment
		{

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = initLayout(inflater, container);
		initView(rootView);
		return rootView;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		init();
		bindEvent();
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







}
