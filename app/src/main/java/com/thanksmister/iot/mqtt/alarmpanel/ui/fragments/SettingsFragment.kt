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
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.thanksmister.iot.mqtt.alarmpanel.BaseFragment
import com.thanksmister.iot.mqtt.alarmpanel.R
import com.thanksmister.iot.mqtt.alarmpanel.ui.activities.SettingsActivity
import kotlinx.android.synthetic.main.fragment_settings.*

class SettingsFragment : BaseFragment() {

    private var listener: SettingsFragmentListener? = null

    interface SettingsFragmentListener {
        fun navigatePageNumber(page:Int)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if((activity as SettingsActivity).supportActionBar != null) {
            (activity as SettingsActivity).supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            (activity as SettingsActivity).supportActionBar!!.setDisplayShowHomeEnabled(true)
            (activity as SettingsActivity).supportActionBar!!.title = (getString(R.string.activity_settings_title))
        }
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
        buttonAlarmSettings.setOnClickListener { view.let { Navigation.findNavController(it).navigate(R.id.alarm_action) }}
        buttonMqttSettings.setOnClickListener { view.let { Navigation.findNavController(it).navigate(R.id.mqtt_action) } }
        buttonNotificationSettings.setOnClickListener { view.let { Navigation.findNavController(it).navigate(R.id.notifications_action) }}
        buttonCameraSettings.setOnClickListener { view.let { Navigation.findNavController(it).navigate(R.id.camera_action) }}
        buttonScreenSaverSettings.setOnClickListener { view.let { Navigation.findNavController(it).navigate(R.id.screen_action) }}
        buttonWeatherSettings.setOnClickListener { view.let { Navigation.findNavController(it).navigate(R.id.weather_action) }}
        buttonPlatformSettings.setOnClickListener { view.let { Navigation.findNavController(it).navigate(R.id.platform_action) } }
        buttonAboutSettings.setOnClickListener {view.let { Navigation.findNavController(it).navigate(R.id.about_action) } }
        buttonSensorSettings.setOnClickListener { view.let { Navigation.findNavController(it).navigate(R.id.sensors_action) }}
    }

    companion object {
        fun newInstance(): SettingsFragment {
            return SettingsFragment()
        }
    }
}