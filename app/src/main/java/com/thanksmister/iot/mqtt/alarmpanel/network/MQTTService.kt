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

import android.R.id.message
import android.content.Context
import com.thanksmister.iot.mqtt.alarmpanel.R
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils
import com.thanksmister.iot.mqtt.alarmpanel.utils.StringUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import org.json.JSONObject
import timber.log.Timber
import java.io.IOException
import java.security.GeneralSecurityException
import java.security.NoSuchAlgorithmException
import java.security.spec.InvalidKeySpecException
import java.util.concurrent.atomic.AtomicBoolean

class MQTTService(private var context: Context,
                  private var options: MQTTOptions,
                  private var listener: MqttManagerListener?) :
        MQTTServiceInterface {

    private var mqttClient: MqttAndroidClient? = null
    private var mqttOptions: MQTTOptions? = null
    private val mReady = AtomicBoolean(false)
    private val disposable = CompositeDisposable()

    init {
        initialize(options)
    }

    override fun reconfigure(context: Context,
                             newOptions: MQTTOptions,
                             listener: MqttManagerListener) {
        try {
            close()
        } catch (e: MqttException) {
            // empty
        }
        this.listener = listener
        this.context = context
        initialize(newOptions)
    }

    interface MqttManagerListener {
        fun subscriptionMessage(id: String, topic: String, payload: String)
        fun handleMqttException(errorMessage: String)
        fun handleMqttDisconnected()
        fun handleMqttConnected()
    }

    @Throws(MqttException::class)
    override fun close() {
        Timber.d("close")
        mqttClient?.let {
            it.setCallback(null)
            if (it.isConnected) {
                it.disconnect(0)
            }
            mqttClient = null
            listener = null
            mqttOptions = null
        }
        mReady.set(false)
        disposable.dispose()
    }

    override fun publishAlarm(command: String, code: Int) {
        try {
            if (mReady.get()) {
                mqttClient?.let {
                    if (!it.isConnected) {
                        // if for some reason the mqtt client has disconnected, we should try to connect
                        // it again.
                        try {
                            initializeMqttClient()
                        } catch (e: MqttException) {
                            if (listener != null) {
                                listener!!.handleMqttException("Could not initialize MQTT: " + e.message)
                            }
                        } catch (e: IOException) {
                            if (listener != null) {
                                listener!!.handleMqttException("Could not initialize MQTT: " + e.message)
                            }
                        } catch (e: GeneralSecurityException) {
                            if (listener != null) {
                                listener!!.handleMqttException("Could not initialize MQTT: " + e.message)
                            }
                        }
                    }
                }
                mqttOptions?.let {
                    val mqttMessage = MqttMessage()
                    var payloadString = command
                    if(it.useRemoteCode && code != 0) {
                        val payloadJson = JSONObject()
                        payloadJson.put(MqttUtils.COMMAND, command)
                        if (code > 0) {
                            payloadJson.put(MqttUtils.CODE, code.toString())
                        } else if (command == MqttUtils.COMMAND_PANIC && code < 0) {
                            payloadJson.put(MqttUtils.CODE, "")
                        }
                        payloadString = payloadJson.toString()
                    }
                    mqttMessage.payload = payloadString.toByteArray()
                    mqttMessage.isRetained = it.getRetain()
                    sendMessage(it.getAlarmCommandTopic(), mqttMessage)
                }
            }
        } catch (e: MqttException) {
            listener?.handleMqttException("Exception while subscribing: " + e.message)
        }
    }

    /**
     * This is used to publish messages and data from that are unrelated to the alarm commands.
     * Use to publish sensor data, panic alarm, user presence or other data. Uses the same
     * value for the base command name from user settings.
     */
    override fun publishCommand(command: String, payload: String) {
        try {
            if (mReady.get()) {
                mqttClient?.let {
                    if (!it.isConnected) {
                        try {
                            initializeMqttClient()
                        } catch (e: MqttException) {
                            listener?.handleMqttException("Could not initialize MQTT: " + e.message)
                        } catch (e: IOException) {
                            listener?.handleMqttException("Could not initialize MQTT: " + e.message)
                        } catch (e: GeneralSecurityException) {
                            listener?.handleMqttException("Could not initialize MQTT: " + e.message)
                        }
                    }
                }

                val mqttMessage = MqttMessage()
                mqttMessage.payload = payload.toByteArray()
                mqttOptions?.let {
                    Timber.d("Publishing: $payload")
                    Timber.d("Base Topic: ${it.getBaseCommand()}")
                    Timber.d("Command Topic: $command")
                    mqttMessage.isRetained = it.getRetain()
                    sendMessage(it.getBaseCommand() + "/" + command, mqttMessage)
                }
            }
        } catch (e: MqttException) {
            listener?.handleMqttException("Exception while subscribing: " + e.message)
        }
    }

    /**
     * Initialize a Cloud IoT Endpoint given a set of configuration options.
     * @param options Cloud IoT configuration options.
     */
    private fun initialize(options: MQTTOptions) {
        Timber.d("initialize")
        try {
            mqttOptions = options
            mqttOptions?.let {
                if (it.isValid) {
                    initializeMqttClient()
                } else {
                    listener?.handleMqttDisconnected()
                }
            }
        } catch (e: MqttException) {
            listener?.handleMqttException(context.getString(R.string.error_mqtt_connection))
        } catch (e: IOException) {
            listener?.handleMqttException(context.getString(R.string.error_mqtt_connection))
        } catch (e: GeneralSecurityException) {
            listener?.handleMqttException(context.getString(R.string.error_mqtt_connection))
        }
    }

    @Throws(MqttException::class, IOException::class, NoSuchAlgorithmException::class, InvalidKeySpecException::class)
    private fun initializeMqttClient() {
        Timber.d("initializeMqttClient")
        try {
            mqttOptions?.let { mqttOptions ->
                mqttClient = MqttAndroidClient(context, mqttOptions.brokerUrl, mqttOptions.getClientId(), MemoryPersistence())
                mqttClient?.setCallback(object : MqttCallbackExtended {
                    override fun connectComplete(reconnect: Boolean, serverURI: String?) {
                        Timber.d("connect to broker completed, reconnected: $reconnect")
                        subscribeToTopics() // subscribe to our topics
                    }
                    override fun connectionLost(cause: Throwable?) {}
                    override fun messageArrived(topic: String?, message: MqttMessage?) {}
                    override fun deliveryComplete(token: IMqttDeliveryToken?) {}
                })

                val options = MqttConnectOptions()
                options.isAutomaticReconnect = true
                options.isCleanSession = false
                if (mqttOptions.getUsername().isNotEmpty() && mqttOptions.getPassword().isNotEmpty()) {
                    options.userName = mqttOptions.getUsername()
                    options.password = mqttOptions.getPassword().toCharArray()
                }

                val isConnected = mqttClient?.isConnected ?: false
                if (isConnected) {
                    mReady.set(true)
                    return
                }

                try {
                    mqttClient?.connect(options, null, object : IMqttActionListener {
                        override fun onSuccess(asyncActionToken: IMqttToken) {
                            val disconnectedBufferOptions = DisconnectedBufferOptions()
                            disconnectedBufferOptions.isBufferEnabled = true
                            disconnectedBufferOptions.bufferSize = 100
                            disconnectedBufferOptions.isPersistBuffer = false
                            disconnectedBufferOptions.isDeleteOldestMessages = false
                            mqttClient?.let {
                                try {
                                    it.setBufferOpts(disconnectedBufferOptions)
                                } catch (e: NullPointerException) {
                                    Timber.e(e.message)
                                }
                            }
                            mReady.set(true)
                            listener?.handleMqttConnected()
                        }

                        override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                            if (exception is MqttException) {
                                if (exception.reasonCode == 32100 || exception.reasonCode == 32110) {
                                    listener?.handleMqttConnected()
                                    mReady.set(true)
                                    return // we have a connection established or is establishing
                                }
                            }
                            mqttOptions.let {
                                Timber.e("Failed to connect to: " + it.brokerUrl + " exception: " + exception)
                                listener?.handleMqttException(context.getString(R.string.error_mqtt_subscription))
                            }
                        }
                    })
                } catch (e: NullPointerException) {
                    Timber.e(e, e.message)
                } catch (e: MqttException) {
                    listener?.handleMqttException("" + e.message)
                }
            }

        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        } catch (e: NullPointerException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
            listener?.handleMqttException("" + e.message)
        }
    }

    @Throws(MqttException::class)
    private fun sendMessage(mqttTopic: String?, mqttMessage: MqttMessage) {
        Timber.d("sendMessage")
        mqttClient?.let {
            if (mReady.get() && it.isConnected) {
                try {
                    it.publish(mqttTopic, mqttMessage)
                    Timber.d("Command Topic: $mqttTopic Payload: $message")
                } catch (e: NullPointerException) {
                    Timber.e(e.message)
                } catch (e: MqttException) {
                    Timber.e("Error Sending Command: " + e.message)
                    e.printStackTrace()
                    listener?.handleMqttException(context.getString(R.string.error_mqtt_subscription))
                }
            }
        }
    }

    // TODO should we add a delay to retry if mReady not ready?
    private fun subscribeToTopics() {
        mqttOptions?.let { options ->
            disposable.add(
                    options.getStateTopicsFlowable()
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe { topicFilters ->
                                topicFilters?.let {
                                    Timber.d("Subscribe to Topics: " + StringUtils.convertArrayToString(it))
                                    mqttClient?.let { client ->
                                        if (mReady.get()) {
                                            try {
                                                client.subscribe(topicFilters, MqttUtils.getQos(topicFilters.size), MqttUtils.getMqttMessageListeners(it.size, listener))
                                            } catch (e: NullPointerException) {
                                                listener?.handleMqttException("Exception while subscribing: " + e.message)
                                            } catch (e: MqttException) {
                                                listener?.handleMqttException("Exception while subscribing: " + e.message)
                                            }
                                        }
                                    }
                                }
                            })
        }
    }

    companion object {
         // Use mqttQos=1 (at least once delivery), mqttQos=0 (at most once delivery) also supported.
         private val MQTT_QOS = 0
    }
}