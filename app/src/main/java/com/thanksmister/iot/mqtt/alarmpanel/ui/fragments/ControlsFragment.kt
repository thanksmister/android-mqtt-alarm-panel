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

package com.thanksmister.iot.mqtt.alarmpanel.ui.fragments

import android.app.Dialog
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.thanksmister.iot.mqtt.alarmpanel.BaseActivity
import com.thanksmister.iot.mqtt.alarmpanel.BaseFragment
import com.thanksmister.iot.mqtt.alarmpanel.R
import com.thanksmister.iot.mqtt.alarmpanel.ui.Configuration
import com.thanksmister.iot.mqtt.alarmpanel.ui.views.AlarmDisableView
import com.thanksmister.iot.mqtt.alarmpanel.ui.views.AlarmPendingView
import com.thanksmister.iot.mqtt.alarmpanel.ui.views.ArmOptionsView
import com.thanksmister.iot.mqtt.alarmpanel.utils.AlarmUtils
import com.thanksmister.iot.mqtt.alarmpanel.utils.AlarmUtils.Companion.MODE_ARM_AWAY
import com.thanksmister.iot.mqtt.alarmpanel.utils.AlarmUtils.Companion.MODE_ARM_AWAY_PENDING
import com.thanksmister.iot.mqtt.alarmpanel.utils.AlarmUtils.Companion.MODE_ARM_HOME
import com.thanksmister.iot.mqtt.alarmpanel.utils.AlarmUtils.Companion.MODE_ARM_HOME_PENDING
import com.thanksmister.iot.mqtt.alarmpanel.utils.AlarmUtils.Companion.MODE_ARM_PENDING
import com.thanksmister.iot.mqtt.alarmpanel.utils.AlarmUtils.Companion.MODE_AWAY_TRIGGERED_PENDING
import com.thanksmister.iot.mqtt.alarmpanel.utils.AlarmUtils.Companion.MODE_DISARM
import com.thanksmister.iot.mqtt.alarmpanel.utils.AlarmUtils.Companion.MODE_HOME_TRIGGERED_PENDING
import com.thanksmister.iot.mqtt.alarmpanel.utils.AlarmUtils.Companion.MODE_TRIGGERED_PENDING
import com.thanksmister.iot.mqtt.alarmpanel.utils.DialogUtils
import com.thanksmister.iot.mqtt.alarmpanel.viewmodel.MessageViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_controls.*
import timber.log.Timber
import javax.inject.Inject

class ControlsFragment : BaseFragment() {

    @Inject lateinit var dialogUtils: DialogUtils
    @Inject lateinit var configuration: Configuration
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
        fun publishDisarmed()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(dialogUtils)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        observeViewModel(viewModel)

        // TODO this may not be needed since we pick up the state from the mqtt service
        if (viewModel.isArmed()) {
            if (viewModel.getAlarmMode() == MODE_ARM_AWAY) {
                setArmedAwayView()
            } else if (viewModel.getAlarmMode() == MODE_ARM_HOME) {
                setArmedHomeView()
            }
        } else {
            setDisarmedView()
        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is OnControlsFragmentListener) {
            mListener = context
        } else {
            throw RuntimeException(context!!.toString() + " must implement OnControlsFragmentListener")
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
            } else if (readMqttOptions().isValid) {
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

    private fun observeViewModel(viewModel: MessageViewModel) {
        disposable.add(viewModel.getAlarmState()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ state ->
                    Timber.e("Alarm state: " + state)
                    Timber.e("Alarm mode: " + viewModel.getAlarmMode())
                    activity?.runOnUiThread(java.lang.Runnable {
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
                                if (viewModel.isAlarmPendingMode()) {
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
                    })
                }, { error -> Timber.e("Unable to get message: " + error) }))
    }

    private fun setArmedAwayView() {
        alarmText.setText(R.string.text_arm_away)
        alarmText.setTextColor(resources.getColor(R.color.red))
        alarmButtonBackground.setBackgroundDrawable(resources.getDrawable(R.drawable.button_round_red))
    }

    private fun setArmedHomeView() {
        alarmText.setText(R.string.text_arm_home)
        alarmText.setTextColor(resources.getColor(R.color.yellow))
        alarmButtonBackground.setBackgroundDrawable(resources.getDrawable(R.drawable.button_round_yellow))
    }

    /**
     * We want to show a pending countdown view for the given
     * mode which can be arm home, arm away, or arm pending (from HASS).
     * @param mode PREF_ARM_HOME_PENDING, PREF_ARM_AWAY_PENDING, PREF_ARM_PENDING
     */
    private fun setPendingView(mode: String) {
        Timber.d("setPendingView: " + mode)
        viewModel.isArmed(true)
        viewModel.setAlarmMode(mode)
        if (MODE_ARM_HOME_PENDING == mode) {
            alarmText.setText(R.string.text_arm_home)
            alarmText.setTextColor(resources.getColor(R.color.yellow))
            alarmButtonBackground.setBackgroundDrawable(resources.getDrawable(R.drawable.button_round_yellow))
            showAlarmPendingView(configuration.pendingHomeTime)
        } else if (MODE_ARM_AWAY_PENDING == mode) {
            alarmText.setText(R.string.text_arm_away)
            alarmText.setTextColor(resources.getColor(R.color.red))
            alarmButtonBackground.setBackgroundDrawable(resources.getDrawable(R.drawable.button_round_red))
            showAlarmPendingView(configuration.pendingAwayTime)
        } else if (MODE_ARM_PENDING == mode) {
            alarmText.setText(R.string.text_alarm_pending)
            alarmText.setTextColor(resources.getColor(R.color.gray))
            alarmButtonBackground.setBackgroundDrawable(resources.getDrawable(R.drawable.button_round_gray))
            showAlarmPendingView(configuration.pendingTime)
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
        alarmPendingView!!.alarmListener = (object : AlarmPendingView.ViewListener {
            override fun onTimeOut() {
                hideAlarmPendingView()
            }
        })
        alarmPendingView!!.startCountDown(pendingTime)
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
                Timber.d("onCancel")
                dialogUtils.clearDialogs()
            }
        }, viewModel.getAlarmCode(), false, delayTime, configuration.systemSounds)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         */
        fun newInstance(): ControlsFragment {
            return ControlsFragment()
        }
    }
}