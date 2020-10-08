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
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.preference.*
import androidx.preference.PreferenceFragmentCompat
import com.thanksmister.iot.mqtt.alarmpanel.BaseActivity
import com.thanksmister.iot.mqtt.alarmpanel.R
import com.thanksmister.iot.mqtt.alarmpanel.persistence.Configuration
import com.thanksmister.iot.mqtt.alarmpanel.persistence.Configuration.Companion.PREF_AWAY_DELAY_TIME
import com.thanksmister.iot.mqtt.alarmpanel.persistence.Configuration.Companion.PREF_AWAY_PENDING_TIME
import com.thanksmister.iot.mqtt.alarmpanel.persistence.Configuration.Companion.PREF_DELAY_TIME
import com.thanksmister.iot.mqtt.alarmpanel.persistence.Configuration.Companion.PREF_FINGERPRINT
import com.thanksmister.iot.mqtt.alarmpanel.persistence.Configuration.Companion.PREF_HOME_DELAY_TIME
import com.thanksmister.iot.mqtt.alarmpanel.persistence.Configuration.Companion.PREF_HOME_PENDING_TIME
import com.thanksmister.iot.mqtt.alarmpanel.persistence.Configuration.Companion.PREF_PENDING_TIME
import com.thanksmister.iot.mqtt.alarmpanel.ui.activities.SettingsActivity
import com.thanksmister.iot.mqtt.alarmpanel.ui.views.AlarmCodeView
import com.thanksmister.iot.mqtt.alarmpanel.utils.DialogUtils
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject
import com.wei.android.lib.fingerprintidentify.FingerprintIdentify
import com.wei.android.lib.fingerprintidentify.base.BaseFingerprint
import timber.log.Timber


class AlarmSettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

    @Inject lateinit var configuration: Configuration
    @Inject lateinit var dialogUtils: DialogUtils

    private var pendingPreference: EditTextPreference? = null
    private var pendingHomePreference: EditTextPreference? = null
    private var pendingAwayPreference: EditTextPreference? = null
    private var delayPreference: EditTextPreference? = null
    private var delayHomePreference: EditTextPreference? = null
    private var delayAwayPreference: EditTextPreference? = null
    private var fingerprintPreference: CheckBoxPreference? = null

    private var defaultCode: Int = 0
    private var tempCode: Int = 0
    private var confirmCode = false

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
        if((activity as SettingsActivity).supportActionBar != null) {
            (activity as SettingsActivity).supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            (activity as SettingsActivity).supportActionBar!!.setDisplayShowHomeEnabled(true)
            (activity as SettingsActivity).supportActionBar!!.title = (getString(R.string.preference_title_alarm))
        }
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

        fingerprintPreference = findPreference(PREF_FINGERPRINT) as CheckBoxPreference

        if (isFingerprintSupported()) {
            fingerprintPreference!!.isVisible = true
            fingerprintPreference!!.isChecked = configuration.fingerPrint
        } else {
            fingerprintPreference!!.isVisible = false
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // the first time we need to set the alarm code
        if(configuration.isFirstTime) {
            showAlarmCodeDialog();
        }
    }

    @SuppressLint("InlinedApi")
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        val value: String
        when (key) {

            PREF_FINGERPRINT -> {
                val checked = fingerprintPreference!!.isChecked
                if (isFingerprintSupported()) {
                    configuration.fingerPrint = checked
                } else {
                    Toast.makeText(activity, getString(R.string.pref_fingerprint_error), Toast.LENGTH_LONG).show()
                    fingerprintPreference!!.isChecked = false
                    configuration.fingerPrint = false
                }
            }
        }
    }

    @SuppressLint("InlinedApi")
    private fun isFingerprintSupported(): Boolean {
        try {
            val fingerPrintIdentity = FingerprintIdentify(context)
            Timber.w("Fingerprint isFingerprintEnable: " + fingerPrintIdentity.isFingerprintEnable)
            Timber.w("Fingerprint isHardwareEnable: " + fingerPrintIdentity.isHardwareEnable)
            if(fingerPrintIdentity.isFingerprintEnable && fingerPrintIdentity.isHardwareEnable) {
                return true
            }
        } catch (e: ClassNotFoundException) {
            Timber.w("Fingerprint: " + e.message)
        }
        return false
    }

    private fun showAlarmCodeDialog() {
        // store the default alarm code
        defaultCode = configuration.alarmCode
        if (activity != null && isAdded) {
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
                        if (activity != null && isAdded) {
                            showAlarmCodeDialog()
                        }
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