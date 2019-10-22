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

package dpreference

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import com.google.android.gms.common.util.IOUtils

/**
 * Created by wangyida on 15/12/18.
 */
internal object PrefAccessor {

    fun getString(context: Context, name: String, key: String, defaultValue: String): String {
        val URI = PreferenceProvider.buildUri(name, key, PreferenceProvider.PREF_STRING)
        var value = defaultValue
        val cursor = context.contentResolver.query(URI, null, null, null, null)
        if (cursor != null && cursor.moveToFirst()) {
            value = cursor.getString(cursor.getColumnIndex(PreferenceProvider.PREF_VALUE))
        }
        IOUtils.closeQuietly(cursor)
        return value
    }

    fun getInt(context: Context, name: String, key: String, defaultValue: Int): Int {
        val URI = PreferenceProvider.buildUri(name, key, PreferenceProvider.PREF_INT)
        var value = defaultValue
        val cursor = context.contentResolver.query(URI, null, null, null, null)
        if (cursor != null && cursor.moveToFirst()) {
            value = cursor.getInt(cursor.getColumnIndex(PreferenceProvider.PREF_VALUE))
        }
        IOUtils.closeQuietly(cursor)
        return value
    }

    fun getLong(context: Context, name: String, key: String, defaultValue: Long): Long {
        val URI = PreferenceProvider.buildUri(name, key, PreferenceProvider.PREF_LONG)
        var value = defaultValue
        val cursor = context.contentResolver.query(URI, null, null, null, null)
        if (cursor != null && cursor.moveToFirst()) {
            value = cursor.getLong(cursor.getColumnIndex(PreferenceProvider.PREF_VALUE))
        }
        IOUtils.closeQuietly(cursor)
        return value
    }

    fun getBoolean(context: Context, name: String, key: String, defaultValue: Boolean): Boolean {
        val URI = PreferenceProvider.buildUri(name, key, PreferenceProvider.PREF_BOOLEAN)
        var value = if (defaultValue) 1 else 0
        val cursor = context.contentResolver.query(URI, null, null, null, null)
        if (cursor != null && cursor.moveToFirst()) {
            value = cursor.getInt(cursor.getColumnIndex(PreferenceProvider.PREF_VALUE))
        }
        IOUtils.closeQuietly(cursor)
        return value == 1
    }

    fun remove(context: Context, name: String, key: String) {
        val URI = PreferenceProvider.buildUri(name, key, PreferenceProvider.PREF_STRING)
        context.contentResolver.delete(URI, null, null)
    }

    fun setString(context: Context, name: String, key: String, value: String) {
        val URI = PreferenceProvider.buildUri(name, key, PreferenceProvider.PREF_STRING)
        val cv = ContentValues()
        cv.put(PreferenceProvider.PREF_KEY, key)
        cv.put(PreferenceProvider.PREF_VALUE, value)
        context.contentResolver.update(URI, cv, null, null)
    }

    fun setBoolean(context: Context, name: String, key: String, value: Boolean) {
        val URI = PreferenceProvider.buildUri(name, key, PreferenceProvider.PREF_BOOLEAN)
        val cv = ContentValues()
        cv.put(PreferenceProvider.PREF_KEY, key)
        cv.put(PreferenceProvider.PREF_VALUE, value)
        context.contentResolver.update(URI, cv, null, null)
    }

    fun setInt(context: Context, name: String, key: String, value: Int) {
        val URI = PreferenceProvider.buildUri(name, key, PreferenceProvider.PREF_INT)
        val cv = ContentValues()
        cv.put(PreferenceProvider.PREF_KEY, key)
        cv.put(PreferenceProvider.PREF_VALUE, value)
        context.contentResolver.update(URI, cv, null, null)
    }

    fun setLong(context: Context, name: String, key: String, value: Long) {
        val URI = PreferenceProvider.buildUri(name, key, PreferenceProvider.PREF_LONG)
        val cv = ContentValues()
        cv.put(PreferenceProvider.PREF_KEY, key)
        cv.put(PreferenceProvider.PREF_VALUE, value)
        context.contentResolver.update(URI, cv, null, null)
    }
}

