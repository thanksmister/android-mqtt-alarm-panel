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
import android.os.Handler
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout
import com.squareup.picasso.Picasso
import com.thanksmister.iot.mqtt.alarmpanel.R
import com.thanksmister.iot.mqtt.alarmpanel.network.ImageApi
import com.thanksmister.iot.mqtt.alarmpanel.network.ImageOptions
import com.thanksmister.iot.mqtt.alarmpanel.network.fetchers.ImageFetcher
import com.thanksmister.iot.mqtt.alarmpanel.network.model.ImageResponse
import com.thanksmister.iot.mqtt.alarmpanel.network.model.Item
import com.thanksmister.iot.mqtt.alarmpanel.tasks.ImageTask
import com.thanksmister.iot.mqtt.alarmpanel.tasks.NetworkTask
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

    private var listener: ViewListener? = null
    private var saverContext: Context? = null

    private val delayRotationRunnable = object : Runnable {
        override fun run() {
            rotationHandler!!.removeCallbacks(this)
            startImageRotation()
        }
    }

    private val timeRunnable = object : Runnable {
        override fun run() {
            val currentTimeString = DateFormat.getTimeInstance(DateFormat.DEFAULT, Locale.getDefault()).format(Date())
            screenSaverClock.text = currentTimeString
            if (timeHandler != null) {
                timeHandler!!.postDelayed(this, 1000)
            }
        }
    }

    interface ViewListener {
        fun onMotion()
    }

    constructor(context: Context) : super(context) {
        saverContext = context
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        saverContext = context
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
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

    fun setListener(listener: ViewListener) {
        this.listener = listener
    }

    fun setScreenSaver(useImageScreenSaver: Boolean, useClockScreenSaver: Boolean, options:ImageOptions) {

        this.options = options
        this.rotationInterval = (options.getRotation() * 60 * 1000).toLong() // convert to milliseconds

        if (useImageScreenSaver && options.isValid) {
            screenSaverImage.visibility = View.VISIBLE
            screenSaverClock.visibility = View.GONE
            if (timeHandler != null) {
                timeHandler!!.removeCallbacks(timeRunnable)
            }
            startImageScreenSavor()
        } else if(useClockScreenSaver) { // use clock
            screenSaverImage.visibility = View.GONE
            screenSaverClock.visibility = View.VISIBLE
            timeHandler = Handler()
            timeHandler!!.postDelayed(timeRunnable, 10)
        }
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