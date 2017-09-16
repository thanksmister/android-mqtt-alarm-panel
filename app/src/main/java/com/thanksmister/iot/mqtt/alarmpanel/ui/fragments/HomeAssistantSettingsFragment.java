/*
 * <!--
 *   ~ Copyright (c) 2017. ThanksMister LLC
 *   ~
 *   ~ Licensed under the Apache License, Version 2.0 (the "License");
 *   ~ you may not use this file except in compliance with the License. 
 *   ~ You may obtain a copy of the License at
 *   ~
 *   ~ http://www.apache.org/licenses/LICENSE-2.0
 *   ~
 *   ~ Unless required by applicable law or agreed to in writing, software distributed 
 *   ~ under the License is distributed on an "AS IS" BASIS, 
 *   ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 *   ~ See the License for the specific language governing permissions and 
 *   ~ limitations under the License.
 *   -->
 */

package com.thanksmister.iot.mqtt.alarmpanel.ui.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.text.TextUtils;
import android.view.View;

import com.thanksmister.iot.mqtt.alarmpanel.BaseActivity;
import com.thanksmister.iot.mqtt.alarmpanel.ui.Configuration;

import butterknife.ButterKnife;
import timber.log.Timber;

import static com.thanksmister.iot.mqtt.alarmpanel.R.xml.preferences_hass;

public class HomeAssistantSettingsFragment extends PreferenceFragmentCompat 
        implements SharedPreferences.OnSharedPreferenceChangeListener {
    
    private CheckBoxPreference hassModulePreference;
    private EditTextPreference hassUrlPreference;
    private Configuration configuration;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(preferences_hass);
    }
    
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
    }
    
    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }
    
    @Override
    public void onDetach() {
        super.onDetach();
        ButterKnife.unbind(this);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        
        super.onViewCreated(view, savedInstanceState);

        hassModulePreference = (CheckBoxPreference) findPreference("pref_module_hass");
        hassUrlPreference = (EditTextPreference) findPreference("pref_hass_web_url");
        
        if(isAdded()) {
            configuration = ((BaseActivity) getActivity()).getConfiguration();
        }

        if(!TextUtils.isEmpty(configuration.getHassUrl())) {
            hassUrlPreference.setText(configuration.getHassUrl());
            hassUrlPreference.setSummary(configuration.getHassUrl());
        }

        hassModulePreference.setChecked(configuration.showHassModule());
        hassUrlPreference.setEnabled(configuration.showHassModule());
    }
    
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        
        String value = "";
        switch (key) {
            case "pref_module_hass":
                boolean checked = hassModulePreference.isChecked();
                Timber.d("checked: " + checked);
                configuration.setShowHassModule(checked);
                hassUrlPreference.setEnabled(checked);
                break;
            case "pref_hass_web_url":
                value = hassUrlPreference.getText();
                configuration.setHassUrl(value);
                hassUrlPreference.setText(value);
                hassUrlPreference.setSummary(value);
                break;
        }
    }
}