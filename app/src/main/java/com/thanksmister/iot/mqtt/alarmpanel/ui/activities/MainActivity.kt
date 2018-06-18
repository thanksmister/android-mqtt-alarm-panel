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

package com.thanksmister.iot.mqtt.alarmpanel.ui.activities

import android.Manifest
import android.arch.lifecycle.Observer
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.support.annotation.LayoutRes
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatDelegate
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.thanksmister.iot.mqtt.alarmpanel.BaseActivity
import com.thanksmister.iot.mqtt.alarmpanel.BaseFragment
import com.thanksmister.iot.mqtt.alarmpanel.BuildConfig
import com.thanksmister.iot.mqtt.alarmpanel.R
import com.thanksmister.iot.mqtt.alarmpanel.managers.DayNightAlarmLiveData
import com.thanksmister.iot.mqtt.alarmpanel.network.MQTTOptions
import com.thanksmister.iot.mqtt.alarmpanel.ui.fragments.ControlsFragment
import com.thanksmister.iot.mqtt.alarmpanel.ui.fragments.MainFragment
import com.thanksmister.iot.mqtt.alarmpanel.ui.fragments.PlatformFragment
import com.thanksmister.iot.mqtt.alarmpanel.ui.modules.CameraModule
import com.thanksmister.iot.mqtt.alarmpanel.ui.modules.MQTTModule
import com.thanksmister.iot.mqtt.alarmpanel.ui.modules.TextToSpeechModule
import com.thanksmister.iot.mqtt.alarmpanel.ui.views.ScreenSaverView
import com.thanksmister.iot.mqtt.alarmpanel.utils.AlarmUtils
import com.thanksmister.iot.mqtt.alarmpanel.utils.ComponentUtils.Companion.NOTIFICATION_STATE_TOPIC
import com.thanksmister.iot.mqtt.alarmpanel.utils.NotificationUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber
import javax.inject.Inject


class MainActivity : BaseActivity(), ViewPager.OnPageChangeListener, ControlsFragment.OnControlsFragmentListener,
        MQTTModule.MQTTListener, CameraModule.CallbackListener, MainFragment.OnMainFragmentListener, PlatformFragment.OnPlatformFragmentListener {

    private lateinit var pagerAdapter: PagerAdapter

    private var textToSpeechModule: TextToSpeechModule? = null
    private var mqttModule: MQTTModule? = null
    private var mBackgroundThread: HandlerThread? = null
    private var mBackgroundHandler: Handler? = null
    private var cameraModule: CameraModule? = null
    private var alertDialog: AlertDialog? = null
    private var releaseWakeHandler:Handler? = null
    private var alarmLiveData: DayNightAlarmLiveData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBackgroundThread = HandlerThread("BackgroundThread")
        mBackgroundThread?.start()
        mBackgroundHandler = Handler(mBackgroundThread?.looper)

        pagerAdapter = MainSlidePagerAdapter(supportFragmentManager)
        view_pager.adapter = pagerAdapter
        view_pager.addOnPageChangeListener(this)
        view_pager.setPagingEnabled(false)

        if(BuildConfig.DEBUG) {
            configuration.alarmCode = BuildConfig.ALARM_CODE
            darkSkyOptions.darkSkyKey = BuildConfig.DARK_SKY_KEY
            darkSkyOptions.latitude = BuildConfig.LATITUDE
            darkSkyOptions.longitude = BuildConfig.LONGITUDE
            mqttOptions.setBroker(BuildConfig.BROKER)
            configuration.webUrl = BuildConfig.HASS_URL
            configuration.setMailFrom(BuildConfig.MAIL_FROM)
            configuration.setMailGunApiKey(BuildConfig.MAIL_GUN_KEY)
            configuration.setMailTo(BuildConfig.MAIL_TO)
            configuration.setMailGunUrl(BuildConfig.MAIL_GUN_URL)
            configuration.telegramChatId = BuildConfig.TELEGRAM_CHAT_ID
            configuration.telegramToken = BuildConfig.TELEGRAM_TOKEN
            imageOptions.imageClientId = BuildConfig.IMGUR_CLIENT_ID
            imageOptions.imageSource = BuildConfig.IMGUR_TAG // Imgur tags
            darkSkyOptions.setIsCelsius(true)
            configuration.isFirstTime = false
            configuration.setHasNotifications(true)
            configuration.setClockScreenSaverModule(true)
            configuration.setPhotoScreenSaver(false)
            configuration.setHasCamera(true)
            configuration.setWebModule(true)
            configuration.setShowWeatherModule(true)
            configuration.setTssModule(true)
        }

        if (configuration.isFirstTime) {
            alertDialog = AlertDialog.Builder(this@MainActivity, R.style.CustomAlertDialog)
                    .setMessage(getString(R.string.dialog_first_time))
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        val intent = SettingsActivity.createStartIntent(this@MainActivity)
                        startActivity(intent)
                    }
                    .show()
        }

        alarmLiveData = DayNightAlarmLiveData(this@MainActivity, configuration)
        alarmLiveData?.observe(this, Observer { dayNightMode ->
            dayNightModeCheck(dayNightMode)
        })

        Timber.d("imageOptions clientID ${imageOptions.imageClientId}")
        Timber.d("mqttOptions broker ${mqttOptions.getBroker()}")
    }

    public override fun onStart() {

        super.onStart()

        lifecycle.addObserver(dialogUtils)

        disposable.add(viewModel.getAlarmState()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({state ->
                    this@MainActivity.runOnUiThread {
                        Timber.d("onStart state: " + state)
                        when (state) {
                            AlarmUtils.STATE_DISARM -> {
                                awakenDeviceForAction(AWAKE_TIME)
                                resetInactivityTimer()
                                if(viewModel.hasSystemAlerts()) {
                                    val notifications = NotificationUtils(this@MainActivity)
                                    notifications.clearNotification()
                                }
                            }
                            AlarmUtils.STATE_ARM_AWAY,
                            AlarmUtils.STATE_ARM_HOME -> {
                                awakenDeviceForAction(AWAKE_TIME)
                                resetInactivityTimer()
                            }
                            AlarmUtils.STATE_TRIGGERED -> {
                                awakenDeviceForAction(TRIGGERED_AWAKE_TIME) // 3 hours
                                if(viewModel.showSystemTriggeredAlert()){
                                    val notifications = NotificationUtils(this@MainActivity)
                                    notifications.createAlarmNotification(getString(R.string.text_notification_trigger_title), getString(R.string.text_notification_trigger_description))
                                }
                            }
                            AlarmUtils.STATE_PENDING -> {
                                awakenDeviceForAction(AWAKE_TIME)
                                if(viewModel.showSystemPendingAlert()){
                                    val notifications = NotificationUtils(this@MainActivity)
                                    notifications.createAlarmNotification(getString(R.string.text_notification_entry_title), getString(R.string.text_notification_entry_description))
                                }
                            }
                        }
                    }
                }, { error -> Timber.e("Unable to get message: " + error)}))

        viewModel.getAlertMessage().observe(this, Observer { message ->
            Timber.d("getAlertMessage")
            dialogUtils.showAlertDialog(this@MainActivity, message!!)

        })
        viewModel.getToastMessage().observe(this, Observer { message ->
            Timber.d("getToastMessage")
            Toast.makeText(this@MainActivity, message, Toast.LENGTH_LONG).show()
        })
    }

    override fun onResume() {
        Timber.d("onResume")
        super.onResume()
        resetInactivityTimer()
        mBackgroundHandler!!.post(initializeOnBackground)
        setViewPagerState()
    }

    override fun onDestroy() {
        Timber.d("onDestroy")
        super.onDestroy()
        try {
            if (mBackgroundThread != null) mBackgroundThread!!.quit()
        } catch (t: Throwable) {
            // close quietly
        }
        mBackgroundThread = null
        mBackgroundHandler = null
        if(alertDialog != null) {
            alertDialog?.dismiss()
            alertDialog = null
        }

        releaseWake();
    }

    override fun onBackPressed() {
        if (view_pager.currentItem == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed()
        } else if ((pagerAdapter as MainSlidePagerAdapter).getCurrentFragment()!!.onBackPressed()){
            // backpress handled by fragment do nothing
        } else {
            // Otherwise, if back key press is not handled by fragment, select the previous step.
            view_pager.currentItem = view_pager.currentItem - 1
        }
    }

    private fun releaseWake() {
        if(releaseWakeHandler != null) {
            releaseWakeHandler?.removeCallbacks(releaseWakeLockRunnable)
            releaseWakeHandler = null
        }
    }

    override fun navigateAlarmPanel() {
        view_pager.currentItem = 0
    }

    override fun setPagingEnabled(value: Boolean) {
        view_pager.setPagingEnabled(value)
    }

    override fun navigatePlatformPanel() {
        view_pager.currentItem = 1
    }

    private fun setViewPagerState() {
        if (viewModel.hasPlatform()) {
            view_pager.setPagingEnabled(true)
        } else {
            view_pager.setPagingEnabled(false)
        }
    }

    override fun publishArmedHome() {
        Timber.d("publishArmedHome")
        if (mqttModule != null) {
            mqttModule?.publish(AlarmUtils.COMMAND_ARM_HOME)
        }
    }

    override fun publishArmedAway() {
        Timber.d("publishArmedAway")
        if (mqttModule != null) {
            mqttModule?.publish(AlarmUtils.COMMAND_ARM_AWAY)
        }
    }

    override fun publishDisarmed() {
        Timber.d("publishDisarmed")
        if (mqttModule != null) {
            mqttModule?.publish(AlarmUtils.COMMAND_DISARM)
        }
        Handler().postDelayed({ captureImage() }, 300)
    }

    private val initializeOnBackground = Runnable {
        Timber.d("initializeOnBackground")
        if (textToSpeechModule == null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && viewModel.hasTss()) {
            Timber.d("textToSpeechModule")
            textToSpeechModule = TextToSpeechModule(this@MainActivity, configuration)
            runOnUiThread {
                lifecycle.addObserver(textToSpeechModule!!)
            }
        }
        if (mqttModule == null && mqttOptions.isValid) {
            Timber.d("mqttModule")
            mqttModule = MQTTModule(this@MainActivity.applicationContext, mqttOptions,this@MainActivity)
            runOnUiThread {
                lifecycle.addObserver(mqttModule!!)
            }
        }
        if (cameraModule == null && viewModel.hasCamera() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (ActivityCompat.checkSelfPermission(this@MainActivity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                Timber.d("cameraModule")
                cameraModule = CameraModule(this@MainActivity, mBackgroundHandler!!, this@MainActivity)
                runOnUiThread{
                    lifecycle.addObserver(cameraModule!!)
                }
            }
        }
    }

    @LayoutRes
    override fun getLayoutId(): Int {
        return R.layout.activity_main
    }

    override fun handleNetworkConnect() {
        if (mqttModule != null && !hasNetworkConnectivity()) {
            mqttModule?.restart()
        }
        super.handleNetworkConnect()
    }

    override fun handleNetworkDisconnect() {
        if (mqttModule != null && hasNetworkConnectivity()) {
            mqttModule?.pause()
        }
        super.handleNetworkDisconnect()
    }

    /**
     * Temporarily wake the screen so we can notify the user of pending alarm and
     * then allow the device to sleep again as needed after a set amount of time.
     */
    private var releaseWakeLockRunnable: Runnable = Runnable {
        releaseTemporaryWakeLock()
    }

    /**
     * We need to awaken the device and allow the user to take action.
     */
    private fun awakenDeviceForAction(timeout: Long) {
        Timber.d("awakenDeviceForAction")
        acquireTemporaryWakeLock(timeout)
        stopDisconnectTimer() // stop screen saver mode
        if (view_pager != null && pagerAdapter.count > 0) {
            view_pager.currentItem = 0
        }
        bringApplicationToForegroundIfNeeded()
        hideScreenSaver()
    }

    private fun captureImage() {
        if (cameraModule != null) {
            cameraModule?.takePicture(configuration.getCameraRotate()!!)
        }
    }

    override fun onMQTTMessage(id: String, topic: String, payload: String) {
        if(mqttOptions.getNotificationTopic() == topic) {
            this@MainActivity.runOnUiThread {
                if(viewModel.hasSystemAlerts()) {
                    val notifications = NotificationUtils(this@MainActivity)
                    notifications.createAlarmNotification(getString(R.string.preference_title_system_notifications), payload)
                }
                if(viewModel.hasAlerts() || viewModel.hasTss()) {
                    awakenDeviceForAction(AWAKE_TIME) // wake device temporarily for alerts
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                        if (textToSpeechModule != null && viewModel.hasTss()) {
                            textToSpeechModule!!.speakText(payload)
                        }
                    }
                    if (viewModel.hasAlerts()) {
                        alertDialog = AlertDialog.Builder(this@MainActivity, R.style.CustomAlertDialog)
                                .setMessage(payload)
                                .setPositiveButton(android.R.string.ok, null)
                                .show()
                    }
                }
            }
        }
        viewModel.insertMessage(id, topic, payload)
    }

    override fun onMQTTException(message: String) {
        Timber.d("onMQTTException")
        this@MainActivity.runOnUiThread {
            dialogUtils.showAlertDialog(this@MainActivity, message, DialogInterface.OnClickListener { _, _ ->
                if (mqttModule != null) {
                    mqttModule!!.restart()
                }
            })
        }
    }

    override fun onMQTTDisconnect() {
        Timber.d("onMQTTDisconnect")
        this@MainActivity.runOnUiThread {
            dialogUtils.showAlertDialog(this@MainActivity, getString(R.string.error_mqtt_connection), DialogInterface.OnClickListener { _, _ ->
                if (mqttModule != null) {
                    mqttModule!!.restart()
                }
            })
        }
    }

    override fun onCameraException(message: String) {
        this@MainActivity.runOnUiThread {
            dialogUtils.showAlertDialog(this@MainActivity, message)
        }
    }

    override fun onCameraComplete(bitmap: Bitmap) {
        Timber.d("onCameraComplete")
        viewModel.sendCapturedImage(bitmap)
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
    }

    override fun onPageSelected(position: Int) {
    }

    override fun onPageScrollStateChanged(state: Int) {
    }

    override fun manuallyLaunchScreenSaver() {
        showScreenSaver()
    }

    private inner class MainSlidePagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {
        private var currentFragment : BaseFragment? = null

        override fun getItem(position: Int): Fragment {
            when (position) {
                0 -> return MainFragment.newInstance()
                1 -> return PlatformFragment.newInstance()
                else -> return MainFragment.newInstance()
            }
        }
        override fun getCount(): Int {
            return 2
        }

        override fun setPrimaryItem(container: ViewGroup, position: Int, `object`: Any) {
            super.setPrimaryItem(container, position, `object`)

            if (currentFragment != `object`){
                currentFragment = `object` as BaseFragment
            }
        }

        fun getCurrentFragment() = currentFragment
    }

    companion object {
        const val AWAKE_TIME: Long = 30000 // 30 SECONDS
        const val TRIGGERED_AWAKE_TIME: Long = 10800000 // 3 HOURS
    }
}