package com.tanpn.applocker.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by phamt_000 on 9/29/16.
 */
public class PreUtils {

    // file app_lock_list ( ~ app_lock_list.xml): luu cac gia tri cua app lock list
    public static String PREF_FILE_APPS = "app_lock_list";


    // tra ve doi tuong share preference
    public static SharedPreferences appPrefs(Context context){
        return context.getSharedPreferences(PREF_FILE_APPS, Context.MODE_PRIVATE);
    }

    // luu lai gia tri cua mEditor
    @SuppressLint("NewApi")
    public static void apply(SharedPreferences.Editor editor) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
            editor.commit();
        } else {
            editor.apply();
        }
    }

    // lay gia tri tu file app_lock_list
    public static Set<String> getLockedApps(Context c) {
        SharedPreferences sp = appPrefs(c);
        return new HashSet<>(sp.getAll().keySet());
    }

}
