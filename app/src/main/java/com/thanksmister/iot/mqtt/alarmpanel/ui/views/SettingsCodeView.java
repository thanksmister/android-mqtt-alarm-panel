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
import android.util.AttributeSet;
import android.widget.TextView;

import com.thanksmister.iot.mqtt.alarmpanel.R;

import butterknife.Bind;

public class SettingsCodeView extends BaseAlarmView {

    @Bind(R.id.codeTitle)
    TextView codeTitle;

    public SettingsCodeView(Context context) {
        super(context);
    }

    public SettingsCodeView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        codeTitle.setText(R.string.text_settings_code_title);
        buttonDel.setEnabled(false);
        buttonDel.setVisibility(INVISIBLE);
    }
    
    @Override
    protected void onCancel() {
        if(listener != null) {
            listener.onCancel();
        }
        codeComplete = false;
        enteredCode = "";
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if(handler != null) {
            handler.removeCallbacks(delayRunnable);
        }
    }

    @Override
    void reset() {
    }

    @Override
    public void setCode(int code) {
        this.code = code;
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
    }

    private void validateCode(String validateCode) {
        int codeInt = Integer.parseInt(validateCode);
        if(codeInt == code) {
            if(listener != null) {
                listener.onComplete(code);
            }
        } else {
            codeComplete = false;
            enteredCode = "";
            if(listener != null) {
                listener.onError();
            }
        }
    }
}