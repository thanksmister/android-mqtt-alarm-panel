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

package com.thanksmister.iot.mqtt.alarmpanel.persistence

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "DarkSky")
class DarkSky {
    @PrimaryKey(autoGenerate = true)
    var uid: Int = 0

    @ColumnInfo(name = "icon")
    var icon: String? = null

    @ColumnInfo(name = "apparentTemperature")
    var apparentTemperature: String? = null

    @ColumnInfo(name = "units")
    var units: String? = null

    @ColumnInfo(name = "summary")
    var summary: String? = null

    @ColumnInfo(name = "precipProbability")
    var precipProbability: String? = null

    @ColumnInfo(name = "data")
    var data: String? = null

    @ColumnInfo(name = "umbrella")
    var umbrella: Boolean = false

    @ColumnInfo(name = "createdAt")
    var createdAt: String? = null
}