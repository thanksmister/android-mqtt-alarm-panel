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

import android.text.TextUtils

import dpreference.DPreference
import javax.inject.Inject

/**
 * For original implementation see https://github.com/androidthings/sensorhub-cloud-iot.
 */
class ImageOptions @Inject
constructor(private val sharedPreferences: DPreference) {

    val isValid: Boolean
        get() = !TextUtils.isEmpty(imageSource) && !TextUtils.isEmpty(imageClientId)

    var imageClientId: String?
        get() = sharedPreferences.getPrefString(PREF_IMAGE_CLIENT_ID, "")
        set(value) = this.sharedPreferences.setPrefString(PREF_IMAGE_CLIENT_ID, value.orEmpty())

    var imageRotation: Int
        get() = sharedPreferences.getPrefInt(PREF_IMAGE_ROTATION, ROTATE_TIME_IN_MINUTES)
        set(value) = this.sharedPreferences.setPrefInt(PREF_IMAGE_ROTATION, value)

    var imageFitScreen: Boolean
        get() = sharedPreferences.getPrefBoolean(PREF_IMAGE_FIT_SIZE, false)
        set(value) = this.sharedPreferences.setPrefBoolean(PREF_IMAGE_FIT_SIZE, value)

    var imageSource: String
        get() = sharedPreferences.getPrefString(PREF_IMAGE_SOURCE, "landscape")
        set(value) = this.sharedPreferences.setPrefString(PREF_IMAGE_SOURCE, value)

    private fun setOptionsUpdated(value: Boolean) {
        this.sharedPreferences.setPrefBoolean(IMAGE_OPTIONS_UPDATED, value)
    }

    fun hasUpdates(): Boolean {
        val updates = sharedPreferences.getPrefBoolean(IMAGE_OPTIONS_UPDATED, false)
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

        /**
         * Construct an object from Configuration.
         */
        /*fun from(sharedPreferences: DPreference): ImageOptions {
            try {
                val options = ImageOptions(sharedPreferences)
                options.imageSource = sharedPreferences.getPrefString(PREF_IMAGE_SOURCE, "landscape")
                options.clientId = sharedPreferences.getPrefString(PREF_IMAGE_CLIENT_ID, null)
                options.fitScreen = sharedPreferences.getPrefBoolean(PREF_IMAGE_FIT_SIZE, false)
                options.rotation = sharedPreferences.getPrefInt(PREF_IMAGE_ROTATION, ROTATE_TIME_IN_MINUTES)
                return options
            } catch (e: Exception) {
                throw IllegalArgumentException("While processing image options", e)
            }

        }*/
    }
}