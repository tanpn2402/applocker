package com.tanpn.applocker.fragments;


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
public class AppsFragment extends Fragment {


    // khai bao listview chua app list elements
    private ListView lvAppListElement;

    public AppsFragment() {
        // Required empty public constructor
    }


    @Override
                             public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =inflater.inflate(R.layout.fragment_apps, container, false);

        lvAppListElement = (ListView) v.findViewById(R.id.lvAppListElement);

        // applistADAPTER
        AppListAdapter adapter = new AppListAdapter(getContext());

        lvAppListElement.setAdapter(adapter);

        return v;
    }

}
