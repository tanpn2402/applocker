package com.tanpn.applocker.user;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.tanpn.applocker.R;
import com.tanpn.applocker.dialogs.AddGroup;
import com.tanpn.applocker.dialogs.ViewListApp;

/**
 * A simple {@link Fragment} subclass.
 */
public class Permission extends Fragment  {


    public Permission() {
        // Required empty public constructor
    }

    private FloatingActionButton fabAddGroup;
    private ListView listGroup;
    private PermissionAdapter adapter ;

    private AddGroup addGroupDialog;

    private void init(View v){
        addGroupDialog = new AddGroup();
        fabAddGroup = (FloatingActionButton) v.findViewById(R.id.fabAddGroup);

        fabAddGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addNewGroup();
            }
        });


        listGroup = (ListView) v.findViewById(R.id.listGroup);
        adapter = new PermissionAdapter(getContext());
        listGroup.setAdapter(adapter);

        listGroup.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                GroupPermission g = (GroupPermission)adapter.getItem(i);
                //ViewListApp v = new ViewListApp();
                //v.setGroup(g);
                //v.show(getActivity().getSupportFragmentManager(), "dialog");

                changeLayout(g);
            }
        });

        /**
         * more, see at: http://stackoverflow.com/questions/8802157/how-to-use-localbroadcastmanager?noredirect=1&lq=1
         *
         * */
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(changeGroup, new IntentFilter("CHANGE_GROUP"));

    }

    private BroadcastReceiver changeGroup = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");
            onChange(message);
        }
    };

    private void onChange(String m){
        String[] mes = m.split("\\|");
        if(mes[1].equals("add")){
            adapter.addAppIntoGroup(mes[0], mes[2]);
        }
        else{
            adapter.removeAppInGroup(mes[0], mes[2]);
        }
    }


    private void changeLayout(GroupPermission g){
        String data = g.getTitle() + "|" + g.getApps().toString();

        Intent in = new Intent(getContext(), ListAppView.class);
        in.putExtra("data", data);
        startActivity(in);
    }

    private final int ADD_GROUP_CODE = 2;
    private void addNewGroup() {
        addGroupDialog.show(getActivity().getSupportFragmentManager(), "dialog");
        addGroupDialog.setTargetFragment(this, ADD_GROUP_CODE);
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_permission, container, false);
        init(v);



        return v;
    }





    public AlertDialog.Builder showAlertDialog(String mes){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        //builder.setTitle("");
        builder.setMessage(mes);
        builder.setPositiveButton("Đóng", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });

        return builder;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode != Activity.RESULT_OK)
            return;

        if(requestCode == ADD_GROUP_CODE){
            onAddGroup(data.getStringExtra("data"));
        }
    }

    public void onAddGroup(String group) {

        String groupname = group.split("\\|")[0];
        String pass = group.split("\\|")[1];

        if(groupname.equals("")){
            showAlertDialog("Tên nhóm không được để trống").create().show();

            return;
        }

        if(adapter.contains(groupname)){
            showAlertDialog("Nhóm này đã được tạo trước đó rồi").create().show();

            return;
        }

        if(pass.length() < 4){
            showAlertDialog("Mật khẩu phải lớn hơn 4 kí tự").create().show();

            return;
        }

        GroupPermission g = new GroupPermission(groupname, pass, 0, "");
        adapter.add(g);
        adapter.notifyDataSetChanged();


    }
}
