package com.thanksmister.iot.mqtt.alarmpanel.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import com.thanksmister.iot.mqtt.alarmpanel.persistence.Sensor
import com.thanksmister.iot.mqtt.alarmpanel.persistence.SensorDatabase
import io.reactivex.functions.Predicate
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Test the implementation of {@link SensorDao}
 */
@RunWith(AndroidJUnit4::class)
@SmallTest
class SensorsDaoTest {

    private lateinit var mDatabase: SensorDatabase

    @Rule @JvmField var instantTaskExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

    private val sensor: Sensor by lazy {
        val sensorTopic = Sensor()
        sensorTopic.uid = 3
        sensorTopic.topic = "main_door"
        sensorTopic
    }

    @Before
    fun setUp() {
        mDatabase = Room.inMemoryDatabaseBuilder(
                InstrumentationRegistry.getInstrumentation().context,
                SensorDatabase::class.java) // allowing main thread queries, just for testing
                .allowMainThreadQueries()
                .build()
    }

    @After
    fun tearDown() {
        mDatabase.close();
    }

    @Test
    fun insertAndGetSensorByTopic() {

        // Insert new sensor
        mDatabase.sensorDao().insertItem(sensor).blockingAwait()

        // When subscribing to the emissions of sensor
        mDatabase.sensorDao()
                .getSensorByTopic(sensor.topic!!)
                .test() // assertValue asserts that there was only one emission
                .assertValue(object : Predicate<Sensor?> {
                    @Throws(Exception::class)
                    override fun test(t: Sensor): Boolean {
                        return t.topic.equals(sensor.topic)
                    }
                })
    }

    @Test
    fun updateAndSensor() {
        // Given that we have a user in the data source
        mDatabase.sensorDao().insertItem(sensor).blockingAwait()

        // When we are updating the name of the user
        val updatedUser = Sensor()
        updatedUser.uid = sensor.uid
        updatedUser.topic = "front_door"

        mDatabase.sensorDao().insertItem(updatedUser).blockingAwait()

        // When subscribing to the emissions of the user
        mDatabase.sensorDao().getSenors()
                .test() // assertValue asserts that there was only one emission of the user
                .assertValue({ sensors ->
                    sensors[0].topic == updatedUser.topic
                })
    }
}