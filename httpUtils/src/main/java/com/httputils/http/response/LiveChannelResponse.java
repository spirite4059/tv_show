package com.httputils.http.response;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by zfy on 2015/9/11.
 */
public class LiveChannelResponse implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 8176267159599698658L;
    /**
     *
     */
    public int liveId;
    public String name;// ": "央视",
    public int displayOrder;// ": 0,
    public ArrayList<ChannelResponse> list;

    public class ChannelResponse implements Serializable {

        /**
         *
         */
        private static final long serialVersionUID = -7544757088239633214L;
        /**
         *
         */
        // public int id;//": 0,
        public String liveNum;// ": "001",
        public String liveName;// ": "CCTV1",
        public String liveTypeId;// "//: "1",
        public String liveSource;// ": "5",
        public String sourceAddress;// ": "http://127.0.0.1:9906/cmd.xml?cmd=switch_chan&id=5525068f0003218e04d897964beb88e4&server=caomei1_force_live.vego.tv:9906",
        public String imgUrl;// ": "",
        public String status;// ": "1",
        public String liveType;// ": null

        public int selectedStatus = 0;

        public int selectedPosition = 0;
    }

}
