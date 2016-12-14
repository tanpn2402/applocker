package com.tanpn.applocker.user;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.tanpn.applocker.R;
import com.tanpn.applocker.sqlite.SQLAppUsages;
import com.tanpn.applocker.utils.AppDetail;
import com.tanpn.applocker.utils.AppInstalled;
import com.tanpn.applocker.utils.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by phamt_000 on 11/28/16.
 */
public class AppUsageAdapter extends BaseAdapter {

    private Context context;
    private SQLAppUsages sqLiteHelper;
    private List<String> appPackages;
    private Map<String, Integer> appUsages;
    private LayoutInflater mInflater;
    private Handler handler;

    private AppInstalled appInstalled;

    private List<AppDetail> listApp = new ArrayList<>();

    public AppUsageAdapter(Context context){
        this.context = context;
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        sqLiteHelper = new SQLAppUsages(context);
        handler = new Handler();
        appUsages = sqLiteHelper.getAll();

        appInstalled =  AppInstalled.getInstance(context);
        listApp = appInstalled.getAllAppsInstalled();
        Collections.sort(listApp);
        notifyDataSetChanged();

        appPackages = new ArrayList<>();
        for(Map.Entry<String, Integer> m : appUsages.entrySet() ){
            appPackages.add(m.getKey());

            // key = app_package
            // values = usages
            appInstalled.setUsageTimes(m.getKey(), m.getValue());
        }

        //new getApp().execute((Void[]) null);

        //handler.post(new getApp());
    }

    /*class getApp implements Runnable{

        @Override
        public void run() {
            final PackageManager pm =context.getPackageManager();
            List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);

            for (ApplicationInfo packageInfo : packages) {
                AppDetail app = new AppDetail(packageInfo.packageName, packageInfo.loadLabel(context.getPackageManager()).toString(), packageInfo.loadIcon(context.getPackageManager()), 0);
                if(appPackages.contains(app.appPackage)){
                    app.usage = appUsages.get(app.appPackage);
                }
                listApp.add(app);
                Collections.sort(listApp);
                notifyDataSetChanged();
            }
        }
    }*/
    

    @Override
    public int getCount() {
        return listApp.size();
    }

    @Override
    public Object getItem(int i) {
        return listApp.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    class ViewHolder{
        ImageView imAppIcon;
        TextView tvAppName;
        TextView tvPackage;
        TextView tvAppUsages;
    }

    /*class AppDetail implements Comparable{
        public String appPackage;
        public String appName;
        public Drawable icon;
        public int usage;

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
    }*/




    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {

        final ViewHolder holder;

        if(convertView == null){
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.layout_app_usage_item, null);
            holder.imAppIcon = (ImageView) convertView.findViewById(R.id.imAppIcon);
            holder.tvAppName = (TextView) convertView.findViewById(R.id.tvAppName);
            holder.tvPackage = (TextView) convertView.findViewById(R.id.tvPackage);
            holder.tvAppUsages = (TextView) convertView.findViewById(R.id.tvAppUsages);

            convertView.setTag(holder);
        }else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.imAppIcon.setImageBitmap(utils.drawableToBitmap(listApp.get(i).getIcon()));
        holder.tvAppName.setText(listApp.get(i).getAppName());
        holder.tvPackage.setText(listApp.get(i).getAppPackage());
        holder.tvAppUsages.setText(listApp.get(i).getUsage() + "");

        return convertView;
    }
}
