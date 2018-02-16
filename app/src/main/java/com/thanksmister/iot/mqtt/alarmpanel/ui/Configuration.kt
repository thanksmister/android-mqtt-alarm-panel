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

package com.thanksmister.iot.mqtt.alarmpanel.ui

import android.text.TextUtils
import com.thanksmister.iot.mqtt.alarmpanel.utils.AlarmUtils
import com.thanksmister.iot.mqtt.alarmpanel.utils.AlarmUtils.Companion.MODE_DISARM

import dpreference.DPreference
import javax.inject.Inject

/**
 * Store configurations
 */
class Configuration @Inject
constructor(private val sharedPreferences: DPreference) {

    var webUrl: String?
        get() = this.sharedPreferences.getPrefString(PREF_WEB_URL, null)
        set(value) = this.sharedPreferences.setPrefString(PREF_WEB_URL, value)

    var alarmMode: String
        get() = this.sharedPreferences.getPrefString(PREF_ALARM_MODE, MODE_DISARM)
        set(value) = this.sharedPreferences.setPrefString(PREF_ALARM_MODE, value)

    var systemAlerts: Boolean
        get() = this.sharedPreferences.getPrefBoolean(PREF_SYSTEM_NOTIFICATIONS, false)
        set(value) = this.sharedPreferences.setPrefBoolean(PREF_SYSTEM_NOTIFICATIONS, value)

    var inactivityTime: Long
        get() = this.sharedPreferences.getPrefLong(PREF_INACTIVITY_TIME, 300000)
        set(value) = this.sharedPreferences.setPrefLong(PREF_INACTIVITY_TIME, value)

    var isFirstTime: Boolean
        get() = sharedPreferences.getPrefBoolean(PREF_FIRST_TIME, true)
        set(value) = sharedPreferences.setPrefBoolean(PREF_FIRST_TIME, value)

    var pendingTime: Int
        get() = this.sharedPreferences.getPrefInt(PREF_PENDING_TIME, AlarmUtils.PENDING_TIME)
        set(value) = this.sharedPreferences.setPrefInt(PREF_PENDING_TIME, value)

    var delayTime: Int
        get() = this.sharedPreferences.getPrefInt(PREF_DELAY_TIME, AlarmUtils.DELAY_TIME)
        set(value) = this.sharedPreferences.setPrefInt(PREF_DELAY_TIME, value)

    var delayHomeTime: Int
        get() = this.sharedPreferences.getPrefInt(PREF_HOME_DELAY_TIME, AlarmUtils.DELAY_HOME_TIME)
        set(value) = this.sharedPreferences.setPrefInt(PREF_HOME_DELAY_TIME, value)

    var delayAwayTime: Int
        get() = this.sharedPreferences.getPrefInt(PREF_AWAY_DELAY_TIME, AlarmUtils.DELAY_AWAY_TIME)
        set(value) = this.sharedPreferences.setPrefInt(PREF_AWAY_DELAY_TIME, value)

    var pendingHomeTime: Int
        get() = this.sharedPreferences.getPrefInt(PREF_HOME_PENDING_TIME, AlarmUtils.PENDING_HOME_TIME)
        set(value) = this.sharedPreferences.setPrefInt(PREF_HOME_PENDING_TIME, value)

    var pendingAwayTime: Int
        get() = this.sharedPreferences.getPrefInt(PREF_AWAY_PENDING_TIME, AlarmUtils.PENDING_AWAY_TIME)
        set(value) = this.sharedPreferences.setPrefInt(PREF_AWAY_PENDING_TIME, value)

    var disableTime: Int
        get() = this.sharedPreferences.getPrefInt(PREF_DISABLE_DIALOG_TIME, AlarmUtils.DISABLE_TIME)
        set(value) = this.sharedPreferences.setPrefInt(PREF_DISABLE_DIALOG_TIME, value)

    var alarmCode: Int
        get() = this.sharedPreferences.getPrefInt(PREF_ALARM_CODE, 1234)
        set(value) = this.sharedPreferences.setPrefInt(PREF_ALARM_CODE, value)

    var platformBar: Boolean
        get() = this.sharedPreferences.getPrefBoolean(PREF_PLATFORM_BAR, true)
        set(value) = this.sharedPreferences.setPrefBoolean(PREF_PLATFORM_BAR, value)

    var systemSounds: Boolean
        get() = this.sharedPreferences.getPrefBoolean(PREF_SYSTEM_SOUNDS, true)
        set(value) = this.sharedPreferences.setPrefBoolean(PREF_SYSTEM_SOUNDS, value)

    var telegramChatId: String
        get() = this.sharedPreferences.getPrefString(PREF_TELEGRAM_CHAT_ID, "")
        set(value) = this.sharedPreferences.setPrefString(PREF_TELEGRAM_CHAT_ID, value)

    var telegramToken: String
        get() = this.sharedPreferences.getPrefString(PREF_TELEGRAM_TOKEN, "")
        set(value) = this.sharedPreferences.setPrefString(PREF_TELEGRAM_TOKEN, value)

    var telegramModule: Boolean
        get() = sharedPreferences.getPrefBoolean(PREF_TELEGRAM_MODULE, true)
        set(value) = sharedPreferences.setPrefBoolean(PREF_TELEGRAM_MODULE, value)

    fun hasPlatformModule(): Boolean {
        return sharedPreferences.getPrefBoolean(PREF_MODULE_WEB, false)
    }

    fun setWebModule(value: Boolean) {
        this.sharedPreferences.setPrefBoolean(PREF_MODULE_WEB, value)
    }

    fun hasTssModule(): Boolean {
        return sharedPreferences.getPrefBoolean(PREF_MODULE_TSS, false)
    }

    fun setTssModule(value: Boolean) {
        this.sharedPreferences.setPrefBoolean(PREF_MODULE_TSS, value)
    }

    fun hasAlertsModule(): Boolean {
        return sharedPreferences.getPrefBoolean(PREF_MODULE_ALERTS, false)
    }

    fun hasSystemAlerts(): Boolean {
        return sharedPreferences.getPrefBoolean(PREF_SYSTEM_NOTIFICATIONS, false)
    }

    fun setAlertsModule(value: Boolean) {
        this.sharedPreferences.setPrefBoolean(PREF_MODULE_ALERTS, value)
    }

    fun showClockScreenSaverModule(): Boolean {
        return sharedPreferences.getPrefBoolean(PREF_MODULE_CLOCK_SAVER, false)
    }

    fun setClockScreenSaverModule(value: Boolean) {
        this.sharedPreferences.setPrefBoolean(PREF_MODULE_CLOCK_SAVER, value)
    }

    fun showPhotoScreenSaver(): Boolean {
        return sharedPreferences.getPrefBoolean(PREF_MODULE_PHOTO_SAVER, false)
    }

    fun setPhotoScreenSaver(value: Boolean) {
        this.sharedPreferences.setPrefBoolean(PREF_MODULE_PHOTO_SAVER, value)
    }

    fun setShowWeatherModule(show: Boolean) {
        sharedPreferences.setPrefBoolean(PREF_MODULE_WEATHER, show)
    }

    fun showWeatherModule(): Boolean {
        return sharedPreferences.getPrefBoolean(PREF_MODULE_WEATHER, false)
    }

    fun hasNotifications(): Boolean {
        return sharedPreferences.getPrefBoolean(PREF_MODULE_NOTIFICATION, false)
    }

    fun setHasNotifications(value: Boolean) {
        this.sharedPreferences.setPrefBoolean(PREF_MODULE_NOTIFICATION, value)
    }

    fun getMailTo(): String? {
        return sharedPreferences.getPrefString(PREF_MAIL_TO, null)
    }

    fun setMailTo(value: String) {
        sharedPreferences.setPrefString(PREF_MAIL_TO, value)
    }

    fun getMailFrom(): String? {
        return sharedPreferences.getPrefString(PREF_MAIL_FROM, null)
    }

    fun setMailFrom(value: String) {
        sharedPreferences.setPrefString(PREF_MAIL_FROM, value)
    }

    fun getMailGunApiKey(): String? {
        return sharedPreferences.getPrefString(PREF_MAIL_API_KEY, null)
    }

    fun setMailGunApiKey(value: String) {
        sharedPreferences.setPrefString(PREF_MAIL_API_KEY, value)
    }

    fun getMailGunUrl(): String? {
        return sharedPreferences.getPrefString(PREF_MAIL_URL, null)
    }

    fun setMailGunUrl(value: String) {
        sharedPreferences.setPrefString(PREF_MAIL_URL, value)
    }

    fun hasMailGunCredentials(): Boolean {
        return !TextUtils.isEmpty(getMailGunUrl()) && !TextUtils.isEmpty(getMailGunApiKey())
                && !TextUtils.isEmpty(getMailTo()) && !TextUtils.isEmpty(getMailFrom())
    }

    fun hasTelegramCredentials(): Boolean {
        return !TextUtils.isEmpty(telegramChatId) && !TextUtils.isEmpty(telegramToken)
    }

    fun hasCamera(): Boolean {
        return sharedPreferences.getPrefBoolean(PREF_MODULE_CAMERA, false)
    }

    fun setHasCamera(value: Boolean) {
        this.sharedPreferences.setPrefBoolean(PREF_MODULE_CAMERA, value)
    }

    fun getCameraRotate(): Float? {
        return sharedPreferences.getPrefString(PREF_CAMERA_ROTATE, "0f").toFloat()
    }

    fun setCameraRotate(value: String) {
        sharedPreferences.setPrefString(PREF_CAMERA_ROTATE, value)
    }

    /**
     * Reset the `SharedPreferences` and database
     */
    fun reset() {
        sharedPreferences.removePreference(PREF_PENDING_TIME)
        sharedPreferences.removePreference(PREF_MODULE_CLOCK_SAVER)
        sharedPreferences.removePreference(PREF_MODULE_PHOTO_SAVER)
        sharedPreferences.removePreference(PREF_INACTIVITY_TIME)
        sharedPreferences.removePreference(PREF_MODULE_WEATHER)
        sharedPreferences.removePreference(PREF_MODULE_WEB)
        sharedPreferences.removePreference(PREF_MODULE_NOTIFICATION)
        sharedPreferences.removePreference(PREF_WEB_URL)
        sharedPreferences.removePreference(PREF_FIRST_TIME)
        sharedPreferences.removePreference(PREF_MAIL_TO)
        sharedPreferences.removePreference(PREF_MAIL_FROM)
        sharedPreferences.removePreference(PREF_MODULE_CAMERA)
        sharedPreferences.removePreference(PREF_MAIL_API_KEY)
        sharedPreferences.removePreference(PREF_MAIL_URL)
        sharedPreferences.removePreference(PREF_CAMERA_ROTATE)
        sharedPreferences.removePreference(PREF_MODULE_TSS)
        sharedPreferences.removePreference(PREF_MODULE_ALERTS)
        sharedPreferences.removePreference(PREF_DEVICE_SCREEN_DENSITY)
        sharedPreferences.removePreference(PREF_DEVICE_TIME_ZONE)
        sharedPreferences.removePreference(PREF_DEVICE_TIME)
        sharedPreferences.removePreference(PREF_DEVICE_TIME_FORMAT)
        sharedPreferences.removePreference(PREF_DEVICE_TIME_SERVER)
        sharedPreferences.removePreference(PREF_DEVICE_SCREEN_BRIGHTNESS)
        sharedPreferences.removePreference(PREF_DEVICE_SCREEN_TIMEOUT)
        sharedPreferences.removePreference(PREF_AWAY_DELAY_TIME)
        sharedPreferences.removePreference(PREF_HOME_DELAY_TIME)
        sharedPreferences.removePreference(PREF_DELAY_TIME)
        sharedPreferences.removePreference(PREF_AWAY_PENDING_TIME)
        sharedPreferences.removePreference(PREF_HOME_PENDING_TIME)
        sharedPreferences.removePreference(PREF_SYSTEM_NOTIFICATIONS)
        sharedPreferences.removePreference(PREF_PLATFORM_BAR)
        sharedPreferences.removePreference(PREF_TELEGRAM_MODULE)
        sharedPreferences.removePreference(PREF_TELEGRAM_CHAT_ID)
        sharedPreferences.removePreference(PREF_TELEGRAM_TOKEN)
    }

    companion object {
        @JvmField val PREF_PENDING_TIME = "pref_pending_time"
        @JvmField val PREF_HOME_PENDING_TIME = "pref_home_pending_time"
        @JvmField val PREF_AWAY_PENDING_TIME = "pref_away_pending_time"
        @JvmField val PREF_DELAY_TIME = "pref_delay_time"
        @JvmField val PREF_HOME_DELAY_TIME = "pref_home_delay_time"
        @JvmField val PREF_AWAY_DELAY_TIME = "pref_away_delay_time"

        @JvmField val PREF_ALARM_MODE = "pref_alarm_mode"
        @JvmField val PREF_ALARM_CODE = "pref_alarm_code"
        @JvmField val PREF_MODULE_CLOCK_SAVER = "pref_module_saver_clock"
        @JvmField val PREF_MODULE_PHOTO_SAVER = "pref_module_saver_photo"
        @JvmField val PREF_IMAGE_SOURCE = "pref_image_source"
        @JvmField val PREF_IMAGE_FIT_SIZE = "pref_image_fit"
        @JvmField val PREF_IMAGE_ROTATION = "pref_image_rotation"
        @JvmField val PREF_IMAGE_CLIENT_ID = "pref_image_client_id"
        @JvmField val PREF_INACTIVITY_TIME = "pref_inactivity_time"
        @JvmField val PREF_MODULE_NOTIFICATION = "pref_module_notification"
        @JvmField val PREF_SYSTEM_SOUNDS = "pref_system_sounds"
        @JvmField val PREF_MODULE_TSS = "pref_module_tss"
        @JvmField val PREF_SYSTEM_NOTIFICATIONS = "pref_system_notifications"
        @JvmField val PREF_MODULE_ALERTS = "pref_module_alerts"
        @JvmField val PREF_MAIL_TO = "pref_mail_to"
        @JvmField val PREF_MAIL_FROM = "pref_mail_from"
        @JvmField val PREF_MAIL_API_KEY = "pref_mail_api_key"
        @JvmField val PREF_MAIL_URL = "pref_mail_url"
        @JvmField val PREF_DISABLE_DIALOG_TIME = "pref_disable_dialog_time" // this isn't configurable
        @JvmField val PREF_MODULE_CAMERA = "pref_module_camera"
        @JvmField val PREF_CAMERA_ROTATE = "pref_camera_rotate"
        @JvmField  val PREF_MODULE_WEATHER = "pref_module_weather"
        @JvmField val PREF_MODULE_WEB = "pref_module_web"
        @JvmField val PREF_WEB_URL = "pref_web_url"
        private val PREF_FIRST_TIME = "pref_first_time"
        @JvmField val PREF_DEVICE_TIME_SERVER = "pref_device_time_server"
        @JvmField val PREF_DEVICE_TIME_FORMAT = "pref_device_time_format"
        @JvmField val PREF_DEVICE_TIME = "pref_device_time"
        @JvmField val PREF_DEVICE_TIME_ZONE = "pref_device_time_zone"
        @JvmField val PREF_DEVICE_SCREEN_DENSITY = "pref_device_screen_density"
        @JvmField val PREF_DEVICE_SCREEN_BRIGHTNESS = "pref_device_brightness"
        @JvmField val PREF_DEVICE_SCREEN_TIMEOUT = "pref_device_timeout"
        @JvmField val PREF_WEATHER_WEATHER = "pref_weather_module"
        @JvmField val PREF_WEATHER_UNITS = "pref_weather_units"
        @JvmField val PREF_WEATHER_API_KEY = "pref_weather_api_key"
        @JvmField val PREF_WEATHER_LATITUDE = "pref_weather_latitude"
        @JvmField val PREF_WEATHER_LONGITUDE = "pref_weather_longitude"
        @JvmField val PREF_PLATFORM_BAR = "pref_platform_bar"
        @JvmField val PREF_TELEGRAM_MODULE = "pref_telegram_module"
        @JvmField val PREF_TELEGRAM_CHAT_ID = "pref_telegram_chat_id"
        @JvmField val PREF_TELEGRAM_TOKEN = "pref_telegram_token"
    }
}