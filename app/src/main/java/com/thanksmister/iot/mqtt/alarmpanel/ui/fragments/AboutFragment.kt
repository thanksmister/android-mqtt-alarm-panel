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
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.thanksmister.iot.mqtt.alarmpanel.BaseActivity
import com.thanksmister.iot.mqtt.alarmpanel.BaseFragment
import com.thanksmister.iot.mqtt.alarmpanel.R
import com.thanksmister.iot.mqtt.alarmpanel.ui.activities.SettingsActivity
import com.thanksmister.iot.mqtt.alarmpanel.utils.DialogUtils
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_about.*
import timber.log.Timber
import javax.inject.Inject

class AboutFragment : BaseFragment() {

    @Inject lateinit var dialogUtils: DialogUtils
    private var versionNumber: String? = null


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if((activity as SettingsActivity).supportActionBar != null) {
            (activity as SettingsActivity).supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            (activity as SettingsActivity).supportActionBar!!.setDisplayShowHomeEnabled(true)
            (activity as SettingsActivity).supportActionBar!!.title = (getString(R.string.text_about))
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_about, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            val packageInfo = activity!!.packageManager.getPackageInfo(activity!!.packageName, 0)
           // val versionName = activity!!.findViewById<View>(R.id.versionName)
            versionNumber = " v" + packageInfo.versionName
            versionName.text = versionNumber
        } catch (e: PackageManager.NameNotFoundException) {
            Timber.e(e.message)
        }

        sendFeedbackButton.setOnClickListener { feedback() }
        rateApplicationButton.setOnClickListener { rate() }
        licenseButton.setOnClickListener { showLicense() }
        githubButton.setOnClickListener { showGitHub() }
        supportButton.setOnClickListener { showSupport() }
    }

    override fun onDetach() {
        super.onDetach()
    }

    private fun rate() {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + GOOGLE_PLAY_RATING)))
        } catch (ex: android.content.ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + GOOGLE_PLAY_RATING)))
        }
    }

    private fun showSupport() {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(SUPPORT_URL)))
    }

    private fun showGitHub() {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(GITHUB_URL)))
    }

    private fun feedback() {
        val Email = Intent(Intent.ACTION_SENDTO)
        Email.type = "text/email"
        Email.data = Uri.parse("mailto:" + EMAIL_ADDRESS)
        Email.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.mail_to_subject_text) + " " + versionNumber)
        startActivity(Intent.createChooser(Email, getString(R.string.mail_subject_text)))
    }

    private fun showLicense() {
        if (isAdded) {
            dialogUtils.showAlertDialog(activity as BaseActivity, getString(R.string.license))
        }
    }

    companion object {
        const val SUPPORT_URL:String = "https://thanksmister.com/android-mqtt-alarm-panel/"
        const val GOOGLE_PLAY_RATING = "com.thanksmister.iot.mqtt.alarmpanel"
        const val GITHUB_URL = "https://github.com/thanksmister/android-mqtt-alarm-panel"
        const val EMAIL_ADDRESS = "mister@thanksmister.com"

        fun newInstance(): AboutFragment {
            return AboutFragment()
        }
    }
}