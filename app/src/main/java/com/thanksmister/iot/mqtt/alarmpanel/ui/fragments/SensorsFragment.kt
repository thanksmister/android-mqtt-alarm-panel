package com.thanksmister.iot.mqtt.alarmpanel.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.thanksmister.iot.mqtt.alarmpanel.BaseFragment
import com.thanksmister.iot.mqtt.alarmpanel.R
import com.thanksmister.iot.mqtt.alarmpanel.network.MQTTOptions
import com.thanksmister.iot.mqtt.alarmpanel.persistence.Sensor
import com.thanksmister.iot.mqtt.alarmpanel.ui.adapters.SensorAdapter
import com.thanksmister.iot.mqtt.alarmpanel.ui.views.SensorDialogView
import com.thanksmister.iot.mqtt.alarmpanel.utils.DialogUtils
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils.Companion.SENSOR_GENERIC_TYPE
import com.thanksmister.iot.mqtt.alarmpanel.viewmodel.SensorsViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.LongConsumer
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_sensors.*
import timber.log.Timber
import javax.inject.Inject

class SensorsFragment : BaseFragment(), SensorAdapter.OnItemClickListener {

    @Inject
    lateinit var sensorViewModel: SensorsViewModel

    @Inject
    lateinit var dialogUtils: DialogUtils

    @Inject
    lateinit var mqttOptions: MQTTOptions

    private val sensorAdapter: SensorAdapter by lazy {
        SensorAdapter(mqttOptions.getAlarmSensorsTopic() + "/", this)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {

        super.onActivityCreated(savedInstanceState)

        observeViewModel(sensorViewModel)

        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (activity as AppCompatActivity).supportActionBar?.setDisplayShowHomeEnabled(true)
        (activity as AppCompatActivity).supportActionBar?.title = getString(R.string.pref_category_title_alarm_sensors)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val context = view.context
        sensorList.layoutManager = LinearLayoutManager(context)
        sensorList.adapter = sensorAdapter
        addSensorButton.setOnClickListener {
            showAddSensorDialog(Sensor())
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_sensors, container, false)
    }

    private fun observeViewModel(viewModel: SensorsViewModel) {
        disposable.add(viewModel.getSenors()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ items ->
                    sensorAdapter.setItems(items)
                    //sensorList.adapter = SensorAdapter(items, mqttOptions.getAlarmSensorsTopic() + "/", this)
                    //sensorList.invalidate()
                }, { error -> Timber.e("Unable to get sensors: " + error) }))
    }

    companion object {
        fun newInstance(): SensorsFragment {
            return SensorsFragment()
        }
    }

    override fun onItemClick(sensor: Sensor) {
        showAddSensorDialog(sensor)
    }

    private fun showAddSensorDialog(sensor: Sensor) {
        dialogUtils.showSensorDialog(requireContext(), sensor, mqttOptions.getAlarmSensorsTopic(), object : SensorDialogView.ViewListener {
            override fun onUpdate(sensor: Sensor) {
                verifySensorAndCommit(sensor)
                dialogUtils.clearDialogs()
            }

            override fun onRemove(sensor: Sensor) {
                sensorViewModel.deleteItem(sensor)
                dialogUtils.clearDialogs()
            }

            override fun onCancel() {
                Toast.makeText(activity, getString(R.string.toast_changes_cancelled), Toast.LENGTH_SHORT).show()
                dialogUtils.clearDialogs()
            }
        })
    }

    private fun verifySensorAndCommit(sensor: Sensor) {
        var valid = true
        when {
            sensor.topic.isNullOrEmpty() -> {
                valid = false
            }
            sensor.payloadActive.isNullOrEmpty() -> {
                valid = false
            }
            sensor.payloadInactive.isNullOrEmpty() -> {
                valid = false
            }
        }

        // if not valid show error and make no changes
        if (!valid) {
            dialogUtils.showAlertDialog(requireActivity(), getString(R.string.error_sensor_empty_data))
            return;
        }

        // if not type set to generic
        if (sensor.type.isNullOrEmpty()) {
            sensor.type = SENSOR_GENERIC_TYPE
        }

        // just set the name if its empty
        if (sensor.name.isNullOrEmpty()) {
            sensor.name = context?.getString(R.string.text_sensor)
        }

        sensorViewModel.insertItem(sensor)
    }
}