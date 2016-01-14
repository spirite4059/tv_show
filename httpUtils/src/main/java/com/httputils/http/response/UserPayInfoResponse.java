package com.httputils.http.response;

import java.io.Serializable;

/**
 * Created by zfy on 2015/8/19.
 */
public class UserPayInfoResponse implements Serializable {


    // 订单号
    public String oid;
    //第三方流水号
    public String thirdid;
    // 支付平台
    public String payplatform;
    // 终端类型
    public String clienttype;
    // 终端编号
    public String did;
    // 用户编号
    public String uid;
    // 业务编号
    public String softwareid;
    // 商品编号
    public String itemid;
    // 商品名称
    public String itemname;
    // 商品数量
    public String quantity;
    // 支付金额
    public String amount;
    // 货币类型
    public String currencycode;
    // 支付时间
    public String paytime;
    // 订单生效时间
    public String starttime;
    //订单结束时间
    public String endtime;
    // 订单状态
    public String stat;


}
