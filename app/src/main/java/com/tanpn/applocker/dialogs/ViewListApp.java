package com.tanpn.applocker.dialogs;


import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.tanpn.applocker.R;
import com.tanpn.applocker.user.GroupPermission;

/**
 * A simple {@link Fragment} subclass.
 */
public class ViewListApp extends DialogFragment {


    public ViewListApp() {
        // Required empty public constructor
    }

    private ListView listApp;
    private CustomAdapter adapter;
    private GroupPermission group;

    public void setGroup(GroupPermission g){
        group = g;
    }

    private void init(View v){
        listApp = (ListView) v.findViewById(R.id.listApp);
        adapter = new CustomAdapter(getContext(), group.getApps());
        listApp.setAdapter(adapter);
    }



    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View dialogView = getActivity().getLayoutInflater().inflate(R.layout.fragment_view_list_app, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        init(dialogView);

        return builder.setView(dialogView).create();
    }

}
