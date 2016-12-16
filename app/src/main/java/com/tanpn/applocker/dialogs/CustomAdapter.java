package com.tanpn.applocker.dialogs;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.tanpn.applocker.R;
import com.tanpn.applocker.sqlite.SQLAppUsages;
import com.tanpn.applocker.utils.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by phamt_000 on 11/28/16.
 */
public class CustomAdapter extends BaseAdapter {

    private Context context;
    private Map<String, Integer> appUsages;
    private LayoutInflater mInflater;
    private Handler handler;


    private List<AppDetail> listApp = new ArrayList<>();
    private List<AppDetail> listAppToShow = new ArrayList<>();
    private List<String> appToShow;

    public CustomAdapter(Context context, List<String> apps){
        this.context = context;
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        handler = new Handler();

        appToShow = new ArrayList<>(apps);

        handler.post(new getApp());
    }

    class getApp implements Runnable{

        @Override
        public void run() {
            final PackageManager pm =context.getPackageManager();
            List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);

            for (ApplicationInfo packageInfo : packages) {
                if(appToShow.contains(packageInfo.packageName)){
                    // nam trong danh sach app can hien thi
                    AppDetail app = new AppDetail(packageInfo.packageName, packageInfo.loadLabel(context.getPackageManager()).toString(), packageInfo.loadIcon(context.getPackageManager()), 0);
                    listAppToShow.add(app);
                    notifyDataSetChanged();
                }

            }
        }
    }
    

    @Override
    public int getCount() {
        return listAppToShow.size();
    }

    @Override
    public Object getItem(int i) {
        return listAppToShow.get(i);
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
        ImageButton ibtRemove;
    }

    class AppDetail implements Comparable{
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
    }



    @Override
    public View getView(final int i, View convertView, ViewGroup viewGroup) {

        final ViewHolder holder;

        if(convertView == null){
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.layout_app_usage_item, null);
            holder.imAppIcon = (ImageView) convertView.findViewById(R.id.imAppIcon);
            holder.tvAppName = (TextView) convertView.findViewById(R.id.tvAppName);
            holder.tvPackage = (TextView) convertView.findViewById(R.id.tvPackage);
            holder.tvAppUsages = (TextView) convertView.findViewById(R.id.tvAppUsages);
            holder.ibtRemove = (ImageButton) convertView.findViewById(R.id.ibtRemove);

            convertView.setTag(holder);
        }else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.imAppIcon.setImageBitmap(utils.drawableToBitmap(listApp.get(i).icon));
        holder.tvAppName.setText(listApp.get(i).appName);
        holder.tvPackage.setText(listApp.get(i).appPackage);
        holder.tvAppUsages.setVisibility(View.GONE);
        holder.ibtRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                remove(listApp.get(i));
            }
        });

        return convertView;
    }

    public void remove(AppDetail app){
        listApp.remove(app);
        notifyDataSetChanged();
    }
}
