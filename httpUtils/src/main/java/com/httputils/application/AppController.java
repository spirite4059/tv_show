package com.httputils.application;

import android.app.Application;

/**
 * Created by fq_mbp on 15/7/30.
 */
public class AppController extends Application {

//    public static final String TAG = AppController.class.getSimpleName();
//
//    private RequestQueue mRequestQueue;
//    private static AppController mInstance;
//
//    @Override
//    public void onCreate() {
//        super.onCreate();
//        mInstance = this;
//    }
//
//    public static synchronized AppController getInstance() {
//        return mInstance;
//    }
//
//    public RequestQueue getRequestQueue() {
//        if (mRequestQueue == null) {
////            mRequestQueue = Volley.newRequestQueue(getApplicationContext(), new OkHttpStack(new OkHttpClient()));
//        }
//        return mRequestQueue;
//    }
//
//    public <T> void addToRequestQueue(Request<T> req, String tag) {
//        // set the default tag if tag is empty
//        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
//        getRequestQueue().add(req);
//    }
//
//    public <T> void addToRequestQueue(Request<T> req) {
//        req.setTag(TAG);
//        getRequestQueue().add(req);
//    }
//
//    public void cancelPendingRequests(Object tag) {
//        if (mRequestQueue != null) {
//            mRequestQueue.cancelAll(tag);
//        }
//    }
}
