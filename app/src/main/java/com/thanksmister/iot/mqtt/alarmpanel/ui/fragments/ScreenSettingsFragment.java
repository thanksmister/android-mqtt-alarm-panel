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
import android.view.View;

import com.thanksmister.iot.mqtt.alarmpanel.BaseActivity;
import com.thanksmister.iot.mqtt.alarmpanel.R;
import com.thanksmister.iot.mqtt.alarmpanel.ui.Configuration;

import static com.thanksmister.iot.mqtt.alarmpanel.R.xml.preferences_screen_saver;
import static com.thanksmister.iot.mqtt.alarmpanel.ui.Configuration.PREF_IMAGE_FIT_SIZE;
import static com.thanksmister.iot.mqtt.alarmpanel.ui.Configuration.PREF_IMAGE_ROTATION;
import static com.thanksmister.iot.mqtt.alarmpanel.ui.Configuration.PREF_IMAGE_SOURCE;
import static com.thanksmister.iot.mqtt.alarmpanel.ui.Configuration.PREF_MODULE_SAVER;

public class ScreenSettingsFragment extends PreferenceFragmentCompat
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    private CheckBoxPreference modulePreference;
    private EditTextPreference urlPreference;
    private CheckBoxPreference imageFitPreference;
    private EditTextPreference rotationPreference;
    private Configuration configuration;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(preferences_screen_saver);
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
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
    

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        
        super.onViewCreated(view, savedInstanceState);

        modulePreference = (CheckBoxPreference) findPreference(PREF_MODULE_SAVER);
        urlPreference = (EditTextPreference) findPreference(PREF_IMAGE_SOURCE);
        imageFitPreference = (CheckBoxPreference) findPreference(PREF_IMAGE_FIT_SIZE);
        rotationPreference = (EditTextPreference) findPreference(PREF_IMAGE_ROTATION);
        
        if(isAdded()) {
            configuration = ((BaseActivity) getActivity()).getConfiguration();
        }

        urlPreference.setText(configuration.getImageSource());
        rotationPreference.setText(String.valueOf(configuration.getImageRotation()));

        urlPreference.setSummary(getString(R.string.preference_summary_image_source, configuration.getImageSource()));
        rotationPreference.setSummary(getString(R.string.preference_summary_image_rotation, String.valueOf(configuration.getImageRotation())));

        modulePreference.setChecked(configuration.showScreenSaverModule());
        imageFitPreference.setChecked(configuration.getImageFitScreen());
        urlPreference.setEnabled(configuration.showScreenSaverModule());
        imageFitPreference.setEnabled(configuration.showScreenSaverModule());
        rotationPreference.setEnabled(configuration.showScreenSaverModule());
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        
        String value = "";
        switch (key) {
            case PREF_MODULE_SAVER:
                boolean checked = modulePreference.isChecked();
                configuration.setScreenSaverModule(checked);
                urlPreference.setEnabled(checked);
                rotationPreference.setEnabled(checked);
                imageFitPreference.setEnabled(checked);
                break;
            case PREF_IMAGE_SOURCE:
                value = urlPreference.getText();
                configuration.setImageSource(value);
                urlPreference.setSummary(getString(R.string.preference_summary_image_source, value));
                break;
            case PREF_IMAGE_FIT_SIZE:
                boolean fitScreen = imageFitPreference.isChecked();
                configuration.setImageFitScreen(fitScreen);
                break;
            case PREF_IMAGE_ROTATION:
                int rotation = Integer.valueOf(rotationPreference.getText());
                configuration.setImageRotation(rotation);
                rotationPreference.setSummary(getString(R.string.preference_summary_image_rotation, String.valueOf(rotation)));
                break;
        }
    }
}