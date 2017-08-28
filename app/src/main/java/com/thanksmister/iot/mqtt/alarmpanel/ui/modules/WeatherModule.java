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

import android.os.AsyncTask;
import android.os.Handler;

import com.thanksmister.iot.mqtt.alarmpanel.network.DarkSkyApi;
import com.thanksmister.iot.mqtt.alarmpanel.network.fetchers.DarkSkyFetcher;
import com.thanksmister.iot.mqtt.alarmpanel.network.model.Daily;
import com.thanksmister.iot.mqtt.alarmpanel.network.model.DarkSkyResponse;
import com.thanksmister.iot.mqtt.alarmpanel.tasks.DarkSkyTask;

import retrofit2.Response;
import timber.log.Timber;

/**
 * We lazily load the hourly forecast and the extended forecast.  We could have done this on a syncadapter or alarm, but
 * we only care that this module runs while we are in the application. 
 */
public class WeatherModule  {
    
    private final long TIME_IN_MILLISECONDS = 1800000; // 30 minutes
    
    private DarkSkyTask task;
    private ForecastListener listener;
    private String apiKey;
    private String latitude;
    private String longitude;
    private String tempUnits;
    private Handler handler;
    
    public WeatherModule() {

    }

    public interface ForecastListener {
        void onWeatherToday(String icon, double apparentTemperature, String summary);
        void onExtendedDaily(Daily daily);
        void onShouldTakeUmbrella(boolean takeUmbrella);
    }

    /**
     * @param key The api key for the DarkSky weather api
     * @param units SI or US
     * @param lat Location latitude 
     * @param lon Location longitude
     * @param callback A nice little listener to wrap up the response
     */
    public void getDarkSkyHourlyForecast(final String key, final String units, final String lat,
                                         final String lon, final ForecastListener callback) {

        apiKey = key;
        listener = callback;
        tempUnits = units;
        latitude = lat;
        longitude = lon;
        
        startDarkSkyHourlyForecast();
    }
    
    private void startDarkSkyHourlyForecast() {

        if(task != null && task.getStatus().equals(AsyncTask.Status.RUNNING)) {
            return; // we have a running task alreadyß
        }

        Timber.d("startDarkSkyHourlyForecast");

        final DarkSkyApi api = new DarkSkyApi();
        final DarkSkyFetcher fetcher = new DarkSkyFetcher(api);

        task = new DarkSkyTask(fetcher);
        task.setOnExceptionListener(new DarkSkyTask.OnExceptionListener() {
            public void onException(Exception exception) {
                Timber.e("Weather Exception: " + exception.getMessage());
            }
        });
        task.setOnCompleteListener(new DarkSkyTask.OnCompleteListener<Response<DarkSkyResponse>>() {
            public void onComplete(Response<DarkSkyResponse> response) {
                Timber.d("Response: " + response);
                Timber.d("Response: " + response.code());
                DarkSkyResponse darkSkyResponse = response.body();
                if (darkSkyResponse != null) {

                    // current weather
                    if (darkSkyResponse.getCurrently() != null) {
                        listener.onWeatherToday(darkSkyResponse.getCurrently().getIcon(), darkSkyResponse.getCurrently().getApparentTemperature(),  darkSkyResponse.getCurrently().getSummary());
                    }

                    // should we take an umbrella today?
                    if (darkSkyResponse.getCurrently() != null && darkSkyResponse.getCurrently().getPrecipProbability() != null) {
                        listener.onShouldTakeUmbrella(shouldTakeUmbrellaToday(darkSkyResponse.getCurrently().getPrecipProbability()));
                    } else {
                        listener.onShouldTakeUmbrella(false);
                    }

                    // extended forecast
                    if(darkSkyResponse.getDaily() != null) {
                        listener.onExtendedDaily(darkSkyResponse.getDaily());
                    }
                    setHandler();
                }
            }
        });
        task.execute(apiKey, tempUnits, latitude, longitude);
    }

    private void setHandler() {
        if(handler == null) {
            handler = new Handler();
        }
        handler.postDelayed(delayRunnable, TIME_IN_MILLISECONDS);
    }

    public void cancelDarkSkyHourlyForecast() {
        if(handler != null) {
            handler.removeCallbacks(delayRunnable);
        }
        if(task != null) {
            task.cancel(true);
            task = null;
        }
    }

    /**
     * Determines if today is a good day to take your umbrella
     * Adapted from https://github.com/HannahMitt/HomeMirror/.
     * @return
     */
    private boolean shouldTakeUmbrellaToday(double precipProbability) {
        return precipProbability > 0.3;
    }

    private Runnable delayRunnable = new Runnable() {
        @Override
        public void run() {
            handler.removeCallbacks(delayRunnable);
            startDarkSkyHourlyForecast();
        }
    };
}