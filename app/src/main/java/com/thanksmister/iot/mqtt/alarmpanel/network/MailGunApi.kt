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

import android.graphics.Bitmap
import android.util.Base64
import com.facebook.stetho.okhttp3.StethoInterceptor
import com.google.gson.GsonBuilder
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

class MailGunApi(domain: String, private val apiKey: String) {

    private val service: MailGunRequest

    init {

        val base_url = "https://api.mailgun.net/v3/$domain/"
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.HEADERS

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
                .addConverterFactory(GsonConverterFactory.create(gson))
                .baseUrl(base_url)
                .build()

        service = retrofit.create(MailGunRequest::class.java)
    }

    fun emailImages(from: String, to: String, subject: String, text: String, bitmap: Bitmap): Call<JSONObject> {
        val clientIdAndSecret = "api" + ":" + apiKey
        val authorizationHeader = BASIC + " " + Base64.encodeToString(clientIdAndSecret.toByteArray(), Base64.NO_WRAP)
        val service = service

        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 75, stream)
        val byteArray = stream.toByteArray()

        return service.sendMailAttachment(authorizationHeader,
                RequestBody.create(MediaType.parse("text/plain"), from),
                RequestBody.create(MediaType.parse("text/plain"), to),
                RequestBody.create(MediaType.parse("text/plain"), subject),
                RequestBody.create(MediaType.parse("text/plain"), text),
                RequestBody.create(MediaType.parse("image/*"), byteArray))
    }

    companion object {

        private val BASIC = "Basic"
    }
}