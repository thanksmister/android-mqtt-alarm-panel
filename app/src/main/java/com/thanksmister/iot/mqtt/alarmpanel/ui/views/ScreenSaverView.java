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

package com.thanksmister.iot.mqtt.alarmpanel.ui.views;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.thanksmister.iot.mqtt.alarmpanel.R;
import com.thanksmister.iot.mqtt.alarmpanel.network.InstagramApi;
import com.thanksmister.iot.mqtt.alarmpanel.network.fetchers.InstagramFetcher;
import com.thanksmister.iot.mqtt.alarmpanel.network.model.InstagramItem;
import com.thanksmister.iot.mqtt.alarmpanel.network.model.InstagramResponse;
import com.thanksmister.iot.mqtt.alarmpanel.tasks.DarkSkyTask;
import com.thanksmister.iot.mqtt.alarmpanel.tasks.InstagramTask;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit2.Response;
import timber.log.Timber;

public class ScreenSaverView extends RelativeLayout {
    
    @Bind(R.id.screenSaverImage)
    ImageView screenSaverImage;

    @Bind(R.id.screenSaverClock)
    TextView screenSaverClock;
    
    private InstagramTask task;
    private String userName;
    private boolean fitToScreen;
    private Handler rotationHandler;
    private Handler timeHandler;
    private Picasso picasso;
    private List<InstagramItem> itemList;
    private Context context;
    private String imageUrl;
    private long rotationInterval;

    public ScreenSaverView(Context context) {
        super(context);
        this.context = context;
    }

    public ScreenSaverView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if(task != null) {
            task.cancel(true);
            task = null;
        }

        if(picasso != null) {
            picasso.invalidate(imageUrl);
            picasso.cancelRequest(screenSaverImage);
            picasso = null;
        }

        if(rotationHandler != null) {
            rotationHandler.removeCallbacks(delayRotationRunnable);
        }
        
        if(timeHandler != null) {
            timeHandler.removeCallbacks(timeRunnable);
        }
    }
    
    public void setScreenSaver(Context context, boolean useImageScreenSaver, String userName, boolean fitToScreen, 
                               int rotationIntervalMinutes) {
        this.context = context;
        this.userName = userName;
        this.fitToScreen = fitToScreen;
        this.rotationInterval = rotationIntervalMinutes*1000; // convert to milliseconds
       
        if(useImageScreenSaver && !TextUtils.isEmpty(userName) ) {
            screenSaverImage.setVisibility(View.VISIBLE);
            screenSaverClock.setVisibility(View.GONE);
            if(timeHandler != null) {
                timeHandler.removeCallbacks(timeRunnable);
            }
            startScreenSavor();
        } else { // use clock
            screenSaverImage.setVisibility(View.GONE);
            screenSaverClock.setVisibility(View.VISIBLE);
            timeHandler = new Handler();
            timeHandler.postDelayed(timeRunnable, 10);
        }
    }
    
    private void startScreenSavor() {
        if(itemList == null || itemList.isEmpty()) {
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

    private Runnable timeRunnable = new Runnable() {
        @Override
        public void run() {
            String currentTimeString = DateFormat.getTimeInstance(DateFormat.DEFAULT, Locale.getDefault()).format(new Date());
            screenSaverClock.setText(currentTimeString);
            if(timeHandler != null) {
                timeHandler.postDelayed(timeRunnable, 1000);
            }
        }
    };

    private void startImageRotation() {
        if(picasso == null) {
            picasso = Picasso.with(context);
        }
        if(itemList != null && !itemList.isEmpty()) {
            final int min = 0;
            final int max = itemList.size() - 1;
            final int random = new Random().nextInt((max - min) + 1) + min;
            InstagramItem instagramItem = itemList.get(random);
            imageUrl = instagramItem.getImages().getStandardResolution().getUrl();
            if(fitToScreen) {
                picasso.load(imageUrl)
                        .placeholder(R.color.black)
                        .resize(screenSaverImage.getWidth(), screenSaverImage.getHeight())
                        .centerCrop()
                        .error(R.color.black)
                        .into(screenSaverImage);
            } else {
                picasso.load(imageUrl)
                        .placeholder(R.color.black)
                        .error(R.color.black)
                        .into(screenSaverImage);
            }
            if(rotationHandler == null) {
                rotationHandler = new Handler();
            }
            rotationHandler.postDelayed(delayRotationRunnable, rotationInterval);
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
                    Timber.d("InstagramResponse: " + instagramResponse);
                    if (instagramResponse != null) {
                        itemList = instagramResponse.getItems();
                        Timber.d("itemList: " + itemList.size());
                        startImageRotation();
                    }
                }
            });
            task.execute(userName);
        }
    }
}