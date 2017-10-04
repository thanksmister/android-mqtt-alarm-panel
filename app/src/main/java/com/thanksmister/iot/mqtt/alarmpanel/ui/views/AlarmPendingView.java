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
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.LinearLayout;

import com.thanksmister.iot.mqtt.alarmpanel.R;
import com.todddavies.components.progressbar.ProgressWheel;

import butterknife.Bind;
import butterknife.ButterKnife;
import timber.log.Timber;

public class AlarmPendingView extends LinearLayout {
    
    @Bind(R.id.countDownProgressWheel)
    ProgressWheel countDownProgressWheel;
    
    private ViewListener listener;
    private int displaySeconds;
    private CountDownTimer countDownTimer;

    public AlarmPendingView(Context context) {
        super(context);
    }

    public AlarmPendingView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopCountDown();
    }

    /**
     * Countdown timer time 
     * @param pendingTime seconds
     */
    public void startCountDown(int pendingTime) {
        
        if(pendingTime <= 0) {
            stopCountDown();
            if(listener != null) {
                listener.onTimeOut();
                displaySeconds = 0;
            }
            return;
        }
        
        if(countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
            displaySeconds = 0;
        }
        Timber.d("startCountDown: "+ pendingTime*1300);
        final int divideBy = 360/pendingTime;
        countDownTimer = new CountDownTimer(pendingTime*1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                displaySeconds = (int) (millisUntilFinished / 1000);
                Animation an = new RotateAnimation(0.0f, 90.0f, 250f, 273f);
                an.setFillAfter(true);
                countDownProgressWheel.setText(String.valueOf(displaySeconds));
                countDownProgressWheel.setProgress(displaySeconds * divideBy);
            }
            @Override
            public void onFinish() {
                Timber.d("Timed up...");
                if(listener != null) {
                    listener.onTimeOut();
                    displaySeconds = 0;
                }
            }
        }.start();
    }

    /**
     * Method to return number of display seconds remaining on countdown.
     * @return Integer for seconds remaining.
     */
    public int getCountDownTimeRemaining() {
        return displaySeconds;
    }
    
    public void stopCountDown() {
        if(countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
        displaySeconds = 0;
    }
    
    public void setListener(@NonNull ViewListener listener) {
        this.listener = listener;
    }

    public interface ViewListener {
        void onTimeOut();
    }
}