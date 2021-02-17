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

package com.thanksmister.iot.mqtt.alarmpanel.ui.views

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.thanksmister.iot.mqtt.alarmpanel.persistence.Forecast
import com.thanksmister.iot.mqtt.alarmpanel.persistence.Weather
import com.thanksmister.iot.mqtt.alarmpanel.ui.adapters.ForecastCardAdapter
import com.thanksmister.iot.mqtt.alarmpanel.utils.DateUtils
import com.thanksmister.iot.mqtt.alarmpanel.utils.StringUtils
import com.thanksmister.iot.mqtt.alarmpanel.utils.WeatherUtils

import kotlinx.android.synthetic.main.dialog_extended_forecast.view.*
import timber.log.Timber
import java.util.*

class ExtendedForecastView : RecyclerView {

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    fun setExtendedForecast(weather: Weather) {
        val forecastList = weather.forecast
        if(forecastList.isNotEmpty()) {
            val groupedForecastList = ArrayList<ForecastDisplay>()
            var groupDay = DateUtils.dayOfWeek(forecastList[0].datetime)
            Timber.d(" group day start ${forecastList[0].datetime}")
            Timber.d(" forecast list ${forecastList.size}")
            var group = ArrayList<Forecast>()
            var count = 0
            for (forecast in forecastList) {
                val day = DateUtils.dayOfWeek(forecast.datetime)
                if (day == groupDay) {
                    group.add(forecast)
                } else {
                    if(!group.isEmpty()){
                        val today = (count == 0)
                        val forecastDisplay = groupForecastsByDay(today, group)
                        Timber.d(" add today ${today}")
                        Timber.d(" add forecastDisplay ${forecastDisplay.day}")
                        groupedForecastList.add(forecastDisplay)
                        count++
                    }
                    groupDay = day
                    group = ArrayList()
                    group.add(forecast)
                }
            }
            if(group.isNotEmpty()){
                val forecastDisplay = groupForecastsByDay(false, group)
                Timber.d(" add forecastDisplay ${forecastDisplay.day}")
                groupedForecastList.add(forecastDisplay)
            }

            recycleView.apply {
                setHasFixedSize(true)
                val linearLayoutManager = LinearLayoutManager(context)
                linearLayoutManager.orientation = LinearLayoutManager.HORIZONTAL
                layoutManager = linearLayoutManager
                forecastList.let {
                    val forecastCardAdapter = ForecastCardAdapter(groupedForecastList)
                    recycleView.adapter = forecastCardAdapter
                }
            }
        }
    }

    private fun groupForecastsByDay(today: Boolean = false, group: ArrayList<Forecast>): ForecastDisplay {
        val forecastDisplay = ForecastDisplay()
        if(group.isNotEmpty()) {
            forecastDisplay.condition = calculateCurrentConditionString(today, group, context)
            forecastDisplay.conditionImage = calculateCurrentConditionImage(today, group)
            forecastDisplay.day = DateUtils.dayOfWeek(group[0].datetime)
            forecastDisplay.precipitation = calculatePrecipitation(group)
            forecastDisplay.temperatureLow = calculateLowTemperature(group)
            forecastDisplay.temperatureHigh = calculateHighTemperature(group)
        }
        return forecastDisplay
    }

    private fun calculatePrecipitation(forecastList: ArrayList<Forecast>): Double {
        var precip = 0.0
        for(forecast in forecastList) {
            forecast.precipitation?.let {
                val doubleValue = StringUtils.stringToDouble(it)
                if(doubleValue > precip) {
                    precip = doubleValue
                }
            }
        }
        return precip
    }

    private fun calculateLowTemperature(forecastList: ArrayList<Forecast>): Double {
        var temp = 0.0
        for(forecast in forecastList) {
            forecast.temperature?.let {
                if(temp == 0.0) {
                    temp = it
                }
                if(it < temp) {
                    temp = it
                }
            }
        }
        return temp
    }

    private fun calculateHighTemperature(forecastList: ArrayList<Forecast>): Double {
        var temp = 0.0
        for(forecast in forecastList) {
            forecast.temperature?.let {
                if(it > temp) {
                    temp = it
                }
            }
        }
        return temp
    }

    private fun calculateCurrentConditionString(today: Boolean = false, forecastList: ArrayList<Forecast>, context: Context): String {
        if(forecastList.isNotEmpty()) {
            Timber.d("calculateCurrentConditionString -------------------")
            val condition = getCondition(today, forecastList)
            return WeatherUtils.getOutlookForWeatherCondition(condition, context)
        }
        return WeatherUtils.getOutlookForWeatherCondition(forecastList[0].condition, context)
    }

    private fun calculateCurrentConditionImage(today: Boolean = false, forecastList: ArrayList<Forecast>): Int {
        if(forecastList.isNotEmpty()) {
            Timber.d("calculateCurrentConditionImage -------------------")
            val condition = getCondition(today, forecastList)
            Timber.d("Current condition ${condition} -----------------------")
            return WeatherUtils.getIconForWeatherCondition(condition)
        }
        return WeatherUtils.getIconForWeatherCondition(forecastList[0].condition)
    }

    private fun getCondition(today: Boolean, forecastList: ArrayList<Forecast>): String? {
        val rightNow = Calendar.getInstance()
        val currentHourIn24Format = if (today) (rightNow.get(Calendar.HOUR_OF_DAY) + .5) else 14.5
        Timber.d("currentHourIn24Format $currentHourIn24Format")
        var condition = forecastList[forecastList.size - 1].condition
        for(forecast in forecastList) {
            val day = DateUtils.dayOfWeek(forecast.datetime)
            Timber.d("Current Day $day")
            val hour = DateUtils.hourOfDay(forecast.datetime).toDouble()
            Timber.d("Current Hour $hour")
            if(currentHourIn24Format <= hour) {
                Timber.d("Current Condition ${forecast.condition}")
                condition = forecast.condition
                break
            }
        }
        return condition
    }
}