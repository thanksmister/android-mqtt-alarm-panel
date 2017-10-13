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
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.thanksmister.iot.mqtt.alarmpanel.data.stores.StoreManager;
import com.thanksmister.iot.mqtt.alarmpanel.network.DarkSkyOptions;
import com.thanksmister.iot.mqtt.alarmpanel.network.InstagramOptions;
import com.thanksmister.iot.mqtt.alarmpanel.network.MQTTOptions;
import com.thanksmister.iot.mqtt.alarmpanel.network.model.Daily;
import com.thanksmister.iot.mqtt.alarmpanel.ui.Configuration;
import com.thanksmister.iot.mqtt.alarmpanel.ui.activities.SettingsActivity;
import com.thanksmister.iot.mqtt.alarmpanel.ui.views.AlarmDisableView;
import com.thanksmister.iot.mqtt.alarmpanel.ui.views.ArmOptionsView;
import com.thanksmister.iot.mqtt.alarmpanel.ui.views.SettingsCodeView;
import com.thanksmister.iot.mqtt.alarmpanel.utils.DialogUtils;
import com.thanksmister.iot.mqtt.alarmpanel.utils.NotificationUtils;

import java.util.concurrent.atomic.AtomicBoolean;

import butterknife.ButterKnife;
import dpreference.DPreference;
import timber.log.Timber;

import static com.thanksmister.iot.mqtt.alarmpanel.ui.Configuration.PREF_AWAY_TRIGGERED_PENDING;
import static com.thanksmister.iot.mqtt.alarmpanel.ui.Configuration.PREF_HOME_TRIGGERED_PENDING;
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
    protected AtomicBoolean hasNetwork = new AtomicBoolean(true);
    private NotificationUtils notificationUtils;
    
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
            configuration = new Configuration(baseApplication.getAppSharedPreferences());
        }
        return configuration;
    }

    public DPreference getSharedPreferences() {
        BaseApplication baseApplication = BaseApplication.getInstance();
        return baseApplication.getAppSharedPreferences();
    }

    public MQTTOptions readMqttOptions() {
        return MQTTOptions.from(getSharedPreferences());
    }

    public DarkSkyOptions readWeatherOptions() {
        return DarkSkyOptions.from(getSharedPreferences());
    }

    public InstagramOptions readImageOptions() {
        return InstagramOptions.from(getSharedPreferences());
    }

    public NotificationUtils getNotifications() {
        if(notificationUtils == null) {
            notificationUtils = new NotificationUtils(BaseActivity.this);
        }
        return notificationUtils;
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

    public void hideProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }
    
    public void hideDialog() {
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
        if (screenSaverDialog != null) {
            screenSaverDialog.dismiss();
            screenSaverDialog = null;
        }
    }
    
    public void hideDisableDialog() {
        if (disableDialog != null) {
            disableDialog.dismiss();
            disableDialog = null;
        }
    }

    public void hideAlertDialog() {
        if (alertDialog != null) {
            alertDialog.dismiss();
            alertDialog = null;
        }
    }
    
    public void showAlertDialog(String message, DialogInterface.OnClickListener onClickListener) {
        if(alertDialog != null && alertDialog.isShowing()) {
            return;
        }
        alertDialog = new AlertDialog.Builder(BaseActivity.this)
                .setMessage(Html.fromHtml(message))
                .setPositiveButton(android.R.string.ok, onClickListener)
                .show();
    }

    public void showAlertDialog(String title, String message) {
        if(alertDialog != null && alertDialog.isShowing()) {
            return;
        }
        alertDialog = new AlertDialog.Builder(BaseActivity.this)
                .setTitle(title)
                .setMessage(Html.fromHtml(message))
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    public void showAlertDialog(String message) {
        if(alertDialog != null && alertDialog.isShowing()) {
            return;
        }
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
        dialog = DialogUtils.showArmOptionsDialog(BaseActivity.this, armListener);
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
        Timber.d("showAlarmDisableDialog");
        disableDialog = DialogUtils.showAlarmDisableDialog(BaseActivity.this, alarmCodeListener, code, beep, timeRemaining);
    }

    public void showSettingsCodeDialog(final int code, final SettingsCodeView.ViewListener listener) {
        hideDialog();
        if(getConfiguration().isFirstTime()) {
            Intent intent = SettingsActivity.createStartIntent(BaseActivity.this);
            startActivity(intent);
        } else {
            dialog = DialogUtils.showSettingsCodeDialog(BaseActivity.this, code, listener);
        }
    }

    public void showExtendedForecastDialog(Daily daily) {
        hideDialog();
        dialog = DialogUtils.showExtendedForecastDialog(BaseActivity.this, daily, readWeatherOptions().getWeatherUnits());
    }

    /**
     * Close the screen saver. 
     */
    public void closeScreenSaver() {
        if (screenSaverDialog != null && screenSaverDialog.isShowing()) {
            screenSaverDialog.dismiss();
        }
    }

    /**
     * Show the screen saver only if the alarm isn't triggered. This shouldn't be an issue
     * with the alarm disabled because the disable time will be longer than this. 
     */
    public void showScreenSaver() {
        if(getConfiguration().getAlarmMode().equals(PREF_TRIGGERED)
                || getConfiguration().getAlarmMode().equals(PREF_HOME_TRIGGERED_PENDING)
                || getConfiguration().getAlarmMode().equals(PREF_AWAY_TRIGGERED_PENDING)
                || getConfiguration().getAlarmMode().equals(PREF_TRIGGERED_PENDING)) {
            return;
        }
        if (screenSaverDialog != null && screenSaverDialog.isShowing()) {
            return;
        }
        inactivityHandler.removeCallbacks(inactivityCallback);
        screenSaverDialog = DialogUtils.showScreenSaver(BaseActivity.this, getConfiguration().showPhotoScreenSaver(),
                readImageOptions().getImageSource(), readImageOptions().getImageFitScreen(),
                readImageOptions().getImageRotation(), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (screenSaverDialog != null) {
                            screenSaverDialog.dismiss();
                            screenSaverDialog = null;
                            resetInactivityTimer();
                        }
                    }
                });
    }

    /**
     * On network disconnect show notification or alert message, clear the 
     * screen saver and awake the screen. Override this method in activity 
     * to for extra network disconnect handling such as bring application
     * into foreground.
     */
    public void handleNetworkDisconnect() {
        if (getConfiguration().showNotifications()) {
            getNotifications().createAlarmNotification(getString(R.string.text_notification_network_title),
                    getString(R.string.text_notification_network_description));
        } else {
            closeScreenSaver();
            acquireTemporaryWakeLock();
            showAlertDialog(getString(R.string.text_notification_network_title),
                    getString(R.string.text_notification_network_description));
        }
    }

    /**
     * On network connect hide any alert dialogs generated by
     * the network disconnect and clear any notifications.
     */
    public void handleNetworkConnect() {
        hideAlertDialog();
        getNotifications().clearNotification();
    }
    
    public boolean hasNetworkConnectivity() {
        return hasNetwork.get();
    }
}