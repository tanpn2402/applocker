package com.tanpn.applocker.dialogs;


import android.app.AlertDialog;
import android.app.Dialog;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.tanpn.applocker.R;
import com.tanpn.applocker.utils.AppDetail;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChooseAppDialog extends DialogFragment {


    public ChooseAppDialog() {
        // Required empty public constructor
    }

    private ListView listApp;
    private Button btnCan, btnDone;
    private ChooseAppAdapter adapter;


    private void init(View v){
        listApp = (ListView) v.findViewById(R.id.listApp);
        btnCan = (Button) v.findViewById(R.id.btnCancel);
        btnCan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancel();
            }
        });

        btnDone = (Button) v.findViewById(R.id.btnDone);
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                done();
            }
        });

        adapter = new ChooseAppAdapter(getContext());
        listApp.setAdapter(adapter);


        listApp.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                AppDetail app = (AppDetail) adapter.getItem(i);
                adapter.check(app.getAppPackage());
                Log.i("ahihi", app.getAppPackage());
            }
        });

    }

    private void cancel(){
        dismiss();
    }

    public interface onAddAppListener{
        void onAddApp(List<String> apps);
    }
    private void done(){
        onAddAppListener onAddAppListener = (ChooseAppDialog.onAddAppListener) getActivity();
        onAddAppListener.onAddApp(adapter.getAppChoose());

        dismiss();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        View dialogView = getActivity().getLayoutInflater().inflate(R.layout.fragment_choose_app_dialog, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        init(dialogView);

        return builder.setView(dialogView).create();
    }


}
