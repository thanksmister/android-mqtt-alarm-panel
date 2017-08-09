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
import android.support.annotation.Nullable;

import com.thanksmister.androidthings.iot.alarmpanel.data.database.Db;
import com.thanksmister.androidthings.iot.alarmpanel.data.database.model.db.ComponentContract;

import java.util.ArrayList;
import java.util.List;

import auto.parcel.AutoParcel;

@AutoParcel
public abstract class ComponentModel implements ComponentContract {
    
    public abstract long id();
    public abstract String name();
    public abstract String type();
    public abstract String command_topic();
    public abstract String state_topic();
    @Nullable
    public abstract String payload();
    @Nullable
    public abstract Integer qos();
    @Nullable
    public abstract Boolean retained();
    
    public static ComponentModel getModel (Cursor cursor) {
        if (cursor != null && cursor.getCount() > 0) {
            long id = Db.getLong(cursor, _ID);
            String name = Db.getString(cursor, NAME);
            String type = Db.getString(cursor, TYPE);
            String command_topic = Db.getString(cursor, COMMAND_TOPIC);
            String state_topic = Db.getString(cursor, STATE_TOPIC);
            String payload = Db.getString(cursor, PAYLOAD);
            int qos = Db.getInt(cursor, QOS);
            boolean retained = Db.getBoolean(cursor, RETAINED);
            return new AutoParcel_ComponentModel(id, name, type, command_topic, state_topic, payload, qos, retained);
        }
        return null;
    };

    public static List<ComponentModel> getModelList (Cursor cursor) {
        List<ComponentModel> modelList = new ArrayList<>();
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                ComponentModel dataModel = getModel(cursor);
                modelList.add(dataModel);
            }
        }
        return modelList;
    };

    public static Builder createBuilder(String type, String name, 
                                        String command, String state, 
                                        String payload, int qos, 
                                        boolean retained) {
        return new Builder()
                .name(name)
                .type(type)
                .command_topic(command)
                .state_topic(state)
                .payload(payload)
                .qos(qos)
                .retained(retained);
    }

    public static final class Builder {
        private final ContentValues values = new ContentValues();

        public Builder name(String value) {
            values.put(NAME, value);
            return this;
        }

        public Builder type(String value) {
            values.put(TYPE, value);
            return this;
        }
        
        public Builder command_topic(String value) {
            values.put(COMMAND_TOPIC, value);
            return this;
        }

        public Builder state_topic(String value) {
            values.put(STATE_TOPIC, value);
            return this;
        }

        public Builder payload(String value) {
            values.put(PAYLOAD, value);
            return this;
        }
        
        public Builder qos(Integer value) {
            values.put(QOS, value);
            return this;
        }

        public Builder retained(Boolean value) {
            values.put(RETAINED, (value)? 1:0);
            return this;
        }
        
        public ContentValues build() {
            return values;
        }
    }
}