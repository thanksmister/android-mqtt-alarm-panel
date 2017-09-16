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

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;

import com.thanksmister.iot.mqtt.alarmpanel.BaseActivity;
import com.thanksmister.iot.mqtt.alarmpanel.R;
import com.thanksmister.iot.mqtt.alarmpanel.ui.fragments.HomeAssistantFragment;

public class HomeAssistantActivity extends BaseActivity {

    private static final String HASS_FRAGMENT = "com.thanksmister.fragment.HASS_FRAGMENT";
    
    public static Intent createStartIntent(Context context) {
        return new Intent(context, HomeAssistantActivity.class);
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_hass);

        if(savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.contentFrame, HomeAssistantFragment.newInstance(), HASS_FRAGMENT).commit();
        }
    }

    @Override public void onConfigurationChanged(Configuration newConfig){ super.onConfigurationChanged(newConfig);}
}