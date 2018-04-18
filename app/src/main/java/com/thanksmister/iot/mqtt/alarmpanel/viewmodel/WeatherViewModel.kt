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
import android.os.Handler
import android.text.TextUtils
import com.google.gson.Gson
import com.thanksmister.iot.mqtt.alarmpanel.BaseApplication
import com.thanksmister.iot.mqtt.alarmpanel.network.DarkSkyApi
import com.thanksmister.iot.mqtt.alarmpanel.network.fetchers.DarkSkyFetcher
import com.thanksmister.iot.mqtt.alarmpanel.network.model.DarkSkyResponse
import com.thanksmister.iot.mqtt.alarmpanel.persistence.DarkSky
import com.thanksmister.iot.mqtt.alarmpanel.persistence.DarkSkyDao
import com.thanksmister.iot.mqtt.alarmpanel.ui.Configuration
import com.thanksmister.iot.mqtt.alarmpanel.utils.DateUtils
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.lang.Math.round
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import android.os.Looper



class WeatherViewModel @Inject
constructor(application: Application, private val dataSource: DarkSkyDao, private val configuration: Configuration) : AndroidViewModel(application) {

    private val toastText = ToastMessage()
    private val alertText = AlertMessage()
    private val disposable = CompositeDisposable()

    fun hasWeatherModule() : Boolean {
        return (configuration.showWeatherModule())
    }

    fun getToastMessage(): ToastMessage {
        return toastText
    }

    fun getAlertMessage(): AlertMessage {
        return alertText
    }

    init {
    }

    private fun showAlertMessage(message: String?) {
        Timber.d("showAlertMessage")
        alertText.value = message
    }

    private fun showToastMessage(message: String?) {
        Timber.d("showToastMessage")
        toastText.value = message
    }

    /**
     * Get the items.
     * @return a [Flowable] that will emit every time the messages have been updated.
     */
    fun getItems():Flowable<List<DarkSky>> {
        return dataSource.getItems()
                .filter {items -> items.isNotEmpty()}
    }

    /**
     * Get the last item.
     * @return a [Flowable] that will emit every time the messages have been updated.
     */
    fun getLatestItem():Flowable<DarkSky> {
        return dataSource.getItems()
                .filter {items -> items.isNotEmpty() }
                .map { items -> items[items.size - 1] }
    }

    /**
     * Insert new items into the database.
     */
    private fun insertNetworkResponse(icon: String, temp: String, precip: String, units: String, summary: String, data: String, umbrella: Boolean) {
        Timber.d("insertNetworkResponse")
        disposable.add(Completable.fromAction {
            val createdAt = DateUtils.generateCreatedAtDate()
            val item = DarkSky()
            item.icon = icon
            item.summary = summary
            item.apparentTemperature = temp
            item.precipProbability = precip
            item.data = data
            item.units = units
            item.umbrella = umbrella
            item.createdAt = createdAt
            dataSource.insertItem(item) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                }, { error -> Timber.e("Database error" + error.message)}))
    }

    public override fun onCleared() {
        //prevents memory leaks by disposing pending observable objects
        if ( !disposable.isDisposed) {
            disposable.clear()
        }
    }

    /**
     * @param key The api key for the DarkSky weather api
     * @param units SI or US
     * @param lat Location latitude
     * @param lon Location longitude
     * @param callback A nice little listener to wrap up the response
     */
    fun getDarkSkyHourlyForecast(key: String, units: String, lat: String, lon: String) {
        Timber.d("getDarkSkyHourlyForecast")
        val api = DarkSkyApi()
        val fetcher = DarkSkyFetcher(api)
        disposable.add(Observable.interval(LOAD_INTERVAL, TimeUnit.MINUTES)
                 .startWith(0L)
                  .subscribeOn(Schedulers.io())
                 .flatMap { n -> fetcher.getExtendedFeedData(key, units, lat, lon) }
                 .doOnNext({ darkSkyResponse ->
                     Timber.d("response received")
                     var umbrella = false
                     var icon = ""
                     var temperature = ""
                     var precipitation = ""
                     var summary = ""
                     var data = ""
                     // current weather
                     if (darkSkyResponse.currently != null) {
                         icon = darkSkyResponse.currently.icon
                         temperature = round(darkSkyResponse.currently.apparentTemperature).toString()
                         precipitation = darkSkyResponse.currently.precipProbability.toString()
                         summary = darkSkyResponse.currently.summary
                     }
                     // should we take an umbrella today?
                     if (darkSkyResponse.currently != null && darkSkyResponse.currently.precipProbability != null) {
                         umbrella = shouldTakeUmbrellaToday(darkSkyResponse.currently.precipProbability!!)
                     }
                     // extended forecast
                     if (darkSkyResponse.daily != null) {
                         data = Gson().toJson(darkSkyResponse.daily.data)
                     }

                     insertNetworkResponse(icon, temperature, precipitation, units, summary, data, umbrella)
                 })
                 .doOnComplete({
                     Timber.d("complete")}
                 )
                 .doOnError { error ->
                     var errorMessage: String? = "Error retrieving Dark Sky data."
                     if(!TextUtils.isEmpty(error.message)) {
                         errorMessage = "Dark Sky Error " + error.message
                     }
                     Timber.e("Dark Sky Error: " + errorMessage);
                     Observable.just(true)
                             .observeOn(AndroidSchedulers.mainThread())
                             .subscribe { aBoolean ->
                                 showAlertMessage("Dark Sky: " + errorMessage)
                             }
                 }
                 .observeOn(AndroidSchedulers.mainThread())
                 .subscribe())


       /* disposable.add(fetcher.getExtendedFeedData(key, units, lat, lon)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith( object : DisposableObserver<DarkSkyResponse>() {
                    override fun onNext(darkSkyResponse: DarkSkyResponse) {
                        var umbrella = false
                        var icon = ""
                        var temperature = ""
                        var precipitation = ""
                        var summary = ""
                        var data = ""

                        // current weather
                        if (darkSkyResponse.currently != null) {
                            icon = darkSkyResponse.currently.icon
                            temperature = round(darkSkyResponse.currently.apparentTemperature).toString()
                            precipitation = darkSkyResponse.currently.precipProbability.toString()
                            summary = darkSkyResponse.currently.summary
                        }

                        // should we take an umbrella today?
                        if (darkSkyResponse.currently != null && darkSkyResponse.currently.precipProbability != null) {
                            umbrella = shouldTakeUmbrellaToday(darkSkyResponse.currently.precipProbability!!)
                        }

                        // extended forecast
                        if (darkSkyResponse.daily != null) {
                            data = Gson().toJson(darkSkyResponse.daily.data)
                        }

                        showAlertMessage("We win!!")
                        insertNetworkResponse(icon, temperature, precipitation, units, summary, data, umbrella)
                    }
                    override fun onComplete() {
                        Timber.d("complete");
                        showAlertMessage("We win!!")
                    }
                    override fun onError(error: Throwable) {
                        var errorMessage: String? = "Error retrieving DarkSky data."
                        if(!TextUtils.isEmpty(error.message)) {
                            errorMessage = "DarkSky Error " + error.message
                        }
                        Timber.e("error: " + errorMessage);
                        showAlertMessage(errorMessage)
                    }
                }))*/
    }

    /**
     * Determines if today is a good day to take your umbrella
     * Adapted from https://github.com/HannahMitt/HomeMirror/.
     * @return
     */
    private fun shouldTakeUmbrellaToday(precipProbability: Double): Boolean {
        return precipProbability > 0.3
    }

    /**
     * Network connectivity receiver to notify client of the network disconnect issues and
     * to clear any network notifications when reconnected. It is easy for network connectivity
     * to run amok that is why we only notify the user once for network disconnect with
     * a boolean flag.
     */
    companion object {
        val LOAD_INTERVAL: Long = 30 // in minutes
    }
}