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

package com.thanksmister.iot.mqtt.alarmpanel.ui.fragments

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.preference.*
import com.thanksmister.iot.mqtt.alarmpanel.R
import com.thanksmister.iot.mqtt.alarmpanel.persistence.Configuration
import com.thanksmister.iot.mqtt.alarmpanel.ui.activities.DashboardsActivity
import com.thanksmister.iot.mqtt.alarmpanel.ui.activities.LiveCameraActivity
import com.thanksmister.iot.mqtt.alarmpanel.ui.activities.SensorsActivity
import com.thanksmister.iot.mqtt.alarmpanel.ui.activities.SettingsActivity
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class PlatformSettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

    @Inject lateinit var configuration: Configuration

    private var platformRefreshPreference: CheckBoxPreference? = null
    private var platformBarPreference: CheckBoxPreference? = null
    private var browserActivityPreference: SwitchPreference? = null
    private var browserHeaderPreference: EditTextPreference? = null

    private val manageDashboardsPreference: Preference by lazy {
        findPreference("button_dashboards") as Preference
    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if((activity as SettingsActivity).supportActionBar != null) {
            (activity as SettingsActivity).supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            (activity as SettingsActivity).supportActionBar!!.setDisplayShowHomeEnabled(true)
            (activity as SettingsActivity).supportActionBar!!.title = (getString(R.string.preference_title_web_settings))
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preferences_platform)
    }

    override fun onResume() {
        super.onResume()
        preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onDetach() {
        super.onDetach()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        platformBarPreference = findPreference("pref_platform_bar") as CheckBoxPreference
        platformRefreshPreference = findPreference("pref_platform_pull_refresh") as CheckBoxPreference
        browserHeaderPreference = findPreference(getString(R.string.key_setting_browser_user_agent)) as EditTextPreference
        browserActivityPreference = findPreference(getString(R.string.key_setting_app_showactivity)) as SwitchPreference

        /*if (!configuration.webUrl.isNullOrEmpty().not()) {
            webUrlPreference?.text = configuration.webUrl
            webUrlPreference?.summary = configuration.webUrl
        }*/

        platformBarPreference!!.isChecked = configuration.platformBar
        platformRefreshPreference!!.isChecked = configuration.platformRefresh

        manageDashboardsPreference.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            startDashboardsActivity(it.context)
            false
        }
    }

    private fun startDashboardsActivity(context: Context) {
        startActivity(Intent(context, DashboardsActivity::class.java))
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when (key) {
            /*"pref_module_web" -> {
                val checked = webModulePreference!!.isChecked
                configuration.setWebModule(checked)
                configuration.setHasPlatformChange(true)
            }*/
            "pref_platform_bar" -> {
                val checked = platformBarPreference!!.isChecked
                configuration.platformBar = checked
            }
            "pref_platform_pull_refresh" -> {
                val checked = platformRefreshPreference!!.isChecked
                configuration.platformRefresh = checked
            }
            getString(R.string.key_setting_browser_user_agent) -> {
                val value = browserHeaderPreference!!.text
                configuration.browserUserAgent = value
                browserHeaderPreference!!.setDefaultValue(value)
                browserHeaderPreference!!.summary = value
                configuration.setHasPlatformChange(true)
            }
            getString(R.string.key_setting_app_showactivity) -> {
                val checked = browserActivityPreference!!.isChecked
                configuration.appShowActivity = checked
                configuration.setHasPlatformChange(true)
            }
            /*"pref_web_url" -> {
                val value = webUrlPreference!!.text
                configuration.webUrl = value
                webUrlPreference?.text = value
                webUrlPreference?.summary = value
                configuration.setHasPlatformChange(true)
            }*/
        }
    }
}