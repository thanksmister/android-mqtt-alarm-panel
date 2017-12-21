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
import android.util.AttributeSet
import android.widget.LinearLayout

import kotlinx.android.synthetic.main.dialog_alarm_options.view.*

class ArmOptionsView : LinearLayout {

    private var listener: ViewListener? = null

    private fun armAwayButtonClick() {
        if (listener != null) {
            listener!!.onArmAway()
        }
    }

    private fun armHomeButtonClick() {
        if (listener != null) {
            listener!!.onArmHome()
        }
    }

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    override fun onFinishInflate() {
        super.onFinishInflate()
        armAwayButton.setOnClickListener { armAwayButtonClick() }
        armStayButton.setOnClickListener { armHomeButtonClick() }
    }

    fun setListener(listener: ViewListener) {
        this.listener = listener
    }

    interface ViewListener {
        fun onArmHome()
        fun onArmAway()
    }
}