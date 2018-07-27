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
import android.support.v7.preference.CheckBoxPreference
import android.support.v7.preference.EditTextPreference
import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceFragmentCompat
import android.text.TextUtils
import android.view.View
import android.widget.Toast

import com.thanksmister.iot.mqtt.alarmpanel.R
import com.thanksmister.iot.mqtt.alarmpanel.network.MQTTOptions
import com.thanksmister.iot.mqtt.alarmpanel.persistence.Configuration

import com.thanksmister.iot.mqtt.alarmpanel.network.MQTTOptions.Companion.PREF_NOTIFICATION_TOPIC
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class NotificationsSettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

    @Inject lateinit var configuration: Configuration
    @Inject lateinit var mqttOptions: MQTTOptions

    private var systemPreference: CheckBoxPreference? = null
    private var soundPreference: CheckBoxPreference? = null

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preferences_notifications)
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

        soundPreference = findPreference(Configuration.PREF_SYSTEM_SOUNDS) as CheckBoxPreference
        systemPreference = findPreference(Configuration.PREF_SYSTEM_NOTIFICATIONS) as CheckBoxPreference

        systemPreference!!.isChecked = configuration.hasSystemAlerts()
        soundPreference!!.isChecked = configuration.systemSounds
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when (key) {
            Configuration.PREF_SYSTEM_SOUNDS -> {
                val sounds = soundPreference!!.isChecked
                configuration.systemSounds = sounds
            }
            Configuration.PREF_SYSTEM_NOTIFICATIONS -> {
                val checked = systemPreference!!.isChecked
                configuration.systemAlerts = checked
            }
        }
    }
}