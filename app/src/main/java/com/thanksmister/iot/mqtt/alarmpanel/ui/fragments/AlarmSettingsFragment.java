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

package com.thanksmister.iot.mqtt.alarmpanel.ui.fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.thanksmister.iot.mqtt.alarmpanel.BaseActivity;
import com.thanksmister.iot.mqtt.alarmpanel.R;
import com.thanksmister.iot.mqtt.alarmpanel.ui.Configuration;
import com.thanksmister.iot.mqtt.alarmpanel.ui.views.AlarmCodeView;
import com.thanksmister.iot.mqtt.alarmpanel.utils.DialogUtils;

import static com.thanksmister.iot.mqtt.alarmpanel.ui.Configuration.PREF_ALARM_CODE;
import static com.thanksmister.iot.mqtt.alarmpanel.ui.Configuration.PREF_BROKER;
import static com.thanksmister.iot.mqtt.alarmpanel.ui.Configuration.PREF_CLIENT_ID;
import static com.thanksmister.iot.mqtt.alarmpanel.ui.Configuration.PREF_COMMAND_TOPIC;
import static com.thanksmister.iot.mqtt.alarmpanel.ui.Configuration.PREF_NOTIFICATIONS;
import static com.thanksmister.iot.mqtt.alarmpanel.ui.Configuration.PREF_PASSWORD;
import static com.thanksmister.iot.mqtt.alarmpanel.ui.Configuration.PREF_PENDING_TIME;
import static com.thanksmister.iot.mqtt.alarmpanel.ui.Configuration.PREF_PORT;
import static com.thanksmister.iot.mqtt.alarmpanel.ui.Configuration.PREF_STATE_TOPIC;
import static com.thanksmister.iot.mqtt.alarmpanel.ui.Configuration.PREF_TLS_CONNECTION;
import static com.thanksmister.iot.mqtt.alarmpanel.ui.Configuration.PREF_USERNAME;

public class AlarmSettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    private EditTextPreference brokerPreference;
    private EditTextPreference clientPreference;
    private EditTextPreference portPreference;
    private EditTextPreference commandTopicPreference;
    private EditTextPreference stateTopicPreference;
    private EditTextPreference userNamePreference;
    private EditTextPreference passwordPreference;
    private EditTextPreference pendingPreference;
    private CheckBoxPreference sslPreference;
    private CheckBoxPreference notificationsPreference;
    private Configuration configuration;
    private Dialog alarmCodeDialog;
    private int defaultCode;
    private int tempCode;
    private boolean confirmCode = false;
  
    /**
     * Called during {@link #onCreate(Bundle)} to supply the preferences for this fragment.
     * Subclasses are expected to call {@link #setPreferenceScreen(PreferenceScreen)} either
     * directly or via helper methods such as {@link #addPreferencesFromResource(int)}.
     *
     * @param savedInstanceState If the fragment is being re-created from
     * a previous saved state, this is the state.
     * @param rootKey If non-null, this preference fragment should be rooted at the
     * {@link PreferenceScreen} with this key.
     */
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        
        super.onViewCreated(view, savedInstanceState);

        Preference buttonPreference = findPreference(PREF_ALARM_CODE);
        buttonPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                showAlarmCodeDialog();
                return true;
            }
        });

        brokerPreference = (EditTextPreference) findPreference(PREF_BROKER);
        clientPreference = (EditTextPreference) findPreference(PREF_CLIENT_ID);
        portPreference = (EditTextPreference) findPreference(PREF_PORT);
        commandTopicPreference = (EditTextPreference) findPreference(PREF_COMMAND_TOPIC);
        stateTopicPreference = (EditTextPreference) findPreference(PREF_STATE_TOPIC);
        userNamePreference = (EditTextPreference) findPreference(PREF_USERNAME);
        passwordPreference = (EditTextPreference) findPreference(PREF_PASSWORD);
        pendingPreference = (EditTextPreference) findPreference(PREF_PENDING_TIME);
        sslPreference = (CheckBoxPreference) findPreference(PREF_TLS_CONNECTION);
        notificationsPreference = (CheckBoxPreference) findPreference(PREF_NOTIFICATIONS);
        
        if(isAdded()) {
            configuration = ((BaseActivity) getActivity()).getConfiguration();
        }
        
        brokerPreference.setText(configuration.getBroker());
        clientPreference.setText(String.valueOf(configuration.getClientId()));
        portPreference.setText(String.valueOf(configuration.getPort()));
        commandTopicPreference.setText(configuration.getCommandTopic());
        stateTopicPreference.setText(configuration.getStateTopic());
        userNamePreference.setText(configuration.getUserName());
        passwordPreference.setText(configuration.getPassword());
        pendingPreference.setText(String.valueOf(configuration.getPendingTime()));
        sslPreference.setChecked(configuration.getTlsConnection());
        notificationsPreference.setChecked(configuration.showNotifications());

        if(!TextUtils.isEmpty(configuration.getBroker())) {
            brokerPreference.setSummary(configuration.getBroker());
        }
        if(!TextUtils.isEmpty(configuration.getClientId())) {
            clientPreference.setSummary(configuration.getClientId());
        }
        if(!TextUtils.isEmpty(String.valueOf(configuration.getPort()))) {
            portPreference.setSummary(String.valueOf(configuration.getPort()));
        }
        if(!TextUtils.isEmpty(configuration.getCommandTopic())) {
            commandTopicPreference.setSummary(configuration.getCommandTopic());
        }
        if(!TextUtils.isEmpty(configuration.getStateTopic())) {
            stateTopicPreference.setSummary(configuration.getStateTopic());
        }
        if(!TextUtils.isEmpty(configuration.getUserName())) {
            userNamePreference.setSummary(configuration.getUserName());
        }
        if(!TextUtils.isEmpty(configuration.getPassword())) {
            passwordPreference.setSummary(toStars(configuration.getPassword()));
        }
        pendingPreference.setSummary(getString(R.string.preference_summary_pending_time, String.valueOf(configuration.getPendingTime())));
        
        // the first time we need to set the alarm code
        if(configuration.isFirstTime()) {
            showAlarmCodeDialog();
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        String value;
        switch (key) {
            case PREF_BROKER:
                value = brokerPreference.getText();
                if (!TextUtils.isEmpty(value)) {
                    configuration.setBroker(value);
                    brokerPreference.setSummary(value);
                } else if(isAdded()) {
                    Toast.makeText(getActivity(), R.string.text_error_blank_entry, Toast.LENGTH_SHORT).show();
                }
                break;
            case PREF_CLIENT_ID:
                value = clientPreference.getText();
                if (!TextUtils.isEmpty(value)) {
                    configuration.setClientId(value);
                    clientPreference.setSummary(value);
                } else if(isAdded()) {
                    Toast.makeText(getActivity(), R.string.text_error_blank_entry, Toast.LENGTH_LONG).show();
                    clientPreference.setText(configuration.getClientId());
                }
                break;
            case PREF_PORT:
                value = portPreference.getText();
                if (value.matches("[0-9]+") && !TextUtils.isEmpty(value)) {
                    configuration.setPort(Integer.valueOf(value));
                    portPreference.setSummary(String.valueOf(value));
                } else if(isAdded()) {
                    Toast.makeText(getActivity(), R.string.text_error_only_numbers, Toast.LENGTH_LONG).show();
                    portPreference.setText(String.valueOf(configuration.getPort()));
                }
                break;
            case PREF_COMMAND_TOPIC:
                value = commandTopicPreference.getText();
                if (!TextUtils.isEmpty(value)) {
                    configuration.setCommandTopic(value);
                    commandTopicPreference.setSummary(value);
                } else if(isAdded()) {
                    Toast.makeText(getActivity(), R.string.text_error_blank_entry, Toast.LENGTH_LONG).show();
                    commandTopicPreference.setText(configuration.getCommandTopic());
                }
                break;
            case PREF_STATE_TOPIC:
                value = stateTopicPreference.getText();
                if (!TextUtils.isEmpty(value)) {
                    configuration.setStateTopic(value);
                    stateTopicPreference.setSummary(value);
                }else if(isAdded()) {
                    Toast.makeText(getActivity(), R.string.text_error_blank_entry, Toast.LENGTH_LONG).show();
                    stateTopicPreference.setText(configuration.getStateTopic());
                }
                break;
            case PREF_USERNAME:
                value = userNamePreference.getText();
                configuration.setUserName(value);
                userNamePreference.setSummary(value);
                break;
            case PREF_PASSWORD:
                value = passwordPreference.getText();
                configuration.setPassword(value);
                passwordPreference.setSummary(toStars(value));
                break;
            case PREF_PENDING_TIME:
                value = pendingPreference.getText();
                if (value.matches("[0-9]+") && !TextUtils.isEmpty(value)) {
                    int pendingTime = Integer.parseInt(value);
                    if(pendingTime < 10) {
                        if(isAdded()) {
                            ((BaseActivity) getActivity()).showAlertDialog(getString(R.string.text_error_pending_time_low));
                        }
                    }
                    configuration.setPendingTime(pendingTime);
                    pendingPreference.setText(String.valueOf(pendingTime));
                    pendingPreference.setSummary(getString(R.string.preference_summary_pending_time, String.valueOf(pendingTime)));
                } else if(isAdded()) {
                    Toast.makeText(getActivity(), R.string.text_error_only_numbers, Toast.LENGTH_LONG).show();
                    pendingPreference.setText(String.valueOf(configuration.getPendingTime()));
                }
                break;
            case PREF_TLS_CONNECTION:
                boolean checked = sslPreference.isChecked();
                configuration.setTlsConnection(checked);
                break;
            case PREF_NOTIFICATIONS:
                boolean notify = notificationsPreference.isChecked();
                configuration.setNotifications(notify);
                break;
        }
    }

    private String toStars(String text) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            sb.append('*');
        }
        text = sb.toString();
        return text;
    }

    private void hideAlarmCodeDialog() {
        if(alarmCodeDialog != null) {
            alarmCodeDialog.dismiss();
            alarmCodeDialog = null;
        }
    }
    
    private void showAlarmCodeDialog() {
        // store the default alarm code
        defaultCode = configuration.getAlarmCode();
        hideAlarmCodeDialog();
        if(isAdded()) {
            alarmCodeDialog = DialogUtils.showCodeDialog(((BaseActivity) getActivity()), confirmCode, new AlarmCodeView.ViewListener() {
                @Override
                public void onComplete(int code) {
                    if (code == defaultCode) {
                        confirmCode = false;
                        hideAlarmCodeDialog();
                        Toast.makeText(getActivity(), R.string.toast_code_match, Toast.LENGTH_LONG).show();
                    } else if (!confirmCode) {
                        tempCode = code;
                        confirmCode = true;
                        hideAlarmCodeDialog();
                        showAlarmCodeDialog();
                    } else if (confirmCode && code == tempCode) {
                        configuration.setAlarmCode(tempCode);
                        configuration.setFirstTime(false);
                        tempCode = 0;
                        confirmCode = false;
                        hideAlarmCodeDialog();
                        Toast.makeText(getActivity(), R.string.toast_code_changed, Toast.LENGTH_LONG).show();
                    } else if (confirmCode) {
                        tempCode = 0;
                        confirmCode = false;
                        hideAlarmCodeDialog();
                        Toast.makeText(getActivity(), R.string.toast_code_not_match, Toast.LENGTH_LONG).show();
                    }
                }
                @Override
                public void onError() {
                }
                @Override
                public void onCancel() {
                    confirmCode = false;
                    hideAlarmCodeDialog();
                    Toast.makeText(getActivity(), R.string.toast_code_unchanged, Toast.LENGTH_SHORT).show();
                }
            }, new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialogInterface) {
                    confirmCode = false;
                    Toast.makeText(getActivity(), R.string.toast_code_unchanged, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}