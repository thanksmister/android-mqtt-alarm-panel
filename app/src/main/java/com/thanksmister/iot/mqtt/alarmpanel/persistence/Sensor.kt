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

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Sensors")
class Sensor {

    @PrimaryKey(autoGenerate = true)
    var uid: Int = 0

    @ColumnInfo(name = "name")
    var name: String? = null

    @ColumnInfo(name = "type")
    var type: String? = null

    @ColumnInfo(name = "topic")
    var topic: String? = null

    @ColumnInfo(name = "payloadActive")
    var payloadActive: String? = null

    @ColumnInfo(name = "payloadInactive")
    var payloadInactive: String? = null

    @ColumnInfo(name = "payload")
    var payload: String? = null

    @ColumnInfo(name = "notify")
    var notify: Boolean = false

    @ColumnInfo(name = "alarmMode")
    var alarmMode: Boolean = false
}