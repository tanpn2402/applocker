package com.tanpn.applocker.user;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.tanpn.applocker.R;
import com.tanpn.applocker.applist.AppListAdapter;

/**
 * A simple {@link Fragment} subclass.
 */
public class AppUsageDetail extends Fragment /*implements AppUsageAdapter.OnEventListener*/{


    public AppUsageDetail() {
        // Required empty public constructor
    }

    private ListView listApp;
    private AppUsageAdapter adapter;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_app_usage_detail, container, false);

        listApp = (ListView) v.findViewById(R.id.listApp);
        adapter = new AppUsageAdapter(getContext());
        //adapter.setOnEventListener(this);
        listApp.setAdapter(adapter);

        return v;
    }

}
