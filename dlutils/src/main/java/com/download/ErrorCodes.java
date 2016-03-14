package com.download;

/**
 * Created by fq_mbp on 16/2/29.
 */
final class ErrorCodes {

    /**
     * 中断下载
     */
    public static final int ERROR_INTERRUPT_DOWNLOAD = 1001;


    /**
     * 重新下载当前内容
     */
    public static final int ERROR_DOWNLOAD_WRITE = 1002;


    /**
     * 读取数据过程出错
     */
    public static final int ERROR_DOWNLOADING_READ = 1003;

    /**
     * 线程数小于0
     */
    public static final int ERROR_THREAD_NUMBERS = 1004;


    /**
     * URL生成失败
     */
    public static final int ERROR_DOWNLOAD_URL = 1005;


    /**
     * 获取网络流失败
     */
    public static final int ERROR_DOWNLOAD_CONN = 1006;

    /**
     * 获取文件大小有问题
     */
    public static final int ERROR_DOWNLOAD_FILE_SIZE = 1007;

    /**
     * 下载文件存储路径出错
     */
    public static final int ERROR_DOWNLOAD_FILE_LOCAL = 1008;

    /**
     * 下载文件生成出错
     */
    public static final int ERROR_DOWNLOAD_FILE_NULL = 1009;

    /**
     * 下载中断
     */
    public static final int ERROR_DOWNLOAD_INTERRUPT = 1010;

    /**
     * in缓冲流出错或null
     */
    public static final int ERROR_DOWNLOAD_BUFFER_IN = 1011;
    /**
     * 随机流出错
     */
    public static final int ERROR_DOWNLOAD_RANDOM = 1012;
    /**
     * 随机流定为出错
     */
    public static final int ERROR_DOWNLOAD_RANDOM_SEEK = 1013;

    /**
     * 随机流定为出错
     */
    public static final int ERROR_DOWNLOAD_UNKNOWN = 1014;

    /**
     *  下载文件大小出错
     */
    public static final int ERROR_DOWNLOAD_FILE_UNKNOWN = 1015;


    static final int HTTP_OK = 200;
    static final int HTTP_CREATED = 201;
    static final int HTTP_ACCEPTED = 202;
    static final int HTTP_NOT_AUTHORITATIVE = 203;
    static final int HTTP_NO_CONTENT = 204;
    static final int HTTP_RESET = 205;
    static final int HTTP_PARTIAL = 206;
    static final int HTTP_MULTI_STATUS = 207;

    static final int HTTP_MULT_CHOICE = 300;
    static final int HTTP_MOVED_PERM = 301;
    static final int HTTP_MOVED_TEMP = 302;
    static final int HTTP_SEE_OTHER = 303;
    static final int HTTP_NOT_MODIFIED = 304;
    static final int HTTP_USE_PROXY = 305;
    static final int HTTP_TEMP_REDIRECT = 307;

    static final int HTTP_BAD_REQUEST = 400;
    static final int HTTP_UNAUTHORIZED = 401;
    static final int HTTP_PAYMENT_REQUIRED = 402;
    static final int HTTP_FORBIDDEN = 403;
    static final int HTTP_NOT_FOUND = 404;
    static final int HTTP_BAD_METHOD = 405;
    static final int HTTP_NOT_ACCEPTABLE = 406;
    static final int HTTP_PROXY_AUTH = 407;
    static final int HTTP_CLIENT_TIMEOUT = 408;
    static final int HTTP_CONFLICT = 409;
    static final int HTTP_GONE = 410;
    static final int HTTP_LENGTH_REQUIRED = 411;
    static final int HTTP_PRECON_FAILED = 412;
    static final int HTTP_ENTITY_TOO_LARGE = 413;
    static final int HTTP_REQ_TOO_LONG = 414;
    static final int HTTP_UNSUPPORTED_TYPE = 415;
    static final int HTTP_REQUESTED_RANGE_NOT_SATISFIABLE = 416;
    static final int HTTP_EXPECTATION_FAILED = 417;
    static final int HTTP_UNPROCESSABLE_ENTITY = 422;
    static final int HTTP_LOCKED = 423;
    static final int HTTP_FAILED_DEPENDENCY = 424;

    static final int HTTP_INTERNAL_ERROR = 500;
    static final int HTTP_NOT_IMPLEMENTED = 501;
    static final int HTTP_BAD_GATEWAY = 502;
    static final int HTTP_UNAVAILABLE = 503;
    static final int HTTP_GATEWAY_TIMEOUT = 504;
    static final int HTTP_VERSION = 505;
    static final int HTTP_INSUFFICIENT_STORAGE = 507;

}
