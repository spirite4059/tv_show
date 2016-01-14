package com.httputils.http.request;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.Map;

/**
 * Created by fq_mbp on 15/8/3.
 */
public class StatisticalPostRequest<T> extends StringRequest {

    private Map<String, String> paramsMap;
    private Response.Listener<String> listener;
    private String tag;




    /**
     *
     * @param url               请求url
     * @param paramsMap         参数集合
     * @param tag               request 标签，为了以后取消改请求用
     * @param listener          volley的请求返回监听
     * @param errorListener     volley的错误返回监听
     */
    public StatisticalPostRequest(String url, Map<String, String> paramsMap, String tag, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        super(Request.Method.POST, url, listener, errorListener);
        this.listener = listener;
        this.paramsMap = paramsMap;
        this.tag = tag;
    }



    /**
     *
     * @param url               请求url
     * @param tag               request 标签，为了以后取消改请求用
     * @param listener          volley的请求返回监听
     * @param errorListener     volley的错误返回监听
     */
    public StatisticalPostRequest(String url, String tag, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        this(url, null, tag, listener, errorListener);
    }




    //mMap是已经按照前面的方式,设置了参数的实例
    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return paramsMap;
    }


}
