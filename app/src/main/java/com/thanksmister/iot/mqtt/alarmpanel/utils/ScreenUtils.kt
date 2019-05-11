/*
 * Copyright (c) 2019 ThanksMister LLC
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

package com.thanksmister.iot.mqtt.alarmpanel.utils

import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import android.provider.Settings
import com.thanksmister.iot.mqtt.alarmpanel.persistence.Configuration

import timber.log.Timber
import javax.inject.Inject

class ScreenUtils @Inject
constructor(context: Context, private val configuration: Configuration): ContextWrapper(context) {

    fun tisTheDay(): Boolean {
        val uiMode = resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK;
        if (uiMode == android.content.res.Configuration.UI_MODE_NIGHT_YES) {
            Timber.d("Tis the night!")
            return false
        } else if (uiMode == android.content.res.Configuration.UI_MODE_NIGHT_NO) {
            Timber.d("Tis the day!")
            return true
        }
        return true
    }

   fun resetScreenBrightness(isTheDay: Boolean = true) {
        Timber.d("resetScreenBrightness useScreenBrightness ${configuration.useScreenBrightness}")
        if(configuration.useScreenBrightness) {
            Timber.d("resetScreenBrightness")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.System.canWrite(applicationContext)) {
                setDeviceBrightnessControl(isTheDay)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.System.canWrite(applicationContext)) {
                restoreDeviceBrightnessControl()
            } else {
                setDeviceBrightnessControl(isTheDay)
            }
        } else {
            Timber.d("resetScreenBrightness ignored")
            restoreDeviceBrightnessControl()
        }
    }

    private fun setDeviceBrightnessControl(isTheDay: Boolean = true) {
        setDeviceBrightnessMode(false)
        try {
            Timber.d("resetScreenBrightness setting brightness without permission")
            Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL)
            if (isTheDay && configuration.screenBrightness in 1..255) {
                Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, configuration.screenBrightness)
                Timber.d("calculated brightness ${configuration.screenBrightness}")
            } else if (!isTheDay && configuration.screenNightBrightness in 1..255) {
                Timber.d("calculated brightness ${configuration.screenNightBrightness}")
                Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, configuration.screenNightBrightness)
            }
        } catch (e: SecurityException) {
            Timber.e(e.message)
        }
    }

    fun setScreenBrightnessLevels() {
        Timber.d("setScreenBrightnessLevels")
        try {
            val brightness = Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS)
            configuration.screenBrightness = brightness
            configuration.screenNightBrightness = (brightness * Configuration.PREF_BRIGHTNESS_FACTOR).toInt()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // The user no longer has screen write permission or has chosen to not use this permission
    fun restoreDeviceBrightnessControl() {
        Timber.d("resetScreenBrightness remove write permissions")
        setDeviceBrightnessMode(true)
        configuration.useScreenBrightness = false
        try {
            Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, configuration.screenBrightness)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        setScreenBrightnessLevels()
    }

    private fun setDeviceBrightnessMode(automatic: Boolean = false) {
        var mode = -1
        try {
            mode = Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE) //this will return integer (0 or 1)
        } catch (e: Settings.SettingNotFoundException) {
            Timber.e(e.message)
        }
        try {
            if(automatic) {
                if (mode == Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL) {
                    //reset back to automatic mode
                    Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC)
                }
            } else {
                if (mode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
                    //Automatic mode, need to be in manual to change brightness
                    Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL)
                }
            }
        } catch (e: SecurityException) {
            Timber.e(e.message)
        }
    }
}