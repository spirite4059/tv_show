package com.httputils;

import android.app.Activity;
import android.os.Bundle;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);

//        String url = "http://api.vego.tv" + "/focus_v1/focusList";
//        Map<String, String> params = new HashMap<String, String>();
//        params.put("focusId", "19");
//        HttpUtils.doHttpGet(FocusListResponse.class, url, params, new OnRequestListener<FocusListResponse>() {
//            @Override
//            public void onSuccess(FocusListResponse response, String url) {
//                if (response instanceof FocusListResponse) {
//                    Log.e("Tag", "success -> url: " + url);
//                }
//            }
//
//            @Override
//            public void onError(String errorMsg, String url) {
//                Log.e("Tag", "fali -> errorMsg: " + errorMsg);
//            }
//        }, "test");

    }

}
