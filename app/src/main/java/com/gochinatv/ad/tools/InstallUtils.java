package com.gochinatv.ad.tools;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

/**
 * Created by hqc on 2015/8/12.
 *
 * 安装app 工具类
 */
public class InstallUtils {

    /**
     * 下载 完成 自动安装，，不实现静默安装 体验不好
     * @param  context
     * @param  apkFile 安装目录
     * @param  installSilence 静默安装，，
     */

    public static void  installAuto(Context context,File apkFile,boolean installSilence ){
        //静默 安装
        if(installSilence){
            PackageInfo pInfo = null;
            try {
                pInfo = context.getPackageManager().getPackageInfo("com.gochinatv.ad", 0);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            if(hasRootPermission()){
                //  have  root
//                Toast.makeText(context, "提醒：提醒：即将开始安装新版本，稍后自动重启！", Toast.LENGTH_LONG).show();
                SharedPreference.getSharedPreferenceUtils(context).saveDate("isClientInstall", true);
                LogCat.e("获取到root权限，开始静默升级。。。。。。。");
                installSilent(context, apkFile.getAbsolutePath(), true);
                // rootClientInstall(apkFile.getAbsolutePath());
            }else if (isSystemApp(pInfo) || isSystemUpdateApp(pInfo)){
//                Toast.makeText(context,"正在更新软件！",Toast.LENGTH_SHORT).show();
                SharedPreference.getSharedPreferenceUtils(context).saveDate("isClientInstall", true);
                LogCat.e("获取到系统权限，开始静默升级。。。。。。。");
//                Toast.makeText(context,"提醒：即将开始安装新版本，稍后自动重启！",Toast.LENGTH_LONG).show();
                installSilent(context, apkFile.getAbsolutePath(), false);

            }else {
                LogCat.e("没有获取到任何权限，普通安装。。。。。。。");
//                Intent installAPKIntent = new Intent(Intent.ACTION_VIEW);
//                installAPKIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                installAPKIntent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
//                context.startActivity(installAPKIntent);

            }
            return;
        }
        Intent installAPKIntent = new Intent(Intent.ACTION_VIEW);
        installAPKIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        installAPKIntent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
        context.startActivity(installAPKIntent);
    }

    /**
     * 点击系统通知栏安装
     * @param apkFile
     * @throws IOException
     */
    public  static  void  installByNotification (Context context,File apkFile) throws IOException {

        NotificationManager  mNotifyManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);

        String appName=context.getString(context.getApplicationInfo().labelRes);
        int icon=context.getApplicationInfo().icon;
        mBuilder.setContentTitle(appName).setSmallIcon(icon);

        mBuilder.setContentText("下载成功").setProgress(0, 0, false);
        Intent installAPKIntent = new Intent(Intent.ACTION_VIEW);
        //如果没有设置SDCard写权限，或者没有sdcard,apk文件保存在内存中，需要授予权限才能安装
        String[] command = {"chmod","777",apkFile.toString()};
        ProcessBuilder builder = new ProcessBuilder(command);
        builder.start();
        installAPKIntent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
        //installAPKIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //installAPKIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        //installAPKIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, installAPKIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setContentIntent(pendingIntent);
        Notification noti = mBuilder.build();
        noti.flags = Notification.FLAG_AUTO_CANCEL;
        mNotifyManager.notify(0, noti);
    }


    public static String installSilent(Context context, String filePath,boolean isRoot) {
        return installSilent(context, filePath, " -r " + "-f", isRoot);
    }

    public static String installSilent(Context context, String filePath, String pmParams,boolean isRoot){

        StringBuilder command = new StringBuilder().append("LD_LIBRARY_PATH=/vendor/lib:/system/lib pm install ")
                .append(pmParams == null ? "" : pmParams).append(" ").append(filePath.replace(" ", "\\ "));

        CommandResult commandResult = execCommand(command.toString(), isRoot, true);

        if (commandResult.successMsg != null
                && (commandResult.successMsg.contains("Success") || commandResult.successMsg.contains("success"))) {
            Toast.makeText(context,"更新成功，正在重启！",Toast.LENGTH_SHORT).show();
            return "INSTALL_SUCCEEDED";
        }
        return "INSTALL_FAILED_OTHER";
    }

    public static CommandResult execCommand(String command, boolean isRoot, boolean isNeedResultMsg) {
        return execCommand(new String[] {command}, isRoot, isNeedResultMsg);
    }

    public static CommandResult execCommand(String[] commands, boolean isRoot, boolean isNeedResultMsg) {
        int result = -1;
        if (commands == null || commands.length == 0) {
            return new CommandResult(result, null, null);
        }
        Process process = null;
        BufferedReader successResult = null;
        BufferedReader errorResult = null;
        StringBuilder successMsg = null;
        StringBuilder errorMsg = null;

        DataOutputStream os = null;
        try {
            process = Runtime.getRuntime().exec(isRoot ? "su" : "sh");
            os = new DataOutputStream(process.getOutputStream());
            for (String command : commands) {
                if (command == null) {
                    continue;
                }
                // donnot use os.writeBytes(commmand), avoid chinese charset error
                os.write(command.getBytes());
                os.writeBytes("\n");
                os.flush();
            }
            os.writeBytes("exit\n");
            os.flush();

            result = process.waitFor();
            // get command result
            if (isNeedResultMsg) {
                successMsg = new StringBuilder();
                errorMsg = new StringBuilder();
                successResult = new BufferedReader(new InputStreamReader(process.getInputStream()));
                errorResult = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                String s;
                while ((s = successResult.readLine()) != null) {
                    successMsg.append(s);
                }
                while ((s = errorResult.readLine()) != null) {
                    errorMsg.append(s);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                if (successResult != null) {
                    successResult.close();
                }
                if (errorResult != null) {
                    errorResult.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (process != null) {
                process.destroy();
            }
        }
        return new CommandResult(result, successMsg == null ? null : successMsg.toString(), errorMsg == null ? null
                : errorMsg.toString());
    }

    public static class CommandResult {

        /** result of command **/
        public int    result;
        /** success message of command result **/
        public String successMsg;
        /** error message of command result **/
        public String errorMsg;

        public CommandResult(int result) {
            this.result = result;
        }

        public CommandResult(int result, String successMsg, String errorMsg) {
            this.result = result;
            this.successMsg = successMsg;
            this.errorMsg = errorMsg;
        }
    }

    /**
     * 是否为系统软件
     * @param pInfo
     * @return
     */
    public static boolean isSystemApp(PackageInfo pInfo) {
        if(pInfo == null ) return false ;
        return ((pInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0);
    }

    /**
     * 是否为系统更新软件
     * @param pInfo
     * @return
     */
    public static boolean isSystemUpdateApp(PackageInfo pInfo) {
        if(pInfo == null ) return false ;
        return ((pInfo.applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0);
    }

    /**
     * 是否为用户软件
     * @param pInfo
     * @return
     */
    private static boolean isUserApp(PackageInfo pInfo) {
        if(pInfo == null ) return false ;
        return (!isSystemApp(pInfo) && !isSystemUpdateApp(pInfo));
    }

    /**
     * 静默卸载
     */
    private static boolean clientUninstall(String packageName){
        PrintWriter PrintWriter = null;
        Process process = null;
        try {
            process = Runtime.getRuntime().exec("su");
            PrintWriter = new PrintWriter(process.getOutputStream());
            PrintWriter.println("LD_LIBRARY_PATH=/vendor/lib:/system/lib ");
            PrintWriter.println("pm uninstall "+packageName);
            PrintWriter.flush();
            PrintWriter.close();
            int value = process.waitFor();
            return returnResult(value);
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            if(process!=null){
                process.destroy();
            }
        }
        return false;
    }

    /**
     * 启动app
     * com.exmaple.client/.MainActivity
     *
     */
    public static boolean startApp(String packageName,String activityName){
        boolean isSuccess = false;
        String cmd = "am start -n " + packageName + "/" + activityName + " \n";
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(cmd);
            int value = process.waitFor();
            return returnResult(value);
        } catch (Exception e) {
            e.printStackTrace();
        } finally{
            if(process!=null){
                process.destroy();
            }
        }
        return isSuccess;
    }


    /**
     * 判断手机是否有root权限
     */
    public static boolean hasRootPermission(){
        PrintWriter PrintWriter = null;
        Process process = null;
        try {
            process = Runtime.getRuntime().exec("su");//执行su是向系统请求root权限,赋给当前进程
            PrintWriter = new PrintWriter(process.getOutputStream());
            PrintWriter.flush();
            PrintWriter.close();
            int value = process.waitFor();
            return returnResult(value);
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            if(process!=null){
                process.destroy();
            }
        }
        return false;
    }

    /**
     * 结果返回 处理
     * @param value
     * @return
     */
    private static boolean returnResult(int value){
        // 代表成功
        if (value == 0) {
            return true;
        } else if (value == 1) { // 失败
            return false;
        } else { // 未知情况
            return false;
        }
    }
}
