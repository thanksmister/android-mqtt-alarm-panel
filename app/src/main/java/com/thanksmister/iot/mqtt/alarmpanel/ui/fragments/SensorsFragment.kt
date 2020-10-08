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
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.Navigation
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.SwitchPreference
import androidx.recyclerview.widget.LinearLayoutManager
import com.thanksmister.iot.mqtt.alarmpanel.BaseActivity
import com.thanksmister.iot.mqtt.alarmpanel.BaseFragment
import com.thanksmister.iot.mqtt.alarmpanel.R
import com.thanksmister.iot.mqtt.alarmpanel.network.MQTTOptions
import com.thanksmister.iot.mqtt.alarmpanel.persistence.Sensor
import com.thanksmister.iot.mqtt.alarmpanel.ui.activities.SettingsActivity
import com.thanksmister.iot.mqtt.alarmpanel.ui.adapters.SensorAdapter
import com.thanksmister.iot.mqtt.alarmpanel.ui.views.AlarmCodeView
import com.thanksmister.iot.mqtt.alarmpanel.ui.views.EditTextDialogView
import com.thanksmister.iot.mqtt.alarmpanel.ui.views.SensorDialogView
import com.thanksmister.iot.mqtt.alarmpanel.utils.DialogUtils
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils.Companion.COMMAND_DEVICE_SENSOR
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils.Companion.SENSOR_GENERIC_TYPE
import com.thanksmister.iot.mqtt.alarmpanel.viewmodel.SensorViewModel
import dagger.android.support.AndroidSupportInjection
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_sensors.*
import kotlinx.android.synthetic.main.fragment_sensors.view.*
import timber.log.Timber
import javax.inject.Inject

class SensorsFragment : BaseSettingsFragment() {

    @Inject lateinit var sensorViewModel: SensorViewModel
    @Inject lateinit var mqttOptions: MQTTOptions

    private val sensorsPreference: Preference by lazy {
        findPreference("button_sensors") as Preference
    }

    private val sensorOnePreference: SwitchPreference by lazy {
        findPreference("key_sensor_one") as SwitchPreference
    }

    private val sensorOneNamePreference: EditTextPreference by lazy {
        findPreference("key_sensor_two_name") as EditTextPreference
    }

    private val sensorTwoPreference: SwitchPreference by lazy {
        findPreference("key_sensor_two") as SwitchPreference
    }

    private val sensorTwoNamePreference: EditTextPreference by lazy {
        findPreference("key_sensor_two_name") as EditTextPreference
    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preference_general)
        lifecycle.addObserver(dialogUtils)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if((activity as SettingsActivity).supportActionBar != null) {
            (activity as SettingsActivity).supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            (activity as SettingsActivity).supportActionBar!!.setDisplayShowHomeEnabled(true)
            (activity as SettingsActivity).supportActionBar!!.title = (getString(R.string.activity_settings_title))
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when (key) {
            "key_sensor_one" -> {
               configuration.sensorOne = sensorOnePreference.isChecked
           }
           "key_sensor_two" -> {
               configuration.sensorTwo = sensorTwoPreference.isChecked
           }
           "key_sensor_one_name" -> {
               configuration.sensorOneName = sensorOneNamePreference.text
           }
           "key_sensor_two_name" -> {
               configuration.sensorTwoName = sensorTwoNamePreference.text
           }
        }
    }

    private fun toStars(textToStars: String?): String {
        var text = textToStars
        val sb = StringBuilder()
        for (i in text.orEmpty().indices) {
            sb.append('*')
        }
        text = sb.toString()
        return text
    }

    companion object {
        fun newInstance(): SensorsFragment {
            return SensorsFragment()
        }
    }
}