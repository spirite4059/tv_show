package com.httputils.http.request;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.httputils.utils.LogCat;

import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by fq_mbp on 15/8/3.
 */
public class GetRequest<T> extends Request<T> {

    private Gson gson;
    private Class<T> mClazz;
    private Response.Listener<T> listener;
    private Map<String, String> headersMap;
    private String tag;


    /**
     *
     * @param clazz             返回实体类型
     * @param url               接口请求url
     * @param headersMap        请求header设置
     * @param paramsMap         参数集合
     * @param tag               请求tag标签
     * @param listener          请求成功标签
     * @param errorListener     请求失败标签
     */
    public GetRequest(Class<T> clazz, String url, Map<String, String> headersMap, Map<String, String> paramsMap, String tag, Response.Listener<T> listener, Response.ErrorListener errorListener) {
        super(Method.GET, getUrlWithParams(url, paramsMap), errorListener);
        this.headersMap = headersMap;
        this.tag = tag;
        this.listener = listener;
        this.mClazz = clazz;
    }

    /**
     *
     * @param clazz             返回实体类型
     * @param url               接口请求url
     * @param paramsMap         参数集合
     * @param tag               请求tag标签
     * @param listener          请求成功标签
     * @param errorListener     请求失败标签
     */
    public GetRequest(Class<T> clazz, String url, Map<String, String> paramsMap, String tag, Response.Listener<T> listener, Response.ErrorListener errorListener) {
        this(clazz, url, paramsMap, null, tag, listener, errorListener);
    }

    /**
     *
     * @param clazz             返回实体类型
     * @param url               接口请求url
     * @param tag               请求tag标签
     * @param listener          请求成功标签
     * @param errorListener     请求失败标签
     */
    public GetRequest(Class<T> clazz, String url, String tag, Response.Listener<T> listener, Response.ErrorListener errorListener) {
        this(clazz, url, null, null, tag, listener, errorListener);
    }



    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return headersMap != null ? headersMap : super.getHeaders();
    }

    @Override
    protected void deliverResponse(T response) {
        if(listener != null){
            listener.onResponse(response);
        }
    }

    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        try {
            gson = new Gson();
            String json = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
            return Response.success(gson.fromJson(json, mClazz),
                    HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (JsonSyntaxException e) {
            return Response.error(new ParseError(e));
        }
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