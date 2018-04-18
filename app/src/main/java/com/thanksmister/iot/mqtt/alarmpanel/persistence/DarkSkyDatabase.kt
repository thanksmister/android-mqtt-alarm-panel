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

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context

/**
 * The Room database that contains the weather table
 */
@Database(entities = arrayOf(DarkSky::class), version = 1, exportSchema = false)
abstract class DarkSkyDatabase : RoomDatabase() {

    abstract fun darkSkyDao(): DarkSkyDao

    companion object {

        @Volatile private var INSTANCE: DarkSkyDatabase? = null

        @JvmStatic fun getInstance(context: Context): DarkSkyDatabase =
                INSTANCE ?: synchronized(this) {
                    INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
                }

        private fun buildDatabase(context: Context) =
                Room.databaseBuilder(context.applicationContext,
                        DarkSkyDatabase::class.java, "weather.db")
                        .fallbackToDestructiveMigration()
                        .build()
    }
}