package com.httputils.http.request;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.httputils.utils.LogCat;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by fq_mbp on 15/9/17.
 */
public class StringGetRequest extends StringRequest {

    private Response.Listener<String> listener;
    private Map<String, String> paramsMap;

    public StringGetRequest(String url, Map<String, String> paramsMap, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        super(Method.GET, getUrlWithParams(url, paramsMap), listener, errorListener);
        this.listener = listener;
        this.paramsMap = paramsMap;
    }

    public StringGetRequest(String url, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        this(url, null, listener, errorListener);
    }

    private static String getUrlWithParams(String url, Map<String, String> paramsMap){
        if(paramsMap == null || paramsMap.size() == 0){
            return url;
        }
        StringBuilder sbUrl = new StringBuilder(url);
        sbUrl.append("?");
        Set<String> keySet = paramsMap.keySet();
        int size = keySet.size();
        Iterator<String> iterator = keySet.iterator();
        for(int i = 0; i < size; i++){
            String key = iterator.next();
            sbUrl.append(key);
            sbUrl.append("=");
            sbUrl.append(paramsMap.get(key));
            if(i < size - 1){
                sbUrl.append("&");
            }

        }
        LogCat.e("realUrl: " + sbUrl);
        return sbUrl.toString();
    }


}
