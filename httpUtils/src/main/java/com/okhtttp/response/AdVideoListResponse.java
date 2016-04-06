package com.okhtttp.response;

import java.util.ArrayList;

/**
 * Created by fq_mbp on 16/4/6.
 */
public class AdVideoListResponse {

    public String status;
    public String message;
    public String currentTime;
    public ArrayList<AdDetailResponse> current;
    public ArrayList<AdDetailResponse> next;

}
