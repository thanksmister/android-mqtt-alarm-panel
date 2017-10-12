package com.thanksmister.iot.mqtt.alarmpanel.network;

import android.text.TextUtils;

import dpreference.DPreference;

/**
 * For original implementation see https://github.com/androidthings/sensorhub-cloud-iot.
 */
public class InstagramOptions {

    public static final String PREF_IMAGE_SOURCE = "pref_image_source";
    public static final String PREF_IMAGE_FIT_SIZE = "pref_image_fit";
    public static final String PREF_IMAGE_ROTATION = "pref_image_rotation";
    
    private static final String IMAGE_OPTIONS_UPDATED = "pref_image_options_updated";
    private static final int ROTATE_TIME_IN_MINUTES = 30; // 30 minutes
    
    /**
     * Fit image to screen.
     */
    private boolean fitScreen;
    
    /**
     * Image source.
     */
    private String imageSource;
    
    /**
     * Rotation interval.
     */
    private int rotation;

    /**
     * Preferences.
     */
    private final DPreference sharedPreferences;
    
    private InstagramOptions(DPreference sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    public boolean isValid() {
        return !TextUtils.isEmpty(imageSource);
    }
    
    /**
     * Construct a MqttOptions object from Configuration.
     */
    public static InstagramOptions from(DPreference sharedPreferences) {
        try {
            InstagramOptions options = new InstagramOptions(sharedPreferences);
            options.imageSource = sharedPreferences.getPrefString(PREF_IMAGE_SOURCE, "omjsk");
            options.fitScreen = sharedPreferences.getPrefBoolean(PREF_IMAGE_FIT_SIZE, false);
            options.rotation = sharedPreferences.getPrefInt(PREF_IMAGE_ROTATION, ROTATE_TIME_IN_MINUTES);
            return options;
        } catch (Exception e) {
            throw new IllegalArgumentException("While processing image options", e);
        }
    }
    
    public String getImageSource() {
        return imageSource;
    }

    public int getImageRotation() {
        return rotation;
    }

    public boolean getImageFitScreen() {
        return fitScreen;
    }

    public void setImageSource(String value) {
        this.sharedPreferences.setPrefString(PREF_IMAGE_SOURCE, value);
    }

    public void setImageRotation(int value) {
        this.sharedPreferences.setPrefInt(PREF_IMAGE_ROTATION, value);
    }

    public void setImageFitScreen(boolean value) {
        this.sharedPreferences.setPrefBoolean(PREF_IMAGE_FIT_SIZE, value);
    }

    private void setOptionsUpdated(boolean value) {
        this.sharedPreferences.setPrefBoolean(IMAGE_OPTIONS_UPDATED, value);
    }
    
    public boolean hasUpdates() {
        boolean updates = sharedPreferences.getPrefBoolean(IMAGE_OPTIONS_UPDATED, false);
        if(updates) {
            setOptionsUpdated(false);
        }
        return updates;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof InstagramOptions)) {
            return false;
        }
        InstagramOptions o = (InstagramOptions) obj;
        return TextUtils.equals(imageSource , o.imageSource)
                && fitScreen == o.fitScreen
                && rotation == o.rotation;
    }
}