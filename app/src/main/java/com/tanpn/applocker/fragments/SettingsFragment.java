package com.tanpn.applocker.fragments;


import android.os.Bundle;
import android.preference.ListPreference;

import android.support.v4.app.Fragment;

import android.support.v4.preference.PreferenceFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.tanpn.applocker.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends PreferenceFragment {

    private ListPreference mListPreference;

    public SettingsFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        addPreferencesFromResource(R.xml.settings);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        /*mListPreference = (ListPreference)  getPreferenceManager().findPreference("preference_key");
        mListPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                return false;
            }
        });
*/
        return super.onCreateView(inflater, container,
                savedInstanceState);
    }


    @Override
    public boolean onPreferenceTreeClick() {
        return false;
    }
}
