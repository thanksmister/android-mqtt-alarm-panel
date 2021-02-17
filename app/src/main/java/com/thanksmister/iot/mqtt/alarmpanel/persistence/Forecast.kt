/*
 * Copyright (c) 2019 ThanksMister LLC
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

package com.thanksmister.iot.mqtt.alarmpanel.persistence

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Forecast {

    @SerializedName("datetime")
    @Expose
    var datetime: String? = null

    @SerializedName("temperature")
    @Expose
    var temperature: Double? = null

    @SerializedName("templow")
    @Expose
    var templow: Double? = null

    @SerializedName("precipitation")
    @Expose
    var precipitation: String? = null

    @SerializedName("condition")
    @Expose
    var condition: String? = null

    @SerializedName("wind_bearing")
    @Expose
    var wind_bearing: String? = null

    @SerializedName("wind_speed")
    @Expose
    var wind_speed: String? = null
}