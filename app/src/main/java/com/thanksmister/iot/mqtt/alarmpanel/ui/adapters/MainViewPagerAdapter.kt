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

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.thanksmister.iot.mqtt.alarmpanel.BaseFragment
import com.thanksmister.iot.mqtt.alarmpanel.persistence.Dashboard
import com.thanksmister.iot.mqtt.alarmpanel.ui.fragments.MainFragment
import com.thanksmister.iot.mqtt.alarmpanel.ui.fragments.PlatformFragment

class MainSlidePagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
    private var dashboards: List<Dashboard> = emptyList()

    fun addDashboards(dashboards: List<Dashboard>) {
        this.dashboards = dashboards
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return dashboards.size + 1
    }

    override fun createFragment(position: Int): Fragment {
        if (position == 0) {
            return MainFragment()
        } else if (dashboards.isNotEmpty()) {
            return PlatformFragment.newInstance(dashboards[position - 1], position - 1)
        } else {
            return PlatformFragment()
        }
    }
}