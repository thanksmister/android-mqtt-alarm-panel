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

package com.thanksmister.iot.mqtt.alarmpanel.tasks

import android.graphics.Bitmap
import com.thanksmister.iot.mqtt.alarmpanel.network.fetchers.MailGunFetcher
import org.json.JSONObject
import retrofit2.Response

class MailGunTask(private val fetcher: MailGunFetcher) : NetworkTask<kotlin.Any, Void, Response<JSONObject>>() {
    @Throws(Exception::class)
    override fun doNetworkAction(vararg params: kotlin.Any): Response<JSONObject> {
        if (params.size != 5) {
            throw Exception("Wrong number of params, expected 5, received " + params.size)
        }
        val from = params[0] as String
        val to = params[1] as String
        val subject = params[2] as String
        val text = params[3] as String
        val bitmap = params[4] as Bitmap
        val call = fetcher.emailImage(from, to, subject, text, bitmap)
        return call.execute()
    }
}