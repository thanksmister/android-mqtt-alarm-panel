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

package com.thanksmister.iot.mqtt.alarmpanel.network

import android.annotation.SuppressLint
import android.app.KeyguardManager
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.Observer
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.net.wifi.WifiManager
import android.os.*
import android.provider.Settings
import androidx.core.content.ContextCompat
import android.text.TextUtils
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.gson.GsonBuilder
import com.koushikdutta.async.AsyncServer
import com.koushikdutta.async.ByteBufferList
import com.koushikdutta.async.http.server.AsyncHttpServer
import com.koushikdutta.async.http.server.AsyncHttpServerResponse
import com.thanksmister.iot.mqtt.alarmpanel.LifecycleHandler
import com.thanksmister.iot.mqtt.alarmpanel.R
import com.thanksmister.iot.mqtt.alarmpanel.managers.ConnectionLiveData
import com.thanksmister.iot.mqtt.alarmpanel.modules.*
import com.thanksmister.iot.mqtt.alarmpanel.persistence.*
import com.thanksmister.iot.mqtt.alarmpanel.ui.activities.MainActivity
import com.thanksmister.iot.mqtt.alarmpanel.utils.AlarmUtils
import com.thanksmister.iot.mqtt.alarmpanel.utils.ComponentUtils
import com.thanksmister.iot.mqtt.alarmpanel.utils.ComponentUtils.Companion.COMMAND_ALERT
import com.thanksmister.iot.mqtt.alarmpanel.utils.ComponentUtils.Companion.COMMAND_AUDIO
import com.thanksmister.iot.mqtt.alarmpanel.utils.ComponentUtils.Companion.COMMAND_CAPTURE
import com.thanksmister.iot.mqtt.alarmpanel.utils.ComponentUtils.Companion.COMMAND_NOTIFICATION
import com.thanksmister.iot.mqtt.alarmpanel.utils.ComponentUtils.Companion.COMMAND_SENSOR_FACE
import com.thanksmister.iot.mqtt.alarmpanel.utils.ComponentUtils.Companion.COMMAND_SENSOR_MOTION
import com.thanksmister.iot.mqtt.alarmpanel.utils.ComponentUtils.Companion.COMMAND_SENSOR_PREFIX
import com.thanksmister.iot.mqtt.alarmpanel.utils.ComponentUtils.Companion.COMMAND_SENSOR_QR_CODE
import com.thanksmister.iot.mqtt.alarmpanel.utils.ComponentUtils.Companion.COMMAND_SPEAK
import com.thanksmister.iot.mqtt.alarmpanel.utils.ComponentUtils.Companion.COMMAND_STATE
import com.thanksmister.iot.mqtt.alarmpanel.utils.ComponentUtils.Companion.COMMAND_SUN
import com.thanksmister.iot.mqtt.alarmpanel.utils.ComponentUtils.Companion.COMMAND_WAKE
import com.thanksmister.iot.mqtt.alarmpanel.utils.ComponentUtils.Companion.COMMAND_WEATHER
import com.thanksmister.iot.mqtt.alarmpanel.utils.ComponentUtils.Companion.STATE_BRIGHTNESS
import com.thanksmister.iot.mqtt.alarmpanel.utils.ComponentUtils.Companion.STATE_CURRENT_URL
import com.thanksmister.iot.mqtt.alarmpanel.utils.ComponentUtils.Companion.STATE_SCREEN_ON
import com.thanksmister.iot.mqtt.alarmpanel.utils.ComponentUtils.Companion.VALUE
import com.thanksmister.iot.mqtt.alarmpanel.utils.DateUtils
import com.thanksmister.iot.mqtt.alarmpanel.utils.NotificationUtils
import dagger.android.AndroidInjection
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.exceptions.UndeliverableException
import io.reactivex.schedulers.Schedulers
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber
import java.io.IOException
import java.nio.ByteBuffer
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

class AlarmPanelService : LifecycleService(), MQTTModule.MQTTListener {

    @Inject
    lateinit var configuration: Configuration
    @Inject
    lateinit var sensorReader: SensorReader
    @Inject
    lateinit var mqttOptions: MQTTOptions
    @Inject
    lateinit var notifications: NotificationUtils
    @Inject
    lateinit var messageDataSource: MessageDao
    @Inject
    lateinit var weatherDao: WeatherDao
    @Inject
    lateinit var sunDao: SunDao

    private var cameraReader: CameraReader? = null
    private val disposable = CompositeDisposable()
    private val mJpegSockets = ArrayList<AsyncHttpServerResponse>()
    private var partialWakeLock: PowerManager.WakeLock? = null
    private var wifiLock: WifiManager.WifiLock? = null
    private var keyguardLock: KeyguardManager.KeyguardLock? = null
    private var audioPlayer: MediaPlayer? = null
    private var audioPlayerBusy: Boolean = false
    private var httpServer: AsyncHttpServer? = null
    private val mBinder = AlarmPanelServiceBinder()
    private val motionClearHandler = Handler()
    private val qrCodeClearHandler = Handler()
    private val faceClearHandler = Handler()
    private var textToSpeechModule: TextToSpeechModule? = null
    private var mqttModule: MQTTModule? = null
    private var connectionLiveData: ConnectionLiveData? = null
    private var hasNetwork = AtomicBoolean(true)
    private var motionDetected: Boolean = false
    private var qrCodeRead: Boolean = false
    private var faceDetected: Boolean = false
    private var currentUrl: String? = null
    private val reconnectHandler = Handler()
    private var localBroadCastManager: LocalBroadcastManager? = null
    private var mqttAlertMessageShown = false
    private var mqttConnected = false
    private var mqttConnecting = false
    private var mqttInitConnection = AtomicBoolean(true)

    private val restartMQTTRunnable = Runnable {
        clearAlertMessage() // clear any dialogs
        mqttAlertMessageShown = false
        mqttConnecting = false
        sendToastMessage(getString(R.string.toast_connect_retry))
        mqttModule?.restart()
    }

    inner class AlarmPanelServiceBinder : Binder() {
        val service: AlarmPanelService
            get() = this@AlarmPanelService
    }

    override fun onCreate() {
        super.onCreate()

        AndroidInjection.inject(this)

        // this must start immediately or there is a crash due to Androids new requirements for this type of service
        val notification = notifications.createOngoingNotification(getString(R.string.app_name), getString(R.string.service_notification_message))
        startForeground(ONGOING_NOTIFICATION_ID, notification)

        // prepare the lock types we may use
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        //noinspection deprecation
        partialWakeLock = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            pm.newWakeLock(PowerManager.FULL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP, "alarmpanel:partialWakeLock")
        } else {
            pm.newWakeLock(PowerManager.FULL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.ON_AFTER_RELEASE, "alarmpanel:partialWakeLock")
        }

        // wifi lock
        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        wifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL, "alarmpanel:wifiLock")

        // Some Amazon devices are not seeing this permission so we are trying to check
        val permission = "android.permission.DISABLE_KEYGUARD"
        val checkSelfPermission = ContextCompat.checkSelfPermission(this@AlarmPanelService, permission)
        if (checkSelfPermission == PackageManager.PERMISSION_GRANTED) {
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardLock = keyguardManager.newKeyguardLock("ALARM_KEYBOARD_LOCK_TAG")
            keyguardLock?.disableKeyguard()
        }

        this.currentUrl = configuration.webUrl

        configureCamera()
        startForegroundService()
        configureMqtt()
        configurePowerOptions()
        startHttp()
        configureAudioPlayer()
        configureTextToSpeech()
        startSensors()

        val filter = IntentFilter()
        filter.addAction(BROADCAST_EVENT_URL_CHANGE)
        filter.addAction(BROADCAST_EVENT_SCREEN_TOUCH)
        filter.addAction(BROADCAST_EVENT_ALARM_MODE)
        filter.addAction(BROADCAST_EVENT_USER_INACTIVE)
        filter.addAction(Intent.ACTION_SCREEN_ON)
        filter.addAction(Intent.ACTION_SCREEN_OFF)
        filter.addAction(Intent.ACTION_USER_PRESENT)

        localBroadCastManager = LocalBroadcastManager.getInstance(this)
        localBroadCastManager?.registerReceiver(mBroadcastReceiver, filter)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!disposable.isDisposed) {
            disposable.clear()
        }
        localBroadCastManager?.unregisterReceiver(mBroadcastReceiver)
        mqttModule?.let {
            it.pause()
            mqttModule = null
        }
        cameraReader?.stopCamera()
        sensorReader.stopReadings()
        stopHttp()
        stopPowerOptions()
        reconnectHandler.removeCallbacks(restartMQTTRunnable)
    }

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return mBinder
    }

    private val isScreenOn: Boolean
        get() {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH && powerManager.isInteractive || Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT_WATCH && powerManager.isScreenOn
        }

    private val screenBrightness: Int
        get() {
            Timber.d("getScreenBrightness")
            var brightness = 0
            try {
                brightness = Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return brightness
        }

    private val state: JSONObject
        get() {
            Timber.d("getState")
            val state = JSONObject()
            try {
                state.put(STATE_CURRENT_URL, currentUrl)
                state.put(STATE_SCREEN_ON, isScreenOn)
                state.put(STATE_BRIGHTNESS, screenBrightness)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            return state
        }

    private fun startForegroundService() {
        Timber.d("startForegroundService")
        connectionLiveData = ConnectionLiveData(this)
        connectionLiveData?.observe(this, Observer { connected ->
            if (connected!!) {
                handleNetworkConnect()
            } else {
                handleNetworkDisconnect()
            }
        })
        sendServiceStarted()
    }

    private fun handleNetworkConnect() {
        Timber.d("handleNetworkConnect")
        mqttModule?.let {
            if (!hasNetwork.get()) {
                it.restart()
            }
        }
        hasNetwork.set(true)
    }

    private fun handleNetworkDisconnect() {
        Timber.d("handleNetworkDisconnect")
        mqttModule?.let {
            if (hasNetwork.get()) {
                mqttConnected = false
                it.pause()
            }
        }
        hasNetwork.set(false)
    }

    private fun hasNetwork(): Boolean {
        return hasNetwork.get()
    }

    @SuppressLint("WakelockTimeout")
    private fun configurePowerOptions() {
        Timber.d("configurePowerOptions")
        partialWakeLock?.let {
            if (!it.isHeld) {
                it.acquire(3000)
            }
        }
        wifiLock?.let {
            if (!it.isHeld) {
                it.acquire()
            }
        }
        try {
            keyguardLock?.disableKeyguard()
        } catch (ex: Exception) {
            Timber.i("Disabling keyguard didn't work")
            ex.printStackTrace()
        }
    }

    private fun stopPowerOptions() {
        Timber.i("Releasing Screen/WiFi Locks")
        partialWakeLock?.let {
            if (it.isHeld) {
                it.release()
            }
        }
        wifiLock?.let {
            if (it.isHeld) {
                it.release()
            }
        }
        try {
            keyguardLock?.reenableKeyguard()
        } catch (ex: Exception) {
            Timber.i("Enabling keyguard didn't work")
            ex.printStackTrace()
        }
    }

    private fun startSensors() {
        Timber.d("sensorsEnabled ${configuration.deviceSensors}")
        if (configuration.deviceSensors && mqttOptions.isValid) {
            Timber.d("startSensors")
            sensorReader.startReadings(configuration.mqttSensorFrequency, sensorCallback)
        }
    }

    private fun configureMqtt() {
        if (mqttModule == null && mqttOptions.isValid) {
            mqttModule = MQTTModule(this@AlarmPanelService.applicationContext, mqttOptions, this@AlarmPanelService)
            lifecycle.addObserver(mqttModule!!)
            publishState(COMMAND_STATE, state.toString())
        }
    }

    override fun onMQTTConnect() {
        Timber.w("onMQTTConnect")
        if (mqttAlertMessageShown) {
            clearAlertMessage() // clear any dialogs
            mqttAlertMessageShown = false
        }
        clearFaceDetected()
        clearMotionDetected()
        mqttConnected = true
        mqttInitConnection.set(false)
    }

    override fun onMQTTDisconnect() {
        Timber.w("onMQTTDisconnect")
        handleMQTDisconnectError()
    }

    override fun onMQTTException(message: String) {
        Timber.w("onMQTTException: $message")
        handleMQTDisconnectError()
    }

    private fun handleMQTDisconnectError() {
        if(hasNetwork()) {
            mqttConnected = false
            if (mqttInitConnection.get()) {
                mqttInitConnection.set(false)
                sendAlertMessage(getString(R.string.error_mqtt_exception))
                mqttAlertMessageShown = true
            }
            if(!mqttConnecting) {
                reconnectHandler.postDelayed(restartMQTTRunnable, 30000)
                mqttConnecting = true
            }
        }
    }

    override fun onMQTTMessage(id: String, topic: String, payload: String) {
        Timber.i("onMQTTMessage topic: $topic")
        //Timber.i("onMQTTMessage payload: $payload")
        // TODO this is deprecated as we've moved this to commands but we will keep for backwards compatibility
        if (mqttOptions.getNotificationTopic() == topic) {
            speakMessage(payload)
            insertMessage(id, topic, payload)
        } else if (AlarmUtils.ALARM_STATE_TOPIC == topic && AlarmUtils.hasSupportedStates(payload)) {
            when (payload) {
                AlarmUtils.STATE_DISARM -> {
                    switchScreenOn(AWAKE_TIME)
                    if (configuration.hasSystemAlerts()) {
                        notifications.clearNotification()
                    }
                }
                AlarmUtils.STATE_ARM_AWAY,
                AlarmUtils.STATE_ARM_HOME -> {
                    switchScreenOn(AWAKE_TIME)
                }
                AlarmUtils.STATE_TRIGGERED -> {
                    switchScreenOn(TRIGGERED_AWAKE_TIME) // 3 hours
                    if (configuration.alarmMode == AlarmUtils.MODE_TRIGGERED && configuration.hasSystemAlerts()) {
                        notifications.createAlarmNotification(getString(R.string.text_notification_trigger_title), getString(R.string.text_notification_trigger_description))
                    }
                }
                AlarmUtils.STATE_PENDING -> {
                    switchScreenOn(AWAKE_TIME)
                    if ((configuration.alarmMode == AlarmUtils.MODE_ARM_HOME || configuration.alarmMode == AlarmUtils.MODE_ARM_AWAY) && configuration.hasSystemAlerts()) {
                        notifications.createAlarmNotification(getString(R.string.text_notification_entry_title), getString(R.string.text_notification_entry_description))
                    }
                }
            }
            insertMessage(id, topic, payload)
        } else {
            processCommand(id, topic, payload)
        }
    }

    private fun publishAlarm(command: String) {
        mqttModule?.let {
            Timber.d("publishAlarm $command")
            it.publishAlarm(command)
            if (command == AlarmUtils.COMMAND_DISARM) {
                captureImageTask()
            }
        }
    }

    private fun publishState(command: String, data: JSONObject) {
        publishState(command, data.toString())
    }

    private fun publishState(command: String, message: String) {
        mqttModule?.let {
            //Timber.d("publishState command $command")
            //Timber.d("publishState message $message")
            it.publishState(command, message)
        }
    }

    /**
     * Configure the camera with multiple detections (face, motion, qrcode, mjpeg stream) or
     * just set up the camera to take a capture a photo on alarm disarm
     */
    private fun configureCamera() {
        Timber.d("configureCamera ${configuration.cameraEnabled}")
        if (configuration.hasCameraDetections() || configuration.captureCameraImage()) {
            cameraReader = CameraReader(this.applicationContext)
            cameraReader?.startCamera(cameraDetectorCallback, configuration)
        }
    }

    private fun configureTextToSpeech() {
        if (textToSpeechModule == null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Timber.d("configureTextToSpeech")
            textToSpeechModule = TextToSpeechModule(this)
            lifecycle.addObserver(textToSpeechModule!!)
        }
    }

    private fun configureAudioPlayer() {
        audioPlayer = MediaPlayer()
        audioPlayer?.setOnPreparedListener { audioPlayer ->
            Timber.d("audioPlayer: File buffered, playing it now")
            audioPlayerBusy = false
            audioPlayer.start()
        }
        audioPlayer?.setOnCompletionListener { audioPlayer ->
            Timber.d("audioPlayer: Cleanup")
            if (audioPlayer.isPlaying) {  // should never happen, just in case
                audioPlayer.stop()
            }
            audioPlayer.reset()
            audioPlayerBusy = false
        }
        audioPlayer!!.setOnErrorListener { audioPlayer, i, i1 ->
            Timber.d("audioPlayer: Error playing file")
            audioPlayerBusy = false
            false
        }
    }

    private fun startHttp() {
        if (httpServer == null && configuration.httpMJPEGEnabled) {
            Timber.d("startHttp")
            httpServer = AsyncHttpServer()
            if (configuration.httpMJPEGEnabled) {
                startMJPEG()
                httpServer?.addAction("GET", "/camera/stream") { _, response ->
                    Timber.i("GET Arrived (/camera/stream)")
                    startMJPEG(response)
                }
                //Timber.i("Enabled MJPEG Endpoint")
            }
            httpServer?.addAction("*", "*") { request, response ->
                Timber.i("Unhandled Request Arrived")
                response.code(404)
                response.send("")
            }
            httpServer?.listen(AsyncServer.getDefault(), configuration.httpPort)
            Timber.i("Started HTTP server on " + configuration.httpPort)
        }
    }

    private fun stopHttp() {
        Timber.d("stopHttp")
        httpServer?.let {
            stopMJPEG()
            it.stop()
            httpServer = null
        }
    }

    private fun startMJPEG() {
        Timber.d("startMJPEG")
        try {
            cameraReader?.let {
                it.getJpeg().observe(this, Observer { jpeg ->
                    if (mJpegSockets.size > 0 && jpeg != null) {
                        var i = 0
                        while (i < mJpegSockets.size) {
                            val s = mJpegSockets[i]
                            val bb = ByteBufferList()
                            if (s.isOpen) {
                                bb.recycle()
                                bb.add(ByteBuffer.wrap("--jpgboundary\r\nContent-Type: image/jpeg\r\n".toByteArray()))
                                bb.add(ByteBuffer.wrap(("Content-Length: " + jpeg.size + "\r\n\r\n").toByteArray()))
                                bb.add(ByteBuffer.wrap(jpeg))
                                bb.add(ByteBuffer.wrap("\r\n".toByteArray()))
                                s.write(bb)
                            } else {
                                mJpegSockets.removeAt(i)
                                i--
                                //Timber.i("MJPEG Session Count is " + mJpegSockets.size)
                            }
                            i++
                        }
                    }
                })
            }

        } catch (e: Exception) {
            Timber.e(e.message)
        }
    }

    private fun stopMJPEG() {
        Timber.d("stopMJPEG Called")
        mJpegSockets.clear()
    }

    private fun startMJPEG(response: AsyncHttpServerResponse) {
        Timber.d("startMJPEG")
        if (mJpegSockets.size < configuration.httpMJPEGMaxStreams) {
            response.headers.add("Cache-Control", "no-cache")
            response.headers.add("Connection", "close")
            response.headers.add("Pragma", "no-cache")
            response.setContentType("multipart/x-mixed-replace; boundary=--jpgboundary")
            response.code(200)
            response.writeHead()
            mJpegSockets.add(response)
        } else {
            Timber.i("MJPEG stream limit was reached, not starting")
            response.send("Max streams exceeded")
            response.end()
        }
        Timber.i("MJPEG Session Count is " + mJpegSockets.size)
    }

    private fun processCommand(id: String, topic: String, commandJson: JSONObject) {
        // Timber.d("processCommand ${commandJson}")
        var payload: String = ""
        try {
            if (commandJson.has(COMMAND_WAKE)) {
                payload = commandJson.getString(COMMAND_WAKE)
                insertMessage(id, topic, payload)
                switchScreenOn(AWAKE_TIME)
            }
            if (commandJson.has(COMMAND_AUDIO)) {
                payload = commandJson.getString(COMMAND_AUDIO)
                playAudio(payload)
            }
            if (commandJson.has(COMMAND_SPEAK)) {
                payload = commandJson.getString(COMMAND_SPEAK)
                insertMessage(id, topic, payload)
                speakMessage(payload)
            }
            if (commandJson.has(COMMAND_NOTIFICATION)) {
                payload = commandJson.getString(COMMAND_NOTIFICATION)
                insertMessage(id, topic, payload)
                val title = getString(R.string.notification_title)
                notifications.createAlarmNotification(title, payload)
            }
            if (commandJson.has(COMMAND_ALERT)) {
                payload = commandJson.getString(COMMAND_ALERT)
                insertMessage(id, topic, payload)
                sendAlertMessage(payload)
            }
            if (commandJson.has(COMMAND_CAPTURE)) {
                payload = commandJson.getString(COMMAND_CAPTURE)
                insertMessage(id, topic, payload)
                captureImageTask()
            }
            if (commandJson.has(COMMAND_WEATHER) && configuration.showWeatherModule()) {
                payload = commandJson.getString(COMMAND_WEATHER)
                insertWeather(payload)
            }
            if (commandJson.has(COMMAND_SUN)) {
                payload = commandJson.getString(COMMAND_SUN)
                insertSun(payload)
            }
        } catch (ex: JSONException) {
            Timber.e("JSON Error: " + ex.message)
            Timber.e("Invalid JSON passed as a command: " + commandJson.toString())
        }
    }

    private fun processCommand(id: String, topic: String, command: String) {
        Timber.d("processCommand")
        return try {
            processCommand(id, topic, JSONObject(command))
        } catch (ex: JSONException) {
            Timber.e("JSON Error: " + ex.message)
            Timber.e("Invalid JSON passed as a command: $command")
        }
    }

    private fun playAudio(audioUrl: String) {
        Timber.d("audioPlayer")
        var isPlaying = false
        audioPlayer?.let {
            isPlaying = it.isPlaying
        }
        if (audioPlayerBusy) {
            Timber.d("audioPlayer: Cancelling all previous buffers because new audio was requested")
            audioPlayer?.reset()
        } else if (isPlaying) {
            Timber.d("audioPlayer: Stopping all media playback because new audio was requested")
            audioPlayer?.stop()
            audioPlayer?.reset()
        }
        audioPlayerBusy = true
        try {
            audioPlayer?.setDataSource(audioUrl)
        } catch (e: IOException) {
            Timber.e("audioPlayer: An error occurred while preparing audio (" + e.message + ")")
            audioPlayerBusy = false
            audioPlayer?.reset()
            return
        }
        Timber.d("audioPlayer: Buffering $audioUrl")
        audioPlayer?.prepareAsync()
    }

    private fun speakMessage(message: String) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            Timber.d("speakMessage $message")
            textToSpeechModule?.speakText(message)
        }
    }

    @SuppressLint("WakelockTimeout")
    private fun switchScreenOn(awakeTime: Long) {
        Timber.d("switchScreenOn")
        partialWakeLock?.let {
            if (!it.isHeld) {
                it.acquire(awakeTime)
            } else if (it.isHeld) {
                it.release()
                it.acquire(awakeTime)
            }
        }
        sendWakeScreen()
    }

    private fun publishMotionDetected() {
        Timber.d("publishMotionDetected")
        val delay = (configuration.motionResetTime * 1000).toLong()
        val data = JSONObject()
        try {
            data.put(VALUE, true)
        } catch (ex: JSONException) {
            ex.printStackTrace()
        }
        motionDetected = true
        publishState(COMMAND_SENSOR_MOTION, data)
        motionClearHandler.postDelayed({ clearMotionDetected() }, delay)
    }

    private fun publishFaceDetected() {
        Timber.d("publishFaceDetected")
        val data = JSONObject()
        try {
            data.put(VALUE, true)
        } catch (ex: JSONException) {
            ex.printStackTrace()
        }
        faceDetected = true
        publishState(COMMAND_SENSOR_FACE, data)
        faceClearHandler.postDelayed({ clearFaceDetected() }, 1000)
    }

    private fun clearMotionDetected() {
        Timber.d("Motion cleared")
        motionDetected = false
        val data = JSONObject()
        try {
            data.put(VALUE, false)
        } catch (ex: JSONException) {
            ex.printStackTrace()
        }
        publishState(COMMAND_SENSOR_MOTION, data)
    }

    private fun clearFaceDetected() {
        Timber.d("Face cleared")
        val data = JSONObject()
        try {
            data.put(VALUE, false)
        } catch (ex: JSONException) {
            ex.printStackTrace()
        }
        faceDetected = false
        publishState(COMMAND_SENSOR_FACE, data)
    }

    private fun clearQrCodeRead() {
        if (qrCodeRead) {
            qrCodeRead = false
        }
    }

    private fun publishQrCode(data: String) {
        if (!qrCodeRead) {
            Timber.d("publishQrCode")
            val jdata = JSONObject()
            try {
                jdata.put(VALUE, data)
            } catch (ex: JSONException) {
                ex.printStackTrace()
            }
            qrCodeRead = true
            sendToastMessage(getString(R.string.toast_qr_code_read))
            publishState(COMMAND_SENSOR_QR_CODE, jdata)
            qrCodeClearHandler.postDelayed({ clearQrCodeRead() }, 5000)
        }
    }

    private fun insertSun(payload: String) {
        Timber.d("insertSun $payload")
        disposable.add(Completable.fromAction {
            val sun = Sun()
            sun.sun = payload
            sun.createdAt = DateUtils.generateCreatedAtDate()
            sunDao.updateItem(sun)
        }.subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                }, { error -> Timber.e("Database error" + error.message) }))
    }

    private fun insertWeather(payload: String) {
        Timber.d("insertWeather")
        val gson = GsonBuilder().serializeNulls().create()
        try {
            val weather = gson.fromJson<Weather>(payload, Weather::class.java)
            disposable.add(Completable.fromAction {
                weather.createdAt = DateUtils.generateCreatedAtDate()
                weatherDao.updateItem(weather)
            }.subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                    }, { error -> Timber.e("Weather insert error" + error.message) }))
        } catch (error: Exception) {
            Timber.e("Weather parsing error" + error.message)
        }
    }

    private fun insertMessage(messageId: String, topic: String, payload: String) {
        Timber.d("insertMessage: " + topic)
        Timber.d("insertMessage: " + payload)
        val type = when (topic) {
            ComponentUtils.TOPIC_COMMAND -> ComponentUtils.TOPIC_COMMAND
            else -> AlarmUtils.ALARM_TYPE
        }
        disposable.add(Completable.fromAction {
            val createdAt = DateUtils.generateCreatedAtDate()
            val message = MessageMqtt()
            message.type = type
            message.topic = topic
            message.payload = payload
            message.messageId = messageId
            message.createdAt = createdAt
            messageDataSource.insertMessage(message)
        }.subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                }, { error -> Timber.e("Database error" + error.message) }))
    }

    /**
     * Capture and send image if user has setup this feature.
     */
    private fun captureImageTask() {
        if (configuration.captureCameraImage()) {
            val bitmapTask = BitmapTask(object : OnCompleteListener {
                override fun onComplete(bitmap: Bitmap?) {
                    if (bitmap != null) {
                        sendCapturedImage(bitmap)
                    }
                }
            })
            cameraReader?.getJpeg()?.let {
                bitmapTask.execute(it.value)
            }
        }
    }

    private fun sendAlertMessage(message: String) {
        Timber.d("sendAlertMessage")
        val intent = Intent(BROADCAST_ALERT_MESSAGE)
        intent.putExtra(BROADCAST_ALERT_MESSAGE, message)
        val bm = LocalBroadcastManager.getInstance(applicationContext)
        bm.sendBroadcast(intent)
    }

    private fun sendServiceStarted() {
        Timber.d("clearAlertMessage")
        val intent = Intent(BROADCAST_SERVICE_STARTED)
        val bm = LocalBroadcastManager.getInstance(applicationContext)
        bm.sendBroadcast(intent)
    }

    private fun clearAlertMessage() {
        Timber.d("clearAlertMessage")
        val intent = Intent(BROADCAST_CLEAR_ALERT_MESSAGE)
        val bm = LocalBroadcastManager.getInstance(applicationContext)
        bm.sendBroadcast(intent)
    }

    private fun sendWakeScreen() {
        Timber.d("sendWakeScreen")
        val intent = Intent(BROADCAST_SCREEN_WAKE)
        val bm = LocalBroadcastManager.getInstance(applicationContext)
        bm.sendBroadcast(intent)
    }

    private fun sendToastMessage(message: String) {
        Timber.d("sendToastMessage")
        val intent = Intent(BROADCAST_TOAST_MESSAGE)
        intent.putExtra(BROADCAST_TOAST_MESSAGE, message)
        val bm = LocalBroadcastManager.getInstance(applicationContext)
        bm.sendBroadcast(intent)
    }

    // receive messages from the activity
    private val mBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (BROADCAST_EVENT_URL_CHANGE == intent.action) {
                currentUrl = intent.getStringExtra(BROADCAST_EVENT_URL_CHANGE)
                publishState(COMMAND_STATE, state.toString())
            } else if (Intent.ACTION_SCREEN_OFF == intent.action ||
                    intent.action == Intent.ACTION_SCREEN_ON ||
                    intent.action == Intent.ACTION_USER_PRESENT) {
                Timber.i("Screen state changed")
                publishState(COMMAND_STATE, state.toString())
            } else if (BROADCAST_EVENT_SCREEN_TOUCH == intent.action) {
                Timber.i("Screen touched")
                publishState(COMMAND_STATE, state.toString())
            } else if (BROADCAST_EVENT_USER_INACTIVE == intent.action) {
                Timber.i("User Inactive")
            } else if (BROADCAST_EVENT_ALARM_MODE == intent.action) {
                val alarmMode = intent.getStringExtra(BROADCAST_EVENT_ALARM_MODE)
                Timber.i("Alarm Mode Changed $alarmMode")
                if (!TextUtils.isEmpty(alarmMode)) {
                    publishAlarm(alarmMode)
                }
            }
        }
    }

    private val sensorCallback = object : SensorCallback {
        override fun publishSensorData(sensorName: String, sensorData: JSONObject) {
            publishState(COMMAND_SENSOR_PREFIX + sensorName, sensorData)
        }
    }

    private val cameraDetectorCallback = object : CameraCallback {
        override fun onDetectorError() {
            sendToastMessage(getString(R.string.error_missing_vision_lib))
        }

        override fun onCameraError() {
            sendToastMessage(getString(R.string.toast_camera_source_error))
        }
        override fun onMotionDetected() {
            if (!motionDetected) {
                Timber.d("Motion detected cameraMotionWake ${configuration.cameraMotionWake}")
                if (configuration.cameraMotionWake) {
                    switchScreenOn(AWAKE_TIME)
                }
                publishMotionDetected()
            }
        }
        override fun onTooDark() {
            // Timber.i("Too dark for motion detection")
        }
        override fun onFaceDetected() {
            if (!faceDetected) {
                Timber.d("Face detected")
                if (configuration.cameraFaceWake) {
                    switchScreenOn(AWAKE_TIME)
                }
                publishFaceDetected()
            }
        }
        override fun onQRCode(data: String) {
            Timber.i("QR Code Received: $data")
            sendToastMessage(getString(R.string.toast_qr_code_read))
            publishQrCode(data)
        }
    }

    fun sendCapturedImage(bitmap: Bitmap) {
        if (configuration.hasMailGunCredentials()) {
            emailImage(bitmap)
        }
        if (configuration.hasTelegramCredentials()) {
            sendTelegram(bitmap)
        }
    }

    private fun sendTelegram(bitmap: Bitmap) {
        Timber.d("sendTelegram")
        val token = configuration.telegramToken
        val chatId = configuration.telegramChatId
        val observable = Observable.create { emitter: ObservableEmitter<Any> ->
            val module = TelegramModule(this@AlarmPanelService)
            module.emailImage(token, chatId, bitmap, object : TelegramModule.CallbackListener {
                override fun onComplete() {
                    emitter.onNext(true)  // Pass on the data to subscriber
                }
                override fun onException(message: String?) {
                    emitter.onError(Throwable(message))
                }
            })
        }
        disposable.add(observable
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext { Timber.d("Telegram Message posted successfully!"); }
                .doOnError { throwable -> Timber.e("Telegram Message error: " + throwable.message); }
                .subscribe())
    }

    private fun emailImage(bitmap: Bitmap) {
        val domain = configuration.getMailGunUrl()
        val key = configuration.getMailGunApiKey()
        val from = configuration.getMailFrom()
        val to = configuration.getMailTo()
        val observable = Observable.create { emitter: ObservableEmitter<Any> ->
            val mailGunModule = MailGunModule(this@AlarmPanelService)
            val fromSubject = getString(R.string.text_camera_image_subject, "<$from>")
            mailGunModule.emailImage(domain!!, key!!, fromSubject, to!!, bitmap, object : MailGunModule.CallbackListener {
                override fun onComplete() {
                    emitter.onNext(true)  // Pass on the data to subscriber
                }
                override fun onException(message: String?) {
                    if (message != null) {
                        try {
                            emitter.onError(Throwable(message))
                        } catch (e: UndeliverableException) {
                            Timber.e(e.message)
                        }
                    }
                }
            })
        }
        disposable.add(observable
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext { Timber.d("Image posted successfully!"); }
                .doOnError { throwable -> Timber.e("Image error: " + throwable.message); }
                .onErrorReturn {
                    sendToastMessage(getString(R.string.error_mailgun_credentials))
                }
                .subscribe())
    }

    interface OnCompleteListener {
        fun onComplete(bitmap: Bitmap?)
    }

    /**
     * Convert byte array to bitmap for disarmed image
     */
    class BitmapTask(private val onCompleteListener: OnCompleteListener) : AsyncTask<Any, Void, Bitmap>() {
        override fun doInBackground(vararg params: kotlin.Any): Bitmap? {
            if (isCancelled) {
                return null
            }
            try {
                val byteArray = params[0] as ByteArray
                val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size);
                return bitmap
            } catch (e: Exception) {
                return null
            }
        }

        override fun onPostExecute(result: Bitmap?) {
            super.onPostExecute(result)
            if (isCancelled) {
                return
            }
            onCompleteListener.onComplete(result)
        }
    }

    fun bringApplicationToForegroundIfNeeded() {
        if (!LifecycleHandler.isApplicationInForeground) {
            Timber.d("bringApplicationToForegroundIfNeeded")
            val intent = Intent("intent.alarm.action")
            intent.component = ComponentName(this@AlarmPanelService.packageName, MainActivity::class.java.name)
            intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }
    }

    companion object {
        const val AWAKE_TIME: Long = 30000 // 30 SECONDS
        const val TRIGGERED_AWAKE_TIME: Long = 10800000 // 3 HOURS
        const val ONGOING_NOTIFICATION_ID = 1
        const val BROADCAST_EVENT_URL_CHANGE = "BROADCAST_EVENT_URL_CHANGE"
        const val BROADCAST_EVENT_SCREEN_TOUCH = "BROADCAST_EVENT_SCREEN_TOUCH"
        const val BROADCAST_EVENT_USER_INACTIVE = "BROADCAST_EVENT_USER_INACTIVE"
        const val BROADCAST_EVENT_ALARM_MODE = "BROADCAST_EVENT_ALARM_MODE"
        const val BROADCAST_ALERT_MESSAGE = "BROADCAST_ALERT_MESSAGE"
        const val BROADCAST_TOAST_MESSAGE = "BROADCAST_TOAST_MESSAGE"
        const val BROADCAST_SCREEN_WAKE = "BROADCAST_SCREEN_WAKE"
        const val BROADCAST_CLEAR_ALERT_MESSAGE = "BROADCAST_CLEAR_ALERT_MESSAGE"
        const val BROADCAST_SERVICE_STARTED = "BROADCAST_SERVICE_STARTED"

    }
}