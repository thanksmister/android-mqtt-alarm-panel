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
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.annotation.RequiresApi
import android.support.v14.preference.SwitchPreference
import android.support.v7.app.AlertDialog
import android.support.v7.preference.CheckBoxPreference
import android.support.v7.preference.EditTextPreference
import android.support.v7.preference.ListPreference
import android.support.v7.preference.PreferenceFragmentCompat
import android.text.TextUtils
import android.view.View
import android.webkit.URLUtil
import android.widget.Toast
import com.thanksmister.iot.mqtt.alarmpanel.R
import com.thanksmister.iot.mqtt.alarmpanel.network.ImageOptions
import com.thanksmister.iot.mqtt.alarmpanel.persistence.Configuration
import com.thanksmister.iot.mqtt.alarmpanel.persistence.Configuration.Companion.PREF_BRIGHTNESS_FACTOR
import com.thanksmister.iot.mqtt.alarmpanel.persistence.Configuration.Companion.WEB_SCREEN_SAVER
import com.thanksmister.iot.mqtt.alarmpanel.utils.DateUtils
import com.thanksmister.iot.mqtt.alarmpanel.utils.DateUtils.SECONDS_VALUE
import com.thanksmister.iot.mqtt.alarmpanel.utils.ScreenUtils
import dagger.android.support.AndroidSupportInjection
import timber.log.Timber
import javax.inject.Inject

class ScreenSettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

    @Inject lateinit var configuration: Configuration
    @Inject lateinit var imageOptions: ImageOptions
    @Inject lateinit var screenUtils: ScreenUtils
 
    private var clockSaverPreference: SwitchPreference? = null
    private var photoSaverPreference: SwitchPreference? = null
    private var webSaverPreference: SwitchPreference? = null
    private var urlPreference: EditTextPreference? = null
    private var clientIdPreference: EditTextPreference? = null
    private var imageFitPreference: SwitchPreference? = null
    private var fullScreenPreference: SwitchPreference? = null
    private var rotationPreference: EditTextPreference? = null
    private var webUrlPreference: EditTextPreference? = null
    private var inactivityPreference: ListPreference? = null
    private var dayNightPreference: SwitchPreference? = null
    private var preventSleepPreference: SwitchPreference? = null
    private var screenBrightness: SwitchPreference? = null

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preferences_screen)
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

        clockSaverPreference = findPreference(Configuration.PREF_MODULE_CLOCK_SAVER) as SwitchPreference
        photoSaverPreference = findPreference(getString(R.string.key_saver_photo)) as SwitchPreference
        webSaverPreference = findPreference(getString(R.string.key_setting_web_screensaver)) as SwitchPreference
        clientIdPreference = findPreference(Configuration.PREF_IMAGE_CLIENT_ID) as EditTextPreference
        urlPreference = findPreference(Configuration.PREF_IMAGE_SOURCE) as EditTextPreference
        webUrlPreference = findPreference(getString(R.string.key_setting_web_url)) as EditTextPreference
        imageFitPreference = findPreference(Configuration.PREF_IMAGE_FIT_SIZE) as SwitchPreference
        rotationPreference = findPreference(Configuration.PREF_IMAGE_ROTATION) as EditTextPreference
        inactivityPreference = findPreference(Configuration.PREF_INACTIVITY_TIME) as ListPreference
        fullScreenPreference = findPreference(Configuration.PREF_FULL_SCREEN) as SwitchPreference
        preventSleepPreference = findPreference(getString(R.string.key_setting_app_preventsleep)) as SwitchPreference
        screenBrightness = findPreference(Configuration.PREF_SCREEN_BRIGHTNESS) as SwitchPreference
        dayNightPreference = findPreference(Configuration.PREF_DAY_NIGHT_MODE) as SwitchPreference

        webUrlPreference?.text = configuration.webScreenSaverUrl

        dayNightPreference?.isChecked = configuration.useNightDayMode

        rotationPreference?.text = imageOptions.imageRotation.toString()
        rotationPreference?.summary = getString(R.string.preference_summary_image_rotation, imageOptions.imageRotation.toString())
        rotationPreference?.setDefaultValue(imageOptions.imageRotation.toString())

        urlPreference?.text = imageOptions.imageSource
        urlPreference?.summary = getString(R.string.preference_summary_image_source)

        clientIdPreference?.text = imageOptions.imageClientId
        clientIdPreference?.summary = getString(R.string.preference_summary_image_client_id)

        inactivityPreference?.setDefaultValue(configuration.inactivityTime)
        inactivityPreference?.value = configuration.inactivityTime.toString()

        if (configuration.inactivityTime < SECONDS_VALUE) {
            inactivityPreference?.summary = getString(R.string.preference_summary_inactivity_seconds,
                    DateUtils.convertInactivityTime(configuration.inactivityTime))
        } else {
            inactivityPreference?.summary = getString(R.string.preference_summary_inactivity_minutes,
                    DateUtils.convertInactivityTime(configuration.inactivityTime))
        }

        imageFitPreference?.isChecked = imageOptions.imageFitScreen
        fullScreenPreference?.isChecked = configuration.fullScreen

        setPhotoScreenSaver(configuration.showPhotoScreenSaver())
        setClockScreenSaver(configuration.showClockScreenSaverModule())
        setWebScreenSaver(configuration.webScreenSaver)

        checkWriteSettings()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (requestCode == PERMISSIONS_REQUEST_WRITE_SETTINGS) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.System.canWrite(requireActivity().applicationContext)) {
                    Toast.makeText(requireActivity(), getString(R.string.toast_write_permissions_granted), Toast.LENGTH_LONG).show()
                    screenBrightness?.isChecked = true
                    configuration.useScreenBrightness = true
                } else {
                    Toast.makeText(requireActivity(), getString(R.string.toast_write_permissions_denied), Toast.LENGTH_LONG).show()
                    configuration.useScreenBrightness = false
                }
            }
            screenUtils.setScreenBrightnessLevels()
        }
    }

    private fun checkWriteSettings() {
        Timber.d("checkWriteSettings")
        if (!configuration.writeScreenPermissionsShown && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.System.canWrite(requireActivity().applicationContext)) {
                screenUtils.setScreenBrightnessLevels()
            } else if (!configuration.writeScreenPermissionsShown) {
                // launch the dialog to provide permissions
                configuration.writeScreenPermissionsShown = true
                AlertDialog.Builder(requireActivity())
                        .setMessage(getString(R.string.dialog_brightness_message))
                        .setPositiveButton(android.R.string.ok) { _, _ ->
                            launchWriteSettings()
                        }
                        .setNegativeButton(android.R.string.cancel) { _, _ ->
                            Toast.makeText(requireActivity(), getString(R.string.toast_write_permissions_denied), Toast.LENGTH_LONG).show()
                        }.show()
            }
        } else if (configuration.useScreenBrightness) {
            // rewrite the screen brightness levels until we have a slider in placeß
            screenUtils.setScreenBrightnessLevels()
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun launchWriteSettings() {
        val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:${requireActivity().applicationContext.packageName}"))
        startActivityForResult(intent, 200)
    }

    private fun setPhotoScreenSaver (value : Boolean) {
        photoSaverPreference?.isChecked = value
        imageFitPreference?.isEnabled = value
        rotationPreference?.isEnabled = value
        urlPreference?.isEnabled = value
        clientIdPreference?.isEnabled = value
        configuration.setPhotoScreenSaver(value)
        if(value) {
            setClockScreenSaver(false)
            setWebScreenSaver(false)
        }
    }

    private fun setClockScreenSaver(value: Boolean) {
        clockSaverPreference?.isChecked = value
        configuration.setClockScreenSaverModule(value)
        if(value) {
            setPhotoScreenSaver(false)
            setWebScreenSaver(false)
        }
    }

    private fun setWebScreenSaver(value: Boolean) {
        webSaverPreference?.isChecked = value
        configuration.webScreenSaver = value
        if(value) {
            setPhotoScreenSaver(false)
            setClockScreenSaver(false)
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when (key) {
            Configuration.PREF_MODULE_CLOCK_SAVER -> {
                clockSaverPreference?.let {
                    val checked = it.isChecked
                    setClockScreenSaver(checked)
                }
            }
            getString(R.string.key_saver_photo) -> {
                photoSaverPreference?.let {
                    val checked = it.isChecked
                    setPhotoScreenSaver(checked)
                }
            }
            Configuration.PREF_IMAGE_SOURCE -> {
                val value = urlPreference!!.text
                if(TextUtils.isEmpty(value)) {
                    Toast.makeText(requireActivity(), R.string.text_error_blank_entry, Toast.LENGTH_LONG).show()
                    urlPreference?.text = imageOptions.imageSource
                } else {
                    imageOptions.imageSource = value
                }
            }
            Configuration.PREF_IMAGE_CLIENT_ID -> {
                val value = clientIdPreference!!.text
                if (TextUtils.isEmpty(value)) {
                    Toast.makeText(requireActivity(), R.string.text_error_blank_entry, Toast.LENGTH_LONG).show()
                } else {
                    imageOptions.imageClientId = value
                }
            }
            Configuration.PREF_IMAGE_FIT_SIZE -> {
                val fitScreen = imageFitPreference!!.isChecked
                imageOptions.imageFitScreen = fitScreen
            }
            Configuration.PREF_FULL_SCREEN -> {
                val fullscreen = fullScreenPreference!!.isChecked
                configuration.fullScreen = fullscreen
            }
            Configuration.PREF_SCREEN_BRIGHTNESS -> {
                val useBright = screenBrightness!!.isChecked
                configuration.useScreenBrightness = useBright
                if(useBright) {
                    checkWriteSettings()
                } else {
                    screenUtils.restoreDeviceBrightnessControl()
                }
            }
            Configuration.PREF_IMAGE_ROTATION -> {
                val rotation = Integer.valueOf(rotationPreference?.text)!!
                imageOptions.imageRotation = rotation
                rotationPreference?.summary = getString(R.string.preference_summary_image_rotation, rotation.toString())
            }
            Configuration.PREF_INACTIVITY_TIME -> {
                val inactivity = inactivityPreference?.value!!.toLong()
                configuration.inactivityTime = inactivity
                if (inactivity < SECONDS_VALUE) {
                    inactivityPreference?.summary = getString(R.string.preference_summary_inactivity_seconds, DateUtils.convertInactivityTime(inactivity))
                } else {
                    inactivityPreference?.summary = getString(R.string.preference_summary_inactivity_minutes, DateUtils.convertInactivityTime(inactivity))
                }
            }
            Configuration.PREF_DAY_NIGHT_MODE -> {
                val checked = dayNightPreference!!.isChecked
                configuration.useNightDayMode = checked
                configuration.nightModeChanged = true
            }
            getString(R.string.key_setting_app_preventsleep) -> {
                val checked = preventSleepPreference!!.isChecked
                configuration.appPreventSleep = checked
            }
            getString(R.string.key_setting_web_url) -> {
                val url = webUrlPreference?.text
                if(!TextUtils.isEmpty(url) &&  URLUtil.isValidUrl(url)) {
                    configuration.webScreenSaverUrl = url!!
                } else {
                    configuration.webScreenSaverUrl = WEB_SCREEN_SAVER
                    webUrlPreference?.text = configuration.webScreenSaverUrl
                }
            }
            getString(R.string.key_setting_web_screensaver) -> {
                val checked = webSaverPreference!!.isChecked
                setWebScreenSaver(checked)
            }
        }
    }

    companion object {
        const val PERMISSIONS_REQUEST_WRITE_SETTINGS = 200
    }
}