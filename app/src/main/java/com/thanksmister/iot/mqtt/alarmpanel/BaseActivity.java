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

import android.app.Dialog;
import android.app.KeyguardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
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

import com.thanksmister.iot.mqtt.alarmpanel.data.stores.StoreManager;
import com.thanksmister.iot.mqtt.alarmpanel.network.model.Daily;
import com.thanksmister.iot.mqtt.alarmpanel.ui.Configuration;
import com.thanksmister.iot.mqtt.alarmpanel.ui.activities.MainActivity;
import com.thanksmister.iot.mqtt.alarmpanel.ui.activities.SettingsActivity;
import com.thanksmister.iot.mqtt.alarmpanel.ui.views.AlarmDisableView;
import com.thanksmister.iot.mqtt.alarmpanel.ui.views.ArmOptionsView;
import com.thanksmister.iot.mqtt.alarmpanel.ui.views.ExtendedForecastView;
import com.thanksmister.iot.mqtt.alarmpanel.ui.views.ScreenSaverView;
import com.thanksmister.iot.mqtt.alarmpanel.ui.views.SettingsCodeView;

import butterknife.ButterKnife;
import timber.log.Timber;

import static com.thanksmister.iot.mqtt.alarmpanel.ui.Configuration.PREF_TRIGGERED;
import static com.thanksmister.iot.mqtt.alarmpanel.ui.Configuration.PREF_TRIGGERED_PENDING;

abstract public class BaseActivity extends AppCompatActivity {
    
    private StoreManager storeManager;
    private Configuration configuration;
    private Dialog progressDialog;
    private Dialog dialog;
    private AlertDialog alertDialog;
    private Dialog disableDialog;
    private Dialog screenSaverDialog;
    private Handler inactivityHandler = new Handler();
    private View decorView;
    private PowerManager.WakeLock wakeLock;

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
        releaseTemporaryWakeLock();
    }

    @Override
    protected void onPause() {
        super.onPause();
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
        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
            alertDialog = null;
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
    
    /**
     * Wakes the device temporarily (or always if triggered) when the alarm requires attention.
     */
    public void acquireTemporaryWakeLock() {
        Timber.d("acquireTemporaryWakeLock");
        if(wakeLock == null) {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "ALARM_TEMPORARY_WAKE_TAG");
        }
        if (wakeLock != null && !wakeLock.isHeld()) {  // but we don't hold it
            wakeLock.acquire();
        }

        // Some Amazon devices are not seeing this permission so we are trying to check
        String permission = "android.permission.DISABLE_KEYGUARD";
        int checkSelfPermission = ContextCompat.checkSelfPermission(BaseActivity.this, permission);
        if(checkSelfPermission == PackageManager.PERMISSION_GRANTED) {
            KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
            KeyguardManager.KeyguardLock keyguardLock = keyguardManager.newKeyguardLock("ALARM_KEYBOARD_LOCK_TAG");
            keyguardLock.disableKeyguard();
        }
        if(!LifecycleHandler.isApplicationInForeground()) {
            Intent it = new Intent("intent.my.action");
            it.setComponent(new ComponentName(BaseActivity.this.getPackageName(), MainActivity.class.getName()));
            it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            BaseActivity.this.getApplicationContext().startActivity(it);
        }
    }
    
    /**
     * Wakelock used to temporarily bring application to foreground if alarm needs attention.
     */
    public void releaseTemporaryWakeLock() {
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
        }
    };

    public void resetInactivityTimer() {
        closeScreenSaver();
        if(inactivityHandler != null) {
            inactivityHandler.removeCallbacks(inactivityCallback);
            inactivityHandler.postDelayed(inactivityCallback, getConfiguration().getInactivityTime());
        }
    }

    public void stopDisconnectTimer(){
        if(inactivityHandler != null) {
            inactivityHandler.removeCallbacks(inactivityCallback);
        }
    }

    @Override
    public void onUserInteraction(){
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
    
    public void hideDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
            dialog = null;
        }
        if (disableDialog != null && disableDialog.isShowing()) {
            disableDialog.dismiss();
            disableDialog = null;
        }
        if (screenSaverDialog != null && screenSaverDialog.isShowing()) {
            screenSaverDialog.dismiss();
            screenSaverDialog = null;
        }
    }
    
    public void showAlertDialog(String message, DialogInterface.OnClickListener onClickListener) {
        hideDialog();
        alertDialog = new AlertDialog.Builder(BaseActivity.this)
                .setMessage(Html.fromHtml(message))
                .setPositiveButton(android.R.string.ok, onClickListener)
                .show();
    }

    public void showAlertDialog(String message) {
        hideDialog();
        alertDialog = new AlertDialog.Builder(BaseActivity.this)
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
        dialog = buildImmersiveDialog(true, view, false);
    }

    /**
     * Shows the disable alarm dialog with countdown. It is important that this 
     * dialog only be shown once and not relaunched when already displayed as
     * it resets the timer.
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
        disableDialog = buildImmersiveDialog(true, view, false);
        disableDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                alarmCodeView.destroySoundUtils();
            }
        });
    }

    public void showSettingsCodeDialog(final int code, final SettingsCodeView.ViewListener listener) {
        hideDialog();
        if(getConfiguration().isFirstTime()) {
            Intent intent = SettingsActivity.createStartIntent(BaseActivity.this);
            startActivity(intent);
        } else {
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.dialog_settings_code, null, false);
            final SettingsCodeView settingsCodeView = view.findViewById(R.id.settingsCodeView);
            settingsCodeView.setCode(code);
            settingsCodeView.setListener(listener);
            dialog = buildImmersiveDialog(true, view, false);
        }
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
        if (density == DisplayMetrics.DENSITY_MEDIUM) {
            view.setMinimumHeight((int) (displayRectangle.height() * 0.6f));
        } else {
            view.setMinimumHeight((int)(displayRectangle.height() * 0.8f));
        }
        final ExtendedForecastView  extendedForecastView = view.findViewById(R.id.extendedForecastView);
        extendedForecastView.setExtendedForecast(daily, getConfiguration().getWeatherUnits());
        dialog = buildImmersiveDialog(true, view, false);
    }
    
    public void closeScreenSaver() {
        // Override to handle screen saver close call
    }

    /**
     * Show the screen saver only if the alarm isn't triggered. This shouldn't be an issue
     * with the alarm disabled because the disable time will be longer than this. 
     */
    public void showScreenSaver() {
        if(getConfiguration().getAlarmMode().equals(PREF_TRIGGERED) 
                || getConfiguration().getAlarmMode().equals(PREF_TRIGGERED_PENDING)) {
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
        screenSaverView.setListener(new ScreenSaverView.ViewListener() {
            @Override
            public void onMotion() {
                if (screenSaverDialog != null) {
                    screenSaverDialog.dismiss();
                    screenSaverDialog = null;
                    resetInactivityTimer();
                }
            }
        });
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
        screenSaverDialog = buildImmersiveDialog(true, screenSaverView, true);
    }

    // immersive dialogs without navigation 
    // https://stackoverflow.com/questions/22794049/how-do-i-maintain-the-immersive-mode-in-dialogs
    private Dialog buildImmersiveDialog(boolean cancelable, View view, boolean fullscreen) {
        Dialog dialog;
        if(fullscreen) {
            dialog = new Dialog(BaseActivity.this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        } else {
            dialog = new Dialog(BaseActivity.this);
        }
        dialog.setCancelable(cancelable);
        dialog.setContentView(view);
        //Set the dialog to not focusable (makes navigation ignore us adding the window)			
        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        dialog.getWindow().getDecorView().setSystemUiVisibility(getWindow().getDecorView().getSystemUiVisibility());
        dialog.show();
        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        wm.updateViewLayout(getWindow().getDecorView(), getWindow().getAttributes());
        return dialog;
    }
}