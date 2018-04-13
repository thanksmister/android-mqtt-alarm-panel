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
import android.support.v7.preference.PreferenceFragmentCompat
import android.view.View

import com.thanksmister.iot.mqtt.alarmpanel.BaseActivity
import com.thanksmister.iot.mqtt.alarmpanel.R
import com.thanksmister.iot.mqtt.alarmpanel.network.ImageOptions
import com.thanksmister.iot.mqtt.alarmpanel.ui.Configuration
import com.thanksmister.iot.mqtt.alarmpanel.utils.DateUtils

import com.thanksmister.iot.mqtt.alarmpanel.R.xml.preferences_screen_saver
import com.thanksmister.iot.mqtt.alarmpanel.utils.DateUtils.SECONDS_VALUE
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class ScreenSettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

    @Inject lateinit var configuration: Configuration

    private var clockSaverPreference: CheckBoxPreference? = null
    private var photoSaverPreference: CheckBoxPreference? = null
    private var urlPreference: EditTextPreference? = null
    private var clientIdPreference: EditTextPreference? = null
    private var imageFitPreference: CheckBoxPreference? = null
    private var rotationPreference: EditTextPreference? = null
    private var inactivityPreference: ListPreference? = null
    private var imageOptions: ImageOptions? = null

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(preferences_screen_saver)
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

        if (isAdded && activity != null) {
            imageOptions = (activity as BaseActivity).readImageOptions()
        }

        rotationPreference!!.text = imageOptions!!.imageRotation.toString()
        rotationPreference!!.summary = getString(R.string.preference_summary_image_rotation, imageOptions!!.imageRotation.toString())
        rotationPreference!!.setDefaultValue(imageOptions!!.imageRotation.toString())

        urlPreference!!.text = imageOptions!!.getTag()
        urlPreference!!.summary = getString(R.string.preference_summary_image_source)

        clientIdPreference!!.text = imageOptions!!.getClientId()
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

        imageFitPreference!!.isChecked = imageOptions!!.imageFitScreen

        setPhotoScreenSaver(configuration.showPhotoScreenSaver())
        setClockScreenSaver(configuration.showClockScreenSaverModule())

        setInactivityPreference(configuration.showClockScreenSaverModule(), configuration.showPhotoScreenSaver() )
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
                imageOptions!!.setTag(value)
            }
            Configuration.PREF_IMAGE_CLIENT_ID -> {
                val value = clientIdPreference!!.text
                imageOptions!!.setClientId(value)
            }
            Configuration.PREF_IMAGE_FIT_SIZE -> {
                val fitScreen = imageFitPreference!!.isChecked
                imageOptions!!.imageFitScreen = fitScreen
            }
            Configuration.PREF_IMAGE_ROTATION -> {
                val rotation = Integer.valueOf(rotationPreference!!.text)!!
                imageOptions!!.imageRotation = rotation
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
        }
    }
}