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

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.thanksmister.iot.mqtt.alarmpanel.BaseActivity;
import com.thanksmister.iot.mqtt.alarmpanel.R;
import com.thanksmister.iot.mqtt.alarmpanel.ui.Configuration;
import com.thanksmister.iot.mqtt.alarmpanel.utils.LocationUtils;

import timber.log.Timber;

import static com.thanksmister.iot.mqtt.alarmpanel.R.xml.preferences_weather;

public class WeatherSettingsFragment extends PreferenceFragmentCompat 
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final long HOUR_MILLIS = 60 * 60 * 1000;
    private static final int METERS_MIN = 500;

    private CheckBoxPreference weatherModulePreference;
    private CheckBoxPreference unitsPreference;
    private EditTextPreference weatherApiKeyPreference;
    private EditTextPreference weatherLatitude;
    private EditTextPreference weatherLongitude;
    private Configuration configuration;
    private LocationManager locationManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(preferences_weather);
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
        
        weatherModulePreference = (CheckBoxPreference) findPreference("pref_weather");
        unitsPreference = (CheckBoxPreference) findPreference("pref_units");
        weatherApiKeyPreference = (EditTextPreference) findPreference("pref_weather_api_key");
        weatherLongitude = (EditTextPreference) findPreference("pref_longitude");
        weatherLatitude  = (EditTextPreference) findPreference("pref_latitude");
        
        if(isAdded()) {
            configuration = ((BaseActivity) getActivity()).getConfiguration();
        }
        
        if(!TextUtils.isEmpty(configuration.getDarkSkyKey())) {
            weatherApiKeyPreference.setText(String.valueOf(configuration.getDarkSkyKey()));
            weatherApiKeyPreference.setSummary(String.valueOf(configuration.getDarkSkyKey()));
        }

        if(!TextUtils.isEmpty(configuration.getLatitude())) {
            weatherLatitude.setText(String.valueOf(configuration.getLatitude()));
            weatherLatitude.setSummary(String.valueOf(configuration.getLatitude()));
        }

        if(!TextUtils.isEmpty(configuration.getLongitude())) {
            weatherLongitude.setText(configuration.getLongitude());
            weatherLongitude.setSummary(configuration.getLongitude());
        }

        weatherModulePreference.setChecked(configuration.showWeatherModule());
        
        unitsPreference.setChecked(configuration.getIsCelsius());
        unitsPreference.setEnabled(configuration.showWeatherModule());
        weatherApiKeyPreference.setEnabled(configuration.showWeatherModule());
        weatherLatitude.setEnabled(configuration.showWeatherModule());
        weatherLongitude.setEnabled(configuration.showWeatherModule());
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        
        String value = "";
        switch (key) {
            case "pref_weather":
                boolean checked = weatherModulePreference.isChecked();
                Timber.d("checked: " + checked);
                configuration.setShowWeatherModule(checked);
                weatherApiKeyPreference.setEnabled(checked);
                weatherLatitude.setEnabled(checked);
                weatherLongitude.setEnabled(checked);
                unitsPreference.setEnabled(checked);
                setUpLocationMonitoring();
                break;
            case "pref_units":
                boolean useCelsius = unitsPreference.isChecked();
                configuration.setIsCelsius(useCelsius);
                break;
            case "pref_weather_api_key":
                value = weatherApiKeyPreference.getText();
                configuration.setDarkSkyKey(value);
                weatherApiKeyPreference.setSummary(value);
                break;
            case "pref_longitude":
                value = weatherLongitude.getText();
                if(LocationUtils.longitudeValid(value)) {
                    configuration.setLon(value);
                    weatherLongitude.setSummary(value);
                } else {
                    Toast.makeText(getActivity(), R.string.toast_invalid_latitude, Toast.LENGTH_SHORT).show();
                    configuration.setLon(value);
                    weatherLongitude.setSummary("");
                }
                break;
            case "pref_latitude":
                value = weatherLatitude.getText();
                if(LocationUtils.longitudeValid(value)) {
                    configuration.setLat(value);
                    weatherLatitude.setSummary(value);
                } else {
                    Toast.makeText(getActivity(), R.string.toast_invalid_longitude, Toast.LENGTH_SHORT).show();
                    configuration.setLat(value);
                    weatherLatitude.setSummary("");
                }
                break;
        }
    }
    
    private void setUpLocationMonitoring() {
        
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
       
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        
        try {
            LocationListener locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    if (location != null) {
                        String latitude = String.valueOf(location.getLatitude());
                        String longitude = String.valueOf(location.getLongitude());
                        if(LocationUtils.coordinatesValid(latitude, longitude)){
                            configuration.setLat(String.valueOf(location.getLatitude()));
                            configuration.setLon(String.valueOf(location.getLongitude()));
                            weatherLatitude.setSummary(String.valueOf(configuration.getLatitude()));
                            weatherLongitude.setSummary(configuration.getLongitude());
                        } else {
                            Toast.makeText(getActivity(), R.string.toast_invalid_coordinates, Toast.LENGTH_SHORT).show();
                        }
                        
                        locationManager.removeUpdates(this);
                    }
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {
                    Timber.d("onStatusChanged: " + status);
                }

                @Override
                public void onProviderEnabled(String provider) {
                    Timber.d("onProviderEnabled");
                }

                @Override
                public void onProviderDisabled(String provider) {
                    Timber.d("onProviderDisabled");
                }
            };
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, HOUR_MILLIS, 0, locationListener);
        } catch (SecurityException e) {
            Timber.e("Location manager could not use network provider", e);
            Toast.makeText(getActivity(), R.string.toast_invalid_provider, Toast.LENGTH_SHORT).show();
        }
    }
}