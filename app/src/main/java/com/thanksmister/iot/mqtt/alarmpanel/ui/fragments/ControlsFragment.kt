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

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.thanksmister.iot.mqtt.alarmpanel.BaseActivity
import com.thanksmister.iot.mqtt.alarmpanel.BaseFragment
import com.thanksmister.iot.mqtt.alarmpanel.R
import com.thanksmister.iot.mqtt.alarmpanel.network.MQTTOptions
import com.thanksmister.iot.mqtt.alarmpanel.persistence.Configuration
import com.thanksmister.iot.mqtt.alarmpanel.ui.views.ArmOptionsView
import com.thanksmister.iot.mqtt.alarmpanel.utils.DialogUtils
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils.Companion.STATE_ARMED_AWAY
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils.Companion.STATE_ARMED_HOME
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils.Companion.STATE_ARMED_NIGHT
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils.Companion.STATE_ARMING
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils.Companion.STATE_ARMING_AWAY
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils.Companion.STATE_ARMING_HOME
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils.Companion.STATE_ARMING_NIGHT
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils.Companion.STATE_DISABLED
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils.Companion.STATE_DISARMED
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils.Companion.STATE_PENDING
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils.Companion.STATE_TRIGGERED
import com.thanksmister.iot.mqtt.alarmpanel.viewmodel.MainViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_controls.*
import timber.log.Timber
import javax.inject.Inject

class ControlsFragment : BaseFragment() {

    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    lateinit var viewModel: MainViewModel

    @Inject lateinit var dialogUtils: DialogUtils
    @Inject lateinit var configuration: Configuration
    @Inject lateinit var mqttOptions: MQTTOptions

    private var mListener: OnControlsFragmentListener? = null

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    interface OnControlsFragmentListener {
        fun publishArmedHome()
        fun publishArmedAway()
        fun publishArmedNight()
        fun publishDisarm()
        fun showCodeDialogDisarm()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(dialogUtils)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(MainViewModel::class.java)
        observeViewModel(viewModel)
        setAlarmDisabled(STATE_DISABLED)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnControlsFragmentListener) {
            mListener = context
        } else {
            throw RuntimeException("$context must implement OnControlsFragmentListener")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_controls, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setOnClickListener {
            if (!hasNetworkConnectivity()) {
                handleNetworkDisconnect()
            } else if (mqttOptions.isValid) {
                if (configuration.isAlarmDisarmedMode()) {
                    showArmOptionsDialog()
                } else if (configuration.isAlarmArmedMode()
                        || configuration.isAlarmArming()
                        || configuration.isAlarmTriggered()){
                    mListener?.showCodeDialogDisarm()
                }
            } else {
                dialogUtils.showAlertDialog(requireActivity() as BaseActivity, getString(R.string.text_error_no_alarm_setup))
            }
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    private fun observeViewModel(viewModel: MainViewModel) {
        disposable.add(viewModel.getAlarmState()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ state ->
                    Timber.d("Alarm state: " + state)
                    Timber.d("Alarm mode: " + viewModel.getAlarmMode())
                    requireActivity().runOnUiThread {
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
                            STATE_PENDING ->
                                setPendingMode(state)
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
        viewModel.setAlarmMode(state)
        alarmText.text = getString(R.string.text_disabled)
        systemText.setTextColor(resources.getColor(R.color.body_text_2))
        alarmText.setTextColor(resources.getColor(R.color.gray))
        alarmStateLayout.setBackgroundDrawable(resources.getDrawable(R.drawable.button_round_gray))
        showStateView()
    }

    private fun setArmedAwayView(state: String) {
        viewModel.setAlarmMode(state)
        systemText.setTextColor(resources.getColor(R.color.body_text_2))
        alarmText.setText(R.string.text_armed_away)
        alarmText.setTextColor(resources.getColor(R.color.red))
        alarmStateLayout.setBackgroundDrawable(resources.getDrawable(R.drawable.button_round_red))
        showStateView()
    }

    private fun setArmedHomeView(state: String) {
        viewModel.setAlarmMode(state)
        systemText.setTextColor(resources.getColor(R.color.body_text_2))
        alarmText.setText(R.string.text_armed_home)
        alarmText.setTextColor(resources.getColor(R.color.yellow))
        alarmStateLayout.setBackgroundDrawable(resources.getDrawable(R.drawable.button_round_yellow))
        showStateView()
    }

    // TODO we could add the animation or delay countdown if we have those from the server
    private fun setArmingMode(state: String? = null) {
        state?.let {
            viewModel.setAlarmMode(it)
        }
        systemText.setTextColor(resources.getColor(R.color.body_text_2))
        alarmText.text = getString(R.string.text_arming)
        alarmText.setTextColor(resources.getColor(R.color.gray))
        alarmStateLayout.setBackgroundDrawable(resources.getDrawable(R.drawable.button_round_gray))
        showStateView()
    }

    private fun setArmedNightView(state: String) {
        viewModel.setAlarmMode(state)
        systemText.setTextColor(resources.getColor(R.color.body_text_2))
        alarmText.text = getString(R.string.text_armed_night)
        alarmText.setTextColor(resources.getColor(R.color.black))
        alarmStateLayout.setBackgroundDrawable(resources.getDrawable(R.drawable.button_round_black))
        showStateView()
    }

    // TODO we should show this while waiting for state change from server after disarming
    private fun setDisarmingMode(state: String? = null) {
        state?.let {
            viewModel.setAlarmMode(it)
        }
        systemText.setTextColor(resources.getColor(R.color.body_text_2))
        alarmText.text = getString(R.string.text_disarming)
        alarmText.setTextColor(resources.getColor(R.color.gray))
        alarmStateLayout.setBackgroundDrawable(resources.getDrawable(R.drawable.button_round_gray))
        showStateView()
    }

    /**
     * We want to show a pending view when alarm triggered.
     */
    private fun setPendingMode(state: String) {
        viewModel.setAlarmMode(state)
        systemText.setTextColor(resources.getColor(R.color.body_text_2))
        alarmText.text = resources.getText(R.string.text_alarm_pending)
        alarmText.setTextColor(resources.getColor(R.color.gray))
        alarmStateLayout.setBackgroundDrawable(resources.getDrawable(R.drawable.button_round_gray))
        showStateView()
    }

    private fun setTriggeredMode(state: String) {
        viewModel.setAlarmMode(state)
        alarmText.text = getString(R.string.text_triggered)
        alarmText.setTextColor(resources.getColor(R.color.white_alpha))
        systemText.setTextColor(resources.getColor(R.color.white_alpha))
        alarmStateLayout.setBackgroundDrawable(resources.getDrawable(R.drawable.button_round_white))
        (this.view as CardView).setCardBackgroundColor(resources.getColor(R.color.red))
        showTriggeredView()
    }

    private fun setDisarmedView(state: String) {
        viewModel.setAlarmMode(state)
        alarmText.setText(R.string.text_disarmed)
        alarmText.setTextColor(resources.getColor(R.color.green))
        alarmStateLayout.setBackgroundDrawable(resources.getDrawable(R.drawable.button_round_green))
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
        dialogUtils.showArmOptionsDialog(activity as BaseActivity, object : ArmOptionsView.ViewListener {
            override fun onArmHome() {
                setArmingMode()
                mListener?.publishArmedHome()
                dialogUtils.clearDialogs()
            }
            override fun onArmAway() {
                setArmingMode()
                mListener?.publishArmedAway()
                dialogUtils.clearDialogs()
            }
            override fun onArmNight() {
                setArmingMode()
                mListener?.publishArmedNight()
                dialogUtils.clearDialogs()
            }

            override fun onCloseArmOptions() {
                dialogUtils.clearDialogs()
            }
        })
    }

    companion object {
        fun newInstance(): ControlsFragment {
            return ControlsFragment()
        }
    }

    /*override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnControlsFragmentListener) {
            mListener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnControlsFragmentListener")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_controls, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        alarmPendingView = view.findViewById<AlarmPendingView>(R.id.pendingView)
        alarmView.setOnClickListener {
            if (!hasNetworkConnectivity()) {
                // we can't change the alarm state without network connection.
                handleNetworkDisconnect()
            } else if (mqttOptions.isValid) {
                if (viewModel.getAlarmMode() == MODE_DISARM) {
                    showArmOptionsDialog()
                } else {
                    // this isn't configurable, if you want to stop the alarm before it's set
                    // then we show a countdown dialog with a default time
                    showAlarmDisableDialog(configuration.disableTime)
                }
            } else {
                if (isAdded) {
                    dialogUtils.showAlertDialog(activity as BaseActivity, getString(R.string.text_error_no_alarm_setup))
                }
            }
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    private fun observeViewModel(viewModel: MainViewModel) {
        disposable.add(viewModel.getAlarmState()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ state ->
                    Timber.d("Alarm state: " + state)
                    Timber.d("Alarm mode: " + viewModel.getAlarmMode())
                    activity?.runOnUiThread {
                        when (state) {
                            AlarmUtils.STATE_ARM_AWAY -> {
                                dialogUtils.clearDialogs()
                                hideAlarmPendingView()
                                viewModel.isArmed(true)
                                viewModel.setAlarmMode(MODE_ARM_AWAY)
                                setArmedAwayView()
                            }
                            AlarmUtils.STATE_ARM_HOME -> {
                                dialogUtils.clearDialogs()
                                hideAlarmPendingView()
                                viewModel.isArmed(true)
                                viewModel.setAlarmMode(MODE_ARM_HOME)
                                setArmedHomeView()
                            }
                            AlarmUtils.STATE_DISARM -> {
                                dialogUtils.clearDialogs()
                                hideAlarmPendingView()
                                viewModel.setAlarmMode(MODE_DISARM)
                                setDisarmedView()
                            }
                            AlarmUtils.STATE_PENDING ->
                                if (configuration.isAlarmPendingMode()) {
                                    if (viewModel.getAlarmMode() == MODE_ARM_HOME_PENDING || viewModel.getAlarmMode() == MODE_HOME_TRIGGERED_PENDING) {
                                        setArmedHomeView()
                                    } else if (viewModel.getAlarmMode() == MODE_ARM_AWAY_PENDING || viewModel.getAlarmMode() == MODE_AWAY_TRIGGERED_PENDING) {
                                        setArmedAwayView()
                                    }
                                } else if (viewModel.getAlarmMode() != MODE_ARM_HOME
                                        && viewModel.getAlarmMode() != MODE_ARM_AWAY
                                        && viewModel.getAlarmMode() != MODE_TRIGGERED_PENDING) {
                                    setPendingView(MODE_ARM_PENDING)
                                }
                            AlarmUtils.STATE_ERROR -> {
                                hideAlarmPendingView()
                                setDisarmedView()
                            }
                        }
                    }
                }, { error -> Timber.e("Unable to get message: " + error) }))
    }

    private fun setArmedAwayView() {
        alarmText.setText(R.string.text_armed_away)
        alarmText.setTextColor(resources.getColor(R.color.red))
        alarmButtonBackground.setBackgroundDrawable(resources.getDrawable(R.drawable.button_round_red))
    }

    private fun setArmedHomeView() {
        alarmText.setText(R.string.text_armed_home)
        alarmText.setTextColor(resources.getColor(R.color.yellow))
        alarmButtonBackground.setBackgroundDrawable(resources.getDrawable(R.drawable.button_round_yellow))
    }

    *//**
     * We want to show a pending countdown view for the given
     * mode which can be arm home, arm away, or arm pending (from HASS).
     * @param mode PREF_ARM_HOME_PENDING, PREF_ARM_AWAY_PENDING, PREF_ARM_PENDING
     *//*
    private fun setPendingView(mode: String) {
        Timber.d("setPendingView: " + mode)
        viewModel.isArmed(true)
        viewModel.setAlarmMode(mode)
        if (MODE_ARM_HOME_PENDING == mode) {
            alarmText.setText(R.string.text_armed_home)
            alarmText.setTextColor(resources.getColor(R.color.yellow))
            alarmButtonBackground.setBackgroundDrawable(resources.getDrawable(R.drawable.button_round_yellow))
            showAlarmPendingView(configuration.pendingHomeTime)
        } else if (MODE_ARM_AWAY_PENDING == mode) {
            alarmText.setText(R.string.text_armed_away)
            alarmText.setTextColor(resources.getColor(R.color.red))
            alarmButtonBackground.setBackgroundDrawable(resources.getDrawable(R.drawable.button_round_red))
            showAlarmPendingView(configuration.pendingAwayTime)
        } else if (MODE_ARM_PENDING == mode) {
            alarmText.setText(R.string.text_alarm_pending)
            alarmText.setTextColor(resources.getColor(R.color.gray))
            alarmButtonBackground.setBackgroundDrawable(resources.getDrawable(R.drawable.button_round_gray))
            showAlarmPendingView(configuration.pendingTime)
            // TODO we need translations for this message
            Toast.makeText(activity, "The alarm was set externally, using default pending time.", Toast.LENGTH_LONG).show()
        }
    }

    private fun setDisarmedView() {
        viewModel.isArmed(false)
        viewModel.setAlarmMode(MODE_DISARM)
        alarmText.setText(R.string.text_disarmed)
        alarmText.setTextColor(resources.getColor(R.color.green))
        alarmButtonBackground.setBackgroundDrawable(resources.getDrawable(R.drawable.button_round_green))
    }

    private fun showAlarmPendingView(pendingTime : Int) {
        if (alarmPendingLayout.isShown || pendingTime == 0) {
            return
        }
        alarmPendingLayout.visibility = View.VISIBLE
        alarmPendingView?.setListener( object : AlarmPendingView.ViewListener {
            override fun onTimeOut() {
                hideAlarmPendingView()
            }
        })
        // for home we do not need a pending sound
        alarmPendingView?.setUseSound(configuration.systemSounds)
        alarmPendingView?.startCountDown(pendingTime)
    }

    private fun hideAlarmPendingView() {
        alarmPendingLayout.visibility = View.GONE
        alarmPendingView!!.stopCountDown()
    }

    private fun showArmOptionsDialog() {
        dialogUtils.showArmOptionsDialog(activity as BaseActivity, object : ArmOptionsView.ViewListener {
            override fun onArmHome() {
                mListener!!.publishArmedHome()
                setPendingView(MODE_ARM_HOME_PENDING)
                dialogUtils.clearDialogs()
            }

            override fun onArmAway() {
                mListener!!.publishArmedAway()
                setPendingView(MODE_ARM_AWAY_PENDING)
                dialogUtils.clearDialogs()
            }
        })
    }

    *//**
     * Shows a count down dialog before setting alarm to away or home mode.  This assumes
     * that you are already in the home and uses a build int delay time.
     *//*
    private fun showAlarmDisableDialog(delayTime: Int) {
        dialogUtils.showAlarmDisableDialog(activity as BaseActivity, object : AlarmDisableView.ViewListener {
            override fun onComplete(code: Int) {
                mListener!!.publishDisarmed()
                dialogUtils.clearDialogs()
            }
            override fun onError() {
                Toast.makeText(activity, R.string.toast_code_invalid, Toast.LENGTH_SHORT).show()
            }
            override fun onCancel() {
                Timber.d("onCancel")
                dialogUtils.clearDialogs()
            }
        }, viewModel.getAlarmCode(), delayTime, configuration.fingerPrint)
    }

    companion object {
        fun newInstance(): ControlsFragment {
            return ControlsFragment()
        }
    }*/
}