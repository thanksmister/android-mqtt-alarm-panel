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

package com.thanksmister.androidthings.iot.alarmpanel.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;

import com.thanksmister.androidthings.iot.alarmpanel.BaseActivity;
import com.thanksmister.androidthings.iot.alarmpanel.R;
import com.thanksmister.androidthings.iot.alarmpanel.tasks.UpdateFeedDataTask;
import com.thanksmister.androidthings.iot.alarmpanel.ui.Configuration;
import com.thanksmister.androidthings.iot.alarmpanel.ui.fragments.ControlsFragment;
import com.thanksmister.androidthings.iot.alarmpanel.ui.fragments.InformationFragment;
import com.thanksmister.androidthings.iot.alarmpanel.ui.fragments.SensorsFragment;
import com.thanksmister.androidthings.iot.alarmpanel.ui.modules.MqttModule;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

public class MainActivity extends BaseActivity implements ControlsFragment.OnControlsFragmentListener {

    @Bind(R.id.buttonSettings)
    ImageButton buttonSettings;

    @OnClick(R.id.buttonSettings)
    void buttonSettingsClicked() {
        Intent intent = SettingsActivity.createStartIntent(MainActivity.this);
        startActivity(intent);
    }

    private UpdateFeedDataTask updateFeedDataTask;
    private MqttModule mqttModule;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
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
        getConfiguration().setTopic("home/alarm/set");
        getConfiguration().setUserName("homeassistant");
        getConfiguration().setPassword("2066");
        getConfiguration().setPort(1883);
        getConfiguration().setBroker("192.168.86.39");

        mqttModule = new MqttModule(getApplicationContext(), getConfiguration(), mqttModuleListener);
        mqttModule.makeMqttConnection();

        //SyncService.requestSyncNow(this);
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
                mqttModule.publishMessage(topic, jsonObject.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void publishArmedStay() {
        String topic = "home/alarm/";
        String message = "armed_home";
        mqttModule.publishMessage(topic, message);
        getConfiguration().setArmed(true);
        getConfiguration().setAlarmMode(Configuration.ARMED_STAY);
    }

    @Override
    public void publishArmedAway() {
        String topic = "home/alarm/";
        String message = "armed_away";
        mqttModule.publishMessage(topic, message);
        getConfiguration().setArmed(true);
        getConfiguration().setAlarmMode(Configuration.ARMED_STAY);
    }

    @Override
    public void publishDisarmed() {
        String topic = "home/alarm/";
        String message = "armed_home";
        mqttModule.publishMessage(topic, message);
        getConfiguration().setArmed(false);
        getConfiguration().setAlarmMode(Configuration.DISARMED);
    }

    @Override
    public void publishPending() {
        String topic = "home/alarm/";
        String message = "pending";
        mqttModule.publishMessage(topic, message);
    }

    @Override
    public void publishTriggered() {
        String topic = "home/alarm/";
        String message = "triggered";
        mqttModule.publishMessage(topic, message);
    }

    /**
     * Listen to the callbacks from the MQTT module and update the user interface
     * and record them to the log
     */
    private MqttModule.MqttModuleListener mqttModuleListener = new MqttModule.MqttModuleListener() {
        @Override
        public void subscriptionMessage(String topic, String message) {
            // TODO keep record of outgoing and incoming calls in a log
            showAlertDialog("Message Arrived", "Topic: " + topic + " Message: " + message);
            /*FeedData feedData = new FeedData();
            feedData.setValue(message);
            feedData.setCreatedAt(DateUtils.generateCreatedAtDate());
            getUpdateFeedDataTask().execute(feedData);*/
        }

        @Override
        public void subscriptionError(String message) {

        }

        @Override
        public void publishError(String message) {

        }
    };

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
                if (!response) {
                    Timber.e("Update Exception response: " + response);
                    showAlertDialog(getString(R.string.error_updating_message));
                }
            }
        });
        return updateFeedDataTask;
    }
}