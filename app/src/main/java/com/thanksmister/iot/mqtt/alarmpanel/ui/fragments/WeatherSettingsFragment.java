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
import com.thanksmister.iot.mqtt.alarmpanel.network.DarkSkyOptions;
import com.thanksmister.iot.mqtt.alarmpanel.ui.Configuration;
import com.thanksmister.iot.mqtt.alarmpanel.utils.LocationUtils;

import butterknife.ButterKnife;
import timber.log.Timber;

import static com.thanksmister.iot.mqtt.alarmpanel.R.xml.preferences_weather;
import static com.thanksmister.iot.mqtt.alarmpanel.network.DarkSkyOptions.PREF_DARK_SKY_KEY;
import static com.thanksmister.iot.mqtt.alarmpanel.network.DarkSkyOptions.PREF_WEATHER_LAT;
import static com.thanksmister.iot.mqtt.alarmpanel.network.DarkSkyOptions.PREF_WEATHER_LON;
import static com.thanksmister.iot.mqtt.alarmpanel.network.DarkSkyOptions.PREF_WEATHER_UNITS;
import static com.thanksmister.iot.mqtt.alarmpanel.ui.Configuration.PREF_MODULE_WEATHER;

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
    private DarkSkyOptions weatherOptions;
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
        if( (TextUtils.isEmpty(weatherOptions.getLongitude()) || TextUtils.isEmpty(weatherOptions.getLatitude()))  
                && configuration.showPhotoScreenSaver()) {
            checkLocationEnabled(); // check that we have location permissions 
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }
    
    @Override
    public void onDetach() {
        super.onDetach();
        if(locationHandler != null) {
            locationHandler.removeCallbacks(locationRunnable);
        }
        if(locationManager != null) {
            locationManager.removeUpdates(locationListener);
        }
        ButterKnife.unbind(this);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        
        super.onViewCreated(view, savedInstanceState);
        
        weatherModulePreference = (CheckBoxPreference) findPreference(PREF_MODULE_WEATHER);
        unitsPreference = (CheckBoxPreference) findPreference(PREF_WEATHER_UNITS);
        weatherApiKeyPreference = (EditTextPreference) findPreference(PREF_DARK_SKY_KEY);
        weatherLongitude = (EditTextPreference) findPreference(PREF_WEATHER_LAT);
        weatherLatitude  = (EditTextPreference) findPreference(PREF_WEATHER_LON);
        
        if(isAdded()) {
            configuration = ((BaseActivity) getActivity()).getConfiguration();
            weatherOptions = ((BaseActivity) getActivity()).readWeatherOptions();
        }
        
        if(!TextUtils.isEmpty(weatherOptions.getDarkSkyKey())) {
            weatherApiKeyPreference.setText(String.valueOf(weatherOptions.getDarkSkyKey()));
            weatherApiKeyPreference.setSummary(String.valueOf(weatherOptions.getDarkSkyKey()));
        }

        if(!TextUtils.isEmpty(weatherOptions.getLatitude())) {
            weatherLatitude.setText(String.valueOf(weatherOptions.getLatitude()));
            weatherLatitude.setSummary(String.valueOf(weatherOptions.getLatitude()));
        }

        if(!TextUtils.isEmpty(weatherOptions.getLongitude())) {
            weatherLongitude.setText(weatherOptions.getLongitude());
            weatherLongitude.setSummary(weatherOptions.getLongitude());
        }

        weatherModulePreference.setChecked(configuration.showWeatherModule());
        unitsPreference.setChecked(weatherOptions.getIsCelsius());
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
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSIONS);
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
            case PREF_MODULE_WEATHER:
                boolean checked = weatherModulePreference.isChecked();
                configuration.setShowWeatherModule(checked);
                weatherApiKeyPreference.setEnabled(checked);
                weatherLatitude.setEnabled(checked);
                weatherLongitude.setEnabled(checked);
                unitsPreference.setEnabled(checked);
                if(checked) {
                    checkLocationEnabled(); // check that we have location permissions
                }
                break;
            case PREF_WEATHER_UNITS:
                boolean useCelsius = unitsPreference.isChecked();
                weatherOptions.setIsCelsius(useCelsius);
                break;
            case PREF_DARK_SKY_KEY:
                value = weatherApiKeyPreference.getText();
                weatherOptions.setDarkSkyKey(value);
                weatherApiKeyPreference.setSummary(value);
                break;
            case PREF_WEATHER_LON:
                value = weatherLongitude.getText();
                if(LocationUtils.longitudeValid(value)) {
                    weatherOptions.setLon(value);
                    weatherLongitude.setSummary(value);
                } else {
                    Toast.makeText(getActivity(), R.string.toast_invalid_latitude, Toast.LENGTH_SHORT).show();
                    weatherOptions.setLon(value);
                    weatherLongitude.setSummary("");
                }
                break;
            case PREF_WEATHER_LAT:
                value = weatherLatitude.getText();
                if(LocationUtils.longitudeValid(value)) {
                    weatherOptions.setLat(value);
                    weatherLatitude.setSummary(value);
                } else {
                    Toast.makeText(getActivity(), R.string.toast_invalid_longitude, Toast.LENGTH_SHORT).show();
                    weatherOptions.setLat(value);
                    weatherLatitude.setSummary("");
                }
                break;
        }
    }

    final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if (location != null) {
                if(isAdded()) {
                    ((BaseActivity) getActivity()).hideProgressDialog();
                    String latitude = String.valueOf(location.getLatitude());
                    String longitude = String.valueOf(location.getLongitude());
                    if (LocationUtils.coordinatesValid(latitude, longitude)) {
                        Timber.d("setUpLocationMonitoring complete");
                        Timber.d("setUpLocationMonitoring latitude: " + latitude);
                        Timber.d("setUpLocationMonitoring longitude: " + longitude);
                        weatherOptions.setLat(String.valueOf(location.getLatitude()));
                        weatherOptions.setLon(String.valueOf(location.getLongitude()));
                        weatherLatitude.setSummary(String.valueOf(location.getLatitude()));
                        weatherLongitude.setSummary(String.valueOf(location.getLongitude()));
                    } else {
                        Toast.makeText(getActivity(), R.string.toast_invalid_coordinates, Toast.LENGTH_SHORT).show();
                    }
                    locationManager.removeUpdates(this);
                }
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
    
    private void setUpLocationMonitoring() {
        Timber.d("setUpLocationMonitoring");
        if(isAdded()) {
            ((BaseActivity) getActivity()).showProgressDialog(getString(R.string.progress_location), false);
            locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
            if(locationManager != null) {
                boolean gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                boolean network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
                try {
                    if (network_enabled) {
                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
                    } else if (gps_enabled) {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, HOUR_MILLIS, METERS_MIN, locationListener);
                    }
                } catch (SecurityException e) {
                    Timber.e("Location manager could not use network provider: " + e.getMessage());
                    ((BaseActivity) getActivity()).hideProgressDialog();
                    Toast.makeText(getActivity(), R.string.toast_invalid_provider, Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getActivity(), R.string.toast_invalid_provider, Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    private Runnable locationRunnable = new Runnable() {
        @Override
        public void run() {
            if (isAdded()) { // Without this in certain cases application will show ANR
                ((BaseActivity) getActivity()).hideProgressDialog();
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