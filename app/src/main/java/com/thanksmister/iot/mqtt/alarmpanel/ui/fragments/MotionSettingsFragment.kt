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
import android.os.Bundle
import androidx.preference.EditTextPreference
import androidx.preference.PreferenceFragmentCompat
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.preference.SwitchPreference
import com.thanksmister.iot.mqtt.alarmpanel.R
import com.thanksmister.iot.mqtt.alarmpanel.persistence.Configuration
import com.thanksmister.iot.mqtt.alarmpanel.utils.DialogUtils
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class MotionSettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

    @Inject lateinit var configuration: Configuration
    @Inject lateinit var dialogUtils: DialogUtils

    private var motionPreference: SwitchPreference? = null
    private var motionWakePreference: SwitchPreference? = null
    private var motionClearPreference: EditTextPreference? = null
    private var motionLeniencyPreference: EditTextPreference? = null
    private var motionLumaPreference: EditTextPreference? = null

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preferences_motion)
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


        motionPreference = findPreference(getString(R.string.key_setting_camera_motionenabled)) as SwitchPreference
        motionPreference!!.isChecked = configuration.cameraMotionEnabled
        motionWakePreference = findPreference(getString(R.string.key_setting_camera_motionwake)) as SwitchPreference
        motionWakePreference!!.isChecked = configuration.cameraMotionWakeEnabled
        motionLeniencyPreference = findPreference(getString(R.string.key_setting_camera_motionleniency)) as EditTextPreference
        motionLeniencyPreference!!.setDefaultValue(configuration.cameraMotionLeniency.toString())
        motionLeniencyPreference!!.summary = configuration.cameraMotionLeniency.toString()
        motionLumaPreference = findPreference(getString(R.string.key_setting_camera_motionminluma)) as EditTextPreference
        motionLumaPreference!!.setDefaultValue(configuration.cameraMotionMinLuma.toString())
        motionLumaPreference!!.summary = configuration.cameraMotionMinLuma.toString()
        motionClearPreference = findPreference(getString(R.string.key_setting_motion_clear)) as EditTextPreference
        motionClearPreference!!.setDefaultValue(configuration.motionResetTime.toString())
        motionClearPreference!!.summary = configuration.motionResetTime.toString()

    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {

        when (key) {

            getString(R.string.key_setting_camera_motionenabled) -> {
                configuration.cameraMotionEnabled = motionPreference!!.isChecked
            }
            getString(R.string.key_setting_camera_motionwake) -> {
                configuration.cameraMotionWake = motionWakePreference!!.isChecked
            }

            getString(R.string.key_setting_camera_motionleniency) -> {
                try {
                    val value = motionLeniencyPreference!!.text
                    if(!TextUtils.isEmpty(value)) {
                        configuration.cameraMotionLeniency = value.toInt()
                        motionLeniencyPreference!!.summary = value
                    } else if (isAdded) {
                        Toast.makeText(activity, R.string.text_error_blank_entry, Toast.LENGTH_LONG).show()
                        motionLeniencyPreference!!.setDefaultValue(configuration.cameraMotionLeniency.toString())
                        motionLeniencyPreference!!.summary = configuration.cameraMotionLeniency.toString()
                    }
                } catch (e : Exception) {
                    if(isAdded) {
                        Toast.makeText(activity, R.string.text_error_only_numbers, Toast.LENGTH_LONG).show()
                        motionLeniencyPreference!!.setDefaultValue(configuration.cameraMotionLeniency.toString())
                        motionLeniencyPreference!!.summary = configuration.cameraMotionLeniency.toString()
                    }
                }
            }
            getString(R.string.key_setting_camera_motionminluma) -> {
                try {
                    val value = motionLumaPreference!!.text
                    if(!TextUtils.isEmpty(value)) {
                        configuration.cameraMotionMinLuma = value.toInt()
                        motionLumaPreference!!.summary = value
                    } else if (isAdded) {
                        Toast.makeText(activity, R.string.text_error_blank_entry, Toast.LENGTH_LONG).show()
                        motionLumaPreference!!.setDefaultValue(configuration.cameraMotionMinLuma.toString())
                        motionLumaPreference!!.summary = configuration.cameraMotionMinLuma.toString()
                    }
                } catch (e : Exception) {
                    if(isAdded) {
                        Toast.makeText(activity, R.string.text_error_only_numbers, Toast.LENGTH_LONG).show()
                        motionLumaPreference!!.setDefaultValue(configuration.cameraMotionMinLuma.toString())
                        motionLumaPreference!!.summary = configuration.cameraMotionMinLuma.toString()
                    }
                }
            }
            getString(R.string.key_setting_motion_clear) -> {
                try {
                    val value = motionClearPreference!!.text
                    if(!TextUtils.isEmpty(value)) {
                        configuration.motionResetTime = value.toInt()
                        motionClearPreference!!.summary = value
                    } else if (isAdded) {
                        Toast.makeText(activity, R.string.text_error_blank_entry, Toast.LENGTH_LONG).show()
                        motionClearPreference!!.setDefaultValue(configuration.motionResetTime.toString())
                        motionClearPreference!!.summary = configuration.motionResetTime.toString()
                    }
                } catch (e : Exception) {
                    if(isAdded) {
                        Toast.makeText(activity, R.string.text_error_only_numbers, Toast.LENGTH_LONG).show()
                        motionClearPreference!!.setDefaultValue(configuration.motionResetTime.toString())
                        motionClearPreference!!.summary = configuration.motionResetTime.toString()
                    }
                }
            }
        }
    }
}