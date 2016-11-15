package com.tanpn.applocker;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.tanpn.applocker.fragments.AppsFragment;
import com.tanpn.applocker.fragments.SettingsFragment;
import com.tanpn.applocker.lockservice.AppLockService;
import com.tanpn.applocker.lockservice.LockPreferences;
import com.tanpn.applocker.lockservice.LockService;
import com.tanpn.applocker.utils.PreUtils;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    // tieu de
    private CharSequence mTitle;

    private Fragment currentFragment;
    private int currentFragmentID;

    final String TITLE_ALL_APP = "Tất cả Ứng dụng";
    final String TITLE_SETTING = "Cài đặt";

    private static final String EXTRA_UNLOCKED = "com.twinone.locker.unlocked";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);



        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // get title
        mTitle = getTitle();

        // chuyen den fragment all apps
        currentFragment = new AppsFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_replace, currentFragment).commit();
        currentFragmentID = R.id.nav_all_apps;

        // set title
        setTitle(TITLE_ALL_APP);

        // start service
        toggleService();


        showLockerIfNotUnlocked(false);

        showDialogChoosePasswordType();
    }

    private boolean checkEmptyPassword(){
        // kiem tra xem day co phai la lan dau su dung hay khng
        // hoac kiem tra xem mat khau co null hay khong

        PreUtils preUtils = new PreUtils(this);
        boolean b = preUtils.isCurrentPasswordEmpty();
        Log.i("TAG", b ?  "true" : "false");



        return preUtils.isCurrentPasswordEmpty();
    }

    private void showDialogChoosePasswordType(){
        if (!checkEmptyPassword())
            return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Choose password type");
        builder.setMessage("To start using App Lock, please choose and set your passwor");

        builder.setPositiveButton("Pattern", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Log.i("TAG", "choose pattern");
                createNewLock(LockPreferences.TYPE_PATTERN);

            }
        });

        builder.setNegativeButton("Password", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Log.i("TAG", "Choose password");
                createNewLock(LockPreferences.TYPE_PASSWORD);

            }
        });

        builder.create();
        builder.show();
    }

    private void createNewLock(int type){
        LockService.showCreate(this, type);
    }

    @Override
    public void setTitle(CharSequence title) {
        super.setTitle(title);

        mTitle = title;
        getSupportActionBar().setTitle(mTitle);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    final String tag = "tag";

    @Override
    protected void onPause() {
        super.onPause();

        Log.i(tag, "pause");
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();

        showLockerIfNotUnlocked(true);
        //Log.i(tag, "resume");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //Log.i(tag, "destroy");
    }


    private void toggleService(){
        Log.i(tag, "toggle service");

        if(AppLockService.isRunning(this)){
            // service is running
        }
        else{
            // service has been stopped
            AppLockService.toggle(this);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.global, menu);
        return true;
    }

    /*@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }*/

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_all_apps && currentFragmentID != R.id.nav_all_apps) {
            // Handle the camera action
            currentFragment = new AppsFragment();
            getSupportFragmentManager().beginTransaction().replace(
                    R.id.fragment_replace,
                    currentFragment
            ).commit();

            // set title
            setTitle(TITLE_ALL_APP);

            // set currentFragment ID
            currentFragmentID = R.id.nav_all_apps;
        } else if (id == R.id.nav_settings && currentFragmentID != R.id.nav_settings) {
            currentFragment = new SettingsFragment();
            getSupportFragmentManager().beginTransaction().replace(
                    R.id.fragment_replace,
                    currentFragment
            ).commit();

            // set title
            setTitle(TITLE_SETTING);

            // set currentFragment ID
            currentFragmentID = R.id.nav_settings;
        } else if (id == R.id.nav_contact) {
            onContactButtonSelected();
        } else if (id == R.id.nav_share) {
            onShareButtonSelected();
        } else if (id == R.id.nav_rate) {
            onRateButtonSelected();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
    * Mo Google Play va cho 5*
    *
    * */
    private void onRateButtonSelected(){
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("market://details?id=" + getPackageName()));
        if (getPackageManager().queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY).size() >= 1) {
            startActivity(intent);
        }
    }

    /**
     * Share thong qua facebook.
    **/
    private TextView sTitle;
    private TextView sMessage;
    private void onShareButtonSelected(){
        /// tao dialog
        AlertDialog.Builder shareDialog = new AlertDialog.Builder(this);

        View shareView = getLayoutInflater().inflate(R.layout.layout_share_dialog, null);
        shareDialog.setView(shareView);
        shareDialog.create();
        shareDialog.show();

        // lay cac control trong shareView
        sTitle = (TextView) shareView.findViewById(R.id.tvDialogTitle);
        sMessage = (TextView) shareView.findViewById(R.id.tvDialogMessage);
        Button sLeftButton = (Button) shareView.findViewById(R.id.btnDialogLeft);
        Button sRightButton = (Button) shareView.findViewById(R.id.btnDialogRight);

        sLeftButton.setOnClickListener(shareDialogButtonOnClick);
        sRightButton.setOnClickListener(shareDialogButtonOnClick);

    }
    private View.OnClickListener shareDialogButtonOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(view.getId() == R.id.btnDialogLeft){
                // dismiss
                onBackPressed();
            }
            else if(view.getId() == R.id.btnDialogRight){
                // share
                String shareBody = sMessage.getText().toString();
                String shareSubject = sTitle.getText().toString();
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, shareSubject);
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(sharingIntent, getString(R.string.main_share_by_using)));
            }
        }
    };

    /**
    * Mo ung dung MAIL va soan tin nhan den dia chi mail: abc.....
    * */
    private void onContactButtonSelected(){
        try
        {
            Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
            emailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            emailIntent.setType("message/rfc822");
            emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[] {getString(R.string.main_contact_email)});
            emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.main_contact_subject));
            emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, getString(R.string.main_contact_text));
            startActivity(Intent.createChooser(emailIntent, getString(R.string.main_contact_by_using)));
        }
        catch(Exception ex)
        {
            Toast.makeText(this, "Some problems while open email app", Toast.LENGTH_SHORT).show();
        }
    }


    private void showLockerIfNotUnlocked(boolean relock) {
        boolean unlocked = getIntent().getBooleanExtra(EXTRA_UNLOCKED, false);
        if (new PreUtils(this).isCurrentPasswordEmpty()) {
            unlocked = true;
        }
        if (!unlocked) {
            LockService.showCompare(this, getPackageName());
        }
        getIntent().putExtra(EXTRA_UNLOCKED, !relock);
    }
}
