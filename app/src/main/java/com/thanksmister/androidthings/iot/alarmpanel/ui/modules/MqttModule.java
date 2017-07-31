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

package com.thanksmister.androidthings.iot.alarmpanel.ui.modules;

import android.content.Context;

import com.thanksmister.androidthings.iot.alarmpanel.ui.Configuration;
import com.thanksmister.androidthings.iot.alarmpanel.utils.MQTTUtils;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import timber.log.Timber;

/**
 * Module to subscribe to the MQTT broker, publish and receive messages
 */
public class MqttModule {
    
    private Context context;
    private Configuration configuration;
    private MqttAndroidClient mqttAndroidClient;
    private MqttModuleListener mqttModuleListener;

    public interface MqttModuleListener {
        void subscriptionMessage(String topic, String message);
        void subscriptionError(String message);
        void publishError(String message);
    }
    
    public MqttModule (Context context, Configuration configuration, MqttModuleListener mqttModuleListener) {
        this.context = context;
        this.configuration = configuration;
        this.mqttModuleListener = mqttModuleListener;
    }
    
    public void makeMqttConnection() {
        
        // TODO test secure connection
        final boolean tlsConnection = false;
        final String serverUri;
        if(tlsConnection) {
            serverUri = "ssl://" + configuration.getBroker() + ":" + configuration.getPort();
        } else {
            serverUri = "tcp://" + configuration.getBroker() + ":1883";
        }

        Timber.d("Server Uri: " + serverUri);
        final String clientId = configuration.getClientId();
        final String topic = configuration.getTopic();

        MqttConnectOptions mqttConnectOptions = MQTTUtils.getMqttConnectOptions(configuration.getUserName(), configuration.getPassword());
        mqttAndroidClient = MQTTUtils.getMqttAndroidClient(context, serverUri, clientId, topic, new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                if (reconnect) {
                    Timber.d("Reconnected to : " + serverURI);
                    // Because Clean Session is true, we need to re-subscribe
                    subscribeToTopic(topic);
                } else {
                    Timber.d("Connected to: " + serverURI);
                }
            }

            @Override
            public void connectionLost(Throwable cause) {
                Timber.d("The Connection was lost.");
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                Timber.d("Incoming message: " + new String(message.getPayload()));
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });

        try {
            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                    disconnectedBufferOptions.setBufferEnabled(true);
                    disconnectedBufferOptions.setBufferSize(100);
                    disconnectedBufferOptions.setPersistBuffer(false);
                    disconnectedBufferOptions.setDeleteOldestMessages(false);
                    mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);
                    subscribeToTopic(topic);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Timber.e("Failed to connect to: " + serverUri + " exception: " + exception.getMessage());
                }
            });

        } catch (MqttException ex){
            ex.printStackTrace();
        }
    }

    public void publishMessage(String publishTopic, String publishMessage) {
        try {
            MqttMessage message = new MqttMessage();
            message.setPayload(publishMessage.getBytes());
            mqttAndroidClient.publish(publishTopic, message);
            Timber.d("Message Published: " + publishTopic);
            if(!mqttAndroidClient.isConnected()){
                Timber.d(mqttAndroidClient.getBufferedMessageCount() + " messages in buffer.");
            }
        } catch (MqttException e) {
            Timber.e("Error Publishing: " + e.getMessage());
            e.printStackTrace();
            mqttModuleListener.publishError(e.getMessage());
        }
    }
    
    public void subscribeToTopic(final String topic) {
        try {
            // TODO do we need this one?
            mqttAndroidClient.subscribe(topic, 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Timber.d("Subscribed!");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Timber.e("Failed to subscribe");
                }
            });

            // THIS DOES NOT WORK!
            mqttAndroidClient.subscribe(topic, 0, new IMqttMessageListener() {
                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    // message Arrived!
                    Timber.i("Message: " + topic + " : " + new String(message.getPayload()));
                    mqttModuleListener.subscriptionMessage(topic, new String(message.getPayload()));
                }
            });

        } catch (MqttException ex){
            Timber.e("Exception whilst subscribing");
            ex.printStackTrace();
            mqttModuleListener.subscriptionError(ex.getMessage());
            //hideProgressDialog();
        }
    }
}