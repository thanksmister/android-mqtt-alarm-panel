/*
 * Copyright (c) 2020 ThanksMister LLC
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
import com.thanksmister.iot.mqtt.alarmpanel.persistence.Configuration
import com.thanksmister.iot.mqtt.alarmpanel.persistence.MessageDao
import com.thanksmister.iot.mqtt.alarmpanel.persistence.MessageMqtt
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import javax.inject.Inject

class SensorControlViewModel @Inject
constructor(application: Application, private val messageDataSource: MessageDao,
            private val configuration: Configuration) : AndroidViewModel(application) {

    private val disposable = CompositeDisposable()

    public override fun onCleared() {
        if (!disposable.isDisposed) {
            disposable.clear()
        }
    }

    fun getSensorStates(): Flowable<List<MessageMqtt>> {
        return messageDataSource.getMessages(MqttUtils.TYPE_SENSOR)
                .distinct()
                .filter {
                    messages -> messages.isNotEmpty()
                }
    }

    companion object {

    }
}