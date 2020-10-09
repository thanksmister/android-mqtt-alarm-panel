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
import android.content.SharedPreferences
import android.text.TextUtils
import com.thanksmister.iot.mqtt.alarmpanel.R
import com.thanksmister.iot.mqtt.alarmpanel.utils.AlarmUtils
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils.Companion.STATE_DISARMED
import java.lang.Exception
import javax.inject.Inject

class Configuration @Inject
constructor(private val context: Context, private val sharedPreferences: SharedPreferences) {

    var useNightDayMode: Boolean
        get() = this.sharedPreferences.getBoolean(PREF_DAY_NIGHT_MODE, false)
        set(value) = this.sharedPreferences.edit().putBoolean(PREF_DAY_NIGHT_MODE, value).apply()

    var useDarkTheme: Boolean
        get() = this.sharedPreferences.getBoolean(PREF_DARK_THEME, false)
        set(value) = this.sharedPreferences.edit().putBoolean(PREF_DARK_THEME, value).apply()

    var sensorOne: Boolean
        get() = this.sharedPreferences.getBoolean(PREF_SENSOR_ONE, false)
        set(value) = this.sharedPreferences.edit().putBoolean(PREF_SENSOR_ONE, value).apply()

    var sensorOneName: String
        get() = this.sharedPreferences.getString(PREF_SENSOR_ONE_NAME, "Sensor").orEmpty()
        set(value) = this.sharedPreferences.edit().putString(PREF_SENSOR_ONE_NAME, value).apply()

    var sensorTwo: Boolean
        get() = this.sharedPreferences.getBoolean(PREF_SENSOR_TWO, false)
        set(value) = this.sharedPreferences.edit().putBoolean(PREF_SENSOR_TWO, value).apply()

    var sensorTwoName: String
        get() = this.sharedPreferences.getString(PREF_SENSOR_TWO_NAME, "Sensor").orEmpty()
        set(value) = this.sharedPreferences.edit().putString(PREF_SENSOR_TWO_NAME, value).apply()

    var alarmMode: String
        get() = this.sharedPreferences.getString(PREF_ALARM_MODE, STATE_DISARMED).orEmpty()
        set(value) = this.sharedPreferences.edit().putString(PREF_ALARM_MODE, value).apply()

    var systemAlerts: Boolean
        get() = this.sharedPreferences.getBoolean(PREF_SYSTEM_NOTIFICATIONS, false)
        set(value) = this.sharedPreferences.edit().putBoolean(PREF_SYSTEM_NOTIFICATIONS, value).apply()

    var inactivityTime: Long
        get() {
            return try {
                this.sharedPreferences.getLong(PREF_INACTIVITY_TIME, 300000)
            } catch (e: Exception) {
                this.sharedPreferences.edit().putLong(PREF_INACTIVITY_TIME, 300000).apply()
                300000
            }
        }
        set(value) = this.sharedPreferences.edit().putLong(PREF_INACTIVITY_TIME, value).apply()

    var isFirstTime: Boolean
        get() = sharedPreferences.getBoolean(PREF_FIRST_TIME, true)
        set(value) = sharedPreferences.edit().putBoolean(PREF_FIRST_TIME, value).apply()

    var alarmCode: Int
        get() = this.sharedPreferences.getInt(PREF_ALARM_CODE, 1234)
        set(value) = this.sharedPreferences.edit().putInt(PREF_ALARM_CODE, value).apply()

    fun setTssModule(value: Boolean) {
        this.sharedPreferences.edit().putBoolean(PREF_MODULE_TSS, value).apply()
    }

    fun hasSystemAlerts(): Boolean {
        return sharedPreferences.getBoolean(PREF_SYSTEM_NOTIFICATIONS, false)
    }

    fun isAlarmArmedMode(): Boolean {
        return (alarmMode == MqttUtils.STATE_ARMED_HOME
                || alarmMode == MqttUtils.STATE_ARMED_AWAY
                || alarmMode == MqttUtils.STATE_ARMED_NIGHT)
    }

    fun isAlarmArming(): Boolean {
        return (alarmMode == MqttUtils.STATE_ARMING
                || alarmMode == MqttUtils.STATE_PENDING)
    }


    fun isAlarmDisarmedMode(): Boolean {
        return alarmMode == MqttUtils.STATE_DISARMED
    }

    fun isAlarmTriggered(): Boolean {
        return (alarmMode == MqttUtils.STATE_TRIGGERED)
    }

    var webUrl: String?
        get() = this.sharedPreferences.getString(PREF_WEB_URL, "")
        set(value) = this.sharedPreferences.edit().putString(PREF_WEB_URL, value.orEmpty()).apply()

    var appPreventSleep: Boolean
        get() = this.sharedPreferences.getBoolean(context.getString(R.string.key_setting_app_preventsleep), false)
        set(value) = this.sharedPreferences.edit().putBoolean(context.getString(R.string.key_setting_app_preventsleep), value).apply()

    var dayNightMode: String
        get() = this.sharedPreferences.getString(DISPLAY_MODE_DAY_NIGHT, DISPLAY_MODE_DAY).orEmpty()
        set(value) = this.sharedPreferences.edit().putString(DISPLAY_MODE_DAY_NIGHT, value).apply()

    var nightModeChanged: Boolean
        get() = this.sharedPreferences.getBoolean(DISPLAY_MODE_DAY_NIGHT_CHANGED, false)
        set(value) = this.sharedPreferences.edit().putBoolean(DISPLAY_MODE_DAY_NIGHT_CHANGED, value).apply()

    var dayNightModeStartTime: String
        get() = this.sharedPreferences.getString(PREF_MODE_DAY_NIGHT_START, DAY_NIGHT_START_VALUE_DEFAULT).orEmpty()
        set(value) = this.sharedPreferences.edit().putString(PREF_MODE_DAY_NIGHT_START, value).apply()

    var dayNightModeEndTime: String
        get() = this.sharedPreferences.getString(PREF_MODE_DAY_NIGHT_END, DAY_NIGHT_END_VALUE_DEFAULT).orEmpty()
        set(value) = this.sharedPreferences.edit().putString(PREF_MODE_DAY_NIGHT_END, value).apply()

    var screenBrightness: Int
        get() = this.sharedPreferences.getInt(PREF_DEVICE_SCREEN_BRIGHTNESS, 255)
        set(value) = this.sharedPreferences.edit().putInt(PREF_DEVICE_SCREEN_BRIGHTNESS, value).apply()

    var screenNightModeBrightness: Int
        get() = sharedPreferences.getInt(PREF_DEVICE_SCREEN_SAVER_BRIGHTNESS, 0)
        set(value) = sharedPreferences.edit().putInt(PREF_DEVICE_SCREEN_SAVER_BRIGHTNESS, value).apply()

    var nightModeDimValue: Int
        get() = sharedPreferences.getInt(PREF_SCREEN_DIM_VALUE, 25)
        set(value) = sharedPreferences.edit().putInt(PREF_SCREEN_DIM_VALUE, value).apply()

    var useScreenBrightness: Boolean
        get() = this.sharedPreferences.getBoolean(PREF_SCREEN_BRIGHTNESS, false)
        set(value) = this.sharedPreferences.edit().putBoolean(PREF_SCREEN_BRIGHTNESS, value).apply()

    var fullScreen: Boolean
        get() = this.sharedPreferences.getBoolean(PREF_FULL_SCREEN, true)
        set(value) = this.sharedPreferences.edit().putBoolean(PREF_FULL_SCREEN, value).apply()

    var userHardwareAcceleration: Boolean
        get() = sharedPreferences.getBoolean(PREF_HARDWARE_ACCELERATION, false)
        set(value) = sharedPreferences.edit().putBoolean(PREF_HARDWARE_ACCELERATION, value).apply()

    var fingerPrint: Boolean
        get() = sharedPreferences.getBoolean(PREF_FINGERPRINT, false)
        set(value) = sharedPreferences.edit().putBoolean(PREF_FINGERPRINT, value).apply()

    var platformBar: Boolean
        get() = this.sharedPreferences.getBoolean(PREF_PLATFORM_BAR, true)
        set(value) = this.sharedPreferences.edit().putBoolean(PREF_PLATFORM_BAR, value).apply()

    var platformRefresh: Boolean
        get() = this.sharedPreferences.getBoolean(PREF_PLATFORM_REFRESH, true)
        set(value) = this.sharedPreferences.edit().putBoolean(PREF_PLATFORM_REFRESH, value).apply()

    var systemSounds: Boolean
        get() = this.sharedPreferences.getBoolean(PREF_SYSTEM_SOUNDS, true)
        set(value) = this.sharedPreferences.edit().putBoolean(PREF_SYSTEM_SOUNDS, value).apply()

    var telegramChatId: String
        get() = this.sharedPreferences.getString(PREF_TELEGRAM_CHAT_ID, "").orEmpty()
        set(value) = this.sharedPreferences.edit().putString(PREF_TELEGRAM_CHAT_ID, value).apply()

    var telegramToken: String
        get() = this.sharedPreferences.getString(PREF_TELEGRAM_TOKEN, "").orEmpty()
        set(value) = this.sharedPreferences.edit().putString(PREF_TELEGRAM_TOKEN, value).apply()

    var cameraMotionWake: Boolean
        get() = sharedPreferences.getBoolean(context.getString(R.string.key_setting_camera_motionwake), false)
        set(value) = sharedPreferences.edit().putBoolean(context.getString(R.string.key_setting_camera_motionwake), value).apply()

    var cameraFaceWake: Boolean
        get() = sharedPreferences.getBoolean(context.getString(R.string.key_setting_camera_facewake), false)
        set(value) = sharedPreferences.edit().putBoolean(context.getString(R.string.key_setting_camera_facewake), value).apply()

    var cameraFPS: Float
        get() = this.sharedPreferences.getFloat(PREF_CAMERA_FPS, 15f)
        set(value) = this.sharedPreferences.edit().putFloat(PREF_CAMERA_FPS, value).apply()

    var cameraId: Int
        get() = this.sharedPreferences.getInt(PREF_CAMERA_ID, 0)
        set(value) = this.sharedPreferences.edit().putInt(PREF_CAMERA_ID, value).apply()

    var cameraOrientation: Int
        get() = this.sharedPreferences.getInt(PREF_CAMERA_ORIENTATION, 0)
        set(value) = this.sharedPreferences.edit().putInt(PREF_CAMERA_ORIENTATION, value).apply()

    var cameraWidth: Int
        get() = this.sharedPreferences.getInt(PREF_CAMERA_WIDTH, 640)
        set(value) = this.sharedPreferences.edit().putInt(PREF_CAMERA_WIDTH, value).apply()

    var cameraHeight: Int
        get() = this.sharedPreferences.getInt(PREF_CAMERA_HEIGHT, 480)
        set(value) = this.sharedPreferences.edit().putInt(PREF_CAMERA_HEIGHT, value).apply()

    var cameraMotionMinLuma: Int
        get() = this.sharedPreferences.getInt(PREF_MOTION_LUMA, 1000)
        set(value) = this.sharedPreferences.edit().putInt(PREF_MOTION_LUMA, value).apply()

    var cameraRotate: Float
        get() = this.sharedPreferences.getFloat(PREF_CAMERA_ROTATE, 0f)
        set(value) = this.sharedPreferences.edit().putString(PREF_CAMERA_ROTATE, value.toString()).apply()

    var mqttSensorFrequency: Int
        get() = this.sharedPreferences.getInt(PREF_SENSOR_FREQUENCY, 30)
        set(value) = this.sharedPreferences.edit().putInt(PREF_SENSOR_FREQUENCY, value).apply()

    var httpPort: Int
        get() = this.sharedPreferences.getInt(PREF_HTTP_PORT, 2971)
        set(value) = this.sharedPreferences.edit().putInt(PREF_HTTP_PORT, value).apply()

    var cameraMotionLeniency: Int
        get() = this.sharedPreferences.getInt(context.getString(R.string.key_setting_camera_motionleniency),
                context.getString(R.string.default_setting_camera_motionleniency).toInt())
        set(value) = this.sharedPreferences.edit().putInt(context.getString(R.string.key_setting_camera_motionleniency), value).apply()

    var httpMJPEGMaxStreams: Int
        get() = this.sharedPreferences.getInt(PREF_MAX_STREAMS, 1)
        set(value) = this.sharedPreferences.edit().putInt(PREF_MAX_STREAMS, value).apply()

    var motionResetTime: Int
        get() = this.sharedPreferences.getInt(PREF_MOTION_CLEAR, 30)
        set(value) = this.sharedPreferences.edit().putInt(PREF_MOTION_CLEAR, value).apply()

    var cameraEnabled: Boolean
        get() = this.sharedPreferences.getBoolean(context.getString(R.string.key_setting_camera_enabled), false)
        set(value) = this.sharedPreferences.edit().putBoolean(context.getString(R.string.key_setting_camera_enabled), value).apply()

    var cameraMotionEnabled: Boolean
        get() = this.sharedPreferences.getBoolean(context.getString(R.string.key_setting_camera_motionenabled), false)
        set(value) = this.sharedPreferences.edit().putBoolean(context.getString(R.string.key_setting_camera_motionenabled), value).apply()

    var cameraMotionWakeEnabled: Boolean
        get() = this.sharedPreferences.getBoolean(context.getString(R.string.key_setting_camera_motionwake), false)
        set(value) = this.sharedPreferences.edit().putBoolean(context.getString(R.string.key_setting_camera_motionwake), value).apply()

    var weatherUnitsImperial: Boolean
        get() = this.sharedPreferences.getBoolean(PREF_WEATHER_UNITS, false)
        set(value) = this.sharedPreferences.edit().putBoolean(PREF_WEATHER_UNITS, value).apply()

    var cameraFaceEnabled: Boolean
        get() = this.sharedPreferences.getBoolean(context.getString(R.string.key_setting_camera_faceenabled), false)
        set(value) = this.sharedPreferences.edit().putBoolean(context.getString(R.string.key_setting_camera_faceenabled), value).apply()

    var cameraQRCodeEnabled: Boolean
        get() = this.sharedPreferences.getBoolean(context.getString(R.string.key_setting_camera_qrcodeenabled), false)
        set(value) = this.sharedPreferences.edit().putBoolean(context.getString(R.string.key_setting_camera_qrcodeenabled), value).apply()

    var httpMJPEGEnabled: Boolean
        get() = this.sharedPreferences.getBoolean(context.getString(R.string.key_setting_http_mjpegenabled), false)
        set(value) = this.sharedPreferences.edit().putBoolean(context.getString(R.string.key_setting_http_mjpegenabled), value).apply()

    var deviceSensors: Boolean
        get() = this.sharedPreferences.getBoolean(context.getString(R.string.key_setting_sensors_enabled), false)
        set(value) = this.sharedPreferences.edit().putBoolean(context.getString(R.string.key_setting_sensors_enabled), value).apply()

    var deviceSensorFrequency: Int
        get() = this.sharedPreferences.getInt(PREF_SENSOR_FREQUENCY, 30)
        set(value) = this.sharedPreferences.edit().putInt(PREF_SENSOR_FREQUENCY, value).apply()

    var appShowActivity: Boolean
        get() = this.sharedPreferences.getBoolean(context.getString(R.string.key_setting_app_showactivity), false)
        set(value) = this.sharedPreferences.edit().putBoolean(context.getString(R.string.key_setting_app_showactivity), value).apply()

    var testZoomLevel: Float
        get() = this.sharedPreferences.getFloat(PREF_ZOOM_LEVEL, 0F)
        set(value) = this.sharedPreferences.edit().putFloat(PREF_ZOOM_LEVEL, value).apply()

    var browserUserAgent: String
        get() = sharedPreferences.getString(context.getString(R.string.key_setting_browser_user_agent), context.getString(R.string.default_browser_user_agent)).orEmpty()
        set(value) = this.sharedPreferences.edit().putString(context.getString(R.string.key_setting_browser_user_agent), value).apply()

    var writeScreenPermissionsShown: Boolean
        get() = sharedPreferences.getBoolean(PREF_WRITE_SCREEN_PERMISSIONS, false)
        set(value) = sharedPreferences.edit().putBoolean(PREF_WRITE_SCREEN_PERMISSIONS, value).apply()

    var webScreenSaver: Boolean
        get() = this.sharedPreferences.getBoolean(context.getString(R.string.key_setting_web_screensaver), false)
        set(value) = this.sharedPreferences.edit().putBoolean(context.getString(R.string.key_setting_web_screensaver), value).apply()

    var webScreenSaverUrl: String
        get() = sharedPreferences.getString(context.getString(R.string.key_setting_web_url), WEB_SCREEN_SAVER).orEmpty()
        set(value) = this.sharedPreferences.edit().putString(context.getString(R.string.key_setting_web_url), value).apply()


    fun hasPlatformModule(): Boolean {
        return sharedPreferences.getBoolean(PREF_MODULE_WEB, false)
    }

    var panicButton: Boolean
        get() = this.sharedPreferences.getBoolean(PREF_PANIC_ALERT, false)
        set(value) = this.sharedPreferences.edit().putBoolean(PREF_PANIC_ALERT, value).apply()

    fun setWebModule(value: Boolean) {
        this.sharedPreferences.edit().putBoolean(PREF_MODULE_WEB, value).apply()
    }

    fun hasPlatformChange(): Boolean {
        return sharedPreferences.getBoolean(PREF_PLATFORM_CHANGED, false)
    }

    fun setHasPlatformChange(value: Boolean) {
        sharedPreferences.edit().putBoolean(PREF_PLATFORM_CHANGED, value).apply()
    }


    fun showClockScreenSaverModule(): Boolean {
        return sharedPreferences.getBoolean(PREF_MODULE_CLOCK_SAVER, false)
    }

    fun setClockScreenSaverModule(value: Boolean) {
        this.sharedPreferences.edit().putBoolean(PREF_MODULE_CLOCK_SAVER, value).apply()
    }

    fun showPhotoScreenSaver(): Boolean {
        return sharedPreferences.getBoolean(context.getString(R.string.key_saver_photo), false)
    }

    fun setPhotoScreenSaver(value: Boolean) {
        this.sharedPreferences.edit().putBoolean(context.getString(R.string.key_saver_photo), value).apply()
    }

    fun setShowWeatherModule(show: Boolean) {
        sharedPreferences.edit().putBoolean(PREF_MODULE_WEATHER, show).apply()
    }

    fun showWeatherModule(): Boolean {
        return sharedPreferences.getBoolean(PREF_MODULE_WEATHER, false)
    }

    fun getMailTo(): String? {
        return sharedPreferences.getString(PREF_MAIL_TO, "")
    }

    fun setMailTo(value: String) {
        sharedPreferences.edit().putString(PREF_MAIL_TO, value).apply()
    }

    fun getMailFrom(): String? {
        return sharedPreferences.getString(PREF_MAIL_FROM, "")
    }

    fun setMailFrom(value: String) {
        sharedPreferences.edit().putString(PREF_MAIL_FROM, value).apply()
    }

    fun getMailGunApiKey(): String? {
        return sharedPreferences.getString(PREF_MAIL_API_KEY, "")
    }

    fun setMailGunApiKey(value: String) {
        sharedPreferences.edit().putString(PREF_MAIL_API_KEY, value).apply()
    }

    fun getMailGunUrl(): String? {
        return sharedPreferences.getString(PREF_MAIL_URL, "")
    }

    fun setMailGunUrl(value: String) {
        sharedPreferences.edit().putString(PREF_MAIL_URL, value).apply()
    }

    fun hasMailGunCredentials(): Boolean {
        return !TextUtils.isEmpty(getMailGunUrl()) && !TextUtils.isEmpty(getMailGunApiKey())
                && !TextUtils.isEmpty(getMailTo()) && !TextUtils.isEmpty(getMailFrom())
    }

    fun hasTelegramCredentials(): Boolean {
        return !TextUtils.isEmpty(telegramChatId) && !TextUtils.isEmpty(telegramToken)
    }

    fun hasCameraCapture(): Boolean {
        return sharedPreferences.getBoolean(PREF_CAMERA_CAPTURE, false)
    }

    fun setHasCameraCapture(value: Boolean) {
        this.sharedPreferences.edit().putBoolean(PREF_CAMERA_CAPTURE, value).apply()
    }

    fun captureCameraImage(): Boolean {
        return cameraEnabled && hasCameraCapture() && (hasTelegramCredentials() || hasMailGunCredentials())
    }

    fun hasCameraDetections(): Boolean {
        return cameraEnabled && (cameraMotionEnabled || cameraQRCodeEnabled || cameraFaceEnabled || httpMJPEGEnabled)
    }

    fun hasScreenSaver(): Boolean {
        return (showPhotoScreenSaver() || showClockScreenSaverModule() || webScreenSaver)
    }

    /**
     * Reset the `SharedPreferences` and database
     */
    fun reset() {
        sharedPreferences.edit().clear().apply()
    }

    companion object {
        const val PREF_FINGERPRINT = "pref_fingerprint"
        const val PREF_ALARM_MODE = "pref_alarm_mode"
        private const val PREF_ALARM_CODE = "pref_alarm_code"
        const val PREF_MODULE_CLOCK_SAVER = "pref_module_saver_clock"
        const val PREF_INACTIVITY_TIME = "pref_inactivity_time"
        const val PREF_FULL_SCREEN = "pref_full_screen"
        const val PREF_SYSTEM_SOUNDS = "pref_system_sounds"
        const val PREF_MODULE_TSS = "pref_module_tss"
        const val PREF_SYSTEM_NOTIFICATIONS = "pref_system_notifications"
        const val PREF_SENSOR_ONE = "pref_sensor_one"
        const val PREF_SENSOR_TWO = "pref_sensor_two"
        const val PREF_SENSOR_ONE_NAME = "pref_sensor_one_name"
        const val PREF_SENSOR_TWO_NAME = "pref_sensor_two_name"
        const val PREF_MAIL_TO = "pref_mail_to"
        const val PREF_MAIL_FROM = "pref_mail_from"
        const val PREF_MAIL_API_KEY = "pref_mail_api_key"
        const val PREF_MAIL_URL = "pref_mail_url"
        const val PREF_CAMERA_CAPTURE = "pref_module_camera"
        private const val PREF_MOTION_LUMA = "pref_motion_luma"
        private const val PREF_CAMERA_ROTATE = "pref_camera_rotate"
        private const val PREF_CAMERA_ID = "pref_camera_id"
        private const val PREF_CAMERA_FPS = "pref_camera_fps"
        private const val PREF_CAMERA_ORIENTATION = "pref_camera_orientation"
        private const val PREF_CAMERA_WIDTH = "pref_camera_orientation"
        private const val PREF_CAMERA_HEIGHT = "pref_camera_height"
        const val PREF_MODULE_WEATHER = "pref_module_weather"
        const val PREF_MODULE_WEB = "pref_module_web"
        const val PREF_WEB_URL = "pref_web_url"
        const val PREF_FIRST_TIME = "pref_first_time"
        private const val PREF_DEVICE_SCREEN_BRIGHTNESS = "pref_screen_brightness"
        const val PREF_DEVICE_SCREEN_SAVER_BRIGHTNESS = "pref_screen_saver_brightness"
        private const val PREF_SCREEN_DIM_VALUE = "pref_screen_dim_value"

        const val PREF_WEATHER_UNITS = "pref_weather_units_imperial"
        const val PREF_PLATFORM_BAR = "pref_platform_bar"
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
        const val PREF_PLATFORM_CHANGED = "pref_platform_changed"
        const val PREF_PLATFORM_REFRESH = "pref_platform_pull_refresh"
        const val PREF_WRITE_SCREEN_PERMISSIONS = "pref_write_screen_permissions"
        private const val PREF_SENSOR_FREQUENCY = "pref_sensors_frequency"
        private const val PREF_ZOOM_LEVEL = "pref_zoom_level"
        const val PREF_SCREEN_BRIGHTNESS = "pref_use_screen_brightness"
        const val SUN_ABOVE_HORIZON = "above_horizon"
        const val SUN_BELOW_HORIZON = "below_horizon"
        const val WEB_SCREEN_SAVER = "https://thanksmister.com/mqtt_alarm_panel/gif_background.html" //"https://lab.hakim.se/origami/"
        const val PREF_HARDWARE_ACCELERATION = "pref_hardware_acceleration"
        const val PREF_PANIC_ALERT = "pref_panic_alert"
        const val PREF_DARK_THEME = "pref_dark_theme"
        private const val PREF_MOTION_CLEAR  = "pref_motion_clear"
        private const val PREF_MAX_STREAMS  = "pref_max_streams"
        private const val PREF_HTTP_PORT  = "pref_http_port"
    }
}