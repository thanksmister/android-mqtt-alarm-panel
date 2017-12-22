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
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.v4.content.res.ResourcesCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.thanksmister.iot.mqtt.alarmpanel.BaseFragment
import com.thanksmister.iot.mqtt.alarmpanel.R
import com.thanksmister.iot.mqtt.alarmpanel.network.DarkSkyRequest
import com.thanksmister.iot.mqtt.alarmpanel.network.model.Daily
import com.thanksmister.iot.mqtt.alarmpanel.ui.Configuration
import com.thanksmister.iot.mqtt.alarmpanel.ui.modules.WeatherModule
import com.thanksmister.iot.mqtt.alarmpanel.utils.WeatherUtils

import java.text.DateFormat

import javax.inject.Inject

import android.os.Looper.getMainLooper
import android.support.v4.app.ActivityCompat
import com.thanksmister.iot.mqtt.alarmpanel.BaseActivity
import com.thanksmister.iot.mqtt.alarmpanel.utils.DialogUtils
import kotlinx.android.synthetic.main.fragment_information.*
import java.lang.Math.round
import java.util.*

class InformationFragment : BaseFragment() {

    @Inject lateinit var configuration: Configuration
    @Inject lateinit var dialogUtils: DialogUtils

    private var weatherModule: WeatherModule? = null
    private var extendedDaily: Daily? = null
    private var weatherHandler: Handler? = null
    private var timeHandler: Handler? = null

    private val timeRunnable = object : Runnable {
        override fun run() {
            val currentDateString = DateFormat.getDateInstance(DateFormat.LONG, Locale.getDefault()).format(Date())
            val currentTimeString = DateFormat.getTimeInstance(DateFormat.DEFAULT, Locale.getDefault()).format(Date())
            dateText.text = currentDateString
            timeText.text = currentTimeString
            if (timeHandler != null) {
                timeHandler!!.postDelayed(this, 1000)
            }
        }
    }

    private val weatherRunnable = object : Runnable {
        override fun run() {
            weatherHandler!!.postDelayed(this, DATE_INTERVAL)
        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        weatherHandler = Handler(getMainLooper())
        timeHandler = Handler(getMainLooper())
        timeHandler!!.postDelayed(timeRunnable, 1000)
        weatherLayout.setOnClickListener({if (extendedDaily != null) {
            dialogUtils.showExtendedForecastDialog(activity as BaseActivity, extendedDaily!!)
        }})
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_information, container, false)
    }

    override fun onResume() {
        super.onResume()
        if (configuration.showWeatherModule() && readWeatherOptions().isValid) {
            if (weatherHandler != null) {
                weatherHandler!!.removeCallbacks(weatherRunnable)
            }
            connectWeatherModule()
        } else {
            disconnectWeatherModule()
            weatherLayout.visibility = View.GONE
        }
    }

    override fun onDetach() {
        super.onDetach()
        disconnectWeatherModule()
        if (timeHandler != null) {
            timeHandler!!.removeCallbacks(timeRunnable)
        }
    }

    private fun disconnectWeatherModule() {
        if (weatherHandler != null) {
            weatherHandler!!.removeCallbacks(weatherRunnable)
        }
        if (weatherModule != null) {
            weatherModule!!.cancelDarkSkyHourlyForecast()
        }
    }

    private fun connectWeatherModule() {
        if (weatherModule == null) {
            weatherModule = WeatherModule()
        }

        val apiKey = readWeatherOptions().darkSkyKey
        val units = readWeatherOptions().weatherUnits
        val lat = readWeatherOptions().latitude
        val lon = readWeatherOptions().longitude
        weatherModule!!.getDarkSkyHourlyForecast(apiKey!!, units!!, lat!!, lon!!, object : WeatherModule.ForecastListener {
            override fun onWeatherToday(icon: String, apparentTemperature: Double, summary: String) {
                weatherLayout.setVisibility(View.VISIBLE)
                outlookText.setText(summary)
                val displayUnits = if (units == DarkSkyRequest.UNITS_US) getString(R.string.text_f) else getString(R.string.text_c)
                temperatureText.setText(getString(R.string.text_temperature, round(apparentTemperature).toString(), displayUnits))
                conditionImage.setImageDrawable(ResourcesCompat.getDrawable(resources, WeatherUtils.getIconForWeatherCondition(icon), activity!!.theme))

                // start the clock
                if (weatherHandler != null) {
                    weatherHandler!!.postDelayed(weatherRunnable, DATE_INTERVAL)
                }
            }

            override fun onExtendedDaily(daily: Daily) {
                extendedDaily = daily
            }

            override fun onShouldTakeUmbrella(takeUmbrella: Boolean) {
                if (takeUmbrella) {
                    conditionImage.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_rain_umbrella, activity!!.theme))
                }
            }
        })
    }

    companion object {
        val DATE_INTERVAL: Long = 3600000 // 1 hour

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         */
        fun newInstance(): InformationFragment {
            return InformationFragment()
        }
    }
}// Required empty public constructor