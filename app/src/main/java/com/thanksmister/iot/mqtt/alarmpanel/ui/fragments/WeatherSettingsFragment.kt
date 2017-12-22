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

package com.thanksmister.iot.mqtt.alarmpanel.ui.fragments

import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AlertDialog
import android.support.v7.preference.CheckBoxPreference
import android.support.v7.preference.EditTextPreference
import android.support.v7.preference.PreferenceFragmentCompat
import android.text.TextUtils
import android.view.View
import android.widget.Toast

import com.thanksmister.iot.mqtt.alarmpanel.BaseActivity
import com.thanksmister.iot.mqtt.alarmpanel.R
import com.thanksmister.iot.mqtt.alarmpanel.network.DarkSkyOptions
import com.thanksmister.iot.mqtt.alarmpanel.ui.Configuration
import com.thanksmister.iot.mqtt.alarmpanel.utils.LocationUtils

import timber.log.Timber

import com.thanksmister.iot.mqtt.alarmpanel.R.xml.preferences_weather
import com.thanksmister.iot.mqtt.alarmpanel.utils.DialogUtils
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class WeatherSettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

    @Inject lateinit var dialogUtils: DialogUtils
    @Inject lateinit var configuration: Configuration

    private var weatherModulePreference: CheckBoxPreference? = null
    private var unitsPreference: CheckBoxPreference? = null
    private var weatherApiKeyPreference: EditTextPreference? = null
    private var weatherLatitude: EditTextPreference? = null
    private var weatherLongitude: EditTextPreference? = null
    private var weatherOptions: DarkSkyOptions? = null
    private var locationManager: LocationManager? = null
    private var locationHandler: Handler? = null

    internal val locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location?) {
            if (location != null) {
                if (isAdded) {
                    dialogUtils.hideProgressDialog()
                    val latitude = location.latitude.toString()
                    val longitude = location.longitude.toString()
                    if (LocationUtils.coordinatesValid(latitude, longitude)) {
                        weatherOptions!!.setLat(location.latitude.toString())
                        weatherOptions!!.setLon(location.longitude.toString())
                        weatherLatitude!!.summary = weatherOptions!!.latitude.toString()
                        weatherLongitude!!.summary = weatherOptions!!.longitude
                    } else {
                        Toast.makeText(activity, R.string.toast_invalid_coordinates, Toast.LENGTH_SHORT).show()
                    }
                    locationManager!!.removeUpdates(this)
                }
            }
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
            Timber.d("onStatusChanged: " + status)
        }

        override fun onProviderEnabled(provider: String) {
            Timber.d("onProviderEnabled")
        }

        override fun onProviderDisabled(provider: String) {
            Timber.d("onProviderDisabled")
            locationHandler = Handler()
            locationHandler!!.postDelayed(locationRunnable, 500)
        }
    }

    private val locationRunnable = Runnable {
        if (isAdded) { // Without this in certain cases application will show ANR
            //dialogUtils.hideProgressDialog()
            val builder = AlertDialog.Builder(activity!!)
            builder.setMessage(R.string.string_location_services_disabled).setCancelable(false).setPositiveButton(android.R.string.ok) { _, _ ->
                val gpsOptionsIntent = Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(gpsOptionsIntent)
            }
            builder.setNegativeButton(android.R.string.cancel) { dialog, _ -> dialog.cancel() }
            val alert = builder.create()
            alert.show()
        }
    }

    override fun onAttach(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Perform injection here for M (API 23) due to deprecation of onAttach(Activity).
            AndroidSupportInjection.inject(this)
        }
        super.onAttach(context)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(preferences_weather)
    }

    override fun onResume() {
        super.onResume()
        preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onDetach() {
        super.onDetach()
        if (locationHandler != null) {
            locationHandler!!.removeCallbacks(locationRunnable)
        }
        if (locationManager != null) {
            locationManager!!.removeUpdates(locationListener)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        weatherModulePreference = findPreference("pref_weather") as CheckBoxPreference
        unitsPreference = findPreference("pref_units") as CheckBoxPreference
        weatherApiKeyPreference = findPreference("pref_weather_api_key") as EditTextPreference
        weatherLongitude = findPreference("pref_longitude") as EditTextPreference
        weatherLatitude = findPreference("pref_latitude") as EditTextPreference

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(activity as BaseActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(activity as BaseActivity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                weatherModulePreference!!.isEnabled = false
                unitsPreference!!.isEnabled = false
                weatherApiKeyPreference!!.isEnabled = false
                weatherLongitude!!.isEnabled = false
                weatherLatitude!!.isEnabled = false
                configuration.setShowWeatherModule(false)
                dialogUtils.showAlertDialog(activity as BaseActivity, getString(R.string.dialog_no_location_permissions))
                return
            }
        }

        if (isAdded && activity != null) {
            weatherOptions = (activity as BaseActivity).readWeatherOptions()
        }

        if (!TextUtils.isEmpty(weatherOptions!!.darkSkyKey)) {
            weatherApiKeyPreference!!.text = weatherOptions!!.darkSkyKey.toString()
            weatherApiKeyPreference!!.summary = weatherOptions!!.darkSkyKey.toString()
        }

        if (!TextUtils.isEmpty(weatherOptions!!.latitude)) {
            weatherLatitude!!.text = weatherOptions!!.latitude.toString()
            weatherLatitude!!.summary = weatherOptions!!.latitude.toString()
        }

        if (!TextUtils.isEmpty(weatherOptions!!.longitude)) {
            weatherLongitude!!.text = weatherOptions!!.longitude
            weatherLongitude!!.summary = weatherOptions!!.longitude
        }

        weatherModulePreference!!.isChecked = configuration.showWeatherModule()

        unitsPreference!!.isChecked = weatherOptions!!.getIsCelsius()
        unitsPreference!!.isEnabled = configuration.showWeatherModule()
        weatherApiKeyPreference!!.isEnabled = configuration.showWeatherModule()
        weatherLatitude!!.isEnabled = configuration.showWeatherModule()
        weatherLongitude!!.isEnabled = configuration.showWeatherModule()
    }

    // TODO (make these all constants in the configuration)
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when (key) {
            "pref_weather" -> {
                val checked = weatherModulePreference!!.isChecked
                configuration.setShowWeatherModule(checked)
                weatherApiKeyPreference!!.isEnabled = checked
                weatherLatitude!!.isEnabled = checked
                weatherLongitude!!.isEnabled = checked
                unitsPreference!!.isEnabled = checked
                if (checked) {
                    setUpLocationMonitoring()
                }
            }
            "pref_units" -> {
                val useCelsius = unitsPreference!!.isChecked
                weatherOptions!!.setIsCelsius(useCelsius)
            }
            "pref_weather_api_key" -> {
                val value = weatherApiKeyPreference!!.text
                weatherOptions!!.darkSkyKey = value
                weatherApiKeyPreference!!.summary = value
            }
            "pref_longitude" -> {
                val value = weatherLongitude!!.text
                if (LocationUtils.longitudeValid(value)) {
                    weatherOptions!!.setLon(value)
                    weatherLongitude!!.summary = value
                } else {
                    Toast.makeText(activity, R.string.toast_invalid_latitude, Toast.LENGTH_SHORT).show()
                    weatherOptions!!.setLon(value)
                    weatherLongitude!!.summary = ""
                }
            }
            "pref_latitude" -> {
                val value = weatherLatitude!!.text
                if (LocationUtils.longitudeValid(value)) {
                    weatherOptions!!.setLat(value)
                    weatherLatitude!!.summary = value
                } else {
                    Toast.makeText(activity, R.string.toast_invalid_longitude, Toast.LENGTH_SHORT).show()
                    weatherOptions!!.setLat(value)
                    weatherLatitude!!.summary = ""
                }
            }
        }
    }

    private fun setUpLocationMonitoring() {
        Timber.d("setUpLocationMonitoring")
        if (isAdded) {
            dialogUtils.showProgressDialog(getString(R.string.progress_location), false)
            locationManager = activity!!.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val criteria = Criteria()
            criteria.accuracy = Criteria.ACCURACY_COARSE
            try {
                if (locationManager!!.allProviders.contains(LocationManager.NETWORK_PROVIDER)) {
                    locationManager!!.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, HOUR_MILLIS, METERS_MIN.toFloat(), locationListener)
                }
                if (locationManager!!.allProviders.contains(LocationManager.GPS_PROVIDER)) {
                    locationManager!!.requestLocationUpdates(LocationManager.GPS_PROVIDER, HOUR_MILLIS, METERS_MIN.toFloat(), locationListener)
                }
            } catch (e: SecurityException) {
                Timber.e("Location manager could not use network provider", e)
                dialogUtils.hideProgressDialog()
                Toast.makeText(activity, R.string.toast_invalid_provider, Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private val HOUR_MILLIS = (60 * 60 * 1000).toLong()
        private val METERS_MIN = 500
    }
}