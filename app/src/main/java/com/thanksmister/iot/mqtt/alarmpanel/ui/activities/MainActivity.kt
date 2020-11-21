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

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.snackbar.Snackbar
import com.thanksmister.iot.mqtt.alarmpanel.BaseActivity
import com.thanksmister.iot.mqtt.alarmpanel.BaseFragment
import com.thanksmister.iot.mqtt.alarmpanel.BuildConfig
import com.thanksmister.iot.mqtt.alarmpanel.R
import com.thanksmister.iot.mqtt.alarmpanel.constants.CodeTypes
import com.thanksmister.iot.mqtt.alarmpanel.network.AlarmPanelService
import com.thanksmister.iot.mqtt.alarmpanel.network.AlarmPanelService.Companion.BROADCAST_EVENT_PUBLISH_PANIC
import com.thanksmister.iot.mqtt.alarmpanel.network.AlarmPanelService.Companion.BROADCAST_SNACK_MESSAGE
import com.thanksmister.iot.mqtt.alarmpanel.persistence.Weather
import com.thanksmister.iot.mqtt.alarmpanel.ui.controls.CustomViewPager
import com.thanksmister.iot.mqtt.alarmpanel.ui.fragments.*
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils
import com.thanksmister.iot.mqtt.alarmpanel.viewmodel.MainViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber
import javax.inject.Inject

class MainActivity : BaseActivity(), ViewPager.OnPageChangeListener,
        ControlsFragment.OnControlsFragmentListener,
        PanicBottomSheetFragment.OnBottomSheetFragmentListener,
        MainFragment.OnMainFragmentListener,
        InformationFragment.InformationFragmentListener,
        TriggeredFragment.OnTriggeredFragmentListener,
        PlatformFragment.OnPlatformFragmentListener {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val triggeredView: View by lazy {
        findViewById<View>(R.id.triggeredView)
    }

    private val pagerView: CustomViewPager by lazy {
        findViewById<CustomViewPager>(R.id.view_pager)
    }

    private lateinit var viewModel: MainViewModel
    private lateinit var pagerAdapter: PagerAdapter

    private var alertDialog: AlertDialog? = null
    private var localBroadCastManager: LocalBroadcastManager? = null
    private var decorView: View? = null
    private var userPresent: Boolean = false
    private val inactivityHandler: Handler = Handler()

    private var forecastBottomSheet: ForecastBottomSheetFragment? = null
    private var optionsBottomSheet: OptionsBottomSheetFragment? = null
    private var codeBottomSheet: CodeBottomSheetFragment? = null
    private var previousAlarmMode: String? = null
    private var previousSunState: String? = null

    private val inactivityCallback = Runnable {
        dismissBottomSheets()
        dialogUtils.clearDialogs()
        userPresent = false
        val intent = Intent(AlarmPanelService.BROADCAST_EVENT_USER_INACTIVE)
        intent.putExtra(AlarmPanelService.BROADCAST_EVENT_USER_INACTIVE, true)
        val bm = LocalBroadcastManager.getInstance(applicationContext)
        bm.sendBroadcast(intent)
        clearInactivityTimer()
        showScreenSaver()
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        if (configuration.useDarkTheme) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
        setContentView(R.layout.activity_main)

        this.window.setFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)

        if (configuration.userHardwareAcceleration && Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            window.setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED, WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED)
        }

        decorView = window.decorView

        pagerAdapter = MainSlidePagerAdapter(supportFragmentManager)
        pagerView.adapter = pagerAdapter
        pagerView.addOnPageChangeListener(this)
        pagerView.setPagingEnabled(false)

        if (BuildConfig.DEBUG) {
            configuration.alarmCode = BuildConfig.ALARM_CODE
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
            configuration.isFirstTime = false
            configuration.setClockScreenSaverModule(true)
            configuration.setHasCameraCapture(true)
            configuration.setWebModule(true)
            configuration.setShowWeatherModule(true)
            configuration.setTssModule(true)

            // Sensors
            mqttOptions.sensorOneActive = true
            mqttOptions.sensorOneName = "Main Door"
            mqttOptions.sensorOneState = "closed"
            mqttOptions.sensorTwoActive = true
            mqttOptions.sensorTwoName = "Service Door"
            mqttOptions.sensorTwoState = "closed"
            mqttOptions.sensorThreeActive = true
            mqttOptions.sensorThreeName = "Outside Motion"
            mqttOptions.sensorThreeState = "clear"
            mqttOptions.sensorFourActive = true
            mqttOptions.sensorFourName = "Inside Motion"
            mqttOptions.sensorFourState = "clear"
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

        previousAlarmMode = configuration.alarmMode

        // We must be sure we have the instantiated the view model before we observe.
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(MainViewModel::class.java)

        observeViewModel()

        if (configuration.cameraEnabled || (configuration.captureCameraImage() || configuration.hasCameraDetections())) {
            window.setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED, WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED)
        }
        Timber.d("Prevent Sleep ${configuration.appPreventSleep}")
        if (configuration.appPreventSleep) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            decorView?.keepScreenOn = true
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            decorView?.keepScreenOn = false
        }
    }

    override fun onStart() {
        super.onStart()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(alarmPanelService)
        } else {
            startService(alarmPanelService)
        }
    }

    override fun onUserInteraction() {
        onWindowFocusChanged(true)
        if (!userPresent) {
            userPresent = true
            val intent = Intent(AlarmPanelService.BROADCAST_EVENT_SCREEN_TOUCH)
            intent.putExtra(AlarmPanelService.BROADCAST_EVENT_SCREEN_TOUCH, true)
            val bm = LocalBroadcastManager.getInstance(applicationContext)
            bm.sendBroadcast(intent)
        }
        resetInactivityTimer()
    }

    private fun observeViewModel() {
        disposable.add(viewModel.getAlarmState()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ state ->
                    if (previousAlarmMode != configuration.alarmMode) {
                        this@MainActivity.runOnUiThread {
                            when (state) {
                                MqttUtils.STATE_DISARMED -> {
                                    awakenDeviceForAction()
                                    resetInactivityTimer()
                                }
                                MqttUtils.STATE_ARMED_NIGHT,
                                MqttUtils.STATE_ARMED_AWAY,
                                MqttUtils.STATE_ARMED_HOME -> {
                                    awakenDeviceForAction()
                                    resetInactivityTimer()
                                }
                                MqttUtils.STATE_TRIGGERED -> {
                                    awakenDeviceForAction() // 3 hours
                                    stopDisconnectTimer() // stop screen saver mode
                                    clearInactivityTimer() // Remove inactivity timer
                                }
                                MqttUtils.STATE_ARMING_NIGHT,
                                MqttUtils.STATE_ARMING_AWAY,
                                MqttUtils.STATE_ARMING_HOME,
                                MqttUtils.STATE_ARMING,
                                MqttUtils.STATE_DISARMING,
                                MqttUtils.STATE_PENDING -> {
                                    awakenDeviceForAction()
                                    resetInactivityTimer()
                                }
                            }
                        }
                        previousAlarmMode = state
                    }
                }, { error -> Timber.e("Unable to get message: " + error) }))

        disposable.add(viewModel.getSun()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { sunValue ->
                    if (previousSunState != sunValue.sun) {
                        previousSunState = sunValue.sun
                        this@MainActivity.runOnUiThread {
                            if (configuration.useNightDayMode && configuration.useDarkTheme.not()) {
                                dayNightModeCheck(sunValue.sun)
                            }
                        }
                    }
                })

        viewModel.getAlertMessage().observe(this, Observer<String> { message ->
            Snackbar.make(coordinator, message, Snackbar.LENGTH_LONG)
                    .setAction(android.R.string.ok, View.OnClickListener() {
                        val intent = SettingsActivity.createStartIntent(this@MainActivity)
                        startActivity(intent)
                    }).show()

        })

        viewModel.getToastMessage().observe(this, Observer<String> { message ->
            Toast.makeText(this@MainActivity, message, Toast.LENGTH_LONG).show()
        })
    }

    override fun onResume() {
        super.onResume()
        resetInactivityTimer()
        setViewPagerState()
        // Filter messages from service
        val filter = IntentFilter()
        filter.addAction(AlarmPanelService.BROADCAST_ALERT_MESSAGE)
        filter.addAction(AlarmPanelService.BROADCAST_CLEAR_ALERT_MESSAGE)
        filter.addAction(AlarmPanelService.BROADCAST_TOAST_MESSAGE)
        filter.addAction(AlarmPanelService.BROADCAST_SCREEN_WAKE)
        filter.addAction(AlarmPanelService.BROADCAST_SERVICE_STARTED)
        localBroadCastManager = LocalBroadcastManager.getInstance(this)
        localBroadCastManager!!.registerReceiver(mBroadcastReceiver, filter)
    }

    override fun onPause() {
        super.onPause()
        clearInactivityTimer()
        if (localBroadCastManager != null) {
            localBroadCastManager!!.unregisterReceiver(mBroadcastReceiver)
        }
        dismissBottomSheets()
        alertDialog?.let {
            it.dismiss()
            alertDialog = null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        window.clearFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED)
        hideScreenSaver()
        clearInactivityTimer()
        dismissBottomSheets()
        alertDialog?.let {
            it.dismiss()
            alertDialog = null
        }
        codeBottomSheet?.dismiss()
    }

    override fun onBackPressed() {
        if (pagerView.currentItem == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed()
        } else if ((pagerAdapter as MainSlidePagerAdapter).getCurrentFragment()!!.onBackPressed()) {
            // backpress handled by fragment do nothing
        } else {
            // Otherwise, if back key press is not handled by fragment, select the previous step.
            pagerView.currentItem = pagerView.currentItem - 1
        }
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
                else -> visibility = (View.SYSTEM_UI_FLAG_VISIBLE)
            }
            decorView?.systemUiVisibility = visibility
        }
    }

    override fun navigateAlarmPanel() {
        pagerView.currentItem = 0
    }

    override fun setPagingEnabled(value: Boolean) {
        pagerView.setPagingEnabled(value)
    }

    override fun navigatePlatformPanel() {
        pagerView.currentItem = 1
    }

    private fun setViewPagerState() {
        if (viewModel.hasPlatform()) {
            pagerView.setPagingEnabled(true)
        } else {
            pagerView.setPagingEnabled(false)
        }
    }

    override fun publishArmedHome() {
        val alarmMode = configuration.alarmMode
        val intent = Intent(AlarmPanelService.BROADCAST_EVENT_ALARM_MODE)
        intent.putExtra(AlarmPanelService.BROADCAST_EVENT_ALARM_MODE, MqttUtils.COMMAND_ARM_HOME)
        intent.putExtra(AlarmPanelService.BROADCAST_EVENT_ALARM_CODE, "")
        val bm = LocalBroadcastManager.getInstance(applicationContext)
        bm.sendBroadcast(intent)
    }

    override fun publishArmedAway() {
        val alarmMode = configuration.alarmMode
        val intent = Intent(AlarmPanelService.BROADCAST_EVENT_ALARM_MODE)
        intent.putExtra(AlarmPanelService.BROADCAST_EVENT_ALARM_MODE, MqttUtils.COMMAND_ARM_AWAY)
        intent.putExtra(AlarmPanelService.BROADCAST_EVENT_ALARM_CODE, "")
        val bm = LocalBroadcastManager.getInstance(applicationContext)
        bm.sendBroadcast(intent)
    }

    override fun publishArmedNight() {
        val alarmMode = configuration.alarmMode
        val intent = Intent(AlarmPanelService.BROADCAST_EVENT_ALARM_MODE)
        intent.putExtra(AlarmPanelService.BROADCAST_EVENT_ALARM_MODE, MqttUtils.COMMAND_ARM_NIGHT)
        intent.putExtra(AlarmPanelService.BROADCAST_EVENT_ALARM_CODE, "")
        val bm = LocalBroadcastManager.getInstance(applicationContext)
        bm.sendBroadcast(intent)
    }

    override fun publishDisarm(code: String) {
        val alarmMode = configuration.alarmMode
        val intent = Intent(AlarmPanelService.BROADCAST_EVENT_ALARM_MODE)
        intent.putExtra(AlarmPanelService.BROADCAST_EVENT_ALARM_MODE, MqttUtils.COMMAND_DISARM)
        intent.putExtra(AlarmPanelService.BROADCAST_EVENT_ALARM_CODE, code)
        val bm = LocalBroadcastManager.getInstance(applicationContext)
        bm.sendBroadcast(intent)
    }

    override fun publishAlertCall() {
        val bottomSheetFragment = PanicBottomSheetFragment()
        bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
    }

    /**
     * Show the code dialog with a CodeTypes value take different actions on code such as disarm, settings, or arming.
     */
    override fun showCodeDialog(type: CodeTypes) {
        var codeType = type
        if (mqttOptions.useRemoteConfig) {
            if (mqttOptions.requireCodeForDisarming && type == CodeTypes.DISARM) {
                codeType = CodeTypes.DISARM_REMOTE
            } else if (mqttOptions.requireCodeForArming && type == CodeTypes.ARM) {
                codeType = CodeTypes.ARM_REMOTE
            }
        }
        codeBottomSheet = CodeBottomSheetFragment.newInstance(configuration.alarmCode.toString(), codeType,
                object : CodeBottomSheetFragment.OnAlarmCodeFragmentListener {
                    override fun onComplete(code: String) {
                        when (type) {
                            CodeTypes.DISARM -> {
                                publishDisarm(code)
                            }
                            CodeTypes.SETTINGS -> {
                                val intent = SettingsActivity.createStartIntent(this@MainActivity)
                                startActivity(intent)
                            }
                            CodeTypes.ARM_HOME -> {
                                publishArmedHome()
                            }
                            CodeTypes.ARM_AWAY -> {
                                publishArmedAway()
                            }
                            CodeTypes.ARM_NIGHT -> {
                                publishArmedNight()
                            }
                            else -> {
                                // na-da
                            }
                        }
                        codeBottomSheet?.dismiss()
                    }

                    override fun onCodeError() {
                        Toast.makeText(this@MainActivity, R.string.toast_code_invalid, Toast.LENGTH_SHORT).show()
                    }

                    override fun onCancel() {
                        codeBottomSheet?.dismiss()
                    }
                })
        codeBottomSheet?.show(supportFragmentManager, codeBottomSheet?.tag)
    }

    override fun showAlarmTriggered() {
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) // keep the screen awake
        triggeredView.visibility = View.VISIBLE
        pagerView.visibility = View.INVISIBLE
    }

    override fun hideTriggeredView() {
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) // let the screen sleep
        pagerView.visibility = View.VISIBLE
        triggeredView.visibility = View.INVISIBLE
    }

    override fun showArmOptionsDialog() {
        optionsBottomSheet = OptionsBottomSheetFragment(object : OptionsBottomSheetFragment.OptionsBottomSheetFragmentListener {
            override fun onArmHome() {
                if (mqttOptions.useRemoteConfig && mqttOptions.requireCodeForArming) {
                    showCodeDialog(CodeTypes.ARM_HOME)
                } else {
                    publishArmedHome()
                }
            }

            override fun onArmAway() {
                if (mqttOptions.useRemoteConfig && mqttOptions.requireCodeForArming) {
                    showCodeDialog(CodeTypes.ARM_AWAY)
                } else {
                    publishArmedAway()
                }
            }

            override fun onArmNight() {
                if (mqttOptions.useRemoteConfig && mqttOptions.requireCodeForArming) {
                    showCodeDialog(CodeTypes.ARM_NIGHT)
                } else {
                    publishArmedNight()
                }
            }
        })
        optionsBottomSheet?.show(supportFragmentManager, optionsBottomSheet?.tag)
    }

    override fun onSendAlert() {
        val intent = Intent(BROADCAST_EVENT_PUBLISH_PANIC)
        intent.putExtra(BROADCAST_EVENT_PUBLISH_PANIC, MqttUtils.COMMAND_ON)
        val bm = LocalBroadcastManager.getInstance(applicationContext)
        bm.sendBroadcast(intent)
    }

    /**
     * We need to awaken the device and allow the user to take action.
     */
    private fun awakenDeviceForAction() {
        Timber.d("awakenDeviceForAction")
        if (pagerAdapter.count > 0) {
            pagerView.currentItem = 0
        }
    }

    /**
     * As part of cleanup we should dismiss any bottom sheets either due to user inaction or other events.
     */
    private fun dismissBottomSheets() {
        forecastBottomSheet?.dismiss()
        codeBottomSheet?.dismiss()
        optionsBottomSheet?.dismiss()
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
    }

    override fun onPageSelected(position: Int) {
    }

    override fun onPageScrollStateChanged(state: Int) {
    }

    override fun manuallyLaunchScreenSaver() {
        val alarmMode = configuration.alarmMode
        if (configuration.isAlarmTriggered().not() && configuration.isAlarmArming().not() && configuration.isDisarming().not()) {
            showScreenSaver()
        }
        clearInactivityTimer()
    }

    private inner class MainSlidePagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {
        private var currentFragment: BaseFragment? = null
        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> MainFragment.newInstance()
                1 -> PlatformFragment.newInstance()
                else -> MainFragment.newInstance()
            }
        }

        override fun getCount(): Int {
            return 2
        }

        override fun setPrimaryItem(container: ViewGroup, position: Int, `object`: Any) {
            super.setPrimaryItem(container, position, `object`)

            if (currentFragment != `object`) {
                currentFragment = `object` as BaseFragment
            }
        }

        fun getCurrentFragment() = currentFragment
    }

    private fun resetInactivityTimer() {
        Timber.d("resetInactivityTimer")
        hideScreenSaver()
        inactivityHandler.removeCallbacks(inactivityCallback)
        inactivityHandler.postDelayed(inactivityCallback, configuration.inactivityTime)
    }

    /**
     * We only remove the inactivity handler if the activity is destroyed, the alarm is triggered
     * or we have a screen saver.   This should only be called during one of those three scenarios.
     */
    private fun clearInactivityTimer() {
        Timber.d("clearInactivityTimer")
        inactivityHandler.removeCallbacks(inactivityCallback)
    }

    private fun stopDisconnectTimer() {
        if (!userPresent) {
            userPresent = true
        }
        hideScreenSaver()
        clearInactivityTimer()
    }

    // handler for received data from service
    private val mBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Timber.d("intent.action ${intent.action}")
            if (AlarmPanelService.BROADCAST_ALERT_MESSAGE == intent.action && !isFinishing) {
                val message = intent.getStringExtra(AlarmPanelService.BROADCAST_ALERT_MESSAGE)
                try {
                    resetInactivityTimer()
                    dialogUtils.showAlertDialog(this@MainActivity, message.orEmpty())
                } catch (e: Exception) {
                    Timber.e(e.message) // getting crashes on some devices
                }
            } else if (BROADCAST_SNACK_MESSAGE == intent.action && !isFinishing) {
                val message = intent.getStringExtra(AlarmPanelService.BROADCAST_SNACK_MESSAGE)
                message?.let {
                    Snackbar.make(coordinator, message, Snackbar.LENGTH_LONG)
                            .setAction(android.R.string.ok, View.OnClickListener() {
                                // na-da
                            }).show()
                }
            } else if (AlarmPanelService.BROADCAST_TOAST_MESSAGE == intent.action && !isFinishing) {
                resetInactivityTimer()
                val message = intent.getStringExtra(AlarmPanelService.BROADCAST_TOAST_MESSAGE)
                Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
            } else if (AlarmPanelService.BROADCAST_SCREEN_WAKE == intent.action && !isFinishing) {
                resetInactivityTimer()
            } else if (AlarmPanelService.BROADCAST_CLEAR_ALERT_MESSAGE == intent.action && !isFinishing) {
                resetInactivityTimer()
                dialogUtils.clearDialogs()
            } else if (AlarmPanelService.BROADCAST_SERVICE_STARTED == intent.action && !isFinishing) {
                serviceStarted = true
            }
        }
    }

    override fun openExtendedForecast(weather: Weather) {
        supportFragmentManager.let {
            forecastBottomSheet = ForecastBottomSheetFragment(weather)
            forecastBottomSheet?.show(it, forecastBottomSheet?.tag)
        }
    }
}