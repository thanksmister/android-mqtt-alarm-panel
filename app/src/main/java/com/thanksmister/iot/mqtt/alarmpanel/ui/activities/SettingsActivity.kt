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

package com.thanksmister.iot.mqtt.alarmpanel.ui.activities

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import com.thanksmister.iot.mqtt.alarmpanel.BaseActivity
import com.thanksmister.iot.mqtt.alarmpanel.R
import com.thanksmister.iot.mqtt.alarmpanel.network.AlarmPanelService
import com.thanksmister.iot.mqtt.alarmpanel.network.MQTTOptions
import com.thanksmister.iot.mqtt.alarmpanel.persistence.Configuration
import com.thanksmister.iot.mqtt.alarmpanel.utils.DialogUtils
import com.thanksmister.iot.mqtt.alarmpanel.utils.ScreenUtils
import dagger.android.support.DaggerAppCompatActivity
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import javax.inject.Inject
import kotlin.system.exitProcess

class SettingsActivity : DaggerAppCompatActivity(){

    @Inject
    lateinit var configuration: Configuration
    @Inject
    lateinit var mqttOptions: MQTTOptions
    @Inject
    lateinit var dialogUtils: DialogUtils
    @Inject
    lateinit var screenUtils: ScreenUtils

    val disposable = CompositeDisposable()

    private val inactivityHandler: Handler = Handler()

    private val inactivityCallback = Runnable {
        if(configuration.useInactivityTimer) {
            Toast.makeText(this@SettingsActivity, getString(R.string.toast_screen_timeout), Toast.LENGTH_LONG).show()
            dialogUtils.clearDialogs()
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_settings)

        supportActionBar?.show()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setTitle(R.string.activity_settings_title)

        //val alarmPanelService = Intent(this, AlarmPanelService::class.java)
        //stopService(alarmPanelService)

        lifecycle.addObserver(dialogUtils)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            onBackPressed()
            return true
        } else if (id == R.id.action_help) {
            support()
        } else if (id == R.id.action_logs) {
            logs()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        /*if(configuration.nightModeChanged) {
            configuration.nightModeChanged = false
            restartApp()
        } else {
            super.onBackPressed()
        }*/
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_settings, menu)
        val itemLen = menu.size()
        for (i in 0 until itemLen) {
            val drawable = menu.getItem(i).icon
            if (drawable != null) {
                drawable.mutate()
                drawable.setColorFilter(resources.getColor(R.color.gray), PorterDuff.Mode.SRC_ATOP)
            }
        }
        return true
    }

    override fun onResume() {
        super.onResume()
        inactivityHandler.postDelayed(inactivityCallback, 180000)
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.dispose()
        inactivityHandler.removeCallbacks(inactivityCallback)
    }

    override fun onUserInteraction() {
        inactivityHandler.removeCallbacks(inactivityCallback)
        inactivityHandler.postDelayed(inactivityCallback, 180000)
    }

    private fun logs() {
        val intent = LogActivity.createStartIntent(this@SettingsActivity)
        startActivity(intent)
    }

    private fun support() {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(SUPPORT_URL)))
        } catch (ex: android.content.ActivityNotFoundException) {
            Timber.e(ex.message)
        }
    }

    private fun restartApp() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                or Intent.FLAG_ACTIVITY_CLEAR_TASK
                or Intent.FLAG_ACTIVITY_NEW_TASK)
        val pendingIntent = PendingIntent.getActivity(this.applicationContext, 0, intent, PendingIntent.FLAG_ONE_SHOT)
        val mgr = this.applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        mgr[AlarmManager.RTC, System.currentTimeMillis() + 100] = pendingIntent
        finish()
        exitProcess(2)
    }

    companion object {
        val SUPPORT_URL:String = "https://thanksmister.com/android-mqtt-alarm-panel/"
        fun createStartIntent(context: Context): Intent {
            return Intent(context, SettingsActivity::class.java)
        }
        const val PERMISSIONS_REQUEST_CAMERA = 201
    }
}