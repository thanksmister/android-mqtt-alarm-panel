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
import android.graphics.Color
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.thanksmister.iot.mqtt.alarmpanel.BaseActivity
import com.thanksmister.iot.mqtt.alarmpanel.BaseFragment
import com.thanksmister.iot.mqtt.alarmpanel.R
import com.thanksmister.iot.mqtt.alarmpanel.constants.CodeTypes
import com.thanksmister.iot.mqtt.alarmpanel.network.AlarmPanelService
import com.thanksmister.iot.mqtt.alarmpanel.network.MQTTOptions
import com.thanksmister.iot.mqtt.alarmpanel.persistence.Configuration
import com.thanksmister.iot.mqtt.alarmpanel.utils.DialogUtils
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils.Companion.COMMAND_ARM_AWAY
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils.Companion.COMMAND_ARM_HOME
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils.Companion.COMMAND_ARM_NIGHT
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils.Companion.COMMAND_DISARM
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils.Companion.STATE_ARMED_AWAY
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils.Companion.STATE_ARMED_HOME
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils.Companion.STATE_ARMED_NIGHT
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils.Companion.STATE_ARMING
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils.Companion.STATE_ARMING_AWAY
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils.Companion.STATE_ARMING_HOME
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils.Companion.STATE_ARMING_NIGHT
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils.Companion.STATE_ARM_AWAY
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils.Companion.STATE_ARM_HOME
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils.Companion.STATE_ARM_NIGHT
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils.Companion.STATE_DISABLED
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils.Companion.STATE_DISARMED
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils.Companion.STATE_DISARMING
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

    var filter = IntentFilter(AlarmPanelService.BROADCAST_EVENT_ALARM_MODE)

    private var delayTimerHandler: Handler? = null

    private var mediaPlayer: MediaPlayer? = null

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
                STATE_DISARMED -> {
                    setDisarmedView(configuration.alarmMode)
                }
            }
        }
    }

    interface OnControlsFragmentListener {
        fun publishArmedHome()
        fun publishArmedAway()
        fun publishArmedNight()
        fun publishDisarm(code: String)
        fun showCodeDialog(type: CodeTypes)
        fun showArmOptionsDialog()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(dialogUtils)
        // setup audio
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
            val alarmMode = configuration.alarmMode
            if (!hasNetworkConnectivity()) {
                handleNetworkDisconnect()
            } else if (mqttOptions.isValid) {
                if (configuration.isAlarmDisarmedMode()) {
                    showArmOptionsDialog()
                } else if (configuration.isAlarmArmedMode()
                        || configuration.isAlarmArming()
                        || configuration.isAlarmTriggered()
                        || configuration.isPendingMode()) {
                    listener?.showCodeDialog(CodeTypes.DISARM)
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
        stopContinuousAlarm()
        listener = null
    }

    /**
     * We want to listen for a disarming mode due to delays in communication we want to show action
     * in the application for disarming, so users know we are waiting.
     */
    private val alarmBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (AlarmPanelService.BROADCAST_EVENT_ALARM_MODE == intent.action) {
                val alarmMode = intent.getStringExtra(AlarmPanelService.BROADCAST_EVENT_ALARM_MODE).orEmpty()
                if (alarmMode == COMMAND_DISARM) {
                    delayTimerHandler?.removeCallbacks(delayTimerRunnable)
                    setDisarmingMode(alarmMode)
                } else if (alarmMode == COMMAND_ARM_AWAY || alarmMode == COMMAND_ARM_HOME || alarmMode == COMMAND_ARM_NIGHT)  {
                    delayTimerHandler?.removeCallbacks(delayTimerRunnable)
                    setArmingMode(alarmMode)
                }
            }
        }
    }

    private fun observeViewModel(viewModel: MainViewModel) {
        disposable.add(viewModel.getAlarmState()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ state ->
                    Timber.d("Alarm state: " + state)
                    Timber.d("Alarm mode: " + viewModel.getAlarmMode())
                    requireActivity().runOnUiThread {
                        // removes disarming or arming view when receiving response from server
                        delayTimerHandler?.removeCallbacks(delayTimerRunnable)
                        when (state) {
                            STATE_ARMED_AWAY -> {
                                dialogUtils.clearDialogs()
                                setArmedAwayView(state)
                            }
                            STATE_ARMED_HOME -> {
                                dialogUtils.clearDialogs()
                                setArmedHomeView(state)
                            }
                            STATE_ARMED_NIGHT -> {
                                dialogUtils.clearDialogs()
                                setArmedNightView(state)
                            }
                            STATE_DISARMED -> {
                                dialogUtils.clearDialogs()
                                setDisarmedView(state)
                            }
                            STATE_ARMING,
                            STATE_ARMING_HOME,
                            STATE_ARMING_AWAY,
                            STATE_ARMING_NIGHT -> {
                                setArmingMode(state)
                            }
                            STATE_PENDING -> {
                                if (configuration.isAlarmArmedMode()) {
                                    setEntryMode(MqttUtils.STATE_PENDING_ALARM)
                                } else {
                                    setPendingMode(state)
                                }
                            }
                            STATE_DISARMING -> {
                                setDisarmingMode(state)
                            }
                            STATE_TRIGGERED ->
                                setTriggeredMode(state)
                            else -> {
                                setDisarmedView(STATE_DISARMED)
                            }
                        }
                    }
                }, { error -> Timber.e("Unable to get message: " + error) }))
    }

    private fun setAlarmDisabled(state: String) {
        stopContinuousAlarm()
        viewModel.setAlarmMode(state)
        alarmText.text = getString(R.string.text_disabled)
        alarmImage.visibility = View.VISIBLE
        alarmImageUnlocked.visibility = View.INVISIBLE
        pendingAnimation.visibility = View.GONE
        systemText.setTextColor(resources.getColor(R.color.body_text_2))
        alarmText.setTextColor(resources.getColor(R.color.gray))
        alarmStateLayout.setBackgroundDrawable(resources.getDrawable(R.drawable.button_round_gray))
        (this.view as CardView).setCardBackgroundColor(resources.getColor(R.color.colorPrimary))
        showStateView()
    }

    private fun setArmedAwayView(state: String) {
        stopContinuousAlarm()
        viewModel.setAlarmMode(state)
        systemText.setTextColor(resources.getColor(R.color.body_text_2))
        alarmImage.visibility = View.VISIBLE
        alarmImageUnlocked.visibility = View.INVISIBLE
        pendingAnimation.visibility = View.GONE
        alarmText.setText(R.string.text_armed_away)
        alarmText.setTextColor(resources.getColor(R.color.red))
        alarmStateLayout.setBackgroundDrawable(resources.getDrawable(R.drawable.button_round_red))
        (this.view as CardView).setCardBackgroundColor(resources.getColor(R.color.colorPrimary))
        showStateView()
    }

    private fun setArmedHomeView(state: String) {
        stopContinuousAlarm()
        viewModel.setAlarmMode(state)
        systemText.setTextColor(resources.getColor(R.color.body_text_2))
        alarmImage.visibility = View.VISIBLE
        alarmImageUnlocked.visibility = View.INVISIBLE
        pendingAnimation.visibility = View.GONE
        alarmText.setText(R.string.text_armed_home)
        alarmText.setTextColor(resources.getColor(R.color.yellow))
        alarmStateLayout.setBackgroundDrawable(resources.getDrawable(R.drawable.button_round_yellow))
        (this.view as CardView).setCardBackgroundColor(resources.getColor(R.color.colorPrimary))
        showStateView()
    }

    private fun setArmedNightView(state: String) {
        stopContinuousAlarm()
        viewModel.setAlarmMode(state)
        systemText.setTextColor(resources.getColor(R.color.body_text_2))
        alarmImage.visibility = View.VISIBLE
        alarmImageUnlocked.visibility = View.INVISIBLE
        pendingAnimation.visibility = View.GONE
        alarmText.text = getString(R.string.text_armed_night)
        alarmText.setTextColor(resources.getColor(R.color.black))
        alarmStateLayout.setBackgroundDrawable(resources.getDrawable(R.drawable.button_round_black))
        (this.view as CardView).setCardBackgroundColor(resources.getColor(R.color.colorPrimary))
        showStateView()
    }

    private fun setArmingMode(state: String) {
        stopContinuousAlarm()
        viewModel.setAlarmMode(state)
        pendingAnimation.visibility = View.VISIBLE
        systemText.setTextColor(resources.getColor(R.color.body_text_2))
        alarmImage.visibility = View.VISIBLE
        alarmImageUnlocked.visibility = View.INVISIBLE
        when(state) {
            COMMAND_ARM_HOME,
            STATE_ARM_HOME,
            STATE_ARMING_HOME -> {
                pendingAnimation.setColor(Color.YELLOW)
                if(mqttOptions.useRemoteConfig) {
                    delayTimerHandler?.postDelayed(delayTimerRunnable, (mqttOptions.remoteArmingHomeTime*10000+ 3000).toLong())
                }
                alarmStateLayout.setBackgroundDrawable(resources.getDrawable(R.drawable.button_round_yellow_alpha))
                alarmText.text = getString(R.string.text_arming)
            }
            COMMAND_ARM_AWAY,
            STATE_ARM_AWAY,
            STATE_ARMING_AWAY -> {
                pendingAnimation.setColor(Color.RED)
                if(mqttOptions.useRemoteConfig) {
                    delayTimerHandler?.postDelayed(delayTimerRunnable, (mqttOptions.remoteArmingAwayTime*10000 + 3000).toLong())
                }
                playContinuousBeep()
                alarmStateLayout.setBackgroundDrawable(resources.getDrawable(R.drawable.button_round_red_alpha))
                alarmText.text = getString(R.string.text_arming)
            }
            COMMAND_ARM_NIGHT,
            STATE_ARM_NIGHT,
            STATE_ARMING_NIGHT -> {
                pendingAnimation.setColor(Color.BLACK)
                if(mqttOptions.useRemoteConfig) {
                    delayTimerHandler?.postDelayed(delayTimerRunnable, (mqttOptions.remoteArmingNightTime*10000).toLong()+ 3000)
                }
                alarmStateLayout.setBackgroundDrawable(resources.getDrawable(R.drawable.button_round_black_alpha))
                alarmText.text = getString(R.string.text_arming)
            } else -> {
                pendingAnimation.setColor(Color.WHITE)
                alarmStateLayout.setBackgroundDrawable(resources.getDrawable(R.drawable.button_round_gray))
                alarmText.text = getString(R.string.text_arming)
            }
        }

        alarmText.setTextColor(resources.getColor(R.color.gray))
        (this.view as CardView).setCardBackgroundColor(resources.getColor(R.color.colorPrimary))
        showStateView()
    }

    private fun setDisarmingMode(state: String) {
        systemText.setTextColor(resources.getColor(R.color.body_text_2))
        alarmImage.visibility = View.INVISIBLE
        alarmImageUnlocked.visibility = View.VISIBLE
        pendingAnimation.visibility = View.VISIBLE
        alarmText.text = getString(R.string.text_disarming)
        alarmText.setTextColor(resources.getColor(R.color.gray))
        alarmStateLayout.setBackgroundDrawable(resources.getDrawable(R.drawable.button_round_gray))
        (this.view as CardView).setCardBackgroundColor(resources.getColor(R.color.colorPrimary))
        val previousState = configuration.alarmMode
        when(previousState) {
            COMMAND_ARM_HOME,
            STATE_ARMED_HOME,
            STATE_ARMING_HOME -> {
                pendingAnimation.setColor(Color.YELLOW)
                alarmStateLayout.setBackgroundDrawable(resources.getDrawable(R.drawable.button_round_yellow_alpha))
            }
            COMMAND_ARM_AWAY,
            STATE_ARMED_AWAY,
            STATE_ARMING_AWAY -> {
                pendingAnimation.setColor(Color.RED)
                alarmStateLayout.setBackgroundDrawable(resources.getDrawable(R.drawable.button_round_red_alpha))
            }
            COMMAND_ARM_NIGHT,
            STATE_ARMED_NIGHT,
            STATE_ARMING_NIGHT -> {
                pendingAnimation.setColor(Color.BLACK)
                alarmStateLayout.setBackgroundDrawable(resources.getDrawable(R.drawable.button_round_black_alpha))
            } else -> {
                pendingAnimation.setColor(Color.WHITE)
                alarmStateLayout.setBackgroundDrawable(resources.getDrawable(R.drawable.button_round_gray))
            }
        }
        viewModel.setAlarmMode(state)
        showStateView()
        delayTimerHandler?.postDelayed(delayTimerRunnable, 10000)
    }

    /**
     * We want to show a pending view when alarm triggered.
     */
    private fun setPendingMode(state: String) {
        stopContinuousAlarm()
        playContinuousBeep()
        systemText.setTextColor(resources.getColor(R.color.body_text_2))
        pendingAnimation.visibility = View.VISIBLE
        alarmText.setTextColor(resources.getColor(R.color.gray))
        val previousState = configuration.alarmMode
        when(previousState) {
            COMMAND_ARM_HOME,
            STATE_ARMING_HOME -> {
                pendingAnimation.setColor(Color.YELLOW)
                alarmStateLayout.setBackgroundDrawable(resources.getDrawable(R.drawable.button_round_yellow_alpha))
                alarmText.text = getString(R.string.text_arming)
            }
            COMMAND_ARM_AWAY,
            STATE_ARMING_AWAY -> {
                pendingAnimation.setColor(Color.RED)
                alarmStateLayout.setBackgroundDrawable(resources.getDrawable(R.drawable.button_round_red_alpha))
                alarmText.text = getString(R.string.text_arming)
            }
            COMMAND_ARM_NIGHT,
            STATE_ARMING_NIGHT -> {
                pendingAnimation.setColor(Color.BLACK)
                alarmStateLayout.setBackgroundDrawable(resources.getDrawable(R.drawable.button_round_black_alpha))
                alarmText.text = getString(R.string.text_arming)
            } else -> {
                pendingAnimation.setColor(Color.WHITE)
                alarmStateLayout.setBackgroundDrawable(resources.getDrawable(R.drawable.button_round_gray))
                alarmText.text = resources.getText(R.string.text_alarm_pending)
            }
        }
        (this.view as CardView).setCardBackgroundColor(resources.getColor(R.color.colorPrimary))
        viewModel.setAlarmMode(state)
        showStateView()
    }

    private fun setEntryMode(state: String) {
        playContinuousAlarm()
        systemText.setTextColor(resources.getColor(R.color.body_text_2))
        pendingAnimation.visibility = View.VISIBLE
        alarmText.text = "ALARM ENTRY"
        alarmText.setTextColor(resources.getColor(R.color.gray))
        alarmStateLayout.setBackgroundDrawable(resources.getDrawable(R.drawable.button_round_gray))
        (this.view as CardView).setCardBackgroundColor(resources.getColor(R.color.colorPrimary))
        val previousState = configuration.alarmMode
        when(previousState) {
            COMMAND_ARM_HOME,
            STATE_ARMED_HOME,
            STATE_ARMING_HOME -> {
                pendingAnimation.setColor(Color.YELLOW)
                alarmStateLayout.setBackgroundDrawable(resources.getDrawable(R.drawable.button_round_yellow_alpha))
            }
            COMMAND_ARM_AWAY,
            STATE_ARMED_AWAY,
            STATE_ARMING_AWAY -> {
                pendingAnimation.setColor(Color.RED)
                alarmStateLayout.setBackgroundDrawable(resources.getDrawable(R.drawable.button_round_red_alpha))
            }
            COMMAND_ARM_NIGHT,
            STATE_ARMED_NIGHT,
            STATE_ARMING_NIGHT -> {
                pendingAnimation.setColor(Color.BLACK)
                alarmStateLayout.setBackgroundDrawable(resources.getDrawable(R.drawable.button_round_black_alpha))
            } else -> {
                pendingAnimation.setColor(Color.WHITE)
                alarmStateLayout.setBackgroundDrawable(resources.getDrawable(R.drawable.button_round_gray))
            }
        }
        viewModel.setAlarmMode(state)
        showStateView()
    }

    private fun setTriggeredMode(state: String) {
        stopContinuousAlarm()
        viewModel.setAlarmMode(state)
        alarmText.text = getString(R.string.text_triggered)
        alarmText.setTextColor(resources.getColor(R.color.white_alpha))
        systemText.setTextColor(resources.getColor(R.color.white_alpha))
        alarmStateLayout.setBackgroundDrawable(resources.getDrawable(R.drawable.button_round_white))
        (this.view as CardView).setCardBackgroundColor(resources.getColor(R.color.red))
        showTriggeredView()
    }

    private fun setDisarmedView(state: String) {
        stopContinuousAlarm()
        viewModel.setAlarmMode(state)
        alarmText.setText(R.string.text_disarmed)
        alarmText.setTextColor(resources.getColor(R.color.green))
        alarmImage.visibility = View.INVISIBLE
        alarmImageUnlocked.visibility = View.VISIBLE
        pendingAnimation.visibility = View.GONE
        alarmStateLayout.setBackgroundDrawable(resources.getDrawable(R.drawable.button_round_green))
        (this.view as CardView).setCardBackgroundColor(resources.getColor(R.color.colorPrimary))
        showStateView()
    }

    private fun showTriggeredView() {
        alarmTriggeredLayout.visibility = View.VISIBLE
        alarmStateLayout.visibility = View.INVISIBLE
    }

    private fun showStateView() {
        alarmTriggeredLayout.visibility = View.INVISIBLE
        alarmStateLayout.visibility = View.VISIBLE
    }

    private fun showArmOptionsDialog() {
        listener?.showArmOptionsDialog()
    }

    private fun stopContinuousAlarm() {
        mediaPlayer?.stop()
    }

    private fun playContinuousAlarm() {
        if (configuration.systemSounds) {
            try {
                var alert: Uri? = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                if (alert == null) {
                    alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    if (alert == null) {
                        alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                    }
                }
                mediaPlayer = MediaPlayer.create(context!!, alert)
                mediaPlayer?.isLooping = true
                mediaPlayer?.start()
            } catch (e: SecurityException) {
                playContinuousBeep()
            } catch (e: FileNotFoundException) {
                playContinuousBeep()
            }
        } else {
            playContinuousBeep()
        }
    }

    private fun playContinuousBeep() {
        Timber.d("playContinuousBeep")
        mediaPlayer = MediaPlayer.create(context!!, R.raw.beep_loop)
        mediaPlayer?.isLooping = true
        mediaPlayer?.start()
    }

    companion object {
        fun newInstance(): ControlsFragment {
            return ControlsFragment()
        }
    }
}