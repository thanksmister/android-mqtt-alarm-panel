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

package com.thanksmister.iot.mqtt.alarmpanel.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.PRIORITY_MAX
import androidx.core.app.NotificationCompat.VISIBILITY_PUBLIC
import androidx.core.app.TaskStackBuilder
import androidx.core.content.ContextCompat
import com.thanksmister.iot.mqtt.alarmpanel.R
import com.thanksmister.iot.mqtt.alarmpanel.ui.activities.MainActivity
import timber.log.Timber
import javax.inject.Inject

class NotificationUtils @Inject
constructor(private val context: Context): ContextWrapper(context) {

    private var notificationManager: NotificationManager? = null
    private val pendingIntent: PendingIntent
    private var notificationIntent: Intent

    /*@RequiresApi(api = Build.VERSION_CODES.O)
    private void createChannels() {
        String description = getString(R.string.text_channel_description);
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel mChannel = new NotificationChannel(ANDROID_CHANNEL_ID, getString(R.string.notification_channel_name), importance);
        mChannel.setDescription(description);
        mChannel.enableLights(true);
        mChannel.setLightColor(Color.RED);
        mChannel.enableVibration(true);
        mChannel.setLockscreenVisibility(VISIBILITY_PUBLIC);
        mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
        getManager().createNotificationChannel(mChannel);
    }*/

    init {
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationIntent = Intent(context, MainActivity::class.java)
        notificationIntent.action = Intent.ACTION_MAIN
        notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER)
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannels()
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun createChannels() {
        val description = getString(R.string.text_channel_description)
        val importance = NotificationManager.IMPORTANCE_LOW
        val mChannel = NotificationChannel(ANDROID_CHANNEL_ID, getString(R.string.notification_channel_name), importance)
        mChannel.description = description
        notificationManager?.createNotificationChannel(mChannel)
    }

    fun createOngoingNotification(title: String, message: String): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nb = getAndroidChannelOngoingNotification(title, message)
            return nb.build()
        } else {
            val nb = getAndroidOngoingNotification(title, message)
            // This ensures that navigating backward from the Activity leads out of your app to the Home screen.
            val stackBuilder = TaskStackBuilder.create(applicationContext)
            stackBuilder.addParentStack(MainActivity::class.java)
            stackBuilder.addNextIntent(notificationIntent)
            nb.setContentIntent(pendingIntent)
            return nb.build()
        }
    }

    fun createAlarmNotification(title: String, message: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nb = getAndroidChannelNotification(title, message)
            notificationManager?.notify(NOTIFICATION_ID, nb.build())
        } else {
            val nb = getAndroidNotification(title, message)
            // This ensures that navigating backward from the Activity leads out of your app to the Home screen.
            val stackBuilder = TaskStackBuilder.create(applicationContext)
            stackBuilder.addParentStack(MainActivity::class.java)
            stackBuilder.addNextIntent(notificationIntent)
            nb.setContentIntent(pendingIntent)
            notificationManager?.notify(NOTIFICATION_ID, nb.build())
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun getAndroidChannelOngoingNotification(title: String, body: String): Notification.Builder {
        val color = ContextCompat.getColor(applicationContext, R.color.colorPrimary)
        val builder = Notification.Builder(applicationContext, ANDROID_CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(body)
                .setColor(color)
                .setOngoing(true)
                .setLocalOnly(true)
                .setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))
                .setSmallIcon(R.drawable.ic_home_white)
                .setAutoCancel(false)

        builder.setContentIntent(pendingIntent)
        return builder
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun getAndroidChannelNotification(title: String, body: String): Notification.Builder {
        val color = ContextCompat.getColor(applicationContext, R.color.colorPrimary)
        val builder = Notification.Builder(applicationContext, ANDROID_CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(body)
                .setColor(color)
                .setOnlyAlertOnce(true)
                .setOngoing(false)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_stat_alarm_panel)
                .setAutoCancel(true)

        builder.setContentIntent(pendingIntent)
        return builder
    }

    private fun getAndroidOngoingNotification(title: String, body: String): NotificationCompat.Builder {
        val color = ContextCompat.getColor(applicationContext, R.color.colorPrimary)
        return NotificationCompat.Builder(applicationContext)
                .setSmallIcon(R.drawable.ic_home_white)
                .setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))
                .setContentTitle(title)
                .setContentText(body)
                .setOngoing(true)
                .setLocalOnly(true)
                .setColor(color)
                .setAutoCancel(false)
    }

    private fun getAndroidNotification(title: String, body: String): NotificationCompat.Builder {
        val color = ContextCompat.getColor(applicationContext, R.color.colorPrimary)
        return NotificationCompat.Builder(applicationContext)
                .setSmallIcon(R.drawable.ic_stat_alarm_panel)
                .setContentTitle(title)
                .setContentText(body)
                .setOnlyAlertOnce(true)
                .setOngoing(false)
                .setVibrate(longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400))
                .setPriority(PRIORITY_MAX)
                .setVisibility(VISIBILITY_PUBLIC)
                .setWhen(System.currentTimeMillis())
                .setColor(color)
                .setAutoCancel(true)
    }

    fun clearNotification() {
        Timber.d("clearNotification")
       //notificationManager?.cancel(NOTIFICATION_ID)
    }

    companion object {
        const val NOTIFICATION_ID = 1138
        const val ANDROID_CHANNEL_ID = "com.thanksmister.iot.mqtt.alarmpanel.ANDROID"
    }
}