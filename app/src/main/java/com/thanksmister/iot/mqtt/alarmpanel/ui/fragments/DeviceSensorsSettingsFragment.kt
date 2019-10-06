/*
 * Copyright (c) 2018 ThanksMister LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.thanksmister.iot.mqtt.alarmpanel.ui.fragments

import android.content.Context
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import androidx.preference.SwitchPreference
import androidx.preference.*
import android.text.TextUtils
import android.view.View
import android.widget.Toast

import com.thanksmister.iot.mqtt.alarmpanel.R
import com.thanksmister.iot.mqtt.alarmpanel.persistence.Configuration

import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class DeviceSensorsSettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

    @Inject lateinit var configuration: Configuration

    private var sensorsPreference: SwitchPreference? = null
    private var sensorPublishFrequency: EditTextPreference? = null

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preference_device_sensors)
    }

    override fun onResume() {
        super.onResume()
        preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        sensorsPreference = findPreference(getString(R.string.key_setting_sensors_enabled)) as SwitchPreference
        sensorsPreference!!.isChecked = configuration.deviceSensors

        sensorPublishFrequency = findPreference(getString(R.string.key_setting_mqtt_sensorfrequency)) as EditTextPreference
        sensorPublishFrequency!!.setDefaultValue(configuration.deviceSensorFrequency.toString())
        sensorPublishFrequency!!.summary = configuration.deviceSensorFrequency.toString()

        val mSensorManager = activity!!.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        setSensorPreferenceSummary(findPreference(getString(R.string.key_settings_sensors_temperature)), mSensorManager.getSensorList(Sensor.TYPE_AMBIENT_TEMPERATURE));
        setSensorPreferenceSummary(findPreference(getString(R.string.key_settings_sensors_light)), mSensorManager.getSensorList(Sensor.TYPE_LIGHT));
        setSensorPreferenceSummary(findPreference(getString(R.string.key_settings_sensors_magneticField)), mSensorManager.getSensorList(Sensor.TYPE_MAGNETIC_FIELD));
        setSensorPreferenceSummary(findPreference(getString(R.string.key_settings_sensors_pressure)), mSensorManager.getSensorList(Sensor.TYPE_PRESSURE));
        setSensorPreferenceSummary(findPreference(getString(R.string.key_settings_sensors_humidity)), mSensorManager.getSensorList(Sensor.TYPE_RELATIVE_HUMIDITY));
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when (key) {
            getString(R.string.key_setting_mqtt_sensorfrequency) -> {
                try {
                    val value = sensorPublishFrequency!!.text
                    if(!TextUtils.isEmpty(value)) {
                        configuration.deviceSensorFrequency = value.toInt()
                        sensorPublishFrequency!!.summary = value
                    } else if (isAdded) {
                        Toast.makeText(activity, R.string.text_error_blank_entry, Toast.LENGTH_LONG).show()
                        sensorPublishFrequency!!.setDefaultValue(configuration.deviceSensorFrequency.toString())
                        sensorPublishFrequency!!.setSummary(configuration.deviceSensorFrequency.toString())
                    }
                } catch (e : Exception) {
                    if(isAdded) {
                        Toast.makeText(activity, R.string.text_error_only_numbers, Toast.LENGTH_LONG).show()
                        sensorPublishFrequency!!.setDefaultValue(configuration.deviceSensorFrequency.toString())
                        sensorPublishFrequency!!.setSummary(configuration.deviceSensorFrequency.toString())
                    }
                }
            }
            getString(R.string.key_setting_sensors_enabled) -> {
                val checked = sensorsPreference!!.isChecked
                configuration.deviceSensors = checked
            }
        }
    }

    private fun setSensorPreferenceSummary(preference: Preference, sensorList: List<Sensor>) {
        if (sensorList.isNotEmpty()) { // Could we have multiple sensors of same type?
            preference.summary = sensorList[0].name
        }
    }
}