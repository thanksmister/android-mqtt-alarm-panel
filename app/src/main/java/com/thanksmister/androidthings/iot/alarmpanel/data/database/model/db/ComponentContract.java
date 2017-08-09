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

package com.thanksmister.androidthings.iot.alarmpanel.data.database.model.db;

import android.provider.BaseColumns;

public interface ComponentContract extends BaseColumns {
    
    // DB contract strings for queries
    public static final String TABLE_NAME = "mqtt_component_data";
    public static final String NAME = "name";
    public static final String TYPE = "type";
    public static final String COMMAND_TOPIC = "command_topic";
    public static final String STATE_TOPIC = "state_topic";
    public static final String PAYLOAD = "payload";
    public static final String QOS = "qos";
    public static final String RETAINED = "retained";

    public static final String CREATE_TABLE_STATEMENT = "CREATE TABLE IF NOT EXISTS "
            + TABLE_NAME
            + " ("
            + _ID
            + " INTEGER PRIMARY KEY, "
            + NAME
            + " TEXT, "
            + COMMAND_TOPIC
            + " TEXT, "
            + STATE_TOPIC
            + " TEXT, "
            + PAYLOAD
            + " TEXT, "
            + QOS
            + " INTEGER, "
            + RETAINED
            + " INTEGER, "
            + TYPE
            + " TEXT)";

    public static final String DELETE_TABLE_STATEMENT = "DROP TABLE IF EXISTS " + TABLE_NAME;

    public static final String[] COLUMN_NAMES = {_ID, TYPE, NAME, COMMAND_TOPIC, STATE_TOPIC, PAYLOAD, QOS, RETAINED};
}
