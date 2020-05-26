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

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import android.content.*
import android.os.Build
import android.os.Bundle
import android.os.Handler

import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.viewpager.widget.*
import com.thanksmister.iot.mqtt.alarmpanel.*
import com.thanksmister.iot.mqtt.alarmpanel.network.AlarmPanelService
import com.thanksmister.iot.mqtt.alarmpanel.ui.fragments.ControlsFragment
import com.thanksmister.iot.mqtt.alarmpanel.ui.fragments.MainFragment
import com.thanksmister.iot.mqtt.alarmpanel.ui.fragments.PlatformFragment
import com.thanksmister.iot.mqtt.alarmpanel.utils.AlarmUtils
import com.thanksmister.iot.mqtt.alarmpanel.viewmodel.MainViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber
import javax.inject.Inject

class MainActivity : BaseActivity(), ViewPager.OnPageChangeListener, ControlsFragment.OnControlsFragmentListener,
        MainFragment.OnMainFragmentListener, PlatformFragment.OnPlatformFragmentListener {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var viewModel: MainViewModel

    private lateinit var pagerAdapter: PagerAdapter
    private var alertDialog: AlertDialog? = null
    private var localBroadCastManager: LocalBroadcastManager? = null
    private var decorView: View? = null
    private var userPresent: Boolean = false
    private val inactivityHandler: Handler = Handler()

    val inactivityCallback = Runnable {
        Timber.d("inactivityCallback")
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

        setContentView(R.layout.activity_main)

        this.window.setFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)

        if(configuration.userHardwareAcceleration && Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            window.setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED, WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED)
        }

        decorView = window.decorView

        if(configuration.cameraEnabled || (configuration.captureCameraImage() || configuration.hasCameraDetections())) {
            window.setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED, WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED)
        }

        if (configuration.appPreventSleep) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            decorView?.keepScreenOn = true
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            decorView?.keepScreenOn = false
        }

        pagerAdapter = MainSlidePagerAdapter(supportFragmentManager)
        view_pager.adapter = pagerAdapter
        view_pager.addOnPageChangeListener(this)
        view_pager.setPagingEnabled(false)

       if(BuildConfig.DEBUG) {
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
            imageOptions.imageSource = BuildConfig.IMGUR_TAG
            configuration.isFirstTime = false
            configuration.setPhotoScreenSaver(false)
            configuration.setHasCameraCapture(true)
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

        // We must be sure we have the instantiated the view model before we observe.
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(MainViewModel::class.java)
        observeViewModel()
    }

    override fun onStart() {
        super.onStart()
        Timber.d("onStart")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(alarmPanelService)
        } else {
            startService(alarmPanelService)
        }
    }

    override fun onUserInteraction() {
        onWindowFocusChanged(true)
        Timber.d("onUserInteraction")
        if(!userPresent) {
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
                .subscribe({state ->
                    this@MainActivity.runOnUiThread {
                        Timber.d("onStart state: " + state)
                        when (state) {
                            AlarmUtils.STATE_TRIGGERED -> {
                                awakenDeviceForAction() // 3 hours
                                stopDisconnectTimer() // stop screen saver mode
                                clearInactivityTimer() // Remove inactivity timer
                            }
                            AlarmUtils.STATE_DISARMED,
                            AlarmUtils.STATE_ARMED_AWAY,
                            AlarmUtils.STATE_ARMED_HOME,
                            AlarmUtils.STATE_ARMING,
                            AlarmUtils.STATE_DISARMING,
                            AlarmUtils.STATE_PENDING -> {
                                awakenDeviceForAction()
                                resetInactivityTimer()
                            }
                        }
                    }
                }, { error -> Timber.e("Unable to get message: " + error)}))

        disposable.add(viewModel.getSun()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                /*.takeWhile { sunValue ->
                    configuration.dayNightMode != sunValue.sun.orEmpty()
                }*/
                .subscribe({sunValue ->
                    this@MainActivity.runOnUiThread {
                        if (configuration.useNightDayMode) {
                            dayNightModeCheck(sunValue.sun)
                        }
                    }
                }, { error -> Timber.e("Sun Data error: " + error)}))

        viewModel.getAlertMessage().observe(this, Observer<String> { message ->
            Timber.d("getAlertMessage")
            dialogUtils.showAlertDialog(this@MainActivity, message)

        })

        viewModel.getToastMessage().observe(this, Observer<String> { message ->
            Timber.d("getToastMessage")
            Toast.makeText(this@MainActivity, message, Toast.LENGTH_LONG).show()
        })
    }

    override fun onResume() {
        Timber.d("onResume")
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
        if(localBroadCastManager != null) {
            localBroadCastManager!!.unregisterReceiver(mBroadcastReceiver)
        }
        super.onPause()
       /* val activityManager = applicationContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        activityManager.moveTaskToFront(taskId, 0)*/
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.d("onDestroy")
        window.clearFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED)
        hideScreenSaver()
        clearInactivityTimer()
        alertDialog?.let {
            it.dismiss()
            alertDialog = null
        }
    }

    // TODO make this play nice for back button
    override fun onBackPressed() {
        if (view_pager.currentItem == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            //super.onBackPressed()
        } else if ((pagerAdapter as MainSlidePagerAdapter).getCurrentFragment()!!.onBackPressed()){
            // backpress handled by fragment do nothing
        } else {
            // Otherwise, if back key press is not handled by fragment, select the previous step.
            view_pager.currentItem = view_pager.currentItem - 1
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
        val intent = Intent(AlarmPanelService.BROADCAST_EVENT_ALARM_MODE)
        intent.putExtra(AlarmPanelService.BROADCAST_EVENT_ALARM_MODE, AlarmUtils.COMMAND_ARM_HOME)
        val bm = LocalBroadcastManager.getInstance(applicationContext)
        bm.sendBroadcast(intent)
    }

    override fun publishArmedAway() {
        Timber.d("publishArmedAway")
        val intent = Intent(AlarmPanelService.BROADCAST_EVENT_ALARM_MODE)
        intent.putExtra(AlarmPanelService.BROADCAST_EVENT_ALARM_MODE, AlarmUtils.COMMAND_ARM_AWAY)
        val bm = LocalBroadcastManager.getInstance(applicationContext)
        bm.sendBroadcast(intent)
    }

    override fun publishArmedNight() {
        Timber.d("publishArmedNight")
        val intent = Intent(AlarmPanelService.BROADCAST_EVENT_ALARM_MODE)
        intent.putExtra(AlarmPanelService.BROADCAST_EVENT_ALARM_MODE, AlarmUtils.COMMAND_ARM_NIGHT)
        val bm = LocalBroadcastManager.getInstance(applicationContext)
        bm.sendBroadcast(intent)
    }

    override fun publishDisarmed() {
        Timber.d("publishDisarmed")
        val intent = Intent(AlarmPanelService.BROADCAST_EVENT_ALARM_MODE)
        intent.putExtra(AlarmPanelService.BROADCAST_EVENT_ALARM_MODE, AlarmUtils.COMMAND_DISARM)
        val bm = LocalBroadcastManager.getInstance(applicationContext)
        bm.sendBroadcast(intent)
    }

    /**
     * We need to awaken the device and allow the user to take action.
     */
    private fun awakenDeviceForAction() {
        Timber.d("awakenDeviceForAction")
        if (view_pager != null && pagerAdapter.count > 0) {
            view_pager.currentItem = 0
        }
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
    }

    override fun onPageSelected(position: Int) {
    }

    override fun onPageScrollStateChanged(state: Int) {
    }

    override fun manuallyLaunchScreenSaver() {
        val intent = Intent(AlarmPanelService.BROADCAST_EVENT_USER_INACTIVE)
        intent.putExtra(AlarmPanelService.BROADCAST_EVENT_USER_INACTIVE, true)
        val bm = LocalBroadcastManager.getInstance(applicationContext)
        bm.sendBroadcast(intent)
        clearInactivityTimer()
        showScreenSaver()
    }

    private inner class MainSlidePagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {
        private var currentFragment : BaseFragment? = null
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

            if (currentFragment != `object`){
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
        if(!userPresent) {
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
                    dialogUtils.showAlertDialog(this@MainActivity, message)
                } catch (e: Exception) {
                    Timber.e(e.message) // getting crashes on some devices
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

    /**
     * Attempts to bring the application to the foreground if needed.
     */
    private fun bringApplicationToForegroundIfNeeded() {
        if (!LifecycleHandler.isApplicationInForeground) {
            Timber.d("bringApplicationToForegroundIfNeeded")
            val intent = Intent("intent.alarm.action")
            intent.component = ComponentName(this@MainActivity.packageName, MainActivity::class.java.name)
            intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }
    }
}