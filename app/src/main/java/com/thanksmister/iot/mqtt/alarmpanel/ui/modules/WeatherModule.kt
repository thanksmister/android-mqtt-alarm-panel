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

package com.thanksmister.iot.mqtt.alarmpanel.ui.modules

import android.content.Context
import android.content.ContextWrapper
import android.os.AsyncTask
import android.os.Handler

import com.thanksmister.iot.mqtt.alarmpanel.network.DarkSkyApi
import com.thanksmister.iot.mqtt.alarmpanel.network.fetchers.DarkSkyFetcher
import com.thanksmister.iot.mqtt.alarmpanel.network.model.Daily
import com.thanksmister.iot.mqtt.alarmpanel.network.model.DarkSkyResponse
import com.thanksmister.iot.mqtt.alarmpanel.tasks.DarkSkyTask
import com.thanksmister.iot.mqtt.alarmpanel.tasks.NetworkTask
import com.thanksmister.iot.mqtt.alarmpanel.ui.Configuration

import retrofit2.Response
import timber.log.Timber

/**
 * We lazily load the hourly forecast and the extended forecast.  We could have done this on a syncadapter or alarm, but
 * we only care that this module runs while we are in the application.
 */
class WeatherModule() : AutoCloseable {

    override fun close() {
        cancelDarkSkyHourlyForecast()
    }

    private val TIME_IN_MILLISECONDS: Long = 1800000 // 30 minutes

    private var task: DarkSkyTask? = null
    private var listener: ForecastListener? = null
    private var apiKey: String? = null
    private var latitude: String? = null
    private var longitude: String? = null
    private var tempUnits: String? = null
    private var handler: Handler? = null

    private val delayRunnable = object : Runnable {
        override fun run() {
            handler!!.removeCallbacks(this)
            startDarkSkyHourlyForecast()
        }
    }

    interface ForecastListener {
        fun onWeatherToday(icon: String, apparentTemperature: Double, summary: String)
        fun onExtendedDaily(daily: Daily)
        fun onShouldTakeUmbrella(takeUmbrella: Boolean)
    }

    /**
     * @param key The api key for the DarkSky weather api
     * @param units SI or US
     * @param lat Location latitude
     * @param lon Location longitude
     * @param callback A nice little listener to wrap up the response
     */
    fun getDarkSkyHourlyForecast(key: String, units: String, lat: String,
                                 lon: String, callback: ForecastListener) {

        apiKey = key
        listener = callback
        tempUnits = units
        latitude = lat
        longitude = lon

        startDarkSkyHourlyForecast()
    }

    private fun startDarkSkyHourlyForecast() {

        if (task != null && task!!.status == AsyncTask.Status.RUNNING) {
            return  // we have a running task already
        }

        val api = DarkSkyApi()
        val fetcher = DarkSkyFetcher(api)

        task = DarkSkyTask(fetcher)
        task!!.setOnExceptionListener(object : NetworkTask.OnExceptionListener {
            override fun onException(paramException: Exception) {
                Timber.e("Weather Exception: " + paramException.message)
                setHandler()
            }
        })
        task!!.setOnCompleteListener(object : NetworkTask.OnCompleteListener<Response<DarkSkyResponse>> {
            override fun onComplete(paramResult: Response<DarkSkyResponse>) {
                Timber.d("Response: " + paramResult)
                Timber.d("Response: " + paramResult.code())
                val darkSkyResponse = paramResult.body()
                if (darkSkyResponse != null) {

                    // current weather
                    if (darkSkyResponse.currently != null) {
                        listener!!.onWeatherToday(darkSkyResponse.currently.icon, darkSkyResponse.currently.apparentTemperature!!, darkSkyResponse.currently.summary)
                    }

                    // should we take an umbrella today?
                    if (darkSkyResponse.currently != null && darkSkyResponse.currently.precipProbability != null) {
                        listener!!.onShouldTakeUmbrella(shouldTakeUmbrellaToday(darkSkyResponse.currently.precipProbability!!))
                    } else {
                        listener!!.onShouldTakeUmbrella(false)
                    }

                    // extended forecast
                    if (darkSkyResponse.daily != null) {
                        listener!!.onExtendedDaily(darkSkyResponse.daily)
                    }
                    setHandler()
                }
            }
        })
        task!!.execute(apiKey, tempUnits, latitude, longitude)
    }

    private fun setHandler() {
        if (handler == null) {
            handler = Handler()
        }
        handler!!.postDelayed(delayRunnable, TIME_IN_MILLISECONDS)
    }

    fun cancelDarkSkyHourlyForecast() {
        if (handler != null) {
            handler!!.removeCallbacks(delayRunnable)
        }
        if (task != null) {
            task!!.cancel(true)
            task = null
        }
    }

    /**
     * Determines if today is a good day to take your umbrella
     * Adapted from https://github.com/HannahMitt/HomeMirror/.
     * @return
     */
    private fun shouldTakeUmbrellaToday(precipProbability: Double): Boolean {
        return precipProbability > 0.3
    }
}