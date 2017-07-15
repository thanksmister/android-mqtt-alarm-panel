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

package com.thanksmister.androidthings.iot.alarmpanel.data.database.model;

import android.content.ContentValues;
import android.database.Cursor;

import com.thanksmister.androidthings.iot.alarmpanel.data.database.Db;
import com.thanksmister.androidthings.iot.alarmpanel.data.database.model.db.UpdatesContract;

import java.text.SimpleDateFormat;
import java.util.Date;

import auto.parcel.AutoParcel;

@AutoParcel
public abstract class WeatherModel implements UpdatesContract
{
    public abstract long id();
    public abstract long updateId();
    public abstract String updateTable();
    public abstract String createDate();
    
    public static WeatherModel getModel (Cursor cursor) {
        long id = Db.getLong(cursor, _ID);
        long updateId = Db.getLong(cursor, UPDATE_ID);
        String updateTable = Db.getString(cursor, UPDATE_TABLE);
        String createDate = Db.getString(cursor, CREATE_DATE);
        return new AutoParcel_WeatherModel(id, updateId, updateTable, createDate);
    };

    public static Builder createBuilder(String updateTable, long updateId) {
        SimpleDateFormat updateDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String createDate = updateDateFormat.format(new Date());
        return new Builder()
                .updateId(updateId)
                .updateTableName(updateTable)
                .createDate(createDate);
    }

    public static final class Builder {
        private final ContentValues values = new ContentValues();

        public Builder updateId(long value) {
            values.put(UPDATE_ID, value);
            return this;
        }

        public Builder updateTableName(String value) {
            values.put(UPDATE_TABLE, value);
            return this;
        }

        public Builder createDate(String value) {
            values.put(CREATE_DATE, value);
            return this;
        }
        
        public ContentValues build() {
            return values;
        }
    }
}