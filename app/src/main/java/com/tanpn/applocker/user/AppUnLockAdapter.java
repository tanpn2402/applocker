package com.tanpn.applocker.user;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.tanpn.applocker.R;
import com.tanpn.applocker.sqlite.SQLAppUnLock;
import com.tanpn.applocker.utils.AppDetail;
import com.tanpn.applocker.utils.AppInstalled;
import com.tanpn.applocker.utils.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by phamt_000 on 12/3/16.
 */
public class AppUnLockAdapter extends BaseAdapter {

    private Context context;

    private LayoutInflater mInflater;
    private Handler handler;

    private List<AppDetail> listApps;
    private List<AppDetail> list;
    private SQLAppUnLock sqlAppUnLock;



    public AppUnLockAdapter(Context context){
        this.context = context;
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        handler = new Handler();
        listApps = new ArrayList<>();
        sqlAppUnLock = new SQLAppUnLock(context);
        list = sqlAppUnLock.getAll();
        listApps = new ArrayList<>();
        handler.post(new getApp());
    }

    class getApp implements Runnable{

        @Override
        public void run() {
            for(AppDetail app : list){
                AppDetail a = AppInstalled.getInstance(context).getDetail(app.getAppPackage());
                if(a != null){
                    app.setAppName(a.getAppName());
                    app.setIcon(a.getIcon());


                    listApps.add(app);
                    Collections.sort(listApps);

                    notifyDataSetChanged();
                }
            }
        }
    }


    @Override
    public int getCount() {
        return listApps.size();
    }

    @Override
    public Object getItem(int i) {
        return listApps.get(i);
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

        holder.imAppIcon.setImageBitmap(utils.drawableToBitmap(listApps.get(i).getIcon()));
        holder.tvAppName.setText(listApps.get(i).getAppName());
        holder.tvPackage.setText(listApps.get(i).getAppPackage());
        holder.tvAppUsages.setText(listApps.get(i).getDatetime().size() + "");

        return convertView;
    }
}
