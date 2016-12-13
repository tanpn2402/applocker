package com.tanpn.applocker.utils;

import android.graphics.drawable.Drawable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by phamt_000 on 12/1/16.
 */
public class AppDetail implements Comparable{
    private String appPackage;
    private String appName;
    private Drawable icon;
    private int usage;
    private boolean chose;  // duoc chon hay chua, duoc dung trong danh sach group, luc chon app vao nhom


    // dành riêng cho app unlock history
    private List<String> datetime;
    private List<String> location;
    private List<String> photo;

    public List<String> getDatetime() {return datetime;}

    public List<String> getLocation() {return location;}

    public List<String> getPhoto() {return photo;}

    public void setDatetime(List<String> datetime) {this.datetime = datetime;}

    public void setLocation(List<String> location) {this.location = location;}

    public void setPhoto(List<String> photo) {this.photo = photo;}

    public AppDetail(String s1, String s2, Drawable d, int i,String dt, String l, String p){
        appPackage = s1;
        appName = s2;
        icon = d;
        usage = i;


        if(dt.equals("")){
            datetime = new ArrayList<>();
        }else{
            String h = dt.substring(1, dt.length() - 1);
            datetime = new ArrayList<>(Arrays.asList(h.split("[\\s,]+")));
        }

        if(l.equals("")){
            location = new ArrayList<>();
        }else{
            String h = l.substring(1, l.length() - 1);
            location = new ArrayList<>(Arrays.asList(h.split("[\\s,]+")));
        }

        if(p.equals("")){
            photo = new ArrayList<>();
        }else{
            String h = p.substring(1, p.length() - 1);
            photo = new ArrayList<>(Arrays.asList(h.split("[\\s,]+")));
        }
    }

    //

    public String getAppPackage() {return appPackage;}

    public String getAppName() {return appName;}

    public Drawable getIcon() {return icon;}

    public void setIcon(Drawable icon) {this.icon = icon;}

    public void setAppName(String appName) {this.appName = appName;}

    public void setAppPackage(String appPackage) {this.appPackage = appPackage;}

    public int getUsage() {return usage;}

    public boolean isChose() {return chose;}

    public void setChose(boolean chose) {this.chose = chose;}

    public void setUsage(int usage) {this.usage = usage;}

    public AppDetail(String s1, String s2, Drawable d, int i){
        appPackage = s1;
        appName = s2;
        icon = d;
        usage = i;
    }

    @Override
    public int compareTo(Object o) {

        AppDetail a = (AppDetail) o;
        if(usage == a.usage)
            return 0;
        else if(usage > a.usage)
            return -1;
        else
            return 1;
    }
}