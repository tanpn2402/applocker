package com.tanpn.applocker.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.tanpn.applocker.R;
import com.tanpn.applocker.applist.AppListAdapter;
import com.tanpn.applocker.applist.AppListElement;

/**
 * A simple {@link Fragment} subclass.
 */
public class AppsFragment extends Fragment implements AppListAdapter.OnEventListener, AdapterView.OnItemClickListener {


    // khai bao listview chua app list elements
    private ListView lvAppListElement;

    private AppListAdapter adapter;

    public AppsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =inflater.inflate(R.layout.fragment_apps, container, false);

        lvAppListElement = (ListView) v.findViewById(R.id.lvAppListElement);

        // applistADAPTER
        adapter = new AppListAdapter(getContext());
        adapter.setOnEventListener(this);

        lvAppListElement.setAdapter(adapter);
        lvAppListElement.setOnItemClickListener(this);

        // co menu layout o tren thanh action bar
        setHasOptionsMenu(true);

        return v;
    }

    private Menu mMenu;  // cac menu o tren action bar

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.main_menu, menu);
        mMenu = menu;

        updateMenuLayout();
    }

    private void updateMenuLayout(){
        boolean all = adapter.areAllAppsLocked();
        if (mMenu != null && adapter.isLoadComplete()) {
            mMenu.findItem(R.id.main_menu_lock_all).setVisible(!all);
            mMenu.findItem(R.id.main_menu_unlock_all).setVisible(all);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemID = item.getItemId();
        switch (itemID){
            case R.id.main_menu_lock_all:
                setLockAll(true);
                return true;
            case R.id.main_menu_unlock_all:
                setLockAll(false);
                return true;
            case R.id.menu_main_sort:

                return true;
            case R.id.main_menu_undo:

                return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onLoadComplete() {

    }

    @Override
    public void onDirtyStateChanged(boolean dirty) {

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        AppListElement item = (AppListElement) adapterView.getItemAtPosition(i);
        if(item.isApp()){

            adapter.toggle(item);
            view.findViewById(R.id.imApplist_item_lock).setVisibility( item.locked ? View.VISIBLE : View.GONE);
            //item.locked = !item.locked;
            onDirtyStateChanged(true);
            //adapter.notifyDirtyStateChanged(true);
        }
    }

    // menu khoa tat ca lockall
    private void setLockAll(boolean lockAll){
        adapter.setAllLocked(lockAll);

        updateMenuLayout();
    }

}
