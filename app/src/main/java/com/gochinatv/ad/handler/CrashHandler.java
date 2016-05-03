package com.gochinatv.ad.handler;

import android.content.Context;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.gochinatv.ad.tools.DataUtils;
import com.gochinatv.ad.tools.LogCat;
import com.okhtttp.OkHttpCallBack;
import com.okhtttp.request.ErrorMsgRequest;
import com.okhtttp.service.ErrorHttpServer;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 收集手机全局崩溃时的exception,并log到本地
 *
 * @author Jackland_zgl
 *
 */
public class CrashHandler implements Thread.UncaughtExceptionHandler {

    public static final int LogFileLimit = 50;

    public static final String TAG = "CrashHandler";

    //系统默认的UncaughtException处理类
    private Thread.UncaughtExceptionHandler mDefaultHandler;
    //CrashHandler实例
    private static CrashHandler INSTANCE = new CrashHandler();
    //程序的Context对象
    private Context mContext;
    //用来存储设备信息和异常信息
//    private Map<String, String> infos = new HashMap<String, String>();

    //用于格式化日期,作为日志文件名的一部分
    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

    private ExecutorService executorService;

    /**
     * 保证只有一个CrashHandler实例
     */
    private CrashHandler() {
    }

    /**
     * 获取CrashHandler实例 ,单例模式
     */
    public static CrashHandler getInstance() {
        return INSTANCE;
    }

    /**
     * 初始化
     *
     * @param context
     */
    public void init(Context context) {
        if(context == null){
            return;
        }
        mContext = context;
        //获取系统默认的UncaughtException处理器
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        //设置该CrashHandler为程序的默认处理器
        Thread.setDefaultUncaughtExceptionHandler(this);
        executorService = Executors.newSingleThreadExecutor();
    }

    /**
     * 当UncaughtException发生时会转入该函数来处理
     */
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        LogCat.e("exception", "uncaughtException..........");
        if (!handleException(ex) && mDefaultHandler != null) {
            //如果用户没有处理则让系统默认的异常处理器来处理
            mDefaultHandler.uncaughtException(thread, ex);
            LogCat.e("exception", "uncaughtException..........系统处理");
        } else {
            LogCat.e("exception", "uncaughtException..........退出系统");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Log.e(TAG, "error : ", e);
            }
            //退出程序
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);
        }
    }


    /**
     * 收集设备参数信息
     *
     * @param ctx
     */
//    public void collectDeviceInfo(Context ctx) {
//        LogCat.e("exception", "collectDeviceInfo.......... ");
//        try {
//            PackageManager pm = ctx.getPackageManager();
//            PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(), PackageManager.GET_ACTIVITIES);
//            if (pi != null) {
//                String versionName = pi.versionName == null ? "null" : pi.versionName;
//                String versionCode = pi.versionCode + "";
//                infos.put("versionName", versionName);
//                infos.put("versionCode", versionCode);
//            }
//        } catch (PackageManager.NameNotFoundException e) {
//            Log.e(TAG, "an error occured when collect package info", e);
//        }
//        Field[] fields = Build.class.getDeclaredFields();
//        for (Field field : fields) {
//            try {
//                field.setAccessible(true);
//                infos.put(field.getName(), field.get(null).toString());
//            } catch (Exception e) {
//                Log.e(TAG, "an error occured when collect crash info", e);
//            }
//        }
//    }

    /**
     * 保存错误信息到文件中
     *
     * @param ex
     * @return 返回文件名称, 便于将文件传送到服务器
     */
    private int saveCrashInfo2File(final Throwable ex) {
        try {
            LogCat.e("exception", "saveCrashInfo2File.......... ");
            StringBuffer sb = new StringBuffer();
//        for (Map.Entry<String, String> entry : infos.entrySet()) {
//            String key = entry.getKey();
//            String value = entry.getValue();
//            sb.append(key + "=" + value + "\n");
//        }
            //递归获取全部的exception信息
            Writer writer = new StringWriter();
            PrintWriter printWriter = new PrintWriter(writer);
            ex.printStackTrace(printWriter);
            Throwable cause = ex.getCause();
            while (cause != null) {
                cause.printStackTrace(printWriter);
                cause = cause.getCause();
            }
            printWriter.close();
            String result = writer.toString();
            sb.append(result); //将写入的结果
            final String errorMsg = sb.toString();

            //写文件和限制数量
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    //将设备信息变成string
                    //构造文件名
                    long timestamp = System.currentTimeMillis();
                    String time = formatter.format(new Date());
                    final String fileName = "crash-" + time + "-" + timestamp + ".log";
                    DataUtils.writeFileToSdcard(DataUtils.getLogDirectory(), fileName, errorMsg);
                    cleanLogFileToN(DataUtils.getLogDirectory() + fileName);
                }
            });


            ErrorHttpServer.doHttpUpLog(mContext, errorMsg, new OkHttpCallBack<ErrorMsgRequest>() {
                @Override
                public void onSuccess(String url, ErrorMsgRequest response) {
                    LogCat.e("video", "错误日志上传成功...........");

                }

                @Override
                public void onError(String url, String errorMsg) {
                    LogCat.e("video", "错误日志上传错误...........");
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }



        return 1;
    }

    Comparator<File> newfileFinder = new Comparator<File>() {

        @Override
        public int compare(File x, File y) {
            // TODO Auto-generated method stub
            if (x.lastModified() > y.lastModified()) return 1;
            if (x.lastModified() < y.lastModified()) return -1;
            else return 0;
        }

    };

    private int cleanLogFileToN(String dirname) {
        try {
            File dir = new File(dirname);
            if (dir.isDirectory()) {
                File[] logFiles = dir.listFiles();
                if (logFiles.length > LogFileLimit) {
                    Arrays.sort(logFiles, newfileFinder);  //从小到大排
                    //删掉N个以前的
                    for (int i = 0; i < logFiles.length - LogFileLimit; i++) {
                        logFiles[i].delete();
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return 1;
    }


    /**
     * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成.
     *
     * @param ex
     * @return true:如果处理了该异常信息;否则返回false.
     */
    private boolean handleException(Throwable ex) {
        LogCat.e("exception", "handleException.......... ");
        if (ex == null) {
            return false;
        }
        //使用Toast来显示异常信息
        new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                Toast.makeText(mContext, "很抱歉,程序出现异常", Toast.LENGTH_LONG).show();
                Looper.loop();
            }
        }.start();

        //收集设备参数信息
//        collectDeviceInfo(mContext);
        //保存日志文件
        saveCrashInfo2File(ex);
        return true;
    }
}