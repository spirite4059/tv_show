package com.gochinatv.ad.cmd;

/**
 * Created by fq_mbp on 16/5/27.
 */
public class CloseCommend implements ICommend {

    private CmdReceiver cmdRecevier;
    private String cmd;

    public CloseCommend(String cmd, CmdReceiver cmdRecevier){
        this.cmdRecevier = cmdRecevier;
        this.cmd = cmd;
    }

    @Override
    public void execute() {
        cmdRecevier.close(cmd);
    }
}
