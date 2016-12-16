package com.tanpn.applocker.user;

import android.content.Intent;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.tanpn.applocker.R;
import com.tanpn.applocker.dialogs.ChooseAppDialog;
import com.tanpn.applocker.lockservice.AppLockService;
import com.tanpn.applocker.sqlite.SQLAppPassword;
import com.tanpn.applocker.sqlite.SQLGroupPermission;
import com.tanpn.applocker.utils.AppInstalled;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ListAppView extends AppCompatActivity implements ChooseAppDialog.onAddAppListener {

    private TextView tvGroupname;
    private ImageButton ibtBack, ibtAdd;
    private ListView listApp;

    private String data;
    private List<String> group;

    private ListAppViewAdapter adapter;

    private SQLGroupPermission sql;
    private SQLAppPassword sqlAppPassword;


    private void init(){

        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            data = bundle.getString("data");
            group = new ArrayList<>(Arrays.asList(data.split("\\|")));

            sql = new SQLGroupPermission(this);
            sqlAppPassword = new SQLAppPassword(this);

            tvGroupname = (TextView) findViewById(R.id.tvGroupName);
            adapter = new ListAppViewAdapter(this, group.get(0));

            listApp = (ListView) findViewById(R.id.listApp);
            listApp.setAdapter(adapter);

            ibtAdd = (ImageButton) findViewById(R.id.ibtAddApp);
            ibtBack = (ImageButton) findViewById(R.id.ibtBack);

            tvGroupname.setText(group.get(0));

            // hien thi cac app co trong group nay

            String h = group.get(1).substring(1, group.get(1).length() - 1);
            if(!h.equals("")){
                List<String> listApp = new ArrayList<>(Arrays.asList(h.split("[\\s,]+")));

                for(String a : listApp){
                    adapter.add(AppInstalled.getInstance(this).getDetail(a));

                }
            }

            ibtAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // hien thi dialog chon app vao group
                    showChooseAppDialog();
                }
            });

            ibtBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onBackPressed();
                }
            });
        }
    }

    private void showChooseAppDialog() {
        ChooseAppDialog c = new ChooseAppDialog();
        c.show(getSupportFragmentManager(), "dialog");


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_app_view);

        init();
    }

    @Override
    public void onAddApp(List<String> apps) {
        if(apps.size() == 0)
            return;


        final String pass = sql.getPassword(group.get(0));

        for(String a : apps){ // tra ve danh sach app duoc chon

            // hien thi vao listApp
            adapter.add(AppInstalled.getInstance(this).getDetail(a));


            // chinh sua trong sqlite
            sql.addAppNember(group.get(0), a);


            // chinh sua trong AppInstalled
            AppInstalled.getInstance(this).setAppChose(a, true);

            //chinh sua password cua app nay
            sqlAppPassword.updatePassword(a, pass);

            /**
             * local broadcast
             * */


            // You can also include some extra data.
            Intent intent = new Intent("CHANGE_GROUP");
            intent.putExtra("message", group.get(0) +  "|add|" + a); //  <ten-nhom>|add|<app-package>
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }


        // khởi tạo lại service để update lại danh sách app bị khóa
        AppLockService.restart(this);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();

        finish(); // thoat ung dung luon
    }
}
