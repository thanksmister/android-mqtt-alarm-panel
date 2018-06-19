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

import android.app.AlarmManager
import android.app.PendingIntent
import android.arch.lifecycle.MutableLiveData
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import android.os.Looper
import com.thanksmister.iot.mqtt.alarmpanel.ui.Configuration
import com.thanksmister.iot.mqtt.alarmpanel.utils.DateUtils
import timber.log.Timber
import java.util.*


class DayNightAlarmLiveData(private val context: Context, private val configuration: Configuration) : MutableLiveData<String>() {

    //private var pendingIntent: PendingIntent? = null
    //private val intentFilter = IntentFilter(ALARM_ACTION)
    //private val alarmManager: AlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
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
        //context.registerReceiver(alarmReceiver, IntentFilter(intentFilter))
        startDayNightMode()
    }

    override fun onInactive() {
        super.onInactive()
        //context.unregisterReceiver(alarmReceiver)
        cancelDayNightMode()
    }

    private fun cancelDayNightMode() {
        Timber.d("cancelDayNightMode")
        if (timeHandler != null) {
            timeHandler!!.removeCallbacks(timeRunnable)
            timeHandler = null
        }
        /*if(pendingIntent != null) {
            Timber.d("cancelDayNightMode")
            pendingIntent = PendingIntent.getBroadcast(context, REQUEST_CODE, Intent(ALARM_ACTION), PendingIntent.FLAG_CANCEL_CURRENT);
            alarmManager.cancel(pendingIntent)
            pendingIntent?.cancel()
            pendingIntent = null
        }*/
    }

    private fun startDayNightMode() {
        Timber.d("startDayNightMode")
        if(timeHandler == null && configuration.useNightDayMode) {
            val firstTime = (5 * 1000).toLong();
            timeHandler = Handler(Looper.getMainLooper())
            timeHandler!!.postDelayed(timeRunnable, firstTime)
        }
        /*if(pendingIntent == null && configuration.useNightDayMode) {
            configuration.dayNightMode = Configuration.DISPLAY_MODE_DAY
            pendingIntent = PendingIntent.getBroadcast(context, REQUEST_CODE, Intent(ALARM_ACTION), PendingIntent.FLAG_UPDATE_CURRENT);
            val firstTime = SystemClock.elapsedRealtime() + 30 * 1000;
            alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, firstTime, INTERVAL_FIFTEEN_MINUTES, pendingIntent)
        }*/
    }

    private fun getPendingIntent(): PendingIntent {
        val pendingIntent = PendingIntent.getBroadcast(context, REQUEST_CODE, Intent(ALARM_ACTION), PendingIntent.FLAG_UPDATE_CURRENT);
        return pendingIntent
    }

    private val alarmReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Timber.d("alarmReceiver")
            setNightDayMode()
        }
    }

    private fun setNightDayMode() {
        val nowTime = "${Calendar.getInstance().get(Calendar.HOUR_OF_DAY)}.${Calendar.getInstance().get(Calendar.MINUTE)}".toFloat()
        val startTime = DateUtils.getHourAndMinutesFromTimePicker(configuration.dayNightModeStartTime)
        val endTime = DateUtils.getHourAndMinutesFromTimePicker(configuration.dayNightModeEndTime)

        if(startTime == endTime) {
            Timber.d("Tis forever night")
            configuration.dayNightMode = Configuration.DISPLAY_MODE_NIGHT
            value = Configuration.DISPLAY_MODE_NIGHT
        } else if(endTime < startTime) {
            if(nowTime >= startTime || nowTime <= endTime) {
                Timber.d("Tis the night")
                configuration.dayNightMode = Configuration.DISPLAY_MODE_NIGHT
                value = Configuration.DISPLAY_MODE_NIGHT
            } else {
                Timber.d("Tis the day")
                configuration.dayNightMode = Configuration.DISPLAY_MODE_DAY
                value = Configuration.DISPLAY_MODE_DAY
            }
        } else if (endTime > startTime) {
            if(nowTime >= startTime && nowTime <= endTime) {
                Timber.d("Tis the night")
                configuration.dayNightMode = Configuration.DISPLAY_MODE_NIGHT
                value = Configuration.DISPLAY_MODE_NIGHT
            } else {
                Timber.d("Tis the day")
                configuration.dayNightMode = Configuration.DISPLAY_MODE_DAY
                value = Configuration.DISPLAY_MODE_DAY
            }
        }
    }

    companion object {
        const val INTERVAL_FIFTEEN_MINUTES = (15 * 60 * 1000).toLong()
        const val ALARM_ACTION = "com.thanksmister.iot.mqtt.alarmpanel.DayNightAlarmReceiver"
        const val REQUEST_CODE = 888
    }
}
