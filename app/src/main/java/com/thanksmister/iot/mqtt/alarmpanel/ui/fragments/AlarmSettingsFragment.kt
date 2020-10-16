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
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.preference.*
import com.thanksmister.iot.mqtt.alarmpanel.BaseActivity
import com.thanksmister.iot.mqtt.alarmpanel.R
import com.thanksmister.iot.mqtt.alarmpanel.network.MQTTOptions
import com.thanksmister.iot.mqtt.alarmpanel.persistence.Configuration
import com.thanksmister.iot.mqtt.alarmpanel.persistence.Configuration.Companion.PREF_FINGERPRINT
import com.thanksmister.iot.mqtt.alarmpanel.ui.activities.SettingsActivity
import com.thanksmister.iot.mqtt.alarmpanel.ui.views.AlarmCodeView
import com.thanksmister.iot.mqtt.alarmpanel.utils.DialogUtils
import dagger.android.support.AndroidSupportInjection
import timber.log.Timber
import javax.inject.Inject


class AlarmSettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

    @Inject
    lateinit var configuration: Configuration

    @Inject
    lateinit var mqttOptions: MQTTOptions

    private val sensorOnePreference: SwitchPreference by lazy {
        findPreference("pref_sensor_one") as SwitchPreference
    }

    private val sensorOneNamePreference: EditTextPreference by lazy {
        findPreference("pref_sensor_one_name") as EditTextPreference
    }

    private val sensorOneTopicPreference: EditTextPreference by lazy {
        findPreference("pref_sensor_one_topic") as EditTextPreference
    }

    private val sensorOneStatePreference: EditTextPreference by lazy {
        findPreference("pref_sensor_one_state") as EditTextPreference
    }

    private val sensorTwoPreference: SwitchPreference by lazy {
        findPreference("pref_sensor_two") as SwitchPreference
    }

    private val sensorTwoNamePreference: EditTextPreference by lazy {
        findPreference("pref_sensor_two_name") as EditTextPreference
    }

    private val sensorTwoTopicPreference: EditTextPreference by lazy {
        findPreference("pref_sensor_two_topic") as EditTextPreference
    }

    private val sensorTwoStatePreference: EditTextPreference by lazy {
        findPreference("pref_sensor_two_state") as EditTextPreference
    }

    private val sensorThreePreference: SwitchPreference by lazy {
        findPreference("pref_sensor_three") as SwitchPreference
    }

    private val sensorThreeNamePreference: EditTextPreference by lazy {
        findPreference("pref_sensor_three_name") as EditTextPreference
    }

    private val sensorThreeTopicPreference: EditTextPreference by lazy {
        findPreference("pref_sensor_three_topic") as EditTextPreference
    }

    private val sensorThreeStatePreference: EditTextPreference by lazy {
        findPreference("pref_sensor_three_state") as EditTextPreference
    }

    private val sensorFourPreference: SwitchPreference by lazy {
        findPreference("pref_sensor_four") as SwitchPreference
    }

    private val sensorFourNamePreference: EditTextPreference by lazy {
        findPreference("pref_sensor_four_name") as EditTextPreference
    }

    private val sensorFourTopicPreference: EditTextPreference by lazy {
        findPreference("pref_sensor_four_topic") as EditTextPreference
    }

    private val sensorFourStatePreference: EditTextPreference by lazy {
        findPreference("pref_sensor_four_state") as EditTextPreference
    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
        if ((activity as SettingsActivity).supportActionBar != null) {
            (activity as SettingsActivity).supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            (activity as SettingsActivity).supportActionBar!!.setDisplayShowHomeEnabled(true)
            (activity as SettingsActivity).supportActionBar!!.title = (getString(R.string.preference_title_alarm))
        }
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
        sensorOnePreference.isChecked = mqttOptions.sensorOneActive
        sensorOneNamePreference.text = mqttOptions.sensorOneName
        sensorOneNamePreference.summary = mqttOptions.sensorOneName
        sensorOneTopicPreference.text = mqttOptions.sensorOneTopic
        sensorOneTopicPreference.summary = mqttOptions.sensorOneTopic
        sensorOneStatePreference.text = mqttOptions.sensorOneState
        sensorOneStatePreference.summary = mqttOptions.sensorOneState

        sensorTwoPreference.isChecked = mqttOptions.sensorTwoActive
        sensorTwoNamePreference.text = mqttOptions.sensorTwoName
        sensorTwoNamePreference.summary = mqttOptions.sensorTwoName
        sensorTwoTopicPreference.text = mqttOptions.sensorTwoTopic
        sensorTwoTopicPreference.summary = mqttOptions.sensorTwoTopic
        sensorTwoStatePreference.text = mqttOptions.sensorTwoState
        sensorTwoStatePreference.summary = mqttOptions.sensorTwoState

        sensorThreePreference.isChecked = mqttOptions.sensorThreeActive
        sensorThreeNamePreference.text = mqttOptions.sensorThreeName
        sensorThreeNamePreference.summary = mqttOptions.sensorThreeName
        sensorThreeTopicPreference.text = mqttOptions.sensorThreeTopic
        sensorThreeTopicPreference.summary = mqttOptions.sensorThreeTopic
        sensorThreeStatePreference.text = mqttOptions.sensorThreeState
        sensorThreeStatePreference.summary = mqttOptions.sensorThreeState

        sensorFourPreference.isChecked = mqttOptions.sensorFourActive
        sensorFourNamePreference.text = mqttOptions.sensorFourName
        sensorFourNamePreference.summary = mqttOptions.sensorFourName
        sensorFourTopicPreference.text = mqttOptions.sensorFourTopic
        sensorFourTopicPreference.summary = mqttOptions.sensorFourTopic
        sensorFourStatePreference.text = mqttOptions.sensorFourState
        sensorFourStatePreference.summary = mqttOptions.sensorFourState
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    @SuppressLint("InlinedApi")
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        val value: String
        when (key) {
            "pref_sensor_one" -> {
                //mqttOptions.setCommandTopic()
            }
            "pref_sensor_one_name" -> {

            }
            "pref_sensor_one_topic" -> {

            }
            "pref_sensor_two" -> {

            }
            "pref_sensor_two_name" -> {

            }
            "pref_sensor_two_topic" -> {

            }
            "pref_sensor_three" -> {

            }
            "pref_sensor_three_name" -> {

            }
            "pref_sensor_three_topic" -> {

            }
            "pref_sensor_four" -> {

            }
            "pref_sensor_four_name" -> {

            }
            "pref_sensor_four_topic" -> {

            }
        }
    }
}