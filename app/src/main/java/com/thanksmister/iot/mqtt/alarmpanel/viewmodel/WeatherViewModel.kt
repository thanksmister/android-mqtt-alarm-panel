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
import com.thanksmister.iot.mqtt.alarmpanel.persistence.Weather
import com.thanksmister.iot.mqtt.alarmpanel.persistence.WeatherDao
import com.thanksmister.iot.mqtt.alarmpanel.utils.DateUtils
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import javax.inject.Inject

class WeatherViewModel @Inject
constructor(application: Application, private val dataSource: WeatherDao) : AndroidViewModel(application) {

    private val toastText = ToastMessage()
    private val alertText = AlertMessage()
    private val disposable = CompositeDisposable()

    fun getToastMessage(): ToastMessage {
        return toastText
    }

    fun getAlertMessage(): AlertMessage {
        return alertText
    }

    fun getLatestItem():Flowable<Weather> {
        return dataSource.getItems()
                .filter {items -> items.isNotEmpty() }
                .map { items -> items[0] }
    }

    public override fun onCleared() {
        Timber.d("onCleared")
        //prevents memory leaks by disposing pending observable objects
        if (!disposable.isDisposed) {
            try {
                disposable.clear()
            } catch (e: Exception) {
                Timber.e(e.message)
            }
        }
    }

    /**
     * @param key The api key for the DarkSky weather api
     * @param units SI or US
     * @param lat Location latitude
     * @param lon Location longitude
     */
    /*fun getDarkSkyHourlyForecast(key: String, units: String, lat: String, lon: String) {
        if(disposable.size() > 0) {
            return
        }
        Timber.d("getDarkSkyHourlyForecast")
        val api = DarkSkyApi()
        val fetcher = DarkSkyFetcher(api)
        disposable.add(Observable.interval(LOAD_INTERVAL, TimeUnit.MINUTES)
                .startWith(0L)
                .subscribeOn(Schedulers.io())
                .doOnError { error ->
                    var errorMessage: String? = "Error retrieving Dark Sky data."
                    if (!TextUtils.isEmpty(error.message)) {
                        errorMessage = "Dark Sky Error " + error.message
                    }
                    Timber.e("Dark Sky Error: " + errorMessage);
                    Observable.just(true)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe {
                                showAlertMessage(errorMessage)
                            }
                }
                .flatMap { n -> fetcher.getExtendedFeedData(key, units, lat, lon) }
                .doOnNext { darkSkyResponse ->
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
                }
                .doOnComplete {
                    Timber.d("complete")
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                }, { error -> Timber.e("Dark Sky error" + error.message) }))
    }*/

    /**
     * Determines if today is a good day to take your umbrella
     * Adapted from https://github.com/HannahMitt/HomeMirror/.
     * @return
     */
    fun shouldTakeUmbrellaToday(precipitation: Double?): Boolean {
        precipitation?.let {
            return precipitation > PRECIP_AMOUNT
        }
        return false
    }

    companion object {
        const val PRECIP_AMOUNT: Double = 0.3 // rain probability
    }
}