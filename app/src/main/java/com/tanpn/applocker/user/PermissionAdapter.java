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
import com.tanpn.applocker.sqlite.SQLGroupPermission;
import com.tanpn.applocker.utils.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by phamt_000 on 11/29/16.
 */
public class PermissionAdapter extends BaseAdapter {

    private Context context;
    private Handler handler;
    private SQLGroupPermission sqlGroupPermission ;

    private List<GroupPermission> list;
    private LayoutInflater mInflater;

    public PermissionAdapter(Context context){
        this.context = context;
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        sqlGroupPermission = new SQLGroupPermission(context);
        handler = new Handler();
        list = new ArrayList<>();

        handler.post(new getGroup());
    }

    class getGroup implements Runnable{

        @Override
        public void run() {
            list = new ArrayList<>(sqlGroupPermission.getAllGroups());
            notifyDataSetChanged();
        }
    }


    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int i) {
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    private class ViewHolder{
        TextView tvGroupTitle;
        TextView tvCount;
    }
    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {

        final ViewHolder holder;

        if(convertView == null){
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.layout_group_item, null);
            holder.tvCount = (TextView) convertView.findViewById(R.id.tvNembers);
            holder.tvGroupTitle = (TextView) convertView.findViewById(R.id.tvGroupName);

            convertView.setTag(holder);
        }else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.tvCount.setText(list.get(i).getApps().size() + "" );
        holder.tvGroupTitle.setText(list.get(i).getTitle());

        return convertView;
    }

    public boolean contains(String name){
        for(GroupPermission g : list){
            if(g.getTitle().equals(name))
                return true;
        }

        return false;
    }

    public void add(GroupPermission g){
        list.add(g);
        sqlGroupPermission.insertGroup(g.getTitle(), g.getPassword());
    }

    public void removeAppInGroup(String groupname, String apppackage){
        for(GroupPermission g : list){
            if(g.getTitle().equals(groupname)){

                g.removeApp(apppackage);
                notifyDataSetChanged();
            }
        }
    }

    public void addAppIntoGroup(String groupname, String apppackage){
        for(GroupPermission g : list){
            if(g.getTitle().equals(groupname)){

                if(!g.getApps().contains(apppackage)){
                    g.addApp(apppackage);
                    notifyDataSetChanged();
                    return;
                }
            }
        }
    }

}
