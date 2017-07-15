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

package com.thanksmister.androidthings.iot.alarmpanel;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.thanksmister.androidthings.iot.alarmpanel.network.model.FeedData;
import com.thanksmister.androidthings.iot.alarmpanel.tasks.UpdateFeedDataTask;
import com.thanksmister.androidthings.iot.alarmpanel.ui.activities.SettingsActivity;
import com.thanksmister.androidthings.iot.alarmpanel.ui.fragments.ControlsFragment;
import com.thanksmister.androidthings.iot.alarmpanel.ui.fragments.InformationFragment;
import com.thanksmister.androidthings.iot.alarmpanel.ui.fragments.SensorsFragment;
import com.thanksmister.androidthings.iot.alarmpanel.utils.DateUtils;
import com.thanksmister.androidthings.iot.alarmpanel.utils.MQTTUtils;

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
import org.json.JSONException;
import org.json.JSONObject;

import butterknife.ButterKnife;
import timber.log.Timber;

public class MainActivity extends BaseActivity {

    // The loader's unique id. Loader ids are specific to the Activity or
    private MqttAndroidClient mqttAndroidClient;
    private UpdateFeedDataTask updateFeedDataTask;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayUseLogoEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        if (savedInstanceState == null) {
            ControlsFragment controlsFragment = ControlsFragment.newInstance();
            InformationFragment informationFragment = InformationFragment.newInstance();
            SensorsFragment sensorsFragment = SensorsFragment.newInstance();
            getSupportFragmentManager().beginTransaction().replace(R.id.controlContainer, controlsFragment).commit();
            getSupportFragmentManager().beginTransaction().replace(R.id.informationContainer, informationFragment).commit();
            getSupportFragmentManager().beginTransaction().replace(R.id.sensorsContainer, sensorsFragment).commit();
        }
        
        // TODO move these to settings
        getConfiguration().setTopic("thanksmister/feeds/maindoor");
        getConfiguration().setUserName("thanksmister");
        getConfiguration().setPassword("036a464017f8466aa236d06fd30ef44c");
        getConfiguration().setPort(8883);
        getConfiguration().setBroker("io.adafruit.com");

        //SyncService.requestSyncNow(this);
        makeMqttConnection();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (this.updateFeedDataTask != null) {
            this.updateFeedDataTask.cancel(true);
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
        } else if (id == R.id.action_publish) {
            final String topic = getConfiguration().getTopic();
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("value", "close");
                publishMessage(topic, jsonObject.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return super.onOptionsItemSelected(item);
    }
    
    private void makeMqttConnection() {

        // TODO test secure connection
        final boolean tlsConnection = false;
        final String serverUri;
        if(tlsConnection) {
            serverUri = "ssl://" + getConfiguration().getBroker() + ":" + getConfiguration().getPort();
        } else {
            serverUri = "tcp://" + getConfiguration().getBroker() + ":1883";
        }
        
        Timber.d("Server Uri: " + serverUri);
        final String clientId = getConfiguration().getClientId();
        final String topic = getConfiguration().getTopic();
        
        MqttConnectOptions mqttConnectOptions = MQTTUtils.getMqttConnectOptions(getConfiguration().getUserName(), getConfiguration().getPassword());
        mqttAndroidClient = MQTTUtils.getMqttAndroidClient(getApplicationContext(), serverUri, clientId, topic, new MqttCallbackExtended() {
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
                Timber.d("Incoming message: " + new String(message.getPayload()));
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
            // TODO do we need this one?
            mqttAndroidClient.subscribe(topic, 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Timber.d("Subscribed!");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Timber.e("Failed to subscribe");
                }
            });

            // THIS DOES NOT WORK!
            mqttAndroidClient.subscribe(topic, 0, new IMqttMessageListener() {
                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    // message Arrived!
                    Timber.i("Message: " + topic + " : " + new String(message.getPayload()));
                    FeedData feedData = new FeedData();
                    feedData.setValue(new String(message.getPayload()));
                    feedData.setCreatedAt(DateUtils.generateCreatedAtDate());
                    UpdateFeedDataTask updateFeedDataTask = getUpdateFeedDataTask();
                    updateFeedDataTask.execute(feedData);
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
                Timber.d(mqttAndroidClient.getBufferedMessageCount() + " messages in buffer.");
            }
        } catch (MqttException e) {
            Timber.e("Error Publishing: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private UpdateFeedDataTask getUpdateFeedDataTask() {
        UpdateFeedDataTask updateFeedDataTask = new UpdateFeedDataTask(getStoreManager());
        updateFeedDataTask.setOnExceptionListener(new UpdateFeedDataTask.OnExceptionListener() {
            public void onException(Exception exception) {
                Timber.e("Update Exception: " + exception.getMessage());
                hideProgressDialog();
                showAlertDialog(getString(R.string.error_updating_message));
            }
        });
        updateFeedDataTask.setOnCompleteListener(new UpdateFeedDataTask.OnCompleteListener<Boolean>() {
            public void onComplete(Boolean response) {
                hideProgressDialog();
                if(!response) {
                    showAlertDialog(getString(R.string.error_updating_message));
                }
            }
        });
        return updateFeedDataTask;
    }
}