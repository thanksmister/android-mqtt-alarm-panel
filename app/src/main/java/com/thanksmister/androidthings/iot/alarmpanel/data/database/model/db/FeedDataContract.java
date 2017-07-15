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

public interface FeedDataContract extends BaseColumns {
    
    // DB contract strings for queries
    public static final String TABLE_NAME = "feed_data";
    public static final String DATA_ID = "dataId";
    public static final String FEED_ID = "feedId";
    public static final String GROUP_ID = "groupId";
    public static final String VALUE = "value";
    public static final String CREATED_AT = "createdAt";
    public static final String UPDATED_AT = "updatedAt";
    public static final String LOCATION = "location";
    public static final String LAT = "lat";
    public static final String LON = "lon";
    public static final String ELE = "ele";
    public static final String CREATED_EPOCH = "createdEpoch";
    public static final String EXPIRATION = "expiration";

    public static final String CREATE_TABLE_STATEMENT = "CREATE TABLE IF NOT EXISTS "
            + TABLE_NAME
            + " ("
            + _ID
            + " INTEGER PRIMARY KEY, "
            + DATA_ID
            + " TEXT, "
            + FEED_ID
            + " INTEGER, "
            + GROUP_ID
            + " INTEGER, "
            + VALUE
            + " TEXT, "
            + CREATED_AT
            + " TEXT, "
            + UPDATED_AT
            + " TEXT, "
            + LOCATION
            + " TEXT, "
            + LAT
            + " INTEGER, "
            + LON
            + " INTEGER, "
            + ELE
            + " INTEGER, "
            + CREATED_EPOCH
            + " INTEGER, "
            + EXPIRATION
            + " INTEGER)";

    public static final String DELETE_TABLE_STATEMENT = "DROP TABLE IF EXISTS " + TABLE_NAME;

    public static final String[] COLUMN_NAMES = {_ID, DATA_ID, FEED_ID, GROUP_ID, VALUE, CREATED_AT, UPDATED_AT,
            LOCATION, LAT, LON, ELE, CREATED_EPOCH, EXPIRATION};
}
