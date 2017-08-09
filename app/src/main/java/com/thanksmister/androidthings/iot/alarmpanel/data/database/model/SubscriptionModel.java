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
import com.thanksmister.androidthings.iot.alarmpanel.data.database.model.db.SubscriptionContract;
import com.thanksmister.androidthings.iot.alarmpanel.utils.DateUtils;

import java.util.ArrayList;
import java.util.List;

import auto.parcel.AutoParcel;

@AutoParcel
public abstract class SubscriptionModel implements SubscriptionContract {
    
    public abstract long id();
    public abstract String topic();
    public abstract String payload();
    public abstract String messageId();
    public abstract String createdAt();
    
    public static SubscriptionModel getModel (Cursor cursor) {
        if (cursor != null && cursor.getCount() > 0) {
            long id = Db.getLong(cursor, _ID);
            String topic = Db.getString(cursor, TOPIC);
            String payload = Db.getString(cursor, PAYLOAD);
            String messageId = Db.getString(cursor, MESSAGE_ID);
            String createdAt = Db.getString(cursor, CREATED_AT);
            return new AutoParcel_SubscriptionModel(id, topic, payload, messageId, createdAt);
        }
        return null;
    };

    public static List<SubscriptionModel> getModelList (Cursor cursor) {
        List<SubscriptionModel> modelList = new ArrayList<>();
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                SubscriptionModel dataModel = getModel(cursor);
                modelList.add(dataModel);
            }
        }
        return modelList;
    };

    public static Builder createBuilder(String topic, String payload, String messageId) {
        String createdAt = DateUtils.generateCreatedAtDate();
        return new Builder()
                .topic(topic)
                .payload(payload)
                .messageId(messageId)
                .createdAt(createdAt);
    }

    public static final class Builder {
        private final ContentValues values = new ContentValues();

        public Builder topic(String value) {
            values.put(TOPIC, value);
            return this;
        }

        public Builder payload(String value) {
            values.put(PAYLOAD, value);
            return this;
        }

        public Builder messageId(String value) {
            values.put(MESSAGE_ID, value);
            return this;
        }
        
        public Builder createdAt(String value) {
            values.put(CREATED_AT, value);
            return this;
        }
        
        public ContentValues build() {
            return values;
        }
    }
}