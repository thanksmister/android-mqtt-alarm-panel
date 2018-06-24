/*
 * Copyright (c) 2018 LocalBuzz
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
import android.arch.lifecycle.AndroidViewModel
import com.thanksmister.iot.mqtt.alarmpanel.persistence.DarkSky
import com.thanksmister.iot.mqtt.alarmpanel.persistence.DarkSkyDao
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class ScreenSaverViewModel @Inject
constructor(application: Application, private val dataSource: DarkSkyDao) : AndroidViewModel(application) {

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

    init {
    }

    public override fun onCleared() {
        //prevents memory leaks by disposing pending observable objects
        if ( !disposable.isDisposed) {
            disposable.clear()
        }
    }

    fun getItems(): Flowable<DarkSky> {
        return dataSource.getItems()
                .filter { items -> items.isNotEmpty() }
                .map { items -> items[0] }
    }

    /*
    private fun updateNotificationRead(notification: Notification) {
        Completable.fromAction {
            notification.read = true
            dataSource.insertItem(notification)
        }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                }, { error -> Timber.e("onMQTTMessage error" + error.message)})
    }*/

    companion object {

    }
}