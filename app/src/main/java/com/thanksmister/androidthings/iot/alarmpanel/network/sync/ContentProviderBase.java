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

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.thanksmister.androidthings.iot.alarmpanel.data.database.DbHelper;
import com.thanksmister.androidthings.iot.alarmpanel.data.database.model.UpdatesModel;

import static com.thanksmister.androidthings.iot.alarmpanel.network.sync.SyncProvider.UPDATES_TABLE_URI;

abstract public class ContentProviderBase extends ContentProvider {
    
    public static final String CONTENT_AUTHORITY = "com.thanksmister.androidthings.iot.alarmpanel";
    public static final Uri CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    
    DbHelper dbOpenHelper;
    ContentResolver contentResolver;

    public SQLiteDatabase getDataBase() {
        if(dbOpenHelper == null) {
            dbOpenHelper = DbHelper.getInstance(getContext());
        }
        return dbOpenHelper.getReadableDatabase();
    }

    @Override
    public boolean onCreate() {
        dbOpenHelper = DbHelper.getInstance(getContext());
        if(getContext() != null) {
            contentResolver = getContext().getContentResolver();
        }
        return true;
    }

    @Override
    public int delete(@NonNull Uri uri, String where, String[] args) {
        String table = getTableName(uri);
        SQLiteDatabase dataBase = getDataBase();
        contentResolver.notifyChange(uri, null, false); // the 3rd parameter causes syncadapter to fire
        return dataBase.delete(table, where, args);
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues initialValues) {
        String table = getTableName(uri);
        SQLiteDatabase database = getDataBase();
        long value = database.insert(table, null, initialValues);

        // If the insert succeeded, the row ID exists.
        if (value > 0) {
            contentResolver.notifyChange(uri, null, false); // the 3rd parameter causes syncadapter to fire
            return Uri.withAppendedPath(CONTENT_URI, String.valueOf(value));
        }

        // If the insert didn't succeed, then the rowID is <= 0. Throws an exception.
        throw new SQLException("Failed to insert row into " + uri);
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        String table = getTableName(uri);
        SQLiteDatabase database = getDataBase();
        return database.query(table, projection, selection, selectionArgs, null, null, sortOrder);
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String whereClause, String[] whereArgs) {
        String table = getTableName(uri);
        SQLiteDatabase database = getDataBase();
        contentResolver.notifyChange(uri, null, false); // the 3rd parameter causes syncadapter to fire
        return database.update(table, values, whereClause, whereArgs);
    }
    
    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values){
        int numInserted = 0;
        String table;
        if (uri.equals(UPDATES_TABLE_URI)) {
            table = UpdatesModel.TABLE_NAME;
        } else {
            throw new SQLException("Unsupported uri " + uri);
        }

        SQLiteDatabase database = getDataBase();
        database.beginTransaction();
        try {
            for (ContentValues cv : values) {
                long newID = database.replaceOrThrow(table, null, cv);
                if (newID <= 0) {
                    throw new SQLException("Failed to insert row into " + uri);
                }
            }
            database.setTransactionSuccessful();
            contentResolver.notifyChange(uri, null);
            numInserted = values.length;
        } finally {
            database.endTransaction();
        }
        return numInserted;
    }

    protected static String getTableName(Uri uri) {
        String value = uri.getPath();
        value = value.replace("/", "");//we need to remove '/'
        return value;
    }
}