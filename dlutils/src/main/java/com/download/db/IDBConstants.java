package com.download.db;

/**
 * Created by fq_mbp on 15/12/24.
 */
public interface IDBConstants {
    // 版本
    int DBBASE_VERSION = 1;
    String DBBASE_NAME = "DB_DOWNLOAD";


    /**
     * ----------------------------下载表---------------------------------
     */
    String DBBASE_DOWNLOAD_TABLE_NAME = "DOWNLOAD_INFO";
    String tid = "tid";
    String tname = "tname";
    String turl = "turl";
    String tlength = "tlength";
    String startPos = "startPos";
    String endPos = "endPos";
    // 创建数据表

    String SQL_CREATE_DOWNLOAD_LIST = "CREATE TABLE IF NOT EXISTS " + DBBASE_DOWNLOAD_TABLE_NAME + " ("
            + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
            + tid + " int, "
            + tname + " VARCHAR, "
            + turl + " VARCHAR, "
            + tlength + " long, "
            + startPos + " long, "
            + endPos + " long"
            + ")";
    // 删除表
    String SQL_DROP_DOWNLOAD_TABLE = "DROP TABLE IF EXISTS " + DBBASE_DOWNLOAD_TABLE_NAME;


    String SQL_QUERY_DOWNLOAD_BY_URL = "select * from " + DBBASE_DOWNLOAD_TABLE_NAME + " where " + turl + " = ?";
    String SQL_QUERY_DOWNLOAD_BY_NAME = "select * from " + DBBASE_DOWNLOAD_TABLE_NAME + " where " + tname + " = ?";
    String SQL_QUERY_DOWNLOAD_BY_ID = "select * from " + DBBASE_DOWNLOAD_TABLE_NAME + " where " + tid + " = ?";
    String SQL_QUERY_DOWNLOAD = "select * from " + DBBASE_DOWNLOAD_TABLE_NAME;

}
