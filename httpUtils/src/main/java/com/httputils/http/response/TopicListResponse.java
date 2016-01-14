package com.httputils.http.response;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by zfy on 2015/8/31.
 */
public class TopicListResponse implements Serializable {

    public String message;// success,
    public String status;// 1,
    public String page;// 1,
    public String size;// 10,
    public String total;// 560,
    public ArrayList<TopicDetailResponse> data;//


}
