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

package com.thanksmister.iot.mqtt.alarmpanel.network.fetchers

import android.graphics.Bitmap
import com.thanksmister.iot.mqtt.alarmpanel.network.MailGunApi
import org.json.JSONObject
import retrofit2.Call

class MailGunFetcher(private val networkApi: MailGunApi) {

    fun emailImage(from: String, to: String, subject: String, text: String, bitmap: Bitmap): Call<JSONObject> {
        return networkApi.emailImages(from, to, subject, text, bitmap)
    }
}