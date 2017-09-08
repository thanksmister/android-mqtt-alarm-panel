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

public class AlarmTriggeredView extends BaseAlarmView {

    public AlarmTriggeredView(Context context) {
        super(context);
    }

    public AlarmTriggeredView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }
    
    @Override
    protected void onCancel() {
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if(handler != null) {
            handler.removeCallbacks(delayRunnable);
        }
    }
    
    @Override
    protected void reset() {
        codeComplete = false;
        enteredCode = "";
    }

    @Override
    protected void addPinCode(String code) {
        if (codeComplete)
            return;

        enteredCode += code;
        
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
    }
    
    private void validateCode(String validateCode) {
        int codeInt = Integer.parseInt(validateCode);
        if(codeInt == code) {
            if(listener != null) {
                listener.onComplete(code);
            }
        } else {
            reset();
            if(listener != null) {
                listener.onError();
            }
        }
    }
}