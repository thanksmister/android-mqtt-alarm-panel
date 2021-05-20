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
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.snackbar.Snackbar
import com.thanksmister.iot.mqtt.alarmpanel.*
import com.thanksmister.iot.mqtt.alarmpanel.constants.CodeTypes
import com.thanksmister.iot.mqtt.alarmpanel.network.AlarmPanelService
import com.thanksmister.iot.mqtt.alarmpanel.network.AlarmPanelService.Companion.BROADCAST_EVENT_PUBLISH_PANIC
import com.thanksmister.iot.mqtt.alarmpanel.network.AlarmPanelService.Companion.BROADCAST_SNACK_MESSAGE
import com.thanksmister.iot.mqtt.alarmpanel.persistence.Weather
import com.thanksmister.iot.mqtt.alarmpanel.ui.adapters.MainSlidePagerAdapter
import com.thanksmister.iot.mqtt.alarmpanel.ui.fragments.*
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils
import com.thanksmister.iot.mqtt.alarmpanel.viewmodel.MainViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_main.*
import timber.log.Timber
import javax.inject.Inject

class MainActivity : BaseActivity(),
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

    private val pagerView: ViewPager2 by lazy {
        findViewById<ViewPager2>(R.id.view_pager)
    }

    private lateinit var viewModel: MainViewModel

    private var alertDialog: AlertDialog? = null
    private var localBroadCastManager: LocalBroadcastManager? = null
    private var decorView: View? = null
    private var userPresent: Boolean = false
    private val inactivityHandler: Handler = Handler()

    private var forecastBottomSheet: ForecastBottomSheetFragment? = null
    private var optionsBottomSheet: OptionsBottomSheetFragment? = null
    private var codeBottomSheet: CodeBottomSheetFragment? = null
    private var previousAlarmMode: String? = null
    private var snackbar: Snackbar? = null
    private var dots = ArrayList<ImageView>()
    private var pages: Int = 0

    private val pagerAdapter: MainSlidePagerAdapter by lazy {
        MainSlidePagerAdapter(this)
    }

    private val inactivityCallback = Runnable {
        dismissBottomSheets()
        dialogUtils.clearDialogs()
        userPresent = false
        val intent = Intent(AlarmPanelService.BROADCAST_EVENT_USER_INACTIVE)
        intent.putExtra(AlarmPanelService.BROADCAST_EVENT_USER_INACTIVE, true)
        val bm = LocalBroadcastManager.getInstance(applicationContext)
        bm.sendBroadcast(intent)
        if(configuration.useInactivityTimer) {
            navigateAlarmPanel()
        }
        manuallyLaunchScreenSaver()
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        try {
            setContentView(R.layout.activity_main)
        } catch (e: Exception) {
            Timber.e(e.message)
        }

        Thread.setDefaultUncaughtExceptionHandler(AppExceptionHandler(this))

        this.window.setFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)

        if (configuration.userHardwareAcceleration && Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            window.setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED, WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED)
        }

        decorView = window.decorView

        pagerView.adapter = pagerAdapter

        pagerView.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                selectDot(position)
            }
        })

        buttonSettings?.setOnClickListener {
            showSettingsCodeDialog()
        }
        platformButton.setOnClickListener {
            navigateAlarmPanel()
        }
        buttonSleep.setOnClickListener {
            manuallyLaunchScreenSaver()
        }
        alertButton.setOnClickListener {
            publishAlertCall()
        }
        buttonRefresh.setOnClickListener {
            // TODO refresh browser
        }

        if (BuildConfig.DEBUG) {
            configuration.alarmCode = BuildConfig.ALARM_CODE
            mqttOptions.setBroker(BuildConfig.BROKER)
            mqttOptions.setPassword("3355")
            mqttOptions.setUsername("mister")
            configuration.webUrl = BuildConfig.HASS_URL
            configuration.setMailFrom(BuildConfig.MAIL_FROM)
            configuration.setMailGunApiKey(BuildConfig.MAIL_GUN_KEY)
            configuration.setMailTo(BuildConfig.MAIL_TO)
            configuration.setMailGunUrl(BuildConfig.MAIL_GUN_URL)
            configuration.telegramChatId = BuildConfig.TELEGRAM_CHAT_ID
            configuration.telegramToken = BuildConfig.TELEGRAM_TOKEN
            configuration.isFirstTime = false
            configuration.setClockScreenSaverModule(true)
            configuration.setHasCameraCapture(false)
            configuration.setWebModule(false)
            configuration.setShowWeatherModule(true)
            configuration.useNightDayMode = true
        }

        previousAlarmMode = configuration.alarmMode

        // We must be sure we have the instantiated the view model before we observe.
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(MainViewModel::class.java)

        if (configuration.cameraEnabled || (configuration.captureCameraImage() || configuration.hasCameraDetections())) {
            window.setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED, WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED)
        }

        if (configuration.appPreventSleep) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            decorView?.keepScreenOn = true
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            decorView?.keepScreenOn = false
        }

        if(configuration.useDarkTheme) {
            setDarkTheme()
        } else  {
            if (configuration.useNightDayMode ) {
                setDayNightMode()
            } else {
                setLightTheme()
            }
        }

        observeViewModel()
    }

    override fun onStart() {
        super.onStart()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && serviceStarted.not()) {
            ContextCompat.startForegroundService(this, alarmPanelService)
        } else if (serviceStarted.not()) {
            startService(alarmPanelService)
        }

        val firstTime = configuration.isFirstTime
        if (firstTime) {
            alertDialog = AlertDialog.Builder(this@MainActivity, R.style.CustomAlertDialog)
                    .setMessage(getString(R.string.dialog_first_time))
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        openSettings()
                    }
                    .show()
        } else {
            val nightModeChanged = configuration.nightModeChanged
            val useDarkTheme = configuration.useDarkTheme
            if (useDarkTheme && nightModeChanged) {
                configuration.nightModeChanged = false
                setDarkTheme(true)
            } else if (nightModeChanged) {
                configuration.nightModeChanged = false
                setDayNightMode(true)
            }
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
        disposable.add(viewModel.getDashboards()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    if(it.isEmpty().not()) {
                        pagerAdapter.addDashboards(it)
                        addDots(it.size + 1)
                    } else {
                        addDots(0)
                    }
                }
        )

        disposable.add(viewModel.getAlarmState()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ state ->
                    val payload = state.payload
                    val delay = state.delay
                    if (previousAlarmMode != configuration.alarmMode) {
                        this@MainActivity.runOnUiThread {
                            when (payload) {
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
                                MqttUtils.STATE_PENDING -> {
                                    awakenDeviceForAction()
                                    resetInactivityTimer()
                                    if (configuration.isAlarmArmedMode()) {
                                        showCodeDialog(CodeTypes.DISARM, delay)
                                    }
                                }
                                MqttUtils.STATE_ARMING -> {
                                    awakenDeviceForAction()
                                    resetInactivityTimer()
                                }
                            }
                        }
                        previousAlarmMode = payload
                    }
                }, { error -> Timber.e("Unable to get message: " + error) }))

        disposable.add(viewModel.getSun()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { sunValue ->
                    this@MainActivity.runOnUiThread {
                        val useNightMode = configuration.useNightDayMode
                        val useDarkTheme = configuration.useDarkTheme
                        if (useNightMode && useDarkTheme.not()) {
                            dayNightModeCheck(sunValue.sun)
                        }
                    }
                })

        viewModel.getAlertMessage().observe(this, Observer<String> { message ->
            snackbar?.dismiss()
            snackbar = Snackbar.make(coordinator, message, Snackbar.LENGTH_LONG)
                    .setAction(android.R.string.ok, View.OnClickListener() {
                        openSettings()
                    })
            snackbar?.show()

        })

        viewModel.getToastMessage().observe(this, Observer<String> { message ->
            Toast.makeText(this@MainActivity, message, Toast.LENGTH_LONG).show()
        })
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
    }

    override fun onRestart() {
        super.onRestart()
    }

    override fun onResume() {
        super.onResume()
        resetInactivityTimer()
        // Filter messages from service
        val filter = IntentFilter()
        filter.addAction(AlarmPanelService.BROADCAST_ALERT_MESSAGE)
        filter.addAction(AlarmPanelService.BROADCAST_CLEAR_ALERT_MESSAGE)
        filter.addAction(AlarmPanelService.BROADCAST_TOAST_MESSAGE)
        filter.addAction(AlarmPanelService.BROADCAST_SCREEN_WAKE)
        filter.addAction(AlarmPanelService.BROADCAST_SERVICE_STARTED)
        filter.addAction(AlarmPanelService.BROADCAST_DASHBOARD)
        filter.addAction(BROADCAST_SNACK_MESSAGE)
        filter.addAction(BROADCAST_EVENT_PUBLISH_PANIC)
        localBroadCastManager = LocalBroadcastManager.getInstance(this)
        localBroadCastManager!!.registerReceiver(mBroadcastReceiver, filter)
        addDots(pages)
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
        buttonSleep?.apply {
            setOnTouchListener(null)
        }
    }

    override fun onBackPressed() {
        if (pagerView.currentItem == 0) {
            // If the user is currently looking at the first page, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed()
        } else {
            // Otherwise, select the previous page.
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
            visibility = when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT -> (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_VISIBLE)
                else -> (View.SYSTEM_UI_FLAG_VISIBLE)
            }
            decorView?.systemUiVisibility = visibility
        }
    }

    override fun navigateAlarmPanel() {
        pagerView.currentItem = 0
    }

    override fun setPagingEnabled(value: Boolean) {
        //pagerView.setPagingEnabled(value)
    }

    override fun navigateDashBoard(dashboard: Int) {
        val itemCount = pagerAdapter.itemCount
        if(itemCount >= 0 && dashboard < itemCount) {
            hideScreenSaver()
            pagerView.currentItem = dashboard
        }
    }

    override fun publishArmedHome(code: String) {
        val intent = Intent(AlarmPanelService.BROADCAST_EVENT_ALARM_MODE)
        intent.putExtra(AlarmPanelService.BROADCAST_EVENT_ALARM_MODE, MqttUtils.COMMAND_ARM_HOME)
        intent.putExtra(AlarmPanelService.BROADCAST_EVENT_ALARM_CODE, code)
        val bm = LocalBroadcastManager.getInstance(applicationContext)
        bm.sendBroadcast(intent)
    }

    override fun publishArmedAway(code: String) {
        val intent = Intent(AlarmPanelService.BROADCAST_EVENT_ALARM_MODE)
        intent.putExtra(AlarmPanelService.BROADCAST_EVENT_ALARM_MODE, MqttUtils.COMMAND_ARM_AWAY)
        intent.putExtra(AlarmPanelService.BROADCAST_EVENT_ALARM_CODE, code)
        val bm = LocalBroadcastManager.getInstance(applicationContext)
        bm.sendBroadcast(intent)
    }

    override fun publishArmedNight(code: String) {
        val intent = Intent(AlarmPanelService.BROADCAST_EVENT_ALARM_MODE)
        intent.putExtra(AlarmPanelService.BROADCAST_EVENT_ALARM_MODE, MqttUtils.COMMAND_ARM_NIGHT)
        intent.putExtra(AlarmPanelService.BROADCAST_EVENT_ALARM_CODE, code)
        val bm = LocalBroadcastManager.getInstance(applicationContext)
        bm.sendBroadcast(intent)
    }

    override fun publishCustomBypass(code: String) {
        val intent = Intent(AlarmPanelService.BROADCAST_EVENT_ALARM_MODE)
        intent.putExtra(AlarmPanelService.BROADCAST_EVENT_ALARM_MODE, MqttUtils.COMMAND_ARM_CUSTOM_BYPASS)
        intent.putExtra(AlarmPanelService.BROADCAST_EVENT_ALARM_CODE, code)
        val bm = LocalBroadcastManager.getInstance(applicationContext)
        bm.sendBroadcast(intent)
    }

    override fun publishDisarm(code: String) {
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

    private fun showSettingsCodeDialog() {
        if (configuration.isFirstTime) {
            openSettings()
        } else {
            showCodeDialog(CodeTypes.SETTINGS, -1)
        }
    }

    private fun openSettings() {
        val alarmPanelService = Intent(this, AlarmPanelService::class.java)
        stopService(alarmPanelService)
        serviceStarted = false
        val intent = SettingsActivity.createStartIntent(applicationContext)
        startActivity(intent)
    }

    /**
     * Show the code dialog with a CodeTypes value take different actions on code such as disarm, settings, or arming.
     */
    @Deprecated("Doesn't appear to be used")
    private fun showDisarmCodeDialog(delay: Int?) {
        var codeType = CodeTypes.DISARM
        if (mqttOptions.useRemoteCode) {
            codeType = CodeTypes.DISARM_REMOTE
        }
        codeBottomSheet = CodeBottomSheetFragment.newInstance(configuration.alarmCode.toString(), delay, codeType,
                object : CodeBottomSheetFragment.OnAlarmCodeFragmentListener {
                    override fun onComplete(code: String) {
                        if (codeType == CodeTypes.DISARM) {
                            publishDisarm(code)
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

    /**
     * Show the code dialog with a CodeTypes value take different actions on code such as disarm, settings, or arming.
     */
    override fun showCodeDialog(type: CodeTypes, delay: Int?) {
        var codeType = type
        val useRemoteCode = mqttOptions.useRemoteCode
        if (useRemoteCode) {
            if (type == CodeTypes.DISARM) {
                codeType = CodeTypes.DISARM_REMOTE
            } else if (type == CodeTypes.ARM_HOME || type == CodeTypes.ARM_AWAY || type == CodeTypes.ARM_NIGHT || type == CodeTypes.ARM_BYPASS) {
                codeType = CodeTypes.ARM_REMOTE
            }
        }
        codeBottomSheet = CodeBottomSheetFragment.newInstance(configuration.alarmCode.toString(), delay, codeType,
                object : CodeBottomSheetFragment.OnAlarmCodeFragmentListener {
                    override fun onComplete(code: String) {
                        when (type) {
                            CodeTypes.DISARM -> {
                                publishDisarm(code)
                            }
                            CodeTypes.SETTINGS -> {
                                openSettings()
                            }
                            CodeTypes.ARM_HOME -> {
                                publishArmedHome(code)
                            }
                            CodeTypes.ARM_AWAY -> {
                                publishArmedAway(code)
                            }
                            CodeTypes.ARM_NIGHT -> {
                                publishArmedNight(code)
                            }
                            CodeTypes.ARM_BYPASS -> {
                                publishCustomBypass(code)
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
        //pagerView.visibility = View.GONE
    }

    override fun hideTriggeredView() {
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) // let the screen sleep
        //pagerView.visibility = View.VISIBLE
        triggeredView.visibility = View.GONE
    }

    override fun showArmOptionsDialog() {
        optionsBottomSheet = OptionsBottomSheetFragment(object : OptionsBottomSheetFragment.OptionsBottomSheetFragmentListener {
            override fun onArmHome() {
                if (mqttOptions.requireCodeForArming) {
                    showCodeDialog(CodeTypes.ARM_HOME, -1)
                } else {
                    publishArmedHome("")
                }
            }

            override fun onArmAway() {
                if (mqttOptions.requireCodeForArming) {
                    showCodeDialog(CodeTypes.ARM_AWAY, -1)
                } else {
                    publishArmedAway("")
                }
            }

            override fun onArmNight() {
                if (mqttOptions.requireCodeForArming) {
                    showCodeDialog(CodeTypes.ARM_NIGHT, -1)
                } else {
                    publishArmedNight("")
                }
            }

            override fun onArmCustomBypass() {
                if (mqttOptions.requireCodeForArming) {
                    showCodeDialog(CodeTypes.ARM_BYPASS, -1)
                } else {
                    publishCustomBypass("")
                }
            }
        }, mqttOptions = mqttOptions)
        optionsBottomSheet?.show(supportFragmentManager, optionsBottomSheet?.tag)
    }

    override fun onSendAlert() {
        val intent = Intent(BROADCAST_EVENT_PUBLISH_PANIC)
        intent.putExtra(BROADCAST_EVENT_PUBLISH_PANIC, MqttUtils.COMMAND_PANIC)
        val bm = LocalBroadcastManager.getInstance(applicationContext)
        bm.sendBroadcast(intent)
    }

    /**
     * We need to awaken the device and allow the user to take action.
     */
    private fun awakenDeviceForAction() {
        Timber.d("awakenDeviceForAction")
        if (pagerAdapter.itemCount > 0) {
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

    override fun manuallyLaunchScreenSaver() {
        showScreenSaver()
        clearInactivityTimer()
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
                    snackbar?.dismiss()
                    snackbar = Snackbar.make(coordinator, message, Snackbar.LENGTH_LONG)
                            .setAction(android.R.string.ok) {
                                snackbar?.dismiss()
                            }
                    snackbar?.show()
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
            } else if (AlarmPanelService.BROADCAST_DASHBOARD == intent.action && !isFinishing) {
                val dashboard = intent.getIntExtra(AlarmPanelService.BROADCAST_DASHBOARD, 0)
                navigateDashBoard(dashboard)
            }
        }
    }

    private fun addDots(size: Int) {
        pages = size
        dotsLayout.removeAllViews()
        dots = ArrayList<ImageView>()
        for (i in 1..pages) {
            val dot = ImageView(dotsLayout.context)
            dot.setImageDrawable(resources.getDrawable(R.drawable.tab_indicator_default))
            val params = LinearLayoutCompat.LayoutParams(
                    LinearLayoutCompat.LayoutParams.WRAP_CONTENT,
                    LinearLayoutCompat.LayoutParams.WRAP_CONTENT
            )
            dot.setPadding(8, 0, 8, 0);
            dotsLayout.addView(dot, params)
            dots.add(dot)
        }
        selectDot(0)
    }

    private fun selectDot(idx: Int) {
        for (i in 0 until pages) {
            val drawableId: Int = if (i == idx) R.drawable.tab_indicator_selected else R.drawable.tab_indicator_default
            val drawable: Drawable = resources.getDrawable(drawableId)
            dots[i].setImageDrawable(drawable)
        }
        if(idx == 0) {
            //settingsContainer.visibility = View.VISIBLE
            buttonSettings.visibility = View.VISIBLE
            if (configuration.hasScreenSaver()) {
                buttonSleep.visibility = View.VISIBLE
            } else {
                buttonSleep.visibility = View.GONE
            }
            if (configuration.panicButton.not()) {
                alertButton?.visibility = View.GONE
            } else {
                alertButton?.visibility = View.VISIBLE
            }
            buttonRefresh.visibility = View.GONE
            platformButton.visibility = View.GONE
        } else {
            if(configuration.platformBar.not()) {
               // settingsContainer.visibility = View.GONE
            } else {
                //settingsContainer.visibility = View.VISIBLE
                buttonSleep.visibility = View.GONE
                alertButton.visibility = View.GONE
                buttonRefresh.visibility = View.VISIBLE
                platformButton.visibility = View.VISIBLE
                buttonSettings.visibility = View.GONE
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