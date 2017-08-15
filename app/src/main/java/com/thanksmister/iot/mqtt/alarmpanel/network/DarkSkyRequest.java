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

package com.thanksmister.iot.mqtt.alarmpanel.network;

import com.thanksmister.iot.mqtt.alarmpanel.network.model.DarkSkyResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface DarkSkyRequest {
    
    public static final String UNITS_SI = "si";
    public static final String UNITS_US = "us";
    public static final String UNITS_AUTO = "auto";

    @GET("/forecast/{apikey}/{lat},{lon}")
    Call<DarkSkyResponse> getHourlyForecast(@Path("apikey") String apiKey,
                                            @Path("lat") String lat,
                                            @Path("lon") String lon,
                                            @Query("exclude") String exclude,
                                            @Query("units") String units,
                                            @Query("lang") String language);

    @GET("/forecast/{apikey}/{lat},{lon}")
    Call<DarkSkyResponse> getExtendedForecast(@Path("apikey") String apiKey,
                                            @Path("lat") String lat,
                                            @Path("lon") String lon,
                                            @Query("exclude") String exclude,
                                            @Query("extended") String extended,
                                            @Query("units") String units,
                                            @Query("lang") String language);
}