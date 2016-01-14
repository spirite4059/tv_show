package com.httputils.http.response;

import java.io.Serializable;

/**
 * Created by zfy on 2015/8/20.
 */
public class UpdateResponse implements Serializable {

    public String status;

    public String msg;

    public UpdateInfoResponse resultForApk;

    /**
     *
     */

    public class UpdateInfoResponse implements Serializable{

        private static final long serialVersionUID = -2223856081144319557L;


        public String id;//	true	string	10	升级信息主键
        public String platformId;//	true	string	10	平台主键
        public String platformName;//	true	string	100	平台名称
        public String brandNumber;//	true	string	100	品牌编号
        public String modelNumber;//	true	string	100	型号编号
        public String roomName;//	true	stirng	100	最低room
        public String versionCode;//	true	string	10	版本等级，判断升级的唯一标示
        public String versionNumber;//	true	string	100	版本号
        public String versionName;//	true	string	100	升级包名称
        public String fileUrl;//	true	string	无限制	升级包下载地址
        public String status;//	true	string	10	1 上线，2下线
        public String description;//	true	string	无限制	升级包基本描述信息
        public String type;//	true	string	10	1 强制强制，2是推荐升级

    }


}
