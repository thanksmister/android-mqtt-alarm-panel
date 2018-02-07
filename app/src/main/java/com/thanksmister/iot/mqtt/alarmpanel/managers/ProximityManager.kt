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

package com.thanksmister.iot.mqtt.alarmpanel.managers

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import timber.log.Timber

class ProximityManager(private val context: Context, private val lifecycle: Lifecycle, private val proximityEventHandler: ProximityEventHandler) : LifecycleObserver, SensorEventListener {

    private var sensorManager: SensorManager? = null
    private var proximitySensor: Sensor? = null

    init {
        sensorManager =  context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        proximitySensor = sensorManager?.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    internal fun registerSensor() {
        Timber.d("registerSensor.");
        if (sensorManager?.getDefaultSensor(Sensor.TYPE_PROXIMITY) != null) {
            // Success! There's sensor.
            sensorManager?.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_GAME);
            proximityEventHandler.deviceHasProximitySensor(true);
        } else {
            // Failure! No sensor.
            Timber.d("Device does not have a proximity sensor.");
            proximityEventHandler.deviceHasProximitySensor(false);
            unRegisterSensor()
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    internal fun unRegisterSensor() {
        if(proximitySensor != null) {
            sensorManager?.unregisterListener(this, proximitySensor)
        }
        sensorManager?.unregisterListener(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // na-da
    }

    override fun onSensorChanged(event: SensorEvent) {
        Timber.d("Sensor Data of X axis " + event.values[0].toString())
        proximityEventHandler.onProximitySensorValueChanged(event.values[0].toString())
    }

    interface ProximityEventHandler {
        fun onProximitySensorValueChanged(value: String)
        fun deviceHasProximitySensor(hasSensor: Boolean)
    }

    companion object {
       //var hasNetwork = AtomicBoolean(true)
    }
}