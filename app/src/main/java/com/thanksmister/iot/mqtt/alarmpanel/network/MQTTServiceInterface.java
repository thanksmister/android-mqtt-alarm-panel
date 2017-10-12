package com.thanksmister.iot.mqtt.alarmpanel.network;

import org.eclipse.paho.client.mqttv3.MqttException;

/**
 * TODO: Add a class header comment!
 */
public interface MQTTServiceInterface {

    boolean isReady();

    void publish(String payload);

    void reconfigure(MQTTOptions options);
    
    void close() throws MqttException;
}