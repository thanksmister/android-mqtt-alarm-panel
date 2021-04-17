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

import androidx.room.*

import io.reactivex.Flowable
import io.reactivex.Maybe

/**
 * Data Access Object for the sensors table.
 */
@Dao
interface DashboardDao {

    /**
     * Get all items
     * @return list of all items
     */
    @Query("SELECT * FROM Dashboards")
    fun getItems(): Maybe<List<Dashboard>>

    /**
     * Get all items
     * @return list of all items
     */
    @Query("SELECT * FROM Dashboards")
    fun getDashboards(): Flowable<List<Dashboard>>

    /**
     * Insert a items in the database. If the item already exists, replace it.
     * @param the item to be inserted.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertItem(item: Dashboard)

    /**
     * Delete item.
     */
    @Delete
    fun deleteItem(item: Dashboard)

    /**
     * Delete all items.
     */
    @Query("DELETE FROM Dashboards")
    fun deleteAllItems()
}