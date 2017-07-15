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

package com.thanksmister.androidthings.iot.alarmpanel.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;

/**
 * TODO: Add a class header comment!
 */
public class MQTTUtils {

    public MQTTUtils(){
    }

    public static MqttConnectOptions getMqttConnectOptions () {
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(false);
        return mqttConnectOptions;
    }

    public static MqttConnectOptions getMqttConnectOptions (@NonNull String username, @NonNull String password) {
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(false);
        
        if(!TextUtils.isEmpty(username)) {
            mqttConnectOptions.setUserName(username);
        }

        if(!TextUtils.isEmpty(password)) {
            char[] passwordArray = password.toCharArray();
            mqttConnectOptions.setPassword(passwordArray);
        }

        return mqttConnectOptions;
    }

    public static MqttAndroidClient getMqttAndroidClient(Context context, String serverUri, String clientId, String topic, 
                                                         MqttCallbackExtended mqttCallbackExtended) {
        MqttAndroidClient mqttAndroidClient = new MqttAndroidClient(context, serverUri, clientId);
        mqttAndroidClient.setCallback(mqttCallbackExtended);
        return mqttAndroidClient;
    }
}
