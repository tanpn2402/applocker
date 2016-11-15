package com.tanpn.applocker.user;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.tanpn.applocker.R;

public class Dashboard extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        //
        getSupportFragmentManager().beginTransaction().replace(R.id.dashboard, new LoginFragment()).commit();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.dashboard_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.dashboard_backup:
                getSupportFragmentManager().beginTransaction().replace(R.id.dashboard, new LoginFragment()).commit();
                return true;
            case R.id.dashboard_detail:
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.dashboard, new LoginFragment()).commit();

                return true;
            case R.id.dashboard_permission:
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.dashboard, new CreateAccountFragment()).commit();

                return true;
            case R.id.dashboard_setting:
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.dashboard, new LoginFragment()).commit();

                return true;
        }
        return true;
    }
}
