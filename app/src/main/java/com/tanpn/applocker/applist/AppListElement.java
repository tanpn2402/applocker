package com.tanpn.applocker.applist;

import android.graphics.drawable.Drawable;

/**
 * Created by phamt_000 on 9/28/16.
 */
public class AppListElement implements Comparable<AppListElement> {
    // title - name - label
    private String title;

    // ten package
    private String packageName;

    // icon cua app
    private Drawable icon;

    // kiem tra xem app nay co bi khoa hay khong
    private boolean locked = false;

    // muc do uu tien cua applist element
    private int priority;

    public static final int PRIORITY_IMPORTANT_CATEGORY = 7;  // separator
    public static final int PRIORITY_IMPORTANT_APPS = 6;
    public static final int PRIORITY_SYSTEM_CATEGORY = 5;   // separator
    public static final int PRIORITY_SYSTEM_APPS = 4;
    public static final int PRIORITY_NORMAL_CATEGORY = 3;   // separator
    public static final int PRIORITY_NORMAL_APPS = 1;

    public Drawable getIcon() {
        if(icon != null)
            return icon;
        else
            return null;
    }

    public String getLabel() {
        return title;
    }

    public AppListElement(String label, Drawable icon, int priority) {
        this.title = label;
        this.icon = icon;
        this.packageName = "dd";
        this.priority = priority;
    }

    /** For separators */
    public AppListElement(String label, int priority) {
        this.title = label;
        this.icon = null;
        this.packageName = "";
        this.priority = priority;
    }

    /** For non activity apps */
    public AppListElement(String label, String packageName, int priority) {
        this.title = label;
        this.icon = null;
        this.packageName = packageName;
        this.priority = priority;

    }


    // kiem tra xem co lock hay khong
    public boolean isLocked(){
        return locked;
    }

    // kiem tra xem Element nay la app hay la separator
    public boolean isApp() {
        return packageName != null && packageName.length() > 0;
    }

    @Override
    public int compareTo(AppListElement appListElement) {
        if (priority != appListElement.priority)
            return appListElement.priority - priority;

        if (this.locked != appListElement.locked)
            return this.locked ? -1 : 1;
        if (this.title == null || appListElement.title == null) {
            return 0;
        }
        return this.title.compareTo(appListElement.title);
    }
}
