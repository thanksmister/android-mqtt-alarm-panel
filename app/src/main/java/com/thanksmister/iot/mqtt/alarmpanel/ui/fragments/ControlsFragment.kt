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
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.thanksmister.iot.mqtt.alarmpanel.BaseActivity
import com.thanksmister.iot.mqtt.alarmpanel.BaseFragment
import com.thanksmister.iot.mqtt.alarmpanel.R
import com.thanksmister.iot.mqtt.alarmpanel.network.MQTTOptions
import com.thanksmister.iot.mqtt.alarmpanel.persistence.Configuration
import com.thanksmister.iot.mqtt.alarmpanel.ui.views.AlarmDisableView
import com.thanksmister.iot.mqtt.alarmpanel.ui.views.AlarmPendingView
import com.thanksmister.iot.mqtt.alarmpanel.ui.views.ArmOptionsView
import com.thanksmister.iot.mqtt.alarmpanel.utils.AlarmUtils.Companion.STATE_ARMED_AWAY
import com.thanksmister.iot.mqtt.alarmpanel.utils.AlarmUtils.Companion.STATE_ARMED_HOME
import com.thanksmister.iot.mqtt.alarmpanel.utils.AlarmUtils.Companion.STATE_ARMED_NIGHT
import com.thanksmister.iot.mqtt.alarmpanel.utils.AlarmUtils.Companion.STATE_ARMING
import com.thanksmister.iot.mqtt.alarmpanel.utils.AlarmUtils.Companion.STATE_DISABLED
import com.thanksmister.iot.mqtt.alarmpanel.utils.AlarmUtils.Companion.STATE_DISARMED
import com.thanksmister.iot.mqtt.alarmpanel.utils.AlarmUtils.Companion.STATE_DISARMING
import com.thanksmister.iot.mqtt.alarmpanel.utils.AlarmUtils.Companion.STATE_PENDING
import com.thanksmister.iot.mqtt.alarmpanel.utils.AlarmUtils.Companion.STATE_TRIGGERED
import com.thanksmister.iot.mqtt.alarmpanel.utils.DialogUtils
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
    private var alarmPendingView: AlarmPendingView? = null
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
        fun publishDisarmed()
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
                handleNetworkDisconnect()
            } else if (mqttOptions.isValid) {
                if (configuration.isAlarmDisarmedMode()) {
                    showArmOptionsDialog()
                } else if (configuration.isAlarmArmedMode()){
                    showAlarmDisableDialog(configuration.disableTime)
                }
            } else {
                if (isAdded) {
                    dialogUtils.showAlertDialog(requireActivity() as BaseActivity, getString(R.string.text_error_no_alarm_setup))
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
                            STATE_ARMING -> {
                                setArmingMode(state)
                            }
                            STATE_DISARMING -> {
                                setDisarmingMode(state)
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
        alarmText.text = "DISABLED"
        alarmText.setTextColor(resources.getColor(R.color.gray))
        alarmButtonBackground.setBackgroundDrawable(resources.getDrawable(R.drawable.button_round_gray))
    }

    private fun setArmedAwayView(state: String) {
        viewModel.setAlarmMode(state)
        alarmText.setText(R.string.text_armed_away)
        alarmText.setTextColor(resources.getColor(R.color.red))
        alarmButtonBackground.setBackgroundDrawable(resources.getDrawable(R.drawable.button_round_red))
        hideAlarmPendingView()
    }

    private fun setArmedHomeView(state: String) {
        viewModel.setAlarmMode(state)
        alarmText.setText(R.string.text_armed_home)
        alarmText.setTextColor(resources.getColor(R.color.yellow))
        alarmButtonBackground.setBackgroundDrawable(resources.getDrawable(R.drawable.button_round_yellow))
        hideAlarmPendingView()
    }

    private fun setArmingMode(state: String? = null) {
        state?.let {
            viewModel.setAlarmMode(it)
        }
        alarmText.text = "ARMING"
        alarmText.setTextColor(resources.getColor(R.color.gray))
        alarmButtonBackground.setBackgroundDrawable(resources.getDrawable(R.drawable.button_round_gray))
        showArmingView(configuration.pendingTime)
    }

    private fun setArmedNightView(state: String) {
        viewModel.setAlarmMode(state)
        alarmText.text = "ARMED NIGHT"
        alarmText.setTextColor(resources.getColor(R.color.yellow))
        alarmButtonBackground.setBackgroundDrawable(resources.getDrawable(R.drawable.button_round_yellow))
        hideAlarmPendingView()
    }

    private fun setDisarmingMode(state: String? = null) {
        state?.let {
            viewModel.setAlarmMode(it)
        }
        alarmText.text = "DISARMING"
        alarmText.setTextColor(resources.getColor(R.color.gray))
        alarmButtonBackground.setBackgroundDrawable(resources.getDrawable(R.drawable.button_round_gray))
        showDisarmingView()
    }

    /**
     * We want to show a pending view when alarm triggered.
     */
    private fun setPendingMode(state: String) {
        viewModel.setAlarmMode(state)
        alarmText.text = "TRIGGERED"
        alarmText.setTextColor(resources.getColor(R.color.gray))
        alarmButtonBackground.setBackgroundDrawable(resources.getDrawable(R.drawable.button_round_gray))
        showArmingView(configuration.pendingTime)
    }

    private fun setTriggeredMode(state: String) {
        viewModel.setAlarmMode(state)
        alarmText.text = "TRIGGERED"
        alarmText.setTextColor(resources.getColor(R.color.gray))
        alarmButtonBackground.setBackgroundDrawable(resources.getDrawable(R.drawable.button_round_gray))
        showTriggeredView()
    }

    private fun setDisarmedView(state: String) {
        viewModel.setAlarmMode(state)
        alarmText.setText(R.string.text_disarmed)
        alarmText.setTextColor(resources.getColor(R.color.green))
        alarmButtonBackground.setBackgroundDrawable(resources.getDrawable(R.drawable.button_round_green))
        hideAlarmPendingView()
    }

    private fun showTriggeredView() {
        if (alarmPendingLayout.isShown) {
            return
        }
        alarmPendingLayout.visibility = View.VISIBLE
        alarmPendingView?.visibility = View.INVISIBLE
    }

    private fun showDisarmingView() {
        if (alarmPendingLayout.isShown) {
            return
        }
        alarmPendingLayout.visibility = View.VISIBLE
        alarmPendingView?.visibility = View.INVISIBLE
    }

    private fun showArmingView(pendingTime : Int) {
        if (alarmPendingLayout.isShown || pendingTime == 0) {
            return
        }
        alarmPendingLayout.visibility = View.VISIBLE
        alarmPendingView?.visibility = View.VISIBLE
        alarmPendingView?.alarmListener = (object : AlarmPendingView.ViewListener {
            override fun onTimeOut() {
                hideAlarmPendingView()
            }
        })
        alarmPendingView?.startCountDown(pendingTime)
    }

    private fun hideAlarmPendingView() {
        alarmPendingLayout.visibility = View.GONE
        alarmPendingView?.stopCountDown()
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
        })
    }

    /**
     * Shows a count down dialog before setting alarm to away or home mode.  This assumes
     * that you are already in the home and uses a build int delay time.
     */
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
                dialogUtils.clearDialogs()
            }
        }, viewModel.getAlarmCode(), delayTime, configuration.fingerPrint)
    }

    companion object {
        fun newInstance(): ControlsFragment {
            return ControlsFragment()
        }
    }
}