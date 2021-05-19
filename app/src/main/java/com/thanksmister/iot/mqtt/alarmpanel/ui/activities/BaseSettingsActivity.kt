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

package com.thanksmister.iot.mqtt.alarmpanel.ui.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.*
import androidx.core.app.ActivityCompat
import com.thanksmister.iot.mqtt.alarmpanel.R
import com.thanksmister.iot.mqtt.alarmpanel.network.AlarmPanelService
import com.thanksmister.iot.mqtt.alarmpanel.network.ImageOptions
import com.thanksmister.iot.mqtt.alarmpanel.network.MQTTOptions
import com.thanksmister.iot.mqtt.alarmpanel.persistence.Configuration
import com.thanksmister.iot.mqtt.alarmpanel.persistence.WeatherDao
import com.thanksmister.iot.mqtt.alarmpanel.utils.DialogUtils
import com.thanksmister.iot.mqtt.alarmpanel.utils.ScreenUtils
import dagger.android.support.DaggerAppCompatActivity
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

abstract class BaseSettingsActivity : DaggerAppCompatActivity() {

    @Inject
    lateinit var configuration: Configuration
    @Inject
    lateinit var mqttOptions: MQTTOptions
    @Inject
    lateinit var dialogUtils: DialogUtils
    @Inject
    lateinit var screenUtils: ScreenUtils

    val disposable = CompositeDisposable()

    override fun onDestroy() {
        super.onDestroy()
        disposable.dispose()
    }

    public override fun onResume() {
        super.onResume()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return item.itemId == android.R.id.home
    }

    // When activity is recreated we can switch the day night mode
    private fun setDayNightMode() {
        val dayNightMode = configuration.dayNightMode
        if (dayNightMode == Configuration.SUN_BELOW_HORIZON) {
            setDarkTheme()
        } else if (dayNightMode == Configuration.SUN_ABOVE_HORIZON) {
            setLightTheme()
        }
    }

    private fun setDarkTheme() {
        val nightMode = getDefaultNightMode()
        if(nightMode == MODE_NIGHT_NO || nightMode == MODE_NIGHT_UNSPECIFIED) {
            screenUtils.setScreenBrightness()
            setDefaultNightMode(MODE_NIGHT_YES)
            recreate()
        }
    }

    private fun setLightTheme() {
        val nightMode = getDefaultNightMode()
        if(nightMode == MODE_NIGHT_YES || nightMode == MODE_NIGHT_UNSPECIFIED) {
            screenUtils.setScreenBrightness()
            setDefaultNightMode(MODE_NIGHT_NO)
            recreate()
        }
    }
}