package com.tanpn.applocker.user;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.tanpn.applocker.R;
import com.tanpn.applocker.utils.AppDetail;

/**
 * A simple {@link Fragment} subclass.
 */
public class AppUnlockDetail extends Fragment {


    public AppUnlockDetail() {
        // Required empty public constructor
    }

    private ListView listApp;
    private AppUnLockAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_app_usage_detail, container, false);

        listApp = (ListView) v.findViewById(R.id.listApp);
        adapter = new AppUnLockAdapter(getContext());
        listApp.setAdapter(adapter);

        listApp.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                AppDetail app = (AppDetail) adapter.getItem(i);
                viewDetail(app);
            }
        });

        return v;
    }

    private void viewDetail(AppDetail app) {
        Intent in = new Intent(getContext(), AppUnLockView.class);
        in.putExtra("data", app.getAppPackage());
        startActivity(in);
    }

}
