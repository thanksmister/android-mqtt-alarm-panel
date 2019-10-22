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

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import androidx.collection.ArrayMap

import android.text.TextUtils

/**
 * Created by wangyida on 15/12/18.
 */
class PreferenceProvider : ContentProvider() {

    override fun onCreate(): Boolean {
        return true
    }

    override fun query(uri: Uri, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?, sortOrder: String?): Cursor? {
        var cursor: MatrixCursor? = null
        val model = getPrefModelByUri(uri)
        when (sUriMatcher.match(uri)) {
            PREF_BOOLEAN -> if (getDPreference(model.name)!!.hasKey(model.key)) {
                cursor = preferenceToCursor(if (getDPreference(model.name)!!.getPrefBoolean(model.key, false)) 1 else 0)
            }
            PREF_STRING -> if (getDPreference(model.name)!!.hasKey(model.key)) {
                cursor = preferenceToCursor(getDPreference(model.name)!!.getPrefString(model.key, ""))
            }
            PREF_INT -> if (getDPreference(model.name)!!.hasKey(model.key)) {
                cursor = preferenceToCursor(getDPreference(model.name)!!.getPrefInt(model.key, -1))
            }
            PREF_LONG -> if (getDPreference(model.name)!!.hasKey(model.key)) {
                cursor = preferenceToCursor(getDPreference(model.name)!!.getPrefLong(model.key, -1))
            }
        }
        return cursor
    }

    override fun getType(uri: Uri): String? {
        return null
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        throw IllegalStateException("insert unsupport!!!")
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        when (sUriMatcher.match(uri)) {
            PREF_BOOLEAN, PREF_LONG, PREF_STRING, PREF_INT -> {
                val model = getPrefModelByUri(uri)
                if (model != null) {
                    getDPreference(model.name)!!.removePreference(model.key)
                }
            }
            else -> throw IllegalStateException(" unsupported uri : $uri")
        }
        return 0
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int {
        val model = getPrefModelByUri(uri)
                ?: throw IllegalArgumentException("update prefModel is null")
        when (sUriMatcher.match(uri)) {
            PREF_BOOLEAN -> persistBoolean(model.name, values)
            PREF_LONG -> persistLong(model.name, values)
            PREF_STRING -> persistString(model.name, values)
            PREF_INT -> persistInt(model.name, values)
            else -> throw IllegalStateException("update unsupported uri : $uri")
        }
        return 0
    }

    private fun <T> preferenceToCursor(value: T): MatrixCursor {
        val matrixCursor = MatrixCursor(PREFERENCE_COLUMNS, 1)
        val builder = matrixCursor.newRow()
        builder.add(value)
        return matrixCursor
    }

    private fun persistInt(name: String, values: ContentValues?) {
        requireNotNull(values) { " values is null!!!" }
        val kInteger = values.getAsString(PREF_KEY)
        val vInteger = values.getAsInteger(PREF_VALUE)!!
        getDPreference(name)!!.setPrefInt(kInteger, vInteger)
    }

    private fun persistBoolean(name: String, values: ContentValues?) {
        requireNotNull(values) { " values is null!!!" }
        val kBoolean = values.getAsString(PREF_KEY)
        val vBoolean = values.getAsBoolean(PREF_VALUE)!!
        getDPreference(name)!!.setPrefBoolean(kBoolean, vBoolean)
    }

    private fun persistLong(name: String, values: ContentValues?) {
        requireNotNull(values) { " values is null!!!" }
        val kLong = values.getAsString(PREF_KEY)
        val vLong = values.getAsLong(PREF_VALUE)!!
        getDPreference(name)!!.setPrefLong(kLong, vLong)
    }

    private fun persistString(name: String, values: ContentValues?) {
        requireNotNull(values) { " values is null!!!" }
        val kString = values.getAsString(PREF_KEY)
        val vString = values.getAsString(PREF_VALUE)
        getDPreference(name)!!.setPrefString(kString, vString)
    }

    private fun getDPreference(name: String): IPrefImpl? {
        require(!TextUtils.isEmpty(name)) { "getDPreference name is null!!!" }
        if (sPreferences[name] == null) {
            val pref = PreferenceImpl(context!!, name)
            sPreferences[name] = pref
        }
        return sPreferences[name]
    }

    private fun getPrefModelByUri(uri: Uri?): PrefModel {
        require(!(uri == null || uri.pathSegments.size != 3)) { "getPrefModelByUri uri is wrong : " + uri!! }
        val name = uri.pathSegments[1]
        val key = uri.pathSegments[2]
        return PrefModel(name, key)
    }

    private class PrefModel(name: String, key: String) {
        var name: String
            internal set

        var key: String
            internal set

        init {
            this.name = name
            this.key = key
        }
    }

    companion object {

        private val TAG = PreferenceProvider::class.java.simpleName

        private val AUTHORITY = "com.thanksmister.iot.mqtt.alarmpanel.dpreference.PreferenceProvider"

        val CONTENT_PREF_BOOLEAN_URI = "content://$AUTHORITY/boolean/"
        val CONTENT_PREF_STRING_URI = "content://$AUTHORITY/string/"
        val CONTENT_PREF_INT_URI = "content://$AUTHORITY/integer/"
        val CONTENT_PREF_LONG_URI = "content://$AUTHORITY/long/"


        val PREF_KEY = "key"
        val PREF_VALUE = "value"

        val PREF_BOOLEAN = 1
        val PREF_STRING = 2
        val PREF_INT = 3
        val PREF_LONG = 4

        private val sUriMatcher: UriMatcher

        init {
            sUriMatcher = UriMatcher(UriMatcher.NO_MATCH)
            sUriMatcher.addURI(AUTHORITY, "boolean/*/*", PREF_BOOLEAN)
            sUriMatcher.addURI(AUTHORITY, "string/*/*", PREF_STRING)
            sUriMatcher.addURI(AUTHORITY, "integer/*/*", PREF_INT)
            sUriMatcher.addURI(AUTHORITY, "long/*/*", PREF_LONG)

        }

        private val PREFERENCE_COLUMNS = arrayOf(PREF_VALUE)

        private val sPreferences = ArrayMap<String, IPrefImpl>()


        fun buildUri(name: String, key: String, type: Int): Uri {
            return Uri.parse(getUriByType(type) + name + "/" + key)
        }

        private fun getUriByType(type: Int): String {
            when (type) {
                PreferenceProvider.PREF_BOOLEAN -> return PreferenceProvider.CONTENT_PREF_BOOLEAN_URI
                PreferenceProvider.PREF_INT -> return PreferenceProvider.CONTENT_PREF_INT_URI
                PreferenceProvider.PREF_LONG -> return PreferenceProvider.CONTENT_PREF_LONG_URI
                PreferenceProvider.PREF_STRING -> return PreferenceProvider.CONTENT_PREF_STRING_URI
            }
            throw IllegalStateException("unsupport preftype : $type")
        }
    }

}
