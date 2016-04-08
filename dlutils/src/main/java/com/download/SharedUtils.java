package com.download;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by fq_mbp on 16/4/8.
 */
public class SharedUtils {


    private static final String SHARED_TAG = "SharedUtils";

    private static final String SHARED_KEY = "SharedUtils_download_file_name";

    private SharedUtils(){};


    public static void put(Context context, String value){
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_TAG, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SHARED_KEY, value);
        editor.apply();
    }

    public static String getValue(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_TAG, Activity.MODE_PRIVATE);
        return sharedPreferences.getString(SHARED_KEY, "");
    }


    public static void clear(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_TAG, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }



}
