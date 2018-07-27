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
import android.arch.lifecycle.AndroidViewModel
import com.thanksmister.iot.mqtt.alarmpanel.persistence.Sensor
import com.thanksmister.iot.mqtt.alarmpanel.persistence.SensorDao
import com.thanksmister.iot.mqtt.alarmpanel.persistence.Configuration
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

class SensorViewModel @Inject
constructor(application: Application, private val dataSource: SensorDao, private val configuration: Configuration) : AndroidViewModel(application) {

    private val disposable = CompositeDisposable()

    init {
    }

    /**
     * Get the items.
     * @return a [Flowable]
     */
    fun getItems(): Flowable<List<Sensor>> {
        return dataSource.getItems()
                .filter { items -> items.isNotEmpty() }
    }

    /**
     * Insert new item into the database.
     */
    fun insertItem(sensor: Sensor) {
        Timber.d("insertItem")
        disposable.add(Completable.fromAction {
            dataSource.insertItem(sensor)
        }
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                }, { error -> Timber.e("Database error" + error.message) }))

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