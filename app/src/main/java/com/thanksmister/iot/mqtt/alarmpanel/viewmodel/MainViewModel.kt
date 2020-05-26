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
import android.text.TextUtils
import com.thanksmister.iot.mqtt.alarmpanel.network.MQTTOptions
import com.thanksmister.iot.mqtt.alarmpanel.persistence.*
import com.thanksmister.iot.mqtt.alarmpanel.persistence.Configuration
import com.thanksmister.iot.mqtt.alarmpanel.utils.AlarmUtils
import com.thanksmister.iot.mqtt.alarmpanel.utils.AlarmUtils.Companion.ALARM_TYPE

import com.thanksmister.iot.mqtt.alarmpanel.utils.DateUtils
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

class MainViewModel @Inject
constructor(application: Application, private val messageDataSource: MessageDao, private val sunSource: SunDao, private val configuration: Configuration,
            private val mqttOptions: MQTTOptions) : AndroidViewModel(application) {

    private val disposable = CompositeDisposable()
    private val toastText = ToastMessage()
    private val alertText = AlertMessage()
    private val snackbarText = SnackbarMessage()

    fun getToastMessage(): ToastMessage {
        return toastText
    }

    fun getAlertMessage(): AlertMessage {
        return alertText
    }

    fun getSnackBarMessage(): SnackbarMessage {
        return snackbarText
    }

    private fun showSnackbarMessage(message: Int) {
        snackbarText.value = message
    }

    private fun showAlertMessage(message: String) {
        alertText.value = message
    }

    private fun showToastMessage(message: String) {
        toastText.value = message
    }

    @AlarmUtils.AlarmStates
    private fun setAlarmModeFromState(state: String?) {
        state?.let {
            setAlarmMode(state)
        }
    }

    fun hasPlatform() : Boolean {
        Timber.d("Has Plaform Module: " + configuration.hasPlatformModule())
        Timber.d("Has Plaform Url: " + configuration.webUrl)
        return (configuration.hasPlatformModule() && !TextUtils.isEmpty(configuration.webUrl))
    }

    fun setAlarmMode(value: String) {
        configuration.alarmMode = value;
    }

    fun getAlarmDelayTime(): Int {
        return when(getAlarmMode()) {
            AlarmUtils.STATE_ARMING -> {
                configuration.delayAwayTime
            }
            else -> configuration.delayTime
        }
    }

    fun getAlarmCode(): Int {
        return configuration.alarmCode
    }

    fun getAlarmMode(): String {
        return configuration.alarmMode
    }

    /**
     * Get the messages.
     * @return a [Flowable] that will emit every time the messages have been updated.
     */
    fun getMessages():Flowable<List<MessageMqtt>> {
        return messageDataSource.getMessages()
                .filter {messages -> messages.isNotEmpty()}
    }

    fun getAlarmState():Flowable<String> {
        return messageDataSource.getMessages(ALARM_TYPE)
                .filter {messages -> messages.isNotEmpty()}
                .map {messages -> messages[messages.size - 1]}
                .map {message ->
                    Timber.d("state: " + message.payload)
                    setAlarmModeFromState(message.payload)
                    message.payload
                }
    }

    fun getSun(): Flowable<Sun> {
        return sunSource.getItems()
                .filter {items -> items.isNotEmpty()}
                .map { items -> items[items.size - 1] }
    }

    /**
     * Insert new message into the database.
     */
    @Deprecated("Moved to service")
    fun insertMessage(messageId: String,topic: String, payload: String) {
        Timber.d("insertMessage: " + topic)
        Timber.d("insertMessage: " + payload)
        val type = ALARM_TYPE
        disposable.add(Completable.fromAction {
            val createdAt = DateUtils.generateCreatedAtDate()
            val message = MessageMqtt()
            message.type = type
            message.topic = topic
            message.payload = payload
            message.messageId = messageId
            message.createdAt = createdAt
            messageDataSource.insertMessage(message)
        } .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                }, { error -> Timber.e("Database error" + error.message) }))
    }

    fun clearMessages():Completable {
        return Completable.fromAction {
            messageDataSource.deleteAllMessages()
        }
    }

    public override fun onCleared() {
        //prevents memory leaks by disposing pending observable objects
        if (!disposable.isDisposed) {
            disposable.clear()
        }
    }

    companion object {

    }
}