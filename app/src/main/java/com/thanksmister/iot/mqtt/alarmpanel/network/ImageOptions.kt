package com.thanksmister.iot.mqtt.alarmpanel.network

import android.text.TextUtils

import dpreference.DPreference

/**
 * For original implementation see https://github.com/androidthings/sensorhub-cloud-iot.
 */
class ImageOptions private constructor(
        /**
         * Preferences.
         */
        private val sharedPreferences: DPreference) {

    /**
     * Fit image to screen.
     */
    private var fitScreen: Boolean = false

    /**
     * Image source.
     */
    private var imageSource: String? = null

    /**
     * Client Id.
     */
    private var clientId: String? = null

    /**
     * Rotation interval.
     */
    private var rotation: Int = 0

    val isValid: Boolean
        get() = !TextUtils.isEmpty(imageSource) && !TextUtils.isEmpty(imageClientId)

    var imageClientId: String?
        get() = clientId
        set(value) = this.sharedPreferences.setPrefString(PREF_IMAGE_CLIENT_ID, value)

    var imageRotation: Int
        get() = rotation
        set(value) = this.sharedPreferences.setPrefInt(PREF_IMAGE_ROTATION, value)

    var imageFitScreen: Boolean
        get() = fitScreen
        set(value) = this.sharedPreferences.setPrefBoolean(PREF_IMAGE_FIT_SIZE, value)

    fun getClientId(): String? {
        return clientId
    }

    fun setClientId(value: String) {
        this.sharedPreferences.setPrefString(PREF_IMAGE_CLIENT_ID, value)
    }

    fun getRotation(): Int {
        return imageRotation
    }

    fun setRotation(value: Int) {
        this.sharedPreferences.setPrefInt(PREF_IMAGE_ROTATION, value)
    }

    fun getTag(): String? {
        return imageSource
    }

    fun setTag(value: String) {
        this.sharedPreferences.setPrefString(PREF_IMAGE_SOURCE, value)
    }

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

        val PREF_IMAGE_SOURCE = "pref_image_source"
        val PREF_IMAGE_FIT_SIZE = "pref_image_fit"
        val PREF_IMAGE_ROTATION = "pref_image_rotation"
        val PREF_IMAGE_CLIENT_ID = "pref_image_client_id"

        private val IMAGE_OPTIONS_UPDATED = "pref_image_options_updated"
        private val ROTATE_TIME_IN_MINUTES = 30 // 30 minutes

        /**
         * Construct a MqttOptions object from Configuration.
         */
        fun from(sharedPreferences: DPreference): ImageOptions {
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

        }
    }
}