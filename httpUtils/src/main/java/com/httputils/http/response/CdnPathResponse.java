package com.httputils.http.response;

/**
 * Created by fq_mbp on 15/11/24.
 */
public class CdnPathResponse {

    public String code;
    public String message;
    public String ts;
    public PathInfo data;

    public class PathInfo{
        public String version;
        public String key;
        public String url;//经过防盗链加密的
    }

}
