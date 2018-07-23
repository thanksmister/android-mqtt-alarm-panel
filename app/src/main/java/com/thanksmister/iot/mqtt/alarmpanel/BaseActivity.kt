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

package com.thanksmister.iot.mqtt.alarmpanel

import android.Manifest
import android.app.Dialog
import android.app.KeyguardManager
import android.arch.lifecycle.Observer
import android.content.*
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.PowerManager
import android.support.annotation.NonNull
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AppCompatDelegate
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import com.thanksmister.iot.mqtt.alarmpanel.managers.ConnectionLiveData
import com.thanksmister.iot.mqtt.alarmpanel.network.AlarmPanelService.Companion.BROADCAST_ACTION_CLEAR_BROWSER_CACHE
import com.thanksmister.iot.mqtt.alarmpanel.network.AlarmPanelService.Companion.BROADCAST_ACTION_LOAD_URL
import com.thanksmister.iot.mqtt.alarmpanel.network.AlarmPanelService.Companion.BROADCAST_ACTION_RELOAD_PAGE
import com.thanksmister.iot.mqtt.alarmpanel.network.AlarmPanelService.Companion.BROADCAST_ALERT_MESSAGE
import com.thanksmister.iot.mqtt.alarmpanel.network.AlarmPanelService.Companion.BROADCAST_TOAST_MESSAGE
import com.thanksmister.iot.mqtt.alarmpanel.network.DarkSkyOptions
import com.thanksmister.iot.mqtt.alarmpanel.network.ImageOptions
import com.thanksmister.iot.mqtt.alarmpanel.network.MQTTOptions
import com.thanksmister.iot.mqtt.alarmpanel.persistence.Configuration
import com.thanksmister.iot.mqtt.alarmpanel.persistence.DarkSkyDao
import com.thanksmister.iot.mqtt.alarmpanel.ui.activities.MainActivity
import com.thanksmister.iot.mqtt.alarmpanel.utils.DialogUtils
import com.thanksmister.iot.mqtt.alarmpanel.utils.NotificationUtils
import dagger.android.support.DaggerAppCompatActivity
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

abstract class BaseActivity : DaggerAppCompatActivity() {

    private val REQUEST_PERMISSIONS = 88

    @Inject lateinit var configuration: Configuration
    @Inject lateinit var mqttOptions: MQTTOptions
    @Inject lateinit var imageOptions: ImageOptions
    @Inject lateinit var darkSkyOptions: DarkSkyOptions
    @Inject lateinit var dialogUtils: DialogUtils
    @Inject lateinit var darkSkyDataSource: DarkSkyDao

    private val inactivityHandler: Handler = Handler()
    private var hasNetwork = AtomicBoolean(true)
    private var wakeLock: PowerManager.WakeLock? = null
    private var decorView: View? = null
    private var userPresent: Boolean = false
    private var connectionLiveData: ConnectionLiveData? = null

    val disposable = CompositeDisposable()
    private var screenSaverDialog : Dialog? = null

    abstract fun getLayoutId(): Int

    private val inactivityCallback = Runnable {
        Timber.d("inactivityCallback")
        dialogUtils.clearDialogs()
        userPresent = false
        showScreenSaver()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getLayoutId())

        this.window.setFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)

        decorView = window.decorView
    }

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

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        val visibility: Int
        if (hasFocus && configuration.fullScreen) {
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT -> visibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN -> visibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_LOW_PROFILE
                        or View.SYSTEM_UI_FLAG_FULLSCREEN)
                else -> {
                    visibility = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
                    window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                            WindowManager.LayoutParams.FLAG_FULLSCREEN)
                }
            }
            decorView?.systemUiVisibility = visibility
        } else if (hasFocus) {
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT -> visibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_VISIBLE)
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN -> visibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_VISIBLE)
                else -> visibility = (View.SYSTEM_UI_FLAG_VISIBLE)
            }
            decorView?.systemUiVisibility = visibility
        }
    }

    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this@BaseActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this@BaseActivity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this@BaseActivity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this@BaseActivity, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_PERMISSIONS)
                return
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, @NonNull permissions: Array<String>, @NonNull grantResults: IntArray) {
        when (requestCode) {
            REQUEST_PERMISSIONS -> {
                if (grantResults.isNotEmpty()) {
                    var permissionsDenied = false
                    for (permission in grantResults) {
                        if (permission != PackageManager.PERMISSION_GRANTED) {
                            permissionsDenied = true
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
        inactivityHandler.removeCallbacks(inactivityCallback)
        disposable.dispose()
    }

    fun resetInactivityTimer() {
        Timber.d("resetInactivityTimer")
        hideScreenSaver()
        inactivityHandler.removeCallbacks(inactivityCallback)
        inactivityHandler.postDelayed(inactivityCallback, configuration.inactivityTime)
    }

    fun stopDisconnectTimer() {
        hideScreenSaver()
        inactivityHandler.removeCallbacks(inactivityCallback)
    }

    override fun onUserInteraction() {
        Timber.d("onUserInteraction")
        onWindowFocusChanged(true)
        userPresent = true
        resetInactivityTimer()
    }

    public override fun onStop() {
        super.onStop()
    }

    public override fun onResume() {
        super.onResume()
        //releaseTemporaryWakeLock()
        checkPermissions()
        if(configuration.nightModeChanged) {
            configuration.nightModeChanged = false // reset
            dayNightModeChanged() // reset screen brightness if day/night mode inactive
        }
    }

    /**
     * Wakes the device temporarily (or always if triggered) when the alarm requires attention.
     */
    /*fun acquireTemporaryWakeLock(timeout: Long) {
        Timber.d("acquireTemporaryWakeLock")
        if (wakeLock == null) {
            val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP, "alarm:ALARM_TEMPORARY_WAKE_TAG")
        }
        if (wakeLock != null && !wakeLock!!.isHeld) {  // but we don't hold it
            wakeLock!!.acquire(timeout)
        }

        // Some Amazon devices are not seeing this permission so we are trying to check
        val permission = "android.permission.DISABLE_KEYGUARD"
        val checkSelfPermission = ContextCompat.checkSelfPermission(this@BaseActivity, permission)
        if (checkSelfPermission == PackageManager.PERMISSION_GRANTED) {
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            val keyguardLock = keyguardManager.newKeyguardLock("ALARM_KEYBOARD_LOCK_TAG")
            keyguardLock.disableKeyguard()
        }
    }*/

    /**
     * Wakelock used to temporarily bring application to foreground if alarm needs attention.
     */
    /*fun releaseTemporaryWakeLock() {
        if (wakeLock != null && wakeLock!!.isHeld) {
            wakeLock!!.release()
        }
    }*/

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return item.itemId == android.R.id.home
    }

    open fun dayNightModeCheck(dayNightMode:String?) {
        Timber.d("dayNightModeCheck")
        val uiMode = resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK;
        if(dayNightMode == Configuration.DISPLAY_MODE_NIGHT && uiMode == android.content.res.Configuration.UI_MODE_NIGHT_NO) {
            Timber.d("Tis the night!")
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            recreate()
        } else if (dayNightMode == Configuration.DISPLAY_MODE_DAY && uiMode == android.content.res.Configuration.UI_MODE_NIGHT_YES) {
            Timber.d("Tis the day!")
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            recreate()
        }
    }

    private fun dayNightModeChanged() {
        Timber.d("dayNightModeChanged")
        val uiMode = resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK;
        if (!configuration.useNightDayMode && uiMode == android.content.res.Configuration.UI_MODE_NIGHT_YES) {
            Timber.d("Tis the day!")
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            recreate()
        }
    }

    /**
     * Show the screen saver only if the alarm isn't triggered. This shouldn't be an issue
     * with the alarm disabled because the disable time will be longer than this.
     */
    open fun showScreenSaver() {
        if (!configuration.isAlarmTriggeredMode() && configuration.hasScreenSaver()) {
            inactivityHandler.removeCallbacks(inactivityCallback)
            val hasWeather = (configuration.showWeatherModule() && darkSkyOptions.isValid)
            dialogUtils.showScreenSaver(this@BaseActivity,
                    configuration.showPhotoScreenSaver(),
                    imageOptions,
                    View.OnClickListener {
                        dialogUtils.hideScreenSaverDialog()
                        resetInactivityTimer()
                    }, darkSkyDataSource, hasWeather)
        }
    }

    open fun hideScreenSaver() {
        dialogUtils.hideScreenSaverDialog()
        screenSaverDialog = null
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
        bringApplicationToForegroundIfNeeded()
        dialogUtils.showAlertDialogToDismiss(this@BaseActivity, getString(R.string.text_notification_network_title),
                getString(R.string.text_notification_network_description))
        hasNetwork.set(false)
    }

    /**
     * On network connect hide any alert dialogs generated by
     * the network disconnect and clear any notifications.
     */
    open fun handleNetworkConnect() {
        dialogUtils.hideAlertDialog()
        hasNetwork.set(true)
    }

    fun hasNetworkConnectivity(): Boolean {
        return hasNetwork.get()
    }

    fun bringApplicationToForegroundIfNeeded() {
        if (!LifecycleHandler.isApplicationInForeground()) {
            Timber.d("bringApplicationToForegroundIfNeeded")
            val intent = Intent("intent.alarm.action")
            intent.component = ComponentName(this@BaseActivity.packageName, MainActivity::class.java.name)
            intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }
    }

    companion object {

    }
}