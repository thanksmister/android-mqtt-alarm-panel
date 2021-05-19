/*
 * Copyright (c) 2018 ThanksMister LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.thanksmister.iot.mqtt.alarmpanel.ui.views

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.widget.RelativeLayout
import com.thanksmister.iot.mqtt.alarmpanel.persistence.Dashboard
import kotlinx.android.synthetic.main.dialog_edit_dashboard.view.*
import kotlinx.android.synthetic.main.fragment_dashboards.view.*

class EditDashboardDialogView : RelativeLayout {

    private var listener: ViewListener? = null
    internal var value: String = ""
    internal var dashboard: Dashboard = Dashboard()

    interface ViewListener {
        fun onUpdate(dashboard: Dashboard)
        fun onRemove(dashboard: Dashboard)
        fun onEmpty()
        fun onCancel()
    }

    fun setListener(listener: ViewListener) {
        this.listener = listener
    }

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    fun setValue(dashboard: Dashboard) {
        this.dashboard = dashboard
        editDashboardUrlText.setText(dashboard.url.orEmpty())
    }

    override fun onFinishInflate() {

        super.onFinishInflate()

        editDashboardUrlText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                value = s.toString()
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

        closeDashboardDialogButton.setOnClickListener {
            listener?.onCancel()
        }

        updateDashboardButton.setOnClickListener {
            when {
                value.isEmpty() -> {
                    listener?.onEmpty()
                }
                value != dashboard.url -> {
                    dashboard.url = value
                    listener?.onUpdate(dashboard)
                }
                else -> {
                    listener?.onCancel()
                }
            }
        }

        removeDashboardButton.setOnClickListener {
            listener?.onRemove(dashboard)
        }
    }
}