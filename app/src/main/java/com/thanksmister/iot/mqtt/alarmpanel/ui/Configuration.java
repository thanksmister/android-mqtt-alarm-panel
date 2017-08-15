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

import android.content.Context;

import com.thanksmister.iot.mqtt.alarmpanel.network.DarkSkyRequest;
import com.thanksmister.iot.mqtt.alarmpanel.utils.AlarmUtils;
import com.thanksmister.iot.mqtt.alarmpanel.utils.DeviceUtils;

import dpreference.DPreference;

/**
 * Store configurations 
 */
public class Configuration {

    private final DPreference sharedPreferences;
    private final Context context;
    
    public static final String PREF_FIRST_TIME = "pref_first_time";
    public static final String PREF_ALARM_MODE = "pref_alarm_mode";
    public static final String PREF_USERNAME = "pref_username";
    public static final String PREF_COMMAND_TOPIC = "pref_command_topic";
    public static final String PREF_STATE_TOPIC = "pref_state_topic";
    public static final String PREF_TLS_CONNECTION = "pref_tls_connection";
    public static final String PREF_ALARM_CODE = "pref_alarm_code";
    public static final String PREF_ARMED = "pref_armed";
    public static final String PREF_PASSWORD = "pref_password";
    public static final String PREF_PORT = "pref_port";
    public static final String PREF_CLIENT_ID = "pref_client_id";
    public static final String PREF_BROKER = "pref_broker";
    public static final String PREF_PENDING_TIME = "pref_pending_time";
    public static final String PREF_TRIGGER_TIME = "pref_trigger_time";

    public static final String PREF_MODULE_WEATHER = "pref_module_weather";
    public static final String PREF_WEATHER_UNITS = "pref_weather_units";
    public static final String PREF_DARK_SKY_KEY = "pref_dark_sky_key";
    public static final String PREF_LAT = "pref_weather_lat";
    public static final String PREF_LON = "pref_weather_lon";

    public static final String PREF_ARM_HOME = "arm_home";
    public static final String PREF_ARM_HOME_PENDING = "arm_home_pending";
    public static final String PREF_ARM_PENDING = "arm_pending";
    public static final String PREF_ARM_AWAY = "ARM_AWAY";
    public static final String PREF_ARM_AWAY_PENDING = "prefs_arm_away_pending";
    public static final String PREF_DISARM = "DISARM";
    
    
    public Configuration(Context context, DPreference sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
        this.context = context;
    }

    public void setFirstTime(boolean value){
        sharedPreferences.setPrefBoolean(PREF_FIRST_TIME, value);
    }

    public boolean isFirstTime(){
        return sharedPreferences.getPrefBoolean(PREF_FIRST_TIME, true);
    }

    public void setShowWeatherModule(boolean show){
        sharedPreferences.setPrefBoolean(PREF_MODULE_WEATHER, show);
    }

    public boolean showWeatherModule(){
        return sharedPreferences.getPrefBoolean(PREF_MODULE_WEATHER, false);
    }

    public void setLon(String longitude) {
        this.sharedPreferences.setPrefString(PREF_LON, longitude);
    }

    public void setLat(String latitude) {
        this.sharedPreferences.setPrefString(PREF_LAT, latitude);
    }

    public String getLatitude() {
        return this.sharedPreferences.getPrefString(PREF_LAT, null);
    }

    public String getLongitude() {
        return this.sharedPreferences.getPrefString(PREF_LON, null);
    }
    
    public boolean getIsCelsius() {
        String units = getWeatherUnits();
        return DarkSkyRequest.UNITS_SI.equals(units);
    }

    public String getWeatherUnits() {
        return sharedPreferences.getPrefString(PREF_WEATHER_UNITS, DarkSkyRequest.UNITS_US);
    }

    public void setIsCelsius(boolean isCelsius) {
        sharedPreferences.setPrefString(PREF_WEATHER_UNITS, isCelsius ? DarkSkyRequest.UNITS_SI : DarkSkyRequest.UNITS_US);
    }

    public String getDarkSkyKey() {
        return this.sharedPreferences.getPrefString(PREF_DARK_SKY_KEY, null);
    }

    public void setDarkSkyKey(String value) {
        this.sharedPreferences.setPrefString(PREF_DARK_SKY_KEY, value);
    }
    
    public String getUserName() {
        return this.sharedPreferences.getPrefString(PREF_USERNAME, null);
    }

    public void setUserName(String value) {
        this.sharedPreferences.setPrefString(PREF_USERNAME, value);
    }

    public String getPassword() {
        return this.sharedPreferences.getPrefString(PREF_PASSWORD, null);
    }

    public void setPassword(String value) {
        this.sharedPreferences.setPrefString(PREF_PASSWORD, value);
    }

    public String getClientId() {
        return this.sharedPreferences.getPrefString(PREF_CLIENT_ID, DeviceUtils.getDeviceIdHash(context));
    }

    public void setClientId(String value) {
        this.sharedPreferences.setPrefString(PREF_CLIENT_ID, value);
    }

    public int getPort() {
        return this.sharedPreferences.getPrefInt(PREF_PORT, AlarmUtils.PORT);
    }

    public void setPort(int value) {
        this.sharedPreferences.setPrefInt(PREF_PORT, value);
    }

    public String getBroker() {
        return this.sharedPreferences.getPrefString(PREF_BROKER, "");
    }

    public void setBroker(String value) {
        this.sharedPreferences.setPrefString(PREF_BROKER, value);
    }

    public int getPendingTime() {
        return this.sharedPreferences.getPrefInt(PREF_PENDING_TIME, AlarmUtils.PENDING_TIME);
    }

    public void setTriggerTime(int value) {
        this.sharedPreferences.setPrefInt(PREF_TRIGGER_TIME, value);
    }

    public int getTriggerTime() {
        return this.sharedPreferences.getPrefInt(PREF_TRIGGER_TIME, AlarmUtils.TRIGGER_TIME);
    }

    public void setPendingTime(int value) {
        this.sharedPreferences.setPrefInt(PREF_PENDING_TIME, value);
    }

    public String getCommandTopic() {
        return this.sharedPreferences.getPrefString(PREF_COMMAND_TOPIC, AlarmUtils.COMMAND_TOPIC);
    }

    public void setCommandTopic(String value) {
        this.sharedPreferences.setPrefString(PREF_COMMAND_TOPIC, value);
    }

    public String getStateTopic() {
        return this.sharedPreferences.getPrefString(PREF_STATE_TOPIC, AlarmUtils.STATE_TOPIC);
    }

    public void setStateTopic(String value) {
        this.sharedPreferences.setPrefString(PREF_STATE_TOPIC, value);
    }

    public boolean getTlsConnection() {
        return this.sharedPreferences.getPrefBoolean(PREF_TLS_CONNECTION, false);
    }

    public void setTlsConnection(boolean value) {
        this.sharedPreferences.setPrefBoolean(PREF_TLS_CONNECTION, value);
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
        return sharedPreferences.getPrefString(PREF_ALARM_MODE, PREF_DISARM);
    }

    // TODO guarantee mode is of one of three types
    public void setAlarmMode(String mode) {
        sharedPreferences.setPrefString(PREF_ALARM_MODE, mode);
    }
    
    /**
     * Reset the <code>SharedPreferences</code> and database
     */
    public void reset() {
        sharedPreferences.removePreference(PREF_COMMAND_TOPIC);
        sharedPreferences.removePreference(PREF_COMMAND_TOPIC);
        sharedPreferences.removePreference(PREF_TLS_CONNECTION);
        sharedPreferences.removePreference(PREF_USERNAME);
        sharedPreferences.removePreference(PREF_PORT);
        sharedPreferences.removePreference(PREF_PASSWORD);
        sharedPreferences.removePreference(PREF_COMMAND_TOPIC);
        sharedPreferences.removePreference(PREF_BROKER);
        sharedPreferences.removePreference(PREF_ALARM_MODE);
        sharedPreferences.removePreference(PREF_ARMED);
        sharedPreferences.removePreference(PREF_ALARM_CODE);
    }
}