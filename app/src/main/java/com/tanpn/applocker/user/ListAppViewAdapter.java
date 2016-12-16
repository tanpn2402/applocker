package com.tanpn.applocker.user;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.tanpn.applocker.R;
import com.tanpn.applocker.lockservice.AppLockService;
import com.tanpn.applocker.sqlite.SQLAppPassword;
import com.tanpn.applocker.sqlite.SQLGroupPermission;
import com.tanpn.applocker.utils.AppDetail;
import com.tanpn.applocker.utils.AppInstalled;
import com.tanpn.applocker.utils.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by phamt_000 on 12/1/16.
 */
public class ListAppViewAdapter extends BaseAdapter {

    private Context context;
    private LayoutInflater mInflater;
    private SQLGroupPermission sql;
    private String groupName;
    private SQLAppPassword sqlAppPassword;


    private List<AppDetail> listApp = new ArrayList<>();

    public ListAppViewAdapter(Context context, String groupname){

        this.context = context;
        groupName = groupname;
        sql = new SQLGroupPermission(context);
        sqlAppPassword = new SQLAppPassword(context);
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }



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
        ImageButton ibtRemove;
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

        holder.imAppIcon.setImageBitmap(utils.drawableToBitmap(listApp.get(i).getIcon()));
        holder.tvAppName.setText(listApp.get(i).getAppName());
        holder.tvPackage.setText(listApp.get(i).getAppPackage());
        holder.tvAppUsages.setVisibility(View.GONE);
        holder.ibtRemove.setVisibility(View.VISIBLE);
        holder.ibtRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                remove(listApp.get(i));
            }
        });

        return convertView;
    }

    public void remove(AppDetail app){
        // xoa trong listview
        listApp.remove(app);

        // xoa trong sql
        sql.removeAppNember(groupName, app.getAppPackage());

        // xoa trong AppsInstalled
        AppInstalled.getInstance(context).setAppChose(app.getAppPackage(), false);

        /**
         * local broadcast
         * */

        Intent intent = new Intent("CHANGE_GROUP");
        // You can also include some extra data.
        intent.putExtra("message", groupName + "|remove|" + app.getAppPackage());   //  <ten-nhom>|add|<app-package>
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

        // update password of app removed = ""
        sqlAppPassword.updatePassword(app.getAppPackage(), "");

        // khởi tạo lại service để update lại danh sách app bị khóa
        AppLockService.restart(context);

        notifyDataSetChanged();
    }

    public void add(AppDetail app){
        listApp.add(app);
        notifyDataSetChanged();
    }
}
