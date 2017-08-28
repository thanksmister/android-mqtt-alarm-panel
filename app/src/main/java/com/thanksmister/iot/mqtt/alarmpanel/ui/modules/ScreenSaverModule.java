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

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.thanksmister.iot.mqtt.alarmpanel.R;
import com.thanksmister.iot.mqtt.alarmpanel.network.DarkSkyApi;
import com.thanksmister.iot.mqtt.alarmpanel.network.InstagramApi;
import com.thanksmister.iot.mqtt.alarmpanel.network.fetchers.DarkSkyFetcher;
import com.thanksmister.iot.mqtt.alarmpanel.network.fetchers.InstagramFetcher;
import com.thanksmister.iot.mqtt.alarmpanel.network.model.DarkSkyResponse;
import com.thanksmister.iot.mqtt.alarmpanel.network.model.InstagramItem;
import com.thanksmister.iot.mqtt.alarmpanel.network.model.InstagramResponse;
import com.thanksmister.iot.mqtt.alarmpanel.tasks.DarkSkyTask;
import com.thanksmister.iot.mqtt.alarmpanel.tasks.InstagramTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import retrofit2.Response;
import timber.log.Timber;

/**
 * We lazily load the images for our screen saver.
 */
public class ScreenSaverModule {

    private final long ROTATE_TIME_IN_MILLISECONDS = 2 * 60 * 1000; // 9 minutes

    private InstagramTask task;
    private String userName;
    private ImageView imageView;
    private boolean fitToScreen;
    private Context context;
    private Handler rotationHandler;
    private Picasso picasso;
    private List<InstagramItem> itemList;

    public ScreenSaverModule() {
        rotationHandler = new Handler();
        itemList = new ArrayList<>();
    }

    public void getScreenSaverImages(Context context, ImageView imageView, String userName, boolean fitToScreen) {
        this.context = context;
        this.imageView = imageView;
        this.userName = userName;
        this.fitToScreen = fitToScreen;
    }

    public void startScreenSavor() {
        if(itemList.isEmpty() && !TextUtils.isEmpty(userName) ) {
           fetchMediaData();
       } else {
            startImageRotation();
        }
    }

    private Runnable delayRotationRunnable = new Runnable() {
        @Override
        public void run() {
            rotationHandler.removeCallbacks(delayRotationRunnable);
            startImageRotation();
        }
    };

    private void startImageRotation() {

        if(context == null || imageView == null) return;

        if(picasso == null) {
          picasso = Picasso.with(context);
        }

        if(itemList != null && !itemList.isEmpty()) {
            final int min = 0;
            final int max = itemList.size() - 1;
            final int random = new Random().nextInt((max - min) + 1) + min;
            InstagramItem instagramItem = itemList.get(random);
            String url = instagramItem.getImages().getStandardResolution().getUrl();
            if(fitToScreen) {
                picasso.load(url)
                        .placeholder(R.color.black)
                        .resize(imageView.getWidth(), imageView.getHeight())
                        .centerCrop()
                        .error(R.color.black)
                        .into(imageView);
            } else {
                picasso.load(url)
                        .placeholder(R.color.black)
                        .error(R.color.black)
                        .into(imageView);
            }
            if(rotationHandler != null) {
                rotationHandler.postDelayed(delayRotationRunnable, ROTATE_TIME_IN_MILLISECONDS);
            }
        }
    }

    public void stopScreeSaver() {
        if(task != null) {
            task.cancel(true);
            task = null;
        }

        if(picasso != null && imageView != null) {
            picasso.cancelRequest(imageView);
            picasso = null;
            imageView = null;
        }

        if(rotationHandler != null) {
            rotationHandler.removeCallbacks(delayRotationRunnable);
        }
    }

    private void fetchMediaData() {
        if(task == null || task.isCancelled()) {
            final InstagramApi api = new InstagramApi();
            final InstagramFetcher fetcher = new InstagramFetcher(api);
            task = new InstagramTask(fetcher);
            task.setOnExceptionListener(new DarkSkyTask.OnExceptionListener() {
                public void onException(Exception exception) {
                    Timber.e("Instagram Exception: " + exception.getMessage());
                }
            });
            task.setOnCompleteListener(new InstagramTask.OnCompleteListener<Response<InstagramResponse>>() {
                public void onComplete(Response<InstagramResponse> response) {
                    Timber.d("Response: " + response);
                    Timber.d("Response: " + response.code());
                    InstagramResponse instagramResponse = response.body();
                    if (instagramResponse != null) {
                        itemList = instagramResponse.getItems();
                        startImageRotation();
                    }
                }
            });
            task.execute(userName);
        }
    }
}