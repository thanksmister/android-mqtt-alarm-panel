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

package com.thanksmister.iot.mqtt.alarmpanel.network.fetchers;

import android.support.annotation.NonNull;

import com.thanksmister.iot.mqtt.alarmpanel.network.DarkSkyApi;
import com.thanksmister.iot.mqtt.alarmpanel.network.InstagramApi;
import com.thanksmister.iot.mqtt.alarmpanel.network.model.DarkSkyResponse;
import com.thanksmister.iot.mqtt.alarmpanel.network.model.InstagramResponse;

import java.util.Locale;

import retrofit2.Call;

public class InstagramFetcher {

    private final InstagramApi networkApi;

    public InstagramFetcher(@NonNull InstagramApi networkApi) {
        this.networkApi = networkApi;
    }
    
    public Call<InstagramResponse> getMedia(final String userName) {
        return networkApi.getMedia(userName);
    }
}