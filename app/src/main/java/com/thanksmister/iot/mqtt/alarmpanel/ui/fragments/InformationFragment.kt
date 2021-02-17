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

package com.thanksmister.iot.mqtt.alarmpanel.ui.fragments

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper.getMainLooper
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.thanksmister.iot.mqtt.alarmpanel.BaseActivity
import com.thanksmister.iot.mqtt.alarmpanel.BaseFragment
import com.thanksmister.iot.mqtt.alarmpanel.R
import com.thanksmister.iot.mqtt.alarmpanel.persistence.Configuration
import com.thanksmister.iot.mqtt.alarmpanel.persistence.Weather
import com.thanksmister.iot.mqtt.alarmpanel.utils.DialogUtils
import com.thanksmister.iot.mqtt.alarmpanel.utils.StringUtils.isDouble
import com.thanksmister.iot.mqtt.alarmpanel.utils.StringUtils.stringToDouble
import com.thanksmister.iot.mqtt.alarmpanel.utils.WeatherUtils
import com.thanksmister.iot.mqtt.alarmpanel.viewmodel.WeatherViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_information.*
import timber.log.Timber
import java.text.DateFormat
import java.util.*
import javax.inject.Inject

class InformationFragment() : BaseFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var weatherViewModel: WeatherViewModel

    @Inject
    lateinit var configuration: Configuration

    @Inject
    lateinit var dialogUtils: DialogUtils

    private var weather: Weather? = null
    private var timeHandler: Handler? = null

    private var listener: InformationFragmentListener? = null

    interface InformationFragmentListener {
        fun openExtendedForecast(weather: Weather)
    }

    private val timeRunnable = object : Runnable {
        override fun run() {
            try {
                val currentDateString = DateFormat.getDateInstance(DateFormat.LONG, Locale.getDefault()).format(Date())
                val currentTimeString = DateUtils.formatDateTime(context, Date().time, DateUtils.FORMAT_SHOW_TIME)
                dateText.text = currentDateString
                timeText.text = currentTimeString
                if (timeHandler != null) {
                    timeHandler?.postDelayed(this, 1000)
                }
            } catch (e: IllegalArgumentException) {
                Timber.e(e.message)
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        weatherViewModel = ViewModelProviders.of(this, viewModelFactory).get(WeatherViewModel::class.java)
        observeViewModel(weatherViewModel)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        timeHandler = Handler(getMainLooper())
        timeHandler!!.postDelayed(timeRunnable, 1000)
        weatherLayout.visibility = View.VISIBLE
        weatherLayout.setOnClickListener {
            weather?.let { weather ->
                listener?.openExtendedForecast(weather)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_information, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is InformationFragmentListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement InformationFragmentListener")
        }
    }

    override fun onResume() {
        super.onResume()
        if (configuration.showWeatherModule()) {
            weatherLayout.visibility = View.VISIBLE
        } else {
            weatherViewModel.onCleared()
            weatherLayout.visibility = View.GONE
        }
    }

    override fun onPause() {
        super.onPause()
        weatherViewModel.onCleared()
    }

    override fun onDetach() {
        super.onDetach()
        if (timeHandler != null) {
            timeHandler!!.removeCallbacks(timeRunnable)
        }
    }

    private fun observeViewModel(viewModel: WeatherViewModel) {
        viewModel.getAlertMessage().observe(this, Observer { message ->
            dialogUtils.showAlertDialog(activity as BaseActivity, message!!)
        })
        viewModel.getToastMessage().observe(this, Observer { message ->
            Toast.makeText(activity as BaseActivity, message, Toast.LENGTH_LONG).show()
        })
        disposable.add(
                viewModel.getLatestItem()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .onErrorReturn {
                            Weather()
                        }
                        .subscribe { item ->
                            item?.let {
                                weather = item
                                weatherLayout.visibility = View.VISIBLE
                                val displayUnits = if (configuration.weatherUnitsImperial) getString(R.string.text_f) else getString(R.string.text_c)
                                temperatureText.text = getString(R.string.text_temperature, weather?.temperature.toString(), displayUnits)
                                weather?.let {
                                    val precipitation = it.precipitation.orEmpty()
                                    if (isDouble(precipitation) && viewModel.shouldTakeUmbrellaToday(stringToDouble(precipitation))) {
                                        conditionImage.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_rain_umbrella, (activity as BaseActivity).theme))
                                    } else {
                                        conditionImage.setImageDrawable(ResourcesCompat.getDrawable(resources, WeatherUtils.getIconForWeatherCondition(it.condition.orEmpty()), (activity as BaseActivity).theme))
                                    }
                                    context?.let { context ->
                                        outlookText.text = WeatherUtils.getOutlookForWeatherCondition(it.condition.orEmpty(), context)
                                    }
                                }
                            }

                        }
        )
    }

    companion object {
        fun newInstance(): InformationFragment {
            return InformationFragment()
        }
    }
}