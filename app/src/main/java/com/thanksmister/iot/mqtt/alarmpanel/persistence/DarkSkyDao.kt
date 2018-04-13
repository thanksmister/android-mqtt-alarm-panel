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

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query

import io.reactivex.Flowable

/**
 * Data Access Object for the messages table.
 */
@Dao
interface DarkSkyDao {

    /**
     * Get all items
     * @return list of all weather data items.
     */
    @Query("SELECT * FROM DarkSky")
    fun getItems(): Flowable<List<DarkSky>>

    /**
     * Insert an item in the database. If the message already exists, replace it.
     * @param user the message to be inserted.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertItem(item: DarkSky)

    /**
     * Delete all items.
     */
    @Query("DELETE FROM DarkSky")
    fun deleteAllItems()
}