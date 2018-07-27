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

package com.thanksmister.iot.mqtt.alarmpanel.utils;

import android.support.annotation.NonNull;
import android.text.TextUtils;

/**
 * Validate longitude and latitude coordinates
 */
public class LocationUtils {

    public static boolean latitudeValid(String latitude) {
        double lat = parseDouble(latitude, 0);
        return latitudeValid(lat);
    }

    public static boolean longitudeValid(String longitude) {
        double lon = parseDouble(longitude, 0);
        return longitudeValid(lon);
    }
    
    public static boolean coordinatesValid(String latitude, String longitude) {
        double lat = parseDouble(latitude, 0);
        double lon = parseDouble(longitude, 0);
        return coordinatesValid(lat, lon);
    }

    private static boolean coordinatesValid(double lat, double lon) {
        return (latitudeValid(lat) && longitudeValid(lon));
    }

    private static boolean latitudeValid(double lat) {
        return (lat != 0 && lat >= -90 && lat <= 90);
    }

    private static boolean longitudeValid(double lon) {
        return (lon != 0 && lon >= -180 && lon <= 180);
    }

    private static double parseDouble (@NonNull String value, double defaultValue) {
        if (!TextUtils.isEmpty(value)) {
            try {
                return Double.parseDouble(value);
            } catch (Exception e) {
                return defaultValue;
            }
        } else {
            return defaultValue;
        }
    }
}
