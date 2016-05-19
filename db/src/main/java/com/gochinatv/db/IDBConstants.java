package com.gochinatv.db;

/**
 * Created by fq_mbp on 15/12/24.
 */
public interface IDBConstants {
    // 版本
    int DBBASE_VERSION = 1;
    // 数据库名称
    String DBBASE_NAME = "DB_CHINA_RESTAURANT_AD";
    // 表明
    String DBBASE_TD_VIDEOS_TABLE_NAME = "TABLE_CHINA_RESTAURANT_AD_TD";
    String DBBASE_TM_VIDEOS_TABLE_NAME = "TABLE_CHINA_RESTAURANT_AD_TM";

    String adVideoName = "adVideoName";
    String adVideoUrl = "adVideoUrl";
    String videoPath = "videoPath";
    String adVideoIndex = "adVideoIndex";
    String adVideoLength = "adVideoLength";
    String adVideoId = "adVideoId";

    // 创建数据表
    String SQL_CREATE_TODAY_VIDEO_LIST = "CREATE TABLE IF NOT EXISTS " + DBBASE_TD_VIDEOS_TABLE_NAME + " ("
            + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
            + adVideoId + " int, "
            + adVideoName + " VARCHAR, "
            + adVideoUrl + " VARCHAR, "
            + videoPath + " VARCHAR, "
            + adVideoIndex + " int, "
            + adVideoLength + " long"
            + ")";

    // 删除表
    String SQL_DROP = "DROP TABLE IF EXISTS " + DBBASE_TD_VIDEOS_TABLE_NAME;

    // 插入表
    String SQL_INSERT = "insert into " + DBBASE_TD_VIDEOS_TABLE_NAME + " (" + adVideoId
            + ", " + adVideoName + ", " + adVideoUrl + ", "
            + videoPath + ", " + adVideoIndex + ", "
            + adVideoLength + ") values (?,?,?,?,?,?)";

    String SQL_QUERY = "select * from " + DBBASE_TD_VIDEOS_TABLE_NAME + " order by " + adVideoId;
    String SQL_QUERY_ID = "select * from " + DBBASE_TD_VIDEOS_TABLE_NAME + " where " + adVideoId + " = ?";
    String SQL_QUERY_NAME = "select * from " + DBBASE_TD_VIDEOS_TABLE_NAME + " where "  + adVideoName + " = ?";



    // 创建数据表
    String SQL_CREATE_TOMORROW_VIDEO_LIST = "CREATE TABLE IF NOT EXISTS " + DBBASE_TM_VIDEOS_TABLE_NAME + " ("
            + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
            + adVideoId + " int, "
            + adVideoName + " VARCHAR, "
            + adVideoUrl + " VARCHAR, "
            + videoPath + " VARCHAR, "
            + adVideoIndex + " int, "
            + adVideoLength + " long"
            + ")";

    String SQL_DROP_TM = "DROP TABLE IF EXISTS " + DBBASE_TM_VIDEOS_TABLE_NAME;

    String SQL_INSERT_TM = "insert into " + DBBASE_TM_VIDEOS_TABLE_NAME + " (" + adVideoId
            + ", " + adVideoName + ", " + adVideoUrl + ", "
            + videoPath + ", " + adVideoIndex + ", "
            + adVideoLength + ") values (?,?,?,?,?,?)";

    String SQL_QUERY_TM = "select * from " + DBBASE_TM_VIDEOS_TABLE_NAME + " order by " + adVideoId;
    String SQL_QUERY_ID_TM = "select * from " + DBBASE_TM_VIDEOS_TABLE_NAME + " where " + adVideoId + " = ?";
    String SQL_QUERY_NAME_TM = "select * from " + DBBASE_TM_VIDEOS_TABLE_NAME + " where "  + adVideoName + " = ?";







}
