package com.tanpn.applocker.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import java.util.HashSet;
import java.util.Set;

import com.tanpn.applocker.R;
import com.tanpn.applocker.lockservice.LockPreferences;

/**
 * Created by phamt_000 on 9/29/16.
 * Preference utils class:
 */
public class PreUtils {

    // file app_lock_list ( ~ app_lock_list.xml): luu cac gia tri cua app lock list
    public static String PREF_FILE_APPS = "app_lock_list";
    public static final String PREF_FILE_DEFAULT = "com.twinone.locker.prefs.default";

    private static final String ALIAS_CLASSNAME = "com.twinone.locker.MainActivityAlias";



    private final Context context;

    private final SharedPreferences prefs;
    private SharedPreferences apps;
    private SharedPreferences.Editor editor;

    public PreUtils(Context c) {
        context = c;
        prefs = context.getSharedPreferences(PREF_FILE_DEFAULT,
                Context.MODE_PRIVATE);
    }

    SharedPreferences.Editor FuncEditor() {
        if (editor == null) {
            editor = prefs.edit();
        }
        return editor;
    }

    public SharedPreferences.Editor put(int keyResId, Object value) {
        final String key = context.getString(keyResId);
        if (key == null) {
            throw new IllegalArgumentException(
                    "No resource matched key resource id");
        }
        Log.d("", "putting (key=" + key + ",value=" + value + ")");
        final SharedPreferences.Editor editor = FuncEditor();
        if (value instanceof String)
            editor.putString(key, (String) value);
        else if (value instanceof Integer)
            editor.putInt(key, (Integer) value);
        else if (value instanceof Boolean)
            editor.putBoolean(key, (Boolean) value);
        else if (value instanceof Float)
            editor.putFloat(key, (Float) value);
        else if (value instanceof Long)
            editor.putLong(key, (Long) value);
        else
            throw new IllegalArgumentException("Unknown data type");
        return editor;
    }

    public SharedPreferences.Editor putString(int keyResId, int valueResId) {
        final SharedPreferences.Editor editor = FuncEditor();
        editor.putString(context.getString(keyResId),
                context.getString(valueResId));
        return editor;
    }


    // ham lay string tu preferences
    public String getString(int keyResId) {
        return prefs.getString(context.getString(keyResId), null);
    }

	/**
	lấy string với key = keyResId
	nếu k có giá trị nào có key bằng nó thì lấy giá trị mặc định có key = defResId
	
	*/
    public String getString(int keyResId, int defResId) {
        final String key = context.getString(keyResId);
        return (prefs.contains(key)) ? prefs.getString(key, null) : context
                .getString(defResId);
    }
	/**
	lấy string với key = keyResId
	nếu k có giá trị nào có key bằng nó thì lấy giá trị mặc định = defValue
	
	*/
    public String getString(int keyResId, String defValue) {
        return prefs.getString(context.getString(keyResId), defValue);
    }

    /**

     */
    Integer parseInt(int keyResId) {
        try {
            return Integer.parseInt(getString(keyResId));
        } catch (Exception e) {
            return null;
        }
    }

    /**


     */
    public Integer parseInt(int keyResId, int defResId) {
        final Integer result = parseInt(keyResId);
        return (result != null) ? result : Integer.parseInt(context
                .getString(defResId));
    }

    /**

     */
    public Integer parseLong(int keyResId) {
        try {
            return Integer.parseInt(getString(keyResId));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     *
     */
    public Long parseLong(int keyResId, int defResId) {
        final Integer result = parseInt(keyResId);
        return (result != null) ? result : Long.parseLong(context
                .getString(defResId));
    }


    public boolean getBoolean(int keyResId, int defResId) {
        final Boolean result = getBooleanOrNull(keyResId);
        return result != null ? result : context.getResources().getBoolean(
                defResId);
    }

    private Boolean getBooleanOrNull(int keyResId) {
        final String key = context.getString(keyResId);
        return (prefs.contains(key)) ? prefs.getBoolean(key, false) : null;
    }

    public Float getFloatOrNull(int keyResId) {
        final String key = context.getString(keyResId);
        return (prefs.contains(key)) ? prefs.getFloat(key, 0) : null;
    }

    public Long getLongOrNull(int keyResId) {
        final String key = context.getString(keyResId);
        return (prefs.contains(key)) ? prefs.getLong(key, 0) : null;
    }

    /**

     */
    //--------------------------
    // tra ve doi tuong share preference
    public static SharedPreferences appPrefs(Context context){
        return context.getSharedPreferences(PREF_FILE_APPS, Context.MODE_PRIVATE);
    }

    // lay gia tri tu file app_lock_list
    public static Set<String> getLockedApps(Context c) {
        SharedPreferences sp = appPrefs(c);
        return new HashSet<>(sp.getAll().keySet());
    }

    // luu lai gia tri cua mEditor
    @SuppressLint("NewApi")
    public static void apply(SharedPreferences.Editor editor) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
            editor.commit();
        } else {
            editor.apply();
        }
    }
    public void apply() {
        apply(FuncEditor());
        editor = null;
    }

    // lay kieu khoa hien tai: password, pattern
    String getCurrentLockType() {
        return getString(R.string.pref_key_lock_type,
                R.string.pref_def_lock_type);
    }

    public int getCurrentLockTypeInt() {
        final String type = getCurrentLockType();
        if (type.equals(context
                .getString(R.string.pref_val_lock_type_password)))
            return LockPreferences.TYPE_PASSWORD;
        else if (type.equals(context
                .getString(R.string.pref_val_lock_type_pattern)))
            return LockPreferences.TYPE_PATTERN;
        return 0;

    }

    // lay password hien tai
    String getCurrentPassword() {
        int lockType = getCurrentLockTypeInt();
        String password = null;
        switch (lockType) {
            case LockPreferences.TYPE_PASSWORD:
                password = getString(R.string.pref_key_password);
                break;
            case LockPreferences.TYPE_PATTERN:
                password = getString(R.string.pref_key_pattern);
                break;
        }
        return password;
    }

    // kiem tra xem password co bi rong hay khong
    public boolean isCurrentPasswordEmpty() {
        String password = getCurrentPassword();
        return password == null || password.isEmpty();
    }
}
