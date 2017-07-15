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
import com.thanksmister.androidthings.iot.alarmpanel.data.database.model.db.FeedDataContract;
import com.thanksmister.androidthings.iot.alarmpanel.network.model.FeedData;

import java.util.ArrayList;
import java.util.List;

import auto.parcel.AutoParcel;

@AutoParcel
public abstract class FeedDataModel implements FeedDataContract {
    
    public abstract long id();
    public abstract String dataId();
    @Nullable
    public abstract Integer feedId();
    @Nullable
    public abstract Integer groupId();
    @Nullable
    public abstract String value();
    @Nullable
    public abstract String createdAt();
    @Nullable
    public abstract String updatedAt();
    @Nullable
    public abstract String location();
    @Nullable
    public abstract Integer lat();
    @Nullable
    public abstract Integer lon();
    @Nullable
    public abstract Integer ele();
    @Nullable
    public abstract Integer createdEpoch();

    public static FeedDataModel getModel (Cursor cursor) {
        if (cursor != null && cursor.getCount() > 0) {
            long id = Db.getLong(cursor, _ID);
            String dataId = Db.getString(cursor, DATA_ID);
            int feedId = Db.getInt(cursor, FEED_ID);
            int groupId = Db.getInt(cursor, GROUP_ID);
            String value = Db.getString(cursor, VALUE);
            String createdAt = Db.getString(cursor, CREATED_AT);
            String updatedAt = Db.getString(cursor, UPDATED_AT);
            String location = Db.getString(cursor, LOCATION);
            int lat = Db.getInt(cursor, LAT);
            int lon = Db.getInt(cursor, LON);
            int ele = Db.getInt(cursor, ELE);
            int createdEpoch = Db.getInt(cursor, CREATED_EPOCH);
            return new AutoParcel_FeedDataModel(id, dataId, feedId, groupId, value, createdAt, updatedAt, location,
                    lat, lon, ele, createdEpoch);
        }
        return null;
    };

    public static List<FeedDataModel> getModelList (Cursor cursor) {
        List<FeedDataModel> modelList = new ArrayList<>();
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                FeedDataModel dataModel = getModel(cursor);
                modelList.add(dataModel);
            }
        }
        return modelList;
    };

    public static FeedData getNetworkModel(FeedDataModel model) {
        FeedData feedData = new FeedData();
        feedData.setId(model.dataId());
        feedData.setFeedId(model.feedId());
        feedData.setGroupId(model.groupId());
        feedData.setValue(model.value());
        feedData.setCreatedAt(model.createdAt());
        feedData.setUpdatedAt(model.updatedAt());
        feedData.setLocation(model.location());
        feedData.setLon(model.lon());
        feedData.setLat(model.lat());
        feedData.setEle(model.ele());
        feedData.setCreatedEpoch(model.createdEpoch());
        return feedData;
    }

    public static Builder createBuilder(FeedData feedData) {
        return new Builder()
                .dataId(feedData.getId())
                .groupId(feedData.getGroupId())
                .groupId(feedData.getGroupId())
                .feedId(feedData.getFeedId())
                .value(feedData.getValue())
                .createdAt(feedData.getCreatedAt())
                .updatedAt(feedData.getUpdatedAt())
                .location(feedData.getLocation())
                .lon(feedData.getLon())
                .lat(feedData.getLat())
                .ele(feedData.getEle())
                .createdEpoch(feedData.getCreatedEpoch());
    }

    public static final class Builder {
        private final ContentValues values = new ContentValues();

        public Builder dataId(String value) {
            values.put(DATA_ID, value);
            return this;
        }
        
        public Builder feedId(Integer value) {
            values.put(FEED_ID, value);
            return this;
        }

        public Builder groupId(Integer value) {
            values.put(GROUP_ID, value);
            return this;
        }

        public Builder value(String value) {
            values.put(VALUE, value);
            return this;
        }

        public Builder createdAt(String value) {
            values.put(CREATED_AT, value);
            return this;
        }

        public Builder updatedAt(String value) {
            values.put(UPDATED_AT, value);
            return this;
        }

        public Builder location(String value) {
            values.put(LOCATION, value);
            return this;
        }

        public Builder lat(Integer value) {
            values.put(LAT, value);
            return this;
        }

        public Builder lon(Integer value) {
            values.put(LON, value);
            return this;
        }

        public Builder ele(Integer value) {
            values.put(ELE, value);
            return this;
        }

        public Builder createdEpoch(Integer value) {
            values.put(CREATED_EPOCH, value);
            return this;
        }

        public ContentValues build() {
            return values;
        }
    }
}