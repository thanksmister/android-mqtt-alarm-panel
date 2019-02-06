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
import com.google.gson.annotations.SerializedName

@Entity(tableName = "Weather")
class Weather {
    @PrimaryKey(autoGenerate = true)
    var uid: Int = 0

    @SerializedName("temperature")
    @ColumnInfo(name = "temperature")
    var temperature: String? = null

    @SerializedName("units")
    @ColumnInfo(name = "units")
    var units: String? = null

    @SerializedName("summary")
    @ColumnInfo(name = "summary")
    var summary: String? = null

    @SerializedName("forecast")
    @ColumnInfo(name = "forecast")
    var forecast: String? = null

    @SerializedName("icon")
    @ColumnInfo(name = "icon")
    var icon: String? = null

    @SerializedName("precipitation")
    @ColumnInfo(name = "precipitation")
    var precipitation: Double? = null

    @ColumnInfo(name = "createdAt")
    var createdAt: String? = null
}