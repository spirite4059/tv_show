package com.gochinatv.ad.cmd;

/**
 * Created by fq_mbp on 16/5/27.
 */
public class FreshCommend implements ICommend {

    private CmdReceiver cmdRecevier;
    private String cmd;

    public FreshCommend(String cmd, CmdReceiver cmdRecevier){
        this.cmdRecevier = cmdRecevier;
        this.cmd = cmd;
    }

    @Override
    public void execute() {
        cmdRecevier.fresh(cmd);
    }
}
