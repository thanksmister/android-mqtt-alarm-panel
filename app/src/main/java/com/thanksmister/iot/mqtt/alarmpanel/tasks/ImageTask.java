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

package com.thanksmister.iot.mqtt.alarmpanel.tasks;

import android.support.annotation.NonNull;

import com.thanksmister.iot.mqtt.alarmpanel.network.fetchers.ImageFetcher;
import com.thanksmister.iot.mqtt.alarmpanel.network.model.ImageResponse;

import retrofit2.Call;
import retrofit2.Response;

public class ImageTask extends NetworkTask<String, Void, Response<ImageResponse>> {

    private ImageFetcher fetcher;

    public ImageTask(ImageFetcher fetcher) {
        this.fetcher = fetcher;
    }

    protected Response<ImageResponse> doNetworkAction(@NonNull String... params) throws Exception {
        if (params.length != 2) {
            throw new Exception("Wrong number of params, expected 2, received " + params.length);
        }

        String clientId = params[0];
        String tag = params[1];

        //Call<ImageResponse> call = fetcher.getImagesByTag(clientId, tag);
        return null; //call.execute();
    }
}