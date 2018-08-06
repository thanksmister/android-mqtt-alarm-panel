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

package com.thanksmister.iot.mqtt.alarmpanel.persistence

import android.content.Context
import android.text.TextUtils
import com.thanksmister.iot.mqtt.alarmpanel.R
import com.thanksmister.iot.mqtt.alarmpanel.utils.AlarmUtils
import com.thanksmister.iot.mqtt.alarmpanel.utils.AlarmUtils.Companion.MODE_DISARM

import dpreference.DPreference
import javax.inject.Inject

/**
 * Store configurations
 */
class Configuration @Inject
constructor(private val context: Context, private val sharedPreferences: DPreference) {

    var webUrl: String?
        get() = this.sharedPreferences.getPrefString(PREF_WEB_URL, null)
        set(value) = this.sharedPreferences.setPrefString(PREF_WEB_URL, value)

    var appPreventSleep: Boolean
        get() = this.sharedPreferences.getPrefBoolean(context.getString(R.string.key_setting_app_preventsleep), false)
        set(value) = this.sharedPreferences.setPrefBoolean(context.getString(R.string.key_setting_app_preventsleep), value)

    var useNightDayMode: Boolean
        get() = this.sharedPreferences.getPrefBoolean(PREF_DAY_NIGHT_MODE, true)
        set(value) = this.sharedPreferences.setPrefBoolean(PREF_DAY_NIGHT_MODE, value)

    var dayNightMode: String
        get() = this.sharedPreferences.getPrefString(DISPLAY_MODE_DAY_NIGHT, DISPLAY_MODE_DAY)
        set(value) = this.sharedPreferences.setPrefString(DISPLAY_MODE_DAY_NIGHT, value)

    var nightModeChanged: Boolean
        get() = this.sharedPreferences.getPrefBoolean(DISPLAY_MODE_DAY_NIGHT_CHANGED, false)
        set(value) = this.sharedPreferences.setPrefBoolean(DISPLAY_MODE_DAY_NIGHT_CHANGED, value)

    var dayNightModeStartTime: String
        get() = this.sharedPreferences.getPrefString(PREF_MODE_DAY_NIGHT_START, DAY_NIGHT_START_VALUE_DEFAULT)
        set(value) = this.sharedPreferences.setPrefString(PREF_MODE_DAY_NIGHT_START, value)

    var dayNightModeEndTime: String
        get() = this.sharedPreferences.getPrefString(PREF_MODE_DAY_NIGHT_END, DAY_NIGHT_END_VALUE_DEFAULT)
        set(value) = this.sharedPreferences.setPrefString(PREF_MODE_DAY_NIGHT_END, value)

    var screenBrightness: Int
        get() = this.sharedPreferences.getPrefInt(PREF_DEVICE_SCREEN_BRIGHTNESS, 5)
        set(value) = this.sharedPreferences.setPrefInt(PREF_DEVICE_SCREEN_BRIGHTNESS, value)

    var alarmMode: String
        get() = this.sharedPreferences.getPrefString(PREF_ALARM_MODE, MODE_DISARM)
        set(value) = this.sharedPreferences.setPrefString(PREF_ALARM_MODE, value)

    var fullScreen: Boolean
        get() = this.sharedPreferences.getPrefBoolean(PREF_FULL_SCXREEN, true)
        set(value) = this.sharedPreferences.setPrefBoolean(PREF_FULL_SCXREEN, value)

    var systemAlerts: Boolean
        get() = this.sharedPreferences.getPrefBoolean(PREF_SYSTEM_NOTIFICATIONS, false)
        set(value) = this.sharedPreferences.setPrefBoolean(PREF_SYSTEM_NOTIFICATIONS, value)

    var inactivityTime: Long
        get() = this.sharedPreferences.getPrefLong(PREF_INACTIVITY_TIME, 300000)
        set(value) = this.sharedPreferences.setPrefLong(PREF_INACTIVITY_TIME, value)

    var isFirstTime: Boolean
        get() = sharedPreferences.getPrefBoolean(PREF_FIRST_TIME, true)
        set(value) = sharedPreferences.setPrefBoolean(PREF_FIRST_TIME, value)

    var fingerPrint: Boolean
        get() = sharedPreferences.getPrefBoolean(PREF_FINGERPRINT, false)
        set(value) = sharedPreferences.setPrefBoolean(PREF_FINGERPRINT, value)

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

    var cameraMotionWake: Boolean
        get() = sharedPreferences.getPrefBoolean(context.getString(R.string.key_setting_camera_motionwake), false)
        set(value) = sharedPreferences.setPrefBoolean(context.getString(R.string.key_setting_camera_motionwake), value)

    var cameraFaceWake: Boolean
        get() = sharedPreferences.getPrefBoolean(context.getString(R.string.key_setting_camera_facewake), false)
        set(value) = sharedPreferences.setPrefBoolean(context.getString(R.string.key_setting_camera_facewake), value)

    var cameraFPS: Float
        get() = this.sharedPreferences.getPrefString(context.getString(R.string.key_setting_camera_fps), "15f").toFloat()
        set(value) = this.sharedPreferences.setPrefString(context.getString(R.string.key_setting_camera_fps), value.toString())

    var cameraId: Int
        get() = this.sharedPreferences.getPrefInt(context.getString(R.string.key_setting_camera_cameraid), 0)
        set(value) = this.sharedPreferences.setPrefInt(context.getString(R.string.key_setting_camera_cameraid), value)

    var cameraMotionMinLuma: Int
        get() = this.sharedPreferences.getPrefInt(context.getString(R.string.key_setting_camera_motionminluma),
                context.getString(R.string.default_setting_camera_motionminluma).toInt())
        set(value) = this.sharedPreferences.setPrefInt(context.getString(R.string.key_setting_camera_motionminluma), value)

    var mqttSensorFrequency: Int
        get() = this.sharedPreferences.getPrefInt(context.getString(R.string.key_setting_mqtt_sensorfrequency),
                context.getString(R.string.default_setting_mqtt_sensorfrequency).toInt())
        set(value) = this.sharedPreferences.setPrefInt(context.getString(R.string.key_setting_mqtt_sensorfrequency), value)

    var httpPort: Int
        get() = this.sharedPreferences.getPrefInt(context.getString(R.string.key_setting_http_port),
                context.getString(R.string.default_setting_http_port).toInt())
        set(value) = this.sharedPreferences.setPrefInt(context.getString(R.string.key_setting_http_port), value)

    var cameraMotionLeniency: Int
        get() = this.sharedPreferences.getPrefInt(context.getString(R.string.key_setting_camera_motionleniency),
                context.getString(R.string.default_setting_camera_motionleniency).toInt())
        set(value) = this.sharedPreferences.setPrefInt(context.getString(R.string.key_setting_camera_motionleniency), value)

    var httpMJPEGMaxStreams: Int
        get() = this.sharedPreferences.getPrefInt(context.getString(R.string.key_setting_http_mjpegmaxstreams),
                context.getString(R.string.default_setting_http_mjpegmaxstreams).toInt())
        set(value) = this.sharedPreferences.setPrefInt(context.getString(R.string.key_setting_http_mjpegmaxstreams), value)

    var motionResetTime: Int
        get() = this.sharedPreferences.getPrefInt(context.getString(R.string.key_setting_motion_clear),
                context.getString(R.string.default_motion_clear).toInt())
        set(value) = this.sharedPreferences.setPrefInt(context.getString(R.string.key_setting_motion_clear), value)

    var cameraEnabled: Boolean
        get() = this.sharedPreferences.getPrefBoolean(context.getString(R.string.key_setting_camera_enabled), false)
        set(value) = this.sharedPreferences.setPrefBoolean(context.getString(R.string.key_setting_camera_enabled), value)

    var cameraMotionEnabled: Boolean
        get() = this.sharedPreferences.getPrefBoolean(context.getString(R.string.key_setting_camera_motionenabled), false)
        set(value) = this.sharedPreferences.setPrefBoolean(context.getString(R.string.key_setting_camera_motionenabled), value)

    var cameraMotionWakeEnabled: Boolean
        get() = this.sharedPreferences.getPrefBoolean(context.getString(R.string.key_setting_camera_motionwake), false)
        set(value) = this.sharedPreferences.setPrefBoolean(context.getString(R.string.key_setting_camera_motionwake), value)

    var sensorsEnabled: Boolean
        get() = this.sharedPreferences.getPrefBoolean(PREF_SENSOR_ENABLED, false)
        set(value) = this.sharedPreferences.setPrefBoolean(PREF_SENSOR_ENABLED, value)

    var cameraFaceEnabled: Boolean
        get() = this.sharedPreferences.getPrefBoolean(context.getString(R.string.key_setting_camera_faceenabled), false)
        set(value) = this.sharedPreferences.setPrefBoolean(context.getString(R.string.key_setting_camera_faceenabled), value)

    var cameraQRCodeEnabled: Boolean
        get() = this.sharedPreferences.getPrefBoolean(context.getString(R.string.key_setting_camera_qrcodeenabled), false)
        set(value) = this.sharedPreferences.setPrefBoolean(context.getString(R.string.key_setting_camera_qrcodeenabled), value)

    var httpMJPEGEnabled: Boolean
        get() = this.sharedPreferences.getPrefBoolean(context.getString(R.string.key_setting_http_mjpegenabled), false)
        set(value) = this.sharedPreferences.setPrefBoolean(context.getString(R.string.key_setting_http_mjpegenabled), value)

    var deviceSensors: Boolean
        get() = this.sharedPreferences.getPrefBoolean(context.getString(R.string.key_setting_sensors_enabled), false)
        set(value) = this.sharedPreferences.setPrefBoolean(context.getString(R.string.key_setting_sensors_enabled), value)

    var deviceSensorFrequency: Int
        get() = this.sharedPreferences.getPrefInt(context.getString(R.string.key_setting_mqtt_sensorfrequency),
                context.getString(R.string.default_setting_mqtt_sensorfrequency).toInt())
        set(value) = this.sharedPreferences.setPrefInt(context.getString(R.string.key_setting_mqtt_sensorfrequency), value)

    var appShowActivity: Boolean
        get() = this.sharedPreferences.getPrefBoolean(context.getString(R.string.key_setting_app_showactivity), false)
        set(value) = this.sharedPreferences.setPrefBoolean(context.getString(R.string.key_setting_app_showactivity), value)

    var testZoomLevel: Float
        get() = this.sharedPreferences.getPrefString(context.getString(R.string.key_setting_test_zoomlevel),
                context.getString(R.string.default_setting_test_zoomlevel)).toFloat()
        set(value) = this.sharedPreferences.setPrefString(context.getString(R.string.key_setting_test_zoomlevel), value.toString())

    var browserUserAgent: String
        get() = sharedPreferences.getPrefString(context.getString(R.string.key_setting_browser_user_agent),
                context.getString(R.string.default_browser_user_agent))
        set(value) = this.sharedPreferences.setPrefString(context.getString(R.string.key_setting_browser_user_agent), value)

    fun hasPlatformModule(): Boolean {
        return sharedPreferences.getPrefBoolean(PREF_MODULE_WEB, false)
    }

    fun setWebModule(value: Boolean) {
        this.sharedPreferences.setPrefBoolean(PREF_MODULE_WEB, value)
    }

    fun hasPlatformChange(): Boolean {
        return sharedPreferences.getPrefBoolean(PREF_PLATFORM_CHANGED, false)
    }

    fun setHasPlatformChange(value: Boolean) {
        sharedPreferences.setPrefBoolean(PREF_PLATFORM_CHANGED, value)
    }

    fun setTssModule(value: Boolean) {
        this.sharedPreferences.setPrefBoolean(PREF_MODULE_TSS, value)
    }

    fun hasSystemAlerts(): Boolean {
        return sharedPreferences.getPrefBoolean(PREF_SYSTEM_NOTIFICATIONS, false)
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

    fun hasCameraCapture(): Boolean {
        return sharedPreferences.getPrefBoolean(PREF_CAMERA_CAPTURE, false)
    }

    fun setHasCameraCapture(value: Boolean) {
        this.sharedPreferences.setPrefBoolean(PREF_CAMERA_CAPTURE, value)
    }

    fun captureCameraImage() : Boolean {
        return cameraEnabled && hasCameraCapture() && (hasTelegramCredentials() || hasMailGunCredentials())
    }

    fun hasCameraDetections() : Boolean {
        return cameraEnabled && (cameraMotionEnabled || cameraQRCodeEnabled || cameraFaceEnabled || httpMJPEGEnabled)
    }

    fun hasScreenSaver() : Boolean {
        return (showPhotoScreenSaver() || showClockScreenSaverModule())
    }

    fun isAlarmTriggeredMode(): Boolean {
        return alarmMode == AlarmUtils.MODE_TRIGGERED
                || alarmMode == AlarmUtils.MODE_HOME_TRIGGERED_PENDING
                || alarmMode == AlarmUtils.MODE_AWAY_TRIGGERED_PENDING
                || alarmMode == AlarmUtils.MODE_TRIGGERED_PENDING
    }

    fun isAlarmPendingMode(): Boolean {
        return (alarmMode == AlarmUtils.MODE_ARM_AWAY_PENDING
                || alarmMode == AlarmUtils.MODE_ARM_HOME_PENDING
                || alarmMode == AlarmUtils.MODE_AWAY_TRIGGERED_PENDING
                || alarmMode == AlarmUtils.MODE_HOME_TRIGGERED_PENDING)
    }

    fun isAlarmDisableMode(): Boolean {
        return (alarmMode == AlarmUtils.MODE_ARM_HOME
                || alarmMode == AlarmUtils.MODE_ARM_AWAY
                || alarmMode == AlarmUtils.MODE_HOME_TRIGGERED_PENDING
                || alarmMode == AlarmUtils.MODE_AWAY_TRIGGERED_PENDING
                || alarmMode == AlarmUtils.MODE_TRIGGERED_PENDING)
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
        sharedPreferences.removePreference(PREF_CAMERA_CAPTURE)
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
        sharedPreferences.removePreference(PREF_FINGERPRINT)
        sharedPreferences.removePreference(PREF_PLATFORM_BACK_BEHAVIOR)
        sharedPreferences.removePreference(PREF_PLATFORM_ADMIN_MENU)
        sharedPreferences.removePreference(PREF_FULL_SCXREEN)
    }

    companion object {
        const val PREF_PLATFORM_BACK_BEHAVIOR = "pref_platform_back_behavior"
        const val PREF_PLATFORM_ADMIN_MENU = "pref_platform_admin_menu"
        const val PREF_FINGERPRINT = "pref_fingerprint"
        const val PREF_PENDING_TIME = "pref_pending_time"
        const val PREF_HOME_PENDING_TIME = "pref_home_pending_time"
        const val PREF_AWAY_PENDING_TIME = "pref_away_pending_time"
        const val PREF_DELAY_TIME = "pref_delay_time"
        const val PREF_HOME_DELAY_TIME = "pref_home_delay_time"
        const val PREF_AWAY_DELAY_TIME = "pref_away_delay_time"
        const val PREF_ALARM_MODE = "pref_alarm_mode"
        const val PREF_ALARM_CODE = "pref_alarm_code"
        const val PREF_MODULE_CLOCK_SAVER = "pref_module_saver_clock"
        const val PREF_MODULE_PHOTO_SAVER = "pref_module_saver_photo"
        const val PREF_IMAGE_SOURCE = "pref_image_source"
        const val PREF_IMAGE_FIT_SIZE = "pref_image_fit"
        const val PREF_IMAGE_ROTATION = "pref_image_rotation"
        const val PREF_IMAGE_CLIENT_ID = "pref_image_client_id"
        const val PREF_INACTIVITY_TIME = "pref_inactivity_time"
        const val PREF_FULL_SCXREEN = "pref_full_screen"
        const val PREF_MODULE_NOTIFICATION = "pref_module_notification"
        const val PREF_SYSTEM_SOUNDS = "pref_system_sounds"
        const val PREF_MODULE_TSS = "pref_module_tss"
        const val PREF_SYSTEM_NOTIFICATIONS = "pref_system_notifications"
        const val PREF_MODULE_ALERTS = "pref_module_alerts"
        const val PREF_MAIL_TO = "pref_mail_to"
        const val PREF_MAIL_FROM = "pref_mail_from"
        const val PREF_MAIL_API_KEY = "pref_mail_api_key"
        const val PREF_MAIL_URL = "pref_mail_url"
        const val PREF_DISABLE_DIALOG_TIME = "pref_disable_dialog_time" // this isn't configurable
        const val PREF_CAMERA_CAPTURE = "pref_module_camera"
        const val PREF_CAMERA_ROTATE = "pref_camera_rotate"
        const val PREF_MODULE_WEATHER = "pref_module_weather"
        const val PREF_MODULE_WEB = "pref_module_web"
        const val PREF_WEB_URL = "pref_web_url"
        const val PREF_FIRST_TIME = "pref_first_time"
        const val PREF_DEVICE_TIME_SERVER = "pref_device_time_server"
        const val PREF_DEVICE_TIME_FORMAT = "pref_device_time_format"
        const val PREF_DEVICE_TIME = "pref_device_time"
        const val PREF_DEVICE_TIME_ZONE = "pref_device_time_zone"
        const val PREF_DEVICE_SCREEN_DENSITY = "pref_device_screen_density"
        const val PREF_DEVICE_SCREEN_BRIGHTNESS = "pref_screen_brightness"
        const val PREF_DEVICE_SCREEN_TIMEOUT = "pref_device_timeout"
        const val PREF_WEATHER_WEATHER = "pref_weather_module"
        const val PREF_WEATHER_UNITS = "pref_weather_units"
        const val PREF_WEATHER_API_KEY = "pref_weather_api_key"
        const val PREF_WEATHER_LATITUDE = "pref_weather_latitude"
        const val PREF_WEATHER_LONGITUDE = "pref_weather_longitude"
        const val PREF_PLATFORM_BAR = "pref_platform_bar"
        const val PREF_TELEGRAM_MODULE = "pref_telegram_module"
        const val PREF_TELEGRAM_CHAT_ID = "pref_telegram_chat_id"
        const val PREF_TELEGRAM_TOKEN = "pref_telegram_token"
        const val PREF_DAY_NIGHT_MODE = "pref_day_night_mode"
        const val PREF_MODE_DAY_NIGHT_END = "mode_day_night_end"
        const val PREF_MODE_DAY_NIGHT_START = "mode_day_night_start"
        const val DISPLAY_MODE_DAY_NIGHT = "mode_day_night"
        const val DISPLAY_MODE_DAY_NIGHT_CHANGED = "mode_day_night_changed"
        const val DISPLAY_MODE_DAY = "mode_day"
        const val DISPLAY_MODE_NIGHT = "mode_night"
        const val DAY_NIGHT_START_VALUE_DEFAULT = "19:00"
        const val DAY_NIGHT_END_VALUE_DEFAULT = "6:00"
        const val PREF_SENSOR_ENABLED = "pref_device_sensors_enabled"
        const val PREF_PLATFORM_CHANGED = "pref_platform_changed"
    }
}