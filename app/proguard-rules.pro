# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/fq_mbp/develop/android-sdk-macosx/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class adVideoName to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

#
# 友盟统计混淆 ----------------start----------------
#
-keepclassmembers class * {
   public <init>(org.json.JSONObject);
}

-keep public class com.gochinatv.ad.R$*{
public static final int *;
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-dontwarn com.vego.player.**
-keep class com.vego.player.** {*; }
-keepattributes Signature

# For using GSON @Expose annotation
-keepattributes *Annotation*

# Gson specific classes
-keep class sun.misc.Unsafe { *; }


-dontwarn com.google.**
-keep class com.google.** {*; }


-dontwarn com.httputils.**
-keep public class com.httputils.** {*; }

-dontwarn com.squareup.okhttp.**

-dontwarn okio.**

-dontwarn com.android.volley.**

#忽略警告
-ignorewarning

-dump class_files.txt
#未混淆的类和成员
-printseeds build/seeds.txt
#列出从 apk 中删除的代码
-printusage build/unused.txt
#混淆前后的映射
-printmapping build/mapping.txt

-keepattributes SourceFile,LineNumberTable

#指定代码的压缩级别
-optimizationpasses 5
#包明不混合大小写
-dontusemixedcaseclassnames

#优化 不优化输入的类文件
-dontoptimize
#预校验
-dontpreverify