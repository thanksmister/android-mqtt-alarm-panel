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

package com.thanksmister.iot.mqtt.alarmpanel.data.stores;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.thanksmister.iot.mqtt.alarmpanel.data.database.model.SubscriptionModel;
import com.thanksmister.iot.mqtt.alarmpanel.data.provider.ContentProvider;

import java.util.ArrayList;
import java.util.List;

import dpreference.DPreference;

/**
 * Place to store global application values as well as get references to local and remote data stores
 */
public class StoreManager {
    
    private final DPreference sharedPreferences;
    private final ContentResolver contentResolver;
    private final Context context;

    public StoreManager(Context context, ContentResolver contentResolver, DPreference sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
        this.contentResolver = contentResolver;
        this.context = context;
    }

    /**
     * Returns the <code>MqttModel</code> list from the database
     * @return
     */
    public List<SubscriptionModel> getMqttDataList() {
        List<SubscriptionModel> modelList = new ArrayList<>();
        Cursor cursor = contentResolver.query(ContentProvider.SUBSCRIPTION_DATA_TABLE_URI, null, null, null, null);
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                SubscriptionModel dataModel = SubscriptionModel.getModel(cursor);
                modelList.add(dataModel);
            }
            cursor.close();
        }
        return modelList;
    }

    /**
     * Insert the MQTT data into the database
     * @param topic
     * @param payload
     * @param messageId
     */
    public void insertMqttData(String topic, String payload, String messageId) {
        synchronized( this ) {
            contentResolver.insert(ContentProvider.SUBSCRIPTION_DATA_TABLE_URI, SubscriptionModel.createBuilder(topic, payload, messageId).build());
            contentResolver.notifyChange(ContentProvider.SUBSCRIPTION_DATA_TABLE_URI, null);
        }
    }

    public void reset() {
        contentResolver.delete(ContentProvider.SUBSCRIPTION_DATA_TABLE_URI, null, null);
    }

    /**
     * Convenience method for clearing a table based on Uri
     * @param uri
     */
    public void resetTable(Uri uri) {
        contentResolver.delete(uri, null, null);
    }
}