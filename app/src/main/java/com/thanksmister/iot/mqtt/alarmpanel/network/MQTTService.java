package com.thanksmister.iot.mqtt.alarmpanel.network;


import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils;

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

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.concurrent.atomic.AtomicBoolean;

import timber.log.Timber;

import static android.R.id.message;

public class MQTTService implements MQTTServiceInterface {
    
    // Indicate if this message should be a MQTT 'retained' message.
    private static final boolean SHOULD_RETAIN = false;

    // Use mqttQos=1 (at least once delivery), mqttQos=0 (at most once delivery) also supported.
    private static final int MQTT_QOS = 0;

    private Context context;
    private MqttAndroidClient mqttClient;
    private MQTTOptions mqttOptions;
    private AtomicBoolean mReady = new AtomicBoolean(false);
    private MqttManagerListener listener;

    public MQTTService(@NonNull Context context, 
                       @NonNull MQTTOptions options, 
                       MqttManagerListener listener) {
        this.listener = listener;
        this.context = context;
        initialize(options);
    }
    
    public void reconfigure(@NonNull Context context, 
                            @NonNull MQTTOptions newOptions, 
                            MqttManagerListener listener) {
        this.listener = listener;
        this.context = context;
        if (newOptions.equals(mqttOptions)) {
            return;
        }
        try {
            close();
        } catch (MqttException e) {
            // empty
        }
        initialize(newOptions);
    }
    
    public interface MqttManagerListener {
        void subscriptionMessage(String topic, String payload, String id);
        void handleMqttException(String errorMessage);
        void handleMqttDisconnected();
    }
    
    public boolean isReady() {
        return mReady.get();
    }
    
    public void close() throws MqttException {
        listener = null;
        mqttOptions = null;
        if (mqttClient != null) {
            // TODO IllegalArgumentException: Invalid ClientHandle and no dialog showing sound stuck
            if (mqttClient.isConnected()) {
                mqttClient.disconnect();
            }
            mqttClient = null;
        }
        mReady.set(false);
    }
    
    public void publish(String payload) {
        Timber.d("publish: " + payload);
        try {
            if (isReady()) {
                if (mqttClient != null && !mqttClient.isConnected()) {
                    // if for some reason the mqtt client has disconnected, we should try to connect
                    // it again.
                    try {
                        initializeMqttClient();
                    } catch (MqttException | IOException | GeneralSecurityException e) {
                        if(listener != null) {
                            listener.handleMqttException("Could not initialize MQTT: " + e.getMessage());
                        }
                    }
                }
                Timber.d("Publishing: " + payload);
                MqttMessage mqttMessage = new MqttMessage();
                mqttMessage.setPayload(payload.getBytes());
                sendMessage(mqttOptions.getCommandTopic(), mqttMessage);
            }
        } catch (MqttException e) {
            if(listener != null) {
                listener.handleMqttException("Exception while subscribing: " + e.getMessage());
            }
        }
    }
    
    /**
     * Initialize a Cloud IoT Endpoint given a set of configuration options.
     * @param options Cloud IoT configuration options.
     */
    private void initialize(@NonNull MQTTOptions options) {
        Timber.d("initialize");
        try {
            mqttOptions = options;
            Timber.i("Service Configuration:");
            Timber.i("Client ID: " + mqttOptions.getClientId());
            Timber.i("Username: " + mqttOptions.getUsername());
            Timber.i("Password: " + mqttOptions.getPassword());
            Timber.i("TslConnect: " + mqttOptions.getTlsConnection());
            Timber.i("MQTT Configuration:");
            Timber.i("Broker: " + mqttOptions.getBrokerUrl() + ":" + mqttOptions.getPort());
            Timber.i("Publishing to topic: "+ mqttOptions.getStateTopic());
            Timber.i("Subscribing to topic: "+ mqttOptions.getCommandTopic());
            if(mqttOptions.isValid()) {
                initializeMqttClient();
            } else {
                if(listener != null) {
                    listener.handleMqttDisconnected();
                }
            }
        } catch (MqttException | IOException | GeneralSecurityException e) {
            if(listener != null) {
                listener.handleMqttException("Could not initialize MQTT: " + e.getMessage());
            }
        }
    }

    private void initializeMqttClient()
            throws MqttException, IOException, NoSuchAlgorithmException, InvalidKeySpecException {

        Timber.d("initializeMqttClient");
        
        mqttClient = new MqttAndroidClient(context, mqttOptions.getBrokerUrl(), mqttOptions.getClientId());

        MqttConnectOptions options = new MqttConnectOptions();
        if(!TextUtils.isEmpty(mqttOptions.getUsername()) && !TextUtils.isEmpty(mqttOptions.getPassword())){
            options.setUserName(mqttOptions.getUsername());
            options.setPassword(mqttOptions.getPassword().toCharArray());
        }
        mqttClient = MqttUtils.getMqttAndroidClient(context, mqttOptions.getBrokerUrl(), mqttOptions.getClientId(), 
                mqttOptions.getStateTopic(), new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                if (reconnect) {
                    Timber.d("Reconnected to : " + serverURI);
                    // Because Clean Session is true, we need to re-subscribe
                    subscribeToTopic(mqttOptions.getStateTopic());
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
                Timber.i("Received Message : " + topic + " : " + new String(message.getPayload()));
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
            }
        });

        options.setAutomaticReconnect(true);
        options.setCleanSession(false);
        
        try {
            mqttClient.connect(options, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                    disconnectedBufferOptions.setBufferEnabled(true);
                    disconnectedBufferOptions.setBufferSize(100);
                    disconnectedBufferOptions.setPersistBuffer(false);
                    disconnectedBufferOptions.setDeleteOldestMessages(false);
                    if(mqttClient != null) {
                        mqttClient.setBufferOpts(disconnectedBufferOptions);
                    }
                    if(mqttOptions != null) {
                        subscribeToTopic(mqttOptions.getStateTopic());
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    if(listener != null && mqttOptions != null) {
                        Timber.e("Failed to connect to: " + mqttOptions.getBrokerUrl() + " exception: " + exception);
                        listener.handleMqttException("Error connecting to the broker and port: " + mqttOptions.getBrokerUrl());
                    }
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
            if(listener != null) {
                listener.handleMqttException("Error while connecting: " + e.getMessage());
            }
        }
        mReady.set(true);
    }

    private void sendMessage(String mqttTopic, MqttMessage mqttMessage) throws MqttException {
        Timber.d("sendMessage");
        if (isReady() && mqttClient != null && mqttClient.isConnected()) {
            try {
                mqttClient.publish(mqttTopic, mqttMessage);
                Timber.d("Command Topic: " + mqttTopic + " Payload: " + message);
            } catch (MqttException e) {
                Timber.e("Error Sending Command: " + e.getMessage());
                e.printStackTrace();
                if(listener != null) {
                    listener.handleMqttException("Error Sending Command: " + e.getMessage());
                }
            }
        }
    }

    private void subscribeToTopic(final String topic) {
        Timber.d("subscribeToTopic");
        try {
            if (isReady() && mqttClient != null ) {
                mqttClient.subscribe(topic, 0, new IMqttMessageListener() {
                    @Override
                    public void messageArrived(final String topic, final MqttMessage message) throws Exception {
                        Timber.i("Subscribe Topic: " + topic + "  Payload: " + new String(message.getPayload()));
                        if(listener != null) {
                            listener.subscriptionMessage(topic, new String(message.getPayload()), String.valueOf(message.getId()));
                        }
                    }
                });
            }
        } catch (MqttException e) {
            if(listener != null) {
                listener.handleMqttException("Exception while subscribing: " + e.getMessage());
            }
        }
    }
}