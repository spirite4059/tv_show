package com.gochinatv.ad.cmd;

/**
 * Created by fq_mbp on 16/5/27.
 */
public class RefreshWebCommend implements ICommend {

    private CmdReceiver cmdReceiver;

    public RefreshWebCommend(CmdReceiver cmdRecevier){
        this.cmdReceiver = cmdRecevier;
    }

    @Override
    public void execute() {
        cmdReceiver.refreshWeb();
    }
}
