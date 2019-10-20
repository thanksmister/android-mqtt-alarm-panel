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

/**
 * Created by wangyida on 15-4-9.
 */
class DPreference {

    lateinit var mContext: Context

    /**
     * preference file name
     */
    lateinit var mName: String

    private constructor() {}

    constructor(context: Context, name: String) {
        this.mContext = context
        this.mName = name
    }

    fun getPrefString(key: String, defaultValue: String): String {
        return PrefAccessor.getString(mContext, mName, key, defaultValue)
    }

    fun setPrefString(key: String, value: String) {
        PrefAccessor.setString(mContext, mName, key, value)
    }

    fun getPrefBoolean(key: String, defaultValue: Boolean): Boolean {
        return PrefAccessor.getBoolean(mContext, mName, key, defaultValue)
    }

    fun setPrefBoolean(key: String, value: Boolean) {
        PrefAccessor.setBoolean(mContext, mName, key, value)
    }

    fun setPrefInt(key: String, value: Int) {
        PrefAccessor.setInt(mContext, mName, key, value)
    }

    fun getPrefInt(key: String, defaultValue: Int): Int {
        return PrefAccessor.getInt(mContext, mName, key, defaultValue)
    }

    fun setPrefLong(key: String, value: Long) {
        PrefAccessor.setLong(mContext, mName, key, value)
    }

    fun getPrefLong(key: String, defaultValue: Long): Long {
        return PrefAccessor.getLong(mContext, mName, key, defaultValue)
    }

    fun removePreference(key: String) {
        PrefAccessor.remove(mContext, mName, key)
    }
}