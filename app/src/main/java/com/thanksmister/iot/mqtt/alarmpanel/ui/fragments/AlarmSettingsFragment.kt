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

import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v7.preference.EditTextPreference
import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceFragmentCompat
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import com.thanksmister.iot.mqtt.alarmpanel.BaseActivity
import com.thanksmister.iot.mqtt.alarmpanel.R
import com.thanksmister.iot.mqtt.alarmpanel.ui.Configuration
import com.thanksmister.iot.mqtt.alarmpanel.ui.Configuration.Companion.PREF_AWAY_DELAY_TIME
import com.thanksmister.iot.mqtt.alarmpanel.ui.Configuration.Companion.PREF_AWAY_PENDING_TIME
import com.thanksmister.iot.mqtt.alarmpanel.ui.Configuration.Companion.PREF_DELAY_TIME
import com.thanksmister.iot.mqtt.alarmpanel.ui.Configuration.Companion.PREF_HOME_DELAY_TIME
import com.thanksmister.iot.mqtt.alarmpanel.ui.Configuration.Companion.PREF_HOME_PENDING_TIME
import com.thanksmister.iot.mqtt.alarmpanel.ui.Configuration.Companion.PREF_PENDING_TIME
import com.thanksmister.iot.mqtt.alarmpanel.ui.views.AlarmCodeView
import com.thanksmister.iot.mqtt.alarmpanel.utils.DialogUtils
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class AlarmSettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

    @Inject lateinit var configuration: Configuration
    @Inject lateinit var dialogUtils: DialogUtils

    private var pendingPreference: EditTextPreference? = null
    private var pendingHomePreference: EditTextPreference? = null
    private var pendingAwayPreference: EditTextPreference? = null
    private var delayPreference: EditTextPreference? = null
    private var delayHomePreference: EditTextPreference? = null
    private var delayAwayPreference: EditTextPreference? = null

    private var defaultCode: Int = 0
    private var tempCode: Int = 0
    private var confirmCode = false

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey : String?) {
        addPreferencesFromResource(R.xml.alarm_preferences)
        lifecycle.addObserver(dialogUtils)
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

        val buttonPreference = findPreference(Configuration.PREF_ALARM_CODE)
        buttonPreference.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            showAlarmCodeDialog()
            true
        }

        pendingPreference = findPreference(PREF_PENDING_TIME) as EditTextPreference
        pendingHomePreference = findPreference(PREF_HOME_PENDING_TIME) as EditTextPreference
        pendingAwayPreference = findPreference(PREF_AWAY_PENDING_TIME) as EditTextPreference
        delayPreference = findPreference(PREF_DELAY_TIME) as EditTextPreference
        delayHomePreference = findPreference(PREF_HOME_DELAY_TIME) as EditTextPreference
        delayAwayPreference = findPreference(PREF_AWAY_DELAY_TIME) as EditTextPreference


        pendingPreference!!.text = configuration.pendingTime.toString()
        pendingHomePreference!!.text = configuration.pendingHomeTime.toString()
        pendingAwayPreference!!.text = configuration.pendingAwayTime.toString()
        delayPreference!!.text = configuration.delayTime.toString()
        delayAwayPreference!!.text = configuration.delayAwayTime.toString()
        delayHomePreference!!.text = configuration.delayHomeTime.toString()

        pendingPreference!!.summary = getString(R.string.preference_summary_pending_time, configuration.pendingTime.toString())
        pendingHomePreference!!.summary = getString(R.string.pref_home_pending_summary, configuration.pendingHomeTime.toString())
        pendingAwayPreference!!.summary = getString(R.string.pref_away_pending_summary, configuration.pendingAwayTime.toString())
        delayPreference!!.summary = getString(R.string.pref_delay_summary, configuration.delayTime.toString())
        delayHomePreference!!.summary = getString(R.string.pref_home_delay_summary, configuration.delayHomeTime.toString())
        delayAwayPreference!!.summary = getString(R.string.pref_away_delay_summary, configuration.delayAwayTime.toString())

        // the first time we need to set the alarm code
        if(configuration.isFirstTime) {
            showAlarmCodeDialog();
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        val value: String
        when (key) {
            PREF_PENDING_TIME -> {
                value = pendingPreference!!.text
                if (value.matches("[0-9]+".toRegex()) && !TextUtils.isEmpty(value)) {
                    val pendingTime = Integer.parseInt(value)
                    configuration.pendingTime = pendingTime
                    pendingPreference!!.text = pendingTime.toString()
                    pendingPreference!!.summary = getString(R.string.preference_summary_pending_time, pendingTime.toString())
                } else if (isAdded) {
                    Toast.makeText(activity, R.string.text_error_only_numbers, Toast.LENGTH_LONG).show()
                    pendingPreference!!.text = configuration.pendingTime.toString()
                }
            }
            PREF_DELAY_TIME -> {
                value = delayPreference!!.text
                if (value.matches("[0-9]+".toRegex()) && !TextUtils.isEmpty(value)) {
                    val pendingTime = Integer.parseInt(value)
                    configuration.delayTime = pendingTime
                    delayPreference!!.text = pendingTime.toString()
                    delayPreference!!.summary = getString(R.string.pref_delay_summary, pendingTime.toString())
                } else if (isAdded) {
                    Toast.makeText(activity, R.string.text_error_only_numbers, Toast.LENGTH_LONG).show()
                    delayPreference!!.text = configuration.delayTime.toString()
                }
            }
            PREF_HOME_DELAY_TIME -> {
                value = delayHomePreference!!.text
                if (value.matches("[0-9]+".toRegex()) && !TextUtils.isEmpty(value)) {
                    val pendingTime = Integer.parseInt(value)
                    configuration.delayHomeTime = pendingTime
                    delayHomePreference!!.text = pendingTime.toString()
                    delayHomePreference!!.summary = getString(R.string.pref_home_delay_summary, pendingTime.toString())
                } else if (isAdded) {
                    Toast.makeText(activity, R.string.text_error_only_numbers, Toast.LENGTH_LONG).show()
                    delayHomePreference!!.text = configuration.delayHomeTime.toString()
                }
            }
            PREF_AWAY_DELAY_TIME -> {
                value = delayAwayPreference!!.text
                if (value.matches("[0-9]+".toRegex()) && !TextUtils.isEmpty(value)) {
                    val pendingTime = Integer.parseInt(value)
                    configuration.delayAwayTime = pendingTime
                    delayAwayPreference!!.text = pendingTime.toString()
                    delayAwayPreference!!.summary = getString(R.string.pref_away_delay_summary, pendingTime.toString())
                } else if (isAdded) {
                    Toast.makeText(activity, R.string.text_error_only_numbers, Toast.LENGTH_LONG).show()
                    delayAwayPreference!!.text = configuration.delayAwayTime.toString()
                }
            }
            PREF_HOME_PENDING_TIME -> {
                value = pendingHomePreference!!.text
                if (value.matches("[0-9]+".toRegex()) && !TextUtils.isEmpty(value)) {
                    val pendingTime = Integer.parseInt(value)
                    configuration.pendingHomeTime = pendingTime
                    pendingHomePreference!!.text = pendingTime.toString()
                    pendingHomePreference!!.summary = getString(R.string.pref_home_pending_summary, pendingTime.toString())
                } else if (isAdded) {
                    Toast.makeText(activity, R.string.text_error_only_numbers, Toast.LENGTH_LONG).show()
                    pendingHomePreference!!.text = configuration.pendingHomeTime.toString()
                }
            }
            PREF_AWAY_PENDING_TIME -> {
                value = pendingAwayPreference!!.text
                if (value.matches("[0-9]+".toRegex()) && !TextUtils.isEmpty(value)) {
                    val pendingTime = Integer.parseInt(value)
                    configuration.pendingAwayTime = pendingTime
                    pendingAwayPreference!!.text = pendingTime.toString()
                    pendingAwayPreference!!.summary = getString(R.string.pref_away_pending_summary, pendingTime.toString())
                } else if (isAdded) {
                    Toast.makeText(activity, R.string.text_error_only_numbers, Toast.LENGTH_LONG).show()
                    pendingAwayPreference!!.text = configuration.pendingTime.toString()
                }
            }
        }
    }

    private fun showAlarmCodeDialog() {
        // store the default alarm code
        defaultCode = configuration.alarmCode
        if (isAdded) {
            dialogUtils.showCodeDialog(activity as BaseActivity, confirmCode, object : AlarmCodeView.ViewListener {
                override fun onComplete(code: Int) {
                    if (code == defaultCode) {
                        confirmCode = false
                        dialogUtils.clearDialogs()
                        Toast.makeText(activity, R.string.toast_code_match, Toast.LENGTH_LONG).show()
                    } else if (!confirmCode) {
                        tempCode = code
                        confirmCode = true
                        dialogUtils.clearDialogs()
                        showAlarmCodeDialog()
                    } else if (code == tempCode) {
                        configuration.isFirstTime = false;
                        configuration.alarmCode = tempCode
                        tempCode = 0
                        confirmCode = false
                        dialogUtils.clearDialogs()
                        Toast.makeText(activity, R.string.toast_code_changed, Toast.LENGTH_LONG).show()
                    } else {
                        tempCode = 0
                        confirmCode = false
                        dialogUtils.clearDialogs()
                        Toast.makeText(activity, R.string.toast_code_not_match, Toast.LENGTH_LONG).show()
                    }
                }
                override fun onError() {}
                override fun onCancel() {
                    confirmCode = false
                    dialogUtils.clearDialogs()
                    Toast.makeText(activity, R.string.toast_code_unchanged, Toast.LENGTH_SHORT).show()
                }
            }, DialogInterface.OnCancelListener {
                confirmCode = false
                Toast.makeText(activity, R.string.toast_code_unchanged, Toast.LENGTH_SHORT).show()
            }, configuration.systemSounds)
        }
    }
}