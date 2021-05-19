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

package com.thanksmister.iot.mqtt.alarmpanel.ui.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.thanksmister.iot.mqtt.alarmpanel.R
import com.thanksmister.iot.mqtt.alarmpanel.persistence.Dashboard
import com.thanksmister.iot.mqtt.alarmpanel.persistence.Sensor
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils
import kotlinx.android.synthetic.main.adapter_dashboard.view.*
import kotlinx.android.synthetic.main.adapter_sensor_preference.view.*
import kotlinx.android.synthetic.main.adapter_sensor_preference.view.nameText

class DashboardAdapter(private val items: List<Dashboard>, private val listener: OnItemClickListener) : RecyclerView.Adapter<DashboardAdapter.ViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(dashboard: Dashboard)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.adapter_dashboard, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return if (items.isNotEmpty()) items.size else 0
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems(items[position], listener)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        @SuppressLint("SetTextI18n")
        fun bindItems(item: Dashboard, listener: OnItemClickListener) {
            itemView.urlText.text = item.url
            itemView.setOnClickListener {
                listener.onItemClick(item)
            }
        }
    }
}