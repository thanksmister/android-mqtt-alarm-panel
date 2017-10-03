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
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.view.View;

import com.thanksmister.iot.mqtt.alarmpanel.BaseActivity;
import com.thanksmister.iot.mqtt.alarmpanel.R;
import com.thanksmister.iot.mqtt.alarmpanel.ui.Configuration;
import com.thanksmister.iot.mqtt.alarmpanel.utils.DateUtils;

import butterknife.ButterKnife;
import timber.log.Timber;

import static com.thanksmister.iot.mqtt.alarmpanel.R.xml.preferences_screen_saver;
import static com.thanksmister.iot.mqtt.alarmpanel.ui.Configuration.PREF_IMAGE_FIT_SIZE;
import static com.thanksmister.iot.mqtt.alarmpanel.ui.Configuration.PREF_IMAGE_ROTATION;
import static com.thanksmister.iot.mqtt.alarmpanel.ui.Configuration.PREF_IMAGE_SOURCE;
import static com.thanksmister.iot.mqtt.alarmpanel.ui.Configuration.PREF_INACTIVITY_TIME;
import static com.thanksmister.iot.mqtt.alarmpanel.ui.Configuration.PREF_MODULE_PHOTO_SAVER;
import static com.thanksmister.iot.mqtt.alarmpanel.ui.Configuration.PREF_MODULE_SAVER;

public class ScreenSettingsFragment extends PreferenceFragmentCompat
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final int REQUEST_PERMISSIONS = 88;
    
    private CheckBoxPreference modulePreference;
    private CheckBoxPreference photoSaverPreference;
    private EditTextPreference urlPreference;
    private CheckBoxPreference imageFitPreference;
    private EditTextPreference rotationPreference;
    private ListPreference inactivityPreference;
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
    public void onDetach() {
        super.onDetach();
        ButterKnife.unbind(this);
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        
        super.onViewCreated(view, savedInstanceState);

        modulePreference = (CheckBoxPreference) findPreference(PREF_MODULE_SAVER);
        photoSaverPreference = (CheckBoxPreference) findPreference(PREF_MODULE_PHOTO_SAVER);
        urlPreference = (EditTextPreference) findPreference(PREF_IMAGE_SOURCE);
        imageFitPreference = (CheckBoxPreference) findPreference(PREF_IMAGE_FIT_SIZE);
        rotationPreference = (EditTextPreference) findPreference(PREF_IMAGE_ROTATION);
        inactivityPreference = (ListPreference) findPreference(PREF_INACTIVITY_TIME);
        
        if(isAdded()) {
            configuration = ((BaseActivity) getActivity()).getConfiguration();
        }

        urlPreference.setText(configuration.getImageSource());
        rotationPreference.setText(String.valueOf(configuration.getImageRotation()));
        rotationPreference.setSummary(getString(R.string.preference_summary_image_rotation, String.valueOf(configuration.getImageRotation())));
        urlPreference.setSummary(getString(R.string.preference_summary_image_source, configuration.getImageSource()));
        
        inactivityPreference.setDefaultValue(String.valueOf(configuration.getInactivityTime()));
        inactivityPreference.setSummary(getString(R.string.preference_summary_inactivity, 
                DateUtils.convertInactivityTime(configuration.getInactivityTime())));
        
        modulePreference.setChecked(configuration.showScreenSaverModule());
        photoSaverPreference.setEnabled(configuration.showScreenSaverModule());
        photoSaverPreference.setChecked(configuration.showPhotoScreenSaver());
        imageFitPreference.setChecked(configuration.getImageFitScreen());
        urlPreference.setEnabled(configuration.showPhotoScreenSaver());
        imageFitPreference.setEnabled(configuration.showPhotoScreenSaver());
        rotationPreference.setEnabled(configuration.showPhotoScreenSaver());
        inactivityPreference.setEnabled(configuration.showScreenSaverModule());
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        String value = "";
        switch (key) {
            case PREF_MODULE_SAVER:
                boolean checked = modulePreference.isChecked();
                configuration.setScreenSaverModule(checked);
                photoSaverPreference.setEnabled(checked);
                break;
            case PREF_MODULE_PHOTO_SAVER:
                boolean usePhotos = photoSaverPreference.isChecked();
                configuration.setPhotoScreenSaver(usePhotos);
                urlPreference.setEnabled(usePhotos);
                rotationPreference.setEnabled(usePhotos);
                imageFitPreference.setEnabled(usePhotos);
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
            case PREF_INACTIVITY_TIME:
                long inactivity = Long.valueOf(inactivityPreference.getValue());
                Timber.d("Inactivity time: " + inactivity);
                configuration.setInactivityTime(inactivity);
                inactivityPreference.setSummary(getString(R.string.preference_summary_inactivity, DateUtils.convertInactivityTime(inactivity)));
                break;
        }
    }
}