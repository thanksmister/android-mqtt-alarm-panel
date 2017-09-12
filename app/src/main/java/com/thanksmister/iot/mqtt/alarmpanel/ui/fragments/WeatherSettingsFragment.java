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

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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

    private static final int REQUEST_PERMISSIONS = 6;
    private static final long HOUR_MILLIS = 60 * 60 * 1000;
    private static final int METERS_MIN = 500;

    private CheckBoxPreference weatherModulePreference;
    private CheckBoxPreference unitsPreference;
    private EditTextPreference weatherApiKeyPreference;
    private EditTextPreference weatherLatitude;
    private EditTextPreference weatherLongitude;
    private Configuration configuration;
    private LocationManager locationManager;
    private Handler locationHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        addPreferencesFromResource(preferences_weather);
    }
    
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.search, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_location:
                checkLocationEnabled();
                return true;
        }
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        if(TextUtils.isEmpty(configuration.getLongitude()) || TextUtils.isEmpty(configuration.getLatitude()) ) {
            checkLocationEnabled(); // check that we have location permissions 
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(locationHandler != null) {
            locationHandler.removeCallbacks(locationRunnable);
        }
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

    public void checkLocationEnabled() {
        if (isAdded() && getActivity() != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_PERMISSIONS);
                    return;
                }
            }
            setUpLocationMonitoring();
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSIONS: {
                if (grantResults.length > 0) {
                    boolean permissionsDenied = false;
                    for (int permission : grantResults) {
                        if (permission != PackageManager.PERMISSION_GRANTED) {
                            permissionsDenied = true;
                            break;
                        }
                    }
                    if (!permissionsDenied) {
                        setUpLocationMonitoring();
                    } 
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
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
        
        Timber.d("setUpLocationMonitoring");
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        
        try {
            final LocationListener locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    if (location != null) {
                        String latitude = String.valueOf(location.getLatitude());
                        String longitude = String.valueOf(location.getLongitude());
                        if(LocationUtils.coordinatesValid(latitude, longitude)){
                            Timber.d("setUpLocationMonitoring complete");
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
                    locationHandler = new Handler();
                    locationHandler.postDelayed(locationRunnable, 500);
                }
            };
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, HOUR_MILLIS, 0, locationListener);
        } catch (SecurityException e) {
            Timber.e("Location manager could not use network provider", e);
            Toast.makeText(getActivity(), R.string.toast_invalid_provider, Toast.LENGTH_SHORT).show();
        }
    }
    
    private Runnable locationRunnable = new Runnable() {
        @Override
        public void run() {
            if (isAdded()) { // Without this in certain cases application will show ANR
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(R.string.string_location_services_disabled).setCancelable(false).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent gpsOptionsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(gpsOptionsIntent);
                    }
                });
                builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
            }
        }
    };
}