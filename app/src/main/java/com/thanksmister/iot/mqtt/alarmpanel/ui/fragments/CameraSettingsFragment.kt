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

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.support.v14.preference.SwitchPreference
import android.support.v4.app.ActivityCompat
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
import com.thanksmister.iot.mqtt.alarmpanel.persistence.Configuration
import com.thanksmister.iot.mqtt.alarmpanel.ui.activities.LiveCameraActivity
import com.thanksmister.iot.mqtt.alarmpanel.utils.CameraUtils
import com.thanksmister.iot.mqtt.alarmpanel.utils.DialogUtils
import dagger.android.support.AndroidSupportInjection
import timber.log.Timber
import javax.inject.Inject

class CameraSettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

    @Inject lateinit var configuration: Configuration
    @Inject lateinit var dialogUtils: DialogUtils

    private var tolPreference: EditTextPreference? = null
    private var fromPreference: EditTextPreference? = null
    private var domainPreference: EditTextPreference? = null
    private var keyPreference: EditTextPreference? = null
    private var activePreference: CheckBoxPreference? = null
    private var telegramTokenPreference: EditTextPreference? = null
    private var telegramChatIdPreference: EditTextPreference? = null
    private var cameraListPreference: ListPreference? = null
    private var cameraTestPreference: Preference? = null
    private var cameraPreference: SwitchPreference? = null
    private var fpsPreference: EditTextPreference? = null
    private var facePreference: SwitchPreference? = null
    private var faceWakePreference: SwitchPreference? = null
    private var httpMjpegPreference: SwitchPreference? = null
    private var httpMjpegStreamsPreference: EditTextPreference? = null
    private var httpPortPreference: EditTextPreference? = null
    private var qrCodePreference: SwitchPreference? = null
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
        addPreferencesFromResource(R.xml.preferences_camera)
    }

    override fun onResume() {
        super.onResume()
        preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (ActivityCompat.checkSelfPermission(activity!!.applicationContext, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                createCameraList()
            }
        } else {
            createCameraList()
        }
    }

    override fun onPause() {
        super.onPause()
        preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        telegramChatIdPreference = findPreference(Configuration.PREF_TELEGRAM_CHAT_ID) as EditTextPreference
        telegramTokenPreference = findPreference(Configuration.PREF_TELEGRAM_TOKEN) as EditTextPreference
        tolPreference = findPreference(Configuration.PREF_MAIL_TO) as EditTextPreference
        fromPreference = findPreference(Configuration.PREF_MAIL_FROM) as EditTextPreference
        domainPreference = findPreference(Configuration.PREF_MAIL_URL) as EditTextPreference
        keyPreference = findPreference(Configuration.PREF_MAIL_API_KEY) as EditTextPreference
        activePreference = findPreference(Configuration.PREF_CAMERA_CAPTURE) as CheckBoxPreference

        cameraPreference = findPreference(getString(R.string.key_setting_camera_enabled)) as SwitchPreference
        cameraPreference!!.isChecked = configuration.cameraEnabled
        motionPreference = findPreference(getString(R.string.key_setting_camera_motionenabled)) as SwitchPreference
        motionPreference!!.isChecked = configuration.cameraMotionEnabled
        motionWakePreference = findPreference(getString(R.string.key_setting_camera_motionwake)) as SwitchPreference
        motionWakePreference!!.isChecked = configuration.cameraMotionWakeEnabled
        motionLeniencyPreference = findPreference(getString(R.string.key_setting_camera_motionleniency)) as EditTextPreference
        motionLeniencyPreference!!.setDefaultValue(configuration.cameraMotionLeniency)
        motionLeniencyPreference!!.setSummary(configuration.cameraMotionLeniency)
        motionLumaPreference = findPreference(getString(R.string.key_setting_camera_motionminluma)) as EditTextPreference
        motionLumaPreference!!.setDefaultValue(configuration.cameraMotionMinLuma)
        motionLumaPreference!!.setSummary(configuration.cameraMotionMinLuma)
        motionClearPreference = findPreference(getString(R.string.key_setting_motion_clear)) as EditTextPreference
        motionClearPreference!!.setDefaultValue(configuration.motionResetTime)
        motionClearPreference!!.setSummary(configuration.motionResetTime)
        facePreference = findPreference(getString(R.string.key_setting_camera_faceenabled)) as SwitchPreference
        facePreference!!.isChecked = configuration.cameraFaceEnabled

        faceWakePreference = findPreference(getString(R.string.key_setting_camera_facewake)) as SwitchPreference
        faceWakePreference!!.isChecked = configuration.cameraFaceWake

        httpMjpegPreference = findPreference(getString(R.string.key_setting_http_mjpegenabled)) as SwitchPreference
        httpMjpegPreference!!.isChecked = configuration.httpMJPEGEnabled

        httpMjpegStreamsPreference = findPreference(getString(R.string.key_setting_http_mjpegmaxstreams)) as EditTextPreference
        httpMjpegStreamsPreference!!.setDefaultValue(configuration.httpMJPEGMaxStreams)
        httpMjpegStreamsPreference!!.setSummary(configuration.httpMJPEGMaxStreams)

        httpPortPreference = findPreference(getString(R.string.key_setting_http_port)) as EditTextPreference
        httpPortPreference!!.setDefaultValue(configuration.httpPort)
        httpPortPreference!!.setSummary(configuration.httpPort)

        qrCodePreference = findPreference(getString(R.string.key_setting_camera_qrcodeenabled)) as SwitchPreference
        qrCodePreference!!.isChecked = configuration.cameraQRCodeEnabled

        fpsPreference = findPreference(getString(R.string.key_setting_camera_fps)) as EditTextPreference
        fpsPreference!!.setDefaultValue(configuration.cameraFPS)
        fpsPreference!!.summary = configuration.cameraFPS.toString()

        cameraListPreference = findPreference(getString(R.string.key_setting_camera_cameraid)) as ListPreference
        cameraListPreference!!.isEnabled = false
        cameraListPreference!!.setOnPreferenceChangeListener { preference, newValue ->
            if (preference is ListPreference) {
                val index = preference.findIndexOfValue(newValue.toString())
                preference.setSummary(
                        if (index >= 0)
                            preference.entries[index]
                        else
                            "")
            }
            true;
        }

        cameraTestPreference = findPreference("button_key_camera_test")
        cameraTestPreference!!.onPreferenceClickListener = Preference.OnPreferenceClickListener { preference ->
            startCameraTest(preference.context)
            false
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (ActivityCompat.checkSelfPermission(activity as BaseActivity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                telegramChatIdPreference!!.isEnabled = false
                telegramTokenPreference!!.isEnabled = false
                tolPreference!!.isEnabled = false
                fromPreference!!.isEnabled = false
                domainPreference!!.isEnabled = false
                activePreference!!.isEnabled = false
                keyPreference!!.isEnabled = false
                configuration.setHasCameraCapture(false)
                dialogUtils.showAlertDialog(activity as BaseActivity, getString(R.string.dialog_no_camera_permissions))
                return
            }
        }

        activePreference!!.isChecked = configuration.hasCameraCapture()

        if (!TextUtils.isEmpty(configuration.getMailTo())) {
            tolPreference!!.setDefaultValue(configuration.getMailTo())
            tolPreference!!.summary = configuration.getMailTo()
        }

        if (!TextUtils.isEmpty(configuration.getMailFrom())) {
            fromPreference!!.setDefaultValue(configuration.getMailFrom())
            fromPreference!!.summary = configuration.getMailFrom()
        }

        if (!TextUtils.isEmpty(configuration.getMailGunUrl())) {
            domainPreference!!.setDefaultValue(configuration.getMailGunUrl())
            domainPreference!!.summary = configuration.getMailGunUrl()
        }

        if (!TextUtils.isEmpty(configuration.getMailGunApiKey())) {
            keyPreference!!.setDefaultValue(configuration.getMailGunApiKey())
            keyPreference!!.summary = configuration.getMailGunApiKey()
        }

        if (!TextUtils.isEmpty(configuration.telegramChatId)) {
            telegramChatIdPreference!!.setDefaultValue(configuration.telegramChatId)
            telegramChatIdPreference!!.summary = configuration.telegramChatId
        }

        if (!TextUtils.isEmpty(configuration.telegramToken)) {
            telegramTokenPreference!!.setDefaultValue(configuration.telegramToken)
            telegramTokenPreference!!.summary = configuration.telegramToken
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {

        when (key) {
            Configuration.PREF_MAIL_TO -> {
                val value = tolPreference!!.text
                configuration.setMailTo(value)
                tolPreference!!.summary = value
            }
            Configuration.PREF_MAIL_FROM -> {
                val value = fromPreference!!.text
                configuration.setMailFrom(value)
                fromPreference!!.summary = value
            }
            Configuration.PREF_MAIL_URL -> {
                val value = domainPreference!!.text
                configuration.setMailGunUrl(value)
                domainPreference!!.summary = value
            }
            Configuration.PREF_MAIL_API_KEY -> {
                val value = keyPreference!!.text
                configuration.setMailGunApiKey(value)
                keyPreference!!.summary = value
            }
            Configuration.PREF_TELEGRAM_CHAT_ID -> {
                val value = telegramChatIdPreference!!.text
                configuration.telegramChatId = value
                telegramChatIdPreference!!.summary = value
            }
            Configuration.PREF_TELEGRAM_TOKEN -> {
                val value = telegramTokenPreference!!.text
                configuration.telegramToken = value
                telegramTokenPreference!!.summary = value
            }
            Configuration.PREF_CAMERA_CAPTURE -> {
                val checked = activePreference!!.isChecked
                configuration.setHasCameraCapture(checked)
            }
            getString(R.string.key_setting_camera_enabled) -> {
                configuration.cameraEnabled = cameraPreference!!.isChecked
            }
            getString(R.string.key_setting_camera_motionenabled) -> {
                configuration.cameraMotionEnabled = motionPreference!!.isChecked
            }
            getString(R.string.key_setting_camera_motionwake) -> {
                configuration.cameraMotionWake = motionWakePreference!!.isChecked
            }
            getString(R.string.key_setting_camera_faceenabled) -> {
                configuration.cameraFaceEnabled = facePreference!!.isChecked
            }
            getString(R.string.key_setting_camera_facewake) -> {
                configuration.cameraFaceWake = faceWakePreference!!.isChecked
            }
            getString(R.string.key_setting_http_mjpegenabled) -> {
                configuration.httpMJPEGEnabled = httpMjpegPreference!!.isChecked
            }
            getString(R.string.key_setting_camera_qrcodeenabled) -> {
                configuration.cameraQRCodeEnabled = qrCodePreference!!.isChecked
            }
            getString(R.string.key_setting_camera_motionleniency) -> {
                try {
                    val value = motionLeniencyPreference!!.text
                    if(!TextUtils.isEmpty(value)) {
                        configuration.cameraMotionLeniency = value.toInt()
                        motionLeniencyPreference!!.summary = value
                    } else if (isAdded) {
                        Toast.makeText(activity, R.string.text_error_blank_entry, Toast.LENGTH_LONG).show()
                        motionLeniencyPreference!!.setDefaultValue(configuration.cameraMotionLeniency)
                        motionLeniencyPreference!!.setSummary(configuration.cameraMotionLeniency)
                    }
                } catch (e : Exception) {
                    if(isAdded) {
                        Toast.makeText(activity, R.string.text_error_only_numbers, Toast.LENGTH_LONG).show()
                        motionLeniencyPreference!!.setDefaultValue(configuration.cameraMotionLeniency)
                        motionLeniencyPreference!!.setSummary(configuration.cameraMotionLeniency)
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
                        motionLumaPreference!!.setDefaultValue(configuration.cameraMotionMinLuma)
                        motionLumaPreference!!.setSummary(configuration.cameraMotionMinLuma)
                    }
                } catch (e : Exception) {
                    if(isAdded) {
                        Toast.makeText(activity, R.string.text_error_only_numbers, Toast.LENGTH_LONG).show()
                        motionLumaPreference!!.setDefaultValue(configuration.cameraMotionMinLuma)
                        motionLumaPreference!!.setSummary(configuration.cameraMotionMinLuma)
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
                        motionClearPreference!!.setDefaultValue(configuration.motionResetTime)
                        motionClearPreference!!.setSummary(configuration.motionResetTime)
                    }
                } catch (e : Exception) {
                    if(isAdded) {
                        Toast.makeText(activity, R.string.text_error_only_numbers, Toast.LENGTH_LONG).show()
                        motionClearPreference!!.setDefaultValue(configuration.motionResetTime)
                        motionClearPreference!!.setSummary(configuration.motionResetTime)
                    }
                }
            }
            getString(R.string.key_setting_http_mjpegmaxstreams) -> {
                try {
                    val value = httpMjpegStreamsPreference!!.text
                    if(!TextUtils.isEmpty(value)) {
                        configuration.motionResetTime = value.toInt()
                        httpMjpegStreamsPreference!!.summary = value
                    } else if (isAdded) {
                        Toast.makeText(activity, R.string.text_error_blank_entry, Toast.LENGTH_LONG).show()
                        httpMjpegStreamsPreference!!.setDefaultValue(configuration.httpMJPEGMaxStreams)
                        httpMjpegStreamsPreference!!.setSummary(configuration.httpMJPEGMaxStreams)
                    }
                } catch (e : Exception) {
                    if(isAdded) {
                        Toast.makeText(activity, R.string.text_error_only_numbers, Toast.LENGTH_LONG).show()
                        httpMjpegStreamsPreference!!.setDefaultValue(configuration.httpMJPEGMaxStreams)
                        httpMjpegStreamsPreference!!.setSummary(configuration.httpMJPEGMaxStreams)
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
                        httpPortPreference!!.setDefaultValue(configuration.httpPort)
                        httpPortPreference!!.setSummary(configuration.httpPort)
                    }
                } catch (e : Exception) {
                    if(isAdded) {
                        Toast.makeText(activity, R.string.text_error_only_numbers, Toast.LENGTH_LONG).show()
                        httpPortPreference!!.setDefaultValue(configuration.httpPort)
                        httpPortPreference!!.setSummary(configuration.httpPort)
                    }
                }
            }
            getString(R.string.key_setting_camera_fps) -> {
                try {
                    val value = fpsPreference!!.text
                    if(!TextUtils.isEmpty(value)) {
                        configuration.cameraFPS = value.toFloat()
                        fpsPreference!!.summary = value
                    } else if (isAdded) {
                        Toast.makeText(activity, R.string.text_error_blank_entry, Toast.LENGTH_LONG).show()
                        fpsPreference!!.setDefaultValue(configuration.cameraFPS.toString())
                        fpsPreference!!.summary = configuration.cameraFPS.toString()
                    }
                } catch (e : Exception) {
                    if(isAdded) {
                        Toast.makeText(activity, R.string.text_error_only_numbers, Toast.LENGTH_LONG).show()
                        fpsPreference!!.setDefaultValue(configuration.cameraFPS.toString())
                        fpsPreference!!.summary = configuration.cameraFPS.toString()
                    }
                }
            }
        }
    }

    private fun createCameraList() {
        Timber.d("createCameraList")
        try {
            val cameraList = CameraUtils.getCameraListError(activity!!)
            cameraListPreference!!.entries = cameraList.toTypedArray<CharSequence>()
            val vals = arrayOfNulls<CharSequence>(cameraList.size)
            for (i in cameraList.indices) {
                vals[i] = Integer.toString(i)
            }
            cameraListPreference?.entryValues = vals
            val index = cameraListPreference!!.findIndexOfValue(configuration.cameraId.toString())
            cameraListPreference!!.summary = if (index >= 0)
                cameraListPreference!!.entries[index]
            else
                ""
            cameraListPreference!!.isEnabled = true
        } catch (e: Exception) {
            Timber.e(e.message)
            cameraListPreference!!.isEnabled = false
            if(activity != null) {
                Toast.makeText(activity!!, getString(R.string.toast_camera_source_error), Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun startCameraTest(c: Context) {
        startActivity(Intent(c, LiveCameraActivity::class.java))
    }
}