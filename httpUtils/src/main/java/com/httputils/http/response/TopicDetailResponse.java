package com.httputils.http.response;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by zfy on 2015/8/31.
 */
public class TopicDetailResponse implements Serializable {


    public String focusId;// success,
    public String focusName;// success,
    public String type;
    public String displayOrder;
    public ArrayList<FocusItemDetail> data;
    public String description;
    public String image;
    public String backImage;
    public String sort;
    public boolean startScroll;


}
