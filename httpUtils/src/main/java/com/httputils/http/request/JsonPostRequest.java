package com.httputils.http.request;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * Created by zfy on 2015/8/19.
 */
public class JsonPostRequest<T> extends JsonObjectRequest {


    private Gson gson;
    private Class<T> mClazz;
    private Map<String, String> headersMap;
    private String requestBody;



    public JsonPostRequest(Class<T> clazz, String url, Map<String, String> headersMap,String requestBody, Response.Listener listener, Response.ErrorListener errorListener) {
        super(Request.Method.POST, url, requestBody, listener, errorListener);
        this.mClazz = clazz;
        this.headersMap = headersMap;
        this.requestBody = requestBody;

    }


    public JsonPostRequest(int method, String url, String requestBody, Response.Listener listener, Response.ErrorListener errorListener) {
        super(method, url, requestBody, listener, errorListener);
    }


    /**
     * 设置请求
     * @return
     * @throws AuthFailureError
     */
    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return headersMap != null ? headersMap : super.getHeaders();
    }


    @Override
    public byte[] getBody() {
        return requestBody == null ? super.getBody() : requestBody.getBytes();
    }

    /**
     * 解析并返回实体类
     * @param response
     * @return
     */
    @Override
    protected Response parseNetworkResponse(NetworkResponse response) {
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



}
