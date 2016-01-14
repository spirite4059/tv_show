package com.httputils.http.response;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by zfy on 2015/8/21.
 */
public class AlbumListByLabelResponse implements Serializable {



    private static final long serialVersionUID = 1341891870982004104L;

    public String message;// success,
   // public String status;// 1,
    public String timestamp;// 1421720902419,
    //public String page;// 1,
    //public String ok;// 1,
   // public String size;// 10,
    public String total;// 560,
    public String ts;//
    public ArrayList<AlbumDetailResponse> data;//

    public int code;
}
