package com.tanpn.applocker.user;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;

import com.tanpn.applocker.R;
import com.tanpn.applocker.sqlite.SQLAppUnLock;
import com.tanpn.applocker.utils.AppDetail;

public class AppUnLockView extends AppCompatActivity {


    ListView listView;
    AppUnlockViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_unlock_view);

        Bundle bundle = getIntent().getExtras();
        if(bundle == null)
            return;

        final String app_package = bundle.getString("data");

        AppDetail app = new SQLAppUnLock(this).getOne(app_package);

        listView = (ListView) findViewById(R.id.listView);
        adapter = new AppUnlockViewAdapter(this, app);
        listView.setAdapter(adapter);
    }
}
