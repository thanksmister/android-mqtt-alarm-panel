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

package com.thanksmister.iot.mqtt.alarmpanel.ui.views

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.net.http.SslError
import android.os.Build
import android.os.Handler
import android.text.TextUtils
import android.text.format.DateUtils
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.webkit.*
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import com.thanksmister.iot.mqtt.alarmpanel.BaseActivity
import com.thanksmister.iot.mqtt.alarmpanel.R
import com.thanksmister.iot.mqtt.alarmpanel.network.ImageOptions
import com.thanksmister.iot.mqtt.alarmpanel.persistence.Configuration
import com.thanksmister.iot.mqtt.alarmpanel.persistence.Weather
import com.thanksmister.iot.mqtt.alarmpanel.persistence.WeatherDao
import com.thanksmister.iot.mqtt.alarmpanel.tasks.ImageTask
import com.thanksmister.iot.mqtt.alarmpanel.utils.StringUtils
import com.thanksmister.iot.mqtt.alarmpanel.utils.WeatherUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.adapter_forcast_card.view.*
import kotlinx.android.synthetic.main.dialog_screen_saver.view.*
import kotlinx.android.synthetic.main.dialog_screen_saver.view.temperatureText
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit

class ScreenSaverView : RelativeLayout {

    private val disposable = CompositeDisposable()
    private var task: ImageTask? = null
    private var rotationHandler: Handler? = null
    private var timeHandler: Handler? = null
    private var picasso: Picasso? = null
    private var imageUrl: String? = null
    private var rotationInterval: Long = 0
    private var saverContext: Context? = null
    private var weatherSource: WeatherDao? = null
    private var useUnsplashScreenSaver: Boolean = false
    private var useWebScreenSaver: Boolean = false
    private var useClockScreenSaver: Boolean = false
    private var hasWeather: Boolean = false
    private var useImperial: Boolean = false
    private var parentWidth: Int = 0
    private var parentHeight: Int = 0
    private val calendar: Calendar = Calendar.getInstance()
    private var delayRotate = true
    private var webUrl = Configuration.WEB_SCREEN_SAVER
    private var certPermissionsShown = false

    private val delayRotationRunnable = object : Runnable {
        override fun run() {
            startImageScreenSavor()
            rotationHandler?.postDelayed(this, TimeUnit.SECONDS.toMillis(rotationInterval))
        }
    }

    private val timeRunnable = object : Runnable {
        override fun run() {
            val date = Date()
            calendar.time = date
            val currentTimeString = DateUtils.formatDateTime(context, date.time, DateUtils.FORMAT_SHOW_TIME)
            screenSaverClock.text = currentTimeString
            screenSaverClockSmall.text = currentTimeString

            // use this only with the clock feature
            if (!useUnsplashScreenSaver && !useWebScreenSaver && !delayRotate) {
                val width = screenSaverClockLayout.width
                val height = screenSaverClockLayout.height
                parentWidth = screenSaverView.width
                parentHeight = screenSaverView.height
                try {
                    if (width > 0 && height > 0 && parentWidth > 0 && parentHeight > 0) {
                        if (parentHeight - width > 0) {
                            val newX = Random().nextInt(parentWidth - width)
                            screenSaverClockLayout.x = newX.toFloat()
                        }
                        if (parentHeight - height > 0) {
                            val newY = Random().nextInt(parentHeight - height)
                            screenSaverClockLayout.y = newY.toFloat()
                        }
                    }
                } catch (e: IllegalArgumentException) {
                    Timber.e(e.message)
                }
            }

            val offset = 60L - calendar.get(Calendar.SECOND)
            timeHandler?.postDelayed(this, TimeUnit.SECONDS.toMillis(offset))
            delayRotate = false
        }
    }

    constructor(context: Context) : super(context) {
        saverContext = context
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        saverContext = context
    }

    fun init(useUnsplashScreenSaver: Boolean = false,
             useClockScreenSaver: Boolean = false,
             hasWeather: Boolean = false,
             useImperial: Boolean = false,
             hasWebScreenSaver: Boolean = false,
             imageOptions: ImageOptions,
             webUrl: String = "",
             weatherDao: WeatherDao) {

        this.weatherSource = weatherDao
        this.rotationInterval = imageOptions.imageRotation.toLong()
        this.hasWeather = hasWeather
        this.useImperial = useImperial
        this.useUnsplashScreenSaver = useUnsplashScreenSaver
        this.useWebScreenSaver = hasWebScreenSaver
        this.useClockScreenSaver = useClockScreenSaver
        if (!TextUtils.isEmpty(webUrl)) {
            this.webUrl = webUrl
        }
        setScreenSaverView()
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

        disposable.clear()
    }

    private fun setScreenSaverView() {
        val initialRegular = screenSaverClock.textSize
        val initialSmall = screenSaverClockSmall.textSize

        if (!hasWeather) {
            screenSaverClock.setTextSize(TypedValue.COMPLEX_UNIT_PX, initialRegular + 100)
            screenSaverClockSmall.setTextSize(TypedValue.COMPLEX_UNIT_PX, initialSmall + 60)
        }

        if (useUnsplashScreenSaver) {
            screenSaverImageLayout.visibility = View.VISIBLE
            screenSaverWebViewLayout.visibility = View.GONE
            screenSaverWeatherSmallLayout.visibility = View.GONE
            screenSaverClockSmall.visibility = View.GONE
            if (hasWeather && useClockScreenSaver) {
                setWeatherDataOnView()
                screenSaverWeatherSmallLayout.visibility = View.VISIBLE
                screenSaverClockSmall.visibility = View.VISIBLE
                timeHandler = Handler()
                timeHandler?.postDelayed(timeRunnable, 10)
            }
            rotationHandler = Handler()
            rotationHandler?.postDelayed(delayRotationRunnable, 10)
        } else if (useWebScreenSaver && webUrl.isNotEmpty()) {
            screenSaverWebViewLayout.visibility = View.VISIBLE
            screenSaverImageLayout.visibility = View.GONE
            screenSaverClockLayout.visibility = View.GONE
            if (hasWeather && useClockScreenSaver) {
                setWeatherDataOnView()
                screenSaverWeatherSmallLayout.visibility = View.VISIBLE
                screenSaverClockSmall.visibility = View.VISIBLE
                timeHandler = Handler()
                timeHandler?.postDelayed(timeRunnable, 10)
            }
            startWebScreenSaver(webUrl)
        } else if (useClockScreenSaver) { // use clock
            screenSaverWebViewLayout.visibility = View.GONE
            screenSaverImageLayout.visibility = View.GONE
            screenSaverClockLayout.visibility = View.VISIBLE
            if (!hasWeather) {
                screenSaverWeatherLayout.visibility = View.GONE
            } else {
                setWeatherDataOnView()
                screenSaverWeatherLayout.visibility = View.VISIBLE
            }
            timeHandler = Handler()
            timeHandler?.postDelayed(timeRunnable, 10)
        }
    }

    @SuppressLint("CheckResult")
    private fun setWeatherDataOnView() {
        weatherSource?.let {
            disposable.add(it.getItems()
                    .filter { items -> items.isNotEmpty() }
                    .map { items -> items[0] }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnError { Weather() }
                    .subscribe { item ->
                        setDisplayData(item)
                    })
        }
    }

    private fun setDisplayData(weather: Weather) {
        val displayUnits = if (useImperial) saverContext!!.getString(R.string.text_f) else saverContext!!.getString(R.string.text_c)
        val precipitation = weather.precipitation.orEmpty()
        if (useUnsplashScreenSaver) {
            temperatureTextSmall.text = saverContext?.getString(R.string.text_temperature, weather.temperature.toString(), displayUnits)
            temperatureTextSmall.text = saverContext?.getString(R.string.text_temperature, weather.temperature.toString(), displayUnits)
            if (StringUtils.isDouble(precipitation) && shouldTakeUmbrellaToday(StringUtils.stringToDouble(precipitation))) {
                conditionImageSmall.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_rain_umbrella, (saverContext!! as BaseActivity).theme))
            } else {
                conditionImageSmall.setImageDrawable(ResourcesCompat.getDrawable(resources, WeatherUtils.getIconForWeatherCondition(weather.condition), (saverContext!! as BaseActivity).theme))
            }
        } else {
            temperatureText.text = saverContext?.getString(R.string.text_temperature, weather.temperature.toString(), displayUnits)
            if (StringUtils.isDouble(precipitation) && shouldTakeUmbrellaToday(StringUtils.stringToDouble(precipitation))) {
                conditionImage.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_rain_umbrella, (saverContext!! as BaseActivity).theme))
            } else {
                conditionImage.setImageDrawable(ResourcesCompat.getDrawable(resources, WeatherUtils.getIconForWeatherCondition(weather.condition), (saverContext!! as BaseActivity).theme))
            }
        }
    }

    private fun shouldTakeUmbrellaToday(precipitation: Double?): Boolean {
        precipitation?.let {
            return precipitation > PRECIP_AMOUNT
        }
        return false
    }

    private fun closeView() {
        this.callOnClick()
    }

    private fun startWebScreenSaver(url: String) {
        Timber.d("startWebScreenSaver $url")
        loadWebPage(url)
    }

    private fun startImageScreenSavor() {
        Picasso.get()
                .load(String.format(UNSPLASH_IT_URL, screenSaverView.width, screenSaverView.height))
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .networkPolicy(NetworkPolicy.NO_CACHE)
                .into(screenSaverImage)
    }

    private fun loadWebPage(url: String) {
        Timber.d("loadWebPage url ${url}")
        configureWebSettings("")
        clearCache()
        screenSaverWebView?.webChromeClient = object : WebChromeClient() {
            override fun onJsAlert(view: WebView, url: String, message: String, result: JsResult): Boolean {
                AlertDialog.Builder(view.context, R.style.CustomAlertDialog)
                        .setTitle(context.getString(R.string.dialog_title_ssl_error))
                        .setMessage(message)
                        .setPositiveButton(android.R.string.ok, null)
                        .show()
                return true
            }
        }
        screenSaverWebView?.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                v?.performClick()
                closeView()
                return false
            }
        })
        screenSaverWebView?.webViewClient = object : WebViewClient() {
            //If you will not use this method url links are open in new browser not in webview
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                view.loadUrl(url)
                return true
            }

            override fun onReceivedError(view: WebView, errorCode: Int, description: String, failingUrl: String) {
                Toast.makeText(context, description, Toast.LENGTH_SHORT).show()
            }

            // TODO we need to load SSL certificates
            override fun onReceivedSslError(view: WebView, handler: SslErrorHandler?, error: SslError?) {
                if (!certPermissionsShown) {
                    var message = context.getString(R.string.dialog_message_ssl_generic)
                    when (error?.primaryError) {
                        SslError.SSL_UNTRUSTED -> message = context.getString(R.string.dialog_message_ssl_untrusted)
                        SslError.SSL_EXPIRED -> message = context.getString(R.string.dialog_message_ssl_expired)
                        SslError.SSL_IDMISMATCH -> message = context.getString(R.string.dialog_message_ssl_mismatch)
                        SslError.SSL_NOTYETVALID -> message = context.getString(R.string.dialog_message_ssl_not_yet_valid)
                    }
                    message += context.getString(R.string.dialog_message_ssl_continue)
                    AlertDialog.Builder(context, R.style.CustomAlertDialog)
                            .setTitle(context.getString(R.string.dialog_title_ssl_error))
                            .setMessage(message)
                            .setPositiveButton(android.R.string.ok, DialogInterface.OnClickListener { _, _ ->
                                certPermissionsShown = true
                                handler?.proceed()
                            })
                            .setNegativeButton(android.R.string.cancel, DialogInterface.OnClickListener { _, _ ->
                                certPermissionsShown = false
                                handler?.cancel()
                            })
                            .show()
                } else {
                    // we have already shown permissions, no need to show again on page refreshes or when page auto-refreshes itself
                    handler?.proceed()
                }
            }
        }
        screenSaverWebView?.loadUrl(url)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun configureWebSettings(userAgent: String) {
        val webSettings = screenSaverWebView?.settings
        webSettings?.javaScriptEnabled = true
        webSettings?.domStorageEnabled = true
        webSettings?.databaseEnabled = true
        webSettings?.setAppCacheEnabled(true)
        webSettings?.javaScriptCanOpenWindowsAutomatically = true
        if (!TextUtils.isEmpty(userAgent)) {
            webSettings?.userAgentString = userAgent
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webSettings?.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }
        Timber.d(webSettings?.userAgentString)
    }

    private fun clearCache() {
        screenSaverWebView?.clearCache(true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().removeAllCookies(null)
        }
    }

    companion object {
        const val PRECIP_AMOUNT: Double = 0.3 // rain probability
        const val UNSPLASH_IT_URL = "http://unsplash.it/%s/%s?random"
    }
}