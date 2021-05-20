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
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.pm.PackageManager
import android.os.Build
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.*
import androidx.core.app.ActivityCompat
import com.thanksmister.iot.mqtt.alarmpanel.network.AlarmPanelService
import com.thanksmister.iot.mqtt.alarmpanel.network.ImageOptions
import com.thanksmister.iot.mqtt.alarmpanel.network.MQTTOptions
import com.thanksmister.iot.mqtt.alarmpanel.persistence.Configuration
import com.thanksmister.iot.mqtt.alarmpanel.persistence.WeatherDao
import com.thanksmister.iot.mqtt.alarmpanel.utils.DialogUtils
import com.thanksmister.iot.mqtt.alarmpanel.utils.ScreenUtils
import dagger.android.support.DaggerAppCompatActivity
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject


abstract class BaseActivity : DaggerAppCompatActivity() {

    @Inject
    lateinit var configuration: Configuration
    @Inject
    lateinit var mqttOptions: MQTTOptions
    @Inject
    lateinit var imageOptions: ImageOptions
    @Inject
    lateinit var dialogUtils: DialogUtils
    @Inject
    lateinit var weatherDao: WeatherDao
    @Inject
    lateinit var screenUtils: ScreenUtils

    var serviceStarted: Boolean = false
    val disposable = CompositeDisposable()
    val alarmPanelService: Intent by lazy {
        Intent(this, AlarmPanelService::class.java)
    }

    private var hasNetwork = AtomicBoolean(true)

    override fun onDestroy() {
        super.onDestroy()
        disposable.dispose()
    }

    override fun onStart() {
        super.onStart()
        /*if (configuration.useDarkTheme && configuration.nightModeChanged) {
            configuration.nightModeChanged = false
            setDarkTheme()
        } else if (configuration.nightModeChanged) {
            configuration.nightModeChanged = false
            setDayNightMode()
        }*/
    }

    public override fun onResume() {
        super.onResume()
        checkPermissions()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return item.itemId == android.R.id.home
    }

    // This is called from the MQTT command, we need to recreate the activity to take effect
    fun dayNightModeCheck(sunValue: String?) {
        val nightMode = AppCompatDelegate.getDefaultNightMode()
        sunValue?.let {
            if (it == Configuration.SUN_BELOW_HORIZON && (nightMode == AppCompatDelegate.MODE_NIGHT_NO || nightMode == AppCompatDelegate.MODE_NIGHT_UNSPECIFIED)) {
                configuration.dayNightMode = it
                setDayNightMode()
            } else if (it == Configuration.SUN_ABOVE_HORIZON && (nightMode == AppCompatDelegate.MODE_NIGHT_YES || nightMode == AppCompatDelegate.MODE_NIGHT_UNSPECIFIED)) {
                configuration.dayNightMode = it
                setLightTheme()
            }
        }
    }

    // When activity is recreated we can switch the day night mode
    fun setDayNightMode(force: Boolean = false) {
        val dayNightMode = configuration.dayNightMode
        if (dayNightMode == Configuration.DISPLAY_MODE_NIGHT) {
            setDarkTheme(force)
        } else if (dayNightMode == Configuration.DISPLAY_MODE_DAY) {
            setLightTheme(force)
        }
    }

    fun setDarkTheme(force: Boolean = false) {
        val nightMode = AppCompatDelegate.getDefaultNightMode()
        if(nightMode == MODE_NIGHT_NO || nightMode == MODE_NIGHT_UNSPECIFIED || force) {
            screenUtils.setScreenBrightness()
            setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
             window.setWindowAnimations(R.style.WindowAnimationFadeInOut)
            recreate()
        }
    }

    fun setLightTheme(force: Boolean = false) {
        val nightMode = AppCompatDelegate.getDefaultNightMode()
        if(nightMode == MODE_NIGHT_YES || nightMode == MODE_NIGHT_UNSPECIFIED || force) {
            screenUtils.setScreenBrightness()
            setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
             window.setWindowAnimations(R.style.WindowAnimationFadeInOut)
            recreate()
        }
    }

    /**
     * Show the screen saver only if the alarm isn't triggered. This shouldn't be an issue
     * with the alarm disabled because the disable time will be longer than this.
     */
    fun showScreenSaver() {
        if (configuration.canShowScreenSaver()) {
            val hasWeather = configuration.showWeatherModule()
            val isImperial = configuration.weatherUnitsImperial
            try {
                dialogUtils.showScreenSaver(this@BaseActivity,
                        configuration.showUnsplashScreenSaver(),
                        configuration.showClockScreenSaver(),
                        hasWeather,
                        isImperial,
                        configuration.webScreenSaver,
                        imageOptions,
                        weatherDao,
                        configuration.webScreenSaverUrl,
                        View.OnClickListener {
                            dialogUtils.hideScreenSaverDialog()
                            onUserInteraction()
                        })
                screenUtils.setScreenSaverBrightness(true)
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
        if (hasNetwork.get()) {
            handleNetworkConnect()
        }
        return hasNetwork.get()
    }

    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if ( ActivityCompat.checkSelfPermission(this@BaseActivity, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                try {
                    ActivityCompat.requestPermissions(this@BaseActivity, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_PERMISSIONS)
                } catch (e: RuntimeException) {
                    Timber.e("Permissions error: ${e.message}")
                }
                return
            }
        }
    }

    companion object {
        const val REQUEST_PERMISSIONS = 88
    }
}