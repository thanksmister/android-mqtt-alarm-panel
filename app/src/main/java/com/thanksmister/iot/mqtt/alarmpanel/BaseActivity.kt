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

package com.thanksmister.iot.mqtt.alarmpanel

import android.Manifest
import android.app.Dialog
import android.arch.lifecycle.Observer
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.provider.Settings
import android.support.annotation.NonNull
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatDelegate
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import com.thanksmister.iot.mqtt.alarmpanel.managers.ConnectionLiveData
import com.thanksmister.iot.mqtt.alarmpanel.network.AlarmPanelService
import com.thanksmister.iot.mqtt.alarmpanel.network.ImageOptions
import com.thanksmister.iot.mqtt.alarmpanel.network.MQTTOptions
import com.thanksmister.iot.mqtt.alarmpanel.persistence.Configuration
import com.thanksmister.iot.mqtt.alarmpanel.persistence.WeatherDao
import com.thanksmister.iot.mqtt.alarmpanel.ui.activities.MainActivity
import com.thanksmister.iot.mqtt.alarmpanel.utils.DialogUtils
import com.thanksmister.iot.mqtt.alarmpanel.utils.ScreenUtils
import dagger.android.support.DaggerAppCompatActivity
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

abstract class BaseActivity : DaggerAppCompatActivity() {

    @Inject lateinit var configuration: Configuration
    @Inject lateinit var mqttOptions: MQTTOptions
    @Inject lateinit var imageOptions: ImageOptions
    @Inject lateinit var dialogUtils: DialogUtils
    @Inject lateinit var weatherDao: WeatherDao
    @Inject lateinit var screenUtils: ScreenUtils

    var serviceStarted: Boolean = false
    val disposable = CompositeDisposable()
    val alarmPanelService: Intent by lazy {
        Intent(this, AlarmPanelService::class.java)
    }

    private var hasNetwork = AtomicBoolean(true)
    private var connectionLiveData: ConnectionLiveData? = null

    override fun onStart(){
        super.onStart()
        connectionLiveData = ConnectionLiveData(this)
        connectionLiveData?.observe(this, Observer { connected ->
            if(connected!!) {
                handleNetworkConnect()
            } else {
                handleNetworkDisconnect()
            }
        })
    }

    override fun onRequestPermissionsResult(requestCode: Int, @NonNull permissions: Array<String>, @NonNull grantResults: IntArray) {
        when (requestCode) {
            REQUEST_PERMISSIONS -> {
                if (grantResults.isNotEmpty()) {
                    for (permission in grantResults) {
                        if (permission != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(this@BaseActivity, getString(R.string.dialog_no_camera_permissions), Toast.LENGTH_SHORT).show()
                            break
                        }
                    }
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.d("onDestroy")
        disposable.dispose()
    }

    public override fun onResume() {
        super.onResume()
        checkPermissions()
        if(configuration.nightModeChanged) {
            configuration.nightModeChanged = false // reset
            dayNightModeChanged() // reset screen brightness if day/night mode inactive
        } else {
            screenUtils.resetScreenBrightness(configuration.dayNightMode == Configuration.SUN_ABOVE_HORIZON)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return item.itemId == android.R.id.home
    }

    fun dayNightModeCheck(sunValue:String?) {
        Timber.d("dayNightModeCheck $sunValue")
        if(sunValue == Configuration.SUN_BELOW_HORIZON && screenUtils.tisTheDay() && serviceStarted) {
            stopService(alarmPanelService)
            serviceStarted = false
            hideScreenSaver()
            screenUtils.resetScreenBrightness(false)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            recreate()
        } else if (sunValue == Configuration.SUN_ABOVE_HORIZON && !screenUtils.tisTheDay() && serviceStarted) {
            stopService(alarmPanelService)
            serviceStarted = false
            hideScreenSaver()
            screenUtils.resetScreenBrightness(true)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            recreate()
        }
    }

    /**
     * Show the screen saver only if the alarm isn't triggered. This shouldn't be an issue
     * with the alarm disabled because the disable time will be longer than this.
     */
    fun showScreenSaver() {
        Timber.d("showScreenSaver ${configuration.hasScreenSaver()}")
        if (!configuration.isAlarmTriggeredMode() && configuration.hasScreenSaver()) {
            val hasWeather = configuration.showWeatherModule()
            val isImperial = configuration.weatherUnitsImperial
            try {
                dialogUtils.showScreenSaver(this@BaseActivity,
                        configuration.showPhotoScreenSaver(),
                        imageOptions,
                        View.OnClickListener {
                            dialogUtils.hideScreenSaverDialog()
                            onUserInteraction()
                        },  hasWeather, isImperial, weatherDao, configuration.webScreenSaver, configuration.webScreenSaverUrl)
            } catch (e: Throwable) {
                Timber.e(e.message)
            }
        }
    }

    fun hideScreenSaver() {
        Timber.d("hideScreenSaver")
        dialogUtils.hideScreenSaverDialog()
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    /**
     * On network disconnect show notification or alert message, clear the
     * screen saver and awake the screen. Override this method in activity
     * to for extra network disconnect handling such as bring application
     * into foreground.
     */
    open fun handleNetworkDisconnect() {
        hideScreenSaver()
        dialogUtils.showAlertDialogToDismiss(this@BaseActivity, getString(R.string.text_notification_network_title),
                getString(R.string.text_notification_network_description))
        hasNetwork.set(false)
    }

    /**
     * On network connect hide any alert dialogs generated by
     * the network disconnect and clear any notifications.
     */
    private fun handleNetworkConnect() {
        dialogUtils.hideAlertDialog()
        hasNetwork.set(true)
    }

    fun hasNetworkConnectivity(): Boolean {
        return hasNetwork.get()
    }

    private fun dayNightModeChanged() {
        Timber.d("dayNightModeChanged")
        if (!configuration.useNightDayMode && !screenUtils.tisTheDay() && serviceStarted) {
            stopService(alarmPanelService)
            hideScreenSaver()
            screenUtils.resetScreenBrightness(true)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            recreate()
        }
    }

    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this@BaseActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this@BaseActivity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this@BaseActivity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this@BaseActivity, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                 try {
                     ActivityCompat.requestPermissions(this@BaseActivity, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION,
                             Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_PERMISSIONS)

                 } catch (e: RuntimeException) {
                     Timber.e("Permissions error: ${e.message}")
                 }
                 return
            }
        }
    }

    /**
     * Attempts to bring the application to the foreground if needed.
     */
    private fun bringApplicationToForegroundIfNeeded() {
        if (!LifecycleHandler.isApplicationInForeground()) {
            Timber.d("bringApplicationToForegroundIfNeeded")
            val intent = Intent("intent.alarm.action")
            intent.component = ComponentName(this@BaseActivity.packageName, MainActivity::class.java.name)
            intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }
    }

    companion object {
        const val REQUEST_PERMISSIONS = 88
    }
}