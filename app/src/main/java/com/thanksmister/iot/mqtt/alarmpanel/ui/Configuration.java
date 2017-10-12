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

package com.thanksmister.iot.mqtt.alarmpanel.ui;

import com.thanksmister.iot.mqtt.alarmpanel.utils.AlarmUtils;

import dpreference.DPreference;


/**
 * Store configurations 
 */
public class Configuration {

    private final DPreference sharedPreferences;
    
    public static final String PREF_ALARM_CODE = "pref_alarm_code";
    public static final String PREF_PENDING_TIME = "pref_pending_time";
    public static final String PREF_NOTIFICATIONS = "pref_notifications";
    public static final String PREF_ARM_HOME = "arm_home";
    public static final String PREF_ARM_HOME_PENDING = "arm_home_pending";
    public static final String PREF_ARM_PENDING = "arm_pending";
    public static final String PREF_ARM_AWAY = "arm_away";
    public static final String PREF_ARM_AWAY_PENDING = "prefs_arm_away_pending";
    public static final String PREF_DISARM = "disarm";
    public static final String PREF_TRIGGERED = "triggered";
    public static final String PREF_TRIGGERED_PENDING = "triggered_pending";
    
    public static final String PREF_AWAY_TRIGGERED_PENDING = "triggered_away_pending";
    public static final String PREF_HOME_TRIGGERED_PENDING = "triggered_home_pending";
    
    public static final String PREF_MODULE_SAVER = "pref_module_saver";
    public static final String PREF_MODULE_PHOTO_SAVER = "pref_module_saver_photo";
    public static final String PREF_INACTIVITY_TIME = "pref_inactivity_time";
    public static final String PREF_MODULE_WEATHER = "pref_module_weather";
    
    private static final String PREF_MODULE_HASS = "pref_module_hass";
    private static final String PREF_HASS_WEB_URL = "pref_hass_web_url";
    private static final String PREF_ARMED = "pref_armed";
    private static final String PREF_FIRST_TIME = "pref_first_time";
    private static final String PREF_ALARM_MODE = "pref_alarm_mode";
    
    public static final int PREF_DISABLE_DIALOG_TIME = 30; // this isn't configurable
    private static final long INACTIVITY_TIMEOUT =  5 * 60 * 1000; // 5 min

    public Configuration( DPreference sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    public void setShowWeatherModule(boolean show){
        sharedPreferences.setPrefBoolean(PREF_MODULE_WEATHER, show);
    }

    public boolean showWeatherModule(){
        return sharedPreferences.getPrefBoolean(PREF_MODULE_WEATHER, false);
    }
    
    public boolean showHassModule(){
        return sharedPreferences.getPrefBoolean(PREF_MODULE_HASS, false);
    }

    public void setShowHassModule(boolean value) {
        this.sharedPreferences.setPrefBoolean(PREF_MODULE_HASS, value);
    }

    public String getHassUrl() {
        return this.sharedPreferences.getPrefString(PREF_HASS_WEB_URL, null); 
    }

    public void setHassUrl(String value) {
        this.sharedPreferences.setPrefString(PREF_HASS_WEB_URL, value);
    }

    public boolean showScreenSaverModule(){
        return sharedPreferences.getPrefBoolean(PREF_MODULE_SAVER, true);
    }

    public void setScreenSaverModule(boolean value) {
        this.sharedPreferences.setPrefBoolean(PREF_MODULE_SAVER, value);
    }

    public boolean showPhotoScreenSaver(){
        return sharedPreferences.getPrefBoolean(PREF_MODULE_PHOTO_SAVER, false);
    }

    public void setPhotoScreenSaver(boolean value) {
        this.sharedPreferences.setPrefBoolean(PREF_MODULE_PHOTO_SAVER, value);
    }

    public void setInactivityTime(long value) {
        this.sharedPreferences.setPrefLong(PREF_INACTIVITY_TIME, value);
    }

    public long getInactivityTime() {
        return this.sharedPreferences.getPrefLong(PREF_INACTIVITY_TIME, INACTIVITY_TIMEOUT);
    }

    
    public void setFirstTime(boolean value){
        sharedPreferences.setPrefBoolean(PREF_FIRST_TIME, value);
    }

    public boolean isFirstTime(){
        return sharedPreferences.getPrefBoolean(PREF_FIRST_TIME, true);
    }
    
    public int getPendingTime() {
        return this.sharedPreferences.getPrefInt(PREF_PENDING_TIME, AlarmUtils.PENDING_TIME);
    }
    
    public void setPendingTime(int value) {
        this.sharedPreferences.setPrefInt(PREF_PENDING_TIME, value);
    }
    
    public void setNotifications(boolean value) {
        this.sharedPreferences.setPrefBoolean(PREF_NOTIFICATIONS, value);
    }

    public boolean showNotifications() {
        return this.sharedPreferences.getPrefBoolean(PREF_NOTIFICATIONS, false);
    }
    
    public int getAlarmCode() {
        return this.sharedPreferences.getPrefInt(PREF_ALARM_CODE, 0);
    }

    public void setAlarmCode(int value) {
        this.sharedPreferences.setPrefInt(PREF_ALARM_CODE, value);
    }

    public boolean isArmed() {
        return this.sharedPreferences.getPrefBoolean(PREF_ARMED, false);
    }

    public void setArmed(boolean value) {
        this.sharedPreferences.setPrefBoolean(PREF_ARMED, value);
    }

    public String getAlarmMode() {
        return sharedPreferences.getPrefString(PREF_ALARM_MODE, PREF_DISARM).toLowerCase();
    }

    // TODO guarantee mode is of one of three types
    public void setAlarmMode(String mode) {
        sharedPreferences.setPrefString(PREF_ALARM_MODE, mode);
    }
    
    /**
     * Reset the <code>SharedPreferences</code> and database
     */
    public void reset() {
        sharedPreferences.removePreference(PREF_ALARM_MODE);
        sharedPreferences.removePreference(PREF_ARMED);
        sharedPreferences.removePreference(PREF_ALARM_CODE);
        sharedPreferences.removePreference(PREF_NOTIFICATIONS);
        sharedPreferences.removePreference(PREF_FIRST_TIME);
    }
}