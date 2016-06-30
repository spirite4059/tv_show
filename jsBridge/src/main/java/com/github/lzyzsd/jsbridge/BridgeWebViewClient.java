package com.github.lzyzsd.jsbridge;

import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * Created by bruce on 10/28/15.
 */
public class BridgeWebViewClient extends WebViewClient {
    private BridgeWebView webView;

    //private Handler reloadHandler;
    private RelaodRunnable relaodRunnable;
    private int interval = 3*60*1000;//默认3分钟

    public BridgeWebViewClient(BridgeWebView webView) {
        this.webView = webView;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        try {
            url = URLDecoder.decode(url, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        if (url.startsWith(BridgeUtil.YY_RETURN_DATA)) { // 如果是返回数据
            webView.handlerReturnData(url);
            return true;
        } else if (url.startsWith(BridgeUtil.YY_OVERRIDE_SCHEMA)) { //
            webView.flushMessageQueue();
            return true;
        } else {
            return super.shouldOverrideUrlLoading(view, url);
        }
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);

        if (BridgeWebView.toLoadJs != null) {
            BridgeUtil.webViewLoadLocalJs(view, BridgeWebView.toLoadJs);
        }

        //
        if (webView.getStartupMessage() != null) {
            for (Message m : webView.getStartupMessage()) {
                webView.dispatchMessage(m);
            }
            webView.setStartupMessage(null);
        }
    }

    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        Log.e("BridgeWebView","加载失败onReceivedError : URL加载失败！！！！！！");
        if(webView != null ){
            webView.setVisibility(View.INVISIBLE);
        }
        //重新加载
//        if(reloadHandler == null){
//            reloadHandler = new Handler();
//        }
        if(relaodRunnable == null){
            relaodRunnable = new RelaodRunnable();
        }
        if(webView != null && relaodRunnable != null){
            webView.postDelayed(relaodRunnable,interval);
        }
        super.onReceivedError(view, errorCode, description, failingUrl);
    }

    private class  RelaodRunnable implements Runnable{
        @Override
        public void run() {
            if(webView != null){
                Log.e("BridgeWebView","再次重试加载url");
                webView.reload();
            }
        }
    }

    /**
     * 取消重新加载
     */
    public void cancelRunnable(){
        if(webView != null && relaodRunnable != null){
            Log.e("BridgeWebView","cancelRunnable : 取消了url的重新加载！！！");
            webView.removeCallbacks(relaodRunnable);
        }
    }

}