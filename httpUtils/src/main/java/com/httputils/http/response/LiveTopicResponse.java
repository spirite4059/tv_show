package com.httputils.http.response;

import java.util.ArrayList;

/**
 * Created by zfy on 2015/8/26.
 */
public class LiveTopicResponse {

    public String message;
    public String status;
    public String page;
    public String size;
    public String total;
    public LiveTopicInfo data;


    public class LiveTopicInfo {
        public String picUrl;
        public String text;
        public String liveStartTime;
        public String currentTime;
        public ArrayList<LiveTopicList> list;
    }

    public class LiveTopicList {
        public String picUrl;
        public String name;
        public String playUrl;
        public String isLive;
    }

}
