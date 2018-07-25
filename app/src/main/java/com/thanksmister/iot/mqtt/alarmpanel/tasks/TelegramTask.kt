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

package com.thanksmister.iot.mqtt.alarmpanel.tasks

import android.graphics.Bitmap
import com.thanksmister.iot.mqtt.alarmpanel.network.fetchers.MailGunFetcher
import com.thanksmister.iot.mqtt.alarmpanel.network.fetchers.TelegramFetcher
import org.json.JSONObject
import retrofit2.Response

class TelegramTask(private val fetcher: TelegramFetcher) : NetworkTask<kotlin.Any, Void, Response<JSONObject>>() {
    @Throws(Exception::class)
    override fun doNetworkAction(vararg params: kotlin.Any): Response<JSONObject> {
        if (params.size != 2) {
            throw Exception("Wrong number of params, expected 2, received " + params.size)
        }
        val text = params[0] as String
        val bitmap = params[1] as Bitmap
        val call = fetcher.sendMessage(text, bitmap)
        return call.execute()
    }
}