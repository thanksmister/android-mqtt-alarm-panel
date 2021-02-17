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

package com.thanksmister.iot.mqtt.alarmpanel.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.thanksmister.iot.mqtt.alarmpanel.persistence.*
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils.Companion.TYPE_SENSOR
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils.Companion.TYPE_ALARM
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import javax.inject.Inject

class TriggeredViewModel @Inject
constructor(application: Application) : AndroidViewModel(application) {

    private val disposable = CompositeDisposable()

    public override fun onCleared() {
        if (!disposable.isDisposed) {
            disposable.clear()
        }
    }
}