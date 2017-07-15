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

package com.thanksmister.androidthings.iot.alarmpanel.network.sync;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.thanksmister.androidthings.iot.alarmpanel.data.database.model.db.FeedDataContract;
import com.thanksmister.androidthings.iot.alarmpanel.data.database.model.db.UpdatesContract;

public class SyncProvider extends ContentProviderBase {
    
    public static final long SYNC_FREQUENCY = 5 * 60;  // 5 minutes in seconds

    public static final Uri UPDATES_TABLE_URI = CONTENT_URI.buildUpon().appendPath(UpdatesContract.TABLE_NAME).build();
    public static final Uri FEED_DATA_TABLE_URI = CONTENT_URI.buildUpon().appendPath(FeedDataContract.TABLE_NAME).build();

    public SyncProvider()
    {
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        if (isValidUri(uri)) {
            return super.query(uri, projection, selection, selectionArgs, sortOrder);
        }
        throw new IllegalArgumentException("Unknown URI " + uri);
    }
    
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues initialValues) {
        // Validates the incoming URI. Only the full provider URI is allowed for inserts.
        if (isValidUri(uri)) {
            return super.insert(uri, initialValues);
        }

        throw new IllegalArgumentException("Unknown URI " + uri);
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String whereClause, String[] whereArgs) {
        // Validates the incoming URI. Only the full provider URI is allowed for inserts.
        if (isValidUri(uri)) {
            return super.update(uri, values, whereClause, whereArgs);
        }

        throw new IllegalArgumentException("Unknown URI " + uri);
    }

    @Override
    public int delete(@NonNull Uri uri, String where, String[] args) {
        // Validates the incoming URI. Only the full provider URI is allowed for inserts.
        if (isValidUri(uri)) {
            return super.delete(uri, where, args);
        }

        throw new IllegalArgumentException("Unknown URI " + uri);
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values){
        if (isValidUri(uri)) {
            return super.bulkInsert(uri, values);
        }
        throw new IllegalArgumentException("Unknown URI " + uri);
    }
    
    @Nullable
    @Override
    public String getType(@NonNull Uri uri)
    {
        return null;
    }

    public boolean isValidUri(Uri uri) {
        return (uri.equals(UPDATES_TABLE_URI) || uri.equals(FEED_DATA_TABLE_URI));
    }
}