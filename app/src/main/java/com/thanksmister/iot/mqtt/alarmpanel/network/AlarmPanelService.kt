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
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.net.wifi.WifiManager
import android.os.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.Observer
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
import com.thanksmister.iot.mqtt.alarmpanel.utils.*
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils.Companion.COMMAND_ALERT
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils.Companion.COMMAND_ARM_AWAY
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils.Companion.COMMAND_ARM_CUSTOM_BYPASS
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils.Companion.COMMAND_ARM_HOME
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils.Companion.COMMAND_ARM_NIGHT
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils.Companion.COMMAND_AUDIO
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils.Companion.COMMAND_CAPTURE
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils.Companion.COMMAND_DASHBOARD
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils.Companion.COMMAND_DISARM
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils.Companion.COMMAND_NOTIFICATION
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils.Companion.COMMAND_SENSOR_FACE
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils.Companion.COMMAND_SENSOR_MOTION
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils.Companion.COMMAND_SENSOR_PREFIX
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils.Companion.COMMAND_SENSOR_QR_CODE
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils.Companion.COMMAND_SPEAK
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils.Companion.COMMAND_STATE
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils.Companion.COMMAND_SUN
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils.Companion.COMMAND_WAKE
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils.Companion.COMMAND_WEATHER
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils.Companion.EVENT_ARM_FAILED
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils.Companion.EVENT_COMMAND_NOT_ALLOWED
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils.Companion.EVENT_INVALID_CODE
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils.Companion.EVENT_NO_CODE
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils.Companion.EVENT_SYSTEM_DISABLED
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils.Companion.EVENT_UNKNOWN
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils.Companion.STATE_ARM_AWAY
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils.Companion.STATE_ARM_CUSTOM_BYPASS
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils.Companion.STATE_ARM_HOME
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils.Companion.STATE_ARM_NIGHT
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils.Companion.STATE_BRIGHTNESS
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils.Companion.STATE_CURRENT_URL
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils.Companion.STATE_DISARM
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils.Companion.STATE_PRESENCE
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils.Companion.STATE_SCREEN_ON
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils.Companion.TYPE_ALARM
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils.Companion.TYPE_COMMAND
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils.Companion.TYPE_EVENT
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils.Companion.VALUE
import dagger.android.AndroidInjection
import io.reactivex.*
import io.reactivex.Observable
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
    lateinit var sensorDataSource: SensorDao

    @Inject
    lateinit var weatherDao: WeatherDao

    @Inject
    lateinit var sunDao: SunDao

    @Inject
    lateinit var screenUtils: ScreenUtils

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
    private var userPresence = AtomicBoolean(false)

    private val restartMQTTRunnable = Runnable {
        clearAlertMessage() // clear any dialogs
        mqttAlertMessageShown = false
        mqttConnecting = false
        sendToastMessage(getString(R.string.toast_connect_retry))
        mqttModule?.restart()
    }

    private val alarmPanelService: Intent by lazy {
        Intent(this, AlarmPanelService::class.java)
    }

    inner class AlarmPanelServiceBinder : Binder() {
        val service: AlarmPanelService
            get() = this@AlarmPanelService
    }

    override fun onCreate() {
        super.onCreate()

        AndroidInjection.inject(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val notification = notifications.createOngoingNotification(getString(R.string.app_name), getString(R.string.service_notification_message))
                startForeground(ONGOING_NOTIFICATION_ID, notification)
            } catch (ignored: RuntimeException) {
                ContextCompat.startForegroundService(this, alarmPanelService)
            }
        }

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
        clearDatabasePeriodically()

        val filter = IntentFilter()
        filter.addAction(BROADCAST_EVENT_URL_CHANGE)
        filter.addAction(BROADCAST_EVENT_SCREEN_TOUCH)
        filter.addAction(BROADCAST_EVENT_ALARM_MODE)
        filter.addAction(BROADCAST_EVENT_USER_INACTIVE)
        filter.addAction(BROADCAST_EVENT_PUBLISH_PANIC)
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

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return mBinder
    }

    private val isScreenOn: Boolean
        get() {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH && powerManager.isInteractive || Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT_WATCH && powerManager.isScreenOn
        }

    private fun getState(userActive: Boolean): JSONObject {
        val state = JSONObject()
        state.put(STATE_CURRENT_URL, currentUrl)
        state.put(STATE_SCREEN_ON, isScreenOn)
        state.put(STATE_BRIGHTNESS, screenUtils.getCurrentScreenBrightness())
        state.put(STATE_PRESENCE, userActive.toString())
        return state
    }

    private fun startForegroundService() {
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
        mqttModule?.let {
            if (!hasNetwork.get()) {
                it.restart()
            }
        }
        hasNetwork.set(true)
    }

    private fun handleNetworkDisconnect() {
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
            this.publishCommand(COMMAND_STATE, getState(true).toString())
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
        if (hasNetwork()) {
            mqttConnected = false
            if (mqttInitConnection.get()) {
                mqttInitConnection.set(false)
                sendSnackMessage(getString(R.string.error_mqtt_exception))
                mqttAlertMessageShown = true
            }
            if (!mqttConnecting) {
                reconnectHandler.postDelayed(restartMQTTRunnable, 30000)
                mqttConnecting = true
            }
        }
    }

    // TODO test if payload is json and handl
    override fun onMQTTMessage(id: String, topic: String, payload: String) {
        Timber.i("onMQTTMessage topic: $topic")
        if (mqttOptions.getAlarmStateTopic() == topic || mqttOptions.getAlarmCommandTopic() == topic) {
            val state = MqttUtils.parseStateFromJson(payload)
            val delay = MqttUtils.parseDelayFromJson(payload)
            when (state) {
                MqttUtils.STATE_ARMING -> {
                    if (configuration.hasSystemAlerts()) {
                        notifications.clearNotification()
                    }
                    insertMessage(id, topic, payload, TYPE_ALARM, delay)
                }
                MqttUtils.STATE_DISARMED -> {
                    if (configuration.hasSystemAlerts()) {
                        notifications.clearNotification()
                    }
                    insertMessage(id, topic, payload, TYPE_ALARM, delay)
                }
                MqttUtils.STATE_ARMED_AWAY,
                MqttUtils.STATE_ARMED_NIGHT,
                MqttUtils.STATE_ARMED_HOME -> {
                    insertMessage(id, topic, payload, TYPE_ALARM, delay)
                }
                STATE_ARM_CUSTOM_BYPASS,
                STATE_ARM_NIGHT,
                STATE_ARM_AWAY,
                STATE_ARM_HOME,
                STATE_DISARM,
                COMMAND_ARM_CUSTOM_BYPASS,
                COMMAND_ARM_NIGHT,
                COMMAND_ARM_AWAY,
                COMMAND_DISARM,
                COMMAND_ARM_HOME -> {
                    insertMessage(id, topic, payload, TYPE_ALARM, delay)
                }
                MqttUtils.STATE_TRIGGERED -> {
                    if (configuration.alarmMode == MqttUtils.STATE_TRIGGERED && configuration.hasSystemAlerts()) {
                        notifications.createAlarmNotification(getString(R.string.text_notification_trigger_title), getString(R.string.text_notification_trigger_description))
                    }
                    insertMessage(id, topic, payload, TYPE_ALARM, delay)
                }
                MqttUtils.STATE_PENDING -> {
                    if (configuration.isAlarmArmedMode() && configuration.hasSystemAlerts()) {
                        notifications.createAlarmNotification(getString(R.string.text_notification_entry_title), getString(R.string.text_notification_entry_description))
                    }
                    insertMessage(id, topic, payload, TYPE_ALARM, delay)
                }
            }
        } else if (mqttOptions.getAlarmEventTopic() == topic) {
            processEvent(id, topic, payload)
        } else if (topic.contains(mqttOptions.getAlarmSensorsTopic())) {
            processSensor(topic, payload)
        } else {
            processCommand(id, topic, payload)
        }
    }

    private fun publishAlarm(action: String, code: Int) {
        mqttModule?.let {
            it.publishAlarm(action, code)
            if (action == COMMAND_DISARM) {
                captureImageTask()
            }
        }
    }

    /**
     * Publish command with json formatted payload.
     */
    private fun publishCommand(command: String, data: JSONObject) {
        this.publishCommand(command, data.toString())
    }

    /**
     * Publish command with default payload.
     */
    private fun publishCommand(command: String, message: String) {
        mqttModule?.let {
            it.publishCommand(command, message)
        }
    }

    /**
     * Configure the camera with multiple detections (face, motion, qrcode, mjpeg stream) or
     * just set up the camera to take a capture a photo on alarm disarm
     */
    private fun configureCamera() {
        if (configuration.hasCameraDetections() || configuration.captureCameraImage()) {
            cameraReader = CameraReader(this.applicationContext)
            cameraReader?.startCamera(cameraDetectorCallback, configuration)
        }
    }

    private fun configureTextToSpeech() {
        if (textToSpeechModule == null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            textToSpeechModule = TextToSpeechModule(this)
            lifecycle.addObserver(textToSpeechModule!!)
        }
    }

    private fun configureAudioPlayer() {
        audioPlayer = MediaPlayer()
        audioPlayer?.setOnPreparedListener { audioPlayer ->
            audioPlayerBusy = false
            audioPlayer.start()
        }
        audioPlayer?.setOnCompletionListener { audioPlayer ->
            if (audioPlayer.isPlaying) {  // should never happen, just in case
                audioPlayer.stop()
            }
            audioPlayer.reset()
            audioPlayerBusy = false
        }
        audioPlayer!!.setOnErrorListener { audioPlayer, i, i1 ->
            audioPlayerBusy = false
            false
        }
    }

    private fun startHttp() {
        if (httpServer == null && configuration.httpMJPEGEnabled) {
            httpServer = AsyncHttpServer()
            if (configuration.httpMJPEGEnabled) {
                startMJPEG()
                httpServer?.addAction("GET", "/camera/stream") { _, response ->
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
        }
    }

    private fun stopHttp() {
        httpServer?.let {
            stopMJPEG()
            it.stop()
            httpServer = null
        }
    }

    private fun startMJPEG() {
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
        mJpegSockets.clear()
    }

    private fun startMJPEG(response: AsyncHttpServerResponse) {
        if (mJpegSockets.size < configuration.httpMJPEGMaxStreams) {
            response.headers.add("Cache-Control", "no-cache")
            response.headers.add("Connection", "close")
            response.headers.add("Pragma", "no-cache")
            response.setContentType("multipart/x-mixed-replace; boundary=--jpgboundary")
            response.code(200)
            response.writeHead()
            mJpegSockets.add(response)
        } else {
            response.send("Max streams exceeded")
            response.end()
        }
    }

    private fun processCommand(id: String, topic: String, commandJson: JSONObject) {
        var payload = ""
        try {
            if (commandJson.has(COMMAND_WAKE)) {
                payload = commandJson.getString(COMMAND_WAKE)
                insertMessage(id, topic, commandJson.toString(), TYPE_COMMAND)
                switchScreenOn(AWAKE_TIME)
            }
            if (commandJson.has(COMMAND_DASHBOARD)) {
                payload = commandJson.getString(COMMAND_DASHBOARD)
                insertMessage(id, topic, commandJson.toString(), TYPE_COMMAND)
                val dashboard = payload.toIntOrNull()
                dashboard?.let {
                    browseDashboard(dashboard = it)
                }
            }
            if (commandJson.has(COMMAND_AUDIO)) {
                payload = commandJson.getString(COMMAND_AUDIO)
                insertMessage(id, topic, commandJson.toString(), TYPE_COMMAND)
                playAudio(payload)
            }
            if (commandJson.has(COMMAND_SPEAK)) {
                payload = commandJson.getString(COMMAND_SPEAK)
                insertMessage(id, topic, commandJson.toString(), TYPE_COMMAND)
                speakMessage(payload)
            }
            if (commandJson.has(COMMAND_NOTIFICATION)) {
                payload = commandJson.getString(COMMAND_NOTIFICATION)
                insertMessage(id, topic, commandJson.toString(), TYPE_COMMAND)
                val title = getString(R.string.notification_title)
                notifications.createAlarmNotification(title, payload)
            }
            if (commandJson.has(COMMAND_ALERT)) {
                payload = commandJson.getString(COMMAND_ALERT)
                insertMessage(id, topic, commandJson.toString(), TYPE_COMMAND)
                sendAlertMessage(payload)
            }
            if (commandJson.has(COMMAND_CAPTURE)) {
                payload = commandJson.getString(COMMAND_CAPTURE)
                insertMessage(id, topic, commandJson.toString(), TYPE_COMMAND)
                captureImageTask()
            }
            if (commandJson.has(COMMAND_WEATHER) && configuration.showWeatherModule()) {
                payload = commandJson.getString(COMMAND_WEATHER)
                insertWeather(payload)
            }
            if (commandJson.has(COMMAND_SUN)) {
                payload = commandJson.getString(COMMAND_SUN)
                if (payload.isNotEmpty()) {
                    insertSun(payload)
                }
            }
        } catch (ex: JSONException) {
            Timber.e("JSON Error: " + ex.message)
            Timber.e("Invalid JSON passed as a command: " + commandJson.toString())
        }
    }

    private fun browseDashboard(dashboard: Int) {
        Timber.d("browseUrl")
        val intent = Intent(BROADCAST_DASHBOARD)
        intent.putExtra(BROADCAST_DASHBOARD, dashboard)
        val bm = LocalBroadcastManager.getInstance(applicationContext)
        bm.sendBroadcast(intent)
    }

    // Process the status state
    /**
     * This method processes events for error and alternative state publishing.
     */
    private fun processEvent(id: String, topic: String, payload: String) {
        Timber.d("processEvent")
        if (MqttUtils.isJSONValid(payload)) {
            val json = MqttUtils.parseJSONObjectOrEmpty(payload)
            json?.let {
                if (json.has("event")) {
                    val event: String = json.getString("event").toLowerCase(Locale.getDefault())
                    when (event) {
                        EVENT_NO_CODE -> {
                            sendSnackMessage(getString(R.string.error_no_code))
                            insertMessage(id, topic, event, TYPE_EVENT)
                        }
                        EVENT_SYSTEM_DISABLED -> {
                            sendSnackMessage(getString(R.string.error_system_disabled))
                            insertMessage(id, topic, event, TYPE_EVENT)
                        }
                        EVENT_COMMAND_NOT_ALLOWED -> {
                            sendSnackMessage(getString(R.string.error_command_not_allowed))
                            insertMessage(id, topic, event, TYPE_EVENT)
                        }
                        EVENT_INVALID_CODE -> {
                            sendSnackMessage(getString(R.string.error_invalid_code))
                            insertMessage(id, topic, event, TYPE_EVENT)
                        }
                        EVENT_UNKNOWN -> {
                            sendSnackMessage(getString(R.string.error_unknown))
                            insertMessage(id, topic, event, TYPE_EVENT)
                        }
                        EVENT_ARM_FAILED -> {
                            sendSnackMessage(getString(R.string.error_arm_failed))
                            insertMessage(id, topic, event, TYPE_EVENT)
                        }
                        STATE_ARM_HOME,
                        STATE_ARM_AWAY,
                        STATE_ARM_NIGHT,
                        STATE_ARM_CUSTOM_BYPASS,
                        COMMAND_ARM_HOME,
                        COMMAND_ARM_AWAY,
                        COMMAND_ARM_NIGHT,
                        COMMAND_ARM_CUSTOM_BYPASS -> {
                            val delay = MqttUtils.parseDelayFromJson(payload)
                            insertMessage(id, topic, event, TYPE_ALARM, delay)
                        }
                    }
                }
            }
        }
    }

    private fun processCommand(id: String, topic: String, command: String) {
        Timber.d("processCommand")
        try {
            processCommand(id, topic, JSONObject(command))
        } catch (ex: JSONException) {
            Timber.e("JSON Error: " + ex.message)
            Timber.e("Invalid JSON passed as a command: $command")
        }
    }

    /**
     * Processes the remote config file from the payload as json and sets the values
     * on the mqtt configuration. A restart is most likely needed to set all the
     * remote config changes.
     *
     * {"version": 1,
     * "code_arm_required": false,
     * "code_disarm_required": true,
     * "state_topic": "home/alarm",
     * "status_topic": "home/alarm/status",
     * "command_topic": "home/alarm/set",
     * "delay_times": {"disarmed": 20, "armed_away": 20, "armed_home": 0, "armed_night": 20},
     * "arming_times": {"armed_away": 30, "armed_home": 0, "armed_night": 30},
     * "trigger_times": {"disarmed": 0, "armed_away": 4, "armed_home": 4, "armed_night": 4}}
     */
    private fun processConfig(payload: String) {
        try {
            val configJson = JSONObject(payload)
            if (configJson.has("state_topic")) {
                mqttOptions.setAlarmTopic(configJson.getString("state_topic"))
            }
            if (configJson.has("command_topic")) {
                mqttOptions.setCommandTopic(configJson.getString("command_topic"))
            }
            if (configJson.has("status_topic")) {
                mqttOptions.setAlarmEventTopic = configJson.getString("status_topic")
            }
            if (configJson.has("code_arm_required")) {
                mqttOptions.requireCodeForArming = configJson.getBoolean("code_arm_required")
            }
            if (configJson.has("code_disarm_required")) {
                mqttOptions.requireCodeForDisarming = configJson.getBoolean("code_disarm_required")
            }
            if (configJson.has("arming_times")) {
                val armingTimesJson = configJson.getJSONObject("arming_times")
                if (armingTimesJson.has("armed_away")) {
                    mqttOptions.remoteArmingAwayTime = configJson.getInt("armed_away")
                }
                if (armingTimesJson.has("armed_home")) {
                    mqttOptions.remoteArmingHomeTime = configJson.getInt("armed_home")
                }
                if (armingTimesJson.has("armed_night")) {
                    mqttOptions.remoteArmingNightTime = configJson.getInt("armed_night")
                }
            }
        } catch (ex: JSONException) {
            Timber.e("JSON Error: " + ex.message)
            Timber.e("Invalid config JSON: $payload")
            sendSnackMessage(getString(R.string.toast_config_invalid))
        }
    }

    private fun playAudio(audioUrl: String) {
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Timber.d("speakMessage $message")
            textToSpeechModule?.speakText(message)
        }
    }

    @SuppressLint("WakelockTimeout")
    private fun switchScreenOn(awakeTime: Long) {
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
        val delay = (configuration.motionResetTime * 1000).toLong()
        val data = JSONObject()
        data.put(VALUE, true)
        motionDetected = true
        publishCommand(COMMAND_SENSOR_MOTION, data)
        motionClearHandler.postDelayed({ clearMotionDetected() }, delay)
    }

    private fun publishFaceDetected() {
        val data = JSONObject()
        data.put(VALUE, true)
        faceDetected = true
        publishCommand(COMMAND_SENSOR_FACE, data)
        faceClearHandler.postDelayed({ clearFaceDetected() }, 1000)
    }

    private fun clearMotionDetected() {
        motionDetected = false
        val data = JSONObject()
        data.put(VALUE, false)
        publishCommand(COMMAND_SENSOR_MOTION, data)
    }

    private fun clearFaceDetected() {
        val data = JSONObject()
        data.put(VALUE, false)
        faceDetected = false
        publishCommand(COMMAND_SENSOR_FACE, data)
    }

    private fun clearQrCodeRead() {
        if (qrCodeRead) {
            qrCodeRead = false
        }
    }

    private fun publishQrCode(data: String) {
        if (!qrCodeRead) {
            val jdata = JSONObject()
            try {
                jdata.put(VALUE, data)
            } catch (ex: JSONException) {
                ex.printStackTrace()
            }
            qrCodeRead = true
            sendToastMessage(getString(R.string.toast_qr_code_read))
            publishCommand(COMMAND_SENSOR_QR_CODE, jdata)
            qrCodeClearHandler.postDelayed({ clearQrCodeRead() }, 5000)
        }
    }

    private fun insertSun(payload: String) {
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

    /**
     * We want to clean the database periodically so it does not grow too large
     */
    private fun clearDatabasePeriodically() {
        val handler = Handler()
        val runnable: Runnable = object : Runnable {
            override fun run() {
                try {
                    messageDataSource.deleteAllMessages()
                } catch (e: Exception) {
                    // TODO: handle exception
                } finally {
                    handler.postDelayed(this, 259200000L)
                }
            }
        }
        handler.post(runnable)
    }

    /**
     * Parses and updates existing sensor in the database with new payload.
     */
    private fun processSensor(topic: String, payload: String) {
        Timber.d("insertMessage topic: $topic")
        Timber.d("insertMessage payload: $payload")
        val sensorTopic = topic.replace(mqttOptions.getAlarmSensorsTopic() + "/", "", true)
        disposable.add(getSensor(sensorTopic)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    it?.let {
                        updateSensor(payload, it)
                    }
                })
    }

    private fun updateSensor(payload: String, sensor: Sensor) {
        disposable.add(Completable.fromAction {
            sensor.payload = payload.toLowerCase(Locale.getDefault())
            sensorDataSource.insertItem(sensor)
        }
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                }, {
                    error -> Timber.e("Database error" + error.message)
                }))
    }

    private fun getSensor(topic: String): Maybe<Sensor> {
        return sensorDataSource.getSensorByTopic(topic)
    }

    private fun insertMessage(messageId: String, topic: String, payload: String, type: String, delay: Int = -1) {
        Timber.d("insertMessage topic: $topic")
        Timber.d("insertMessage payload: $payload")
        disposable.add(Completable.fromAction {
            val createdAt = DateUtils.generateCreatedAtDate()
            val message = MessageMqtt()
            message.type = type
            message.topic = topic
            message.payload = payload.toLowerCase(Locale.getDefault())
            message.messageId = messageId
            message.messageId = messageId
            message.delay = delay
            message.createdAt = createdAt
            messageDataSource.insertMessageTransaction(message)
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

    private fun sendSnackMessage(message: String) {
        Timber.d("sendSnackMessage")
        val intent = Intent(BROADCAST_SNACK_MESSAGE)
        intent.putExtra(BROADCAST_SNACK_MESSAGE, message)
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
                this@AlarmPanelService.publishCommand(COMMAND_STATE, getState(userPresence.get()).toString())
            } else if (Intent.ACTION_SCREEN_OFF == intent.action ||
                    intent.action == Intent.ACTION_SCREEN_ON ||
                    intent.action == Intent.ACTION_USER_PRESENT) {
                if (intent.action == Intent.ACTION_USER_PRESENT) {
                    userPresence.set(true)
                }
                this@AlarmPanelService.publishCommand(COMMAND_STATE, getState(userPresence.get()).toString())
            } else if (BROADCAST_EVENT_SCREEN_TOUCH == intent.action) {
                userPresence.set(true)
                this@AlarmPanelService.publishCommand(COMMAND_STATE, getState(userPresence.get()).toString())
            } else if (BROADCAST_EVENT_USER_INACTIVE == intent.action) {
                userPresence.set(false)
                this@AlarmPanelService.publishCommand(COMMAND_STATE, getState(userPresence.get()).toString())
            } else if (BROADCAST_EVENT_ALARM_MODE == intent.action) {
                val alarmMode = intent.getStringExtra(BROADCAST_EVENT_ALARM_MODE).orEmpty()
                var alarmCode = intent.getStringExtra(BROADCAST_EVENT_ALARM_CODE).orEmpty().toIntOrNull()
                if (alarmCode == null) alarmCode = 0
                publishAlarm(alarmMode, alarmCode)
            } else if (BROADCAST_EVENT_PUBLISH_PANIC == intent.action) {
                val mode = intent.getStringExtra(BROADCAST_EVENT_PUBLISH_PANIC).orEmpty()
                var alarmCode = 0
                if (mqttOptions.useRemoteCode) {
                    alarmCode = -1
                }
                publishAlarm(mode, alarmCode)
            }
        }
    }

    private val sensorCallback = object : SensorCallback {
        override fun publishSensorData(sensorName: String, sensorData: JSONObject) {
            publishCommand(COMMAND_SENSOR_PREFIX + sensorName, sensorData)
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
        const val ONGOING_NOTIFICATION_ID = 1
        const val BROADCAST_EVENT_PUBLISH_PANIC = "BROADCAST_EVENT_PUBLISH_ALERT"
        const val BROADCAST_EVENT_URL_CHANGE = "BROADCAST_EVENT_URL_CHANGE"
        const val BROADCAST_EVENT_SCREEN_TOUCH = "BROADCAST_EVENT_SCREEN_TOUCH"
        const val BROADCAST_EVENT_USER_INACTIVE = "BROADCAST_EVENT_USER_INACTIVE"
        const val BROADCAST_EVENT_ALARM_MODE = "BROADCAST_EVENT_ALARM_MODE"
        const val BROADCAST_ALARM_COMMAND = "BROADCAST_ALARM_COMMAND"
        const val BROADCAST_ALARM_DELAY = "BROADCAST_ALARM_DELAY"
        const val BROADCAST_EVENT_ALARM_CODE = "BROADCAST_EVENT_ALARM_CODE"
        const val BROADCAST_ALERT_MESSAGE = "BROADCAST_ALERT_MESSAGE"
        const val BROADCAST_SNACK_MESSAGE = "BROADCAST_SNACK_MESSAGE"
        const val BROADCAST_TOAST_MESSAGE = "BROADCAST_TOAST_MESSAGE"
        const val BROADCAST_SCREEN_WAKE = "BROADCAST_SCREEN_WAKE"
        const val BROADCAST_CLEAR_ALERT_MESSAGE = "BROADCAST_CLEAR_ALERT_MESSAGE"
        const val BROADCAST_SERVICE_STARTED = "BROADCAST_SERVICE_STARTED"
        const val BROADCAST_DASHBOARD = "BROADCAST_ACTION_LOAD_URL"
    }
}