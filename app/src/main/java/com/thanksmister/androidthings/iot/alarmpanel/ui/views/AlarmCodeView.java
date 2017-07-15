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
import android.os.Handler;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.thanksmister.androidthings.iot.alarmpanel.R;

import butterknife.Bind;
import butterknife.ButterKnife;

public class AlarmCodeView extends FrameLayout {
    public static final int MAX_CODE_LENGTH = 4;

    @Bind(R.id.pinCode1)
    ImageView pinCode1;

    @Bind(R.id.pinCode2)
    ImageView pinCode2;

    @Bind(R.id.pinCode3)
    ImageView pinCode3;

    @Bind(R.id.pinCode4)
    ImageView pinCode4;

    @Bind(R.id.button0)
    View button0;

    @Bind(R.id.button1)
    View button1;

    @Bind(R.id.button2)
    View button2;

    @Bind(R.id.button3)
    View button3;

    @Bind(R.id.button4)
    View button4;

    @Bind(R.id.button5)
    View button5;

    @Bind(R.id.button6)
    View button6;

    @Bind(R.id.button7)
    View button7;

    @Bind(R.id.button8)
    View button8;

    @Bind(R.id.button9)
    View button9;

    @Bind(R.id.buttonDel)
    View buttonDel;

    @Bind(R.id.buttonClear)
    View buttonClear;
    
    @Bind(R.id.codeTitle)
    TextView codeTitle;
    
    private boolean codeComplete = false;
    private String enteredCode = "";
    private ViewListener listener;
    private Handler handler;

    public AlarmCodeView(Context context) {
        super(context);
    }

    public AlarmCodeView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        ButterKnife.bind(this);
        
        codeTitle.setText(R.string.text_enter_alarm_code_title);

        button0.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                addPinCode("0");
            }
        });

        button1.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                addPinCode("1");
            }
        });

        button2.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                addPinCode("2");
            }
        });

        button3.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                addPinCode("3");
            }
        });

        button4.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                addPinCode("4");
            }
        });

        button5.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                addPinCode("5");
            }
        });

        button6.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                addPinCode("6");
            }
        });

        button7.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                addPinCode("7");
            }
        });

        button8.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                addPinCode("8");
            }
        });

        button9.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                addPinCode("9");
            }
        });

        buttonDel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                removePinCode();
            }
        });

        buttonDel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                removePinCode();
            }
        });

        buttonClear.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onCancel();
                codeComplete = false;
                enteredCode = "";
                showFilledPins(0);
            }
        });
    }

    public int getEnteredCode() {
        return Integer.valueOf(this.enteredCode);
    }
    
    public void clear() {
        codeComplete = false;
        enteredCode = "";
        showFilledPins(0);
    }

    public void setListener(@NonNull ViewListener listener) {
        this.listener = listener;
    }

    public interface ViewListener {
        void onComplete(int code);
        void onCancel();
    }

    private void addPinCode(String code) {
        
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

    private void removePinCode() {
        if (codeComplete) return;

        if (!TextUtils.isEmpty(enteredCode)) {
            enteredCode = enteredCode.substring(0, enteredCode.length() - 1);
            showFilledPins(enteredCode.length());
        }
    }

    private void showFilledPins(int pinsShown) {
        switch (pinsShown) {
            case 1:
                pinCode1.setImageResource(R.drawable.ic_pin_code_on);
                pinCode2.setImageResource(R.drawable.ic_pin_code_off);
                pinCode3.setImageResource(R.drawable.ic_pin_code_off);
                pinCode4.setImageResource(R.drawable.ic_pin_code_off);
                break;
            case 2:
                pinCode1.setImageResource(R.drawable.ic_pin_code_on);
                pinCode2.setImageResource(R.drawable.ic_pin_code_on);
                pinCode3.setImageResource(R.drawable.ic_pin_code_off);
                pinCode4.setImageResource(R.drawable.ic_pin_code_off);
                break;
            case 3:
                pinCode1.setImageResource(R.drawable.ic_pin_code_on);
                pinCode2.setImageResource(R.drawable.ic_pin_code_on);
                pinCode3.setImageResource(R.drawable.ic_pin_code_on);
                pinCode4.setImageResource(R.drawable.ic_pin_code_off);
                break;
            case 4:
                pinCode1.setImageResource(R.drawable.ic_pin_code_on);
                pinCode2.setImageResource(R.drawable.ic_pin_code_on);
                pinCode3.setImageResource(R.drawable.ic_pin_code_on);
                pinCode4.setImageResource(R.drawable.ic_pin_code_on);
                break;
            default:
                pinCode1.setImageResource(R.drawable.ic_pin_code_off);
                pinCode2.setImageResource(R.drawable.ic_pin_code_off);
                pinCode3.setImageResource(R.drawable.ic_pin_code_off);
                pinCode4.setImageResource(R.drawable.ic_pin_code_off);
                break;
        }
    }
}