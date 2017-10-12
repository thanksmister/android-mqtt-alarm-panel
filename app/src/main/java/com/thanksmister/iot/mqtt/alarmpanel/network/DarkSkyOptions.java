package com.thanksmister.iot.mqtt.alarmpanel.network;

import android.text.TextUtils;

import dpreference.DPreference;

/**
 * For original implementation see https://github.com/androidthings/sensorhub-cloud-iot.
 */
public class DarkSkyOptions {
    
    public static final String PREF_WEATHER_UNITS = "pref_weather_units";
    public static final String PREF_DARK_SKY_KEY = "pref_dark_sky_key";
    public static final String PREF_WEATHER_LAT = "pref_weather_lat";
    public static final String PREF_WEATHER_LON = "pref_weather_lon";
    
    private static final String WEATHER_OPTIONS_UPDATED = "pref_weather_options_updated";
    
    /**
     * Port number.
     */
    private boolean isCelsius;

    /**
     * Dark Sky key.
     */
    private String key;

    /**
     * Longitude.
     */
    private String longitude;

    /**
     * Latitude.
     */
    private String latitude;

    /**
     * Units.
     */
    private String units;

    /**
     * Preferences.
     */
    private final DPreference sharedPreferences;
    
    private DarkSkyOptions(DPreference sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    public boolean isValid() {
        return !TextUtils.isEmpty(latitude) &&
                !TextUtils.isEmpty(longitude) &&
                !TextUtils.isEmpty(units) &&
                !TextUtils.isEmpty(key);
    }
    
    /**
     * Construct a MqttOptions object from Configuration.
     */
    public static DarkSkyOptions from(DPreference sharedPreferences) {
        try {
            DarkSkyOptions options = new DarkSkyOptions(sharedPreferences);
            options.key = sharedPreferences.getPrefString(PREF_DARK_SKY_KEY, null);
            options.latitude = sharedPreferences.getPrefString(PREF_WEATHER_LAT, null);
            options.longitude = sharedPreferences.getPrefString(PREF_WEATHER_LON, null);
            options.isCelsius = sharedPreferences.getPrefBoolean(MQTTOptions.PREF_TLS_CONNECTION, false);
            options.units = sharedPreferences.getPrefString(PREF_WEATHER_UNITS, DarkSkyRequest.UNITS_US);
            return options;
        } catch (Exception e) {
            throw new IllegalArgumentException("While processing weather options", e);
        }
    }

    public void setLon(String longitude) {
        this.sharedPreferences.setPrefString(PREF_WEATHER_LON, longitude);
    }

    public void setLat(String latitude) {
        this.sharedPreferences.setPrefString(PREF_WEATHER_LAT, latitude);
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public boolean getIsCelsius() {
        return isCelsius;
    }

    public String getWeatherUnits() {
        return units;
    }

    public String getDarkSkyKey() {
        return key;
    }

    public void setIsCelsius(boolean isCelsius) {
        sharedPreferences.setPrefString(PREF_WEATHER_UNITS, isCelsius ? DarkSkyRequest.UNITS_SI : DarkSkyRequest.UNITS_US);
        setOptionsUpdated(true);
    }

    public void setDarkSkyKey(String value) {
        this.sharedPreferences.setPrefString(PREF_DARK_SKY_KEY, value);
        setOptionsUpdated(true);
    }

    private void setOptionsUpdated(boolean value) {
        this.sharedPreferences.setPrefBoolean(WEATHER_OPTIONS_UPDATED, value);
    }

    public boolean hasUpdates() {
        boolean updates = sharedPreferences.getPrefBoolean(WEATHER_OPTIONS_UPDATED, false);
        if(updates) {
            setOptionsUpdated(false);
        }
        return updates;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof DarkSkyOptions)) {
            return false;
        }
        DarkSkyOptions o = (DarkSkyOptions) obj;
        return TextUtils.equals(key , o.key)
                && TextUtils.equals(units, o.units)
                && TextUtils.equals(latitude, o.latitude)
                && TextUtils.equals(longitude, o.longitude)
                && isCelsius == o.isCelsius;
    }
}