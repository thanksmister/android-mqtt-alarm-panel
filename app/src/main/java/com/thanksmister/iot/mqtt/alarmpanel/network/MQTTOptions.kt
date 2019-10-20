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

import android.content.Context
import android.text.TextUtils
import com.thanksmister.iot.mqtt.alarmpanel.utils.AlarmUtils
import com.thanksmister.iot.mqtt.alarmpanel.utils.AlarmUtils.Companion.ALARM_COMMAND_TOPIC
import com.thanksmister.iot.mqtt.alarmpanel.utils.AlarmUtils.Companion.ALARM_STATE_TOPIC
import com.thanksmister.iot.mqtt.alarmpanel.utils.ComponentUtils.Companion.BASE_TOPIC
import com.thanksmister.iot.mqtt.alarmpanel.utils.ComponentUtils.Companion.NOTIFICATION_STATE_TOPIC
import com.thanksmister.iot.mqtt.alarmpanel.utils.ComponentUtils.Companion.TOPIC_COMMAND
import com.thanksmister.iot.mqtt.alarmpanel.utils.DeviceUtils
import dpreference.DPreference
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

/**
 * For original implementation see https://github.com/androidthings/sensorhub-cloud-iot.
 */
class MQTTOptions @Inject
constructor(private val context: Context, private val sharedPreferences: DPreference) {

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
        return sharedPreferences.getPrefString(PREF_BROKER, "")
    }

    fun getBaseTopic(): String {
        return sharedPreferences.getPrefString(PREF_BASE_TOPIC, BASE_TOPIC)
    }

    private fun getCommandTopic(): String {
        return getBaseTopic() + "/" + TOPIC_COMMAND
    }

    fun getClientId(): String {
        var clientId = sharedPreferences.getPrefString(PREF_CLIENT_ID, "")
        if (TextUtils.isEmpty(clientId)) {
            clientId = DeviceUtils.uuIdHash
        }
        return clientId
    }

    fun getAlarmCommandTopic(): String {
        return sharedPreferences.getPrefString(PREF_COMMAND_TOPIC, ALARM_COMMAND_TOPIC)
    }

    fun getAlarmStateTopic(): String {
        return sharedPreferences.getPrefString(PREF_STATE_TOPIC, ALARM_STATE_TOPIC)
    }

    // TODO we need to add all the topics from sensor database
    fun getStateTopics(): Array<String> {
        val topics = ArrayList<String>()
        topics.add(getCommandTopic())
        topics.add(getAlarmStateTopic())
        return topics.toArray(arrayOf<String>())
    }

    @Deprecated ("We will move to commands")
    fun getNotificationTopic(): String {
        return sharedPreferences.getPrefString(PREF_NOTIFICATION_TOPIC, NOTIFICATION_STATE_TOPIC)
    }

    fun getUsername(): String {
        return sharedPreferences.getPrefString(PREF_USERNAME, "")
    }

    fun getPassword(): String {
        return sharedPreferences.getPrefString(PREF_PASSWORD, "")
    }

    fun getPort(): Int {
        return sharedPreferences.getPrefInt(PREF_PORT, AlarmUtils.PORT)
    }

    fun getTlsConnection(): Boolean {
        return sharedPreferences.getPrefBoolean(PREF_TLS_CONNECTION, false)
    }

    fun setUsername(value: String) {
        this.sharedPreferences.setPrefString(PREF_USERNAME, value)
        setOptionsUpdated(true)
    }

    fun setClientId(value: String) {
        this.sharedPreferences.setPrefString(PREF_CLIENT_ID, value)
        setOptionsUpdated(true)
    }

    fun setBroker(value: String) {
        this.sharedPreferences.setPrefString(PREF_BROKER, value)
        setOptionsUpdated(true)
    }

    fun setPort(value: Int) {
        this.sharedPreferences.setPrefInt(PREF_PORT, value)
        setOptionsUpdated(true)
    }

    fun setPassword(value: String) {
        this.sharedPreferences.setPrefString(PREF_PASSWORD, value)
        setOptionsUpdated(true)
    }

    fun setBaseTopic(value: String) {
        sharedPreferences.setPrefString(PREF_BASE_TOPIC, value)
        setOptionsUpdated(true)
    }

    fun setCommandTopic(value: String) {
        this.sharedPreferences.setPrefString(PREF_COMMAND_TOPIC, value)
        setOptionsUpdated(true)
    }

    fun setAlarmTopic(value: String) {
        this.sharedPreferences.setPrefString(PREF_STATE_TOPIC, value)
        setOptionsUpdated(true)
    }

    fun getRetain(): Boolean {
        return sharedPreferences.getPrefBoolean(PREF_RETAIN, true)
    }

    fun setRetain(value: Boolean) {
        this.sharedPreferences.setPrefBoolean(PREF_RETAIN, value)
        setOptionsUpdated(true)
    }

    @Deprecated ("We will move to commands")
    fun setNotificationTopic(value: String) {
        this.sharedPreferences.setPrefString(PREF_NOTIFICATION_TOPIC, value)
        setOptionsUpdated(true)
    }

    fun setSensorTopic(value: String) {
        this.sharedPreferences.setPrefString(PREF_SENSOR_TOPIC, value)
        setOptionsUpdated(true)
    }

    fun setTlsConnection(value: Boolean) {
        this.sharedPreferences.setPrefBoolean(PREF_TLS_CONNECTION, value)
        setOptionsUpdated(true)
    }

    fun setOptionsUpdated(value: Boolean) {
        this.sharedPreferences.setPrefBoolean(MQTT_OPTIONS_UPDATED, value)
    }

    fun hasUpdates(): Boolean {
        val updates = sharedPreferences.getPrefBoolean(MQTT_OPTIONS_UPDATED, false)
        Timber.d("Updates: " + updates)
        return updates
    }

    companion object {
        const val SSL_BROKER_URL_FORMAT = "ssl://%s:%d"
        const val TCP_BROKER_URL_FORMAT = "tcp://%s:%d"
        const val HTTP_BROKER_URL_FORMAT = "%s:%d"
        const val PREF_STATE_TOPIC = "pref_alarm_topic"
        const val PREF_BASE_TOPIC = "pref_base_topic"
        const val PREF_NOTIFICATION_TOPIC = "pref_notification_topic"
        const val PREF_SENSOR_TOPIC = "pref_sensor_topic"
        const val PREF_USERNAME = "pref_username"
        const val PREF_COMMAND_TOPIC = "pref_command_topic"
        const val PREF_TLS_CONNECTION = "pref_tls_connection"
        const val PREF_PASSWORD = "pref_password"
        const val PREF_PORT = "pref_port"
        const val PREF_CLIENT_ID = "pref_client_id"
        const val PREF_BROKER = "pref_broker"
        const val PREF_RETAIN = "pref_retain"
        const val MQTT_OPTIONS_UPDATED = "pref_mqtt_options_updated"
    }
}