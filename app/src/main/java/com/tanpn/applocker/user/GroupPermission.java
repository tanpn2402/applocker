package com.tanpn.applocker.user;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by phamt_000 on 12/1/16.
 */
public class GroupPermission {

    private String title;
    private String password;
    private Integer count;
    private List<String> apps;

    public GroupPermission(String s1, String s2, Integer i, List<String> s3){
        title = s1;
        password = s2;
        count = i;
        apps = new ArrayList<>(s3);
    }

    public GroupPermission(String s1, String s2, Integer i, String s3){
        title = s1;
        password = s2;
        count = i;
        if(s3.equals("")){
            apps = new ArrayList<>();
        }else{
            String h = s3.substring(1, s3.length() - 1);
            apps = new ArrayList<>(Arrays.asList(h.split("[\\s,]+")));
        }
    }
    public String getTitle() {return title;}

    public String getPassword() {return password;}

    public Integer getCount() {return count;}

    public List<String> getApps() {return apps;}

    public void setTitle(String title) {
        this.title = title;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public void setApps(List<String> apps) {
        this.apps = new ArrayList<>(apps);
    }

    public void addApp(String app_package){
        if(!this.apps.contains(app_package)){
            apps.add(app_package);
        }
    }

    public void removeApp(String app_pakage){
        apps.remove(app_pakage);
    }
}
