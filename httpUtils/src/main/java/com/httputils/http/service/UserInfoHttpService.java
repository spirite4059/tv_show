package com.httputils.http.service;

import android.content.Context;

import com.httputils.http.HttpUtils;
import com.httputils.http.OnRequestListener;
import com.httputils.http.response.CurrentOrderResponse;
import com.httputils.http.response.OrderListResponse;

import java.util.Map;

/**
 * Created by zfy on 2015/8/19.
 */
public class UserInfoHttpService {

    /**
     * 支付接口
     */
    //private final static String PAY_BASE_HTTP_URL = "http://vp.vego.tv";   http://121.42.192.204:8080/cpay
    private final static String PAY_BASE_HTTP_URL = "http://121.42.192.204:8080/cpay";//测试地址http://192.168.1.207:8081

    /**
     * 查询支付信息
     */
    public static final String URL_CHECK_PAY_STATUS = PAY_BASE_HTTP_URL + "/checkPayStat.html";

    /**
     * 提交支付信息
     */
    public static final String URL_COMMIT_PAY_INFO = PAY_BASE_HTTP_URL + "/toPay.html";

    /**
     * 获取当前有效订单
     */
    public static final String URL_CHECK_USER_PAY_INFO = PAY_BASE_HTTP_URL + "/getCurrentOrder.html";

    /**
     * 获取历史订单
     */
    public static final String URL_GET_ORDER_LIST = PAY_BASE_HTTP_URL + "/getPayHistory.html";


    /**
     *获取历史订单
     */
    public static void doHttpOrderList(Context context,Map<String,String> map,String json,OnRequestListener<OrderListResponse> listener){
        HttpUtils.getInstance(context).doHttpPostJson(OrderListResponse.class,URL_GET_ORDER_LIST,map,json,listener,"orderList");
    }


    /**
     *当前有效订单
     */
    public static void doHttpCurrentOrder(Context context,Map<String,String> map,String json,OnRequestListener<CurrentOrderResponse> listener){
        HttpUtils.getInstance(context).doHttpPostJson(CurrentOrderResponse.class,URL_CHECK_USER_PAY_INFO,map,json,listener,"current");
    }



    /**
     *轮循查询是否支付
     */
    public static void doHttpCheckStatus(Context context,Map<String,String> map,String json,OnRequestListener<CurrentOrderResponse> listener){
        HttpUtils.getInstance(context).doHttpPostJson(CurrentOrderResponse.class,URL_CHECK_PAY_STATUS,map,json,listener,"check");
    }


    /**
     * 取得当前服务器时间
     */
    public static void doHttpGetCurrentTime(Context context,OnRequestListener<String> listener){
        String url = "http://ec2-54-148-190-90.us-west-2.compute.amazonaws.com:8080/date.jsp?o=0";
        HttpUtils.getInstance(context).doHttpGetString(url,listener,"currentTime");
    }







}
