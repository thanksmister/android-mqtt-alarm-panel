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
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;

import com.thanksmister.iot.mqtt.alarmpanel.R;
import com.todddavies.components.progressbar.ProgressWheel;

import butterknife.Bind;
import timber.log.Timber;

public class AlarmDisableView extends BaseAlarmView {

    @Bind(R.id.countDownProgressWheel)
    ProgressWheel countDownProgressWheel;

    private int displaySeconds;
    private CountDownTimer countDownTimer;

    public AlarmDisableView(Context context) {
        super(context);
    }

    public AlarmDisableView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    public void onCancel(){
        codeComplete = false;
        enteredCode = "";
        showFilledPins(0);
        if(listener != null) {
            listener.onCancel();
        }
    }

    public void startCountDown(int pendingTime) {
        Timber.d("startCountDown: "+ pendingTime*1000);
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
                    listener.onCancel();
                }
            }
        }.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if(handler != null) {
            handler.removeCallbacks(delayRunnable);
        }
        if(countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }

    @Override
    public void setCode(int code) {
        this.code = code;
    }

    @Override
    void reset() {
    }

    @Override
    protected void addPinCode(String code) {
        if (codeComplete)
            return;

        enteredCode += code;

        showFilledPins(enteredCode.length());

        if (enteredCode.length() == MAX_CODE_LENGTH) {
            codeComplete = true;
            handler = new Handler();
            handler.postDelayed(delayRunnable, 500);
        }
    }

    private Runnable delayRunnable = new Runnable() {
        @Override
        public void run() {
            handler.removeCallbacks(delayRunnable);
            validateCode(enteredCode);
        }
    };

    @Override
    protected void removePinCode() {
        if (codeComplete) {
            return;
        }

        if (!TextUtils.isEmpty(enteredCode)) {
            enteredCode = enteredCode.substring(0, enteredCode.length() - 1);
        }

        showFilledPins(enteredCode.length());
    }

    private void validateCode(String validateCode) {
        int codeInt = Integer.parseInt(validateCode);
        if(codeInt == code) {
            if(countDownTimer != null) {
                countDownTimer.cancel();
                countDownTimer = null; 
            }
            if(listener != null) {
                listener.onComplete(code);
            }
        } else {
            codeComplete = false;
            enteredCode = "";
            showFilledPins(0);
            if(listener != null) {
                listener.onError();
            }
        }
    }
}