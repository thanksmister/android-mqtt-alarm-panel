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

package com.thanksmister.iot.mqtt.alarmpanel.network

import com.facebook.stetho.okhttp3.StethoInterceptor
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.thanksmister.iot.mqtt.alarmpanel.network.model.DarkSkyResponse
import io.reactivex.Observable

import java.util.concurrent.TimeUnit

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class DarkSkyApi {

    private val service: DarkSkyRequest

    init {

        val base_url = "https://api.darksky.net"
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.NONE

        val httpClient = OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(10000, TimeUnit.SECONDS)
                .readTimeout(10000, TimeUnit.SECONDS)
                .addNetworkInterceptor(StethoInterceptor())
                .build()

        val gson = GsonBuilder()
                .create()

        val retrofit = Retrofit.Builder()
                .client(httpClient)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .baseUrl(base_url)
                .build()

        service = retrofit.create(DarkSkyRequest::class.java)
    }

    fun getHourlyForecast(apiKey: String, lat: String, lon: String, excludes: String, units: String, language: String): Call<DarkSkyResponse> {
        val service = service
        return service.getHourlyForecast(apiKey, lat, lon, excludes, units, language)
    }

    /*fun getExtendedForecast(apiKey: String, lat: String, lon: String, excludes: String, extended: String, units: String, language: String): Call<DarkSkyResponse> {
        val service = service
        return service.getExtendedForecast(apiKey, lat, lon, excludes, extended, units, language)
    }*/

    fun getExtendedForecast(apiKey: String, lat: String, lon: String, excludes: String, extended: String, units: String, language: String): Observable<DarkSkyResponse> {
        return service.getExtendedForecast(apiKey, lat, lon, excludes, extended, units, language)
    }
}