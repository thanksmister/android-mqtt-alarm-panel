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

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.support.v14.preference.SwitchPreference
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.preference.*
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.navigation.Navigation
import com.thanksmister.iot.mqtt.alarmpanel.BaseActivity
import com.thanksmister.iot.mqtt.alarmpanel.R
import com.thanksmister.iot.mqtt.alarmpanel.persistence.Configuration
import com.thanksmister.iot.mqtt.alarmpanel.ui.activities.LiveCameraActivity
import com.thanksmister.iot.mqtt.alarmpanel.ui.activities.SettingsActivity
import com.thanksmister.iot.mqtt.alarmpanel.utils.CameraUtils
import com.thanksmister.iot.mqtt.alarmpanel.utils.DialogUtils
import dagger.android.support.AndroidSupportInjection
import timber.log.Timber
import javax.inject.Inject

class CameraSettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

    @Inject lateinit var configuration: Configuration
    @Inject lateinit var dialogUtils: DialogUtils

    private var cameraListPreference: ListPreference? = null
    private var cameraTestPreference: Preference? = null
    private var cameraPreference: SwitchPreference? = null
    private var fpsPreference: EditTextPreference? = null

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
    }

    override fun onPause() {
        super.onPause()
        preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        cameraPreference = findPreference(getString(R.string.key_setting_camera_enabled)) as SwitchPreference
        cameraPreference!!.isChecked = configuration.cameraEnabled

        fpsPreference = findPreference(getString(R.string.key_setting_camera_fps)) as EditTextPreference
        fpsPreference!!.setDefaultValue(configuration.cameraFPS.toString())
        fpsPreference!!.summary = getString(R.string.pref_camera_fps_summary, configuration.cameraFPS.toInt().toString())

        cameraListPreference = findPreference(getString(R.string.key_setting_camera_cameraid)) as ListPreference
        cameraListPreference!!.setOnPreferenceChangeListener { preference, newValue ->
            if (preference is ListPreference) {
                val index = preference.findIndexOfValue(newValue.toString())
                preference.setSummary(
                        if (index >= 0)
                            preference.entries[index]
                        else
                            "")

                if(index >= 0) {
                    //configuration.cameraId = index
                    Timber.d("Camera Id: " + configuration.cameraId)
                    configuration.cameraId = index
                }
            }
            true;
        }
        cameraListPreference!!.isEnabled = false

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (ActivityCompat.checkSelfPermission(activity!!.applicationContext, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                createCameraList()
            }
        } else {
            createCameraList()
        }


        cameraTestPreference = findPreference("button_key_camera_test")
        cameraTestPreference!!.onPreferenceClickListener = Preference.OnPreferenceClickListener { preference ->
            startCameraTest(preference.context)
            false
        }

        val motionPreference = findPreference("button_key_motion")
        motionPreference!!.onPreferenceClickListener = Preference.OnPreferenceClickListener { preference ->
            view.let { Navigation.findNavController(it).navigate(R.id.motion_action) }
            false
        }

        val facePreference = findPreference("button_key_face")
        facePreference!!.onPreferenceClickListener = Preference.OnPreferenceClickListener { preference ->
            view.let { Navigation.findNavController(it).navigate(R.id.face_action) }
            false
        }

        val qrPreference = findPreference("button_key_qr")
        qrPreference!!.onPreferenceClickListener = Preference.OnPreferenceClickListener { preference ->
            view.let { Navigation.findNavController(it).navigate(R.id.qrcode_action) }
            false
        }

        val mjpegPreference = findPreference("button_key_mjpeg")
        mjpegPreference!!.onPreferenceClickListener = Preference.OnPreferenceClickListener { preference ->
            view.let { Navigation.findNavController(it).navigate(R.id.mjpeg_action) }
            false
        }

        val capturePreference = findPreference("button_key_capture")
        capturePreference!!.onPreferenceClickListener = Preference.OnPreferenceClickListener { preference ->
            view.let { Navigation.findNavController(it).navigate(R.id.capture_action) }
            false
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (ActivityCompat.checkSelfPermission(activity as BaseActivity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                configuration.cameraEnabled = false
                requestCameraPermissions()
                //dialogUtils.showAlertDialog(activity as BaseActivity, getString(R.string.dialog_no_camera_permissions))
                return
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            SettingsActivity.PERMISSIONS_REQUEST_CAMERA -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    createCameraList()
                } else {
                    configuration.cameraEnabled = false
                    cameraPreference!!.isChecked = false
                }
            }
        }
    }

    private fun requestCameraPermissions() {
        Timber.d("requestCameraPermissions")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Timber.d("requestCameraPermissions asking")
            if (PackageManager.PERMISSION_DENIED == ContextCompat.checkSelfPermission(activity!!, android.Manifest.permission.CAMERA)) {
                ActivityCompat.requestPermissions(activity!!,
                        arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE),
                        SettingsActivity.PERMISSIONS_REQUEST_CAMERA)
            }
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {

        when (key) {
            getString(R.string.key_setting_camera_enabled) -> {
                configuration.cameraEnabled = cameraPreference!!.isChecked
            }
            getString(R.string.key_setting_camera_fps) -> {
                try {
                    val value = fpsPreference!!.text
                    if(!TextUtils.isEmpty(value)) {
                        configuration.cameraFPS = value.toFloat()
                        fpsPreference!!.summary = getString(R.string.pref_camera_fps_summary, configuration.cameraFPS.toString())
                    } else if (isAdded) {
                        Toast.makeText(activity, R.string.text_error_blank_entry, Toast.LENGTH_LONG).show()
                        fpsPreference!!.setDefaultValue(configuration.cameraFPS.toString())
                        fpsPreference!!.summary = getString(R.string.pref_camera_fps_summary, configuration.cameraFPS.toString())
                    }
                } catch (e : Exception) {
                    if(isAdded) {
                        Toast.makeText(activity, R.string.text_error_only_numbers, Toast.LENGTH_LONG).show()
                        fpsPreference!!.setDefaultValue(configuration.cameraFPS.toString())
                        fpsPreference!!.summary = getString(R.string.pref_camera_fps_summary, configuration.cameraFPS.toString())
                    }
                }
            }
        }
    }

    private fun createCameraList() {
        Timber.d("createCameraList")
        try {
            val cameraList = CameraUtils.getCameraList(activity!!)
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

    private val bindPreferenceSummaryToValueListener = Preference.OnPreferenceChangeListener { preference, value ->
        val stringValue = value.toString()
        if (preference is SwitchPreference) {
            return@OnPreferenceChangeListener true
        }else if (preference is ListPreference) {
            val index = preference.findIndexOfValue(stringValue)
            preference.setSummary(
                    if (index >= 0)
                        preference.entries[index]
                    else null)
        } else {
            preference.summary = stringValue
        }
        true
    }

    fun bindPreferenceSummaryToValue(preference: Preference) {
        preference.onPreferenceChangeListener = bindPreferenceSummaryToValueListener
        if (preference is SwitchPreference) {
            bindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                    PreferenceManager
                            .getDefaultSharedPreferences(preference.getContext())
                            .getBoolean(preference.getKey(), false))
        } else {
            bindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                    PreferenceManager
                            .getDefaultSharedPreferences(preference.context)
                            .getString(preference.key, "")!!)
        }
    }

    private fun startCameraTest(c: Context) {
        startActivity(Intent(c, LiveCameraActivity::class.java))
    }

    companion object {
        const val PERMISSIONS_REQUEST_CAMERA = 201
    }
}