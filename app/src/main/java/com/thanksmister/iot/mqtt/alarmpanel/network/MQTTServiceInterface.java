package com.thanksmister.iot.mqtt.alarmpanel.network;

import android.content.Context;

import org.eclipse.paho.client.mqttv3.MqttException;

public interface MQTTServiceInterface {

    boolean isReady();

    void publish(String payload);

    void reconfigure(Context context, MQTTOptions options, MQTTService.MqttManagerListener listener);
    
    void close() throws MqttException;
}