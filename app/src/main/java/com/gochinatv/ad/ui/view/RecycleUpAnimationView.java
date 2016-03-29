package com.gochinatv.ad.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Scroller;
import android.widget.TextView;

import com.gochinatv.ad.R;

import java.util.ArrayList;

/**
 * Created by zfy on 2016/3/29.
 */
public class RecycleUpAnimationView extends LinearLayout implements Runnable{


    private Context mContext;
    private Scroller mScroller;
    private LayoutInflater layoutInflater;

    private ArrayList<String> nameList = new ArrayList<>();
    private ArrayList<String> priceList = new ArrayList<>();

    public RecycleUpAnimationView(Context context) {
        super(context);
        mContext = context;
        init();
    }

    public RecycleUpAnimationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    public RecycleUpAnimationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init();
    }


    private void init(){
        mScroller = new Scroller(mContext);
        layoutInflater = LayoutInflater.from(mContext);
        nameList.add("aaaaaaaaaaa");
        priceList.add("13");

        nameList.add("bbbbbbbbbb");
        priceList.add("19");

        nameList.add("cccccccccc");
        priceList.add("18");

        nameList.add("dddddddd");
        priceList.add("12");
        nameList.add("eeeeeeeee");
        priceList.add("17");

        int seiz = nameList.size();
        for(int i=0;i<3;i++){
            View view = layoutInflater.inflate(R.layout.itme_ad_three, this, false);
            TextView name = (TextView)view.findViewById(R.id.ad_three_text_name);
            name.setText(nameList.get(i));
            TextView price = (TextView)view.findViewById(R.id.ad_three_text_price);
            price.setText(priceList.get(i)+"元");

            this.addView(view,i);
        }


    }



    @Override
    public void run() {

    }






}
