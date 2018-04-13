package com.thanksmister.iot.mqtt.alarmpanel.viewmodel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.graphics.Bitmap
import android.text.TextUtils
import android.widget.Toast
import com.thanksmister.iot.mqtt.alarmpanel.R
import com.thanksmister.iot.mqtt.alarmpanel.persistence.MessageMqtt
import com.thanksmister.iot.mqtt.alarmpanel.persistence.MessageDao
import com.thanksmister.iot.mqtt.alarmpanel.persistence.stores.StoreManager
import com.thanksmister.iot.mqtt.alarmpanel.tasks.NetworkTask
import com.thanksmister.iot.mqtt.alarmpanel.tasks.SubscriptionDataTask
import com.thanksmister.iot.mqtt.alarmpanel.ui.Configuration
import com.thanksmister.iot.mqtt.alarmpanel.ui.modules.MailGunModule
import com.thanksmister.iot.mqtt.alarmpanel.ui.modules.TelegramModule
import com.thanksmister.iot.mqtt.alarmpanel.utils.AlarmUtils
import com.thanksmister.iot.mqtt.alarmpanel.utils.AlarmUtils.Companion.ALARM_STATE_TOPIC
import com.thanksmister.iot.mqtt.alarmpanel.utils.AlarmUtils.Companion.ALARM_TYPE
import com.thanksmister.iot.mqtt.alarmpanel.utils.AlarmUtils.Companion.MODE_ARM_AWAY
import com.thanksmister.iot.mqtt.alarmpanel.utils.AlarmUtils.Companion.MODE_ARM_AWAY_PENDING
import com.thanksmister.iot.mqtt.alarmpanel.utils.AlarmUtils.Companion.MODE_ARM_HOME
import com.thanksmister.iot.mqtt.alarmpanel.utils.AlarmUtils.Companion.MODE_ARM_HOME_PENDING
import com.thanksmister.iot.mqtt.alarmpanel.utils.AlarmUtils.Companion.MODE_AWAY_TRIGGERED_PENDING
import com.thanksmister.iot.mqtt.alarmpanel.utils.AlarmUtils.Companion.MODE_HOME_TRIGGERED_PENDING
import com.thanksmister.iot.mqtt.alarmpanel.utils.AlarmUtils.Companion.MODE_TRIGGERED
import com.thanksmister.iot.mqtt.alarmpanel.utils.AlarmUtils.Companion.MODE_TRIGGERED_PENDING
import com.thanksmister.iot.mqtt.alarmpanel.utils.ComponentUtils.NOTIFICATION_TYPE
import com.thanksmister.iot.mqtt.alarmpanel.utils.DateUtils
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

class MessageViewModel @Inject
constructor(application: Application, private val dataSource: MessageDao, private val configuration: Configuration) : AndroidViewModel(application) {

    private var mailSubscription: Disposable? = null
    private var telegramSubscription: Disposable? = null

    private var armed: Boolean = false

    @AlarmUtils.AlarmStates
    private fun setAlarmModeFromState(state: String) {
        if(state == AlarmUtils.STATE_PENDING) {
            if (getAlarmMode().equals(MODE_ARM_HOME) || getAlarmMode().equals(MODE_ARM_AWAY)) {
                if (getAlarmMode().equals(MODE_ARM_HOME)){
                    setAlarmMode(MODE_HOME_TRIGGERED_PENDING);
                } else if(getAlarmMode().equals(MODE_ARM_AWAY)) {
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

    fun hasScreenSaver() : Boolean {
        return (configuration.showPhotoScreenSaver() || configuration.showClockScreenSaverModule())
    }

    fun hasCamera() : Boolean {
        return (configuration.hasCamera() && (configuration.hasMailGunCredentials() || configuration.hasTelegramCredentials()))
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

    fun isAlarmTriggeredMode(): Boolean {
        return getAlarmMode() == MODE_TRIGGERED
                || getAlarmMode() == MODE_HOME_TRIGGERED_PENDING
                || getAlarmMode() == MODE_AWAY_TRIGGERED_PENDING
                || getAlarmMode() == MODE_TRIGGERED_PENDING
    }

    fun isAlarmPendingMode(): Boolean {
        return (getAlarmMode() == MODE_ARM_AWAY_PENDING
                || getAlarmMode() == MODE_ARM_HOME_PENDING
                || getAlarmMode() == MODE_AWAY_TRIGGERED_PENDING
                || getAlarmMode() == MODE_HOME_TRIGGERED_PENDING)
    }

    fun isAlarmDisableMode(): Boolean {
        return (getAlarmMode() == MODE_ARM_HOME
                || getAlarmMode() == MODE_ARM_AWAY
                || getAlarmMode() == MODE_HOME_TRIGGERED_PENDING
                || getAlarmMode() == MODE_AWAY_TRIGGERED_PENDING
                || getAlarmMode() == MODE_TRIGGERED_PENDING)
    }

    fun isArmed(value: Boolean) {
        armed = value
    }

    fun isArmed(): Boolean {
        return armed
    }

    /**
     * Get the messages.
     * @return a [Flowable] that will emit every time the messages have been updated.
     */
    fun getMessages():Flowable<List<MessageMqtt>> {
        return dataSource.getMessages()
                .filter {messages -> messages.isNotEmpty()}
    }

    fun getAlarmState():Flowable<String> {
        return dataSource.getMessages(ALARM_TYPE)
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
    fun insertMessage(messageId: String,topic: String, payload: String): Completable {
        Timber.d("insertMessage: " + topic)
        Timber.d("insertMessage: " + payload)
        val type = if(ALARM_STATE_TOPIC == topic) {
            ALARM_TYPE
        } else {
            NOTIFICATION_TYPE
        }
        return Completable.fromAction {
            val createdAt = DateUtils.generateCreatedAtDate()
            val message = MessageMqtt()
            message.type = type
            message.topic = topic
            message.payload = payload
            message.messageId = messageId
            message.createdAt = createdAt
            dataSource.insertMessage(message)
        }
    }

    fun sendCapturedImage(bitmap: Bitmap) {
        if(configuration.hasMailGunCredentials()) {
            emailImage(bitmap)
        }
        if(configuration.hasTelegramCredentials()) {
            sendTelegram(bitmap)
        }
    }

    private fun sendTelegram(bitmap: Bitmap) {
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
    }

    @Deprecated("moving over to RxJava")
    fun getUpdateMqttDataTask(storeManager: StoreManager): SubscriptionDataTask {
        val dataTask = SubscriptionDataTask(storeManager)
        dataTask.setOnExceptionListener(object : NetworkTask.OnExceptionListener {
            override fun onException(paramException: Exception) {
                Timber.e("Update Exception: " + paramException.message)
            }
        })
        dataTask.setOnCompleteListener(object : NetworkTask.OnCompleteListener<Boolean> {
            override fun onComplete(paramResult: Boolean) {
                if ((!paramResult)) {
                    Timber.e("Update Exception response: " + paramResult)
                }
            }
        })
        return dataTask
    }

    /**
     * Network connectivity receiver to notify client of the network disconnect issues and
     * to clear any network notifications when reconnected. It is easy for network connectivity
     * to run amok that is why we only notify the user once for network disconnect with
     * a boolean flag.
     */
    companion object {

    }
}