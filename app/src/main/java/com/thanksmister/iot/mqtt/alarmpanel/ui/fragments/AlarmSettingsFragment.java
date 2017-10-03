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

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.thanksmister.iot.mqtt.alarmpanel.BaseActivity;
import com.thanksmister.iot.mqtt.alarmpanel.R;
import com.thanksmister.iot.mqtt.alarmpanel.ui.Configuration;
import com.thanksmister.iot.mqtt.alarmpanel.ui.views.AlarmCodeView;

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
import static com.thanksmister.iot.mqtt.alarmpanel.ui.Configuration.PREF_TRIGGER_TIME;
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
    private EditTextPreference triggerPreference;
    private CheckBoxPreference sslPreference;
    private CheckBoxPreference notificationsPreference;
    private Configuration configuration;
    private AlertDialog alarmCodeDialog;
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
        triggerPreference = (EditTextPreference) findPreference(PREF_TRIGGER_TIME);
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
        triggerPreference.setSummary(getString(R.string.preference_summary_trigger_time, String.valueOf(configuration.getTriggerTime())));
        
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
                configuration.setBroker(value);
                brokerPreference.setSummary(value);
                break;
            case PREF_CLIENT_ID:
                value = clientPreference.getText();
                configuration.setClientId(value);
                clientPreference.setSummary(value);
                break;
            case PREF_PORT:
                value = portPreference.getText();
                configuration.setPort(Integer.valueOf(value));
                portPreference.setSummary(String.valueOf(value));
                break;
            case PREF_COMMAND_TOPIC:
                value = commandTopicPreference.getText();
                configuration.setCommandTopic(value);
                commandTopicPreference.setSummary(value);
                break;
            case PREF_STATE_TOPIC:
                value = stateTopicPreference.getText();
                configuration.setStateTopic(value);
                stateTopicPreference.setSummary(value);
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
                configuration.setPendingTime(Integer.parseInt(value));
                pendingPreference.setSummary(getString(R.string.preference_summary_pending_time, String.valueOf(configuration.getPendingTime())));
                break;
            case PREF_TRIGGER_TIME:
                value = triggerPreference.getText();
                configuration.setTriggerTime(Integer.parseInt(value));
                triggerPreference.setSummary(getString(R.string.preference_summary_trigger_time, String.valueOf(configuration.getTriggerTime())));
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

    public void hideAlarmCodeDialog() {
        if(alarmCodeDialog != null) {
            alarmCodeDialog.dismiss();
            alarmCodeDialog = null;
        }
    }
    
    public void showAlarmCodeDialog() {
        
        // store the default alarm code
        defaultCode = configuration.getAlarmCode();
        
        hideAlarmCodeDialog();
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_alarm_code_set, null, false);
        final AlarmCodeView alarmCodeView = view.findViewById(R.id.alarmCodeView);
       
        final TextView titleTextView = alarmCodeView.findViewById(R.id.codeTitle);
        if(confirmCode){
            titleTextView.setText(R.string.text_renter_alarm_code_title);
        }

        alarmCodeView.setListener(new AlarmCodeView.ViewListener() {
            @Override
            public void onComplete(int code) {
                if(code == defaultCode) {
                    confirmCode = false;
                    hideAlarmCodeDialog();
                    Toast.makeText(getActivity(), R.string.toast_code_match, Toast.LENGTH_LONG).show();
                } else if (!confirmCode){
                    tempCode = code;
                    confirmCode = true;
                    hideAlarmCodeDialog();
                    showAlarmCodeDialog();
                } else if (confirmCode && code == tempCode){
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
                // handle error
            }

            @Override
            public void onCancel() {
                confirmCode = false; 
                hideAlarmCodeDialog();
                Toast.makeText(getActivity(), R.string.toast_code_unchanged, Toast.LENGTH_SHORT).show();
            }
        });

        alarmCodeDialog = new AlertDialog.Builder(getActivity())
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        confirmCode = false;
                        Toast.makeText(getActivity(), R.string.toast_code_unchanged, Toast.LENGTH_SHORT).show();
                    }
                })
                .setCancelable(true)
                .setView(view)
                .show();
    }
}