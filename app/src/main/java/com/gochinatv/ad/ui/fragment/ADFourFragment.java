package com.gochinatv.ad.ui.fragment;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.gochinatv.ad.R;
import com.gochinatv.ad.base.BaseFragment;
import com.gochinatv.ad.tools.DataUtils;
import com.gochinatv.ad.tools.LogCat;
import com.gochinatv.ad.ui.view.AutoTextView;
import com.okhtttp.OkHttpCallBack;
import com.okhtttp.response.ADFourResponse;
import com.okhtttp.response.LayoutResponse;
import com.okhtttp.service.ADHttpService;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by zfy on 2016/3/16.
 */
public class ADFourFragment extends BaseFragment {

    private AutoTextView autoTextView;
    private List<String> stringList;

    private boolean isCycleText;
    private Timer cycleTextTimer;

    public LayoutResponse getLayoutResponse() {
        return layoutResponse;
    }

    public void setLayoutResponse(LayoutResponse layoutResponse) {
        this.layoutResponse = layoutResponse;
    }

    private LayoutResponse layoutResponse;

    @Override
    protected View initLayout(LayoutInflater inflater, ViewGroup container) {

        LinearLayout linearLayout = (LinearLayout) inflater.inflate(R.layout.fragment_ad_four, container, false);
        if(layoutResponse != null){

            if(!TextUtils.isEmpty(layoutResponse.adWidth) && !TextUtils.isEmpty(layoutResponse.adHeight)
                    && !TextUtils.isEmpty(layoutResponse.adTop) && !TextUtils.isEmpty(layoutResponse.adLeft)){

                String widthStr = layoutResponse.adWidth;
                String heightStr = layoutResponse.adHeight;
                String topStr = layoutResponse.adTop;
                String leftStr = layoutResponse.adLeft;

                //动态布局
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
                double width = (float) (DataUtils.getDisplayMetricsWidth(getActivity())*(Float.parseFloat(widthStr)));
                double height = (float) (DataUtils.getDisplayMetricsHeight(getActivity())*(Float.parseFloat(heightStr)));
                double top = (float) (DataUtils.getDisplayMetricsWidth(getActivity())*(Float.parseFloat(topStr)));
                double left = (float) (DataUtils.getDisplayMetricsHeight(getActivity())*(Float.parseFloat(leftStr)));

                params.width = (int) Math.floor(width);
                params.height = (int) Math.floor(height);
                params.topMargin = (int) Math.floor(top);
                params.leftMargin = (int) Math.floor(left);
                linearLayout.setLayoutParams(params);
                LogCat.e(" 广告四布局 width: "+params.width+" height: "+params.height+" top: "+params.topMargin+" left: "+params.leftMargin);

            }
        }

        return linearLayout;
    }

    @Override
    protected void initView(View rootView) {
        autoTextView = (AutoTextView) rootView.findViewById(R.id.auto_textview);
    }

    @Override
    protected void init() {
        autoTextView.setText("2016/03/31 19:00 Tuesday,Washington DC      内容合作Contact us:Service@gochinatv.com");
        stringList = new ArrayList<>();

        stringList.add("2016/03/31 19:00 Tuesday,Washington DC");
        stringList.add("内容合作Contact us:Service@gochinatv.com");
        stringList.add("2016/03/31 19:00 Tuesday,Washington DC      内容合作Contact us:Service@gochinatv.com");
        cycleTextTimer = new Timer();
        cycleTextTimer.schedule(new CycleText(),10000,10000);
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
        super.onDestroy();
        if(cycleTextTimer != null){
            cycleTextTimer.cancel();
            cycleTextTimer = null;
        }
    }

    int i = 0;
    private class CycleText extends TimerTask{
        @Override
        public void run() {
            // 定义一个消息传过去
            Message msg = new Message();
            msg.what = i++;
            handler.sendMessage(msg);
        }
    }

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            int index = msg.what;
            if(index > stringList.size()-1){
                index = 0;
                i = 1;
            }
            LogCat.e("i :"+ i +" adVideoIndex :"+index);
            autoTextView.next();
            autoTextView.setText(stringList.get(index));
            super.handleMessage(msg);
        }
    };


    /**
     * 请求文字广告
     */
    private void doGetTextAD(){
        ADHttpService.doHttpGetTextADInfo(getActivity(), new OkHttpCallBack<ADFourResponse>() {
            @Override
            public void onSuccess(String url, ADFourResponse response) {
                LogCat.e(" url " + url);
                if(!isAdded()){
                    return;
                }













            }

            @Override
            public void onError(String url, String errorMsg) {






            }
        });

    }






}
