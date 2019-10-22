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

package com.thanksmister.iot.mqtt.alarmpanel.utils

import android.content.Context
import com.thanksmister.iot.mqtt.alarmpanel.R

object WeatherUtils {

    fun getIconForWeatherCondition(condition: String?): Int {
        when (condition) {
            "sunny", "exceptional" -> return R.drawable.ic_clear_day
            "clear-night" -> return R.drawable.ic_clear_night
            "lightning", "lightning-rainy" -> return R.drawable.ic_thunderstorm
            "snowy-rainy" -> return R.drawable.ic_sleet
            "partlycloudy" -> return R.drawable.ic_partly_cloudy_day
            "cloudy" -> return R.drawable.ic_cloudy
            "fog" -> return R.drawable.ic_fog
            "hail" -> return R.drawable.ic_hail
            "snow" -> return R.drawable.ic_snow
            "windy-variant", "windy" -> return R.drawable.ic_wind
            "pouring", "rainy" -> return R.drawable.ic_rain
            else -> return 0
        }
    }

    fun getOutlookForWeatherCondition(condition: String?, context:Context): String {
        when (condition) {
            "exceptional" -> return context.getString(R.string.weather_beautiful)
            "sunny" -> return context.getString(R.string.weather_sunny)
            "clear-night" -> return context.getString(R.string.weather_clear_night)
            "lightning", "lightning-rainy" -> return context.getString(R.string.weather_stormy)
            "snowy-rainy" -> return context.getString(R.string.weather_sleet)
            "partlycloudy" -> return context.getString(R.string.weather_partly_cloudy)
            "cloudy" -> return context.getString(R.string.weather_cloudy)
            "fog" -> return context.getString(R.string.weather_fog) 
            "hail" -> return context.getString(R.string.weather_hail)   
            "snow" -> return context.getString(R.string.weather_snow)   
            "windy-variant", "windy" -> return context.getString(R.string.weather_windy)
            "rainy" -> return context.getString(R.string.weather_rain)  
            "pouring"-> return context.getString(R.string.weather_pouring_rain)
            else -> return ""
        }
    }
}
