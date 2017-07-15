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

package com.thanksmister.androidthings.iot.alarmpanel.ui.modules;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import timber.log.Timber;

public class BaseModule {
    
    private Context context;
    private PendingIntent pendingIntent;
    private AlarmReceiver alarmReceiver;
    private boolean hasStarted = false;
    private String wakeLock = "ModuleWakeLock";
    private String action = "ModuleAction";
    private AlarmListener alarmListener;

    public interface AlarmListener {
        void onReceived();
    }
    
    public BaseModule(@NonNull Context context) {
        this.context = context;
        this.alarmReceiver = new AlarmReceiver();
    }

    public void startWeatherUpdates(long delayedTime, String action, String wakeLock, AlarmListener alarmListener) {
        
        Timber.d("startWeatherUpdates");
        
        if(!TextUtils.isEmpty(wakeLock)) {
            this.wakeLock = wakeLock;
        }
        
        if(!TextUtils.isEmpty(action)) {
            this.action = action;
        }
       
        this.alarmListener = alarmListener;
        if(alarmListener == null) {
            throw new IllegalArgumentException("alarmListener can not be null");
        }
        
        pendingIntent = PendingIntent.getBroadcast(context, 0, new Intent(action), PendingIntent.FLAG_UPDATE_CURRENT);
        context.registerReceiver(alarmReceiver, new IntentFilter(action));
        scheduleAlarm(delayedTime);
        hasStarted = true;
    }

    public void stopWeatherUpdates(Context context) {
        Timber.d("stopWeatherUpdates");
        if(hasStarted){
            if(pendingIntent != null){
                // Cancel Alarm.
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Service.ALARM_SERVICE);
                alarmManager.cancel(pendingIntent);
            }
            hasStarted = false;
            try{
                context.unregisterReceiver(alarmReceiver);
            } catch(IllegalArgumentException e){
                Timber.e("Unregister error: " + e.getMessage());	
            }
        }
    }

    private void scheduleAlarm(long delayInMilliseconds) {
        long nextAlarmInMilliseconds = System.currentTimeMillis() + delayInMilliseconds;
        Timber.d("Schedule next alarm at " + nextAlarmInMilliseconds);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Service.ALARM_SERVICE);
        Timber.d("Alarm scheduled delayed at: " + delayInMilliseconds);
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextAlarmInMilliseconds, pendingIntent);
    }
    
    class AlarmReceiver extends BroadcastReceiver {
        
        private PowerManager.WakeLock wakelock;

        @Override
        public void onReceive(Context context, Intent intent) {
            Timber.d("Received at:" + System.currentTimeMillis());
            Timber.d("Received action:" + intent.getAction());
            Timber.d("Sent action:" + action);
            PowerManager pm = (PowerManager) context.getSystemService(Service.POWER_SERVICE);
            wakelock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, wakeLock + action);
            wakelock.acquire();
            if(alarmListener != null) {
                alarmListener.onReceived();
            }
            if (wakelock.isHeld()) {
                wakelock.release();
            }
        }
    }
}