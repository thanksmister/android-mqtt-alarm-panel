/*
 * Copyright (c) 2018 ThanksMister LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dpreference;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

/**
 * Created by wangyida on 15/12/18.
 */
class PrefAccessor {

    public static String getString(Context context, String name, String key, String defaultValue) {
        Uri URI = PreferenceProvider.buildUri(name, key, PreferenceProvider.PREF_STRING);
        String value = defaultValue;
        Cursor cursor = context.getContentResolver().query(URI, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            value = cursor.getString(cursor.getColumnIndex(PreferenceProvider.PREF_VALUE));
        }
        IOUtils.closeQuietly(cursor);
        return value;
    }

    public static int getInt(Context context, String name, String key, int defaultValue) {
        Uri URI = PreferenceProvider.buildUri(name, key, PreferenceProvider.PREF_INT);
        int value = defaultValue;
        Cursor cursor = context.getContentResolver().query(URI, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            value = cursor.getInt(cursor.getColumnIndex(PreferenceProvider.PREF_VALUE));
        }
        IOUtils.closeQuietly(cursor);
        return value;
    }

    public static long getLong(Context context, String name, String key, long defaultValue) {
        Uri URI = PreferenceProvider.buildUri(name, key, PreferenceProvider.PREF_LONG);
        long value = defaultValue;
        Cursor cursor = context.getContentResolver().query(URI, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            value = cursor.getLong(cursor.getColumnIndex(PreferenceProvider.PREF_VALUE));
        }
        IOUtils.closeQuietly(cursor);
        return value;
    }

    public static boolean getBoolean(Context context, String name, String key, boolean defaultValue) {
        Uri URI = PreferenceProvider.buildUri(name, key, PreferenceProvider.PREF_BOOLEAN);
        int value = defaultValue ? 1 : 0;
        Cursor cursor = context.getContentResolver().query(URI, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            value = cursor.getInt(cursor.getColumnIndex(PreferenceProvider.PREF_VALUE));
        }
        IOUtils.closeQuietly(cursor);
        return value == 1;
    }

    public static void remove(Context context, String name, String key) {
        Uri URI = PreferenceProvider.buildUri(name, key, PreferenceProvider.PREF_STRING);
        context.getContentResolver().delete(URI, null, null);
    }

    public static void setString(Context context, String name, String key, String value) {
        Uri URI = PreferenceProvider.buildUri(name, key, PreferenceProvider.PREF_STRING);
        ContentValues cv = new ContentValues();
        cv.put(PreferenceProvider.PREF_KEY, key);
        cv.put(PreferenceProvider.PREF_VALUE, value);
        context.getContentResolver().update(URI, cv, null, null);
    }

    public static void setBoolean(Context context, String name, String key, boolean value) {
        Uri URI = PreferenceProvider.buildUri(name, key, PreferenceProvider.PREF_BOOLEAN);
        ContentValues cv = new ContentValues();
        cv.put(PreferenceProvider.PREF_KEY, key);
        cv.put(PreferenceProvider.PREF_VALUE, value);
        context.getContentResolver().update(URI, cv, null, null);
    }

    public static void setInt(Context context, String name, String key, int value) {
        Uri URI = PreferenceProvider.buildUri(name, key, PreferenceProvider.PREF_INT);
        ContentValues cv = new ContentValues();
        cv.put(PreferenceProvider.PREF_KEY, key);
        cv.put(PreferenceProvider.PREF_VALUE, value);
        context.getContentResolver().update(URI, cv, null, null);
    }

    public static void setLong(Context context, String name, String key, long value) {
        Uri URI = PreferenceProvider.buildUri(name, key, PreferenceProvider.PREF_LONG);
        ContentValues cv = new ContentValues();
        cv.put(PreferenceProvider.PREF_KEY, key);
        cv.put(PreferenceProvider.PREF_VALUE, value);
        context.getContentResolver().update(URI, cv, null, null);
    }
}

