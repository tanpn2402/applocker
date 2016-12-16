package com.tanpn.applocker.user;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tanpn.applocker.R;
import com.tanpn.applocker.sqlite.SQLAppUnLock;
import com.tanpn.applocker.utils.AppDetail;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by phamt_000 on 12/3/16.
 */
public class AppUnlockViewAdapter extends BaseAdapter {

    private Context context;
    private AppDetail app;
    private LayoutInflater mInflater;
    private List<info> list;


    public AppUnlockViewAdapter(Context context, AppDetail app){
        this.context = context;
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        this.app = app;

        list = new ArrayList<>();

        Handler handler = new Handler();
        handler.post(new getInfo());

    }

    class getInfo implements Runnable{

        @Override
        public void run() {
            for(int i = 0; i < app.getDatetime().size(); i++){
                info in = new info(app.getDatetime().get(i), null,null);

                if(app.getLocation().size() > i)
                    in.location = app.getLocation().get(i);

                if(app.getPhoto().size() > i)
                    in.photo = app.getPhoto().get(i);

                list.add(in);
                notifyDataSetChanged();
            }
        }
    }

    private class info {
        String date;
        String time;
        String location;
        String photo;

        public info(String s1, String s2, String s3){
            date = s1.split("\\|")[0];
            time = s1.split("\\|")[1];

            location = s2;
            photo = s3;

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

    class ViewHolder{
        TextView tvDate, tvTime, tvLocation;
        RelativeLayout layoutLocation, layoutPhoto;
        ImageView imPhoto;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {

        final ViewHolder holder;

        if(convertView == null){
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.layout_app_unlock_item, null);
            holder.tvDate = (TextView) convertView.findViewById(R.id.tvDate);
            holder.tvTime = (TextView) convertView.findViewById(R.id.tvTime);
            holder.tvLocation = (TextView) convertView.findViewById(R.id.tvLocation);
            holder.layoutLocation = (RelativeLayout) convertView.findViewById(R.id.layout_location);
            holder.layoutPhoto = (RelativeLayout) convertView.findViewById(R.id.layout_photo);
            holder.imPhoto = (ImageView) convertView.findViewById(R.id.imPhoto);

            convertView.setTag(holder);


        }else {
            holder = (ViewHolder) convertView.getTag();
        }



        holder.tvDate.setText(list.get(i).date);
        holder.tvTime.setText(list.get(i).time);

        if(list.get(i).location != null)
            holder.tvLocation.setText(list.get(i).location);
        else
            holder.layoutLocation.setVisibility(View.GONE);

        if(list.get(i).photo != null){
            File imgFile = new  File(list.get(i).photo);
            if(imgFile.exists())
            {
                holder.imPhoto.setImageURI(Uri.fromFile(imgFile));
            }
        }
        else{
            holder.layoutPhoto.setVisibility(View.GONE);
        }



        return convertView;
    }
}
