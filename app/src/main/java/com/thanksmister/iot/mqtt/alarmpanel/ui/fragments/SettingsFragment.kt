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
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.navigation.Navigation
import androidx.preference.Preference
import androidx.preference.SwitchPreference
import com.thanksmister.iot.mqtt.alarmpanel.BaseActivity
import com.thanksmister.iot.mqtt.alarmpanel.R
import com.thanksmister.iot.mqtt.alarmpanel.ui.activities.SettingsActivity
import com.thanksmister.iot.mqtt.alarmpanel.ui.views.AlarmCodeView
import dagger.android.support.AndroidSupportInjection

class SettingsFragment : BaseSettingsFragment() {

    private var defaultCode: Int = 0
    private var tempCode: Int = 0
    private var confirmCode = false

    // Buttons

    private val mqttPreference: Preference by lazy {
        findPreference("button_mqtt") as Preference
    }

    private val aboutPreference: Preference by lazy {
        findPreference("button_about") as Preference
    }

    private val themePreference: SwitchPreference by lazy {
        findPreference("pref_dark_theme") as SwitchPreference
    }

    private val panicPreference: SwitchPreference by lazy {
        findPreference("pref_panic_button") as SwitchPreference
    }

    private val notificationsPreference: Preference by lazy {
        findPreference("button_notifications") as Preference
    }

    private val codePreference: Preference by lazy {
        findPreference("button_alarm_code") as Preference
    }

    private val cameraPreference: Preference by lazy {
        findPreference("button_camera") as Preference
    }

    private val browserPreference: Preference by lazy {
        findPreference("button_browser") as Preference
    }

    private val weatherPreference: Preference by lazy {
        findPreference("button_weather") as Preference
    }

    private val screenPreference: Preference by lazy {
        findPreference("button_screen") as Preference
    }

    private val alarmSensorsPreference: Preference by lazy {
        findPreference("button_alarm_sensors") as Preference
    }

    private val sensorsPreference: Preference by lazy {
        findPreference("button_sensors") as Preference
    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preference_general)
        lifecycle.addObserver(dialogUtils)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if ((activity as SettingsActivity).supportActionBar != null) {
            (activity as SettingsActivity).supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            (activity as SettingsActivity).supportActionBar!!.setDisplayShowHomeEnabled(true)
            (activity as SettingsActivity).supportActionBar!!.title = (getString(R.string.activity_settings_title))
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        mqttPreference.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            view.let { Navigation.findNavController(it).navigate(R.id.mqtt_action) }
            false
        }
        aboutPreference.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            view.let { Navigation.findNavController(it).navigate(R.id.about_action) }
            false
        }
        notificationsPreference.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            view.let { Navigation.findNavController(it).navigate(R.id.notifications_action) }
            false
        }
        screenPreference.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            view.let { Navigation.findNavController(it).navigate(R.id.screen_action) }
            false
        }
        alarmSensorsPreference.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            view.let { Navigation.findNavController(it).navigate(R.id.alarm_action) }
            false
        }
        cameraPreference.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            view.let { Navigation.findNavController(it).navigate(R.id.camera_action) }
            false
        }
        weatherPreference.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            view.let { Navigation.findNavController(it).navigate(R.id.weather_action) }
            false
        }
        codePreference.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            showAlarmCodeDialog()
            true
        }
        browserPreference.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            view.let { Navigation.findNavController(it).navigate(R.id.platform_action) }
            false
        }
        sensorsPreference.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            view.let { Navigation.findNavController(it).navigate(R.id.sensors_action) }
            false
        }

        themePreference.isChecked = configuration.useDarkTheme
        panicPreference.isChecked == configuration.panicButton

        /*if (isFingerprintSupported()) {
            fingerprintPreference.isVisible = true
            fingerprintPreference.isChecked = configuration.fingerPrint
        } else {
            fingerprintPreference.isVisible = false
        }*/

        val code = configuration.alarmCode.toString()
        if (code.isNotEmpty()) {
            codePreference.summary = toStars(code)
        }

    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when (key) {
            "pref_dark_theme" -> {
                configuration.useDarkTheme = themePreference.isChecked
                this.requireActivity().recreate()
            }
            "pref_panic_button" -> {
                configuration.panicButton = panicPreference.isChecked
            }
            /*Configuration.PREF_FINGERPRINT -> {
                val checked = fingerprintPreference.isChecked
                if (isFingerprintSupported()) {
                    configuration.fingerPrint = checked
                } else {
                    Toast.makeText(activity, getString(R.string.pref_fingerprint_error), Toast.LENGTH_LONG).show()
                    fingerprintPreference.isChecked = false
                    configuration.fingerPrint = false
                }
            }*/
        }
    }

    /* @SuppressLint("InlinedApi")
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
     }*/

    private fun showAlarmCodeDialog() {
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

    private fun toStars(textToStars: String?): String {
        var text = textToStars
        val sb = StringBuilder()
        for (i in text.orEmpty().indices) {
            sb.append('*')
        }
        text = sb.toString()
        return text
    }

    companion object {
        fun newInstance(): SettingsFragment {
            return SettingsFragment()
        }
    }
}