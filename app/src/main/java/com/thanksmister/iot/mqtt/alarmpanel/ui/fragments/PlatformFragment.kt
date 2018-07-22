/*
 * Copyright (c) 2017. ThanksMister LLC
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
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.thanksmister.iot.mqtt.alarmpanel.BaseFragment
import com.thanksmister.iot.mqtt.alarmpanel.R
import com.thanksmister.iot.mqtt.alarmpanel.persistence.Configuration
import com.thanksmister.iot.mqtt.alarmpanel.utils.DialogUtils
import kotlinx.android.synthetic.main.fragment_platform.*
import javax.inject.Inject
import android.widget.CheckBox
import com.baviux.homeassistant.HassWebView


class PlatformFragment : BaseFragment(){

    @Inject lateinit var configuration: Configuration
    @Inject lateinit var dialogUtils: DialogUtils

    private var listener: OnPlatformFragmentListener? = null

    interface OnPlatformFragmentListener {
        fun navigateAlarmPanel()
        fun setPagingEnabled(value: Boolean)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is OnPlatformFragmentListener) {
            listener = context
        } else {
            throw RuntimeException(context!!.toString() + " must implement OnPlatformFragmentListener")
        }
    }

    override fun onResume() {
        super.onResume()
        loadWebPage()
        if (configuration.platformBar) {
            settingsContainer.visibility = View.VISIBLE;
            checkbox_hide.isChecked = false
        } else {
            settingsContainer.visibility = View.GONE;
            checkbox_hide.isChecked = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(dialogUtils)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        button_alarm.setOnClickListener({
            if(listener != null) {
                webView.closeMoreInfoDialog()
                listener!!.navigateAlarmPanel()
            }
        })
        button_refresh.setOnClickListener({
            loadWebPage()
        })

        checkbox_hide.setOnClickListener { v ->
            if ((v as CheckBox).isChecked) {
                settingsContainer.visibility = View.GONE;
            } else {
                settingsContainer.visibility = View.VISIBLE;
            }
            configuration.platformBar = !v.isChecked
        }
    }

    private fun loadWebPage() {
        if (configuration.hasPlatformModule() && !TextUtils.isEmpty(configuration.webUrl) && webView != null) {
            webView.loadUrl(configuration.webUrl)
            webView.setAdjustBackKeyBehavior(configuration.adjustBackBehavior)
            webView.setHideAdminMenuItems(configuration.hideAdminMenu)
            webView.setOnFinishEventHandler { button_alarm.callOnClick() }
            webView.setMoreInfoDialogHandler(object : HassWebView.IMoreInfoDialogHandler{
                override fun onShowMoreInfoDialog() {
                    listener!!.setPagingEnabled(false)
                }
                override fun onHideMoreInfoDialog() {
                    listener!!.setPagingEnabled(true)
                }
            })
        } else if (webView != null) {
            webView.loadUrl("about:blank")
        }
    }

    override fun onBackPressed() : Boolean{
        if (webView == null) {
            return super.onBackPressed()
        }

        val handled = webView.onBackPressed()

        // If HassWebView doesn't handle it -> ensure no hass dialog is shown and paging is restored
        if (!handled){
            webView.closeMoreInfoDialog()
        }

        return handled;
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_platform, container, false)
    }

    override fun onDetach() {
        super.onDetach()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         */
        fun newInstance(): PlatformFragment {
            return PlatformFragment()
        }
    }
}// Required empty public constructor