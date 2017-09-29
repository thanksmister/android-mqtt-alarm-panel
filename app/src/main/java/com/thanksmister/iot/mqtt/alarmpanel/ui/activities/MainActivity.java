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
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.thanksmister.iot.mqtt.alarmpanel.BaseActivity;
import com.thanksmister.iot.mqtt.alarmpanel.R;
import com.thanksmister.iot.mqtt.alarmpanel.network.MqttManager;
import com.thanksmister.iot.mqtt.alarmpanel.network.model.SubscriptionData;
import com.thanksmister.iot.mqtt.alarmpanel.tasks.SubscriptionDataTask;
import com.thanksmister.iot.mqtt.alarmpanel.ui.Configuration;
import com.thanksmister.iot.mqtt.alarmpanel.ui.controls.CustomViewPager;
import com.thanksmister.iot.mqtt.alarmpanel.ui.fragments.ControlsFragment;
import com.thanksmister.iot.mqtt.alarmpanel.ui.fragments.HomeAssistantFragment;
import com.thanksmister.iot.mqtt.alarmpanel.ui.fragments.MainFragment;
import com.thanksmister.iot.mqtt.alarmpanel.ui.views.AlarmDisableView;
import com.thanksmister.iot.mqtt.alarmpanel.utils.AlarmUtils;
import com.thanksmister.iot.mqtt.alarmpanel.utils.NotificationUtils;

import butterknife.Bind;
import butterknife.ButterKnife;
import timber.log.Timber;

import static com.thanksmister.iot.mqtt.alarmpanel.ui.Configuration.PREF_ARM_AWAY;
import static com.thanksmister.iot.mqtt.alarmpanel.ui.Configuration.PREF_TRIGGERED;
import static com.thanksmister.iot.mqtt.alarmpanel.ui.Configuration.PREF_TRIGGERED_PENDING;

public class MainActivity extends BaseActivity implements ViewPager.OnPageChangeListener, 
        MainFragment.OnMainFragmentListener, ControlsFragment.OnControlsFragmentListener, 
        MqttManager.MqttManagerListener  {

    private final int NUM_PAGES = 2;
    
    @Bind(R.id.viewPager)
    CustomViewPager viewPager;

    private SubscriptionDataTask subscriptionDataTask;
    private PagerAdapter pagerAdapter;
    private MqttManager mqttManager;
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
        if(mqttManager == null) {
            mqttManager = new MqttManager(this);
        } 
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
    
    @Override
    public void onResume() {
        super.onResume();
        resetInactivityTimer();
        if(mqttManager != null) {
            makeMqttConnection();
        }
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
        if (subscriptionDataTask != null) {
            subscriptionDataTask.cancel(true);
            subscriptionDataTask = null;
        }
        if(releaseWakeHandler != null) {
            releaseWakeHandler.removeCallbacks(releaseWakeLockRunnable);
            releaseWakeHandler = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.global, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.action_settings) {
            Intent intent = SettingsActivity.createStartIntent(MainActivity.this);
            startActivity(intent);
        } 
        return super.onOptionsItemSelected(item);
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

    private void makeMqttConnection() {
        mqttManager.makeMqttConnection(MainActivity.this, getConfiguration().getTlsConnection(),
                getConfiguration().getBroker(), getConfiguration().getPort(), getConfiguration().getClientId(),
                getConfiguration().getStateTopic(), getConfiguration().getUserName(), getConfiguration().getPassword());
    }

    public void publishArmedHome() {
        String topic = AlarmUtils.COMMAND_TOPIC;
        String message = AlarmUtils.COMMAND_ARM_HOME;
        if(mqttManager != null) {
            mqttManager.publishMessage(topic, message);
        }
    }

    @Override
    public void publishArmedAway() {
        String topic = AlarmUtils.COMMAND_TOPIC;
        String message = AlarmUtils.COMMAND_ARM_AWAY;
        if(mqttManager != null) {
            mqttManager.publishMessage(topic, message);
        }
    }

    @Override
    public void showAlarmDisableDialog(boolean beep, int timeRemaining) {
        showAlarmDisableDialog(new AlarmDisableView.ViewListener() {
            @Override
            public void onComplete(int pin) {
                publishDisarmed();
                hideDialog();
            }
            @Override
            public void onError() {
                Toast.makeText(MainActivity.this, R.string.toast_code_invalid, Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onCancel() {
                hideDialog();
            }
        }, getConfiguration().getAlarmCode(), beep, timeRemaining);
    }

    @Override
    public void publishDisarmed() {
        String topic = AlarmUtils.COMMAND_TOPIC;
        String message = AlarmUtils.COMMAND_DISARM;
        if(mqttManager != null) {
            mqttManager.publishMessage(topic, message);
        }
    }

    /**
     * Handles the state change and shows triggered view and remove any dialogs or screen savers if 
     * state is triggered. Returns to normal state if disarmed from HASS.
     */
    @AlarmUtils.AlarmStates
    private void handleStateChange(String state) {
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
                    getConfiguration().setAlarmMode(PREF_TRIGGERED_PENDING);
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
            releaseWakeLock();
        }
    };

    /**
     * We need to awaken the device and allow the user to take action when the
     * user needs to disarm the control panel on entry or alarm triggered.
     */
    public void awakenDeviceForAction() {
        acquireWakeLock();
        stopDisconnectTimer(); // stop screen saver mode
        closeScreenSaver(); // close screen saver
        if(viewPager != null && pagerAdapter != null && pagerAdapter.getCount() > 0) {
            hideDialog();
            viewPager.setCurrentItem(0);
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
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showAlertDialog(errorMessage);
            }
        });
    }

    @Override
    public void handleMqttDisconnected() {
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showAlertDialog(getString(R.string.error_mqtt_connection), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        subscriptionDataTask = getUpdateMqttDataTask();
                        subscriptionDataTask.execute(new SubscriptionData(getConfiguration().getStateTopic(), AlarmUtils.STATE_ERROR, "0"));
                        makeMqttConnection();
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
                    return HomeAssistantFragment.newInstance();
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