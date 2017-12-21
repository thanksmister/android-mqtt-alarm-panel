/*
 * Copyright (c) 2017. ThanksMister LLC
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
import com.thanksmister.iot.mqtt.alarmpanel.BaseActivity
import com.thanksmister.iot.mqtt.alarmpanel.BaseFragment
import com.thanksmister.iot.mqtt.alarmpanel.R
import com.thanksmister.iot.mqtt.alarmpanel.ui.Configuration
import com.thanksmister.iot.mqtt.alarmpanel.ui.activities.LogActivity
import com.thanksmister.iot.mqtt.alarmpanel.ui.activities.MainActivity
import com.thanksmister.iot.mqtt.alarmpanel.ui.activities.SettingsActivity
import com.thanksmister.iot.mqtt.alarmpanel.ui.views.AlarmDisableView
import com.thanksmister.iot.mqtt.alarmpanel.ui.views.AlarmTriggeredView
import com.thanksmister.iot.mqtt.alarmpanel.ui.views.SettingsCodeView
import com.thanksmister.iot.mqtt.alarmpanel.utils.AlarmUtils
import com.thanksmister.iot.mqtt.alarmpanel.utils.DialogUtils
import com.thanksmister.iot.mqtt.alarmpanel.viewmodel.MessageViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_main.*
import timber.log.Timber
import javax.inject.Inject

class MainFragment : BaseFragment() {

    @Inject lateinit var configuration: Configuration;
    @Inject lateinit var dialogUtils: DialogUtils;
    private var listener: OnMainFragmentListener? = null
    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    interface OnMainFragmentListener {
        fun manuallyLaunchScreenSaver()
        fun publishDisarmed()
        fun navigatePlatformPanel()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is OnMainFragmentListener) {
            listener = context
        } else {
            throw RuntimeException(context!!.toString() + " must implement OnMainFragmentListener")
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        observeViewModel(viewModel)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        buttonSettings.setOnClickListener({showSettingsCodeDialog()})
        buttonSleep.setOnClickListener({listener?.manuallyLaunchScreenSaver()})
        buttonLogs.setOnClickListener({
            val intent = LogActivity.createStartIntent(activity!!.applicationContext)
            startActivity(intent)
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onResume() {
        super.onResume()
        if (viewModel.hasPlatform()) {
            platformButton.visibility = View.VISIBLE;
            platformButton.setOnClickListener(View.OnClickListener {listener?.navigatePlatformPanel() })
        } else {
            platformButton.visibility = View.INVISIBLE;
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
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
                            AlarmUtils.STATE_ARM_AWAY, AlarmUtils.STATE_ARM_HOME -> {
                                dialogUtils.clearDialogs()
                            }
                            AlarmUtils.STATE_DISARM -> {
                                dialogUtils.clearDialogs()
                                hideTriggeredView()
                            }
                            AlarmUtils.STATE_PENDING -> {
                                dialogUtils.clearDialogs()
                                if (viewModel.isAlarmDisableMode()) {
                                    showAlarmDisableDialog(viewModel.getAlarmDelayTime())
                                }
                            }
                            AlarmUtils.STATE_TRIGGERED -> {
                                dialogUtils.clearDialogs()
                                showAlarmTriggered()
                            }
                        }
                    })
                }, { error -> Timber.e("Unable to get message: " + error) }))
    }

    private fun showSettingsCodeDialog() {
        if (configuration.isFirstTime) {
            val intent = SettingsActivity.createStartIntent(activity!!.applicationContext)
            startActivity(intent)
        } else {
            dialogUtils.showSettingsCodeDialog(activity as MainActivity, configuration.alarmCode, object : SettingsCodeView.ViewListener {
                override fun onComplete(code: Int) {
                    if (code == configuration.alarmCode) {
                        val intent = SettingsActivity.createStartIntent(activity!!.applicationContext)
                        startActivity(intent)
                    }
                    dialogUtils.clearDialogs()
                }
                override fun onError() {
                    Toast.makeText(activity, R.string.toast_code_invalid, Toast.LENGTH_SHORT).show()
                }
                override fun onCancel() {
                    dialogUtils.clearDialogs()
                }
            })
        }
    }

    /**
     * We show the disarm dialog if we have delay time and a sensor was triggered in home or away mode.
     * This will be the time before the alarm is triggered, otherwise the expected behavior is the
     * alarm will trigger immediately.
     */
    private fun showAlarmDisableDialog(delayTime: Int) {
        if(isAdded && delayTime > 0) {
            dialogUtils.showAlarmDisableDialog(activity as BaseActivity, object : AlarmDisableView.ViewListener {
                override fun onComplete(code: Int) {
                    listener?.publishDisarmed()
                    dialogUtils.clearDialogs()
                }
                override fun onError() {
                    Toast.makeText(activity, R.string.toast_code_invalid, Toast.LENGTH_SHORT).show()
                }
                override fun onCancel() {
                    dialogUtils.clearDialogs()
                }
            }, configuration.alarmCode, true, delayTime)
        }
    }

    private fun showAlarmTriggered() {
        if (isAdded) {
            mainView!!.visibility = View.GONE
            triggeredView!!.visibility = View.VISIBLE
            val code = configuration.alarmCode
            val disarmView = activity!!.findViewById<AlarmTriggeredView>(R.id.alarmTriggeredView)
            disarmView.code = (code)
            disarmView.listener = object : AlarmTriggeredView.ViewListener {
                override fun onComplete(code: Int) {
                    listener!!.publishDisarmed()
                }
                override fun onError() {
                    Toast.makeText(activity, R.string.toast_code_invalid, Toast.LENGTH_SHORT).show()
                }
                override fun onCancel() {}
            }
        }
    }

    private fun hideTriggeredView() {
        mainView.visibility = View.VISIBLE
        triggeredView.visibility = View.GONE
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         */
        @JvmStatic fun newInstance(): MainFragment {
            return MainFragment()
        }
    }
} // Required empty public constructor