package com.thanksmister.iot.mqtt.alarmpanel.ui.views;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.thanksmister.iot.mqtt.alarmpanel.R;
import com.thanksmister.iot.mqtt.alarmpanel.utils.SoundUtils;

import butterknife.Bind;
import butterknife.ButterKnife;

abstract class BaseAlarmView extends LinearLayout {

    protected static final int MAX_CODE_LENGTH = 4;

    protected int code;
    protected boolean codeComplete = false;
    protected String enteredCode = "";
    protected ViewListener listener;
    protected Handler handler;

    @Nullable
    @Bind(R.id.pinCode1)
    ImageView pinCode1;

    @Nullable
    @Bind(R.id.pinCode2)
    ImageView pinCode2;

    @Nullable
    @Bind(R.id.pinCode3)
    ImageView pinCode3;

    @Nullable
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

    @Nullable
    @Bind(R.id.buttonCancel)
    View buttonCancel;

    private SoundUtils soundUtils;

    public interface ViewListener {
        void onComplete(int code);
        void onError();
        void onCancel();
    }

    public void setListener(@NonNull AlarmCodeView.ViewListener listener) {
        this.listener = listener;
    }

    public BaseAlarmView(Context context) {
        super(context);
    }

    public BaseAlarmView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {

        super.onFinishInflate();

        ButterKnife.bind(this);

        button0.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                getSoundUtils().playBuzzerOnButtonPress();
                addPinCode("0");
            }
        });

        button1.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                getSoundUtils().playBuzzerOnButtonPress();
                addPinCode("1");
            }
        });

        button2.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                getSoundUtils(). playBuzzerOnButtonPress();
                addPinCode("2");
            }
        });

        button3.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                getSoundUtils().playBuzzerOnButtonPress();
                addPinCode("3");
            }
        });

        button4.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                getSoundUtils().playBuzzerOnButtonPress();
                addPinCode("4");
            }
        });

        button5.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                getSoundUtils().playBuzzerOnButtonPress();
                addPinCode("5");
            }
        });

        button6.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                getSoundUtils().playBuzzerOnButtonPress();
                addPinCode("6");
            }
        });

        button7.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                getSoundUtils().playBuzzerOnButtonPress();
                addPinCode("7");
            }
        });

        button8.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                getSoundUtils().playBuzzerOnButtonPress();
                addPinCode("8");
            }
        });

        button9.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                getSoundUtils().playBuzzerOnButtonPress();
                addPinCode("9");
            }
        });

        buttonDel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                getSoundUtils().playBuzzerOnButtonPress();
                removePinCode();
            }
        });

        buttonDel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                getSoundUtils().playBuzzerOnButtonPress();
                removePinCode();
            }
        });

        if(buttonCancel != null) {
            buttonCancel.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    getSoundUtils().playBuzzerOnButtonPress();
                    onCancel();
                }
            });
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        ButterKnife.unbind(this);
        destroySoundUtils();
    }

    abstract void onCancel();
    abstract void removePinCode();
    abstract void addPinCode(String code);
    abstract void reset();
    
    public void destroySoundUtils() { 
        if(soundUtils != null) {
            soundUtils.destroyBuzzer();
        }
    }

    public void playContinuousBeep() {
        getSoundUtils().playBuzzerRepeat();
    }

    public SoundUtils getSoundUtils() {
        if(soundUtils == null) {
            soundUtils = new SoundUtils(getContext());
        }
        return soundUtils;
    }

    public void setCode(int code) {
        this.code = code;
    }

    protected void showFilledPins(int pinsShown) {
        if(pinCode1 != null && pinCode2 != null && pinCode3 != null && pinCode4 != null){
            switch (pinsShown) {
                case 1:
                    pinCode1.setImageResource(R.drawable.ic_radio_button_checked_black_32dp);
                    pinCode2.setImageResource(R.drawable.ic_radio_button_unchecked_black_32dp);
                    pinCode3.setImageResource(R.drawable.ic_radio_button_unchecked_black_32dp);
                    pinCode4.setImageResource(R.drawable.ic_radio_button_unchecked_black_32dp);
                    break;
                case 2:
                    pinCode1.setImageResource(R.drawable.ic_radio_button_checked_black_32dp);
                    pinCode2.setImageResource(R.drawable.ic_radio_button_checked_black_32dp);
                    pinCode3.setImageResource(R.drawable.ic_radio_button_unchecked_black_32dp);
                    pinCode4.setImageResource(R.drawable.ic_radio_button_unchecked_black_32dp);
                    break;
                case 3:
                    pinCode1.setImageResource(R.drawable.ic_radio_button_checked_black_32dp);
                    pinCode2.setImageResource(R.drawable.ic_radio_button_checked_black_32dp);
                    pinCode3.setImageResource(R.drawable.ic_radio_button_checked_black_32dp);
                    pinCode4.setImageResource(R.drawable.ic_radio_button_unchecked_black_32dp);
                    break;
                case 4:
                    pinCode1.setImageResource(R.drawable.ic_radio_button_checked_black_32dp);
                    pinCode2.setImageResource(R.drawable.ic_radio_button_checked_black_32dp);
                    pinCode3.setImageResource(R.drawable.ic_radio_button_checked_black_32dp);
                    pinCode4.setImageResource(R.drawable.ic_radio_button_checked_black_32dp);
                    break;
                default:
                    pinCode1.setImageResource(R.drawable.ic_radio_button_unchecked_black_32dp);
                    pinCode2.setImageResource(R.drawable.ic_radio_button_unchecked_black_32dp);
                    pinCode3.setImageResource(R.drawable.ic_radio_button_unchecked_black_32dp);
                    pinCode4.setImageResource(R.drawable.ic_radio_button_unchecked_black_32dp);
                    break;
            }
        }
    }
}