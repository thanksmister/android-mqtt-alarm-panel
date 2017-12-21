package com.thanksmister.iot.mqtt.alarmpanel.network

import android.text.TextUtils
import com.thanksmister.iot.mqtt.alarmpanel.utils.AlarmUtils
import com.thanksmister.iot.mqtt.alarmpanel.utils.AlarmUtils.Companion.ALARM_COMMAND_TOPIC
import com.thanksmister.iot.mqtt.alarmpanel.utils.AlarmUtils.Companion.ALARM_STATE_TOPIC
import com.thanksmister.iot.mqtt.alarmpanel.utils.ComponentUtils.NOTIFICATION_STATE_TOPIC
import com.thanksmister.iot.mqtt.alarmpanel.utils.DeviceUtils
import dpreference.DPreference
import timber.log.Timber
import java.util.*
import kotlin.collections.ArrayList

/**
 * For original implementation see https://github.com/androidthings/sensorhub-cloud-iot.
 */
class MQTTOptions private constructor(private val sharedPreferences: DPreference) {

    /**
     * Client id.
     */
    private var clientId: String? = null

    /**
     * Broker address.
     */
    private var broker: String? = null

    /**
     * Port number.
     */
    private var port: Int = 0

    /**
     * MQTT State Topic.
     */
    var stateTopics: Array<String>? = null
        private set

    /**
     * MQTT Command Topic.
     */
    private var commandTopic: String? = null

    /**
     * Username.
     */
    private var username: String? = null

    /**
     * Password.
     */
    private var password: String? = null

    /**
     * TSL connection.
     */
    private var tlsConnection: Boolean = false

    val brokerUrl: String
        get() = if (!TextUtils.isEmpty(broker)) {
            if (broker!!.contains("http://") || broker!!.contains("https://")) {
                String.format(Locale.getDefault(), HTTP_BROKER_URL_FORMAT, broker, port)
            } else if (tlsConnection) {
                String.format(Locale.getDefault(), SSL_BROKER_URL_FORMAT, broker, port)
            } else {
                String.format(Locale.getDefault(), TCP_BROKER_URL_FORMAT, broker, port)
            }
        } else ""

    val isValid: Boolean
        get() = if (tlsConnection) {
            !TextUtils.isEmpty(broker) &&
                    !TextUtils.isEmpty(clientId) &&
                    stateTopics!!.isNotEmpty() &&
                    !TextUtils.isEmpty(commandTopic) &&
                    !TextUtils.isEmpty(username) &&
                    !TextUtils.isEmpty(password)
        } else !TextUtils.isEmpty(broker) &&
                !TextUtils.isEmpty(clientId) &&
                stateTopics!!.isNotEmpty() &&
                !TextUtils.isEmpty(commandTopic)

    fun getBroker(): String? {
        return broker
    }

    fun getClientId(): String? {
        return clientId
    }

    fun getCommandTopic(): String? {
        return commandTopic
    }

    fun getUsername(): String? {
        return username
    }

    fun getPassword(): String? {
        return password
    }

    fun getPort(): Int {
        return port
    }

    fun getTlsConnection(): Boolean {
        return tlsConnection
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

    fun setCommandTopic(value: String) {
        this.sharedPreferences.setPrefString(PREF_COMMAND_TOPIC, value)
        setOptionsUpdated(true)
    }

    fun setAlarmTopic(value: String) {
        this.sharedPreferences.setPrefString(PREF_ALARM_TOPIC, value)
        setOptionsUpdated(true)
    }

    fun getAlarmTopic(): String {
        return this.sharedPreferences.getPrefString(PREF_ALARM_TOPIC, ALARM_STATE_TOPIC)
    }

    fun setNotificationTopic(value: String) {
        this.sharedPreferences.setPrefString(PREF_NOTIFICATION_TOPIC, value)
        setOptionsUpdated(true)
    }

    fun getNotificationTopic(): String {
        return this.sharedPreferences.getPrefString(PREF_NOTIFICATION_TOPIC, NOTIFICATION_STATE_TOPIC)
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

        private val SSL_BROKER_URL_FORMAT = "ssl://%s:%d"
        private val TCP_BROKER_URL_FORMAT = "tcp://%s:%d"
        private val HTTP_BROKER_URL_FORMAT = "%s:%d"

        @JvmField val PREF_ALARM_TOPIC = "pref_alarm_topic"
        @JvmField val PREF_NOTIFICATION_TOPIC = "pref_notification_topic"
        @JvmField val PREF_USERNAME = "pref_username"
        @JvmField val PREF_COMMAND_TOPIC = "pref_command_topic"
        @JvmField val PREF_TLS_CONNECTION = "pref_tls_connection"
        @JvmField val PREF_PASSWORD = "pref_password"
        @JvmField val PREF_PORT = "pref_port"
        @JvmField val PREF_CLIENT_ID = "pref_client_id"
        @JvmField val PREF_BROKER = "pref_broker"
        private val MQTT_OPTIONS_UPDATED = "pref_mqtt_options_updated"

        /**
         * Construct a MqttOptions object from Configuration.
         */
        fun from(sharedPreferences: DPreference): MQTTOptions {
            try {
                val options = MQTTOptions(sharedPreferences)

                var clientId = sharedPreferences.getPrefString(PREF_CLIENT_ID, null)
                if (TextUtils.isEmpty(clientId)) {
                    clientId = DeviceUtils.getUuIdHash()
                }
                options.clientId = clientId
                options.broker = sharedPreferences.getPrefString(PREF_BROKER, "")
                options.port = sharedPreferences.getPrefInt(PREF_PORT, AlarmUtils.PORT)
                options.username = sharedPreferences.getPrefString(PREF_USERNAME, null)
                options.password = sharedPreferences.getPrefString(PREF_PASSWORD, null)

                val topics = ArrayList<String>()
                topics.add(sharedPreferences.getPrefString(PREF_ALARM_TOPIC, ALARM_STATE_TOPIC))
                topics.add(sharedPreferences.getPrefString(PREF_NOTIFICATION_TOPIC, NOTIFICATION_STATE_TOPIC))
                options.stateTopics = topics.toArray(arrayOf<String>())

                options.commandTopic = sharedPreferences.getPrefString(PREF_COMMAND_TOPIC, ALARM_COMMAND_TOPIC)
                options.tlsConnection = sharedPreferences.getPrefBoolean(PREF_TLS_CONNECTION, false)
                return options
            } catch (e: Exception) {
                throw IllegalArgumentException("While processing configuration options", e)
            }
        }
    }
}