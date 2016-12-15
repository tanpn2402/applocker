package com.tanpn.applocker.dialogs;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.tanpn.applocker.R;
import com.tanpn.applocker.lockservice.LockPreferences;
import com.tanpn.applocker.lockservice.LockService;

/**
 * A simple {@link Fragment} subclass.
 */
public class AddGroup extends DialogFragment {


    public AddGroup() {
        // Required empty public constructor
    }


    private EditText edtGroupName;
    private Button btnAddGroup, btnChoosePass;


    private void init(View v){
        edtGroupName = (EditText) v.findViewById(R.id.edtGroupName);
        btnAddGroup = (Button) v.findViewById(R.id.btnNewGroup);
        btnChoosePass = (Button) v.findViewById(R.id.btnChoosePassword);
        btnChoosePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choosePassword();
            }
        });

        btnAddGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addGroup();
            }
        });
    }

    private void choosePassword() {
        LockService.showCreate(getContext(), LockPreferences.TYPE_PASSWORD, LockPreferences.CREATE_GROUP_LOCK);

        /**
         * more, see at: http://stackoverflow.com/questions/8802157/how-to-use-localbroadcastmanager?noredirect=1&lq=1
         *
         * */
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(changeGroup, new IntentFilter("SET_PASSWORD"));
    }

    private BroadcastReceiver changeGroup = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");
            onChange(message);
        }
    };

    private String password = "1";
    private void onChange(String m){
       password = m;
    }

    private void addGroup() {

        Intent i = new Intent();
        i.putExtra("data", edtGroupName.getText().toString() + "|" + password );
        getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, i);

        dismiss();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View dialogView = getActivity().getLayoutInflater().inflate(R.layout.fragment_add_group, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        init(dialogView);

        return builder.setView(dialogView).create();
    }



}
