package com.tanpn.applocker.lockservice;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tanpn.applocker.R;
import com.tanpn.applocker.utils.PreUtils;
import com.tanpn.applocker.utils.utils;

import java.io.FileNotFoundException;

/**
 * Created by phamt_000 on 11/9/16.
 */
public class LockService extends Service implements View.OnClickListener,
        View.OnKeyListener {

    public static Intent getLockIntent(Context c, String packageName) {

        Intent i = new Intent(c, LockService.class);
        i.setAction(ACTION_COMPARE);
        i.putExtra(EXTRA_PACKAGENAME, packageName);
        return i;
    }

    public static void hide(Context c) {
        Intent i = new Intent(c, LockService.class);
        i.setAction(ACTION_HIDE);
        c.startService(i);
    }

    public static void showCompare(Context c, String packageName) {
        Log.d("AABB", "Dsdas");
        c.startService(getLockIntent(c, packageName));
    }

    /**
     * create new lock
     * */
    public static void showCreate(Context c, int type) {
        Log.i("HAHA", "showCreate (type=" + type + ")");
        Intent i = new Intent(c, LockService.class);
        i.setAction(ACTION_CREATE);
        LockPreferences prefs = new LockPreferences(c);
        prefs.type = type;
        i.putExtra(EXTRA_PREFERENCES, prefs);
        c.startService(i);
    }

    public static void showCreate(Context c, int type, int size) {
        Log.d("HAHA", "showCreate (type=" + type + ",size=" + size + ")");
        Intent i = new Intent(c, LockService.class);
        i.setAction(ACTION_CREATE);
        LockPreferences prefs = new LockPreferences(c);
        prefs.type = type;
        prefs.patternSize = size;
        i.putExtra(EXTRA_PREFERENCES, prefs);
        c.startService(i);
    }


    /**
     * Chức năng của phím trái
     * */
    private enum LeftButtonAction{
        BACK,
        CANCEL
    }

    /**
    * Chức năng của phím phải
    * **/
    private enum RightButtonAction {
        CONFIRM, CONTINUE
    }

    private enum ViewState{
        /**
         * View đã show nhưng chưa hoàn toàn show (đang trong trạng thái chuẩn bị show)
         * user không thể thao tác với view trong trạng thái này
         * **/
        SHOWING,

        /**
         * view đã được show hoàn toàn và user có thể thao tác với nó
         * **/
        SHOWN,

        /**
         * user đã unlock hoặc là nhấn nút back --> view đang trạng thá chuẩn bị ẩn đi
         * */
        HIDING,

        /**
         * view đã hoàn toàn bị ẩn
         * **/
        HIDDEN

    }

    private ViewState mViewState = ViewState.HIDDEN;

    private ServiceState mServiceState = ServiceState.NOT_BOUND;

    private enum ServiceState {
        /**
         * Service is not bound
         */
        NOT_BOUND,
        /**
         * We have requested binding, but not yet received it...
         */
        BINDING,
        /**
         * Service is successfully bound (we can interact with it)
         */
        BOUND,
        /**
         * Service requesting unbind
         */
        UNBINDING
    }

    /**
     *
     */
    private static abstract class BaseAnimationListener implements
            Animation.AnimationListener {

        @Override
        public void onAnimationStart(Animation animation) {
        }

        @Override
        public void onAnimationEnd(Animation animation) {
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }

    }


    /**
     * implememt event OnNumberListener để thao tác với màn hình khóa dạng password
     * */
    private class MyOnNumberListener implements PasswordView.OnNumberListener{

        @Override
        public void onStart() {

        }

        @Override
        public void onNumberButton(String newNumber) {
            if (newNumber.length() > MAX_PASSWORD_LENGTH) {
                newNumber = newNumber.substring(0, MAX_PASSWORD_LENGTH);
                mLockPasswordView.setPassword(newNumber);
            }
            updatePasswordTextView(newNumber);

            if (ACTION_COMPARE.equals(mAction)) {
                doComparePassword(false);
            }
        }

        @Override
        public void onOkButton() {
            if (ACTION_COMPARE.equals(mAction)) {
                doComparePassword(true);
            }
        }

        @Override
        public void onOkButtonLong() {

        }

        @Override
        public void onBackButton() {
            updatePassword();
        }

        @Override
        public void onBackButtonLong() {

        }
    }

    /**
     * Đối chiếu password
     * */
    private void doComparePassword(boolean explicit) {
        final String currentPassword = mLockPasswordView.getPassword();
        if (currentPassword.equals(options.password)) {
            exitSuccessCompare();
        } else if (explicit) {
            mLockPasswordView.clearPassword();
            updatePassword();
            Toast.makeText(this, "Sai mật khẩu",
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Exit when an app has been unlocked successfully
     */
    private void exitSuccessCompare() {
        if (mPackageName == null || mPackageName.equals(getPackageName())) {
            finish(true);
            return;
        }
        if (mServiceState == ServiceState.BOUND) {
            mAppLockService.unlockApp(mPackageName);
        } else {
            Log.w(TAG, "Not bound to lockservice (mServiceState="
                        + mServiceState + ")");
        }
        finish(true);
    }

    private final ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName cn, IBinder binder) {
            Log.v(TAG, "Service bound (mServiceState=" + mServiceState + ")");

            final AppLockService.LocalBinder b = (AppLockService.LocalBinder) binder;
            mAppLockService = b.getInstance();
            mServiceState = ServiceState.BOUND;
        }

        @Override
        public void onServiceDisconnected(ComponentName cn) {
            Log.v(TAG, "Unbound service (mServiceState=" + mServiceState + ")");

            // We can't make it "UNBOUND", because even if the server got
            // unbound, android expects us to call unbindService
            mServiceState = ServiceState.UNBINDING;
        }
    };







    // DEFINE
    private static final String CLASSNAME = LockService.class.getName();
    /**
     * Kiểm tra mật khẩu hiện tại ( kể cả number và parttern)
     */
    public static final String ACTION_COMPARE = CLASSNAME + ".action.compare";

    /**
     * Tạo mật khẩu mới bằng cách yêu cầu user nhập mật khẩu vào 2 lần
     */
    public static final String ACTION_CREATE = CLASSNAME + ".action.create";

    private static final String ACTION_NOTIFY_PACKAGE_CHANGED = CLASSNAME + ".action.notify_package_changed";

    public static final String EXTRA_LOCK = CLASSNAME + ".action.extra_lock";

    /**
     * Khi mà action là ACTION_COMPARE thì sử dụng EXTRA_PACKAGENAME để xác định đối tượng app bị khóa
     */
    public static final String EXTRA_PACKAGENAME = CLASSNAME + ".extra.target_packagename";
    /**
     * A {@link LockPreferences} providing additional details on how this
     * {@link LockService} should behave. You should start with a
     * and change only the
     * properties you want to.
     */
    private static final String EXTRA_PREFERENCES = CLASSNAME + ".extra.options";
    public static final int PATTERN_COLOR_BLUE = 2;
    public static final int PATTERN_COLOR_GREEN = 1;

    public static final int PATTERN_COLOR_WHITE = 0;
    private static final String ACTION_HIDE = CLASSNAME + ".action.hide";

    private static final int MAX_PASSWORD_LENGTH = 8;
    private static final long PATTERN_DELAY = 600;



    private Intent mIntent;
    private String mAction;




    private Animation mAnimHide;

    private Animation mAnimShow;


    private WindowManager mWindowManager;

    private View mRootView;
    private TextView mTextViewPassword;

    private ImageView mViewBackground;

    private TextView mViewMessage;

    private TextView mViewTitle;
    private ImageView mAppIcon;

    private Button mLeftButton;
    private LeftButtonAction mLeftButtonAction;

    private Button mRightButton;
    private RightButtonAction mRightButtonAction;

    private PasswordView mLockPasswordView;
    private RelativeLayout mContainer;
    private LinearLayout mFooterButtons;
    private ViewGroup mLockView;
    private String mNewPassword;

    private String mPackageName;
    private LockPreferences options;
    private WindowManager.LayoutParams mLayoutParams;

    private PasswordView.OnNumberListener mPasswordListener;

    private long mTimeViewShown;

    /**
     * Called after views are inflated
     */
    // private AdViewManager mAdViewManager;
    // private AppLockService mAppLockService;
    private AppLockService mAppLockService;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.lock_footer_b_left:
                if (ACTION_CREATE.equals(mAction)) {
                    if (mLeftButtonAction == LeftButtonAction.BACK) {
                        setupFirst();
                    } else {
                        exitCreate();
                    }
                }
                break;
            case R.id.lock_footer_b_right:
                if (ACTION_CREATE.equals(mAction)) {
                    if (mRightButtonAction == RightButtonAction.CONTINUE) {
                        setupSecond();
                    } else {
                        doConfirm();
                    }
                }
                break;
        }
    }



    private void exitCreate() {
        AppLockService.forceRestart(this);
        finish(true);
    }

    private void doConfirm() {
        if (options.type == LockPreferences.TYPE_PATTERN) {
            //doConfirmPattern();
        } else {
            doConfirmPassword();
        }
    }

    private void doConfirmPassword() {
        final String newValue = mLockPasswordView.getPassword();
        if (!newValue.equals(mNewPassword)) {
            Toast.makeText(this, R.string.password_change_not_match,
                    Toast.LENGTH_SHORT).show();
            setupFirst();
            return;
        }
        PreUtils prefs = new PreUtils(this);
        prefs.put(R.string.pref_key_password, newValue);
        prefs.putString(R.string.pref_key_lock_type,
                R.string.pref_val_lock_type_password);
        prefs.apply();
        Toast.makeText(this, R.string.password_change_saved, Toast.LENGTH_SHORT)
                .show();
        exitCreate();
    }



    @Override
    public boolean onKey(View view, int i, KeyEvent keyEvent) {

        // nếu user nhấn nút back trên device (đang ở màn hinh khóa thì thoát)
        switch (i) {
            case KeyEvent.KEYCODE_BACK:
                finish(false);
                return true;
        }
        return true;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }




    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("HAHA", "lock serive is created");
    }

    private final String TAG = "TAG_LOCK_SERVICE";


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("HAHA", "ron start command");

        if(intent == null){
            Log.i(TAG, "return START_NOT_STICKY");
            return START_NOT_STICKY;
        }

        if(ACTION_HIDE.equals(intent.getAction())){
            // nếu đây là action hide thì finish : có thể là unlock hoặc là back -> view bị hidden
            finish(true);
            return START_NOT_STICKY;
        }

        if (ACTION_NOTIFY_PACKAGE_CHANGED.equals(intent.getAction())) {
            //nếu đây là action mở app khác
            // xác định package
            String newPackageName = intent.getStringExtra(EXTRA_PACKAGENAME);
            if (newPackageName == null || !getPackageName().equals(newPackageName)) {
                // nếu package == null hoặc là chính package của app mình thì thôi
                finish(true);
                return START_NOT_STICKY;
            }
        } else {
            // còn trường hợp còn lại thì xác định hiển thị view
            mIntent = intent;
            showView();
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.d(TAG, "onConfigChange");
        // super.onConfigurationChanged(newConfig);
        if (mViewState == ViewState.SHOWING || mViewState == ViewState.SHOWN) {
            showView();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v(TAG, "onDestroy (mServiceState=" + mServiceState + ")");
        if (mServiceState != ServiceState.NOT_BOUND) {
            Log.v(TAG, "onDestroy unbinding");
            unbindService(mConnection);
            mServiceState = ServiceState.NOT_BOUND;
        }
    }


    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.d(TAG, "onLowMemory()");
    }


    private void finish(boolean unlocked) {
        if (!unlocked && ACTION_COMPARE.equals(mAction)) {
            final Intent i = new Intent(Intent.ACTION_MAIN);
            i.addCategory(Intent.CATEGORY_HOME);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        }
        hideView();
    }



    /**
     * ----------show view
     * **/
    private void showView(){
        Log.v(TAG, "called showView" + " (mViewState=" + mViewState + ")");

        if (mViewState == ViewState.HIDING || mViewState == ViewState.SHOWING) {
            cancelAnimations();
        }

        if (mViewState != ViewState.HIDDEN) {
            Log.w(TAG, "called showView but was not hidden");
            mWindowManager.removeView(mRootView);
        }

        // Prepare everything
        beforeInflate();
        // Create the view
        mRootView = inflateRootView();
        // Show the view
        mWindowManager.addView(mRootView, mLayoutParams);
        // Do some extra stuff when the view's ready
        afterInflate();

        mViewState = ViewState.SHOWING;
        showViewAnimate();
    }

    /**
     * ----------hide view
     * */
    private void hideView(){
        // thoát khỏi màn hình khóa
        Log.v(TAG, "called hideView" + " (mViewState=" + mViewState + ")");
        if (mViewState == ViewState.HIDING || mViewState == ViewState.HIDDEN) {
            Log.w(TAG, "called hideView not hiding (mViewState=" + mViewState
                    + ")");
            onViewHidden();
            return;
        }
        if (mViewState == ViewState.SHOWING) {
            cancelAnimations();
        }
        mViewState = ViewState.HIDING;
        hideViewAnimate();
    }

    /**
     * Sự kiện oonViewHide
     * */
    private void onViewHidden(){
        Log.v(TAG, "called onViewHidden" + " (mViewState=" + mViewState
                    + ")");
        if (mViewState != ViewState.HIDDEN) {
            mViewState = ViewState.HIDDEN;
            mWindowManager.removeView(mRootView);
        }
        mAnimHide = null;


        // With stopSelf there is a problem with the rotation
        // If this isn't in, the ad view will not load

        stopSelf();
    }

    private void hideViewAnimate() {
        Log.v(TAG, "called hideViewAnimate" + " (mViewState=" + mViewState
                    + ")");
        // Log.d(TAG, "animating hide (resId=" + options.hideAnimationResId
        // + ",millis=" + options.hideAnimationMillis + ")");
        if (options.hideAnimationResId == 0 || options.hideAnimationMillis == 0) {
            onViewHidden();
            return;
        }

        mAnimHide = AnimationUtils.loadAnimation(this,
                options.hideAnimationResId);
        mAnimHide.setDuration(options.hideAnimationMillis);
        mAnimHide.setFillEnabled(true);
        mAnimHide.setDetachWallpaper(false);
        mAnimHide.setAnimationListener(new BaseAnimationListener() {

            @Override
            public void onAnimationEnd(Animation animation) {
                // Avoid ugly android error message
                new Handler().post(new Runnable() {

                    @Override
                    public void run() {
                        onViewHidden();
                    }
                });
            }

        });
        mContainer.startAnimation(mAnimHide);
    }

    /**
     * show animation
     * */
    private void showViewAnimate() {
        Log.v(TAG, "called showViewAnimate" + " (mViewState=" + mViewState
                    + ")");
        if (options.showAnimationResId == 0 || options.showAnimationMillis == 0) {
            onViewShown();
            return;
        }
        mAnimShow = AnimationUtils.loadAnimation(this,
                options.showAnimationResId);
        mAnimShow.setAnimationListener(new BaseAnimationListener() {

            @Override
            public void onAnimationEnd(Animation animation) {
                onViewShown();
            }
        });
        mAnimShow.setDuration(options.showAnimationMillis);
        mAnimShow.setFillEnabled(true);
        mContainer.startAnimation(mAnimShow);
    }

    private void onViewShown() {

        mTimeViewShown = System.nanoTime();

        Log.v(TAG, "called onViewShown" + " (mViewState=" + mViewState
                    + ")");
        mViewState = ViewState.SHOWN;
        mAnimShow = null;
    }

    /**
     * Cancel animation nhưng không remove view trong window manager
     */
    private void cancelAnimations() {
        Log.v(TAG, "called hideViewCancel" + " (mViewState=" + mViewState
                + ")");
        if (mViewState == ViewState.HIDING) {
            mAnimHide.setAnimationListener(null);
            mAnimHide.cancel();
            mAnimHide = null;
        } else if (mViewState == ViewState.SHOWING) {
            mAnimShow.setAnimationListener(null);
            mAnimShow.cancel();
            mAnimShow = null;
        }
    }

    /**
     *
     * **/



    private boolean beforeInflate() {
        if (mIntent == null) {
            return false;
        }

        mAction = mIntent.getAction();
        if (mAction == null) {
            Log.w(TAG, "Finishing: No action specified");
            return false;
        }

        if (mIntent.hasExtra(EXTRA_PREFERENCES)) {
            options = (LockPreferences) mIntent
                    .getSerializableExtra(EXTRA_PREFERENCES);
        } else {
            options = new LockPreferences(this);
        }

        mPackageName = mIntent.getStringExtra(EXTRA_PACKAGENAME);

        if (!getPackageName().equals(mPackageName)) {
            Intent i = new Intent(this, AppLockService.class);
            if (mServiceState == ServiceState.NOT_BOUND) {
                Log.v(TAG, "Binding service (mServiceState=" + mServiceState + ")");
                mServiceState = ServiceState.BINDING;
                bindService(i, mConnection, 0);
            } else {
                Log.v(TAG,"Not binding service in afterInflate (mServiceState=" + mServiceState + ")");
            }
        }

        if (ACTION_CREATE.equals(mAction) || mPackageName == getPackageName()) {
            options.showAds = false;
        } else {
            options.showAds = true;
        }

        if (ACTION_CREATE.equals(mAction)) {
            options.patternStealth = false;
        }

        // animations

        mLayoutParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_FULLSCREEN
                        | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT);
        mLayoutParams.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;

        return true;
    }

    private void afterInflate() {
        setBackground();
        if (options.showAds) {


        } else {
            // Don't use precious space
            mRootView.findViewById(R.id.lock_ad_container).setVisibility(
                    View.GONE);
            Log.w(TAG, "Not requesting ads!!!n!!!");
        }


        switch (options.type) {
            case LockPreferences.TYPE_PATTERN:
                //showPatternView();
                break;
            case LockPreferences.TYPE_PASSWORD:
                showPasswordView();
                break;
        }
        // Views
        if (ACTION_COMPARE.equals(mAction)) {
            mAppIcon.setVisibility(View.VISIBLE);
            mFooterButtons.setVisibility(View.GONE);
            ApplicationInfo ai = utils.getaApplicationInfo(mPackageName, this);
            if (ai != null) {
                // Load info of Locker
                String label = ai.loadLabel(getPackageManager()).toString();
                Drawable icon = ai.loadIcon(getPackageManager());
                utils.setBackgroundDrawable(mAppIcon, icon);
                mViewTitle.setText(label);
                if (options.message != null && options.message.length() != 0) {
                    mViewMessage.setVisibility(View.VISIBLE);
                    // Don't use String.format, because this is user input
                    mViewMessage.setText(options.message.replace("%s", label));
                } else {
                    mViewMessage.setVisibility(View.GONE);
                }
            } else {
                // if we can't load, don't take up space
                mAppIcon.setVisibility(View.GONE);
            }
        } else if (ACTION_CREATE.equals(mAction)) {
            mAppIcon.setVisibility(View.GONE);
            mFooterButtons.setVisibility(View.VISIBLE);
            setupFirst();
        }
    }

    private boolean showPasswordView() {
        mLockView.removeAllViews();
        //mLockPatternView = null;
        LayoutInflater li = LayoutInflater.from(this);

        mTextViewPassword = (TextView) li.inflate(
                R.layout.view_lock_number_textview, null);
        mLockView.addView(mTextViewPassword);

        mLockPasswordView = (PasswordView) li.inflate(
                R.layout.view_lock_number, null);
        mLockView.addView(mLockPasswordView);

        mLockPasswordView.setListener(mPasswordListener);
        if (ACTION_CREATE.equals(mAction)) {
            mLockPasswordView.setOkButtonVisibility(View.INVISIBLE);
        } else {
            mLockPasswordView.setOkButtonVisibility(View.VISIBLE);
        }

        mLockPasswordView.setTactileFeedbackEnabled(options.vibration);
        mLockPasswordView.setSwitchButtons(options.passwordSwitchButtons);
        mLockPasswordView.setVisibility(View.VISIBLE);
        options.type = LockPreferences.TYPE_PASSWORD;
        return true;
    }

    private void setupFirst() {
        if (options.type == LockPreferences.TYPE_PATTERN) {
            /*mLockPatternView.setInStealthMode(false);
            mLockPatternView.clearPattern(PATTERN_DELAY);
            mViewTitle.setText(R.string.pattern_change_tit);
            mViewMessage.setText(R.string.pattern_change_head);
            mNewPattern = null;*/
        } else {
            mLockPasswordView.clearPassword();
            updatePassword();

            mViewTitle.setText(R.string.password_change_tit);
            mViewMessage.setText(R.string.password_change_head);
            mNewPassword = null;
        }
        mLeftButton.setText(android.R.string.cancel);
        mRightButton.setText(R.string.button_continue);
        mLeftButtonAction = LeftButtonAction.CANCEL;
        mRightButtonAction = RightButtonAction.CONTINUE;
    }

    private void setupSecond() {
        if (options.type == LockPreferences.TYPE_PATTERN) {
            /*mNewPattern = mLockPatternView.getPatternString();
            if (mNewPattern.length() == 0) {
                return;
            }
            mViewMessage.setText(R.string.pattern_change_confirm);
            mLockPatternView.clearPattern();*/
        } else {
            mNewPassword = mLockPasswordView.getPassword();
            if (mNewPassword.length() == 0) {
                Toast.makeText(this, R.string.password_empty,
                        Toast.LENGTH_SHORT).show();
                return;
            }
            mLockPasswordView.setPassword("");
            updatePassword();
            mViewMessage.setText(R.string.password_change_confirm);
        }
        mLeftButton.setText(R.string.button_back);
        mRightButton.setText(R.string.button_confirm);
        mLeftButtonAction = LeftButtonAction.BACK;
        mRightButtonAction = RightButtonAction.CONFIRM;
    }
    /**

     */
    private void updatePassword() {
        String pwd = mLockPasswordView.getPassword();
        if (MAX_PASSWORD_LENGTH != 0) {
            if (pwd.length() >= MAX_PASSWORD_LENGTH) {
                mLockPasswordView.setPassword(pwd.substring(0,
                        MAX_PASSWORD_LENGTH));
            }
        }
        updatePasswordTextView(mLockPasswordView.getPassword());
    }
    private void updatePasswordTextView(String newText) {
        mTextViewPassword.setText(newText);
    }

    //----------------set background
    private void setBackground() {
        String def = getString(R.string.pref_val_bg_default);
        String blue = getString(R.string.pref_val_bg_blue);
        String dark_blue = getString(R.string.pref_val_bg_dark_blue);
        String green = getString(R.string.pref_val_bg_green);
        String purple = getString(R.string.pref_val_bg_purple);
        String red = getString(R.string.pref_val_bg_red);
        String orange = getString(R.string.pref_val_bg_orange);
        String turquoise = getString(R.string.pref_val_bg_turquoise);
        mViewBackground.setImageBitmap(null);
        if (blue.equals(options.background)) {
            mViewBackground.setBackgroundColor(getResources().getColor(
                    R.color.flat_blue));
        } else if (dark_blue.equals(options.background)) {
            mViewBackground.setBackgroundColor(getResources().getColor(
                    R.color.flat_dark_blue));
        } else if (green.equals(options.background)) {
            mViewBackground.setBackgroundColor(getResources().getColor(
                    R.color.flat_green));
        } else if (purple.equals(options.background)) {
            mViewBackground.setBackgroundColor(getResources().getColor(
                    R.color.flat_purple));
        } else if (red.equals(options.background)) {
            mViewBackground.setBackgroundColor(getResources().getColor(
                    R.color.flat_red));
        } else if (turquoise.equals(options.background)) {
            mViewBackground.setBackgroundColor(getResources().getColor(
                    R.color.flat_turquoise));
        } else if (orange.equals(options.background)) {
            mViewBackground.setBackgroundColor(getResources().getColor(
                    R.color.flat_orange));
        } else if (def.equals(options.background) || !setBackgroundFromUri()) {
            mViewBackground.setImageResource(R.drawable.locker_default_background);
        }
    }

    /**
     * đặt hình nền cho background từ 1 ảnh trong device
     * **/
    private boolean setBackgroundFromUri() {
        if (options.background == null)
            return false;
        Uri uri = Uri.parse(options.background);
        if (uri == null)
            return false;

        Point size = getSizeCompat(mWindowManager.getDefaultDisplay());
        try {
            final Bitmap b = decodeSampledBitmapFromUri(uri, size.x, size.y);
            if (b == null) {
                return false;
            }
            mViewBackground.setImageBitmap(b);
        } catch (FileNotFoundException e) {
            Log.w(TAG, "Error setting background");
            return false;
        }
        return true;
    }


    /**
     * lấy size hiển thị của app
     * */
    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    private Point getSizeCompat(Display display) {
        Point p = new Point();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR2) {
            p.x = display.getWidth();
            p.y = display.getHeight();
        } else {
            display.getSize(p);
        }
        return p;
    }

    /**
     * Chuyển từ uri sang bitmap, 1 ảnh được load lên từ device sẽ có data dưới dạng uri
     * */
    Bitmap decodeSampledBitmapFromUri(Uri uri, int reqWidth,
                                      int reqHeight) throws FileNotFoundException {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(getContentResolver().openInputStream(uri),
                null, options);

        options.inSampleSize = calculateInSampleSize(options, reqWidth,
                reqHeight);

        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeStream(getContentResolver()
                .openInputStream(uri), null, options);
    }
        /**
         * Tính toán sao cho hình load lên làm background cho full screeen
         * **/
    private static int calculateInSampleSize(BitmapFactory.Options options,
                                             int reqWidth, int reqHeight) {
        int scale = 1;
        int width = options.outWidth;
        int height = options.outHeight;
        while (true) {
            if (width / 2 < reqWidth || height / 2 < reqHeight) {
                break;
            }
            width /= 2;
            height /= 2;
            scale *= 2;
        }
        return scale;
    }
    //--------------------------------------load background
    private View inflateRootView() {
        Log.i(TAG, "called inflateRootView" + " (mViewState=" + mViewState
                    + ")");
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        LayoutInflater li = LayoutInflater.from(this);

        setTheme(R.style.LockActivityTheme);
        View root = (View) li.inflate(R.layout.layout_alias_locker, null);
        mContainer = (RelativeLayout) root.findViewById(R.id.lock_container);
        mViewBackground = (ImageView) root
                .findViewById(R.id.lock_iv_background);
        root.setOnKeyListener(this);
        root.setFocusable(true);
        root.setFocusableInTouchMode(true);

        mViewTitle = (TextView) root.findViewById(R.id.lock_tv_title);
        mViewMessage = (TextView) root.findViewById(R.id.lock_tv_footer);
        mAppIcon = (ImageView) root.findViewById(R.id.lock_iv_app_icon);
        mLockView = (ViewGroup) root.findViewById(R.id.lock_lockview);

        mFooterButtons = (LinearLayout) root
                .findViewById(R.id.lock_footer_buttons);
        mLeftButton = (Button) root.findViewById(R.id.lock_footer_b_left);
        mRightButton = (Button) root.findViewById(R.id.lock_footer_b_right);

        mRightButton.setOnClickListener(this);
        mLeftButton.setOnClickListener(this);

        mPasswordListener = new MyOnNumberListener();
        return root;
    }

}
