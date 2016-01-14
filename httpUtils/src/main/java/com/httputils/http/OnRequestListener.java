package com.httputils.http;

public interface OnRequestListener<T> {

    void onSuccess(T response, String url);

    void onError(String errorMsg, String url);
}
