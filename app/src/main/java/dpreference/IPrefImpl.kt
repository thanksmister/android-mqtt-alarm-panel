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


/**
 * Created by wangyida on 15/12/18.
 */
internal interface IPrefImpl {

    fun getPrefString(key: String, defaultValue: String): String

    fun setPrefString(key: String, value: String)

    fun getPrefBoolean(key: String, defaultValue: Boolean): Boolean

    fun setPrefBoolean(key: String, value: Boolean)

    fun setPrefInt(key: String, value: Int)

    fun getPrefInt(key: String, defaultValue: Int): Int

    fun setPrefFloat(key: String, value: Float)

    fun getPrefFloat(key: String, defaultValue: Float): Float

    fun setPrefLong(key: String, value: Long)

    fun getPrefLong(key: String, defaultValue: Long): Long

    fun removePreference(key: String)

    fun hasKey(key: String): Boolean

}
