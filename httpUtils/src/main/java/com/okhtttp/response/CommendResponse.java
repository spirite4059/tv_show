package com.okhtttp.response;

import java.util.List;

/**
 * Created by fq_mbp on 16/5/26.
 */
public class CommendResponse {


    /**
     * isJsCommend : 0
     * cmd : [{"cmdInfo":"open","ad":"1"},{"cmdInfo":"close","ad":"2"}]
     */

    private String isJsCommand;
    /**
     * cmdInfo : open
     * ad : 1
     */

    private List<CmdResponse> cmd;

    public String getIsJsCommand() {
        return isJsCommand;
    }

    public void setIsJsCommend(String isJsCommand) {
        this.isJsCommand = isJsCommand;
    }

    public List<CmdResponse> getCmd() {
        return cmd;
    }

    public void setCmd(List<CmdResponse> cmd) {
        this.cmd = cmd;
    }

    public static class CmdResponse {
        private String cmdInfo;
        private String ad;

        public String getCmdInfo() {
            return cmdInfo;
        }

        public void setCmdInfo(String cmdInfo) {
            this.cmdInfo = cmdInfo;
        }

        public String getAd() {
            return ad;
        }

        public void setAd(String ad) {
            this.ad = ad;
        }
    }
}
