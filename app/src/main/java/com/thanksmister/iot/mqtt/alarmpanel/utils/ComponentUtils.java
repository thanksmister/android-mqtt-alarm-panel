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

package com.thanksmister.iot.mqtt.alarmpanel.utils;

import com.thanksmister.iot.mqtt.alarmpanel.ui.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * Just a utility class to work with the multiple topics and commands for different components.
 */
public class ComponentUtils {

    public static final String NOTIFICATION_STATE_TOPIC = "home/notification";
    public static final String NOTIFICATION_TYPE = "NOTIFICATION";

    public ComponentUtils(){
    }
}