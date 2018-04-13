/*
 * Copyright (c) 2017. ThanksMister LLC
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

package com.thanksmister.iot.mqtt.alarmpanel.network.fetchers;

import android.support.annotation.NonNull;

import com.thanksmister.iot.mqtt.alarmpanel.network.DarkSkyApi;
import com.thanksmister.iot.mqtt.alarmpanel.network.model.DarkSkyResponse;

import java.util.Locale;

import io.reactivex.Observable;
import retrofit2.Call;

public class DarkSkyFetcher {
    
    private final DarkSkyApi networkApi;

    public DarkSkyFetcher(@NonNull DarkSkyApi networkApi) {
        this.networkApi = networkApi;
    }
    
    public Call<DarkSkyResponse> getFeedData(final String apiKey, final String units, final String lat, final String lon) {
        String excludes = "hourly,minutely,flags,alerts";
        return networkApi.getHourlyForecast(apiKey, lat, lon, excludes, units, Locale.getDefault().getLanguage());
    }

    /*public Call<DarkSkyResponse> getExtendedFeedData(final String apiKey, final String units, final String lat, final String lon) {
        String excludes = "hourly,minutely,flags,alerts";
        String extended = "daily";
        return networkApi.getExtendedForecast(apiKey, lat, lon, excludes, extended, units, Locale.getDefault().getLanguage());
    }
*/
    public Observable<DarkSkyResponse> getExtendedFeedData(final String apiKey, final String units, final String lat, final String lon) {
        String excludes = "hourly,minutely,flags,alerts";
        String extended = "daily";
        return networkApi.getExtendedForecast(apiKey, lat, lon, excludes, extended, units, Locale.getDefault().getLanguage());
    }
}