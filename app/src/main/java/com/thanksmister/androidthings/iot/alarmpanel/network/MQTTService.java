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

package com.thanksmister.androidthings.iot.alarmpanel.network;

import com.thanksmister.androidthings.iot.alarmpanel.network.model.FeedData;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;

public interface MQTTService {
    
    //api call to the latest feed data
    @GET("{username}/feeds/{feed_key}/data/last")
    Call<FeedData> getLastDataInQueue(@Header("X-AIO-Key") String key,
                                      @Path("username") String username, 
                                      @Path("feed_key") String feed_key);

    @GET("{username}/feeds/{feed_key}/data/")
    Call<List<FeedData>> getFeedData(@Header("X-AIO-Key") String key,
                                     @Path("username") String username,
                                     @Path("feed_key") String feed_key);
}