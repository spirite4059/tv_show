package com.httputils.http.response;

import java.util.ArrayList;

/**
 * Created by zfy on 2015/8/19.
 */
public class OrderListResponse {


    public PageResponse page;// 560,
    public ArrayList<UserPayInfoResponse> orderlist;//


    public class PageResponse {
        public String total;//
        public String pagecount;//
        public String pagenum;//
        public String pagesize;//

    }
}
