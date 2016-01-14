package com.httputils.http.response;

import java.util.ArrayList;

/**
 * Created by zfy on 2015/9/11.
 */
public class YoutubeUrlResponse {



    public String aid;
    public String vid;
    public String name;
    public String description;
    public ArrayList<PlayInfo> playInfo;


    public class PlayInfo {
        public String remotevid;
        public String siteId;
        public String duration;
        public String hvStartTime;
        public String hvEndTime;
        public String reSourceURL;

    }



}
