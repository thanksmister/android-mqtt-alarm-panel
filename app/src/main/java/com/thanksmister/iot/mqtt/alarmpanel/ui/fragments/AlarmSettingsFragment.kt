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

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.preference.EditTextPreference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import com.thanksmister.iot.mqtt.alarmpanel.R
import com.thanksmister.iot.mqtt.alarmpanel.network.MQTTOptions
import com.thanksmister.iot.mqtt.alarmpanel.persistence.Configuration
import com.thanksmister.iot.mqtt.alarmpanel.ui.activities.SettingsActivity
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class AlarmSettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

    @Inject
    lateinit var configuration: Configuration

    @Inject
    lateinit var mqttOptions: MQTTOptions

    private val remoteCodeSwitchPreference: SwitchPreference by lazy {
        findPreference(PREF_ALARM_REMOTE_CODE) as SwitchPreference
    }

    private val awayModePreference: SwitchPreference by lazy {
        findPreference("pref_mode_away") as SwitchPreference
    }

    private val homeModePreference: SwitchPreference by lazy {
        findPreference("pref_mode_home") as SwitchPreference
    }

    private val nightModePreference: SwitchPreference by lazy {
        findPreference("pref_mode_night") as SwitchPreference
    }

    private val customBypassModePreference: SwitchPreference by lazy {
        findPreference("pref_mode_custom_bypass") as SwitchPreference
    }

    private val pendingAwayPreference: EditTextPreference by lazy {
        findPreference(PREF_AWAY_PENDING_TIME) as EditTextPreference
    }

    private val pendingHomePreference: EditTextPreference by lazy {
        findPreference(PREF_HOME_PENDING_TIME) as EditTextPreference
    }

    private val pendingNightPreference: EditTextPreference by lazy {
        findPreference(PREF_NIGHT_PENDING_TIME) as EditTextPreference
    }

    private val pendingBypassPreference: EditTextPreference by lazy {
        findPreference(PREF_BYPASS_PENDING_TIME) as EditTextPreference
    }

    private val delayAwayPreference: EditTextPreference by lazy {
        findPreference(PREF_AWAY_DELAY_TIME) as EditTextPreference
    }

    private val delayHomePreference: EditTextPreference by lazy {
        findPreference(PREF_HOME_DELAY_TIME) as EditTextPreference
    }

    private val delayNightPreference: EditTextPreference by lazy {
        findPreference(PREF_NIGHT_DELAY_TIME) as EditTextPreference
    }

    private val delayBypassPreference: EditTextPreference by lazy {
        findPreference(PREF_BYPASS_DELAY_TIME) as EditTextPreference
    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
        (activity as SettingsActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (activity as SettingsActivity).supportActionBar?.setDisplayShowHomeEnabled(true)
        (activity as SettingsActivity).supportActionBar?.title = (getString(R.string.preference_title_alarm))
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.alarm_preferences)
    }

    override fun onResume() {
        super.onResume()
        preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        awayModePreference.isChecked = mqttOptions.alarmModeAway
        homeModePreference.isChecked = mqttOptions.alarmModeHome
        nightModePreference.isChecked = mqttOptions.alarmModeNight
        customBypassModePreference.isChecked = mqttOptions.alarmModeCustomBypass

        pendingAwayPreference.text = mqttOptions.pendingTimeAway.toString()
        pendingAwayPreference.summary = getString(R.string.pref_away_pending_summary, mqttOptions.pendingTimeAway.toString())
        pendingHomePreference.text = mqttOptions.pendingTimeHome.toString()
        pendingHomePreference.summary = getString(R.string.pref_home_pending_summary, mqttOptions.pendingTimeHome.toString())
        pendingNightPreference.text = mqttOptions.pendingTimeNight.toString()
        pendingNightPreference.summary = getString(R.string.preference_summary_night_pending_time, mqttOptions.pendingTimeNight.toString())
        pendingBypassPreference.text = mqttOptions.pendingTimeBypass.toString()
        pendingBypassPreference.summary = getString(R.string.pref_bypass_pending_summary, mqttOptions.pendingTimeBypass.toString())

        delayAwayPreference.text = mqttOptions.delayTimeAway.toString()
        delayAwayPreference.summary = getString(R.string.pref_away_delay_summary, mqttOptions.delayTimeAway.toString())
        delayHomePreference.text = mqttOptions.delayTimeHome.toString()
        delayHomePreference.summary = getString(R.string.pref_home_delay_summary, mqttOptions.delayTimeHome.toString())
        delayNightPreference.text = mqttOptions.delayTimeNight.toString()
        delayNightPreference.summary = getString(R.string.pref_night_delay_summary, mqttOptions.delayTimeNight.toString())
        delayBypassPreference.text = mqttOptions.delayTimeBypass.toString()
        delayBypassPreference.summary = getString(R.string.pref_bypass_delay_summary, mqttOptions.delayTimeBypass.toString())

        remoteCodeSwitchPreference.isChecked = mqttOptions.useRemoteCode
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    @SuppressLint("InlinedApi")
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        val value: String
        when (key) {

            "pref_mode_home" -> {
                mqttOptions.alarmModeHome = homeModePreference.isChecked
            }
            "pref_mode_away" -> {
                mqttOptions.alarmModeAway = awayModePreference.isChecked
            }
            "pref_mode_night" -> {
                mqttOptions.alarmModeNight = nightModePreference.isChecked
            }
            "pref_mode_custom_bypass" -> {
                mqttOptions.alarmModeCustomBypass = customBypassModePreference.isChecked
            }
            PREF_AWAY_PENDING_TIME -> {
                mqttOptions.pendingTimeAway = pendingAwayPreference.text.toIntOrNull()?:60
                pendingAwayPreference.summary = getString(R.string.pref_away_pending_summary, mqttOptions.pendingTimeAway.toString())
            }
            PREF_HOME_PENDING_TIME -> {
                mqttOptions.pendingTimeHome = pendingHomePreference.text.toIntOrNull()?:60
                pendingHomePreference.summary = getString(R.string.pref_home_pending_summary, mqttOptions.pendingTimeHome.toString())
            }
            PREF_NIGHT_PENDING_TIME -> {
                mqttOptions.pendingTimeNight = pendingNightPreference.text.toIntOrNull()?:60
                pendingNightPreference.summary = getString(R.string.preference_summary_night_pending_time, mqttOptions.pendingTimeNight.toString())
            }
            PREF_BYPASS_PENDING_TIME -> {
                mqttOptions.pendingTimeBypass = pendingBypassPreference.text.toIntOrNull()?:60
                pendingBypassPreference.summary = getString(R.string.pref_bypass_pending_summary, mqttOptions.pendingTimeBypass.toString())
            }
            PREF_AWAY_DELAY_TIME -> {
                mqttOptions.delayTimeAway = delayAwayPreference.text.toIntOrNull()?:60
                delayAwayPreference.summary = getString(R.string.pref_away_delay_summary, mqttOptions.delayTimeAway.toString())
            }
            PREF_HOME_DELAY_TIME -> {
                mqttOptions.delayTimeHome = delayHomePreference.text.toIntOrNull()?:60
                delayHomePreference.summary = getString(R.string.pref_home_delay_summary, mqttOptions.delayTimeHome.toString())
            }
            PREF_NIGHT_DELAY_TIME -> {
                mqttOptions.delayTimeNight = delayHomePreference.text.toIntOrNull()?:60
                delayNightPreference.summary = getString(R.string.pref_night_delay_summary, mqttOptions.delayTimeNight.toString())
            }
            PREF_BYPASS_DELAY_TIME -> {
                mqttOptions.delayTimeBypass = delayHomePreference.text.toIntOrNull()?:60
                delayBypassPreference.summary = getString(R.string.pref_bypass_delay_summary, mqttOptions.delayTimeBypass.toString())
            }
            PREF_ALARM_REMOTE_CODE -> {
                val checked = remoteCodeSwitchPreference.isChecked
                checked.let {
                    mqttOptions.useRemoteCode = checked
                }
            }
        }
    }

    companion object {
        const val PREF_AWAY_PENDING_TIME = "settings_pref_away_pending_time"
        const val PREF_HOME_PENDING_TIME = "settings_pref_home_pending_time"
        const val PREF_NIGHT_PENDING_TIME = "settings_pref_night_pending_time"
        const val PREF_BYPASS_PENDING_TIME = "settings_pref_bypass_pending_time"
        const val PREF_AWAY_DELAY_TIME = "settings_pref_away_delay_time"
        const val PREF_HOME_DELAY_TIME = "settings_pref_home_delay_time"
        const val PREF_NIGHT_DELAY_TIME = "settings_pref_night_delay_time"
        const val PREF_BYPASS_DELAY_TIME = "settings_pref_bypass_delay_time"
        const val PREF_ALARM_REMOTE_CODE = "settings_alarm_remote_code"
    }
}