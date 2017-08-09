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

package com.thanksmister.androidthings.iot.alarmpanel.ui.views;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.thanksmister.androidthings.iot.alarmpanel.R;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class ArmOptionsView extends LinearLayout {

    @OnClick(R.id.armAwayButton)
    public void armAwayButtonClick() {
        listener.onArmAway();
    }

    @OnClick(R.id.armStayButton)
    public void armHomeButtonClick() {
        listener.onArmHome();
    }

    private ViewListener listener;

    public ArmOptionsView(Context context) {
        super(context);
    }

    public ArmOptionsView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
    }
    
    public void setListener(@NonNull ViewListener listener) {
        this.listener = listener;
    }

    public interface ViewListener {
        void onArmHome();
        void onArmAway();
    }
}