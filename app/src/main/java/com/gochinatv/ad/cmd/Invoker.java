package com.gochinatv.ad.cmd;

/**
 * Created by fq_mbp on 16/5/27.
 */
public class Invoker {

    private ICommend commend;

    public void setCommend(ICommend commend){
        this.commend = commend;
    }

    public void execute(){
        commend.execute();
    }

}
