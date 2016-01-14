package com.gochinatv.ad.db;

/**
 * Created by fq_mbp on 15/12/24.
 */
public interface DBConstants {
    // 版本
    int DBBASE_VERSION = 1;
    // 数据库名称
    String DBBASE_NAME = "DB_CHINA_RESTAURANT_AD";
    // 表明
    String DBBASE_TABLE_NAME = "TABLE_CHINA_RESTAURANT_AD";

    String video_name = "video_name";
    String video_start_time = "video_start_time";
    String video_end_time = "video_end_time";
    String video_index = "video_index";
    String video_id = "video_id";
    String video_path = "video_path";
    String video_url = "video_url";
    String video_downloaded = "video_downloaded";

    // 创建数据表
    String SQL_CREAT = "CREATE TABLE IF NOT EXISTS " + DBBASE_TABLE_NAME + " ("
            + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
            + video_id + " VARCHAR, "
            + video_name + " VARCHAR, "
            + video_index + " VARCHAR, "
            + video_start_time + " VARCHAR, "
            + video_end_time + " VARCHAR, "
            + video_path + " VARCHAR, "
            + video_url + " VARCHAR, "
            + video_downloaded + " VARCHAR"
            + ")";
    // 删除表
    String SQL_DROP = "DROP TABLE IF EXISTS " + DBBASE_TABLE_NAME;
    // 插入表
    String SQL_INSERT = "insert into " + DBBASE_TABLE_NAME + " (" + video_id + ", " + video_name + ", " + video_index + ", " + video_start_time + ", " + video_end_time + ", " + video_path + ", " + video_url + ", " + video_downloaded + ") values (?,?,?,?,?,?,?,?)";

    String SQL_QUERY = "select * from " + DBBASE_TABLE_NAME + " order by " + video_index;

}
