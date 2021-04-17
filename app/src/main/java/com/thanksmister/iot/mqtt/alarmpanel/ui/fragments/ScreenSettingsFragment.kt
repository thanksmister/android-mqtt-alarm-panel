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
import android.text.TextUtils
import android.view.View
import android.webkit.URLUtil
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.preference.*
import com.thanksmister.iot.mqtt.alarmpanel.R
import com.thanksmister.iot.mqtt.alarmpanel.network.ImageOptions
import com.thanksmister.iot.mqtt.alarmpanel.persistence.Configuration
import com.thanksmister.iot.mqtt.alarmpanel.persistence.Configuration.Companion.WEB_SCREEN_SAVER
import com.thanksmister.iot.mqtt.alarmpanel.ui.activities.SettingsActivity
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
    private var unsplashScreenSaver: SwitchPreference? = null
    private var hardwareAcceleration: SwitchPreference? = null
    private var webSaverPreference: SwitchPreference? = null

    private var fullScreenPreference: SwitchPreference? = null
    private var rotationPreference: EditTextPreference? = null
    private var webUrlPreference: EditTextPreference? = null
    private var inactivityPreference: ListPreference? = null
    private var dayNightPreference: SwitchPreference? = null
    private var preventSleepPreference: SwitchPreference? = null
    private var screenBrightness: SwitchPreference? = null
    private var dimPreference: ListPreference? = null
    private var brightnessPreference: Preference? = null

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if((activity as SettingsActivity).supportActionBar != null) {
            (activity as SettingsActivity).supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            (activity as SettingsActivity).supportActionBar!!.setDisplayShowHomeEnabled(true)
            (activity as SettingsActivity).supportActionBar!!.title = (getString(R.string.preference_title_display))
        }
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

        clockSaverPreference = findPreference("pref_settings_clock_saver") as SwitchPreference
        unsplashScreenSaver = findPreference("pref_screensaver_wallpaper") as SwitchPreference

        hardwareAcceleration = findPreference("pref_settings_hardware_acceleration") as SwitchPreference

        webSaverPreference = findPreference(getString(R.string.key_setting_web_screensaver)) as SwitchPreference
        webUrlPreference = findPreference(getString(R.string.key_setting_web_url)) as EditTextPreference

        rotationPreference = findPreference("pref_settings_image_rotation") as EditTextPreference

        inactivityPreference = findPreference("pref_settings_inactivity_time") as ListPreference
        fullScreenPreference = findPreference("pref_settings_fullscreen") as SwitchPreference
        preventSleepPreference = findPreference(getString(R.string.key_setting_app_preventsleep)) as SwitchPreference
        screenBrightness = findPreference("pref_settings_screen_brightness") as SwitchPreference
        dayNightPreference = findPreference("pref_settings_day_night") as SwitchPreference
        dimPreference = findPreference("pref_settings_dim") as ListPreference
        brightnessPreference = findPreference("pref_settings_brightness") as Preference

        webUrlPreference?.text = configuration.webScreenSaverUrl
        dayNightPreference?.isChecked = configuration.useNightDayMode
        hardwareAcceleration?.isChecked = configuration.userHardwareAcceleration

        rotationPreference?.text = imageOptions.imageRotation.toString()
        rotationPreference?.summary = getString(R.string.preference_summary_image_rotation, imageOptions.imageRotation.toString())
        rotationPreference?.setDefaultValue(imageOptions.imageRotation.toString())

        inactivityPreference?.setDefaultValue(configuration.inactivityTime)
        inactivityPreference?.value = configuration.inactivityTime.toString()

        if (configuration.inactivityTime < SECONDS_VALUE) {
            inactivityPreference?.summary = getString(R.string.preference_summary_inactivity_seconds,
                    DateUtils.convertInactivityTime(configuration.inactivityTime))
        } else {
            inactivityPreference?.summary = getString(R.string.preference_summary_inactivity_minutes,
                    DateUtils.convertInactivityTime(configuration.inactivityTime))
        }

        dimPreference?.setDefaultValue(configuration.nightModeDimValue)
        dimPreference?.value = configuration.nightModeDimValue.toString()
        dimPreference?.summary = getString(R.string.preference_summary_dim_screensaver, configuration.nightModeDimValue.toString())

        brightnessPreference!!.onPreferenceClickListener = Preference.OnPreferenceClickListener { preference ->
            screenUtils.setScreenBrightnessLevels()
            Toast.makeText(requireContext(), getString(R.string.toast_screen_brightness_captured), Toast.LENGTH_SHORT).show()
            false
        }

        fullScreenPreference?.isChecked = configuration.fullScreen

        setUnsplashScreenSaver(configuration.showUnsplashScreenSaver())
        setClockScreenSaver(configuration.showClockScreenSaver())
        setWebScreenSaver(configuration.showWebScreenSaver())
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (requestCode == PERMISSIONS_REQUEST_WRITE_SETTINGS) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.System.canWrite(requireActivity().applicationContext)) {
                    Toast.makeText(requireActivity(), getString(R.string.toast_write_permissions_granted), Toast.LENGTH_LONG).show()
                    screenBrightness?.isChecked = true
                    configuration.useScreenBrightness = true
                    Toast.makeText(requireContext(), getString(R.string.toast_screen_brightness_captured), Toast.LENGTH_SHORT).show()
                    screenUtils.setScreenBrightnessLevels()
                } else {
                    Toast.makeText(requireActivity(), getString(R.string.toast_write_permissions_denied), Toast.LENGTH_LONG).show()
                    configuration.useScreenBrightness = false
                    screenBrightness?.isChecked = false
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

    private fun setUnsplashScreenSaver (value : Boolean) {
        unsplashScreenSaver?.isChecked = value
        configuration.setUnsplashScreenSaver(value)
        if(value == true) {
            setWebScreenSaver(false)
        }
    }

    private fun setClockScreenSaver(value: Boolean) {
        clockSaverPreference?.isChecked = value
        configuration.setClockScreenSaverModule(value)
    }

    private fun setWebScreenSaver(value: Boolean) {
        webSaverPreference?.isChecked = value
        configuration.webScreenSaver = value
        if(value == true) {
            setUnsplashScreenSaver(false)
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when (key) {
            "pref_settings_clock_saver" -> {
                clockSaverPreference?.let {
                    val checked = it.isChecked
                    setClockScreenSaver(checked)
                }
            }
            "pref_screensaver_wallpaper" -> {
                unsplashScreenSaver?.let {
                    val checked = it.isChecked
                    setUnsplashScreenSaver(checked)
                }
            }
            "pref_settings_fullscreen" -> {
                val fullscreen = fullScreenPreference!!.isChecked
                configuration.fullScreen = fullscreen
            }
            "pref_settings_screen_brightness" -> {
                val useBright = screenBrightness!!.isChecked
                configuration.useScreenBrightness = useBright
                if(useBright) {
                    checkWriteSettings()
                } else {
                    screenUtils.restoreDeviceBrightnessControl()
                }
            }
            "pref_settings_image_rotation"-> {
                val rotationValue = rotationPreference?.text?.toIntOrNull()
                if(rotationValue != null) {
                    imageOptions.imageRotation = rotationValue
                    rotationPreference?.summary = getString(R.string.preference_summary_image_rotation, rotationValue.toString())
                } else {
                    Toast.makeText(requireActivity(), R.string.text_error_only_numbers, Toast.LENGTH_LONG).show()
                    rotationPreference?.text = imageOptions.imageRotation .toString()
                    rotationPreference?.setDefaultValue(imageOptions.imageRotation .toString())
                    rotationPreference?.summary = getString(R.string.preference_summary_image_rotation, rotationValue.toString())
                }
            }
            "pref_settings_inactivity_time" -> {
                val inactivity = inactivityPreference?.value?.toLongOrNull()
                if(inactivity != null) {
                    configuration.inactivityTime = inactivity
                    if (inactivity < SECONDS_VALUE) {
                        inactivityPreference?.summary = getString(R.string.preference_summary_inactivity_seconds, DateUtils.convertInactivityTime(inactivity))
                    } else {
                        inactivityPreference?.summary = getString(R.string.preference_summary_inactivity_minutes, DateUtils.convertInactivityTime(inactivity))
                    }
                } else {
                    Toast.makeText(requireActivity(), R.string.text_error_only_numbers, Toast.LENGTH_LONG).show()
                    inactivityPreference?.setDefaultValue(configuration.inactivityTime.toString())
                    inactivityPreference?.value = configuration.inactivityTime.toString()
                    if (configuration.inactivityTime < SECONDS_VALUE) {
                        inactivityPreference?.summary = getString(R.string.preference_summary_inactivity_seconds,
                                DateUtils.convertInactivityTime(configuration.inactivityTime))
                    } else {
                        inactivityPreference?.summary = getString(R.string.preference_summary_inactivity_minutes,
                                DateUtils.convertInactivityTime(configuration.inactivityTime))
                    }
                }
            }
            "pref_settings_day_night" -> {
                val checked = dayNightPreference!!.isChecked
                configuration.useNightDayMode = checked
                //configuration.nightModeChanged = true
            }
            "pref_settings_hardware_acceleration" -> {
                val checked = hardwareAcceleration!!.isChecked
                configuration.userHardwareAcceleration = checked
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
            "pref_settings_dim" -> {
                val dim = dimPreference?.value?.toIntOrNull()
                if(dim != null) {
                    configuration.nightModeDimValue = dim
                    screenUtils.setScreenBrightnessLevels()
                    dimPreference?.summary = getString(R.string.preference_summary_dim_screensaver, dim.toString())
                } else {
                    Toast.makeText(requireActivity(), R.string.text_error_only_numbers, Toast.LENGTH_LONG).show()
                    dimPreference?.setDefaultValue(configuration.nightModeDimValue)
                    dimPreference?.value = configuration.nightModeDimValue.toString()
                    dimPreference?.summary = getString(R.string.preference_summary_dim_screensaver, configuration.nightModeDimValue.toString())
                }
            }
        }
    }

    companion object {
        const val PERMISSIONS_REQUEST_WRITE_SETTINGS = 200
    }
}