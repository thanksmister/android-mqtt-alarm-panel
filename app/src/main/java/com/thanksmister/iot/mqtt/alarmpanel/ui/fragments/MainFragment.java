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
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.thanksmister.iot.mqtt.alarmpanel.BaseFragment;
import com.thanksmister.iot.mqtt.alarmpanel.R;
import com.thanksmister.iot.mqtt.alarmpanel.ui.activities.LogActivity;
import com.thanksmister.iot.mqtt.alarmpanel.ui.activities.SettingsActivity;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainFragment extends BaseFragment {
    
    @OnClick(R.id.buttonSettings)
    void buttonSettingsClicked() {
        if(!getConfiguration().isArmed()) {
            Intent intent = SettingsActivity.createStartIntent(getActivity());
            startActivity(intent);
        } else {
            listener.showAlarmDisableDialog(false);
        }
    }

    @OnClick(R.id.buttonLogs)
    void buttonLogsClicked() {
        Intent intent = LogActivity.createStartIntent(getActivity());
        startActivity(intent);
    }

    @OnClick(R.id.buttonSleep)
    void buttonSleep() {
        listener.showScreenSaver();
    }

    private OnMainFragmentListener listener;

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnMainFragmentListener {
        void showScreenSaver();
        void publishDisarmed();
        void showAlarmDisableDialog(boolean beep);
    }
    
    public MainFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     */
    public static MainFragment newInstance() {
        return new MainFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnMainFragmentListener) {
            listener = (OnMainFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnMainFragmentListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        ButterKnife.unbind(this);
    }
}