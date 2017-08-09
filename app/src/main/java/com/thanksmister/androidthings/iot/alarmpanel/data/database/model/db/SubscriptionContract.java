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

public interface SubscriptionContract extends BaseColumns {
    
    // DB contract strings for queries
    public static final String TABLE_NAME = "mqtt_subscription_data";
    public static final String TOPIC = "topic";
    public static final String MESSAGE_ID = "messageId";
    public static final String PAYLOAD = "payload";
    public static final String CREATED_AT = "createdAt";

    public static final String CREATE_TABLE_STATEMENT = "CREATE TABLE IF NOT EXISTS "
            + TABLE_NAME
            + " ("
            + _ID
            + " INTEGER PRIMARY KEY, "
            + TOPIC
            + " TEXT, "
            + PAYLOAD
            + " TEXT, "
            + MESSAGE_ID
            + " TEXT, "
            + CREATED_AT
            + " TEXT)";

    public static final String DELETE_TABLE_STATEMENT = "DROP TABLE IF EXISTS " + TABLE_NAME;

    public static final String[] COLUMN_NAMES = {_ID, TOPIC, PAYLOAD, MESSAGE_ID, CREATED_AT};
}
