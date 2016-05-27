package com.gochinatv.ad.cmd;

import com.okhtttp.response.ADDeviceDataResponse;

/**
 * Created by fq_mbp on 16/5/27.
 */
public class OpenCommend implements ICommend {

    private CmdReceiver cmdReceiver;
    private String cmd;
    ADDeviceDataResponse adDeviceDataResponse;

    public OpenCommend(String cmd, CmdReceiver cmdReceiver, ADDeviceDataResponse adDeviceDataResponse){
        this.cmdReceiver = cmdReceiver;
        this.cmd = cmd;
        this.adDeviceDataResponse = adDeviceDataResponse;
    }

    @Override
    public void execute() {
        cmdReceiver.open(cmd, adDeviceDataResponse);
    }
}
