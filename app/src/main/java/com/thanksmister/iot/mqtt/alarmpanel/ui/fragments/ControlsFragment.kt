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

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.RotateAnimation
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.material.snackbar.Snackbar
import com.thanksmister.iot.mqtt.alarmpanel.BaseActivity
import com.thanksmister.iot.mqtt.alarmpanel.BaseFragment
import com.thanksmister.iot.mqtt.alarmpanel.R
import com.thanksmister.iot.mqtt.alarmpanel.constants.CodeTypes
import com.thanksmister.iot.mqtt.alarmpanel.network.AlarmPanelService
import com.thanksmister.iot.mqtt.alarmpanel.network.MQTTOptions
import com.thanksmister.iot.mqtt.alarmpanel.persistence.Configuration
import com.thanksmister.iot.mqtt.alarmpanel.persistence.Sensor
import com.thanksmister.iot.mqtt.alarmpanel.ui.views.AlarmDelayView
import com.thanksmister.iot.mqtt.alarmpanel.utils.DialogUtils
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils.Companion.COMMAND_ARM_AWAY
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils.Companion.COMMAND_ARM_CUSTOM_BYPASS
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils.Companion.COMMAND_ARM_HOME
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils.Companion.COMMAND_ARM_NIGHT
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils.Companion.COMMAND_DISARM
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils.Companion.STATE_ARMED_AWAY
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils.Companion.STATE_ARMED_CUSTOM_BYPASS
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils.Companion.STATE_ARMED_HOME
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils.Companion.STATE_ARMED_NIGHT
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils.Companion.STATE_ARMING
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils.Companion.STATE_ARM_AWAY
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils.Companion.STATE_ARM_CUSTOM_BYPASS
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils.Companion.STATE_ARM_HOME
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils.Companion.STATE_ARM_NIGHT
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils.Companion.STATE_DISABLED
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils.Companion.STATE_DISARM
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils.Companion.STATE_DISARMED
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils.Companion.STATE_PENDING
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils.Companion.STATE_TRIGGERED
import com.thanksmister.iot.mqtt.alarmpanel.viewmodel.MainViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_controls.*
import timber.log.Timber
import java.io.FileNotFoundException
import javax.inject.Inject


@Suppress("DEPRECATION")
class ControlsFragment : BaseFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    lateinit var viewModel: MainViewModel

    @Inject
    lateinit var dialogUtils: DialogUtils

    @Inject
    lateinit var configuration: Configuration

    @Inject
    lateinit var mqttOptions: MQTTOptions

    private var listener: OnControlsFragmentListener? = null
    private var filter = IntentFilter(AlarmPanelService.BROADCAST_ALARM_COMMAND)
    private var delayTimerHandler: Handler? = null
    private var mediaPlayer: MediaPlayer? = null
    private var alarmListener: AlarmDelayView.ViewListener? = null
    private var pendingSoundFlag = false
    private var countDownTimeRemaining: Int = 0
    private var countDownTimer: CountDownTimer? = null
    private var sensorActiveMap: HashMap<String, Sensor> = HashMap<String, Sensor>()

    @Deprecated("We don't need this any longer")
    private val delayTimerRunnable = object : Runnable {
        override fun run() {
            delayTimerHandler?.removeCallbacks(this)
            when (configuration.alarmMode) {
                STATE_ARMED_HOME -> {
                    setArmedAwayView(configuration.alarmMode)
                }
                STATE_ARMED_AWAY -> {
                    setArmedAwayView(configuration.alarmMode)
                }
                STATE_ARMED_NIGHT -> {
                    setArmedAwayView(configuration.alarmMode)
                }
                STATE_ARMED_CUSTOM_BYPASS -> {
                    setArmedCustomBypass(configuration.alarmMode)
                }
                STATE_DISARMED -> {
                    setDisarmedView(configuration.alarmMode)
                }
            }
        }
    }

    interface OnControlsFragmentListener {
        fun publishArmedHome(code: String)
        fun publishArmedAway(code: String)
        fun publishArmedNight(code: String)
        fun publishDisarm(code: String)
        fun publishCustomBypass(code: String)
        fun showCodeDialog(type: CodeTypes, delay: Int?)
        fun showArmOptionsDialog()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(dialogUtils)
        // setup audio to play as loud as possible
        val am = requireContext().getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val amStreamMusicMaxVol = am.getStreamMaxVolume(AudioManager.STREAM_ALARM)
        am.setStreamVolume(AudioManager.STREAM_ALARM, amStreamMusicMaxVol, 0)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(MainViewModel::class.java)
        observeViewModel(viewModel)
        setAlarmDisabled(STATE_DISABLED)
        LocalBroadcastManager.getInstance(requireActivity()).registerReceiver(alarmBroadcastReceiver, filter)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_controls, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setOnClickListener {
            if (hasNetworkConnectivity().not()) {
                handleNetworkDisconnect()
            } else if (mqttOptions.isValid) {
                if (configuration.isAlarmDisarmedMode()) {
                    showArmOptionsDialog()
                } else if (configuration.isAlarmArmedMode()
                        || configuration.isAlarmArming()
                        || configuration.isAlarmPending()) {
                    if (mqttOptions.requireCodeForDisarming) {
                        listener?.showCodeDialog(CodeTypes.DISARM, -1)
                    } else {
                        listener?.publishDisarm("") // publish without code
                    }
                }
            } else {
                dialogUtils.showAlertDialog(requireActivity() as BaseActivity, getString(R.string.text_error_no_alarm_setup))
            }
        }
    }

    override fun onStop() {
        super.onStop()
        LocalBroadcastManager.getInstance(requireActivity()).unregisterReceiver(alarmBroadcastReceiver)
        delayTimerHandler?.removeCallbacks(delayTimerRunnable)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnControlsFragmentListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnControlsFragmentListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        stopCountdown()
        destroySoundUtils()
        listener = null
    }

    /**
     * We want to subscribe to the command topic to sync with the remote server or other devices issuing alarm commands.
     * TODO we moved this to state management
     */
    private val alarmBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (AlarmPanelService.BROADCAST_ALARM_COMMAND == intent.action) {
                val alarmMode = intent.getStringExtra(AlarmPanelService.BROADCAST_ALARM_COMMAND).orEmpty()
                if (alarmMode == COMMAND_DISARM) {
                    setDisarmingMode(alarmMode)
                } else if (alarmMode == COMMAND_ARM_AWAY ||
                        alarmMode == COMMAND_ARM_HOME ||
                        alarmMode == COMMAND_ARM_NIGHT ||
                        alarmMode == COMMAND_ARM_CUSTOM_BYPASS) {
                    setArmingMode(alarmMode)
                }
            }
        }
    }

    private fun observeViewModel(viewModel: MainViewModel) {
        disposable.add(viewModel.getSensors()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ items ->
                    items.forEach { sensor ->
                        if (sensor.notify && sensor.payloadActive == sensor.payload) {
                            playNotificationSound()
                        } else if (sensor.alarmMode && sensor.payloadActive == sensor.payload) {
                            sensorActiveMap.put(sensor.uid.toString(), sensor)
                        } else if (sensor.alarmMode && sensor.payloadActive != sensor.payload) {
                            sensorActiveMap.remove(sensor.uid.toString())
                        }
                    }
                }, { error ->
                    Timber.e("Unable to get sensors: $error")
                }))

        disposable.add(viewModel.getAlarmState()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ state ->
                    Timber.d("Alarm state: " + state)
                    Timber.d("Alarm mode: " + viewModel.getAlarmMode())
                    requireActivity().runOnUiThread {
                        // removes disarming or arming view when receiving response from server
                        //delayTimerHandler?.removeCallbacks(delayTimerRunnable)
                        val payload = state.payload
                        val delay = state.delay
                        when (payload) {
                            STATE_ARMED_AWAY,
                            STATE_ARMED_HOME,
                            STATE_ARMED_NIGHT,
                            STATE_ARMED_CUSTOM_BYPASS,
                            STATE_DISARMED -> {
                                setArmedMode(payload, delay)
                            }
                            STATE_ARMING -> {
                                if (configuration.isAlarmArming().not()) {
                                    setArmingMode(payload, delay)
                                }
                            }
                            STATE_ARM_CUSTOM_BYPASS,
                            STATE_ARM_NIGHT,
                            STATE_ARM_HOME,
                            STATE_DISARM,
                            STATE_ARM_AWAY,
                            COMMAND_ARM_CUSTOM_BYPASS,
                            COMMAND_ARM_HOME,
                            COMMAND_ARM_NIGHT,
                            COMMAND_ARM_AWAY,
                            COMMAND_DISARM -> {
                                if (configuration.isAlarmArmedMode().not()) {
                                    setArmingMode(payload, delay)
                                }
                            }
                            STATE_PENDING -> {
                                if (configuration.isAlarmArmedMode()) {
                                    setEntryMode(delay)
                                } else if (configuration.isAlarmArming()) {
                                    setArmingMode(payload, delay)
                                }
                            }
                            STATE_TRIGGERED -> {
                                //setTriggeredMode(payload)
                            }
                            else -> {
                                setDisarmedView(STATE_DISARMED)
                            }
                        }
                    }
                }, { error -> Timber.e("Unable to get message: " + error) }))
    }

    /**
     * When the transitioning to an armed mode, sometimes this mode is unknown if set remotely.
     */
    private fun setArmingMode(state: String, delay: Int? = null) {
        viewModel.setAlarmMode(state)
        val delayTime = getDelayTime(state, delay)
        alarmText.text = getString(R.string.text_arming)
        showAlarmIcons(true)
        when (state) {
            STATE_ARM_HOME,
            COMMAND_ARM_HOME -> {
                hideAlarmStates()
                startCountdown(delayTime, false)
                armHomeLayout.visibility = View.VISIBLE
                alarmImage.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_arm_home, null))
                alarmStateLayout.setBackgroundDrawable(ResourcesCompat.getDrawable(resources, R.drawable.button_round_yellow_alpha, null))
            }
            STATE_ARM_AWAY,
            COMMAND_ARM_AWAY -> {
                hideAlarmStates()
                startCountdown(delayTime, false)
                armAwayLayout.visibility = View.VISIBLE
                alarmImage.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_arm_away, null))
                alarmStateLayout.setBackgroundDrawable(ResourcesCompat.getDrawable(resources, R.drawable.button_round_red_alpha, null))
            }
            STATE_ARM_NIGHT,
            COMMAND_ARM_NIGHT -> {
                hideAlarmStates()
                startCountdown(delayTime, false)
                armNightLayout.visibility = View.VISIBLE
                alarmImage.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_arm_night, null))
                alarmStateLayout.setBackgroundDrawable(ResourcesCompat.getDrawable(resources, R.drawable.button_round_red_alpha, null))
            }
            STATE_ARM_CUSTOM_BYPASS,
            COMMAND_ARM_CUSTOM_BYPASS -> {
                hideAlarmStates()
                startCountdown(delayTime, false)
                armBypassLayout.visibility = View.VISIBLE
                alarmImage.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_arm_custom_bypass, null))
                alarmStateLayout.setBackgroundDrawable(ResourcesCompat.getDrawable(resources, R.drawable.button_round_blue_alpha, null))
            }
            else -> {
                hideAlarmStates()
                playContinuousNotification()
                alarmText.text = getString(R.string.text_arming)
                alarmImage.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_shield_armed, null))
                alarmStateLayout.setBackgroundDrawable(ResourcesCompat.getDrawable(resources, R.drawable.button_round_gray_alpha, null))
            }
        }
    }

    // TODO set delay time countdown
    private fun setArmedMode(state: String, delay: Int? = null) {
        dialogUtils.clearDialogs()
        when (state) {
            STATE_DISARMED -> {
                setDisarmedView(state)
            }
            STATE_ARMED_HOME -> {
                setArmedHomeView(state)
            }
            STATE_ARMED_AWAY -> {
                setArmedAwayView(state)
            }
            STATE_ARMED_NIGHT -> {
                setArmedNightView(state)
            }
            STATE_ARMED_CUSTOM_BYPASS -> {
                setArmedCustomBypass(state)
            }
            STATE_ARMING -> {
                if (configuration.isAlarmDisarmedMode()) {
                    playContinuousNotification()
                    setAlarmArming(state)
                }
            }
            else -> {
                setAlarmDisabled(state)
            }
        }
    }

    /**
     * We want to show a pending view when alarm is armed and entry occurs.
     */
    private fun setPendingMode(state: String, delay: Int?) {
        viewModel.setAlarmMode(state)
        startCountdown(getPendingTime(configuration.alarmMode, delay))
        when (configuration.alarmMode) {
            COMMAND_ARM_HOME -> {
                hideAlarmStates()
                armHomeLayout.visibility = View.VISIBLE
                alarmStateLayout.setBackgroundDrawable(resources.getDrawable(R.drawable.button_round_yellow_alpha))
            }
            COMMAND_ARM_AWAY -> {
                hideAlarmStates()
                armAwayLayout.visibility = View.VISIBLE
                alarmStateLayout.setBackgroundDrawable(resources.getDrawable(R.drawable.button_round_red_alpha))
            }
            COMMAND_ARM_NIGHT -> {
                hideAlarmStates()
                armNightLayout.visibility = View.VISIBLE
                alarmStateLayout.setBackgroundDrawable(resources.getDrawable(R.drawable.button_round_black_alpha))
            }
            COMMAND_ARM_CUSTOM_BYPASS,
            STATE_ARM_CUSTOM_BYPASS -> {
                hideAlarmStates()
                armBypassLayout.visibility = View.VISIBLE
                alarmStateLayout.setBackgroundDrawable(resources.getDrawable(R.drawable.button_round_blue_alpha))
            }
            STATE_PENDING -> {
                disabledLayout.visibility = View.VISIBLE
                if (configuration.isAlarmDisarmedMode()) {
                    alarmStateLayout.setBackgroundDrawable(resources.getDrawable(R.drawable.button_round_gray_alpha))
                }
            }
        }
        showAlarmIcons()
    }

    // TODO show the disarm dialog?
    private fun setEntryMode(delay: Int?) {
        alarmText.text = getString(R.string.text_alarm_entry)
        startCountdown(getPendingTime(configuration.alarmMode, delay), true)
        when (configuration.alarmMode) {
            COMMAND_ARM_HOME,
            STATE_ARMED_HOME -> {
                alarmStateLayout.setBackgroundDrawable(resources.getDrawable(R.drawable.button_round_yellow_alpha))
            }
            COMMAND_ARM_AWAY,
            STATE_ARMED_AWAY -> {
                alarmStateLayout.setBackgroundDrawable(resources.getDrawable(R.drawable.button_round_red_alpha))
            }
            COMMAND_ARM_NIGHT,
            STATE_ARMED_NIGHT -> {
                alarmStateLayout.setBackgroundDrawable(resources.getDrawable(R.drawable.button_round_black_alpha))
            }
            COMMAND_ARM_CUSTOM_BYPASS,
            STATE_ARMED_CUSTOM_BYPASS -> {
                alarmStateLayout.setBackgroundDrawable(resources.getDrawable(R.drawable.button_round_blue_alpha))
            }
            else -> {
                alarmStateLayout.setBackgroundDrawable(resources.getDrawable(R.drawable.button_round_gray_alpha))
            }
        }
    }

    private fun setDisarmingMode(state: String) {
        stopCountdown()
        viewModel.setAlarmMode(state)
        hideAlarmStates()
        alarmText.text = getString(R.string.text_disarming)
        stopCountdown()
        showAlarmIcons(true)
    }

    private fun setAlarmArming(state: String) {
        viewModel.setAlarmMode(state)
        alarmText.text = getString(R.string.text_arming)
        hideAlarmStates()
        disabledLayout.visibility = View.VISIBLE
        alarmStateLayout.setBackgroundDrawable(resources.getDrawable(R.drawable.button_round_gray_alpha))
        showAlarmIcons(true)
    }

    private fun setAlarmDisabled(state: String) {
        stopCountdown()
        viewModel.setAlarmMode(state)
        alarmText.text = getString(R.string.text_disabled)
        hideAlarmStates()
        disabledLayout.visibility = View.VISIBLE
        alarmStateLayout.setBackgroundDrawable(resources.getDrawable(R.drawable.button_round_gray_alpha))
        showAlarmIcons(false)
    }

    private fun setArmedAwayView(state: String) {
        stopCountdown()
        viewModel.setAlarmMode(state)
        alarmText.setText(R.string.text_armed_away)
        hideAlarmStates()
        armAwayLayout.visibility = View.VISIBLE
        alarmStateLayout.setBackgroundDrawable(resources.getDrawable(R.drawable.button_round_red_alpha))
        showAlarmIcons(true)
    }

    private fun setArmedCustomBypass(state: String) {
        stopCountdown()
        viewModel.setAlarmMode(state)
        alarmText.text = getString(R.string.text_armed_custom_bypass)
        hideAlarmStates()
        armBypassLayout.visibility = View.VISIBLE
        alarmStateLayout.setBackgroundDrawable(resources.getDrawable(R.drawable.button_round_blue_alpha))
        showAlarmIcons(true)
    }

    private fun setArmedHomeView(state: String) {
        stopCountdown()
        viewModel.setAlarmMode(state)
        alarmText.setText(R.string.text_armed_home)
        hideAlarmStates()
        armHomeLayout.visibility = View.VISIBLE
        alarmStateLayout.setBackgroundDrawable(resources.getDrawable(R.drawable.button_round_yellow_alpha))
        showAlarmIcons(true)
    }

    private fun setArmedNightView(state: String) {
        stopCountdown()
        viewModel.setAlarmMode(state)
        alarmText.text = getString(R.string.text_armed_night)
        hideAlarmStates()
        armNightLayout.visibility = View.VISIBLE
        alarmStateLayout.setBackgroundDrawable(resources.getDrawable(R.drawable.button_round_black_alpha))
        showAlarmIcons(true)
    }

    private fun setDisarmedView(state: String) {
        stopCountdown()
        viewModel.setAlarmMode(state)
        alarmText.setText(R.string.text_disarmed)
        alarmStateLayout.setBackgroundDrawable(resources.getDrawable(R.drawable.button_round_green_alpha))
        hideAlarmStates()
        disarmLayout.visibility = View.VISIBLE
        showAlarmIcons(false)
    }

    private fun hideAlarmStates() {
        armAwayLayout.visibility = View.INVISIBLE
        armBypassLayout.visibility = View.INVISIBLE
        armHomeLayout.visibility = View.INVISIBLE
        armNightLayout.visibility = View.INVISIBLE
        disarmLayout.visibility = View.INVISIBLE
    }

    private fun showAlarmIcons(armed: Boolean = false) {
        if (armed) {
            alarmImage.visibility = View.VISIBLE
            alarmImageUnlocked.visibility = View.INVISIBLE
        } else {
            alarmImage.visibility = View.INVISIBLE
            alarmImageUnlocked.visibility = View.VISIBLE
        }
    }

    private fun hideAlarmIcons() {
        alarmImage.visibility = View.INVISIBLE
        alarmImageUnlocked.visibility = View.INVISIBLE
    }

    private fun showArmOptionsDialog() {
        if (sensorActiveMap.size > 0) {
            val intent = Intent(AlarmPanelService.BROADCAST_SNACK_MESSAGE)
            intent.putExtra(AlarmPanelService.BROADCAST_SNACK_MESSAGE, getString(R.string.snack_check_sensors))
            val bm = LocalBroadcastManager.getInstance(requireContext().applicationContext)
            bm.sendBroadcast(intent)
        } else {
            listener?.showArmOptionsDialog()
        }
    }

    private fun getDelayTime(state: String, delay: Int?): Int {
        delay?.let {
            return it
        }
        when (state) {
            COMMAND_ARM_HOME,
            STATE_ARM_HOME -> {
                return mqttOptions.delayTimeHome
            }
            COMMAND_ARM_AWAY,
            STATE_ARM_AWAY -> {
                return mqttOptions.delayTimeAway
            }
            COMMAND_ARM_NIGHT,
            STATE_ARM_NIGHT -> {
                return mqttOptions.delayTimeNight
            }
            COMMAND_ARM_CUSTOM_BYPASS,
            STATE_ARM_CUSTOM_BYPASS -> {
                return mqttOptions.delayTimeBypass
            }
            else -> {
                return 0
            }
        }
    }

    private fun getPendingTime(state: String, delay: Int?): Int {
        delay?.let {
            return it
        }
        when (state) {
            COMMAND_ARM_HOME,
            STATE_ARM_HOME -> {
                return mqttOptions.pendingTimeHome
            }
            COMMAND_ARM_AWAY,
            STATE_ARM_AWAY -> {
                return mqttOptions.pendingTimeAway
            }
            COMMAND_ARM_NIGHT,
            STATE_ARM_NIGHT -> {
                return mqttOptions.pendingTimeNight
            }
            COMMAND_ARM_CUSTOM_BYPASS,
            STATE_ARM_CUSTOM_BYPASS -> {
                return mqttOptions.pendingTimeBypass
            }
            else -> {
                return 0
            }
        }
    }

    private fun destroySoundUtils() {
        pendingSoundFlag = false
        if (mediaPlayer != null) {
            mediaPlayer?.stop()
        }
        mediaPlayer = null
    }

    /**
     * Countdown timer time
     * @param delayTime seconds
     */
    private fun startCountdown(delayTime: Int, alarm: Boolean = false) {
        if (delayTime <= 0) {
            if (alarmListener != null) {
                alarmListener!!.onTimeOut()
                countDownTimeRemaining = 0
            }
            return
        }
        hideAlarmIcons()
        if(alarm) {
            playContinuousAlarm()
        } else {
            playContinuousNotification()
        }
        countDownProgressWheel.visibility = View.VISIBLE
        val divideBy = 360 / delayTime
        countDownTimer = object : CountDownTimer((delayTime * 1000).toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                countDownTimeRemaining = (millisUntilFinished / 1000).toInt()
                val an = RotateAnimation(0.0f, 90.0f, 250f, 273f)
                an.fillAfter = true
                countDownProgressWheel.setText(countDownTimeRemaining.toString())
                countDownProgressWheel.setWheelProgress(countDownTimeRemaining * divideBy)
            }

            override fun onFinish() {
                Timber.d("Timed up...")
                countDownTimeRemaining = 0
                destroySoundUtils()
                countDownProgressWheel.visibility = View.GONE
            }
        }.start()
    }

    private fun stopCountdown() {
        if (countDownTimer != null) {
            countDownTimer!!.cancel()
            countDownTimer = null
        }
        countDownTimeRemaining = 0
        destroySoundUtils()
        countDownProgressWheel.visibility = View.GONE
        showAlarmIcons()
    }

    private fun playContinuousNotification() {
        if (configuration.systemSounds && !pendingSoundFlag) {
            try {
                val alert: Uri? = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                if (alert == null) {
                    playContinuousBeep()
                    return
                }
                pendingSoundFlag = true
                Handler().postDelayed(Runnable { // Do something after 5s = 5000ms
                    if (pendingSoundFlag) {
                        mediaPlayer = MediaPlayer.create(requireContext(), alert)
                        mediaPlayer?.isLooping = true
                        mediaPlayer?.start()
                    }
                }, 500)
            } catch (e: SecurityException) {
                playContinuousBeep()
            } catch (e: FileNotFoundException) {
                playContinuousBeep()
            }
        }
    }

    private fun playContinuousAlarm() {
        if (configuration.systemSounds && !pendingSoundFlag) {
            try {
                val alert: Uri? = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                if (alert == null) {
                    playContinuousBeep()
                    return
                }
                pendingSoundFlag = true
                Handler().postDelayed(Runnable { // Do something after 5s = 5000ms
                    if (pendingSoundFlag) {
                        mediaPlayer = MediaPlayer.create(requireContext(), alert)
                        mediaPlayer?.isLooping = true
                        mediaPlayer?.start()
                    }
                }, 500)
            } catch (e: SecurityException) {
                playContinuousBeep()
            } catch (e: FileNotFoundException) {
                playContinuousBeep()
            }
        }
    }

    /**
     * This sound plays when sensor marked as notification is activated.
     */
    private fun playNotificationSound() {
        try {
            val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            mediaPlayer = MediaPlayer.create(requireContext(), notification)
            mediaPlayer?.isLooping = false
            mediaPlayer?.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun playContinuousBeep() {
        pendingSoundFlag = false
        mediaPlayer = MediaPlayer.create(requireContext(), R.raw.beep_loop)
        mediaPlayer?.isLooping = true
        mediaPlayer?.start()
    }

    companion object {
        fun newInstance(): ControlsFragment {
            return ControlsFragment()
        }
    }
}