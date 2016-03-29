package com.gochinatv.ad.ui.fragment;

import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.gochinatv.ad.R;
import com.gochinatv.ad.adapter.EpisodeAdapter;
import com.gochinatv.ad.base.BaseFragment;
import com.gochinatv.ad.recycler.EpisodeLayoutManager;
import com.gochinatv.ad.recycler.NoTouchRecyclerView;
import com.okhtttp.response.AdImgResponse;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by fq_mbp on 16/3/29.
 */
public class AdFiveFragment extends BaseFragment {


    public RelativeLayout relEpisodeRoot;
    private NoTouchRecyclerView recyclerView;
    private EpisodeLayoutManager episodeLayoutManager;
    private Timer flushTimer;
    private int position = 2;

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

        initData();


        episodeLayoutManager = new EpisodeLayoutManager(getActivity(), LinearLayoutManager.VERTICAL);

        recyclerView.setLayoutManager(episodeLayoutManager);

        EpisodeAdapter episodeAdapter = new EpisodeAdapter(getActivity(), imgResponses);

        recyclerView.setAdapter(episodeAdapter);

        flushTimer = new Timer();

        flushTimer.schedule(new TimerTask() {
            @Override
            public void run() {

                episodeLayoutManager.smoothScrollTop(recyclerView, position);
                position++;
            }
        }, 5000, 5000);

    }

    @Override
    public void onStop() {
        if(flushTimer != null){
            flushTimer.cancel();
            flushTimer = null;
        }

        super.onStop();
    }

    private ArrayList<AdImgResponse> imgResponses;
    protected void initData() {
        imgResponses = new ArrayList<>();
        for (int i = 'A'; i < 'Z'; i++) {
            AdImgResponse adImgResponse = new AdImgResponse();
            adImgResponse.adImgName = "category " + ((int) i - 65);
            adImgResponse.adImgPrice = String.valueOf((int) i - 65);
            adImgResponse.adImgUrl = String.valueOf((int) i - 65);
            imgResponses.add(adImgResponse);
        }
    }

    @Override
    protected void bindEvent() {

    }





}
