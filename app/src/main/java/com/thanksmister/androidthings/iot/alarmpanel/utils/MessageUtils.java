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

import java.util.ArrayList;
import java.util.List;

/**
 * Talks with other MQTT services to handle subscribing and publishing different topics
 * other than the built-in alarm (lights for example).
 */
public class MessageUtils {
    
    // TODO maybe these can be moved to message util class
    public static final String MESSAGE_TYPE_SUBSCRIBE = "message_subscribe";
    public static final String MESSAGE_TYPE_PUBLISH = "message_publish";
    
    private static final List<String> supportedMessageTypes = new ArrayList<String>();
    
    public MessageUtils(){
    }
    
    static {
        supportedMessageTypes.add(MESSAGE_TYPE_SUBSCRIBE);
        supportedMessageTypes.add(MESSAGE_TYPE_PUBLISH);
    }

    /**
     * Returns true if message type is supported.
     * @return
     */
    public static boolean hasSupportedMessageType(String messageType) {
        return supportedMessageTypes.contains(messageType);
    }
}