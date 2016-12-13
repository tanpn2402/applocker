package com.tanpn.applocker.user;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.tanpn.applocker.MainActivity;
import com.tanpn.applocker.R;
import com.tanpn.applocker.fragments.AppsFragment;

public class Dashboard extends AppCompatActivity {


    private TabLayout tabLayout;
    private ViewPager viewPager;
    private ViewPagesAdapter viewPagesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);




        tabLayout = (TabLayout) findViewById(R.id.tabLayout);

        viewPager = (ViewPager) findViewById(R.id.viewPaper);


        viewPagesAdapter = new ViewPagesAdapter(getSupportFragmentManager());
        viewPagesAdapter.addFragments(new AppUsageDetail(), "Lịch sử");
        viewPagesAdapter.addFragments(new Permission(), "Phân quyền");
        viewPagesAdapter.addFragments(new AppUnlockDetail(), "Chi tiết");
        viewPagesAdapter.addFragments(new Sync(), "Đồng bộ");

        //tabLayout.getTabAt(0).setIcon(R.drawable.ic_event_pink);
        //tabLayout.getTabAt(1).setIcon(R.drawable.ic_picture_gray);
        //tabLayout.getTabAt(2).setIcon(R.drawable.ic_message_gray);


        viewPager.setAdapter(viewPagesAdapter);

        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setTabTextColors(Color.BLACK, Color.WHITE);

    }

    @Override
    public void onBackPressed() {
        // quay ve Main Activity
        //Intent in = new Intent(this , MainActivity.class);
        //startActivity(in);

        //finish();
    }

    @Override
    protected void onPause() {
        super.onPause();

        finish();
    }
}
