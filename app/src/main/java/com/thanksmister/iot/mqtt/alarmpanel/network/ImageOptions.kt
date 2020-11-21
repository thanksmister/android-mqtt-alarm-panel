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

package com.thanksmister.iot.mqtt.alarmpanel.network

import android.content.SharedPreferences
import android.text.TextUtils

import javax.inject.Inject

/**
 * For original implementation see https://github.com/androidthings/sensorhub-cloud-iot.
 */
class ImageOptions @Inject
constructor(private val sharedPreferences: SharedPreferences) {

    val isValid: Boolean
        get() = !TextUtils.isEmpty(imageSource) && !TextUtils.isEmpty(imageClientId)

    var imageClientId: String?
        get() = sharedPreferences.getString(PREF_IMAGE_CLIENT_ID, "").orEmpty()
        set(value) = this.sharedPreferences.edit().putString(PREF_IMAGE_CLIENT_ID, value.orEmpty()).apply()

    var imageRotation: Int
        get() = sharedPreferences.getInt(PREF_IMAGE_ROTATION, ROTATE_TIME_IN_MINUTES)
        set(value) = this.sharedPreferences.edit().putInt(PREF_IMAGE_ROTATION, value).apply()

    var imageFitScreen: Boolean
        get() = sharedPreferences.getBoolean(PREF_IMAGE_FIT_SIZE, false)
        set(value) = this.sharedPreferences.edit().putBoolean(PREF_IMAGE_FIT_SIZE, value).apply()

    var imageSource: String
        get() = sharedPreferences.getString(PREF_IMAGE_SOURCE, "landscape").orEmpty()
        set(value) = this.sharedPreferences.edit().putString(PREF_IMAGE_SOURCE, value).apply()

    private fun setOptionsUpdated(value: Boolean) {
        this.sharedPreferences.edit().putBoolean(IMAGE_OPTIONS_UPDATED, value).apply()
    }

    fun hasUpdates(): Boolean {
        val updates = sharedPreferences.getBoolean(IMAGE_OPTIONS_UPDATED, false)
        if (updates) {
            setOptionsUpdated(false)
        }
        return updates
    }

    companion object {
        const val PREF_IMAGE_SOURCE = "pref_image_source"
        const val PREF_IMAGE_FIT_SIZE = "pref_image_fit"
        const val PREF_IMAGE_ROTATION = "pref_image_rotation"
        const val PREF_IMAGE_CLIENT_ID = "pref_image_client_id"
        private val IMAGE_OPTIONS_UPDATED = "pref_image_options_updated"
        private val ROTATE_TIME_IN_MINUTES = 30 // 30 minutes
    }
}