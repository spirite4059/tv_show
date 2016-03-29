package com.gochinatv.ad.ui.fragment;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.gochinatv.ad.R;
import com.gochinatv.ad.adapter.EpisodeAdapter;
import com.gochinatv.ad.base.BaseFragment;
import com.gochinatv.ad.recycler.EpisodeLayoutManager;
import com.gochinatv.ad.recycler.NoTouchRecyclerView;

import java.util.ArrayList;

/**
 * Created by fq_mbp on 16/3/29.
 */
public class AdFiveFragment extends BaseFragment {


    public RelativeLayout relEpisodeRoot;
    private NoTouchRecyclerView recyclerView;
    // 按键时的当前时间
    private long clickDownTime = 0;
    // 屏蔽长按事件导致的翻页频繁的阈值
    private int clickLimit;
    // 判断是否是长按的阈值
    private int longClickDuration = 600;
    // 长按是最多屏蔽次数
    private static final int MAX_LIMIT_KEY_DOWN_TIMES = 3;

    private EpisodeLayoutManager episodeLayoutManager;

//    private int scrollY;

    private int firstVisiblyPosition;
    // 移动动画是否结束
    private boolean isAnimateEnd;



    @Override
    protected View initLayout(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.fragment_ad_five, container, false);
    }

    @Override
    protected void initView(View rootView) {
        relEpisodeRoot = (RelativeLayout) rootView.findViewById(R.id.root_fragment_episode);
        recyclerView = (NoTouchRecyclerView) rootView.findViewById(R.id.recycler_episodes);
    }

    @Override
    protected void init() {
        episodeLayoutManager = new EpisodeLayoutManager(getActivity(), LinearLayoutManager.VERTICAL);

        recyclerView.setLayoutManager(episodeLayoutManager);

        EpisodeAdapter episodeAdapter = new EpisodeAdapter(getActivity(), episodeBeans);

        recyclerView.setAdapter(episodeAdapter);


    }




//    protected void initData() {
//        episodeBeans = new ArrayList<>();
//        for (int i = 'A'; i < 'Z'; i++) {
//            EpisodeItemResponse episodeBean = new EpisodeItemResponse();
//            episodeBean.name = "category " + ((int) i - 65);
//            episodeBean.installment = String.valueOf((int) i - 65);
//            episodeBeans.add(episodeBean);
//        }
//    }

    @Override
    protected void bindEvent() {

    }





}
