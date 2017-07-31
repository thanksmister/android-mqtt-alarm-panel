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

package com.thanksmister.androidthings.iot.alarmpanel.ui.views;

import android.content.Context;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;

import com.thanksmister.androidthings.iot.alarmpanel.R;
import com.todddavies.components.progressbar.ProgressWheel;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CountDownView extends FrameLayout {
    
    @Bind(R.id.countDownProgressWheel)
    ProgressWheel countDownProgressWheel;
    
    @OnClick(R.id.buttonCancel)
    void buttonCancelClicked() {
        countDownTimer.cancel();
        countDownTimer = null;
        listener.onCancel();
    }
    
    private CountDownTimer countDownTimer;
    private int displaySeconds;
    private ViewListener listener;

    public interface ViewListener {
        void onComplete();
        void onCancel();
    }
    
    public CountDownView(Context context) {
        super(context);
    }

    public CountDownView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);

        countDownTimer = new CountDownTimer(20000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                // decompose difference into days, hours, minutes and seconds
                displaySeconds = (int) ((millisUntilFinished / 1000) % 60);
                Animation an = new RotateAnimation(0.0f, 90.0f, 250f, 273f);
                an.setFillAfter(true);
                countDownProgressWheel.setText(String.valueOf(displaySeconds));
                countDownProgressWheel.setProgress(displaySeconds * 6);
            }

            @Override
            public void onFinish() {
                listener.onComplete();
            }
        }.start();
    }

    public void setListener(@NonNull ViewListener listener) {
        this.listener = listener;
    }
    
}