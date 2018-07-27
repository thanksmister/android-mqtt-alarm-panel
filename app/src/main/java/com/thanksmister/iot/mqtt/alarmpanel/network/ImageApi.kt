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

package com.thanksmister.iot.mqtt.alarmpanel.network

import com.facebook.stetho.okhttp3.StethoInterceptor
import com.google.gson.GsonBuilder
import com.thanksmister.iot.mqtt.alarmpanel.network.adapters.DataTypeAdapterFactory
import com.thanksmister.iot.mqtt.alarmpanel.network.model.ImageResponse
import io.reactivex.Observable

import java.util.concurrent.TimeUnit

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class ImageApi {

    private val service: ImageRequest

    init {

        val base_url = "https://api.imgur.com/"

        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.HEADERS

        val httpClient = OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(10000, TimeUnit.SECONDS)
                .readTimeout(10000, TimeUnit.SECONDS)
                .addNetworkInterceptor(StethoInterceptor())
                .build()

        val gson = GsonBuilder()
                .registerTypeAdapterFactory(DataTypeAdapterFactory())
                .create()

        val retrofit = Retrofit.Builder()
                .client(httpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl(base_url)
                .build()

        service = retrofit.create(ImageRequest::class.java)
    }

    fun getImagesByTag(clientId: String, tag: String): Observable<ImageResponse> {
        val service = service
        val auth = "Client-ID " + clientId
        return service.getImagesByTag(auth, tag)
    }
}