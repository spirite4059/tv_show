package com.gochinatv.ad.ui.fragment;

import android.app.FragmentTransaction;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.gochinatv.ad.R;
import com.gochinatv.ad.base.BaseFragment;
import com.gochinatv.ad.tools.Constants;
import com.gochinatv.ad.tools.DataUtils;
import com.gochinatv.ad.tools.LogCat;
import com.gochinatv.ad.tools.SharedPreference;
import com.gochinatv.ad.ui.view.AutoTextView;
import com.okhtttp.OkHttpCallBack;
import com.okhtttp.response.ADFourResponse;
import com.okhtttp.response.ADTextRseponse;
import com.okhtttp.service.ADHttpService;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;

/**
 * Created by zfy on 2016/3/16.
 */
public class ADFourFragment extends BaseFragment {

    //滚动控件
    private AutoTextView autoTextView;


    //循环滚动定时器
    private boolean isCycleState = false;//滚动的状态
    private int cycleTextTime = 1;//每隔多长去滚动，默认：10 （秒）
    private Handler cycleHandler;//定时器



    //文字广告集合
    private ArrayList<ADTextRseponse> textData;
    //文字集合总数
    private int taotalSize;


    //请求文字广告接口的定时器
    //private Timer getTextADTimer;


    //布局参数
    //private LayoutResponse layoutResponse;

    //是否是第一次网络请求
    //private boolean isFirstDoHttp = true;



    //内置的文字广告
    private String textADString = "For more information about GoChina Media, please contact clientservice@gochinatv.com.";

    //本地数据
    //private ArrayList<String> textList;

    //view的宽度
    private int viewWidth;

    @Override
    protected View initLayout(LayoutInflater inflater, ViewGroup container) {

        LinearLayout linearLayout = (LinearLayout) inflater.inflate(R.layout.fragment_ad_four, container, false);
        String widthStr = null;
        String heightStr = null;
        String topStr = null;
        String leftStr = null;
        if(layoutResponse != null){
            widthStr = TextUtils.isEmpty(layoutResponse.adWidth)?"0.83125":layoutResponse.adWidth;
            heightStr = TextUtils.isEmpty(layoutResponse.adHeight)?"0.084375":layoutResponse.adHeight;
            topStr = TextUtils.isEmpty(layoutResponse.adTop)?"0.915625":layoutResponse.adTop;
            leftStr = TextUtils.isEmpty(layoutResponse.adLeft)?"0":layoutResponse.adLeft;
        }else{
            //默认布局
            widthStr = "0.83125";
            heightStr = "0.084375";
            topStr = "0.915625";
            leftStr = "0";
        }

        //动态布局
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
        double width = (float) (DataUtils.getDisplayMetricsWidth(getActivity())*(Float.parseFloat(widthStr)));
        double height = (float) (DataUtils.getDisplayMetricsHeight(getActivity())*(Float.parseFloat(heightStr)));
        double top = (float) (DataUtils.getDisplayMetricsHeight(getActivity())*(Float.parseFloat(topStr)));
        double left = (float) (DataUtils.getDisplayMetricsWidth(getActivity())*(Float.parseFloat(leftStr)));
        viewWidth = (int) Math.round(width);
        params.width = (int) Math.round(width);
        params.height = (int) Math.round(height);
        params.topMargin = (int) Math.round(top);
        params.leftMargin = (int) Math.round(left);
        linearLayout.setLayoutParams(params);
        LogCat.e("ADFourFragment"," 广告四布局 width: "+params.width+" height: "+params.height+" top: "+params.topMargin+" left: "+params.leftMargin);


        return linearLayout;
    }

    @Override
    protected void initView(View rootView) {
        autoTextView = (AutoTextView) rootView.findViewById(R.id.auto_textview);
    }

    @Override
    protected void init() {
        autoTextView.setViewWidth(viewWidth);
        cycleHandler = new Handler();
        //得到轮询请求接口间隔
        if(DataUtils.isNetworkConnected(getActivity())){
            doGetTextAD();//请求接口
        }else{
            autoTextView.next();
            autoTextView.setText(textADString);
        }

        LogCat.e("ADFourFragment"," httpIntervalTime:  "+ httpIntervalTime);
    }

    @Override
    protected void bindEvent() {

    }

    @Override
    public void onStop() {
        super.onStop();

    }

    @Override
    public void onDestroy() {
        LogCat.e("ADFourFragment"," 取消广告四的滚动 ");

        if(cycleHandler != null && runnable != null){
            cycleHandler.removeCallbacks(runnable);
            isCycleState = false;
        }

//        if(getTextADTimer != null){
//            getTextADTimer.cancel();
//            getTextADTimer = null;
//        }

        if(autoTextView != null){
            autoTextView.stopScroll();
        }

        super.onDestroy();
    }


    //int i = 0;
    int index = 0;
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            isCycleState = true;
            if(index > textData.size()-1){
                index = 0;
                //i = 1;
            }
            //LogCat.e("ADFourFragment","index :"+index  + "     "+textData.get(index).adTextStr);
            if(!TextUtils.isEmpty(textData.get(index).adTextStr)){
                autoTextView.next();
                autoTextView.setText(textData.get(index).adTextStr);
            }
            //下一次的滚动
            if(textData.get(index).adTextInterval >1000){
                cycleTextTime = textData.get(index).adTextInterval;
            }else {
                cycleTextTime = 5000;
            }
            cycleHandler.postDelayed(runnable,cycleTextTime);

            //自加1
            index++;
        }
    };




    /**
     * 请求文字广告
     */
    private int reTryTimesTwo;
    private void doGetTextAD(){
        if(!DataUtils.isNetworkConnected(getActivity())){
            return;
        }
        ADHttpService.doHttpGetTextADInfo(getActivity(), new OkHttpCallBack<ADFourResponse>() {
            @Override
            public void onSuccess(String url, ADFourResponse response) {
                if(!isAdded()){
                    return;
                }
                LogCat.e("ADFourFragment"," 广告四 url " + url);
                if (response == null || !(response instanceof ADFourResponse)) {
                    LogCat.e("ADFourFragment","请求文字接口失败");
                    doError();
                    return;
                }

                if(response.data == null || !(response.data instanceof ArrayList)){
                    LogCat.e("ADFourFragment","请求文字接口失败");
                    doError();
                    return;
                }
                if(textData != null &&textData.size()>0){
                    LogCat.e("ADFourFragment","ADFourFragment 清空之前的文字广告 ");
                    textData.clear();//先清除之前的
                }
                textData = response.data;
                taotalSize = textData.size();
                cycleTextTime = response.adTextInterval;//设置滚动间隔

                if(taotalSize == 0){
                    //此时没有文字广告，要停止之前的滚动
                    // 显示内置文字广告
                    autoTextView.next();
                    autoTextView.setText(textADString);
                    index = 0;
                    LogCat.e("ADFourFragment"," 停止之前的滚动 ");
                    if(cycleHandler != null && runnable != null){
                        cycleHandler.removeCallbacks(runnable);
                        isCycleState = false;
                    }

                }

                if(taotalSize == 1){
                    //此时有1条文字广告，要停止之前的滚动
                    index = 0;
                    if(!TextUtils.isEmpty(textData.get(0).adTextStr)){
                        autoTextView.next();
                        autoTextView.setText(textData.get(0).adTextStr);
                    }
                    LogCat.e("ADFourFragment","停止之前的滚动 ");
                    if(cycleHandler != null && runnable != null){
                        cycleHandler.removeCallbacks(runnable);
                        isCycleState = false;
                    }

                }

                if(taotalSize >1){
                    //有多条文字广告，开启滚动
                    if(!isCycleState){
                        LogCat.e("ADFourFragment","开启滚动 ");
                        if(cycleHandler != null && runnable != null){
                            cycleHandler.postDelayed(runnable,10);
                        }
                    }
                }

//                if(isFirstDoHttp){
//                    if(getTextADTimer == null){
//                        getTextADTimer = new Timer();
//                        getTextADTimer.schedule(new TimerTask() {
//                            @Override
//                            public void run() {
//                                doGetTextAD();
//                            }
//                        },httpIntervalTime,httpIntervalTime);
//                    }
//                }
//                isFirstDoHttp = false;

            }

            private void doError() {
                if (isAdded()) {
                    // 做不升级处理, 继续请求广告视频列表
                    reTryTimesTwo++;
                    if (reTryTimesTwo > 4) {
                        reTryTimesTwo = 0;
                        // 显示内置文字广告
                        autoTextView.next();
                        autoTextView.setText(textADString);
                        LogCat.e("ADFourFragment","文字广告接口已连续请求3次，不在请求");
                    } else {
                        LogCat.e("ADFourFragment","进行第 " + reTryTimesTwo + " 次重试请求。。。。。。。");
                        doGetTextAD();
                    }
                }
            }


            @Override
            public void onError(String url, String errorMsg) {

                doError();
            }
        });

    }




    @Override
    public void onResume() {
        super.onResume();
        SharedPreference sharedPreference = SharedPreference.getSharedPreferenceUtils(getActivity());
        boolean isHasMac = sharedPreference.getDate(Constants.SHARE_KEY_UMENG, false);
        if(isHasMac){
            MobclickAgent.onPageStart("ADFourFragment");
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        SharedPreference sharedPreference = SharedPreference.getSharedPreferenceUtils(getActivity());
        boolean isHasMac = sharedPreference.getDate(Constants.SHARE_KEY_UMENG, false);
        if(isHasMac){
            MobclickAgent.onPageEnd("ADFourFragment");
        }
    }



//    public LayoutResponse getLayoutResponse() {
//        return layoutResponse;
//    }
//
//    /**
//     * 设置布局参数
//     * @param layoutResponse
//     */
//    public void setLayoutResponse(LayoutResponse layoutResponse) {
//        this.layoutResponse = layoutResponse;
//    }

    /**
     * 设置请求接口的间隔
     * @param getTextADTime
     */
//    public void setGetTextADTime(int getTextADTime) {
//        this.getTextADTime = getTextADTime;
//        LogCat.e("ADFourFragment","请求接口时间的间隔 getTextADTime:  " + getTextADTime );
//    }


    /**
     * 暂停滚动
     */
    public void puaseRollingAnimation(){

        if(taotalSize>1){
            if(cycleHandler != null && runnable != null){
                LogCat.e("ADFourFragment","暂停广告4的滚动");
                cycleHandler.removeCallbacks(runnable);
                //isCycleState = false;
            }
        }

    }


    /**
     * 恢复滚动
     */
    public void recoveryRollingAnimation(){
        if(taotalSize>1){
            if(cycleHandler != null && runnable != null){
                LogCat.e("ADFourFragment","恢复广告4的滚动");
                cycleHandler.postDelayed(runnable,5000);
                //isCycleState = false;
            }
        }
    }

    @Override
    public void doHttpRequest() {
        doGetTextAD();
    }

    @Override
    public void removeFragment() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.remove(this);
        ft.commit();
    }
}
