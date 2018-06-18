package com.thanksmister.iot.mqtt.alarmpanel.network

import android.text.TextUtils

import dpreference.DPreference
import javax.inject.Inject

/**
 * For original implementation see https://github.com/androidthings/sensorhub-cloud-iot.
 */
class DarkSkyOptions @Inject
constructor(private val sharedPreferences: DPreference) {

    val isValid: Boolean
        get() = !TextUtils.isEmpty(latitude) &&
                !TextUtils.isEmpty(longitude) &&
                !TextUtils.isEmpty(weatherUnits) &&
                !TextUtils.isEmpty(darkSkyKey)

    var darkSkyKey: String?
        get() = sharedPreferences.getPrefString(PREF_DARK_SKY_KEY, null)
        set(value) {
            this.sharedPreferences.setPrefString(PREF_DARK_SKY_KEY, value)
            setOptionsUpdated(true)
        }

    var weatherUnits: String
        get() = sharedPreferences.getPrefString(PREF_WEATHER_UNITS, DarkSkyRequest.UNITS_US)
        set(value) = this.sharedPreferences.setPrefString(PREF_WEATHER_UNITS, value)

    var longitude: String
        get() = sharedPreferences.getPrefString(PREF_WEATHER_LON, null)
        set(value) = this.sharedPreferences.setPrefString(PREF_WEATHER_LON, value)

    var latitude: String
        get() = sharedPreferences.getPrefString(PREF_WEATHER_LAT, null)
        set(value) = this.sharedPreferences.setPrefString(PREF_WEATHER_LAT, value)

    fun isCelsius(): Boolean {
        return sharedPreferences.getPrefString(PREF_WEATHER_UNITS, DarkSkyRequest.UNITS_US) == DarkSkyRequest.UNITS_SI
    }

    fun setIsCelsius(isCelsius: Boolean) {
        sharedPreferences.setPrefString(PREF_WEATHER_UNITS, if (isCelsius) DarkSkyRequest.UNITS_SI else DarkSkyRequest.UNITS_US)
        setOptionsUpdated(true)
    }

    private fun setOptionsUpdated(value: Boolean) {
        this.sharedPreferences.setPrefBoolean(WEATHER_OPTIONS_UPDATED, value)
    }

    fun hasUpdates(): Boolean {
        val updates = sharedPreferences.getPrefBoolean(WEATHER_OPTIONS_UPDATED, false)
        if (updates) {
            setOptionsUpdated(false)
        }
        return updates
    }

    companion object {

        val PREF_WEATHER_UNITS = "pref_weather_units"
        val PREF_DARK_SKY_KEY = "pref_dark_sky_key"
        val PREF_WEATHER_LAT = "pref_weather_lat"
        val PREF_WEATHER_LON = "pref_weather_lon"

        private val WEATHER_OPTIONS_UPDATED = "pref_weather_options_updated"

        /**
         * Construct a MqttOptions object from Configuration.
         */
        /*fun from(sharedPreferences: DPreference): DarkSkyOptions {
            try {
                val options = DarkSkyOptions(sharedPreferences)
                options.key =
                options.latitude = sharedPreferences.getPrefString(PREF_WEATHER_LAT, null)
                options.longitude = sharedPreferences.getPrefString(PREF_WEATHER_LON, null)
                options.isCelsius = sharedPreferences.getPrefString(PREF_WEATHER_UNITS, DarkSkyRequest.UNITS_US) == DarkSkyRequest.UNITS_SI
                options.weatherUnits = sharedPreferences.getPrefString(PREF_WEATHER_UNITS, DarkSkyRequest.UNITS_US)
                return options
            } catch (e: Exception) {
                throw IllegalArgumentException("While processing weather options", e)
            }

        }*/
    }
}