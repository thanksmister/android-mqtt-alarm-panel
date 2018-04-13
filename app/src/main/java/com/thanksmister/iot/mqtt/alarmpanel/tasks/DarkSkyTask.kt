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

package com.thanksmister.iot.mqtt.alarmpanel.tasks

import com.thanksmister.iot.mqtt.alarmpanel.network.fetchers.DarkSkyFetcher
import com.thanksmister.iot.mqtt.alarmpanel.network.model.DarkSkyResponse
import io.reactivex.Observable

import retrofit2.Call
import retrofit2.Response

class DarkSkyTask(private val fetcher: DarkSkyFetcher) : NetworkTask<String, Void, Observable<DarkSkyResponse>>() {

    @Throws(Exception::class)
    override fun doNetworkAction(vararg params: String): Observable<DarkSkyResponse> {
        if (params.size != 4) {
            throw Exception("Wrong number of params, expected 4, received " + params.size)
        }

        val apiKey = params[0]
        val units = params[1]
        val lat = params[2]
        val lon = params[3]

        return fetcher.getExtendedFeedData(apiKey, units, lat, lon)
    }
}