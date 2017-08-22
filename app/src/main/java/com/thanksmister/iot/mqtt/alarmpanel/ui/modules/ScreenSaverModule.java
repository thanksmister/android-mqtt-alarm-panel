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

package com.thanksmister.iot.mqtt.alarmpanel.ui.modules;

import android.os.Handler;

import com.thanksmister.iot.mqtt.alarmpanel.network.DarkSkyApi;
import com.thanksmister.iot.mqtt.alarmpanel.network.fetchers.DarkSkyFetcher;
import com.thanksmister.iot.mqtt.alarmpanel.network.model.Daily;
import com.thanksmister.iot.mqtt.alarmpanel.network.model.DarkSkyResponse;
import com.thanksmister.iot.mqtt.alarmpanel.tasks.DarkSkyTask;
import com.thanksmister.iot.mqtt.alarmpanel.tasks.GettyImagesTask;

import retrofit2.Response;
import timber.log.Timber;

/**
 * We lazily load the images for our screen saver.
 */
public class ScreenSaverModule {

    private final long TIME_IN_MILLISECONDS = 9 * 60 * 1000; // 9 minutes

    private GettyImagesTask task;
    private ScreenSavorListener listener;
    private Handler handler;

    public ScreenSaverModule() {
        handler = new Handler();
    }

    public interface ScreenSavorListener {
        void onImageDownloaded(String url);
    }

    /**
     * @param key The api key for Getty Images
     * @param callback A nice little listener to wrap up the response
     */
    public void getScreenSaverImages(String key, final ScreenSavorListener callback) {

        //apiKey = key;
        listener = callback;

        startScreenSavor();
    }
    
    private void startScreenSavor() {
        

    }

    public void stopScreeSaver() {

    }

    private Runnable delayRunnable = new Runnable() {
        @Override
        public void run() {
            handler.removeCallbacks(delayRunnable);
            startScreenSavor();
        }
    };
}