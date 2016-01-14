package com.vego.player;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by fq_mbp on 15/8/10.
 */
public class SecurityChain {

    /**
     * 点播地址
     */
    public static final String SECURITY_CHAIN_URL = "http://vod.vegocdn.com";
    /**
     * 测试 直播地址
     */
//    public static final String SECURITY_LIVE_CHAIN_URL = "http://test4.vego.tv";

    /**
     * 测试 path
     */
    public static final String PATH_TEST = "vego.tv";
    /**
     * 直播地址
     */
    public static final String SECURITY_LIVE_CHAIN_URL = "http://live.vegocdn.com";

    public static String getSecurityTokey(String path, String timeStr, String cdnUrlPath){
        String sourceToken = path + "?" + "st=" + timeStr + "&pass=" + cdnUrlPath;
        return MD5.digest(sourceToken);
    }

    /**
     * @return 返回当前时间10分钟后的时间str
     */
    public static String getOutDate(){
        // 1、取得本地时间：
        Calendar cal = Calendar.getInstance();
        // 2、取得时间偏移量：
        int zoneOffset = cal.get(java.util.Calendar.ZONE_OFFSET);
        // 3、取得夏令时差：
        int dstOffset = cal.get(java.util.Calendar.DST_OFFSET);
        // 4、从本地时间里扣除这些差量，即可以取得UTC时间：
        cal.add(java.util.Calendar.MILLISECOND, -(zoneOffset + dstOffset));

        long time = cal.getTimeInMillis() + 600000;
        return String.valueOf(time);
    }

    public static String getOutDate(Date date){
        // 1、取得本地时间：
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        // 2、取得时间偏移量：
        int zoneOffset = cal.get(java.util.Calendar.ZONE_OFFSET);
        // 3、取得夏令时差：
        int dstOffset = cal.get(java.util.Calendar.DST_OFFSET);
        // 4、从本地时间里扣除这些差量，即可以取得UTC时间：
        cal.add(java.util.Calendar.MILLISECOND, -(zoneOffset + dstOffset));

        long time = cal.getTimeInMillis();
        return String.valueOf(time / 1000);
    }


}
