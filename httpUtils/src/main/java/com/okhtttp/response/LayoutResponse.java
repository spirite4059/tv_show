package com.okhtttp.response;

import android.view.View;
import android.widget.RelativeLayout;

import java.io.Serializable;

/**
 * Created by zfy on 2016/4/7.
 */
public class LayoutResponse implements Serializable{

    public String adType;
    public String adWidth;
    public String adHeight;
    public String adTop;
    public String adLeft;


    public void setAdOneLayout(View rootView, int metricsWidth, int metricsHeight){
        adWidth = "0.83125";
        adHeight = "0.83125";
        adTop = "0.084375";
        adLeft = "0";
        //动态布局
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        float width = (metricsWidth * (Float.parseFloat(adWidth)));
        float height = (metricsHeight * (Float.parseFloat(adHeight)));
        float top = (metricsHeight * (Float.parseFloat(adTop)));
        float left = (metricsWidth * (Float.parseFloat(adLeft)));
        params.width = Math.round(width);
        params.height = Math.round(height);
        params.topMargin = Math.round(top);
        params.leftMargin = Math.round(left);


        params.width = (int) Math.round(width);
        params.height = (int) Math.round(height);
        params.topMargin = (int) Math.round(top);
        params.leftMargin = (int) Math.round(left);
        if (rootView != null) {
            rootView.setLayoutParams(params);
        }
    }

}
