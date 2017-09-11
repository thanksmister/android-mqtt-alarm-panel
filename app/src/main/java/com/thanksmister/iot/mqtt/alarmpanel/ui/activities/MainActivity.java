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
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.thanksmister.iot.mqtt.alarmpanel.BaseActivity;
import com.thanksmister.iot.mqtt.alarmpanel.R;
import com.thanksmister.iot.mqtt.alarmpanel.network.model.SubscriptionData;
import com.thanksmister.iot.mqtt.alarmpanel.tasks.SubscriptionDataTask;
import com.thanksmister.iot.mqtt.alarmpanel.ui.fragments.ControlsFragment;
import com.thanksmister.iot.mqtt.alarmpanel.ui.fragments.InformationFragment;
import com.thanksmister.iot.mqtt.alarmpanel.ui.views.AlarmDisableView;
import com.thanksmister.iot.mqtt.alarmpanel.ui.views.AlarmTriggeredView;
import com.thanksmister.iot.mqtt.alarmpanel.utils.AlarmUtils;
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

public class MainActivity extends BaseActivity implements ControlsFragment.OnControlsFragmentListener {
    
    private static final String FRAGMENT_CONTROLS = "com.thanksmister.fragment.FRAGMENT_CONTROLS";
    private static final String FRAGMENT_INFORMATION = "com.thanksmister.fragment.FRAGMENT_INFORMATION";

    @Bind(R.id.triggeredView)
    View triggeredView;

    @OnClick(R.id.buttonSettings)
    void buttonSettingsClicked() {
        if(!getConfiguration().isArmed()) {
            Intent intent = SettingsActivity.createStartIntent(MainActivity.this);
            startActivity(intent);
        } else {
            showAlarmDisableDialog(false);
        }
    }

    @OnClick(R.id.buttonLogs)
    void buttonLogsClicked() {
        Intent intent = LogActivity.createStartIntent(MainActivity.this);
        startActivity(intent);
    }
    
    @OnClick(R.id.buttonSleep)
    void buttonSleep() {
        showScreenSaver();
    }

    private SubscriptionDataTask subscriptionDataTask;
    private MqttAndroidClient mqttAndroidClient;
    private View decorView;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        decorView = getWindow().getDecorView();

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
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
        
       if (savedInstanceState == null) {
            ControlsFragment controlsFragment = ControlsFragment.newInstance();
            InformationFragment informationFragment = InformationFragment.newInstance();
            getSupportFragmentManager().beginTransaction().replace(R.id.controlContainer, controlsFragment, FRAGMENT_CONTROLS).commit();
            getSupportFragmentManager().beginTransaction().replace(R.id.informationContainer, informationFragment, FRAGMENT_INFORMATION).commit();
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        resetInactivityTimer();
        makeMqttConnection();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (subscriptionDataTask != null) {
            subscriptionDataTask.cancel(true);
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
    public void publishArmedHome() {
        String topic = AlarmUtils.COMMAND_TOPIC;
        String message = AlarmUtils.COMMAND_ARM_HOME;
        publishMessage(topic, message);
    }

    @Override
    public void publishArmedAway() {
        String topic = AlarmUtils.COMMAND_TOPIC;
        String message = AlarmUtils.COMMAND_ARM_AWAY;
        publishMessage(topic, message);
    }

    @Override
    public void publishDisarmed() {
        String topic = AlarmUtils.COMMAND_TOPIC;
        String message = AlarmUtils.COMMAND_DISARM;
        publishMessage(topic, message);
    }
    
    private void makeMqttConnection() {
        final boolean tlsConnection = getConfiguration().getTlsConnection();
        final String serverUri;
        if(tlsConnection) {
            serverUri = "ssl://" + getConfiguration().getBroker() + ":" + getConfiguration().getPort();
        } else {
            serverUri = "tcp://" + getConfiguration().getBroker() + ":" + getConfiguration().getPort();
        }

        Timber.d("Server Uri: " + serverUri);
        
        final String clientId = getConfiguration().getClientId();
        final String topic = getConfiguration().getStateTopic();

        MqttConnectOptions mqttConnectOptions = MqttUtils.getMqttConnectOptions(getConfiguration().getUserName(), getConfiguration().getPassword());
        mqttAndroidClient = MqttUtils.getMqttAndroidClient(getApplicationContext(), serverUri, clientId, topic, new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                if (reconnect) {
                    Timber.d("Reconnected to : " + serverURI);
                    // Because Clean Session is true, we need to re-subscribe
                    subscribeToTopic(topic);
                } else {
                    Timber.d("Connected to: " + serverURI);
                    
                }
            }

            @Override
            public void connectionLost(Throwable cause) {
                Timber.d("The Connection was lost.");
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                Timber.i("Sent Message : " + topic + " : " + new String(message.getPayload()));
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
            }
        });

        try {
            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                    disconnectedBufferOptions.setBufferEnabled(true);
                    disconnectedBufferOptions.setBufferSize(100);
                    disconnectedBufferOptions.setPersistBuffer(false);
                    disconnectedBufferOptions.setDeleteOldestMessages(false);
                    mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);
                    subscribeToTopic(topic);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Timber.e("Failed to connect to: " + serverUri + " exception: " + exception.getMessage());
                }
            });

        } catch (MqttException ex){
            ex.printStackTrace();
        }
    }

    private void subscribeToTopic(final String topic) {
        try {
            mqttAndroidClient.subscribe(topic, 0, new IMqttMessageListener() {
                @Override
                public void messageArrived(final String topic, final MqttMessage message) throws Exception {
                    // message Arrived!
                    Timber.i("Subscribe Message message : " + topic + " : " + new String(message.getPayload()));
                    subscriptionDataTask = getUpdateMqttDataTask();
                    subscriptionDataTask.execute(new SubscriptionData(topic, new String(message.getPayload()), String.valueOf(message.getId())));
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(AlarmUtils.hasSupportedStates(new String(message.getPayload()))) {
                                handleStateChange(new String(message.getPayload()));
                            }
                        }
                    });
                }
            });
        } catch (MqttException ex){
            Timber.e("Exception whilst subscribing");
            ex.printStackTrace();
            hideProgressDialog();
        }
    }

    private void publishMessage(String publishTopic, String publishMessage) {
        try {
            MqttMessage message = new MqttMessage();
            message.setPayload(publishMessage.getBytes());
            mqttAndroidClient.publish(publishTopic, message);
            Timber.d("Message Published: " + publishTopic);
            if(!mqttAndroidClient.isConnected()){
                //Timber.d(mqttAndroidClient.getBufferedMessageCount() + " messages in buffer.");
                showAlertDialog(getString(R.string.error_mqtt_connection), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        makeMqttConnection();
                    }
                });
                Timber.d("Unable to connect client.");
            }
        } catch (MqttException e) {
            Timber.e("Error Publishing: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handles the state change and shows triggered view and remove any dialogs or screen savers if 
     * state is triggered. Returns to normal state if disarmed from HASS.
     * @param state
     */
    @AlarmUtils.AlarmStates
    private void handleStateChange(String state) {
        if(AlarmUtils.STATE_TRIGGERED.equals(state)) {
            stopDisconnectTimer(); // stop screen saver mode
            closeScreenSaver(); // close screen saver
            triggeredView.setVisibility(View.VISIBLE);
            int code = getConfiguration().getAlarmCode();
            final AlarmTriggeredView disarmView = (AlarmTriggeredView) findViewById(R.id.alarmTriggeredView);
            disarmView.setCode(code);
            disarmView.setListener(new AlarmTriggeredView.ViewListener() {
                @Override
                public void onComplete(int code) {
                    publishDisarmed();
                }
                @Override
                public void onError() {
                    Toast.makeText(MainActivity.this, R.string.toast_code_invalid, Toast.LENGTH_SHORT).show();
                }
                @Override
                public void onCancel() {

                }
            });
        } else {
            resetInactivityTimer(); // restart screen saver
            if(triggeredView != null) {
                triggeredView.setVisibility(View.GONE);
            }
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
                hideProgressDialog();
                if (!response) {
                    Timber.e("Update Exception response: " + response);
                }
            }
        });
        return dataTask;
    }

    /**
     * Shows a count down dialog before setting alarm to away
     */
    private void showAlarmDisableDialog(boolean beep) {
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
        }, getConfiguration().getAlarmCode(), beep);
    }
}