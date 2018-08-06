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

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.*
import android.os.Build
import android.os.Bundle
import android.support.annotation.LayoutRes
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.content.LocalBroadcastManager
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AlertDialog
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import com.thanksmister.iot.mqtt.alarmpanel.BaseActivity
import com.thanksmister.iot.mqtt.alarmpanel.BaseFragment
import com.thanksmister.iot.mqtt.alarmpanel.BuildConfig
import com.thanksmister.iot.mqtt.alarmpanel.R
import com.thanksmister.iot.mqtt.alarmpanel.managers.DayNightAlarmLiveData
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
    private var alarmLiveData: DayNightAlarmLiveData? = null
    private var localBroadCastManager: LocalBroadcastManager? = null
    private var decorView: View? = null
    private var alarmPanelService: Intent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        this.window.setFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)

        decorView = window.decorView

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

            configuration.setClockScreenSaverModule(true)
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
        lifecycle.addObserver(dialogUtils)
        observeViewModel()

        // Filter messages from service
        val filter = IntentFilter()
        filter.addAction(AlarmPanelService.BROADCAST_ALERT_MESSAGE)
        filter.addAction(AlarmPanelService.BROADCAST_TOAST_MESSAGE)
        filter.addAction(AlarmPanelService.BROADCAST_SCREEN_WAKE)
        localBroadCastManager = LocalBroadcastManager.getInstance(this)
        localBroadCastManager!!.registerReceiver(mBroadcastReceiver, filter)

        if(configuration.cameraEnabled || (configuration.captureCameraImage() || configuration.hasCameraDetections())) {
            window.setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED, WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED)
        }

        Timber.d("Prevent Sleep ${configuration.appPreventSleep}")
        if (configuration.appPreventSleep) {
            window.addFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON )
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    override fun onStart() {
        super.onStart()
        Timber.d("onStart")
        alarmPanelService = Intent(this, AlarmPanelService::class.java)
        startService(alarmPanelService)
    }

    override fun onStop() {
        super.onStop()
        Timber.d("onStop")
        if(alarmPanelService != null) {
            //stopService(alarmPanelService)
        }
    }

    private fun observeViewModel() {
        disposable.add(viewModel.getAlarmState()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({state ->
                    this@MainActivity.runOnUiThread {
                        Timber.d("onStart state: " + state)
                        when (state) {
                            AlarmUtils.STATE_DISARM -> {
                                awakenDeviceForAction()
                                resetInactivityTimer()
                            }
                            AlarmUtils.STATE_ARM_AWAY,
                            AlarmUtils.STATE_ARM_HOME -> {
                                awakenDeviceForAction()
                                resetInactivityTimer()
                            }
                            AlarmUtils.STATE_TRIGGERED -> {
                                awakenDeviceForAction() // 3 hours
                                stopDisconnectTimer() // stop screen saver mode
                            }
                            AlarmUtils.STATE_PENDING -> {
                                awakenDeviceForAction()
                                resetInactivityTimer()
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

        alarmLiveData = DayNightAlarmLiveData(this@MainActivity, configuration)
        alarmLiveData?.observe(this, Observer { dayNightMode ->
            dayNightModeCheck(dayNightMode)
        })
    }

    override fun onResume() {
        Timber.d("onResume")
        super.onResume()
        resetInactivityTimer()
        setViewPagerState()
    }

    override fun onDestroy() {
        Timber.d("onDestroy")
        super.onDestroy()
        if(alertDialog != null) {
            alertDialog?.dismiss()
            alertDialog = null
        }
        if(localBroadCastManager != null) {
            localBroadCastManager!!.unregisterReceiver(mBroadcastReceiver)
        }
        window.clearFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED)
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
        bringApplicationToForegroundIfNeeded()
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

    // handler for received data from service
    private val mBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (AlarmPanelService.BROADCAST_ALERT_MESSAGE == intent.action) {
                val message = intent.getStringExtra(AlarmPanelService.BROADCAST_ALERT_MESSAGE)
                dialogUtils.showAlertDialog(this@MainActivity, message)
            } else if (AlarmPanelService.BROADCAST_TOAST_MESSAGE == intent.action) {
                val message = intent.getStringExtra(AlarmPanelService.BROADCAST_ALERT_MESSAGE)
                Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
            } else if (AlarmPanelService.BROADCAST_SCREEN_WAKE == intent.action) {
                resetInactivityTimer()
            }
        }
    }
}