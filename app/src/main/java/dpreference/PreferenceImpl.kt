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

import android.content.Context
import android.content.SharedPreferences

/**
 * Created by wangyida on 15/12/18.
 */
internal class PreferenceImpl(private val mContext: Context, private val mPrefName: String) : IPrefImpl {

    override fun getPrefString(key: String,
                               defaultValue: String): String {
        val settings = mContext.getSharedPreferences(mPrefName, Context.MODE_PRIVATE)
        return settings.getString(key, defaultValue)?:""
    }

    override fun setPrefString(key: String, value: String) {
        val settings = mContext.getSharedPreferences(mPrefName, Context.MODE_PRIVATE)
        settings.edit().putString(key, value).apply()
    }

    override fun getPrefBoolean(key: String,
                                defaultValue: Boolean): Boolean {
        val settings = mContext.getSharedPreferences(mPrefName, Context.MODE_PRIVATE)
        return settings.getBoolean(key, defaultValue)
    }

    override fun hasKey(key: String): Boolean {
        return mContext.getSharedPreferences(mPrefName, Context.MODE_PRIVATE)
                .contains(key)
    }

    override fun setPrefBoolean(key: String, value: Boolean) {
        val settings = mContext.getSharedPreferences(mPrefName, Context.MODE_PRIVATE)
        settings.edit().putBoolean(key, value).apply()
    }

    override fun setPrefInt(key: String, value: Int) {
        val settings = mContext.getSharedPreferences(mPrefName, Context.MODE_PRIVATE)
        settings.edit().putInt(key, value).apply()
    }

    fun increasePrefInt(key: String) {
        val settings = mContext.getSharedPreferences(mPrefName, Context.MODE_PRIVATE)
        increasePrefInt(settings, key)
    }

    fun increasePrefInt(sp: SharedPreferences, key: String) {
        val v = sp.getInt(key, 0) + 1
        sp.edit().putInt(key, v).apply()
    }

    fun increasePrefInt(sp: SharedPreferences, key: String,
                        increment: Int) {
        val v = sp.getInt(key, 0) + increment
        sp.edit().putInt(key, v).apply()
    }

    override fun getPrefInt(key: String, defaultValue: Int): Int {
        val settings = mContext.getSharedPreferences(mPrefName, Context.MODE_PRIVATE)
        return settings.getInt(key, defaultValue)
    }

    override fun setPrefFloat(key: String, value: Float) {
        val settings = mContext.getSharedPreferences(mPrefName, Context.MODE_PRIVATE)
        settings.edit().putFloat(key, value).apply()
    }

    override fun getPrefFloat(key: String, defaultValue: Float): Float {
        val settings = mContext.getSharedPreferences(mPrefName, Context.MODE_PRIVATE)
        return settings.getFloat(key, defaultValue)
    }

    override fun setPrefLong(key: String, value: Long) {
        val settings = mContext.getSharedPreferences(mPrefName, Context.MODE_PRIVATE)
        settings.edit().putLong(key, value).apply()
    }

    override fun getPrefLong(key: String, defaultValue: Long): Long {
        val settings = mContext.getSharedPreferences(mPrefName, Context.MODE_PRIVATE)
        return settings.getLong(key, defaultValue)
    }


    override fun removePreference(key: String) {
        val prefs = mContext.getSharedPreferences(mPrefName, Context.MODE_PRIVATE)
        prefs.edit().remove(key).apply()
    }

    fun clearPreference(p: SharedPreferences) {
        val editor = p.edit()
        editor.clear()
        editor.apply()
    }

}
