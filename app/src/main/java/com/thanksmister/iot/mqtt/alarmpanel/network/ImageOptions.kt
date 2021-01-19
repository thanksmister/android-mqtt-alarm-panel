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

    var imageRotation: Int
        get() = sharedPreferences.getInt(IMAGE_ROTATION_SETTINGS, ROTATE_TIME_IN_MINUTES)
        set(value) = this.sharedPreferences.edit().putInt(IMAGE_ROTATION_SETTINGS, value).apply()

    private fun setOptionsUpdated(value: Boolean) {
        this.sharedPreferences.edit().putBoolean(IMAGE_OPTIONS_UPDATED, value).apply()
    }

    companion object {
        private const val IMAGE_ROTATION_SETTINGS = "setting_image_rotation"
        private const val IMAGE_OPTIONS_UPDATED = "pref_image_options_updated"
        private const val ROTATE_TIME_IN_MINUTES = 30 // 30 minutes
    }
}