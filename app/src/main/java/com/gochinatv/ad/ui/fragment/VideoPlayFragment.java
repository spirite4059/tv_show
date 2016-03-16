package com.gochinatv.ad.ui.fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.gochinatv.ad.R;
import com.gochinatv.ad.base.BaseFragment;
import com.gochinatv.ad.tools.Constants;
import com.gochinatv.ad.tools.LogCat;
import com.gochinatv.ad.tools.SharedPreference;
import com.gochinatv.ad.video.MeasureVideoView;

/**
 * Created by zfy on 2016/3/16.
 */
public class VideoPlayFragment extends BaseFragment{

    private MeasureVideoView videoView;
    private LinearLayout loading;

    @Override
    protected View initLayout(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.fragment_ad_video, container, false);
    }

    @Override
    protected void initView(View rootView) {
        videoView = (MeasureVideoView) rootView.findViewById(R.id.videoView);

        loading = (LinearLayout) rootView.findViewById(R.id.loading);
    }

    @Override
    protected void init() {

    }

    @Override
    protected void bindEvent() {

    }









    private void setStartTime() {
        SharedPreference sharedPreference = SharedPreference.getSharedPreferenceUtils(getActivity());
        sharedPreference.saveDate(Constants.SHARE_KEY_DURATION, System.currentTimeMillis());
    }

    private void computeTime() {
        SharedPreference sharedPreference = SharedPreference.getSharedPreferenceUtils(getActivity());
        // 计算离开的时候总时长
        try {
            long startLong = sharedPreference.getDate(Constants.SHARE_KEY_DURATION, 0L);
            if (startLong != 0) {
                long duration = System.currentTimeMillis() - startLong;
                if (duration > 0) {
                    long day = duration / (24 * 60 * 60 * 1000);
                    long hour = (duration / (60 * 60 * 1000) - day * 24);
                    long min = ((duration / (60 * 1000)) - day * 24 * 60 - hour * 60);
                    long s = (duration / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - min * 60);
                    String str = day + "天  " + hour + "时" + min + "分" + s + "秒";

                    LogCat.e(str);

                    LogCat.e("上报开机时长。。。。。。。。");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            sharedPreference.saveDate(Constants.SHARE_KEY_DURATION, 0);
        }
    }


    /**
     * 显示loading状态
     */
    public void showLoading() {
        if (loading != null && loading.getVisibility() != View.VISIBLE) {
            loading.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 隐藏loading状态
     */
    public void hideLoading() {
        if (loading != null && loading.getVisibility() != View.GONE) {
            loading.setVisibility(View.GONE);
        }
    }
}
