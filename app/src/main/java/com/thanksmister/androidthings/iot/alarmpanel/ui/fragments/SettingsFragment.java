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

package com.thanksmister.androidthings.iot.alarmpanel.ui.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.thanksmister.androidthings.iot.alarmpanel.BaseActivity;
import com.thanksmister.androidthings.iot.alarmpanel.R;
import com.thanksmister.androidthings.iot.alarmpanel.ui.Configuration;
import com.thanksmister.androidthings.iot.alarmpanel.ui.views.AlarmCodeView;

import timber.log.Timber;

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    private EditTextPreference brokerPreference;
    private EditTextPreference clientPreference;
    private EditTextPreference portPreference;
    private EditTextPreference topicPreference;
    private EditTextPreference userNamePreference;
    private EditTextPreference passwordPreference;
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

        Preference buttonPreference = findPreference("pref_security_code");
        buttonPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                showAlarmCodeDialog();
                return true;
            }
        });

        brokerPreference = (EditTextPreference) findPreference("pref_broker");
        clientPreference = (EditTextPreference) findPreference("pref_client_id");
        portPreference = (EditTextPreference) findPreference("pref_port");
        topicPreference = (EditTextPreference) findPreference("pref_topic");
        userNamePreference = (EditTextPreference) findPreference("pref_username");
        passwordPreference = (EditTextPreference) findPreference("pref_password");
        
        if(isAdded()) {
            configuration = ((BaseActivity) getActivity()).getConfiguration();
        }
        
        brokerPreference.setText(configuration.getBroker());
        clientPreference.setText(String.valueOf(configuration.getClientId()));
        portPreference.setText(String.valueOf(configuration.getPort()));
        topicPreference.setText(configuration.getTopic());
        userNamePreference.setText(configuration.getUserName());
        passwordPreference.setText(configuration.getPassword());

        brokerPreference.setSummary(configuration.getBroker());
        clientPreference.setSummary(configuration.getClientId());
        portPreference.setSummary(String.valueOf(configuration.getPort()));
        topicPreference.setSummary(configuration.getTopic());
        userNamePreference.setSummary(configuration.getUserName());
        passwordPreference.setSummary(configuration.getPassword());
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        String value;
        switch (key) {
            case "pref_broker":
                value = brokerPreference.getText();
                configuration.setBroker(value);
                brokerPreference.setSummary(value);
                break;
            case "pref_client_id":
                value = portPreference.getText();
                configuration.setClientId(value);
                clientPreference.setSummary(value);
                break;
            case "pref_port":
                value = portPreference.getText();
                configuration.setPort(Integer.valueOf(value));
                portPreference.setSummary(String.valueOf(value));
                break;
            case "pref_topic":
                value = portPreference.getText();
                configuration.setTopic(value);
                topicPreference.setSummary(value);
                break;
            case "pref_username":
                value = portPreference.getText();
                configuration.setUserName(value);
                userNamePreference.setSummary(value);
                break;
            case "pref_password":
                value = portPreference.getText();
                configuration.setPassword(value);
                passwordPreference.setSummary(value);
                break;
        }
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
                Timber.d("Code: " + code);
                Timber.d("defaultCode: " + defaultCode);
                Timber.d("tempCode: " + tempCode);
                
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