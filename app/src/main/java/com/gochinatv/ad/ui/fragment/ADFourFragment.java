package com.gochinatv.ad.ui.fragment;

import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gochinatv.ad.R;
import com.gochinatv.ad.base.BaseFragment;
import com.gochinatv.ad.tools.LogCat;
import com.gochinatv.ad.ui.view.AutoTextView;

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

    @Override
    protected View initLayout(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.fragment_ad_four,container,false);
    }

    @Override
    protected void initView(View rootView) {
        autoTextView = (AutoTextView) rootView.findViewById(R.id.auto_textview);
    }

    @Override
    protected void init() {
        autoTextView.setText("东方嘉禾");
        stringList = new ArrayList<>();
        stringList.add("死神");
        stringList.add("海贼王");
        stringList.add("灌篮高手AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
        stringList.add("火影忍者");
        stringList.add("银魂");

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



}
