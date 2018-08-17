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
import android.support.v14.preference.SwitchPreference
import android.support.v7.preference.EditTextPreference
import android.support.v7.preference.PreferenceFragmentCompat
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import com.thanksmister.iot.mqtt.alarmpanel.R
import com.thanksmister.iot.mqtt.alarmpanel.persistence.Configuration
import com.thanksmister.iot.mqtt.alarmpanel.utils.DialogUtils
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class MjpegSettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

    @Inject lateinit var configuration: Configuration
    @Inject lateinit var dialogUtils: DialogUtils

    private var httpMjpegPreference: SwitchPreference? = null
    private var httpMjpegStreamsPreference: EditTextPreference? = null
    private var httpPortPreference: EditTextPreference? = null

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preferences_mjpeg)
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
        httpMjpegPreference = findPreference(getString(R.string.key_setting_http_mjpegenabled)) as SwitchPreference
        httpMjpegPreference!!.isChecked = configuration.httpMJPEGEnabled

        httpMjpegStreamsPreference = findPreference(getString(R.string.key_setting_http_mjpegmaxstreams)) as EditTextPreference
        httpMjpegStreamsPreference!!.setDefaultValue(configuration.httpMJPEGMaxStreams.toString())
        httpMjpegStreamsPreference!!.summary = configuration.httpMJPEGMaxStreams.toString()

        httpPortPreference = findPreference(getString(R.string.key_setting_http_port)) as EditTextPreference
        httpPortPreference!!.setDefaultValue(configuration.httpPort.toString())
        httpPortPreference!!.summary = configuration.httpPort.toString()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when (key) {
            getString(R.string.key_setting_http_mjpegenabled) -> {
                configuration.httpMJPEGEnabled = httpMjpegPreference!!.isChecked
            }
            getString(R.string.key_setting_http_mjpegmaxstreams) -> {
                try {
                    val value = httpMjpegStreamsPreference!!.text
                    if(!TextUtils.isEmpty(value)) {
                        configuration.httpMJPEGMaxStreams = value.toInt()
                        httpMjpegStreamsPreference!!.summary = value
                    } else if (isAdded) {
                        Toast.makeText(activity, R.string.text_error_blank_entry, Toast.LENGTH_LONG).show()
                        httpMjpegStreamsPreference!!.setDefaultValue(configuration.httpMJPEGMaxStreams.toString())
                        httpMjpegStreamsPreference!!.summary = configuration.httpMJPEGMaxStreams.toString()
                    }
                } catch (e : Exception) {
                    if(isAdded) {
                        Toast.makeText(activity, R.string.text_error_only_numbers, Toast.LENGTH_LONG).show()
                        httpMjpegStreamsPreference!!.setDefaultValue(configuration.httpMJPEGMaxStreams.toString())
                        httpMjpegStreamsPreference!!.summary = configuration.httpMJPEGMaxStreams.toString()
                    }
                }
            }
            getString(R.string.key_setting_http_port) -> {
                try {
                    val value = httpPortPreference!!.text
                    if(!TextUtils.isEmpty(value)) {
                        configuration.httpPort = value.toInt()
                        httpPortPreference!!.summary = value
                    } else if (isAdded) {
                        Toast.makeText(activity, R.string.text_error_blank_entry, Toast.LENGTH_LONG).show()
                        httpPortPreference!!.setDefaultValue(configuration.httpPort.toString())
                        httpPortPreference!!.summary = configuration.httpPort.toString()
                    }
                } catch (e : Exception) {
                    if(isAdded) {
                        Toast.makeText(activity, R.string.text_error_only_numbers, Toast.LENGTH_LONG).show()
                        httpPortPreference!!.setDefaultValue(configuration.httpPort.toString())
                        httpPortPreference!!.summary = configuration.httpPort.toString()
                    }
                }
            }
        }
    }
}