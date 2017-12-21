package com.thanksmister.iot.mqtt.alarmpanel.network

import android.text.TextUtils

import dpreference.DPreference

/**
 * For original implementation see https://github.com/androidthings/sensorhub-cloud-iot.
 */
class DarkSkyOptions private constructor(

    /**
     * Preferences.
     */
    private val sharedPreferences: DPreference) {

    /**
     * Port number.
     */
    private var isCelsius: Boolean = false

    /**
     * Dark Sky key.
     */
    private var key: String? = null

    /**
     * Longitude.
     */
    var longitude: String? = null
        private set

    /**
     * Latitude.
     */
    var latitude: String? = null
        private set

    /**
     * Units.
     */
    var weatherUnits: String? = null
        private set

    val isValid: Boolean
        get() = !TextUtils.isEmpty(latitude) &&
                !TextUtils.isEmpty(longitude) &&
                !TextUtils.isEmpty(weatherUnits) &&
                !TextUtils.isEmpty(key)

    var darkSkyKey: String?
        get() = key
        set(value) {
            this.sharedPreferences.setPrefString(PREF_DARK_SKY_KEY, value)
            setOptionsUpdated(true)
        }

    fun setLon(longitude: String) {
        this.sharedPreferences.setPrefString(PREF_WEATHER_LON, longitude)
    }

    fun setLat(latitude: String) {
        this.sharedPreferences.setPrefString(PREF_WEATHER_LAT, latitude)
    }

    fun getIsCelsius(): Boolean {
        return isCelsius
    }

    fun setIsCelsius(isCelsius: Boolean) {
        this.isCelsius = isCelsius
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
        fun from(sharedPreferences: DPreference): DarkSkyOptions {
            try {
                val options = DarkSkyOptions(sharedPreferences)
                options.key = sharedPreferences.getPrefString(PREF_DARK_SKY_KEY, null)
                options.latitude = sharedPreferences.getPrefString(PREF_WEATHER_LAT, null)
                options.longitude = sharedPreferences.getPrefString(PREF_WEATHER_LON, null)
                options.isCelsius = sharedPreferences.getPrefString(PREF_WEATHER_UNITS, DarkSkyRequest.UNITS_US) == DarkSkyRequest.UNITS_SI
                options.weatherUnits = sharedPreferences.getPrefString(PREF_WEATHER_UNITS, DarkSkyRequest.UNITS_US)
                return options
            } catch (e: Exception) {
                throw IllegalArgumentException("While processing weather options", e)
            }

        }
    }
}