package com.tanpn.applocker.dialogs;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.tanpn.applocker.R;
import com.tanpn.applocker.utils.AppDetail;
import com.tanpn.applocker.utils.AppInstalled;
import com.tanpn.applocker.utils.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by phamt_000 on 12/1/16.
 */
public class ChooseAppAdapter  extends BaseAdapter {

    private Context context;
    private LayoutInflater mInflater;

    private List<AppDetail> listAppToShow;
    private List<String> listUnAailableApps; // ds package chua co nhom nao == nhung app ci the chon duoc

    private List<AppDetail> allApps;
    /**
     * bản chấ allApps là đã không chứa những app có nhóm, nhưng trong 1 số trường hợp thig bugs xuất hiện,
     * đó là vẫn xuất hiện 1 số app có nhóm rồi
     *
     * lọc lại thêm 1 lần nữa
     * listUnAailableApps là những app đã có nhóm
     * */

    private Handler handler;

    private Map<String, Boolean> listAppChose;  // danh sach app duoc chon

    public ChooseAppAdapter(Context context){
        this.context = context;
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        listAppToShow = new ArrayList<>();
        allApps = new ArrayList<>();
        listAppChose = new HashMap<>();

        handler = new Handler();
        handler.post(new getUnChooseApp());


        listUnAailableApps = AppInstalled.getInstance(context).getAppChose();
        allApps = AppInstalled.getInstance(context).getAllAppsInstalled();
    }

    class getUnChooseApp implements Runnable{

        @Override
        public void run() {
            for(AppDetail app : allApps){
                if(!listUnAailableApps.contains(app.getAppPackage())){
                    listAppToShow.add(app);
                    listAppChose.put(app.getAppPackage(), false);
                    notifyDataSetChanged();
                }
                /*if(!app.isChose()){
                    listAppToShow.add(app);
                    listAppChose.put(app.getAppPackage(), false);
                    notifyDataSetChanged();
                }*/
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
        CheckBox cbChoose;
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
            holder.cbChoose = (CheckBox) convertView.findViewById(R.id.cbChoose);

            convertView.setTag(holder);

            /*holder.cbChoose.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    listAppChose.put(listAppToShow.get(i).getAppPackage(), b);
                    Log.i("ahihi", listAppToShow.get(i).getAppPackage() + "  -->  "  + String.valueOf(b));

                }
            });*/

        }else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.imAppIcon.setImageBitmap(utils.drawableToBitmap(listAppToShow.get(i).getIcon()));
        holder.tvAppName.setText(listAppToShow.get(i).getAppName());
        holder.tvPackage.setText(listAppToShow.get(i).getAppPackage());
        holder.tvAppUsages.setVisibility(View.GONE);
        holder.ibtRemove.setVisibility(View.GONE);
        holder.cbChoose.setVisibility(View.VISIBLE);
        holder.cbChoose.setChecked(listAppChose.get(listAppToShow.get(i).getAppPackage()));


        return convertView;
    }

    public void check(String app_package){
        listAppChose.put(app_package, listAppChose.get(app_package) == true ? false : true );
        notifyDataSetChanged();
    }

    public List<String> getAppChoose(){
        List<String> list = new ArrayList<>();
        for(Map.Entry<String, Boolean> m : listAppChose.entrySet()){
            if(m.getValue() == true){
                list.add(m.getKey());
            }
        }

        return list;
    }

}
