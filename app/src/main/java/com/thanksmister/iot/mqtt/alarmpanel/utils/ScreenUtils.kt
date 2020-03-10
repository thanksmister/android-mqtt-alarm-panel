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

    fun setScreenBrightness() {
        Timber.d("resetScreenBrightness useScreenBrightness ${configuration.useScreenBrightness}")
        if(configuration.useScreenBrightness) {
            Timber.d("resetScreenBrightness")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && canWriteScreenSetting()) {
                setDeviceBrightnessControl()
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !canWriteScreenSetting()) {
                restoreDeviceBrightnessControl()
            } else {
                setDeviceBrightnessControl()
            }
        } else {
            Timber.d("resetScreenBrightness ignored")
            restoreDeviceBrightnessControl()
        }
    }

    fun getCurrentScreenBrightness(): Int {
        return try {
            Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS)
        } catch (e: Exception) {
            Timber.d("get current brightness error ${e.message}")
            0
        }
    }

    fun hasScreenBrightnessMode():Boolean {
       return configuration.hasScreenSaver() || configuration.useNightDayMode
    }

    /**
     *  We do not use this if we already have night mode on, it will handle dimming and no additional dimming occurs
     *  when screensaver is active.  But if night mode is not active and user has given brightness permission,
     *  we do some auto dimming for better experience.
     */
   fun setScreenSaverBrightness(screenSaver: Boolean) {
        if(hasNightMode() && !tisTheDay()) {
            return
        }
        //setDeviceBrightnessMode(false)
        try {
            if (configuration.screenBrightness in 1..255 && !screenSaver) {
                Timber.d("screenSaver $screenSaver")
                Timber.d("brightness ${configuration.screenBrightness}")
                val dimAmount = configuration.screenBrightness
                Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, dimAmount)
            } else if (configuration.screenNightModeBrightness in 1..255 && configuration.screenBrightness in 1..255 && screenSaver) {
                Timber.d("screenSaver $screenSaver")
                val dimAmount = (configuration.screenBrightness * .75).toInt()
                Timber.d("calculated brightness $dimAmount")
                Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, dimAmount)
            }
        } catch (e: SecurityException) {
            Timber.e(e.message)
        }
    }

    fun setScreenBrightnessLevels() {
        Timber.d("setScreenBrightnessLevels")
        try {
            val brightness = getCurrentScreenBrightness()
            updateScreenBrightness(brightness)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateScreenBrightness(brightness: Int) {
        Timber.d("setScreenBrightness $brightness")
        if(canWriteScreenSetting()) {
            try {
                if (brightness in 1..255) {
                    Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, brightness)
                    configuration.screenBrightness = brightness
                    Timber.d("screenBrightness $brightness")
                    Timber.d("screenSaverDimValue ${configuration.nightModeDimValue}")
                    if(configuration.nightModeDimValue > 0) {
                        val dimAmount = brightness - (brightness * configuration.nightModeDimValue/100)
                        configuration.screenNightModeBrightness = dimAmount
                    } else {
                        configuration.screenNightModeBrightness = brightness
                    }
                    Timber.d("screenScreenSaverBrightness ${configuration.screenNightModeBrightness}")
                }
            } catch (e: SecurityException) {
                Timber.e(e.message)
            }
        }
    }

    // The user no longer has screen write permission or has chosen to not use this permission
    // we want reset device to automatic mode and reset the screen brightness to the last brightens settings
    // we also want to stop using screen brightness
    fun restoreDeviceBrightnessControl() {
        if(canWriteScreenSetting()) {
            Timber.d("restoreDeviceBrightnessControl")
            configuration.useScreenBrightness = false
            Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, getCurrentScreenBrightness())
            configuration.screenBrightness = getCurrentScreenBrightness()
            try {
                if(configuration.nightModeDimValue > 0) {
                    val dimAmount = configuration.screenBrightness - (configuration.screenBrightness * configuration.nightModeDimValue/100)
                    Timber.d("dimAmount $dimAmount")
                    configuration.screenNightModeBrightness = dimAmount
                } else {
                    configuration.screenNightModeBrightness = configuration.screenBrightness
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            setDeviceBrightnessMode(true)
        }
    }

    private fun hasNightMode():Boolean {
        Timber.d("hasNightMode ${configuration.useNightDayMode}")
        return configuration.useNightDayMode
    }

    private fun setDeviceBrightnessControl() {
        setDeviceBrightnessMode(false)
        try {
            Timber.d("configuration.dayNightMode: ${configuration.dayNightMode}")
            if (configuration.screenBrightness in 1..255 && configuration.dayNightMode == Configuration.SUN_ABOVE_HORIZON) {
                Timber.d("saved brightness ${configuration.screenBrightness}")
                val dimAmount = configuration.screenBrightness
                Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, dimAmount)
            } else if (configuration.screenNightModeBrightness in 1..255 && configuration.dayNightMode == Configuration.SUN_BELOW_HORIZON) {
                val dimAmount = configuration.screenNightModeBrightness
                Timber.d("calculated brightness ${configuration.screenNightModeBrightness}")
                Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, dimAmount)
            }
        } catch (e: SecurityException) {
            Timber.e(e.message)
        }
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

    private fun canWriteScreenSetting(): Boolean {
        Timber.d("canWriteScreenSetting ")
        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.System.canWrite(applicationContext)) {
            Timber.d("canWriteScreenSetting true")
            return true
        } else if (Build.VERSION.SDK_INT  < Build.VERSION_CODES.M ) {
            Timber.d("canWriteScreenSetting true")
            return true
        }
        Timber.d("canWriteScreenSetting false")
        return false
    }
}