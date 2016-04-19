package com.gochinatv.ad.db;

/**
 * Created by fq_mbp on 15/12/24.
 */
public interface IDBConstants {
    // 版本
    int DBBASE_VERSION = 1;
    // 数据库名称
    String DBBASE_NAME = "DB_CHINA_RESTAURANT_AD";
    // 表明
    String DBBASE_TABLE_NAME = "TABLE_CHINA_RESTAURANT_AD";

    String adVideoName = "adVideoName";
    String adVideoUrl = "adVideoUrl";
    String videoPath = "videoPath";
    String adVideoIndex = "adVideoIndex";
    String adVideoLength = "adVideoLength";
    String adVideoId = "adVideoId";

    // 创建数据表
    String SQL_CREAT = "CREATE TABLE IF NOT EXISTS " + DBBASE_TABLE_NAME + " ("
            + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
            + adVideoId + " int, "
            + adVideoName + " VARCHAR, "
            + adVideoUrl + " VARCHAR, "
            + videoPath + " VARCHAR, "
            + adVideoIndex + " int, "
            + adVideoLength + " long"
            + ")";
    // 删除表
    String SQL_DROP = "DROP TABLE IF EXISTS " + DBBASE_TABLE_NAME;
    // 插入表
    String SQL_INSERT = "insert into " + DBBASE_TABLE_NAME + " (" + adVideoId
            + ", " + adVideoName + ", " + adVideoUrl + ", "
            + videoPath + ", " + adVideoIndex + ", "
            + adVideoLength + ", " + adVideoId +  ") values (?,?,?,?,?,?,?)";

    String SQL_QUERY = "select * from " + DBBASE_TABLE_NAME + " order by " + adVideoId;
    String SQL_QUERY_ID = "select * from " + DBBASE_TABLE_NAME + " where  adVideoId = ?";





}
