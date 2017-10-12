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

package com.thanksmister.iot.mqtt.alarmpanel.utils;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Rect;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.thanksmister.iot.mqtt.alarmpanel.R;
import com.thanksmister.iot.mqtt.alarmpanel.network.model.Daily;
import com.thanksmister.iot.mqtt.alarmpanel.ui.views.AlarmCodeView;
import com.thanksmister.iot.mqtt.alarmpanel.ui.views.AlarmDisableView;
import com.thanksmister.iot.mqtt.alarmpanel.ui.views.ArmOptionsView;
import com.thanksmister.iot.mqtt.alarmpanel.ui.views.ExtendedForecastView;
import com.thanksmister.iot.mqtt.alarmpanel.ui.views.ScreenSaverView;
import com.thanksmister.iot.mqtt.alarmpanel.ui.views.SettingsCodeView;

/**
 * Dialog utils
 */
public final class DialogUtils {

    private DialogUtils(){
    }
    
    public static Dialog showArmOptionsDialog(AppCompatActivity activity, ArmOptionsView.ViewListener armListener) {
        
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_alarm_options, null, false);
        Rect displayRectangle = new Rect();
        Window window = activity.getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(displayRectangle);
        int density= activity.getResources().getDisplayMetrics().densityDpi;
        if(density == DisplayMetrics.DENSITY_TV ) {
            view.setMinimumWidth((int) (displayRectangle.width() * 0.6f));
            view.setMinimumHeight((int) (displayRectangle.height() * 0.7f));
        } else if (density == DisplayMetrics.DENSITY_MEDIUM) {
            view.setMinimumWidth((int) (displayRectangle.width() * 0.5f));
            view.setMinimumHeight((int) (displayRectangle.height() * 0.6f));
        } else {
            view.setMinimumWidth((int)(displayRectangle.width() * 0.7f));
            view.setMinimumHeight((int)(displayRectangle.height() * 0.8f));
        }
        final ArmOptionsView optionsView = view.findViewById(R.id.armOptionsView);
        optionsView.setListener(armListener);
        return buildImmersiveDialog(activity, true, view, false);
    }

    /**
     * Shows the disable alarm dialog with countdown. It is important that this 
     * dialog only be shown once and not relaunched when already displayed as
     * it resets the timer.
     */
    public static Dialog showAlarmDisableDialog(AppCompatActivity activity, AlarmDisableView.ViewListener alarmCodeListener, 
                                              int code, boolean beep, int timeRemaining) {
      
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_alarm_disable, null, false);
        final AlarmDisableView alarmCodeView = view.findViewById(R.id.alarmDisableView);
        alarmCodeView.setListener(alarmCodeListener);
        alarmCodeView.setCode(code);
        alarmCodeView.startCountDown(timeRemaining);
        if(beep) {
            alarmCodeView.playContinuousBeep();
        }
        
        Dialog disableDialog = buildImmersiveDialog(activity, true, view, false);
        disableDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                alarmCodeView.destroySoundUtils();
            }
        });
        return disableDialog;
    }

    public static Dialog showSettingsCodeDialog(AppCompatActivity activity, final int code, final SettingsCodeView.ViewListener listener) {
            LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.dialog_settings_code, null, false);
            final SettingsCodeView settingsCodeView = view.findViewById(R.id.settingsCodeView);
            settingsCodeView.setCode(code);
            settingsCodeView.setListener(listener);
            return buildImmersiveDialog(activity, true, view, false);
    }
    
    public static Dialog showCodeDialog(AppCompatActivity activity, boolean confirmCode, AlarmCodeView.ViewListener listener,
                                        DialogInterface.OnCancelListener onCancelListener) {
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_alarm_code_set, null, false);
        final AlarmCodeView alarmCodeView = view.findViewById(R.id.alarmCodeView);
        final TextView titleTextView = alarmCodeView.findViewById(R.id.codeTitle);
        if(confirmCode){
            titleTextView.setText(R.string.text_renter_alarm_code_title);
        }
        alarmCodeView.setListener(listener);
        Dialog disableDialog = buildImmersiveDialog(activity ,true, view, false);
        disableDialog.setOnCancelListener(onCancelListener);
        return disableDialog;
    }

    public static Dialog showExtendedForecastDialog(AppCompatActivity activity, Daily daily, String weatherUnits) {
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_extended_forecast, null, false);
        Rect displayRectangle = new Rect();
        Window window = activity.getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(displayRectangle);
        view.setMinimumWidth((int)(displayRectangle.width() * 0.7f));
        int density = activity.getResources().getDisplayMetrics().densityDpi;
        if (density == DisplayMetrics.DENSITY_MEDIUM) {
            view.setMinimumHeight((int) (displayRectangle.height() * 0.6f));
        } else {
            view.setMinimumHeight((int)(displayRectangle.height() * 0.8f));
        }
        final ExtendedForecastView extendedForecastView = view.findViewById(R.id.extendedForecastView);
        extendedForecastView.setExtendedForecast(daily, weatherUnits);
        return buildImmersiveDialog(activity, true, view, false);
    }

    /**
     * Show the screen saver only if the alarm isn't triggered. This shouldn't be an issue
     * with the alarm disabled because the disable time will be longer than this. 
     */
    public static Dialog showScreenSaver(AppCompatActivity activity, boolean showPhotoScreenSaver, 
                                       String imageSource, boolean fitToScreen, int rotation,
                                       View.OnClickListener onClickListener) {
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_screen_saver, null, false);
        final ScreenSaverView screenSaverView = view.findViewById(R.id.screenSaverView);
        screenSaverView.setScreenSaver(activity, showPhotoScreenSaver, imageSource, fitToScreen,rotation);
        screenSaverView.setOnClickListener(onClickListener);
        return buildImmersiveDialog(activity, true, screenSaverView, true);
    }

    // immersive dialogs without navigation 
    // https://stackoverflow.com/questions/22794049/how-do-i-maintain-the-immersive-mode-in-dialogs
    private static Dialog buildImmersiveDialog(AppCompatActivity context, boolean cancelable, View view, boolean fullscreen) {
        Dialog dialog;
        if(fullscreen) {
            dialog = new Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                dialog = new Dialog(context, android.R.style.Theme_Material_Light_Dialog_NoActionBar_MinWidth);
            } else {
                dialog = new Dialog(context, android.R.style.Theme_Holo_Light_Dialog_NoActionBar_MinWidth);
            }
        }
        dialog.setCancelable(cancelable);
        dialog.setContentView(view);
        //Set the dialog to not focusable (makes navigation ignore us adding the window)			
        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        dialog.getWindow().getDecorView().setSystemUiVisibility(context.getWindow().getDecorView().getSystemUiVisibility());
        dialog.show();
        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        wm.updateViewLayout(context.getWindow().getDecorView(), context.getWindow().getAttributes());
        return dialog;
    }
}