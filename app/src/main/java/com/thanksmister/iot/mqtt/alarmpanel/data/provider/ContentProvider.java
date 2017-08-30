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

package com.thanksmister.iot.mqtt.alarmpanel.data.provider;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.thanksmister.iot.mqtt.alarmpanel.data.database.DbHelper;
import com.thanksmister.iot.mqtt.alarmpanel.data.database.model.SubscriptionModel;

public class ContentProvider extends ContentProviderBase {
    
    public static final Uri SUBSCRIPTION_DATA_TABLE_URI = CONTENT_URI.buildUpon().appendPath(SubscriptionModel.TABLE_NAME).build();
    
    public ContentProvider()
    {
    }

    DbHelper dbOpenHelper;
    ContentResolver contentResolver;

    @Override
    public boolean onCreate() {
        dbOpenHelper = new DbHelper(getContext());
        contentResolver = getContext().getContentResolver();
        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        if (isValidUri(uri)) {
            String table = getTableName(uri);
            SQLiteDatabase database = dbOpenHelper.getReadableDatabase();
            Cursor cursor = database.query(table, projection, selection, selectionArgs, null, null, sortOrder);
            // https://stackoverflow.com/questions/7915050/cursorloader-not-updating-after-data-change
            cursor.setNotificationUri(contentResolver, uri);
            return cursor;
        }
        throw new IllegalArgumentException("Unknown URI " + uri);
    }
    
    /*
     String table = getTableName(uri);
        SQLiteDatabase database = dbOpenHelper.getReadableDatabase();
        Cursor cursor = database.query(table, projection, selection, selectionArgs, null, null, sortOrder);
        // https://stackoverflow.com/questions/7915050/cursorloader-not-updating-after-data-change
        cursor.setNotificationUri(contentResolver, uri);
        return cursor;
     */

    public static String getTableName(Uri uri) {
        String value = uri.getPath();
        value = value.replace("/", "");//we need to remove '/'
        return value;
    }
    
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues initialValues) {
        // Validates the incoming URI. Only the full provider URI is allowed for inserts.
        if (isValidUri(uri)) {
            String table = getTableName(uri);
            SQLiteDatabase database = dbOpenHelper.getWritableDatabase();
            long value = database.insert(table, null, initialValues);
            contentResolver.notifyChange(uri, null);
            return Uri.withAppendedPath(CONTENT_URI, String.valueOf(value));
        }

        throw new IllegalArgumentException("Unknown URI " + uri);
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String whereClause, String[] whereArgs) {
        // Validates the incoming URI. Only the full provider URI is allowed for inserts.
        if (isValidUri(uri)) {
            String table = getTableName(uri);
            SQLiteDatabase database = dbOpenHelper.getWritableDatabase();
            contentResolver.notifyChange(uri, null);
            return database.update(table, values, whereClause, whereArgs);
        }

        throw new IllegalArgumentException("Unknown URI " + uri);
    }

    @Override
    public int delete(@NonNull Uri uri, String where, String[] args) {
        // Validates the incoming URI. Only the full provider URI is allowed for inserts.
        if (isValidUri(uri)) {
            String table = getTableName(uri);
            SQLiteDatabase dataBase = dbOpenHelper.getWritableDatabase();
            contentResolver.notifyChange(uri, null);
            return dataBase.delete(table, where, args);
        }

        throw new IllegalArgumentException("Unknown URI " + uri);
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values){
        if (isValidUri(uri)) {
            //contentResolver.notifyChange(uri, null);
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
        return (uri.equals(SUBSCRIPTION_DATA_TABLE_URI));
    }
}