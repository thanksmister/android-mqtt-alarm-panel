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

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

/**
 * Just a utility class to work with the specific settings of the Home Assistant
 * MQTT Manual Alarm Control Panel https://home-assistant.io/components/alarm_control_panel.manual_mqtt/.
 */
public class AlarmUtils {
    
    public static final int PORT = 1883;

    public static final String COMMAND_TOPIC = "home/alarm/set";
    public static final String STATE_TOPIC = "home/alarm";
    
    public static final String COMMAND_ARM_HOME = "ARM_HOME";
    public static final String COMMAND_ARM_AWAY = "ARM_AWAY";
    public static final String COMMAND_DISARM = "DISARM";
    
    public static final String STATE_DISARM = "disarmed";
    public static final String STATE_ARM_AWAY = "armed_away";
    public static final String STATE_ARM_HOME = "armed_home";
    public static final String STATE_PENDING = "pending";
    public static final String STATE_TRIGGERED = "triggered";
    public static final int PENDING_TIME = 60;
    public static final int TRIGGER_TIME = 120;
    
    private static final List<String> supportedCommands = new ArrayList<String>();
    private static final List<String> supportedStates = new ArrayList<String>();
    
    public AlarmUtils(){
    }

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({AlarmUtils.STATE_ARM_AWAY, AlarmUtils.STATE_ARM_HOME, AlarmUtils.STATE_PENDING, AlarmUtils.STATE_DISARM, AlarmUtils.STATE_TRIGGERED})
    public @interface AlarmStates {}

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({AlarmUtils.COMMAND_ARM_HOME, AlarmUtils.COMMAND_ARM_AWAY, AlarmUtils.COMMAND_DISARM})
    public @interface AlarmCommands {}

    static {
        supportedCommands.add(COMMAND_ARM_HOME);
        supportedCommands.add(COMMAND_ARM_AWAY);
        supportedCommands.add(COMMAND_DISARM);
    }

    static {
        supportedStates.add(STATE_DISARM);
        supportedStates.add(STATE_ARM_AWAY);
        supportedStates.add(STATE_ARM_HOME);
        supportedStates.add(STATE_PENDING);
        supportedStates.add(STATE_TRIGGERED);
    }

    /**
     * Topic is of type command topic
     * @param command
     * @return
     */
    @AlarmCommands
    public static boolean hasSupportedCommands(String command) {
        return supportedCommands.contains(command);
    }

    /**
     * Topic is of type state topic
     * @param state
     * @return
     */
    @AlarmStates
    public static boolean hasSupportedStates(String state) {
        return supportedStates.contains(state);
    }
}