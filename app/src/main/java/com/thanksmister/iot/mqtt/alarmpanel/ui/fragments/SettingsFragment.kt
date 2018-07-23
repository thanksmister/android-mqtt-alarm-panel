/*
 * Copyright (c) 2017 ThanksMister LLC
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
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.thanksmister.iot.mqtt.alarmpanel.BaseFragment
import com.thanksmister.iot.mqtt.alarmpanel.R
import kotlinx.android.synthetic.main.fragment_settings.*

class SettingsFragment : BaseFragment() {

    private var listener: SettingsFragmentListener? = null

    private val ALARM_SETTINGS = 1
    private val MQTT_SETTINGS = 2
    private val DEVICE_SENSOR_SETTINGS = 3
    private val NOTIFICATIONS_SETTINGS = 4
    private val CAMERA_SETTINGS = 5
    private val SCREEN_SAVER_SETTINGS = 6
    private val WEATHER_SETTINGS = 7
    private val PLATFORM_SETTINGS = 8
    private val ABOUT_SETTINGS = 9

    interface SettingsFragmentListener {
        fun navigatePageNumber(page:Int)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is SettingsFragmentListener) {
            listener = context
        } else {
            throw RuntimeException(context!!.toString() + " must implement SettingsFragmentListener")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        buttonAlarmSettings.setOnClickListener { listener?.navigatePageNumber(ALARM_SETTINGS) }
        buttonMqttSettings.setOnClickListener { listener?.navigatePageNumber(MQTT_SETTINGS) }
        buttonNotificationSettings.setOnClickListener { listener?.navigatePageNumber(NOTIFICATIONS_SETTINGS) }
        buttonCameraSettings.setOnClickListener { listener?.navigatePageNumber(CAMERA_SETTINGS) }
        buttonScreenSaverSettings.setOnClickListener { listener?.navigatePageNumber(SCREEN_SAVER_SETTINGS) }
        buttonWeatherSettings.setOnClickListener { listener?.navigatePageNumber(WEATHER_SETTINGS) }
        buttonPlatformSettings.setOnClickListener { listener?.navigatePageNumber(PLATFORM_SETTINGS) }
        buttonAboutSettings.setOnClickListener { listener?.navigatePageNumber(ABOUT_SETTINGS) }
        buttonSensorSettings.setOnClickListener { listener?.navigatePageNumber(DEVICE_SENSOR_SETTINGS) }
    }

    companion object {
        fun newInstance(): SettingsFragment {
            return SettingsFragment()
        }
    }
}