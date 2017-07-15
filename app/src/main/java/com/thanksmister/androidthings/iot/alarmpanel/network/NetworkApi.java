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

import android.support.annotation.NonNull;

import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.thanksmister.androidthings.iot.alarmpanel.BuildConfig;
import com.thanksmister.androidthings.iot.alarmpanel.network.model.FeedData;

import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NetworkApi {
    
    private MQTTService mqttService;
    
    public NetworkApi(){
        
        String base_url = NetworkValues.BASE_URL_DEV;
        if(BuildConfig.BASE_ENVIRONMENT.equals(NetworkValues.DEV_ENVIRONMENT)) {
            base_url = NetworkValues.BASE_URL_DEV;
        } else if (BuildConfig.BASE_ENVIRONMENT.equals(NetworkValues.PROD_ENVIRONMENT)) {
            base_url = NetworkValues.BASE_URL_PROD;
        }

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(10000, TimeUnit.SECONDS)
                .readTimeout(10000, TimeUnit.SECONDS)
                .addNetworkInterceptor(new StethoInterceptor())
                .build();

        Gson gson = new GsonBuilder()
                .create();
        
        Retrofit retrofit = new Retrofit.Builder()
                .client(httpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .baseUrl(base_url)
                .build();

        mqttService = retrofit.create(MQTTService.class);
    }
    
    private MQTTService getMQTTService() {
        return mqttService;
    }

    public Call<FeedData> getLastDataInQueue(@NonNull final String key, @NonNull final String username, @NonNull final String feedKey)  {
        final MQTTService mqttService = getMQTTService();
        return mqttService.getLastDataInQueue(key, username, feedKey);
    }

    public Call<List<FeedData>> getFeedData(@NonNull final String key, @NonNull final String username, @NonNull final String feedKey)  {
        final MQTTService mqttService = getMQTTService();
        return mqttService.getFeedData(key, username, feedKey);
    }
}