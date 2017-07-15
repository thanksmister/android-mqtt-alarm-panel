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

public interface WeatherContract extends BaseColumns {
   
    public static final String TABLE_NAME = "updates";
    public static final String UPDATE_TABLE = "update_table";
    public static final String UPDATE_ID = "update_id";
    public static final String CREATE_DATE = "create_date";

    public static final String CREATE_TABLE_STATEMENT = "CREATE TABLE IF NOT EXISTS "
            + TABLE_NAME
            + " ("
            + _ID
            + " INTEGER PRIMARY KEY, "
            + UPDATE_TABLE
            + " TEXT, "
            + UPDATE_ID
            + " INTEGER, "
            + CREATE_DATE
            + " TEXT)";
    
    public static final String DELETE_TABLE_STATEMENT = "DROP TABLE IF EXISTS " + TABLE_NAME;
    public static final String[] COLUMN_NAMES = {_ID, UPDATE_ID, UPDATE_TABLE, CREATE_DATE};
}
