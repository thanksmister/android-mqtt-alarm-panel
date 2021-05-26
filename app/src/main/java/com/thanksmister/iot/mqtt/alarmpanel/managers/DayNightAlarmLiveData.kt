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

package com.thanksmister.iot.mqtt.alarmpanel.managers

import androidx.lifecycle.MutableLiveData
import android.content.Context
import android.os.Handler
import android.os.Looper
import com.thanksmister.iot.mqtt.alarmpanel.persistence.Configuration
import com.thanksmister.iot.mqtt.alarmpanel.utils.DateUtils
import timber.log.Timber
import java.util.*


class DayNightAlarmLiveData(private val context: Context, private val configuration: Configuration) : MutableLiveData<String>() {

    private var timeHandler: Handler? = null

    private val timeRunnable = object : Runnable {
        override fun run() {
            setNightDayMode()
            if (timeHandler != null) {
                timeHandler!!.postDelayed(this, INTERVAL_FIFTEEN_MINUTES)
            }
        }
    }
    init {
    }

    override fun onActive() {
        super.onActive()
         startDayNightMode()
    }

    override fun onInactive() {
        super.onInactive()
        cancelDayNightMode()
    }

    private fun cancelDayNightMode() {
        Timber.d("cancelDayNightMode")
        if (timeHandler != null) {
            timeHandler!!.removeCallbacks(timeRunnable)
            timeHandler = null
        }
    }

    private fun startDayNightMode() {
        Timber.d("startDayNightMode")
        if(timeHandler == null && configuration.useNightDayMode) {
            val firstTime = (5 * 1000).toLong();
            timeHandler = Handler(Looper.getMainLooper())
            timeHandler!!.postDelayed(timeRunnable, firstTime)
        }
    }

    private fun setNightDayMode() {
        val nowTime = "${Calendar.getInstance().get(Calendar.HOUR_OF_DAY)}.${Calendar.getInstance().get(Calendar.MINUTE)}".toFloat()
        val startTime = DateUtils.getHourAndMinutesFromTimePicker(configuration.dayNightModeStartTime)
        val endTime = DateUtils.getHourAndMinutesFromTimePicker(configuration.dayNightModeEndTime)

        if(startTime == endTime) {
            Timber.d("Tis forever night")
            configuration.dayNightMode = Configuration.SUN_BELOW_HORIZON
            value = Configuration.DISPLAY_MODE_NIGHT
        } else if(endTime < startTime) {
            if(nowTime >= startTime || nowTime <= endTime) {
                Timber.d("Tis the night")
                configuration.dayNightMode = Configuration.SUN_BELOW_HORIZON
                value = Configuration.DISPLAY_MODE_NIGHT
            } else {
                Timber.d("Tis the day")
                configuration.dayNightMode = Configuration.SUN_ABOVE_HORIZON
                value = Configuration.DISPLAY_MODE_DAY
            }
        } else if (endTime > startTime) {
            if(nowTime >= startTime && nowTime <= endTime) {
                Timber.d("Tis the night")
                configuration.dayNightMode = Configuration.SUN_BELOW_HORIZON
                value = Configuration.DISPLAY_MODE_NIGHT
            } else {
                Timber.d("Tis the day")
                configuration.dayNightMode = Configuration.SUN_ABOVE_HORIZON
                value = Configuration.DISPLAY_MODE_DAY
            }
        }
    }

    companion object {
        const val INTERVAL_FIFTEEN_MINUTES = (15 * 60 * 1000).toLong()
    }
}
