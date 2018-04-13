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

package com.thanksmister.iot.mqtt.alarmpanel.ui.views

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.util.AttributeSet
import android.widget.FrameLayout

import com.thanksmister.iot.mqtt.alarmpanel.network.model.Daily
import com.thanksmister.iot.mqtt.alarmpanel.network.model.Datum
import com.thanksmister.iot.mqtt.alarmpanel.ui.adapters.ForecastCardAdapter

import kotlinx.android.synthetic.main.dialog_extended_forecast.view.*

class ExtendedForecastView : FrameLayout {

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    override fun onFinishInflate() {
        super.onFinishInflate()
    }

    fun setExtendedForecast(data: List<Datum>) {
        if (data != null) {
            recycleView.setHasFixedSize(true)
            val linearLayoutManager = LinearLayoutManager(context)
            linearLayoutManager.orientation = LinearLayoutManager.HORIZONTAL
            recycleView.layoutManager = linearLayoutManager
            val forecastCardAdapter = ForecastCardAdapter(data)
            recycleView.adapter = forecastCardAdapter
        }
    }
}