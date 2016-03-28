package com.httputils;

import android.app.Activity;
import android.os.Bundle;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);

//        String adVideoUrl = "http://api.vego.tv" + "/focus_v1/focusList";
//        Map<String, String> params = new HashMap<String, String>();
//        params.put("focusId", "19");
//        HttpUtils.doHttpGet(FocusListResponse.class, adVideoUrl, params, new OnRequestListener<FocusListResponse>() {
//            @Override
//            public void onSuccess(FocusListResponse response, String adVideoUrl) {
//                if (response instanceof FocusListResponse) {
//                    Log.e("Tag", "success -> adVideoUrl: " + adVideoUrl);
//                }
//            }
//
//            @Override
//            public void onError(String errorMsg, String adVideoUrl) {
//                Log.e("Tag", "fali -> errorMsg: " + errorMsg);
//            }
//        }, "test");

    }

}
