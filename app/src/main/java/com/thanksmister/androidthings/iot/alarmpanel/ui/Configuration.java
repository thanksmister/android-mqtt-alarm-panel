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

package com.thanksmister.androidthings.iot.alarmpanel.ui;

import android.content.Context;

import com.thanksmister.androidthings.iot.alarmpanel.network.DarkSkyRequest;
import com.thanksmister.androidthings.iot.alarmpanel.utils.DeviceUtils;

import dpreference.DPreference;

/**
 * Store configurations 
 */
public class Configuration {

    private final DPreference sharedPreferences;
    private final Context context;
    
    private static final String PREF_ALARM_MODE = "pref_alarm_mode";
    private static final String PREF_USERNAME = "pref_user_name";
    private static final String PREF_TOPIC = "pref_topic";
    private static final String PREF_ALARM_CODE = "pref_alarm_code";
    private static final String PREF_ARMED = "pref_armed";
    private static final String PREF_PASSWORD = "pref_password";
    private static final String PREF_PORT = "pref_port";
    private static final String PREF_CLIENT_ID = "pref_client_id";
    private static final String PREF_BROKER = "pref_broker";
    
    private static final String PREF_MODULE_WEATHER = "pref_module_weather";
    private static final String PREF_WEATHER_UNITS = "pref_weather_units";
    private static final String PREF_DARK_SKY_KEY = "pref_dark_sky_key";
    private static final String PREF_LAT = "pref_weather_lat";
    private static final String PREF_LON = "pref_weather_lon";

    public static final String ARMED_STAY = "armed_stay";
    public static final String ARMED_AWAY = "armed_away";
    public static final String DISARMED = "disarmed";
    
    public Configuration(Context context, DPreference sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
        this.context = context;
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
        this.sharedPreferences.getPrefString(PREF_ALARM_CODE, DarkSkyRequest.UNITS_US);
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
        return this.sharedPreferences.getPrefInt(PREF_PORT, 1883);
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

    public String getTopic() {
        return this.sharedPreferences.getPrefString(PREF_TOPIC, null);
    }

    public void setTopic(String value) {
        this.sharedPreferences.setPrefString(PREF_TOPIC, value);
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
        return sharedPreferences.getPrefString(PREF_ALARM_MODE, DISARMED);
    }

    // TODO guarantee mode is of one of three types
    public void setAlarmMode(String mode) {
        sharedPreferences.setPrefString(PREF_ALARM_MODE, mode);
    }
    
    /**
     * Reset the <code>SharedPreferences</code> and database
     */
    public void reset() {
        sharedPreferences.removePreference(PREF_USERNAME);
        sharedPreferences.removePreference(PREF_PORT);
        sharedPreferences.removePreference(PREF_PASSWORD);
        sharedPreferences.removePreference(PREF_TOPIC);
        sharedPreferences.removePreference(PREF_BROKER);
        sharedPreferences.removePreference(PREF_ALARM_MODE);
    }
}