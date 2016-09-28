package com.tanpn.applocker.applist;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.tanpn.applocker.utils.*;
import com.tanpn.applocker.R;

/**
 * Created by tanpn on 9/28/16.
 */
public class AppListAdapter extends BaseAdapter {

    private Context context;
    private PackageManager packageManager;
    private Set<AppListElement> initItems;
    private List<AppListElement> listItems;

    public AppListAdapter(Context _context){
        this.context = _context;
        packageManager = context.getPackageManager();

        initItems = new HashSet<>();
        listItems = new ArrayList<>();

        new getApps().execute((Void[]) null);
        //getInstalledApps();
    }

    /*public AppListAdapter(Context _context, List<AppListElement> allApps){
        this.context = _context;
        this.listItems = new ArrayList<>(allApps);
    }*/



    class getApps extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            getInstalledApps();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            sort();
            if (mListener != null) {
                mLoadComplete = true;
                mListener.onLoadComplete();
            }
        }
    }


    private boolean mLoadComplete;

    public boolean isLoadComplete() {
        return mLoadComplete;
    }

    private OnEventListener mListener;

    public void setOnEventListener(OnEventListener listener) {
        mListener = listener;
    }

    public interface OnEventListener {
        public void onLoadComplete();

        public void onDirtyStateChanged(boolean dirty);
    }



    private void getInstalledApps(){
        // lay nhung app quan trong + app cua system
        getImportanceAndSystemApps(initItems);


        //
        initItems.add(new AppListElement("Normal App", AppListElement.PRIORITY_NORMAL_CATEGORY));


        // -------get normal apps
        // tao intent voi category la launcher: tuc la hien thi nhung app co the chay duoc
        final Intent i = new Intent(Intent.ACTION_MAIN);
        i.addCategory(Intent.CATEGORY_LAUNCHER);
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> appRunnable = pm.queryIntentActivities(i, 0);

        for (ResolveInfo app : appRunnable) {
            if (!context.getPackageName().equals(app.activityInfo.packageName)) {

                AppListElement element = new AppListElement(
                        app.loadLabel(pm).toString(), // lay label
                        app.loadIcon(pm),                          // icon
                        AppListElement.PRIORITY_NORMAL_APPS);       // dat priority = PRIORITY_NORMAL_APPS

                initItems.add(element);
            }
        }


        // copy construction
        listItems = new ArrayList<>(initItems);

    }

    private void getImportanceAndSystemApps(Collection<AppListElement> initItemList){
        //ArrayList<AppListElement> appElements = new ArrayList<>();

        final String installer = "com.android.packageinstaller";
        final String systemuiAppPackage = "com.android.systemui";
        final List<String> importantAppPackage = Arrays.asList(new String[] {"com.android.vending", "com.android.settings" });
        final List<String> systemAppPackage = Arrays.asList(new String[] { "com.android.dialer" });

        List<ApplicationInfo> listApps = packageManager.getInstalledApplications(0);
        boolean isSystemApp = false;            // kiem tra xem day co phai la system app hay khong
        boolean isImportantApp = false;         // kiem tra xem day co phai la importance app hay khong

        for(ApplicationInfo app : listApps){

            // neu la systemuiAppPacket --> day la system app
            if(systemuiAppPackage.equals(app.packageName)){
                initItemList.add(new AppListElement("System UI", app.loadIcon(packageManager), AppListElement.PRIORITY_SYSTEM_APPS));

                //dat gia tri isSystem = true
                isSystemApp = true;
            }
            // neu la bo cai dat installer --> day la importance app
            else if(installer.equals(app.packageName)){
                initItemList.add(new AppListElement("Installer", app.loadIcon(packageManager), AppListElement.PRIORITY_IMPORTANT_APPS));

                //dat gia tri isImportanceApp
                isImportantApp = true;
            }



            // dne luot kiem tra 2 cai list importantAppPackage va systemAppPackage
            if(systemAppPackage.contains(app.packageName)){
                // system App
                initItemList.add(new AppListElement(
                        app.loadLabel(packageManager).toString(),
                        app.loadIcon(packageManager),
                        AppListElement.PRIORITY_SYSTEM_APPS));

                isSystemApp = true;
            }

            if(importantAppPackage.contains(app.packageName)){
                // importance app
                initItemList.add(new AppListElement(
                        app.loadLabel(packageManager).toString(),
                        app.loadIcon(packageManager),
                        AppListElement.PRIORITY_IMPORTANT_APPS
                ));

                isImportantApp = true;
            }




            // ket thuc for loop
        }

        // them cac separator
        /*if(isSystemApp){
            initItemList.add(new AppListElement("System Apps", AppListElement.PRIORITY_SYSTEM_CATEGORY));
        }

        if(isImportantApp){
            initItemList.add(new AppListElement("Importance App", AppListElement.PRIORITY_IMPORTANT_CATEGORY));
        }*/

        initItemList.add(new AppListElement("System Apps", AppListElement.PRIORITY_SYSTEM_CATEGORY));
        initItemList.add(new AppListElement("Importance App", AppListElement.PRIORITY_IMPORTANT_CATEGORY));




    }


    @Override
    public int getCount() {
        return listItems.size();
    }

    @Override
    public Object getItem(int i) {
        return listItems.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        if(listItems.get(i).isApp()){
            return createAppView(i,view,viewGroup);
        }
        else
            return createSeparatorView(i, view, viewGroup);
    }


    // ham tao cac separator
    private View createSeparatorView(int i, View view, ViewGroup viewGroup) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v =
                inflater.inflate(R.layout.layout_applistelement_separator, viewGroup, false);


        TextView tvSeparator = (TextView) v.findViewById(R.id.tvSeparator);
        tvSeparator.setText(listItems.get(i).getLabel());

        return v;
    }

    // ham to app row
    private View createAppView(int i, View view, ViewGroup viewGroup) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.layout_applistelement_app, viewGroup, false);


        TextView tvLabel = (TextView) v.findViewById(R.id.tvLabel);
        tvLabel.setText(listItems.get(i).getLabel());

        ImageView imIcon = (ImageView) v.findViewById(R.id.imIcon);
        imIcon.setImageBitmap(utils.drawableToBitmap(listItems.get(i).getIcon()));

        return v;
    }

    public void sort() {
        Collections.sort(listItems);
        notifyDataSetChanged();
        notifyDirtyStateChanged(false);
    }

    private boolean mDirtyState;

    private void notifyDirtyStateChanged(boolean dirty) {
        if (mDirtyState != dirty) {
            mDirtyState = dirty;
            if (mListener != null) {
                mListener.onDirtyStateChanged(dirty);
            }
        }
    }



}
