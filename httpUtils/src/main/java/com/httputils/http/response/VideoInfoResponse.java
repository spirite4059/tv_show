package com.httputils.http.response;

import java.util.ArrayList;

/**
 * Created by zfy on 2015/8/24.
 */
public class VideoInfoResponse {

    public String message;
    public String status;
    public String timestamp;
    public String page;
    public String size;
    public String total;
    public VideoInfoArr data;

    public class VideoInfoArr{
        public String videoId;
        public ArrayList<VideoInfo> playInfo;

    }

    public class VideoInfo{
        public String remotevid;
        public String siteId;//1:youtube企业 2：dailymotion  3:youtube个人  4：facebook个人      5：原力     6：自建CDN  7：2mv
        public String duration;
        public String sourceURL;
    }

//	"message": "success",
//    "status": 1,
//    "timestamp": "1404816758986",
//    "page": 1,
//    "size": 1,
//    "total": 1,
//    "data": {
//        "videoId": 63036,
//        "playInfo": [
//            {
//                "remotevid": "XR1FgfN9fAw",
//                "siteId": "1",
//             }
//        ]
//    }
}
