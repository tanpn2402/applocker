package com.tanpn.applocker.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import com.tanpn.applocker.applist.AppListElement;
import com.tanpn.applocker.sqlite.SQLAppPassword;
import com.tanpn.applocker.sqlite.SQLGroupPermission;
import com.tanpn.applocker.user.GroupPermission;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by phamt_000 on 12/1/16.
 */
public class AppInstalled {

    private static AppInstalled instance = null;

    private static List<AppDetail> allApps = new ArrayList<>();
    private static List<AppDetail> importanceApps = new ArrayList<>();
    private static List<AppDetail> normalApps = new ArrayList<>();
    private static List<String> appChose = new ArrayList<>();


    public static void getChoseApps(Context context){
        // lay nhung app duoc chon

        /**
         * kiem tra nhung app da co nhom
         * */
        SQLGroupPermission sql = new SQLGroupPermission(context);
        List<GroupPermission> allGroup = sql.getAllGroups();


        for(GroupPermission g : allGroup){

            appChose.addAll(g.getApps());

        }

    }

    public static void getImportanceApps(Context context){

        final List<String> sysApp =  Arrays.asList(new String[] { "com.android.dialer" , "com.android.packageinstaller",
                                            "com.android.systemui", "com.android.vending", "com.android.settings" , "", ""});

        final PackageManager pm = context.getPackageManager();
        List<ApplicationInfo> listApps = pm.getInstalledApplications(0);

        for (ApplicationInfo packageInfo : listApps) {
            if(sysApp.contains(packageInfo.packageName)){

                AppDetail app = new AppDetail(packageInfo.packageName, packageInfo.loadLabel(context.getPackageManager()).toString(), packageInfo.loadIcon(context.getPackageManager()), 0);

                if(appChose.contains(app.getAppPackage()))
                    app.setChose(true);

                allApps.add(app);
            }

        }
    }

    public static void getNormalApps(Context context){
        final Intent i = new Intent(Intent.ACTION_MAIN);
        i.addCategory(Intent.CATEGORY_LAUNCHER);
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> appRunnable = pm.queryIntentActivities(i, 0);

        for (ResolveInfo app : appRunnable) {
            //if (!context.getPackageName().equals(app.activityInfo.packageName)) {

                AppDetail a = new AppDetail(app.activityInfo.packageName, app.loadLabel(pm).toString(), app.loadIcon(pm), 0);


                normalApps.add(a);
            //}
        }

    }

    public static AppInstalled getInstance(final Context context){
        if(instance == null){

            getChoseApps(context);

            getImportanceApps(context);

            getNormalApps(context);


            allApps.addAll(normalApps);
            allApps.addAll(importanceApps);

            new Runnable(){

                @Override
                public void run() {
                    SQLAppPassword sqlAppPassword = new SQLAppPassword(context);
                    sqlAppPassword.insertApp(allApps);
                }
            }.run();

            return instance = new AppInstalled();
        }

        else
            return instance;
    }

    public List<String> getAppChose(){
        return appChose;
    }

    public List<AppDetail> getAllAppsInstalled(){

        return allApps;
    }

    public void setAppChose(String app_package, boolean choose){
        for(AppDetail app : allApps){
            if(app.getAppPackage().equals(app_package)){
                app.setChose(choose);
                return;
            }
        }
    }

    public boolean getAppChose(String app_package){
        for(AppDetail app : allApps){
            if(app.getAppPackage().equals(app_package)){
                return app.isChose();
            }
        }

        return false;
    }

    public int getUsageTimes(String app_package){
        for(AppDetail app : allApps){
            if(app.getAppPackage().equals(app_package)){
                return app.getUsage();
            }
        }

        return 0;
    }

    public void setUsageTimes(String app_package, int count){
        for(AppDetail app : allApps){
            if(app.getAppPackage().equals(app_package)){
                app.setUsage(count);

                return;
            }
        }

    }
    public AppDetail getDetail(String app_package){
        for(AppDetail app : allApps){
            if(app.getAppPackage().equals(app_package)){
                return app;
            }
        }

        return null;
    }


}
