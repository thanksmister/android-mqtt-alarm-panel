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

package com.thanksmister.iot.mqtt.alarmpanel;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.thanksmister.iot.mqtt.alarmpanel.data.stores.StoreManager;
import com.thanksmister.iot.mqtt.alarmpanel.network.model.Daily;
import com.thanksmister.iot.mqtt.alarmpanel.ui.Configuration;
import com.thanksmister.iot.mqtt.alarmpanel.ui.views.AlarmDisableView;
import com.thanksmister.iot.mqtt.alarmpanel.ui.views.ArmOptionsView;
import com.thanksmister.iot.mqtt.alarmpanel.ui.views.ExtendedForecastView;
import com.thanksmister.iot.mqtt.alarmpanel.ui.views.ScreenSaverView;

import butterknife.ButterKnife;

import static com.thanksmister.iot.mqtt.alarmpanel.ui.Configuration.PREF_TRIGGERED;
import static com.thanksmister.iot.mqtt.alarmpanel.ui.Configuration.PREF_TRIGGERED_PENDING;

abstract public class BaseActivity extends AppCompatActivity {

    public static final long INACTIVITY_TIMEOUT = 5 * 60 * 1000; // 3 min
    
    private StoreManager storeManager;
    private Configuration configuration;
    private AlertDialog progressDialog;
    private AlertDialog dialog;
    private AlertDialog disableDialog;
    private AlertDialog screenSaverDialog;
    private Handler inactivityHandler = new Handler();
    private View decorView;
    private PowerManager.WakeLock wakeLock;
    private boolean presenceDetected;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // for wakelock
        this.getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        decorView = getWindow().getDecorView();
    }
    
    @Override
    public void onResume() {
        super.onResume();
        releaseWakeLock();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
            dialog = null;
        }
        if (disableDialog != null && disableDialog.isShowing()) {
            disableDialog.dismiss();
            disableDialog = null;
        }
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
            progressDialog = null;
        }
        if(inactivityHandler != null) {
            inactivityHandler.removeCallbacks(inactivityCallback);
            inactivityHandler = null;
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            int visibility;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                visibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                visibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN;

            } else {
                visibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                        WindowManager.LayoutParams.FLAG_FULLSCREEN);
            }
            decorView.setSystemUiVisibility(visibility);
        }
    }
    
    public boolean isPresent() {
        return presenceDetected;
    }
 
    /**
     * Wakes the device when the alarm is triggered or to disarm.
     */
    public void acquireWakeLock() {
        if(wakeLock == null) {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "ALARM_WAKE_TAG");
        }
        if (wakeLock != null && !wakeLock.isHeld()) {  // but we don't hold it 
            wakeLock.acquire();
        }
        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock keyguardLock = keyguardManager.newKeyguardLock("ALARM_KEYBOARD_LOCK_TAG");
        keyguardLock.disableKeyguard();
    }
    
    public void releaseWakeLock() {
        if(wakeLock != null && wakeLock.isHeld()){
            wakeLock.release();
        }
    }

    private Runnable inactivityCallback = new Runnable() {
        @Override
        public void run() {
            hideDialog();
            if(getConfiguration().showScreenSaverModule()) {
                showScreenSaver();
            }
            presenceDetected = false;
        }
    };

    public void resetInactivityTimer() {
        closeScreenSaver();
        if(inactivityHandler != null) {
            inactivityHandler.removeCallbacks(inactivityCallback);
            inactivityHandler.postDelayed(inactivityCallback, INACTIVITY_TIMEOUT);
        }
    }

    public void stopDisconnectTimer(){
        if(inactivityHandler != null) {
            inactivityHandler.removeCallbacks(inactivityCallback);
        }
    }

    @Override
    public void onUserInteraction(){
        presenceDetected = true;
        resetInactivityTimer();
    }

    @Override
    public void onStop() {
        super.onStop();
        stopDisconnectTimer();
    }
    
    public StoreManager getStoreManager() {
        if (storeManager == null) {
            BaseApplication baseApplication = BaseApplication.getInstance();
            storeManager = new StoreManager(getApplicationContext(), getContentResolver(), baseApplication.getAppSharedPreferences());
        }
        return storeManager;
    }
    
    public Configuration getConfiguration() {
        if (configuration == null) {
            BaseApplication baseApplication = BaseApplication.getInstance();
            configuration = new Configuration(getApplicationContext(), baseApplication.getAppSharedPreferences());
        }
        return configuration;
    }

    public void showProgressDialog(String message, boolean modal) {
        if (progressDialog != null) {
            return;
        }
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.dialog_progress, null, false);
        TextView progressDialogMessage = (TextView) dialogView.findViewById(R.id.progressDialogMessage);
        progressDialogMessage.setText(message);
        progressDialog = new AlertDialog.Builder(this)
                .setCancelable(modal)
                .setView(dialogView)
                .show();
    }

    public void showProgressDialog() {
        if (progressDialog != null) {
            return;
        }
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.dialog_progress_no_text, null, false);
        progressDialog = new AlertDialog.Builder(this)
                .setCancelable(false)
                .setView(dialogView)
                .show();
    }

    public void hideProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }
    
    public void hideDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
            dialog = null;
        }
        if (disableDialog != null && disableDialog.isShowing()) {
            disableDialog.dismiss();
            disableDialog = null;
        }
        hideProgressDialog();
    }
    
    public void showAlertDialog(String message, DialogInterface.OnClickListener onClickListener) {
        hideDialog();
        dialog = new AlertDialog.Builder(BaseActivity.this)
                .setMessage(Html.fromHtml(message))
                .setPositiveButton(android.R.string.ok, onClickListener)
                .show();
    }

    public void showAlertDialog(String message) {
        hideDialog();
        dialog = new AlertDialog.Builder(BaseActivity.this)
                .setMessage(Html.fromHtml(message))
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return item.getItemId() == android.R.id.home;
    }
    
    public void showArmOptionsDialog(ArmOptionsView.ViewListener armListener) {
        hideDialog();
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_alarm_options, null, false);
        Rect displayRectangle = new Rect();
        Window window = getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(displayRectangle);
        int density= getResources().getDisplayMetrics().densityDpi;
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
        dialog = new AlertDialog.Builder(BaseActivity.this)
                .setCancelable(true)
                .setView(view)
                .show();
    }

    /**
     * Shows the disable alarm dialog with countdown. It is important that this 
     * dialog only be shown once and not relaunched when already displayed as
     * it resets the timer. 
     * 
     * @param alarmCodeListener
     * @param code
     * @param beep
     * @param timeRemaining
     */
    public void showAlarmDisableDialog(AlarmDisableView.ViewListener alarmCodeListener, 
                                       int code, boolean beep, int timeRemaining) {
        if(disableDialog != null && disableDialog.isShowing()) {
            return;
        }
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_alarm_disable, null, false);
        final AlarmDisableView alarmCodeView = view.findViewById(R.id.alarmDisableView);
        alarmCodeView.setListener(alarmCodeListener);
        alarmCodeView.setCode(code);
        alarmCodeView.startCountDown(timeRemaining);
        if(beep) {
            alarmCodeView.playContinuousBeep();
        }
        disableDialog = new AlertDialog.Builder(BaseActivity.this)
                .setCancelable(true)
                .setView(view)
                .show();

        disableDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                alarmCodeView.destroySoundUtils();
            }
        });
    }

    public void showExtendedForecastDialog(Daily daily) {
        hideDialog();
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_extended_forecast, null, false);
        Rect displayRectangle = new Rect();
        Window window = getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(displayRectangle);
        view.setMinimumWidth((int)(displayRectangle.width() * 0.7f));

        int density= getResources().getDisplayMetrics().densityDpi;
        if(density == DisplayMetrics.DENSITY_TV ) {
        } else if (density == DisplayMetrics.DENSITY_MEDIUM) {
            view.setMinimumHeight((int) (displayRectangle.height() * 0.6f));
        } else {
            view.setMinimumHeight((int)(displayRectangle.height() * 0.8f));
        }
        final ExtendedForecastView  extendedForecastView = view.findViewById(R.id.extendedForecastView);
        extendedForecastView.setExtendedForecast(daily, getConfiguration().getWeatherUnits());
        dialog = new AlertDialog.Builder(BaseActivity.this)
                .setCancelable(true)
                .setView(view)
                .show();
    }
    
    public void closeScreenSaver() {
        if(screenSaverDialog != null) {
            screenSaverDialog.dismiss();
            screenSaverDialog = null;
        }
    }

    /**
     * Show the screen saver only if the alarm isn't triggered.  This shouldn't be an issue
     * with the alarm disabled because the disable time will be longer than this. 
     */
    public void showScreenSaver() {
        if(getConfiguration().getAlarmMode().equals(PREF_TRIGGERED) 
                || getConfiguration().getAlarmMode().equals(PREF_TRIGGERED_PENDING)){
            return;
        }
        if (screenSaverDialog != null && screenSaverDialog.isShowing()) {
            return;
        }
        inactivityHandler.removeCallbacks(inactivityCallback);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_screen_saver, null, false);
        final ScreenSaverView screenSaverView = view.findViewById(R.id.screenSaverView);
        screenSaverView.setScreenSaver(BaseActivity.this, getConfiguration().showPhotoScreenSaver(),
                getConfiguration().getImageSource(), getConfiguration().getImageFitScreen(),
                getConfiguration().getImageRotation());
        screenSaverView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (screenSaverDialog != null) {
                    screenSaverDialog.dismiss();
                    screenSaverDialog = null;
                    resetInactivityTimer();
                }
            }
        });
        screenSaverDialog = new AlertDialog.Builder(BaseActivity.this, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
                .setCancelable(true)
                .setView(view)
                .show();
    }
}