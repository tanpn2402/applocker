package com.tanpn.applocker.capture;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.tanpn.applocker.MainActivity;
import com.tanpn.applocker.R;
import com.tanpn.applocker.lockservice.AppLockService;
import com.tanpn.applocker.lockservice.LockService;
import com.tanpn.applocker.utils.PreUtils;

public class HeadActivity extends AppCompatActivity {
    private static final String EXTRA_UNLOCKED = "com.twinone.locker.unlocked";
    private PreUtils preUtils;

    private Handler handler = new Handler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_head);
        preUtils = new PreUtils(this);


        toggleService();
        showLockerIfNotUnlocked(false);



        LocalBroadcastManager.getInstance(this).registerReceiver(onUnlock, new IntentFilter("LOCK_NOTIFY"));
    }

    private BroadcastReceiver onUnlock = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");
            onChange(message);
        }
    };

    private void onChange(String m){
        if(m.split("\\|")[0].equals("true")){
            // mo khoa thanh cong
            Intent in = (new Intent(this, MainActivity.class));
            in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            in.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            overridePendingTransition(0, 0);
            startActivity(in);
            finish();
            handler.postDelayed(new hideLock(this), 700);
        }
        else{
            // mo khoa k thanh cong, tien hanh chup hinh phạm nhân
            Intent in = (new Intent(this, Capture.class));
            in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            in.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            in.putExtra("package", m.split("\\|")[1]);
            overridePendingTransition(0, 0);
            startActivity(in);
            finish();

            handler.postDelayed(new hideLock(this), 500);
        }
    }

    /**
     * hacking
     * */
    class hideLock implements Runnable{
        Context co;
        public hideLock (Context c){
            co = c;
        }
        @Override
        public void run() {
            LockService.hide(co);
        }
    }


    protected void onPause() {
        super.onPause();
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();

        showLockerIfNotUnlocked(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    private void showLockerIfNotUnlocked(boolean relock) {
        LockService.showCompare(this, getPackageName());
    }

    private void toggleService(){

        try{
            if(preUtils.getBoolean(R.string.pref_key_lock_enable, true)){
                if(AppLockService.isRunning(this))
                    AppLockService.restart(this);       // neu dang chay thi restart
                else
                    AppLockService.toggle(this);        // neu k thi khoi dong lai service
            }
            else
                AppLockService.stopAppLockService(this);
        }catch (NullPointerException ex){}

        if(AppLockService.isRunning(this)){
            // service is running
        }
        else{
            // service has been stopped
            AppLockService.toggle(this);
        }
    }
}
