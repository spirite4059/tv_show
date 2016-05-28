package com.gochinatv.ad.cmd;

/**
 * Created by fq_mbp on 16/5/27.
 */
public interface ICommend {

    String COMMEND_OPEN = "open";
    String COMMEND_CLOSE = "close";
    String COMMEND_FRESH = "fresh";

    void execute();

}
