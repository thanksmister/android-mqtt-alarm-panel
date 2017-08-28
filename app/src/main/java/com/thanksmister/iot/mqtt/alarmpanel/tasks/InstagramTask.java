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

package com.thanksmister.iot.mqtt.alarmpanel.tasks;

import com.thanksmister.iot.mqtt.alarmpanel.network.fetchers.DarkSkyFetcher;
import com.thanksmister.iot.mqtt.alarmpanel.network.fetchers.InstagramFetcher;
import com.thanksmister.iot.mqtt.alarmpanel.network.model.DarkSkyResponse;
import com.thanksmister.iot.mqtt.alarmpanel.network.model.InstagramResponse;

import retrofit2.Call;
import retrofit2.Response;

public class InstagramTask extends NetworkTask<String, Void, Response<InstagramResponse>> {

    private InstagramFetcher fetcher;

    public InstagramTask(InstagramFetcher fetcher) {
        this.fetcher = fetcher;
    }

    protected Response<InstagramResponse> doNetworkAction(String... params) throws Exception {
        if (params.length != 1) {
            throw new Exception("Wrong number of params, expected 1, received " + params.length);
        }
        
        String userName = params[0];
        Call<InstagramResponse> call = fetcher.getMedia(userName);
        return call.execute();
    }
}