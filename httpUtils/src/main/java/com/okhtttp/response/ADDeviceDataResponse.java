package com.okhtttp.response;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by zfy on 2016/4/7.
 */
public class ADDeviceDataResponse implements Serializable{

    public String status;

    public String message;

    public String adStruct;
    public long pollInterval;
    public long currentTime;
    public String code;
    public ScreenShotResponse screenShot;

    public ArrayList<LayoutResponse>  layout;





































}
