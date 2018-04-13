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

package com.thanksmister.iot.mqtt.alarmpanel.ui.modules

import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.os.AsyncTask
import com.thanksmister.iot.mqtt.alarmpanel.R

import com.thanksmister.iot.mqtt.alarmpanel.network.MailGunApi
import com.thanksmister.iot.mqtt.alarmpanel.network.TelegramApi
import com.thanksmister.iot.mqtt.alarmpanel.network.fetchers.MailGunFetcher
import com.thanksmister.iot.mqtt.alarmpanel.network.fetchers.TelegramFetcher
import com.thanksmister.iot.mqtt.alarmpanel.tasks.MailGunTask
import com.thanksmister.iot.mqtt.alarmpanel.tasks.NetworkTask
import com.thanksmister.iot.mqtt.alarmpanel.tasks.TelegramTask
import org.json.JSONObject

import retrofit2.Response
import timber.log.Timber
import java.io.Closeable

/**
 * We lazily load the hourly forecast and the extended forecast.  We could have done this on a syncadapter or alarm, but
 * we only care that this module runs while we are in the application.
 */
class TelegramModule(base: Context?) : Closeable, ContextWrapper(base) {

    override fun close() {
        if (task != null) {
            task!!.cancel(true)
            task = null
        }
    }

    private var task: TelegramTask? = null
    private var token: String? = null
    private var chat_id: String? = null
    private var callback: CallbackListener? = null

    interface CallbackListener {
        fun onComplete()
        fun onException(message: String?)
    }

    fun emailImage(token:String, chat_id:String, bitmap: Bitmap, listener: CallbackListener?) {
        this.token = token
        this.chat_id = chat_id
        this.callback = listener
        sendMessage(token, chat_id, bitmap)
    }

    private fun sendMessage(token:String, chat_id: String, bitmap: Bitmap) {

        if (task != null && task!!.status == AsyncTask.Status.RUNNING) {
            return  // we have a running task already
        }

        val api = TelegramApi(token, chat_id)
        val fetcher = TelegramFetcher(api)

        task = TelegramTask(fetcher)
        task!!.setOnExceptionListener(object : NetworkTask.OnExceptionListener {
            override fun onException(paramException: Exception) {
                Timber.e("Telegram Exception: " + paramException.message)
                if(callback != null) {
                    callback!!.onException(paramException.message)
                }
            }
        })
        task!!.setOnCompleteListener(object : NetworkTask.OnCompleteListener<Response<JSONObject>> {
            override fun onComplete(paramResult: Response<JSONObject>) {
                Timber.d("Response: " + paramResult.body())
                if(callback != null) {
                    callback!!.onComplete()
                }
            }
        })

        task!!.execute(getString(R.string.text_alarm_disabled_email), bitmap)
    }
}