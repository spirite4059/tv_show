package com.httputils.http.response;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by zfy on 2015/8/18.
 */
public class MenuListResponse implements Serializable {


    private static final long serialVersionUID = 1208050209697013116L;
    /** 菜单Id */
    public String menuId;
    /** 菜单内容 */
    public String text;
    /**  */
    public String url;
    /** 菜单图标地址 */
    public String imgUrl;
    /**  */
    public String imgBrightUrl;
    /** 类型 是否有焦点图 */
    public String showType;

    public String focusId;

    /**  */
    public String  parentId;
    /**  */
    public String englishName;
    /** 是否为父节点 */
    public String parentNode;

    public String platform;
    public String displayOrder;
    public ArrayList<ChildNode> childNode;

    /** 频道id--在为用户推荐时视频时用 */
    public String channelId;

    public class ChildNode implements Serializable{
        /**
         *
         */
        private static final long serialVersionUID = 4182200179515490412L;
        /** 菜单Id */
        public String menuId;
        /** 菜单内容 */
        public String text;
        /**  */
        public String url;
        /** 菜单图标地址 */
        public String imgUrl;
        /**  */
        public String imgBrightUrl;
        /** 类型 是否有焦点图 */
        public String showType;
        /**  */
        public String parentId;

        public String focusId;

        /**  */
        public String englishName;
        /** 是否为父节点 */
        public String parentNode;

        public String platform;

        public String displayOrder;

        /** 频道id--在为用户推荐时视频时用 */
        public String channelId;

        public ArrayList<ChildNode> childNode;
    }

}
