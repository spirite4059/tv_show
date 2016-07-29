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


    public void setAdOneLayout(View rootView, int metricsWidth){
        adWidth = "0.83125";
        adHeight = "0.83125";
        adTop = "0.084375";
        adLeft = "0";
        //动态布局
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        float width = (metricsWidth * (Float.parseFloat(adWidth)));
        float height = (metricsWidth * (Float.parseFloat(adHeight)));
        float top = (metricsWidth * (Float.parseFloat(adTop)));
        float left = (metricsWidth * (Float.parseFloat(adLeft)));
        params.width = Math.round(width);
        params.height = Math.round(height);
        params.topMargin = Math.round(top);
        params.leftMargin = Math.round(left);
        if (rootView != null) {
            rootView.setLayoutParams(params);
        }
    }

}
