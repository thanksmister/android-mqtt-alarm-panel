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

import android.app.TimePickerDialog
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
import android.support.v7.preference.*
import android.text.TextUtils
import android.view.View
import android.widget.Toast

import com.thanksmister.iot.mqtt.alarmpanel.R
import com.thanksmister.iot.mqtt.alarmpanel.network.ImageOptions
import com.thanksmister.iot.mqtt.alarmpanel.persistence.Configuration
import com.thanksmister.iot.mqtt.alarmpanel.utils.DateUtils

import com.thanksmister.iot.mqtt.alarmpanel.utils.DateUtils.SECONDS_VALUE
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class ScreenSettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

    @Inject lateinit var configuration: Configuration
    @Inject lateinit var imageOptions: ImageOptions
 
    private var clockSaverPreference: CheckBoxPreference? = null
    private var photoSaverPreference: CheckBoxPreference? = null
    private var urlPreference: EditTextPreference? = null
    private var clientIdPreference: EditTextPreference? = null
    private var imageFitPreference: CheckBoxPreference? = null
    private var fullScreenPreference: CheckBoxPreference? = null
    private var rotationPreference: EditTextPreference? = null
    private var inactivityPreference: ListPreference? = null
    private var dayNightPreference: CheckBoxPreference? = null
    private var preventSleepPreference: CheckBoxPreference? = null
    private var screenBrightness: SwitchPreference? = null
    private var startTimePreference: Preference? = null
    private var endTimePreference: Preference? = null

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

    override fun onDetach() {
        super.onDetach()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        clockSaverPreference = findPreference(Configuration.PREF_MODULE_CLOCK_SAVER) as CheckBoxPreference
        photoSaverPreference = findPreference(Configuration.PREF_MODULE_PHOTO_SAVER) as CheckBoxPreference
        clientIdPreference = findPreference(Configuration.PREF_IMAGE_CLIENT_ID) as EditTextPreference
        urlPreference = findPreference(Configuration.PREF_IMAGE_SOURCE) as EditTextPreference
        imageFitPreference = findPreference(Configuration.PREF_IMAGE_FIT_SIZE) as CheckBoxPreference
        rotationPreference = findPreference(Configuration.PREF_IMAGE_ROTATION) as EditTextPreference
        inactivityPreference = findPreference(Configuration.PREF_INACTIVITY_TIME) as ListPreference
        fullScreenPreference = findPreference(Configuration.PREF_FULL_SCREEN) as CheckBoxPreference
        preventSleepPreference = findPreference(getString(R.string.key_setting_app_preventsleep)) as CheckBoxPreference
        screenBrightness = findPreference(Configuration.PREF_SCREEN_BRIGHTNESS) as SwitchPreference

        dayNightPreference = findPreference(Configuration.PREF_DAY_NIGHT_MODE) as CheckBoxPreference
        dayNightPreference!!.isChecked = configuration.useNightDayMode

        startTimePreference = findPreference(Configuration.PREF_MODE_DAY_NIGHT_START) as Preference
        startTimePreference!!.isPersistent = false
        startTimePreference?.summary = getString(R.string.pref_dark_mode_start_time, configuration.dayNightModeStartTime)
        startTimePreference!!.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            showStartTimePicker()
            true
        }

        endTimePreference = findPreference(Configuration.PREF_MODE_DAY_NIGHT_END) as Preference
        endTimePreference!!.isPersistent = false
        endTimePreference?.summary = getString(R.string.pref_dark_mode_end_time, configuration.dayNightModeEndTime)
        endTimePreference!!.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            showEndTimePicker()
            true
        }

        rotationPreference!!.text = imageOptions.imageRotation.toString()
        rotationPreference!!.summary = getString(R.string.preference_summary_image_rotation, imageOptions.imageRotation.toString())
        rotationPreference!!.setDefaultValue(imageOptions.imageRotation.toString())

        urlPreference!!.text = imageOptions.imageSource
        urlPreference!!.summary = getString(R.string.preference_summary_image_source)

        clientIdPreference!!.text = imageOptions.imageClientId
        clientIdPreference!!.summary = getString(R.string.preference_summary_image_client_id)

        inactivityPreference!!.setDefaultValue(configuration.inactivityTime)
        inactivityPreference!!.value = configuration.inactivityTime.toString()

        if (configuration.inactivityTime < SECONDS_VALUE) {
            inactivityPreference!!.summary = getString(R.string.preference_summary_inactivity_seconds,
                    DateUtils.convertInactivityTime(configuration.inactivityTime))
        } else {
            inactivityPreference!!.summary = getString(R.string.preference_summary_inactivity_minutes,
                    DateUtils.convertInactivityTime(configuration.inactivityTime))
        }

        imageFitPreference!!.isChecked = imageOptions.imageFitScreen
        fullScreenPreference!!.isChecked = configuration.fullScreen

        setPhotoScreenSaver(configuration.showPhotoScreenSaver())
        setClockScreenSaver(configuration.showClockScreenSaverModule())

        setInactivityPreference(configuration.showClockScreenSaverModule(), configuration.showPhotoScreenSaver() )

        // check if we have screen brightness permissions and reset the screen brightness values
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val enabled = Settings.System.canWrite(activity!!.applicationContext)
            screenBrightness!!.isEnabled = enabled
            screenBrightness!!.isChecked = enabled
            configuration.useScreenBrightness = enabled
            try {
                val brightness = Settings.System.getInt(activity!!.contentResolver, Settings.System.SCREEN_BRIGHTNESS)
                configuration.screenBrightness = brightness
                configuration.screenNightBrightness = (brightness*.5).toInt()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        checkWriteSettings()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (requestCode == PERMISSIONS_REQUEST_WRITE_SETTINGS) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && activity != null) {
                if (Settings.System.canWrite(activity!!.applicationContext)) {
                    Toast.makeText(activity!!, "Write settings permission granted…", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(activity!!, "Write settings permission granted…", Toast.LENGTH_LONG).show()
                }
            }
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val enabled = Settings.System.canWrite(activity!!.applicationContext)
                screenBrightness!!.isEnabled = enabled
                screenBrightness!!.isChecked = enabled
                configuration.useScreenBrightness = enabled
                try {
                    val brightness = Settings.System.getInt(activity!!.contentResolver, Settings.System.SCREEN_BRIGHTNESS)
                    configuration.screenBrightness = brightness
                    configuration.screenNightBrightness = (brightness*.5).toInt()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun checkWriteSettings() {
        if (!configuration.writeScreenPermissionsShown && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && activity != null) {
            if (Settings.System.canWrite(activity!!.applicationContext)) {
                val enabled = Settings.System.canWrite(activity!!.applicationContext)
                screenBrightness!!.isEnabled = enabled
                screenBrightness!!.isChecked = enabled
                configuration.useScreenBrightness = enabled
                try {
                    val brightness = Settings.System.getInt(activity!!.contentResolver, Settings.System.SCREEN_BRIGHTNESS)
                    configuration.screenBrightness = brightness
                    configuration.screenNightBrightness = (brightness*.5).toInt()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else if (!configuration.writeScreenPermissionsShown) {
                // launch the dialog to provide permissions
                configuration.writeScreenPermissionsShown = true
                AlertDialog.Builder(activity!!)
                        .setMessage("Do you want to grant permission to modify the system settings for screen brightness?")
                        .setPositiveButton(android.R.string.ok) { _, _ ->
                            launchWriteSettings()
                        }
                        .setNegativeButton(android.R.string.cancel) { _, _ ->
                            Toast.makeText(activity!!, "Write settings permission denied…", Toast.LENGTH_LONG).show()
                        }.show()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun launchWriteSettings() {
        if(activity != null) {
            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:${activity!!.applicationContext.packageName}"))
            startActivityForResult(intent, 200)
        }
    }

    private fun setPhotoScreenSaver (value : Boolean) {
        photoSaverPreference!!.isChecked = value
        imageFitPreference!!.isEnabled = value
        rotationPreference!!.isEnabled = value
        urlPreference!!.isEnabled = value
        clientIdPreference!!.isEnabled = value
        configuration.setPhotoScreenSaver(value)
    }

    private fun setClockScreenSaver(value: Boolean) {
        clockSaverPreference!!.isChecked = value
        configuration.setClockScreenSaverModule(value)
    }

    private fun setInactivityPreference(clock: Boolean, photo:Boolean) {
        inactivityPreference!!.isEnabled = (clock || photo)
    }

    // TODO show time using 12 or 24 hour clock
    private fun showStartTimePicker() {
        val hour = DateUtils.getHourFromTimePicker(configuration.dayNightModeStartTime)
        val minute = DateUtils.getMinutesFromTimePicker(configuration.dayNightModeStartTime)
        val timePicker: TimePickerDialog
        timePicker = TimePickerDialog(context, TimePickerDialog.OnTimeSetListener { _, selectedHour, selectedMinute ->
            val hourOut = DateUtils.padTimePickerOutput(selectedHour.toString())
            val minuteOut = DateUtils.padTimePickerOutput(selectedMinute.toString())
            val output = "$hourOut:$minuteOut"
            startTimePreference?.summary = getString(R.string.pref_dark_mode_start_time, output)
            configuration.dayNightModeStartTime = output
        }, hour, minute, true) //Yes 24 hour time
        timePicker.setTitle(getString(R.string.dialog_select_time))
        timePicker.show()
    }

    // TODO show time using 12 or 24 hour clock
    private fun showEndTimePicker() {
        val hour = DateUtils.getHourFromTimePicker(configuration.dayNightModeEndTime)
        val minute = DateUtils.getMinutesFromTimePicker(configuration.dayNightModeEndTime)
        val timePicker: TimePickerDialog
        timePicker = TimePickerDialog(context, TimePickerDialog.OnTimeSetListener { _, selectedHour, selectedMinute ->
            val hourOut = DateUtils.padTimePickerOutput(selectedHour.toString())
            val minuteOut = DateUtils.padTimePickerOutput(selectedMinute.toString())
            val output = "$hourOut:$minuteOut"
            endTimePreference?.summary = getString(R.string.pref_dark_mode_end_time, output)
            configuration.dayNightModeEndTime = output
        }, hour, minute, true) //Yes 24 hour time
        timePicker.setTitle(getString(R.string.dialog_select_time))
        timePicker.show()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {

        when (key) {
            Configuration.PREF_MODULE_CLOCK_SAVER -> {
                val checked = clockSaverPreference!!.isChecked
                setClockScreenSaver(checked)
                if(checked) {
                    setPhotoScreenSaver(false)
                }
                setInactivityPreference(checked, configuration.showPhotoScreenSaver())
            }
            Configuration.PREF_MODULE_PHOTO_SAVER -> {
                val checked = photoSaverPreference!!.isChecked
                setPhotoScreenSaver(checked)
                if(checked) {
                    setClockScreenSaver(false)
                }
                setInactivityPreference(configuration.showClockScreenSaverModule(), checked)
            }
            Configuration.PREF_IMAGE_SOURCE -> {
                val value = urlPreference!!.text
                if(TextUtils.isEmpty(value)) {
                    Toast.makeText(activity, R.string.text_error_blank_entry, Toast.LENGTH_LONG).show()
                    urlPreference!!.text = imageOptions.imageSource
                } else {
                    imageOptions.imageSource = value
                }
            }
            Configuration.PREF_IMAGE_CLIENT_ID -> {
                val value = clientIdPreference!!.text
                if(TextUtils.isEmpty(value)) {
                    Toast.makeText(activity, R.string.text_error_blank_entry, Toast.LENGTH_LONG).show()
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
                try {
                    val brightness = Settings.System.getInt(activity!!.contentResolver, Settings.System.SCREEN_BRIGHTNESS)
                    configuration.screenBrightness = brightness
                    configuration.screenNightBrightness = (brightness*.4).toInt()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            Configuration.PREF_IMAGE_ROTATION -> {
                val rotation = Integer.valueOf(rotationPreference!!.text)!!
                imageOptions.imageRotation = rotation
                rotationPreference!!.summary = getString(R.string.preference_summary_image_rotation, rotation.toString())
            }
            Configuration.PREF_INACTIVITY_TIME -> {
                val inactivity = inactivityPreference!!.value!!.toLong()
                configuration.inactivityTime = inactivity
                if (inactivity < SECONDS_VALUE) {
                    inactivityPreference!!.summary = getString(R.string.preference_summary_inactivity_seconds, DateUtils.convertInactivityTime(inactivity))
                } else {
                    inactivityPreference!!.summary = getString(R.string.preference_summary_inactivity_minutes, DateUtils.convertInactivityTime(inactivity))
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

        }
    }

    companion object {
        const val PERMISSIONS_REQUEST_WRITE_SETTINGS = 200
    }
}