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

package com.thanksmister.iot.mqtt.alarmpanel.ui.activities;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.widget.Toast;

import com.thanksmister.iot.mqtt.alarmpanel.BaseActivity;
import com.thanksmister.iot.mqtt.alarmpanel.LifecycleHandler;
import com.thanksmister.iot.mqtt.alarmpanel.R;
import com.thanksmister.iot.mqtt.alarmpanel.network.MQTTService;
import com.thanksmister.iot.mqtt.alarmpanel.network.model.SubscriptionData;
import com.thanksmister.iot.mqtt.alarmpanel.tasks.SubscriptionDataTask;
import com.thanksmister.iot.mqtt.alarmpanel.ui.Configuration;
import com.thanksmister.iot.mqtt.alarmpanel.ui.controls.CustomViewPager;
import com.thanksmister.iot.mqtt.alarmpanel.ui.fragments.ControlsFragment;
import com.thanksmister.iot.mqtt.alarmpanel.ui.fragments.MainFragment;
import com.thanksmister.iot.mqtt.alarmpanel.ui.fragments.PlatformFragment;
import com.thanksmister.iot.mqtt.alarmpanel.ui.views.AlarmDisableView;
import com.thanksmister.iot.mqtt.alarmpanel.ui.views.SettingsCodeView;
import com.thanksmister.iot.mqtt.alarmpanel.utils.AlarmUtils;
import com.thanksmister.iot.mqtt.alarmpanel.utils.NotificationUtils;

import org.eclipse.paho.client.mqttv3.MqttException;

import butterknife.Bind;
import butterknife.ButterKnife;
import timber.log.Timber;

import static com.thanksmister.iot.mqtt.alarmpanel.ui.Configuration.PREF_ARM_AWAY;
import static com.thanksmister.iot.mqtt.alarmpanel.ui.Configuration.PREF_AWAY_TRIGGERED_PENDING;
import static com.thanksmister.iot.mqtt.alarmpanel.ui.Configuration.PREF_HOME_TRIGGERED_PENDING;
import static com.thanksmister.iot.mqtt.alarmpanel.ui.Configuration.PREF_TRIGGERED;
import static com.thanksmister.iot.mqtt.alarmpanel.ui.Configuration.PREF_TRIGGERED_PENDING;

public class MainActivity extends BaseActivity implements ViewPager.OnPageChangeListener, 
        MainFragment.OnMainFragmentListener, ControlsFragment.OnControlsFragmentListener, 
        MQTTService.MqttManagerListener  {

    private final int NUM_PAGES = 2;
    
    @Bind(R.id.viewPager)
    CustomViewPager viewPager;

    private SubscriptionDataTask subscriptionDataTask;
    private PagerAdapter pagerAdapter;
    private MQTTService mqttService;
    private Handler releaseWakeHandler;
    private NotificationUtils notificationUtils;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        ButterKnife.bind(this);

        if(getConfiguration().isFirstTime()) {
            showAlertDialog(getString(R.string.dialog_first_time), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    getConfiguration().setAlarmCode(1234); // set default code
                    Intent intent = SettingsActivity.createStartIntent(MainActivity.this);
                    startActivity(intent);
                }
            });
        }

        pagerAdapter = new MainSlidePagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(pagerAdapter);
        viewPager.addOnPageChangeListener(this);
        viewPager.setPagingEnabled(false);
        
        if(notificationUtils == null) {
            notificationUtils = new NotificationUtils(MainActivity.this);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
    
    @Override
    public void onResume() {
        super.onResume();
        initializeMqttService();
        resetInactivityTimer();
        setViewPagerState();
    }

    private void setViewPagerState() {
        if(viewPager != null) {
            if (getConfiguration().showHassModule()
                    && !TextUtils.isEmpty(getConfiguration().getHassUrl())) {
                viewPager.setPagingEnabled(true);
            } else {
                viewPager.setPagingEnabled(false);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Timber.d("onDestroy");
        if (subscriptionDataTask != null) {
            subscriptionDataTask.cancel(true);
            subscriptionDataTask = null;
        }
        if(releaseWakeHandler != null) {
            releaseWakeHandler.removeCallbacks(releaseWakeLockRunnable);
            releaseWakeHandler = null;
        }
        clearMqttService();
    }

    @Override
    public void onBackPressed() {
        if(viewPager != null) {
            if (viewPager.getCurrentItem() == 0) {
                // If the user is currently looking at the first step, allow the system to handle the
                // Back button. This calls finish() on this activity and pops the back stack.
                super.onBackPressed();
            } else {
                // Otherwise, select the previous step.
                viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
            }
        }
    }

    /**
     * We need to initialize or reset the MQTT service if our setup changes or
     * the activity is destroyed.  
     */
    private void initializeMqttService() {
        Timber.d("initializeMqttService");
        if (mqttService == null) {
            try {
                mqttService = new MQTTService(getApplicationContext(), readMqttOptions(), this);
            } catch (Throwable t) {
                // TODO should we loop back and try again? 
                Timber.e("Could not create MQTTPublisher" + t.getMessage());
            }
        } else if (readMqttOptions().hasUpdates()) {
            Timber.d("readMqttOptions().hasUpdates(): " + readMqttOptions().hasUpdates());
            try {
                mqttService.reconfigure(readMqttOptions());
            } catch (Throwable t) {
                // TODO should we loop back and try again? 
                Timber.e("Could not create MQTTPublisher" + t.getMessage());
            }
        }
    }
    
    private void clearMqttService() {
        Timber.d("clearMqttService");
        if(mqttService != null) {
            try {
                mqttService.close();
            } catch (MqttException e) {
                e.printStackTrace();
            }
            mqttService = null;
        }
    }
    
    public void publishArmedHome() {
        if(mqttService != null) {
            mqttService.publish(AlarmUtils.COMMAND_ARM_HOME);
        }
    }

    @Override
    public void publishArmedAway() {
        if(mqttService != null) {
            mqttService.publish(AlarmUtils.COMMAND_ARM_AWAY);
        }
    }

    @Override
    public void publishDisarmed() {
        if(mqttService != null) {
            mqttService.publish(AlarmUtils.COMMAND_DISARM);
        }
    }

    @Override
    public void showAlarmDisableDialog(boolean beep, int timeRemaining) {
        showAlarmDisableDialog(new AlarmDisableView.ViewListener() {
            @Override
            public void onComplete(int pin) {
                publishDisarmed();
                hideDisableDialog();
            }
            @Override
            public void onError() {
                Toast.makeText(MainActivity.this, R.string.toast_code_invalid, Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onCancel() {
                hideDisableDialog();
            }
        }, getConfiguration().getAlarmCode(), beep, timeRemaining);
    }

    @Override
    public void showSettingsCodeDialog() {
        showSettingsCodeDialog(getConfiguration().getAlarmCode(), new SettingsCodeView.ViewListener() {
            @Override
            public void onComplete(int code) {
                if (code == getConfiguration().getAlarmCode()) {
                    Intent intent = SettingsActivity.createStartIntent(MainActivity.this);
                    startActivity(intent);
                }
            }
            @Override
            public void onError() {
                Timber.d("Toast must work!!!");
                Toast.makeText(MainActivity.this, R.string.toast_code_invalid, Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onCancel() {
                hideDialog();
            }
        });
    }

    /**
     * Handles the state change and shows triggered view and remove any dialogs or screen savers if 
     * state is triggered. Returns to normal state if disarmed from HASS.
     */
    @AlarmUtils.AlarmStates
    private void handleStateChange(String state) {
        Timber.d("state: " + state);
        Timber.d("mode: " + getConfiguration().getAlarmMode());
        switch (state) {
            case AlarmUtils.STATE_DISARM:
                awakenDeviceForAction();
                releaseWakeHandler = new Handler();
                releaseWakeHandler.postDelayed(releaseWakeLockRunnable, 10000);
                notificationUtils.clearNotification();
                resetInactivityTimer();
                break;
            case AlarmUtils.STATE_ARM_AWAY:
            case AlarmUtils.STATE_ARM_HOME:
                resetInactivityTimer();
                break;
            case AlarmUtils.STATE_PENDING:
                if (getConfiguration().getAlarmMode().equals(Configuration.PREF_ARM_HOME)
                        || getConfiguration().getAlarmMode().equals(PREF_ARM_AWAY)) {
                    if (!PREF_TRIGGERED_PENDING.equals(getConfiguration().getAlarmMode()) 
                            && getConfiguration().showNotifications()) {
                        notificationUtils.createAlarmNotification(getString(R.string.text_notification_entry_title), getString(R.string.text_notification_entry_description));
                    }
                    if (getConfiguration().getAlarmMode().equals(Configuration.PREF_ARM_HOME)){
                        getConfiguration().setAlarmMode(PREF_HOME_TRIGGERED_PENDING);
                    } else if(getConfiguration().getAlarmMode().equals(PREF_ARM_AWAY)) {
                        getConfiguration().setAlarmMode(PREF_AWAY_TRIGGERED_PENDING);
                    } else {
                        getConfiguration().setAlarmMode(PREF_TRIGGERED_PENDING);
                    }
                    awakenDeviceForAction();
                } else {
                    awakenDeviceForAction();
                    releaseWakeHandler = new Handler();
                    releaseWakeHandler.postDelayed(releaseWakeLockRunnable, 10000);
                }
                break;
            case AlarmUtils.STATE_TRIGGERED:
                if (!PREF_TRIGGERED.equals(getConfiguration().getAlarmMode()) 
                        && getConfiguration().showNotifications()) {
                    notificationUtils.createAlarmNotification(getString(R.string.text_notification_trigger_title), getString(R.string.text_notification_trigger_description));
                }
                getConfiguration().setAlarmMode(PREF_TRIGGERED);
                awakenDeviceForAction();
                break;
            default:
                break; 
        }
    }

    /**
     * Temporarily wake the screen so we can do notify the user of pending alarm and
     * then allow the device to sleep again as needed after set amount of time. 
     */
    private Runnable releaseWakeLockRunnable = new Runnable() {
        @Override
        public void run() {
            releaseTemporaryWakeLock();
        }
    };

    /**
     * We need to awaken the device and allow the user to take action when the
     * user needs to disarm the control panel on entry or alarm triggered.
     */
    public void awakenDeviceForAction() {
        acquireTemporaryWakeLock();
        stopDisconnectTimer(); // stop screen saver mode
        closeScreenSaver(); // close screen saver
        if(viewPager != null && pagerAdapter != null && pagerAdapter.getCount() > 0) {
            hideDialog();
            viewPager.setCurrentItem(0);
        }
        if(!LifecycleHandler.isApplicationInForeground()) {
            Intent intent = new Intent("intent.alarm.action");
            intent.setComponent(new ComponentName(MainActivity.this.getPackageName(), MainActivity.class.getName()));
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        }
    }
    
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    @Override
    public void subscriptionMessage(final String topic, final String payload, final String id) {
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // we don't want to save duplicates when the application is brought to the foreground if the mode is the same
                Timber.d("topic: " + topic);
                Timber.d("payload: " + payload);
                if(AlarmUtils.hasSupportedStates(payload)) {
                    subscriptionDataTask = getUpdateMqttDataTask();
                    subscriptionDataTask.execute(new SubscriptionData(topic, payload, id));
                    handleStateChange(payload);
                }
            }
        });
    }

    @Override
    public void handleMqttException(final String errorMessage) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(!getConfiguration().isFirstTime() && readMqttOptions().isValid()) {
                    showAlertDialog(errorMessage);
                }
            }
        });
    }

    @Override
    public void handleMqttDisconnected() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showAlertDialog(getString(R.string.error_mqtt_connection), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        subscriptionDataTask = getUpdateMqttDataTask();
                        subscriptionDataTask.execute(new SubscriptionData(readMqttOptions().getStateTopic(), AlarmUtils.STATE_ERROR, "0"));
                        initializeMqttService();
                    }
                });
                Timber.d("Unable to connect client.");
            }
        });
    }

    @Override
    public void manuallyLaunchScreenSaver() {
        showScreenSaver();
    }

    private class MainSlidePagerAdapter extends FragmentStatePagerAdapter {
        public MainSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return MainFragment.newInstance();
                case 1:
                    return PlatformFragment.newInstance();
                default:
                    return MainFragment.newInstance();
            }
        }
        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }

    private SubscriptionDataTask getUpdateMqttDataTask() {
        SubscriptionDataTask dataTask = new SubscriptionDataTask(getStoreManager());
        dataTask.setOnExceptionListener(new SubscriptionDataTask.OnExceptionListener() {
            public void onException(Exception exception) {
                Timber.e("Update Exception: " + exception.getMessage());
            }
        });
        dataTask.setOnCompleteListener(new SubscriptionDataTask.OnCompleteListener<Boolean>() {
            public void onComplete(Boolean response) {
                if (!response) {
                    Timber.e("Update Exception response: " + response);
                }
            }
        });
        return dataTask;
    }
}