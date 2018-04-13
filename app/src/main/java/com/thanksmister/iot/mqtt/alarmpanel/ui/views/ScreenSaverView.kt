/*
 * <!--
 *   ~ Copyright (c) 2017. ThanksMister LLC
 *   ~
 *   ~ Licensed under the Apache License, Version 2.0 (the "License");
 *   ~ you may not use this file except in compliance with the License. 
 *   ~ You may obtain a copy of the License at
 *   ~
 *   ~ http://www.apache.org/licenses/LICENSE-2.0
 *   ~
 *   ~ Unless required by applicable law or agreed to in writing, software distributed 
 *   ~ under the License is distributed on an "AS IS" BASIS, 
 *   ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 *   ~ See the License for the specific language governing permissions and 
 *   ~ limitations under the License.
 *   -->
 */

package com.thanksmister.iot.mqtt.alarmpanel.ui.views

import android.content.Context
import android.content.res.Configuration
import android.os.Handler
import android.support.v4.content.res.ResourcesCompat
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.widget.RelativeLayout
import com.squareup.picasso.Picasso
import com.thanksmister.iot.mqtt.alarmpanel.R
import com.thanksmister.iot.mqtt.alarmpanel.network.DarkSkyRequest
import com.thanksmister.iot.mqtt.alarmpanel.network.ImageApi
import com.thanksmister.iot.mqtt.alarmpanel.network.ImageOptions
import com.thanksmister.iot.mqtt.alarmpanel.network.fetchers.ImageFetcher
import com.thanksmister.iot.mqtt.alarmpanel.network.model.ImageResponse
import com.thanksmister.iot.mqtt.alarmpanel.network.model.Item
import com.thanksmister.iot.mqtt.alarmpanel.persistence.DarkSkyDao
import com.thanksmister.iot.mqtt.alarmpanel.tasks.ImageTask
import com.thanksmister.iot.mqtt.alarmpanel.tasks.NetworkTask
import com.thanksmister.iot.mqtt.alarmpanel.utils.WeatherUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.dialog_screen_saver.view.*
import retrofit2.Response
import timber.log.Timber
import java.text.DateFormat
import java.util.*

class ScreenSaverView : RelativeLayout {

    private var task: ImageTask? = null
    private var rotationHandler: Handler? = null
    private var timeHandler: Handler? = null
    private var picasso: Picasso? = null
    private var itemList: List<Item>? = null
    private var imageUrl: String? = null
    private var rotationInterval: Long = 0
    private var options:ImageOptions? = null

    private var saverContext: Context? = null
    private var dataSource: DarkSkyDao? = null
    private var useImageSaver: Boolean = false
    private var hasWeather: Boolean = false

    private val delayRotationRunnable = object : Runnable {
        override fun run() {
            rotationHandler!!.removeCallbacks(this)
            startImageRotation()
        }
    }

    private val timeRunnable = object : Runnable {
        override fun run() {
            val currentTimeString = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.getDefault()).format(Date())
            screenSaverClockSmall.text = currentTimeString
            screenSaverClock.text = currentTimeString
            if (timeHandler != null) {
                timeHandler!!.postDelayed(this, 1000)
            }
        }
    }

    constructor(context: Context) : super(context) {
        saverContext = context
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        saverContext = context
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (task != null) {
            task!!.cancel(true)
            task = null
        }

        if (picasso != null) {
            picasso!!.invalidate(imageUrl!!)
            picasso!!.cancelRequest(screenSaverImage)
            picasso = null
        }

        if (rotationHandler != null) {
            rotationHandler!!.removeCallbacks(delayRotationRunnable)
        }

        if (timeHandler != null) {
            timeHandler!!.removeCallbacks(timeRunnable)
        }
    }

    fun setDataSource(dataSource: DarkSkyDao) {
        this.dataSource = dataSource
    }

    // setup clock size based on screen and weather settings
    private fun setWeatherClockViews() {
        val initialRegular = screenSaverClock.textSize
        val initialSmall = screenSaverClockSmall.textSize
        if (!hasWeather) {
            screenSaverClock.setTextSize(TypedValue.COMPLEX_UNIT_PX, initialRegular + 100)
            screenSaverClockSmall.setTextSize(TypedValue.COMPLEX_UNIT_PX, initialSmall + 60)
        }

        // setup the views
        if (useImageSaver) {
            screenSaverImageLayout.visibility = View.VISIBLE
            screenSaverClockLayout.visibility = View.GONE
            if(!hasWeather) {
                screenSaverWeatherSmallLayout.visibility = View.GONE
            } else {
                screenSaverWeatherSmallLayout.visibility = View.VISIBLE
            }
            startImageScreenSavor()
        } else { // use clock
            screenSaverImageLayout.visibility = View.GONE
            screenSaverClockLayout.visibility = View.VISIBLE
            if(!hasWeather) {
                screenSaverWeatherLayout.visibility = View.GONE
            } else {
                screenSaverWeatherLayout.visibility = View.VISIBLE
            }
        }
    }

    private fun setWeatherDataOnView() {
        if(dataSource != null) {
            dataSource!!.getItems()
                    .filter { items -> items.isNotEmpty() }
                    .map { items -> items[items.size - 1] }
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ item ->
                        if (item != null) {
                            val displayUnits = if (item.units == DarkSkyRequest.UNITS_US) saverContext!!.getString(R.string.text_f) else saverContext!!.getString(R.string.text_c)
                            if (useImageSaver) {
                                temperatureTextSmall.text = saverContext!!.getString(R.string.text_temperature, item.apparentTemperature, displayUnits)
                                conditionImageSmall.setImageDrawable(ResourcesCompat.getDrawable(resources, WeatherUtils.getIconForWeatherCondition(item.icon), saverContext!!.applicationContext.theme))
                                if (item.umbrella) {
                                    conditionImageSmall.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_rain_umbrella, saverContext!!.applicationContext.theme))
                                }
                            } else {
                                temperatureText.text = saverContext!!.getString(R.string.text_temperature, item.apparentTemperature, displayUnits)
                                conditionImage.setImageDrawable(ResourcesCompat.getDrawable(resources, WeatherUtils.getIconForWeatherCondition(item.icon), saverContext!!.applicationContext.theme))
                                if (item.umbrella) {
                                    conditionImage.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_rain_umbrella, saverContext!!.applicationContext.theme))
                                }
                            }
                        }
                    })
        }
    }

    fun setScreenSaver(useImageScreenSaver: Boolean,  options:ImageOptions, dataSource: DarkSkyDao, hasWeather: Boolean) {
        this.dataSource = dataSource
        this.options = options
        this.rotationInterval = (options.getRotation() * 60 * 1000).toLong() // convert to milliseconds
        this.hasWeather = hasWeather
        this.useImageSaver = useImageScreenSaver
        if(hasWeather) {
            setWeatherDataOnView()
        }
        setWeatherClockViews()
        timeHandler = Handler()
        timeHandler!!.postDelayed(timeRunnable, 10)
    }

    private fun startImageScreenSavor() {
        if (itemList == null || itemList!!.isEmpty()) {
            fetchMediaData()
        } else {
            startImageRotation()
        }
    }

    private fun startImageRotation() {
        if (picasso == null) {
            picasso = Picasso.with(context)
        }
        if (itemList != null && !itemList!!.isEmpty()) {
            val min = 0
            val max = itemList!!.size - 1
            val random = Random().nextInt(max - min + 1) + min
            val item = itemList!![random]
            if(item.images !=  null) {
                val minImage = 0
                val maxImage = item.images.size - 1
                val randomImage = Random().nextInt(maxImage - minImage + 1) + minImage
                val image = item.images[randomImage]
                imageUrl = image.link

                if (options!!.imageFitScreen) {
                    picasso!!.load(imageUrl)
                            .placeholder(R.color.black)
                            .resize(screenSaverImage.width, screenSaverImage.height)
                            .centerCrop()
                            .error(R.color.black)
                            .into(screenSaverImage)
                } else {
                    picasso!!.load(imageUrl)
                            .placeholder(R.color.black)
                            .resize(screenSaverImage.width, screenSaverImage.height)
                            .centerInside()
                            .error(R.color.black)
                            .into(screenSaverImage)
                }
                if (rotationHandler == null) {
                    rotationHandler = Handler()
                }
                rotationHandler!!.postDelayed(delayRotationRunnable, rotationInterval)
            } else {
                startImageRotation()
            }
        }
    }

    // TODO move to RxJava
    private fun fetchMediaData() {
        if (task == null || task!!.isCancelled) {
            val api = ImageApi()
            val fetcher = ImageFetcher(api)
            task = ImageTask(fetcher)
            task!!.setOnExceptionListener(object :   NetworkTask.OnExceptionListener {
                override fun onException(paramException: Exception) {
                    Timber.e("Imgur Exception: " + paramException.message)
                }
            })
            task!!.setOnCompleteListener(object : NetworkTask.OnCompleteListener<Response<ImageResponse>> {
                override fun onComplete(paramResult: Response<ImageResponse>) {
                    val instagramResponse = paramResult.body()
                    if (instagramResponse != null) {
                        itemList = instagramResponse.items
                        startImageRotation()
                    }
                }
            })
            task!!.execute(options!!.imageClientId, options!!.getTag())
        }
    }
}