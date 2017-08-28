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
import android.util.Log;
import android.widget.TextView;

import com.google.android.things.contrib.driver.pwmspeaker.Speaker;
import com.thanksmister.iot.mqtt.alarmpanel.BoardDefaults;
import com.thanksmister.iot.mqtt.alarmpanel.R;

import java.io.IOException;

import butterknife.Bind;

import static android.content.ContentValues.TAG;

public class AlarmCodeView extends BaseAlarmView {

    @Bind(R.id.codeTitle)
    TextView codeTitle;

    public AlarmCodeView(Context context) {
        super(context);
    }

    public AlarmCodeView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        codeTitle.setText(R.string.text_enter_alarm_code_title);
    }

    public int getEnteredCode() {
        return Integer.valueOf(this.enteredCode);
    }

    @Override
    protected void onCancel() {
        listener.onCancel();
        codeComplete = false;
        enteredCode = "";
        showFilledPins(0);
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
            listener.onComplete(getEnteredCode());
        }
    };

    @Override
    protected void removePinCode() {
        if (codeComplete) return;
        if (!TextUtils.isEmpty(enteredCode)) {
            enteredCode = enteredCode.substring(0, enteredCode.length() - 1);
            showFilledPins(enteredCode.length());
        }
    }
}