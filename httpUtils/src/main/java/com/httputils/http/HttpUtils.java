package com.httputils.http;

import android.content.Context;
import android.text.TextUtils;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.httputils.http.okHttp.OkHttpStack;
import com.httputils.http.request.GetRequest;
import com.httputils.http.request.JsonPostRequest;
import com.httputils.http.request.StatisticalPostRequest;
import com.httputils.http.request.StringGetRequest;
import com.httputils.http.request.StringPostRequest;
import com.squareup.okhttp.OkHttpClient;

import java.util.Map;

/**
 * Created by fq_mbp on 15/7/30.
 */
public class HttpUtils {

    private final String TAG = "HttpRequest";
    /**
     * volley超时时间
     */
    private static final int VOLLEY_PARAM_TIME_OUT = 10000;
    /**
     * volley重试次数
     */
    private static final int VOLLEY_PARAM_MAX_RETRIES = 3;

    private RequestQueue mRequestQueue;
    private static HttpUtils instance;

    private HttpUtils() {
    }

    private Context context;

    public static synchronized HttpUtils getInstance(Context context) {
        if (instance == null) {
            instance = new HttpUtils();
            instance.context = context.getApplicationContext();
        }
        return instance;
    }


    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(context, new OkHttpStack(new OkHttpClient()));
        }
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag) {
        // set the default tag if tag is empty
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }


    public <T> void doHttpGet(Class<T> clazz, final String url, Map<String, String> headsMap, Map<String, String> paramsMap, final OnRequestListener<T> listener, String tag) {
        GetRequest getRequest = new GetRequest(clazz, url, headsMap, paramsMap, tag, new Response.Listener<T>() {
            @Override
            public void onResponse(T response) {
                if (listener != null) {
                    listener.onSuccess(response, url);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (listener != null) {
                    listener.onError(error.getMessage(), url);
                }
            }
        });
        getRequest.setRetryPolicy(new DefaultRetryPolicy(VOLLEY_PARAM_TIME_OUT, VOLLEY_PARAM_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        addToRequestQueue(getRequest, tag);
    }

    public <T> void doHttpGet(Class<T> clazz, String url, Map<String, String> paramsMap, OnRequestListener<T> listener, String tag) {
        doHttpGet(clazz, url, null, paramsMap, listener, tag);
    }

    public <T> void doHttpGet(Class<T> clazz, String url, OnRequestListener<T> listener, String tag) {
        doHttpGet(clazz, url, null, null, listener, tag);
    }


    public <T> void doHttpPost(final String url, Map<String, String> paramsMap, final OnRequestListener<String> listener, String tag) {
        StatisticalPostRequest postRequest = new StatisticalPostRequest(url, paramsMap, tag, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (listener != null) {
                    listener.onSuccess(response, url);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (listener != null) {
                    listener.onError(error.getMessage(), url);
                }
            }
        });
        postRequest.setRetryPolicy(new DefaultRetryPolicy(VOLLEY_PARAM_TIME_OUT, VOLLEY_PARAM_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        addToRequestQueue(postRequest, tag);
    }


    public <T> void doHttpPost(final String url, final OnRequestListener<String> listener, String tag) {
        doHttpPost(url, null, listener, tag);
    }


    /**
     * 带json的post请求
     * @param clazz
     * @param url
     * @param headersMap
     * @param json
     * @param listener
     * @param tag
     * @param <T>
     */
    public <T> void doHttpPostJson(Class<T> clazz, final String url, Map<String, String> headersMap, String json, final OnRequestListener<T> listener, String tag) {
        JsonPostRequest jsonPostRequest = new JsonPostRequest(clazz, url, headersMap, json, new Response.Listener<T>() {
            @Override
            public void onResponse(T response) {
                if (listener != null) {
                    listener.onSuccess(response, url);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (listener != null) {
                    listener.onError(error.getMessage(), url);
                }
            }
        });
        jsonPostRequest.setRetryPolicy(new DefaultRetryPolicy(VOLLEY_PARAM_TIME_OUT, VOLLEY_PARAM_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        addToRequestQueue(jsonPostRequest, tag);
    }


    /**
     * 返回string的get请求
     * @param url
     * @param listener
     * @param tag
     */
    public void doHttpGetString (final String url,final OnRequestListener<String> listener,String tag){
        StringGetRequest stringRequest = new StringGetRequest(url , new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (listener != null) {
                    listener.onSuccess(response, url);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (listener != null) {
                    listener.onError(error.getMessage(), url);
                }
            }
        });
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(VOLLEY_PARAM_TIME_OUT, VOLLEY_PARAM_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        addToRequestQueue(stringRequest, tag);

    }

    /**
     * 返回string的get请求
     * @param url
     * @param listener
     * @param tag
     */
    public void doHttpGetString (final String url, Map<String, String> params, final OnRequestListener<String> listener,String tag){
        StringGetRequest stringRequest = new StringGetRequest(url , params, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (listener != null) {
                    listener.onSuccess(response, url);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (listener != null) {
                    listener.onError(error.getMessage(), url);
                }
            }
        });
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(VOLLEY_PARAM_TIME_OUT, VOLLEY_PARAM_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        addToRequestQueue(stringRequest, tag);

    }


    /**
     * 返回string的get请求
     * @param url
     * @param listener
     * @param tag
     */
    public void doHttpPostString (final String url, Map<String, String> params, final OnRequestListener<String> listener,String tag){
        StringPostRequest stringRequest = new StringPostRequest(url , params, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (listener != null) {
                    listener.onSuccess(response, url);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (listener != null) {
                    listener.onError(error.getMessage(), url);
                }
            }
        });
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(VOLLEY_PARAM_TIME_OUT, VOLLEY_PARAM_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        addToRequestQueue(stringRequest, tag);

    }

}
