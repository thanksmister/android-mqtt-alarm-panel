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
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.support.v7.preference.CheckBoxPreference
import android.support.v7.preference.EditTextPreference
import android.support.v7.preference.ListPreference
import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceFragmentCompat
import android.text.TextUtils
import android.view.View
import android.widget.Toast

import com.thanksmister.iot.mqtt.alarmpanel.BaseActivity
import com.thanksmister.iot.mqtt.alarmpanel.R
import com.thanksmister.iot.mqtt.alarmpanel.ui.Configuration
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class CameraSettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

    @Inject lateinit var configuration: Configuration

    private var tolPreference: EditTextPreference? = null
    private var fromPreference: EditTextPreference? = null
    private var domainPreference: EditTextPreference? = null
    private var keyPreference: EditTextPreference? = null
    private var activePreference: CheckBoxPreference? = null
    private var descriptionPreference: Preference? = null
    private var rotatePreference: ListPreference? = null

    override fun onAttach(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Perform injection here for M (API 23) due to deprecation of onAttach(Activity).
            AndroidSupportInjection.inject(this)
        }
        super.onAttach(context)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preferences_camera)
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

        tolPreference = findPreference(Configuration.PREF_MAIL_TO) as EditTextPreference
        fromPreference = findPreference(Configuration.PREF_MAIL_FROM) as EditTextPreference
        domainPreference = findPreference(Configuration.PREF_MAIL_URL) as EditTextPreference
        keyPreference = findPreference(Configuration.PREF_MAIL_API_KEY) as EditTextPreference

        activePreference = findPreference(Configuration.PREF_MODULE_CAMERA) as CheckBoxPreference
        rotatePreference = findPreference(Configuration.PREF_CAMERA_ROTATE) as ListPreference
        descriptionPreference = findPreference("pref_mail_description")

        activePreference!!.isChecked = configuration.hasCamera()

        tolPreference!!.isEnabled = configuration.hasCamera()
        if (!TextUtils.isEmpty(configuration.getMailTo())) {
            tolPreference!!.text = configuration.getMailTo()
            tolPreference!!.summary = configuration.getMailTo()
        }

        fromPreference!!.isEnabled = configuration.hasCamera()
        if (!TextUtils.isEmpty(configuration.getMailFrom())) {
            fromPreference!!.text = configuration.getMailFrom()
            fromPreference!!.summary = configuration.getMailFrom()
        }

        domainPreference!!.isEnabled = configuration.hasCamera()
        if (!TextUtils.isEmpty(configuration.getMailGunUrl())) {
            domainPreference!!.text = configuration.getMailGunUrl()
            domainPreference!!.summary = configuration.getMailGunUrl()
        }

        keyPreference!!.isEnabled = configuration.hasCamera()
        if (!TextUtils.isEmpty(configuration.getMailGunApiKey())) {
            keyPreference!!.text = configuration.getMailGunApiKey()
            keyPreference!!.summary = configuration.getMailGunApiKey()
        }

        rotatePreference!!.setDefaultValue(configuration.getCameraRotate())
        descriptionPreference!!.isEnabled = configuration.hasCamera()
        rotatePreference!!.isEnabled = configuration.hasCamera()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        val value: String
        when (key) {
            Configuration.PREF_MAIL_TO -> {
                value = tolPreference!!.text
                if (!TextUtils.isEmpty(value)) {
                    configuration.setMailTo(value)
                    tolPreference!!.summary = value
                } else if (isAdded) {
                    Toast.makeText(activity, R.string.text_error_blank_entry, Toast.LENGTH_LONG).show()
                }
            }
            Configuration.PREF_MAIL_FROM -> {
                value = fromPreference!!.text
                if (!TextUtils.isEmpty(value)) {
                    configuration.setMailFrom(value)
                    fromPreference!!.summary = value
                } else if (isAdded) {
                    Toast.makeText(activity, R.string.text_error_blank_entry, Toast.LENGTH_LONG).show()
                }
            }
            Configuration.PREF_MAIL_URL -> {
                value = domainPreference!!.text
                if (!TextUtils.isEmpty(value)) {
                    configuration.setMailGunUrl(value)
                    domainPreference!!.summary = value
                } else if (isAdded) {
                    Toast.makeText(activity, R.string.text_error_blank_entry, Toast.LENGTH_LONG).show()
                }
            }
            Configuration.PREF_MAIL_API_KEY -> {
                value = keyPreference!!.text
                if (!TextUtils.isEmpty(value)) {
                    configuration.setMailGunApiKey(value)
                    keyPreference!!.summary = value
                } else if (isAdded) {
                    Toast.makeText(activity, R.string.text_error_blank_entry, Toast.LENGTH_LONG).show()
                }
            }
            Configuration.PREF_MODULE_CAMERA -> {
                val checked = activePreference!!.isChecked
                configuration.setHasCamera(checked)
                tolPreference!!.isEnabled = checked
                fromPreference!!.isEnabled = checked
                domainPreference!!.isEnabled = checked
                keyPreference!!.isEnabled = checked
                descriptionPreference!!.isEnabled = checked
                rotatePreference!!.isEnabled = checked
            }
            Configuration.PREF_CAMERA_ROTATE -> {
                val valueFloat = rotatePreference!!.value
                val valueName = rotatePreference!!.entry.toString()
                rotatePreference!!.summary = getString(R.string.preference_camera_flip_summary, valueName)
                configuration.setCameraRotate(valueFloat)
            }
        }
    }
}