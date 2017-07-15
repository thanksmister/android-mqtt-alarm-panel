/*
 * <!--
 *   ~ Copyright (c) 2017. ThanksMister LLC
 *   ~
 *   ~ Licensed under the Apache License, Version 2.0 (the "License");
 *   ~ you may not use this file except in compliance with the License. 
 *   ~ You may obtain a copy of the License at
 *   ~
 *   ~ http://www.apache.org/licenses/LICENSE-2.0
 *   ~
 *   ~ Unless required by applicable law or agreed to in writing, software distributed 
 *   ~ under the License is distributed on an "AS IS" BASIS, 
 *   ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 *   ~ See the License for the specific language governing permissions and 
 *   ~ limitations under the License.
 *   -->
 */

package com.thanksmister.androidthings.iot.alarmpanel.utils;

import com.thanksmister.androidthings.iot.alarmpanel.R;

/**
 * Validate longitude and latitude coordinates
 */
public class WeatherUtils {

    public static int getIconForWeatherCondition(String condition) {
        switch (condition) {
            case "clear-day":
                return R.drawable.ic_action_clear_day;
            case "clear-night":
                return R.drawable.ic_action_clear_night;
            case "thunderstorm":
                return R.drawable.ic_action_thunderstorm;
            case "sleet":
                return R.drawable.ic_action_sleet;
            case "partly-cloudy-day":
                return R.drawable.ic_action_partly_cloudy_day;
            case "partly-cloudy-night":
                return R.drawable.ic_action_partly_cloudy_night;
            case "cloudy":
                return R.drawable.ic_action_cloudy;
            case "fog":
                return R.drawable.ic_action_fog;
            case "hail":
                return R.drawable.ic_action_hail;
            case "tornado":
                return R.drawable.ic_action_tornado;
            case "snow":
                return R.drawable.ic_action_snow;
            case "wind":
                return R.drawable.ic_action_wind;
            case "rain":
                return R.drawable.ic_action_rain;
            default:
                return 0;
        }
    }
}
