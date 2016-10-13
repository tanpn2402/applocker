package com.tanpn.applocker.lockservice;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import com.tanpn.applocker.lockscreen.PasswordView;
import com.tanpn.applocker.utils.PreUtils;
import com.tanpn.applocker.R;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AppLockService extends Service {

    /**
     * duoc dung de stop service
     */
    private static final String ACTION_STOP = "com.tanpn.applocker.intent.action.stop_lock_service";
    /**
     * duoc dung de start alarm
     */
    private static final String ACTION_START = "com.tanpn.applocker.intent.action.start_lock_service";
    /**
     * Khi xac dinh action nay, service se khoi tao lai tat ca doi tuong
     * Nô chi co hieu luc khi service duoc start 1 cach tuong minh
     */
    private static final String ACTION_RESTART = "com.tanpn.applocker.intent.action.restart_lock_service";
    private static final String EXTRA_FORCE_RESTART = "com.tanpn.applocker.intent.extra.force_restart";
    private static final int REQUEST_CODE = 0x1234AF;

    private BroadcastReceiver screenReceiver;
    private Map<String, Boolean> lockedPackages;
    private ActivityManager mActivityManager;
    private final String TAG = "tag";

    private final String TAG_APP = "AppLockService";

    private boolean relockScreenOff;    // duoc lay tu Preferences


    public AppLockService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }


    // bat su kien tat/mp man hinh
    private final class ScreenReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                // khi man hinh mo: thi khong co gi xay ra
                Log.i(TAG, "Screen ON");
                // Trigger package again
                //mLastPackageName = "";
                //startAlarm(AppLockService.this);
            }
            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                //khi man hinh tat thi khoa tat ca cac ung dung (neu co setting)
                Log.i(TAG, "Screen OFF");
                //stopAlarm(AppLockService.this);
                /*if (relockScreenOff) {
                    lockAll();
                }*/
            }
        }
    }


    // ham khoi tao cac doi tuong
    private boolean init(){
        Log.d(TAG, "init");
        /*if (new PreUtils(this).isCurrentPasswordEmpty()) {
            Log.w(TAG, "Not starting service, current password empty");
            return false;
        }
        if (new VersionManager(this).isDeprecated()) {
            Log.i(TAG, "Not starting AlarmService for deprecated version");
            new VersionUtils(this).showDeprecatedNotification();
            return false;
        }*/

        mActivityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);

        lockedPackages = new HashMap<>();
        screenReceiver = new ScreenReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(screenReceiver, filter);

        // lay cac app bi khoa
        final Set<String> apps = PreUtils.getLockedApps(this);
        for (String s : apps) {
            lockedPackages.put(s, true);
        }


        PreUtils prefs = new PreUtils(this);    // tao doi tuong preference utils
        // lay phan setting: khoa khi tat man hinh
        /*relockScreenOff = prefs.getBoolean(
                R.string.pref_key_relock_after_screenoff,
                R.bool.pref_def_relock_after_screenoff);*/

        return true;
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate");

        super.onCreate();
        //init();
    }



    public int findPIDbyPackageName(String packagename) {
        int result = -1;

        if (mActivityManager != null) {
            for (ActivityManager.RunningAppProcessInfo pi : mActivityManager.getRunningAppProcesses()){
                if (pi.processName.equalsIgnoreCase(packagename)) {
                    result = pi.pid;
                    Log.i(TAG, "PID = " + result);
                }
                if (result != -1)
                    break;
            }
        } else {
            result = -1;
        }

        return result;
    }

    public boolean killProcessByID(String packagename) {
        boolean isKill = true;
        if(findPIDbyPackageName(packagename) != -1){
            // neu con ton tai process
            Log.i(TAG, "Process is running");
            try{
                android.os.Process.killProcess(findPIDbyPackageName(packagename));
            }
            catch (Exception ex){
                isKill = false;
            }
        }

        return isKill;
    }

    public boolean killPackageProcesses(String packagename) {
        boolean result = false;

        if (mActivityManager != null) {
            // kill process by Activity Manager
            mActivityManager.killBackgroundProcesses(packagename);

            // kiem tra xem process co duoc kill hay chua, neu chua thi kill by android.os
            result = killProcessByID(packagename);
        }

        return result;
    }

    private String getPackageStarted() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return mActivityManager.getRunningTasks(1).get(0).topActivity.getPackageName();
        } else {
            // Hack, see
            // http://stackoverflow.com/questions/24625936/getrunningtasks-doesnt-work-in-android-l/27140347#27140347
            final List<ActivityManager.RunningAppProcessInfo> pis = mActivityManager.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo pi : pis) {
                if (pi.pkgList.length == 1)
                    return pi.pkgList[0];
            }
        }
        return "";
    }

    private String lastPackageName;
    // ten package duoc mo cuoi cung, duoc dung de so sanh voi 1 app duoc mo

    // kiem tra xem co su thay doi trong mo app hay khong
    private void checkPackageChanged(){
        // lay package duoc mo
        final String packageName = getPackageStarted();

        if(!packageName.equals(lastPackageName)){
            // app duoc mo khac voi app duoc mo truoc do

            Log.i(TAG, "app changed from " + lastPackageName + " to " + packageName);

            onAppClose(lastPackageName, packageName);
            onAppOpen(packageName, lastPackageName);

        }
        // thay đổi mLastCompleteName để cho cuộc gọi hàm tiếp theo
        lastPackageName = packageName;
    }

    // onAppOpen
    private void onAppOpen(final String openPackage, final String closePackage){
        // kiem tra xem app duoc mo co nam trong danh sach khoa hay khong
        if(lockedPackages.containsKey(openPackage)){
            // co nam trong lockedPackages
            onLockAppOpen(openPackage);
        }
    }

    // onLockAppOpen
    private void onLockAppOpen(final String openPackage){
        // kiem tra xem app nay co duoc khoa hay khong
        // onAppOpen chi kiem tra app duoc mo co nam trong danh sach khoa hay khong thoi
        //      chu khong kiem tra co khoa hay khong
        final boolean locked = lockedPackages.get(openPackage);

        if(locked){
            Log.i(TAG, openPackage + " locked");

            showLockScreen();
        }
    }

    private void showLockScreen(){
        // hien thi man hinh khoa
            Log.i(TAG, "Show Lock Screennnnnn");
    }

    private void onAppClose(final String closePackage, final String openPackage){
        // kiem tra xem app duoc close co nam trong danh sach khoa hay khong
        if(lockedPackages.containsKey(closePackage)){
            // co
            onLockAppClose(closePackage, openPackage);
        }
    }

    private void onLockAppClose(final String closePackage, final String openPackage){
        if(getPackageName().equals(closePackage) || getPackageName().equals(openPackage)){
            // khi ma app duoc close == ten app cua minh
            // hoac app duoc open == ten app cua minh
            // thi return
            return;
        }

        if(lockedPackages.containsKey(openPackage)){
            //

            return;
        }

        /// hide lock screen
        //....
    }

    private boolean explicitStarted;
    // kiem tra xem service duoc start 1 cach tuong minh hay khong
    // start 1 cach tuong minh co nghia la:...

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Log.i(TAG, "on Start command");
        if(intent == null || ACTION_START.equals(intent.getAction())){
            // service duoc start
            if(!explicitStarted){
                Log.i(TAG, "khong duoc start 1 cach tuong minh");

                if(!init()){
                    // khong khoi tao duoc cac doi tuong
                    doStopService();
                    // dung service
                    return START_NOT_STICKY;
                }
                explicitStarted = true;
            }
            checkPackageChanged();
        }
        else if(ACTION_RESTART.equals(intent.getAction())){
            // service duoc restart
            if(explicitStarted || intent.getBooleanExtra(EXTRA_FORCE_RESTART, false)){
                // duoc start 1 cach tuong minh
                // hoac duoc bat buoc restart
                Log.i(TAG, "service restart");

                doRestartService();
            }
            else{
                doStopService();
            }
        }
        else{
            Log.i(TAG, "stop service");
            doStopService();
        }


        return START_STICKY;
    }

    private boolean allowDestroy;
    private boolean allowRestart;






    //// khi man hinh off thi khoa tat ca cac app
    // vi dieu kien la trong Setting co cai dat phan Khoa khi man hinh tat
    private void lockAll(){
        for (Map.Entry<String, Boolean> entry : lockedPackages.entrySet()) {
            entry.setValue(true);
        }
    }

    // duoc dung de bat/tat service
    public static boolean toggle(Context c){
        if(isRunning(c)){
            Log.i("tag", "toggle - stop");
            stopAppLockService(c);
            return false;
        }
        else{
            Log.i("tag", "toggle - start");
            startAppLockService(c);
            return true;
        }

    }

    // kiem tra xem service co dang chay hay khong
    public static boolean isRunning(Context c){
        ActivityManager man = (ActivityManager) c.getSystemService(Context.ACTIVITY_SERVICE);
        /// man giu cac service dang chay tren device
        for(ActivityManager.RunningServiceInfo service : man.getRunningServices(Integer.MAX_VALUE)){
            // duyet trong man xem coi co service nao co ten == ten service minh tao ra hay khong (AppLockService)
            if(AppLockService.class.getName().equals(service.service.getClassName())){
                return true;
            }
        }
        return false;
    }

    public static void stopAppLockService(Context c){
        stopAlarm(c);

        // tao 1 intent de chay service nay voi action = stop
        Intent i = new Intent(c, AppLockService.class);
        i.setAction(ACTION_STOP);
        c.startService(i);

    }

    public static void startAppLockService(Context c){
        startAlarm(c);
    }




    private static PendingIntent running_intent;
    private static PendingIntent getRunIntent(Context c){
        if(running_intent == null){
            Intent i = new Intent(c, AppLockService.class);
            i.setAction(ACTION_START);
            running_intent = PendingIntent.getService(c, REQUEST_CODE, i,0);

            /*
             * PendingIntent getActivities (Context context,
                                            int requestCode,
                                            Intent intents,
                                            int flags)
             *  context 	    Context: The Context in which this PendingIntent should start the activity.
                requestCode 	int: Private request code for the sender
                intent      	Intent: Intent of the activity to be launched.
                flags 	        int: May be FLAG_ONE_SHOT, FLAG_NO_CREATE, FLAG_CANCEL_CURRENT, FLAG_UPDATE_CURRENT,
             or any of the flags as supported by Intent.fillIn() to control which unspecified parts of the intent that can be supplied when the actual send happens.
             *
             *
             */

        }

        return running_intent;
    }

    public static void startAlarm(Context c){
        AlarmManager am = (AlarmManager) c.getSystemService(ALARM_SERVICE);
        PendingIntent pi = getRunIntent(c);
        long interval = 250;
        long startTime = SystemClock.elapsedRealtime();
        am.setRepeating(AlarmManager.ELAPSED_REALTIME, startTime, interval, pi);
        Log.i("tag", "alarm started");
        /*
        * void setRepeating (int type,
                long triggerAtMillis,
                long intervalMillis,
                PendingIntent operation)
        * type:              ELAPSED_REALTIME, ELAPSED_REALTIME_WAKEUP, RTC, or RTC_WAKEUP.
        * triggerAtMiilis:   thoi gian dau tien ma alarm thuc hien hanh dong nao do
        * intervalMillis:    khoang cach giua 2 lan lap cua alarm
        * operation:        hanh dong de thuc hien khi alarm off
        * see at: https://developer.android.com/reference/android/app/AlarmManager.html#setRepeating(int,%20long,%20long,%20android.app.PendingIntent)
        * */
    }

    public static void stopAlarm(Context c){
        AlarmManager am = (AlarmManager) c.getSystemService(ALARM_SERVICE);
        am.cancel(getRunIntent(c));
    }




    private void doStopService(){
        stopAlarm(this);
        allowDestroy = true;
        stopForeground(true);
        stopSelf();
    }

    private void doRestartService(){
        allowRestart = true;
        stopSelf();
    }


    @Override
    public void onDestroy() {
        Log.i("TAG", "onDestroy");
        super.onDestroy();

        // huy doi tuong screenReceiver
        if(screenReceiver != null){
            unregisterReceiver(screenReceiver);
        }

        // kiem tra xem service co duoc restart hay khong
        if(allowRestart){
            Log.i(TAG, "allow restart");
            startAppLockService(this);
            allowRestart = false;
            return;
        }

        if(allowDestroy){
            // noi voi main activity la applock service da dung lai
            //...
        }
        else{
            // khong huy service --> bat dau lai
            Log.i(TAG, "not allow destroy --> restart service");
            startAppLockService(this);
        }

        allowDestroy = false;
    }
}
