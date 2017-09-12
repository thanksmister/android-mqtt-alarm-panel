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

package com.thanksmister.iot.mqtt.alarmpanel.ui.views;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.thanksmister.iot.mqtt.alarmpanel.R;
import com.thanksmister.iot.mqtt.alarmpanel.network.model.Daily;
import com.thanksmister.iot.mqtt.alarmpanel.ui.adapters.ForecastCardAdapter;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ExtendedForecastView extends LinearLayout {

    @Bind(R.id.recycleView)
    RecyclerView recycleView;

    public ExtendedForecastView(Context context) {
        super(context);
    }

    public ExtendedForecastView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
    }
    
    public void setExtendedForecast(@NonNull Daily daily, String units) {
        if(daily.getData() != null) {
            recycleView.setHasFixedSize(true);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
            linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            recycleView.setLayoutManager(linearLayoutManager);
            ForecastCardAdapter forecastCardAdapter = new ForecastCardAdapter(getContext(), daily.getData(), units);
            recycleView.setAdapter(forecastCardAdapter);
        }
    }
}