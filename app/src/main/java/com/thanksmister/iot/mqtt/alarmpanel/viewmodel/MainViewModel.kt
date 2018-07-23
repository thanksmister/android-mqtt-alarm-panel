package com.thanksmister.iot.mqtt.alarmpanel.viewmodel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.graphics.Bitmap
import android.text.TextUtils
import com.thanksmister.iot.mqtt.alarmpanel.network.MQTTOptions
import com.thanksmister.iot.mqtt.alarmpanel.persistence.*
import com.thanksmister.iot.mqtt.alarmpanel.persistence.Configuration
import com.thanksmister.iot.mqtt.alarmpanel.utils.AlarmUtils
import com.thanksmister.iot.mqtt.alarmpanel.utils.AlarmUtils.Companion.ALARM_TYPE
import com.thanksmister.iot.mqtt.alarmpanel.utils.AlarmUtils.Companion.MODE_ARM_AWAY
import com.thanksmister.iot.mqtt.alarmpanel.utils.AlarmUtils.Companion.MODE_ARM_HOME
import com.thanksmister.iot.mqtt.alarmpanel.utils.AlarmUtils.Companion.MODE_AWAY_TRIGGERED_PENDING
import com.thanksmister.iot.mqtt.alarmpanel.utils.AlarmUtils.Companion.MODE_HOME_TRIGGERED_PENDING
import com.thanksmister.iot.mqtt.alarmpanel.utils.AlarmUtils.Companion.MODE_TRIGGERED
import com.thanksmister.iot.mqtt.alarmpanel.utils.AlarmUtils.Companion.MODE_TRIGGERED_PENDING
import com.thanksmister.iot.mqtt.alarmpanel.utils.ComponentUtils.Companion.NOTIFICATION_TYPE

import com.thanksmister.iot.mqtt.alarmpanel.utils.DateUtils
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

class MainViewModel @Inject
constructor(application: Application, private val messageDataSource: MessageDao, private val sensorDataSource: SensorDao, private val darkSkyDataSource: DarkSkyDao,
            private val configuration: Configuration, private val mqttOptions: MQTTOptions) : AndroidViewModel(application) {

    private var mailSubscription: Disposable? = null
    private var telegramSubscription: Disposable? = null
    private val disposable = CompositeDisposable()
    private var armed: Boolean = false

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

    @AlarmUtils.AlarmStates
    private fun setAlarmModeFromState(state: String) {
        if(state == AlarmUtils.STATE_PENDING) {
            if (getAlarmMode() == MODE_ARM_HOME || getAlarmMode() == MODE_ARM_AWAY) {
                if (getAlarmMode() == MODE_ARM_HOME){
                    setAlarmMode(MODE_HOME_TRIGGERED_PENDING);
                } else if(getAlarmMode() == MODE_ARM_AWAY) {
                    setAlarmMode(MODE_AWAY_TRIGGERED_PENDING);
                } else {
                    setAlarmMode(MODE_TRIGGERED_PENDING);
                }
            }
        } else if (state == AlarmUtils.STATE_TRIGGERED) {
            setAlarmMode(MODE_TRIGGERED)
        }
    }

    fun isFirstTime(): Boolean {
        return configuration.isFirstTime
    }

    fun hasPlatform() : Boolean {
        return (configuration.hasPlatformModule() && !TextUtils.isEmpty(configuration.webUrl))
    }

    fun hasCamera() : Boolean {
        return (configuration.hasCameraCapture() && (configuration.hasMailGunCredentials() || configuration.hasTelegramCredentials()))
    }

    fun hasTss() : Boolean {
        return configuration.hasTssModule()
    }

    fun hasAlerts() : Boolean {
        return configuration.hasAlertsModule()
    }

    fun setAlarmMode(value: String) {
        configuration.alarmMode = value;
    }

    fun getAlarmDelayTime(): Int {
        if(getAlarmMode() == MODE_ARM_AWAY || getAlarmMode() == MODE_HOME_TRIGGERED_PENDING) {
            return configuration.delayAwayTime
        } else if (getAlarmMode() == MODE_ARM_HOME || getAlarmMode() == MODE_AWAY_TRIGGERED_PENDING) {
            return configuration.delayHomeTime
        }
        return configuration.delayTime
    }

    fun getAlarmCode(): Int {
        return configuration.alarmCode
    }

    fun getAlarmMode(): String {
        return configuration.alarmMode
    }

    fun hasSystemAlerts(): Boolean {
        return configuration.hasSystemAlerts()
    }

    fun showSystemTriggeredAlert(): Boolean {
        return (getAlarmMode() == AlarmUtils.MODE_TRIGGERED) && hasSystemAlerts()
    }

    fun showSystemPendingAlert(): Boolean {
        return (getAlarmMode() == AlarmUtils.MODE_ARM_HOME || getAlarmMode() == AlarmUtils.MODE_ARM_AWAY) && hasSystemAlerts()
    }

    fun isArmed(value: Boolean) {
        armed = value
    }

    fun isArmed(): Boolean {
        return armed
    }

    /**
     * Get the sensor items.
     * @return a [Flowable]
     */
    fun getSensorItems(): Flowable<List<Sensor>> {
        return sensorDataSource.getItems()
                .filter { items -> items.isNotEmpty() }
    }

    /**
     * Insert new item into the database.
     */
    fun insertSensorItem(sensor: Sensor) {
        disposable.add(Completable.fromAction {
            sensorDataSource.insertItem(sensor)
        }
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                }, { error -> Timber.e("Database error" + error.message) }))

    }

    /**
     * Get the messages.
     * @return a [Flowable] that will emit every time the messages have been updated.
     */
    fun getMessages():Flowable<List<MessageMqtt>> {
        return messageDataSource.getMessages()
                .filter {messages -> messages.isNotEmpty()}
    }

    fun getAlarmState():Flowable<String> {
        return messageDataSource.getMessages(ALARM_TYPE)
                .filter {messages -> messages.isNotEmpty()}
                .map {messages -> messages[messages.size - 1]}
                .map {message ->
                    Timber.d("state: " + message.payload)
                    setAlarmModeFromState(message.payload!!)
                    message.payload
                }
    }

    init {

    }

    /**
     * Insert new message into the database.
     */
    fun insertMessage(messageId: String,topic: String, payload: String) {
        Timber.d("insertMessage: " + topic)
        Timber.d("insertMessage: " + payload)
        val type = when (topic) {
            mqttOptions.getNotificationTopic() -> NOTIFICATION_TYPE
            else -> ALARM_TYPE
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
        } .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                }, { error -> Timber.e("Database error" + error.message) }))
    }

    fun clearMessages():Completable {
        return Completable.fromAction {
            messageDataSource.deleteAllMessages()
        }
    }

    fun sendCapturedImage(bitmap: Bitmap) {
        if(configuration.hasMailGunCredentials()) {
            //emailImage(bitmap)
        }
        if(configuration.hasTelegramCredentials()) {
            //sendTelegram(bitmap)
        }
    }

    /*private fun sendTelegram(bitmap: Bitmap) {
        Timber.d("sendTelegram")
        val token = configuration.telegramToken
        val chatId = configuration.telegramChatId
        val observable = Observable.create { emitter: ObservableEmitter<Any> ->
            val module = TelegramModule(getApplication())
            module.emailImage(token, chatId, bitmap, object : TelegramModule.CallbackListener {
                override fun onComplete() {
                    emitter.onNext(true)  // Pass on the data to subscriber
                }
                override fun onException(message: String?) {
                    emitter.onError(Throwable(message))
                }
            })
        }

        telegramSubscription = observable
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext { Timber.d("Telegram Message posted successfully!"); }
                .doOnError({ throwable -> Timber.e("Telegram Message error: " + throwable.message); })
                .subscribe( );
    }

    private fun emailImage(bitmap: Bitmap) {

        val domain = configuration.getMailGunUrl()
        val key = configuration.getMailGunApiKey()
        val from = configuration.getMailFrom()
        val to = configuration.getMailTo()

        val observable = Observable.create { emitter: ObservableEmitter<Any> ->
            val mailGunModule = MailGunModule(getApplication())
            val fromSubject = getApplication<Application>().getString(R.string.text_camera_image_subject, "<$from>")
            mailGunModule.emailImage(domain!!, key!!, fromSubject, to!!, bitmap, object : MailGunModule.CallbackListener {
                override fun onComplete() {
                    emitter.onNext(true)  // Pass on the data to subscriber
                }
                override fun onException(message: String?) {
                    emitter.onError(Throwable(message))
                }
            })
        }

        mailSubscription = observable
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext { Timber.d("Image posted successfully!"); }
                .doOnError({ throwable -> Timber.e("Image error: " + throwable.message); })
                .onErrorReturn { Toast.makeText(getApplication<Application>(), R.string.error_mailgun_credentials, Toast.LENGTH_LONG).show() }
                .subscribe( );
    }*/

    public override fun onCleared() {
        //prevents memory leaks by disposing pending observable objects
        if (!disposable.isDisposed) {
            disposable.clear()
        }
    }

    companion object {

    }
}