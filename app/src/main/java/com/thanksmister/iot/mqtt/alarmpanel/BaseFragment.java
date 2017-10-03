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

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.thanksmister.iot.mqtt.alarmpanel.data.stores.StoreManager;
import com.thanksmister.iot.mqtt.alarmpanel.network.model.Daily;
import com.thanksmister.iot.mqtt.alarmpanel.ui.Configuration;
import com.thanksmister.iot.mqtt.alarmpanel.ui.views.ArmOptionsView;

import butterknife.ButterKnife;

public class BaseFragment extends Fragment {
    
    public BaseFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_controls, container, false);
    }
    
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public StoreManager getStoreManager() {
        return ((BaseActivity) getActivity()).getStoreManager();
    }

    public Configuration getConfiguration() {
        return ((BaseActivity) getActivity()).getConfiguration();
    }
    
    public void showArmOptionsDialog(ArmOptionsView.ViewListener armListener) {
        ((BaseActivity) getActivity()).showArmOptionsDialog(armListener);
    }
    
    public void showExtendedForecastDialog(Daily daily) {
        ((BaseActivity) getActivity()).showExtendedForecastDialog(daily);
    }
    
    public void hideDialog() {
        ((BaseActivity) getActivity()).hideDialog();
    }
}
