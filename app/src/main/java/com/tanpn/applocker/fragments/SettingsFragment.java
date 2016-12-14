package com.tanpn.applocker.fragments;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;

import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.support.v4.app.Fragment;

import android.support.v4.preference.PreferenceFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.tanpn.applocker.R;
import com.tanpn.applocker.lock.ChooseLockType;
import com.tanpn.applocker.lockservice.AppLockService;
import com.tanpn.applocker.utils.PreUtils;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends PreferenceFragment implements
        Preference.OnPreferenceChangeListener, SharedPreferences.OnSharedPreferenceChangeListener,
        Preference.OnPreferenceClickListener {



    public SettingsFragment() {
        // Required empty public constructor
    }


    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    private SwitchPreference enable;

    private CheckBoxPreference vibration_press;
    private CheckBoxPreference vibration_wrong;
    private CheckBoxPreference reboot;

    private Preference changePassword;

    private ListPreference colorBackground;
    private ListPreference wrongTimes;

    private PreferenceCategory catGeneral, catPassword;

    private PreUtils prefUtil;


    @Override
    public void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        addPreferencesFromResource(R.xml.settings);

        PreferenceManager pm = getPreferenceManager();
        pm.setSharedPreferencesName(PreUtils.PREF_FILE_DEFAULT);
        pm.setSharedPreferencesMode(Context.MODE_PRIVATE);


        pref = pm.getSharedPreferences();
        editor = pm.getSharedPreferences().edit();

        init();
    }

    private void init(){
        enable = (SwitchPreference) findPreference(getString(R.string.pref_key_lock_enable));

        vibration_press = (CheckBoxPreference) findPreference(getString(R.string.pref_key_setting_press_vibra));
        vibration_wrong = (CheckBoxPreference) findPreference(getString(R.string.pref_key_setting_vibra));
        reboot = (CheckBoxPreference) findPreference(getString(R.string.pref_key_setting_start_at_boot));

        changePassword = (Preference) findPreference(getString(R.string.pref_key_setting_change_pwrd_pass));

        colorBackground = (ListPreference) findPreference(getString(R.string.pref_key_lock_background));
        wrongTimes = (ListPreference) findPreference(getString(R.string.pref_key_setting_wrong_time));

        catGeneral = (PreferenceCategory) findPreference(getString(R.string.pref_key_setting_title_general));
        catPassword = (PreferenceCategory) findPreference(getString(R.string.pref_key_setting_title_password));


        prefUtil = new PreUtils(getContext());



        pref.registerOnSharedPreferenceChangeListener(this);
        enable.setOnPreferenceChangeListener(this);
        vibration_press.setOnPreferenceChangeListener(this);
        vibration_wrong.setOnPreferenceChangeListener(this);

        reboot.setOnPreferenceChangeListener(this);
        colorBackground.setOnPreferenceChangeListener(this);
        wrongTimes.setOnPreferenceChangeListener(this);

        changePassword.setOnPreferenceClickListener(this);


        initValues();
    }

    private void initValues(){
        enable.setChecked(prefUtil.getBoolean(R.string.pref_key_lock_enable, false));
        vibration_press.setChecked(prefUtil.getBoolean(R.string.pref_key_setting_press_vibra, false));
        vibration_wrong.setChecked(prefUtil.getBoolean(R.string.pref_key_setting_vibra, false));
        reboot.setChecked(prefUtil.getBoolean(R.string.pref_key_setting_start_at_boot, false));

        String s1 = prefUtil.getString(R.string.pref_key_lock_background, "0");
        String s2 = prefUtil.getString(R.string.pref_key_setting_wrong_time, "0");

        colorBackground.setValueIndex(Integer.parseInt(s1));
        wrongTimes.setValueIndex(Integer.parseInt(s2));

        colorBackground.setSummary(colorBackground.getEntry());
        wrongTimes.setSummary(wrongTimes.getEntry());

        if(!enable.isChecked()){
            catPassword.setEnabled(false);
            catGeneral.setEnabled(false);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        return super.onCreateView(inflater, container,
                savedInstanceState);
    }


    @Override
    public boolean onPreferenceTreeClick() {
        return false;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object o) {

        // ap dung vs checkbox + list
        String key = preference.getKey();

        String k1 = getString(R.string.pref_key_lock_enable);

        String k2 = getString(R.string.pref_key_setting_press_vibra);
        String k3 = getString(R.string.pref_key_setting_vibra);
        String k4 = getString(R.string.pref_key_setting_start_at_boot);

        String k5 = getString(R.string.pref_key_lock_background);
        String k6 = getString(R.string.pref_key_setting_wrong_time);


        if(key.equals(k1)){
            editor.putBoolean(k1, (Boolean)o);
            editor.commit();

            catPassword.setEnabled((Boolean)o);
            catGeneral.setEnabled((Boolean)o);
        }
        else if(key.equals(k2)){
            editor.putBoolean(k2, (Boolean)o);
            editor.commit();
        }
        else if(key.equals(k3)){
            editor.putBoolean(k3, (Boolean)o);
            editor.commit();

        }
        else if(key.equals(k4)){
            editor.putBoolean(k4, (Boolean)o);
            editor.commit();
        }
        else if(key.equals(k5)){
            if(o.toString().equals("Tùy chọn")){
                backgroundFromImage();
            }

            editor.putString(k5, o.toString());
            editor.commit();
            colorBackground.setSummary(colorBackground.getEntry());
        }
        else if(key.equals(k6)){
            editor.putString(k6, o.toString());
            editor.commit();
            wrongTimes.setSummary(wrongTimes.getEntry());

        }

        return true;
    }

    private final int IMG_REQ_CODE = 2;
    private void backgroundFromImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_PICK);
        List<ResolveInfo> ris = getActivity().getPackageManager()
                .queryIntentActivities(intent,
                        PackageManager.MATCH_DEFAULT_ONLY);
        if (ris.size() > 0) {
            startActivityForResult(Intent.createChooser(intent, null),
                    IMG_REQ_CODE);
        } else {
            Toast.makeText(getActivity(), "Error - No gallery app(?)",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int req, int res, Intent data) {
        Log.d("", "onActivityResult");
        if (req == IMG_REQ_CODE && res == Activity.RESULT_OK) {
            String image = data.getData().toString();
            prefUtil.put(R.string.pref_key_background, image).apply();
        }

        super.onActivityResult(req, res, data);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        String key = preference.getKey();

        String k1 = getString(R.string.pref_key_setting_change_pwrd_pass);

        if(key.equals(k1)){
            Intent in = new Intent(getContext(), ChooseLockType.class);
            startActivity(in);
        }

        return false;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {



        colorBackground.setSummary(colorBackground.getEntry());
        wrongTimes.setSummary(wrongTimes.getEntry());
        try{
            if(prefUtil.getBoolean(R.string.pref_key_lock_enable, true)){
                if(AppLockService.isRunning(getContext()))
                    AppLockService.restart(getContext());       // neu dang chay thi restart
                else
                    AppLockService.toggle(getContext());        // neu k thi khoi dong lai service
            }
            else
                AppLockService.stopAppLockService(getContext());
        }
        catch (NullPointerException ex){}
    }
}
