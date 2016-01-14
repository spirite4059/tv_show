package com.httputils.http.response;

/**
 * Created by zfy on 2015/8/24.
 */
public class CurrentOrderResponse {


    public ErrorResponse error;

    public UserPayInfoResponse order;



    public class ErrorResponse {
        public String code;//
        public String info;//

    }




}
