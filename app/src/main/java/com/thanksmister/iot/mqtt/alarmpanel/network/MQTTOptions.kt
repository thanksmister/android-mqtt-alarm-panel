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

package com.thanksmister.iot.mqtt.alarmpanel.network

import android.content.SharedPreferences
import android.text.TextUtils
import com.thanksmister.iot.mqtt.alarmpanel.utils.DeviceUtils
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils.Companion.COMMAND_ON
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils.Companion.COMMAND_SPEAK
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils.Companion.DEFAULT_COMMAND_TOPIC
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils.Companion.DEFAULT_CONFIG_TOPIC
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils.Companion.DEFAULT_PANEL_COMMAND_TOPIC
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils.Companion.DEFAULT_SENSOR_TOPIC
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils.Companion.DEFAULT_STATE_TOPIC
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils.Companion.DEFAULT_STATUS_TOPIC
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils.Companion.NOTIFICATION_STATE_TOPIC
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils.Companion.PORT
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils.Companion.TOPIC_COMMAND
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

/**
 * For original implementation see https://github.com/androidthings/sensorhub-cloud-iot.
 */
class MQTTOptions @Inject
constructor(private val sharedPreferences: SharedPreferences) {

    val brokerUrl: String
        get() = if (!TextUtils.isEmpty(getBroker())) {
            if (getBroker().contains("http://") || getBroker().contains("https://")) {
                String.format(Locale.getDefault(), HTTP_BROKER_URL_FORMAT, getBroker(), getPort())
            } else if (getTlsConnection()) {
                String.format(Locale.getDefault(), SSL_BROKER_URL_FORMAT, getBroker(), getPort())
            } else {
                String.format(Locale.getDefault(), TCP_BROKER_URL_FORMAT, getBroker(), getPort())
            }
        } else ""

    val isValid: Boolean
        get() = if (getTlsConnection()) {
            !TextUtils.isEmpty(getBroker()) &&
                    !TextUtils.isEmpty(getClientId()) &&
                    getStateTopics().isNotEmpty() &&
                    !TextUtils.isEmpty(getAlarmCommandTopic()) &&
                    !TextUtils.isEmpty(getAlarmStateTopic()) &&
                    !TextUtils.isEmpty(getUsername()) &&
                    !TextUtils.isEmpty(getPassword())
        } else !TextUtils.isEmpty(getBroker()) &&
                !TextUtils.isEmpty(getClientId()) &&
                getStateTopics().isNotEmpty() &&
                !TextUtils.isEmpty(getAlarmCommandTopic())

    fun getBroker(): String {
        return sharedPreferences.getString(PREF_BROKER, "").orEmpty()
    }

    fun getBaseCommand(): String {
        return sharedPreferences.getString(PREF_PANEL_COMMAND_TOPIC, DEFAULT_PANEL_COMMAND_TOPIC).orEmpty()
    }

    private fun getCommandTopic(): String {
        return getBaseCommand() + "/" + TOPIC_COMMAND
    }

    fun getClientId(): String {
        var clientId = sharedPreferences.getString(PREF_CLIENT_ID, "")
        if (clientId.isNullOrEmpty()) {
            clientId = DeviceUtils.uuIdHash
        }
        return clientId
    }

    fun getAlarmCommandTopic(): String {
        return sharedPreferences.getString(PREF_COMMAND_TOPIC, DEFAULT_COMMAND_TOPIC).orEmpty()
    }

    fun getAlarmStateTopic(): String {
        return sharedPreferences.getString(PREF_STATE_TOPIC, DEFAULT_STATE_TOPIC).orEmpty()
    }

    fun getAlarmConfigTopic(): String {
        return sharedPreferences.getString(PREF_CONFIG_TOPIC, DEFAULT_CONFIG_TOPIC).orEmpty()
    }

    fun getAlarmStatusTopic(): String {
        return sharedPreferences.getString(PREF_ALARM_STATUS, DEFAULT_STATUS_TOPIC).orEmpty()
    }

    fun getStateTopics(): Array<String> {
        val topics = ArrayList<String>()
        topics.add(getCommandTopic())
        topics.add(getAlarmStateTopic())
        topics.add(getAlarmConfigTopic())
        topics.add(getAlarmStatusTopic())
        topics.add(sensorOneTopic)
        topics.add(sensorTwoTopic)
        topics.add(sensorThreeTopic)
        topics.add(sensorFourTopic)
        return topics.toArray(arrayOf<String>())
    }

    @Deprecated("We will move to commands")
    fun getNotificationTopic(): String {
        return sharedPreferences.getString(PREF_NOTIFICATION_TOPIC, NOTIFICATION_STATE_TOPIC).orEmpty()
    }

    fun getUsername(): String {
        return sharedPreferences.getString(PREF_USERNAME, "").orEmpty()
    }

    fun getPassword(): String {
        return sharedPreferences.getString(PREF_PASSWORD, "").orEmpty()
    }

    fun getPort(): Int {
        return sharedPreferences.getString(PREF_PORT, PORT.toString())?.toIntOrNull() ?: PORT
    }

    fun getTlsConnection(): Boolean {
        return sharedPreferences.getBoolean(PREF_TLS_CONNECTION, false)
    }

    fun setUsername(value: String) {
        this.sharedPreferences.edit().putString(PREF_USERNAME, value).apply()
        setOptionsUpdated(true)
    }

    fun setClientId(value: String) {
        this.sharedPreferences.edit().putString(PREF_CLIENT_ID, value).apply()
        setOptionsUpdated(true)
    }

    fun setBroker(value: String) {
        this.sharedPreferences.edit().putString(PREF_BROKER, value).apply()
        setOptionsUpdated(true)
    }

    fun setPort(value: String) {
        this.sharedPreferences.edit().putString(PREF_PORT, value).apply()
        setOptionsUpdated(true)
    }

    fun setPassword(value: String) {
        this.sharedPreferences.edit().putString(PREF_PASSWORD, value).apply()
        setOptionsUpdated(true)
    }

    fun setBaseTopic(value: String) {
        sharedPreferences.edit().putString(PREF_PANEL_COMMAND_TOPIC, value).apply()
        setOptionsUpdated(true)
    }

    fun setCommandTopic(value: String) {
        this.sharedPreferences.edit().putString(PREF_COMMAND_TOPIC, value).apply()
        setOptionsUpdated(true)
    }

    fun setAlarmTopic(value: String) {
        this.sharedPreferences.edit().putString(PREF_STATE_TOPIC, value).apply()
        setOptionsUpdated(true)
    }

    fun getRetain(): Boolean {
        return sharedPreferences.getBoolean(PREF_RETAIN, true)
    }

    fun setRetain(value: Boolean) {
        this.sharedPreferences.edit().putBoolean(PREF_RETAIN, value).apply()
        setOptionsUpdated(true)
    }

    var useRemoteConfig: Boolean
        get() = sharedPreferences.getBoolean(PREF_REMOTE_CONFIG, false)
        set(value) {
            this.sharedPreferences.edit().putBoolean(PREF_REMOTE_CONFIG, value).apply()
            setOptionsUpdated(true)
        }

    var useManualConfig: Boolean
        get() = sharedPreferences.getBoolean(PREF_MANUAL_CONFIG, true)
        set(value) {
            this.sharedPreferences.edit().putBoolean(PREF_MANUAL_CONFIG, value).apply()
            setOptionsUpdated(true)
        }

    /**
     * Used for remote config to requires a code entered to arm the alarm.
     */
    var requireCodeForArming: Boolean
        get() = sharedPreferences.getBoolean(PREF_REQUIRE_CODE_TO_ARM, false)
        set(value) = sharedPreferences.edit().putBoolean(PREF_REQUIRE_CODE_TO_ARM, value).apply()

    /**
     * Used for remote config to requires a code entered to disarm the alarm.
     */
    var requireCodeForDisarming: Boolean
        get() = sharedPreferences.getBoolean(PREF_REQUIRE_CODE_TO_DISARM, false)
        set(value) = sharedPreferences.edit().putBoolean(PREF_REQUIRE_CODE_TO_DISARM, value).apply()

    var remoteConfigTopic: String
        get() = sharedPreferences.getString(PREF_CONFIG_TOPIC, DEFAULT_CONFIG_TOPIC).orEmpty()
        set(value) {
            this.sharedPreferences.edit().putString(PREF_CONFIG_TOPIC, value).apply()
            setOptionsUpdated(true)
        }

    var remoteStatusTopic: String
        get() = sharedPreferences.getString(PREF_STATUS_TOPIC, DEFAULT_STATUS_TOPIC).orEmpty()
        set(value) {
            this.sharedPreferences.edit().putString(PREF_STATUS_TOPIC, value).apply()
            setOptionsUpdated(true)
        }

    var remoteArmingHomeTime: Int
        get() = sharedPreferences.getInt(PREF_ARMING_HOME_TIME, 0)
        set(value) = sharedPreferences.edit().putInt(PREF_ARMING_HOME_TIME, value).apply()

    var remoteArmingAwayTime: Int
        get() = sharedPreferences.getInt(PREF_ARMING_AWAY_TIME, 30)
        set(value) = sharedPreferences.edit().putInt(PREF_ARMING_AWAY_TIME, value).apply()

    var remoteArmingNightTime: Int
        get() = sharedPreferences.getInt(PREF_ARMING_NIGHT_TIME, 30)
        set(value) = sharedPreferences.edit().putInt(PREF_ARMING_NIGHT_TIME, value).apply()

    var sensorOneActive: Boolean
        get() = sharedPreferences.getBoolean(PREF_ONE_SENSOR_ACTIVE, false)
        set(value) = sharedPreferences.edit().putBoolean(PREF_ONE_SENSOR_ACTIVE, value).apply()

    var sensorOneName: String
        get() = sharedPreferences.getString(PREF_ONE_SENSOR_NAME, "Sensor one").orEmpty()
        set(value) = sharedPreferences.edit().putString(PREF_ONE_SENSOR_NAME, value).apply()

    var sensorOneTopic: String
        get() = sharedPreferences.getString(PREF_ONE_SENSOR_TOPIC, "${DEFAULT_SENSOR_TOPIC}one").orEmpty()
        set(value) = sharedPreferences.edit().putString(PREF_ONE_SENSOR_TOPIC, value).apply()

    var sensorOneState: String
        get() = sharedPreferences.getString(PREF_ONE_SENSOR_STATE, COMMAND_ON).orEmpty()
        set(value) = sharedPreferences.edit().putString(PREF_ONE_SENSOR_STATE, value).apply()

    var sensorTwoActive: Boolean
        get() = sharedPreferences.getBoolean(PREF_TWO_SENSOR_ACTIVE, false)
        set(value) = sharedPreferences.edit().putBoolean(PREF_TWO_SENSOR_ACTIVE, value).apply()

    var sensorTwoName: String
        get() = sharedPreferences.getString(PREF_TWO_SENSOR_NAME, "Sensor two").orEmpty()
        set(value) = sharedPreferences.edit().putString(PREF_TWO_SENSOR_NAME, value).apply()

    var sensorTwoTopic: String
        get() = sharedPreferences.getString(PREF_TWO_SENSOR_TOPIC, "${DEFAULT_SENSOR_TOPIC}two").orEmpty()
        set(value) = sharedPreferences.edit().putString(PREF_TWO_SENSOR_TOPIC, value).apply()

    var sensorTwoState: String
        get() = sharedPreferences.getString(PREF_TWO_SENSOR_STATE, COMMAND_ON).orEmpty()
        set(value) = sharedPreferences.edit().putString(PREF_TWO_SENSOR_STATE, value).apply()

    var sensorThreeActive: Boolean
        get() = sharedPreferences.getBoolean(PREF_THREE_SENSOR_ACTIVE, false)
        set(value) = sharedPreferences.edit().putBoolean(PREF_THREE_SENSOR_ACTIVE, value).apply()

    var sensorThreeName: String
        get() = sharedPreferences.getString(PREF_THREE_SENSOR_NAME, "Sensor three").orEmpty()
        set(value) = sharedPreferences.edit().putString(PREF_THREE_SENSOR_NAME, value).apply()

    var sensorThreeTopic: String
        get() = sharedPreferences.getString(PREF_THREE_SENSOR_TOPIC, "${DEFAULT_SENSOR_TOPIC}three").orEmpty()
        set(value) = sharedPreferences.edit().putString(PREF_THREE_SENSOR_TOPIC, value).apply()

    var sensorThreeState: String
        get() = sharedPreferences.getString(PREF_THREE_SENSOR_STATE, COMMAND_ON).orEmpty()
        set(value) = sharedPreferences.edit().putString(PREF_THREE_SENSOR_STATE, value).apply()

    var sensorFourActive: Boolean
        get() = sharedPreferences.getBoolean(PREF_FOUR_SENSOR_ACTIVE, false)
        set(value) = sharedPreferences.edit().putBoolean(PREF_FOUR_SENSOR_ACTIVE, value).apply()

    var sensorFourName: String
        get() = sharedPreferences.getString(PREF_FOUR_SENSOR_NAME, "Sensor four").orEmpty()
        set(value) = sharedPreferences.edit().putString(PREF_FOUR_SENSOR_NAME, value).apply()

    var sensorFourTopic: String
        get() = sharedPreferences.getString(PREF_FOUR_SENSOR_TOPIC, "${DEFAULT_SENSOR_TOPIC}four").orEmpty()
        set(value) = sharedPreferences.edit().putString(PREF_FOUR_SENSOR_TOPIC, value).apply()

    var sensorFourState: String
        get() = sharedPreferences.getString(PREF_FOUR_SENSOR_STATE, COMMAND_ON).orEmpty()
        set(value) = sharedPreferences.edit().putString(PREF_FOUR_SENSOR_STATE, value).apply()

    fun setTlsConnection(value: Boolean) {
        this.sharedPreferences.edit().putBoolean(PREF_TLS_CONNECTION, value).apply()
        setOptionsUpdated(true)
    }

    fun setOptionsUpdated(value: Boolean) {
        this.sharedPreferences.edit().putBoolean(MQTT_OPTIONS_UPDATED, value).apply()
    }

    fun hasUpdates(): Boolean {
        return sharedPreferences.getBoolean(MQTT_OPTIONS_UPDATED, false)
    }

    companion object {
        const val SSL_BROKER_URL_FORMAT = "ssl://%s:%d"
        const val TCP_BROKER_URL_FORMAT = "tcp://%s:%d"
        const val HTTP_BROKER_URL_FORMAT = "%s:%d"
        const val PREF_STATE_TOPIC = "pref_alarm_topic"
        const val PREF_STATUS_TOPIC = "pref_status_topic"
        const val PREF_PANEL_COMMAND_TOPIC = "pref_base_topic"
        const val PREF_NOTIFICATION_TOPIC = "pref_notification_topic"
        const val PREF_USERNAME = "pref_username"
        const val PREF_COMMAND_TOPIC = "pref_command_topic"
        const val PREF_TLS_CONNECTION = "pref_tls_connection"
        const val PREF_PASSWORD = "pref_password"
        const val PREF_PORT = "pref_port"
        const val PREF_CLIENT_ID = "pref_client_id"
        const val PREF_BROKER = "pref_broker"
        const val PREF_RETAIN = "pref_retain"
        const val PREF_REMOTE_CONFIG = "pref_remote_config"
        const val PREF_MANUAL_CONFIG = "pref_manual_config"
        const val MQTT_OPTIONS_UPDATED = "pref_mqtt_options_updated"
        const val PREF_CONFIG_TOPIC = "pref_config_topic"
        const val PREF_ALARM_STATUS = "pref_status_topic"

        private const val PREF_REQUIRE_CODE_TO_ARM = "pref_require_arm_code"
        private const val PREF_REQUIRE_CODE_TO_DISARM = "pref_require_disarm_code"
        private const val PREF_ARMING_HOME_TIME = "pref_arming_home_time"
        private const val PREF_ARMING_AWAY_TIME = "pref_arming_away_time"
        private const val PREF_ARMING_NIGHT_TIME = "pref_arming_night_time"

        private const val PREF_ONE_SENSOR_ACTIVE = "pref_sensor_active_one"
        private const val PREF_ONE_SENSOR_NAME = "pref_sensor_name_one"
        private const val PREF_ONE_SENSOR_TOPIC = "pref_sensor_topic_one"
        private const val PREF_ONE_SENSOR_STATE = "pref_sensor_state_one"

        private const val PREF_TWO_SENSOR_ACTIVE = "pref_sensor_active_two"
        private const val PREF_TWO_SENSOR_NAME = "pref_sensor_name_two"
        private const val PREF_TWO_SENSOR_TOPIC = "pref_sensor_topic_two"
        private const val PREF_TWO_SENSOR_STATE = "pref_sensor_state_two"

        private const val PREF_THREE_SENSOR_ACTIVE = "pref_sensor_active_three"
        private const val PREF_THREE_SENSOR_NAME = "pref_sensor_name_three"
        private const val PREF_THREE_SENSOR_TOPIC = "pref_sensor_topic_three"
        private const val PREF_THREE_SENSOR_STATE = "pref_sensor_state_three"

        private const val PREF_FOUR_SENSOR_ACTIVE = "pref_sensor_active_four"
        private const val PREF_FOUR_SENSOR_NAME = "pref_sensor_name_four"
        private const val PREF_FOUR_SENSOR_TOPIC = "pref_sensor_topic_four"
        private const val PREF_FOUR_SENSOR_STATE = "pref_sensor_state_four"
    }
}