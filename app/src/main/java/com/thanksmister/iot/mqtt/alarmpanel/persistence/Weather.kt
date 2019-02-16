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

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.arch.persistence.room.TypeConverters
import com.google.gson.annotations.SerializedName
import com.google.gson.annotations.Expose

@Entity(tableName = "Weather")
class Weather {

    @PrimaryKey(autoGenerate = true)
    var uid: Int = 0

    @ColumnInfo(name = "createdAt")
    var createdAt: String? = null

    @ColumnInfo(name = "temperature")
    @SerializedName("temperature")
    var temperature: Double? = null

    @ColumnInfo(name = "humidity")
    @SerializedName("humidity")
    var humidity: Double? = null

    @ColumnInfo(name = "ozone")
    @SerializedName("ozone")
    var ozone: Double? = null

    @ColumnInfo(name = "pressure")
    @SerializedName("pressure")
    var pressure: Double? = null

    @ColumnInfo(name = "wind_bearing")
    @SerializedName("wind_bearing")
    var windBearing: Double? = null

    @ColumnInfo(name = "wind_speed")
    @SerializedName("wind_speed")
    var windSpeed: Double? = null

    @ColumnInfo(name = "visibility")
    @SerializedName("visibility")
    var visibility: Double? = null

    @ColumnInfo(name = "attribution")
    @SerializedName("attribution")
    var attribution: String? = null

    @ColumnInfo(name = "forecast")
    @SerializedName("forecast")
    @TypeConverters(ForecastConverter::class)
    var forecast: ArrayList<Forecast>? = null

    @SerializedName("friendly_name")
    var friendlyName: String? = null
}