/*
 * Copyright (c) 2017. ThanksMister LLC
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

package com.thanksmister.iot.mqtt.alarmpanel.di


import android.arch.lifecycle.ViewModel
import com.thanksmister.iot.mqtt.alarmpanel.BaseActivity
import com.thanksmister.iot.mqtt.alarmpanel.BaseFragment
import com.thanksmister.iot.mqtt.alarmpanel.ui.activities.LogActivity
import com.thanksmister.iot.mqtt.alarmpanel.ui.activities.MainActivity
import com.thanksmister.iot.mqtt.alarmpanel.ui.activities.SettingsActivity
import com.thanksmister.iot.mqtt.alarmpanel.ui.fragments.*
import com.thanksmister.iot.mqtt.alarmpanel.viewmodel.MessageViewModel
import com.thanksmister.iot.mqtt.alarmpanel.viewmodel.WeatherViewModel
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

@Module
internal abstract class AndroidBindingModule {

    @Binds
    @IntoMap
    @ViewModelKey(MessageViewModel::class)
    abstract fun bindsMessageViewModel(mainViewModel: MessageViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(WeatherViewModel::class)
    abstract fun bindsWeatherViewModel(mainViewModel: WeatherViewModel): ViewModel

    @ContributesAndroidInjector
    internal abstract fun baseActivity(): BaseActivity

    @ContributesAndroidInjector
    internal abstract fun mainActivity(): MainActivity

    @ContributesAndroidInjector
    internal abstract fun logActivity(): LogActivity

    @ContributesAndroidInjector
    internal abstract fun settingsActivity(): SettingsActivity

    @ContributesAndroidInjector
    internal abstract fun baseFragment(): BaseFragment

    @ContributesAndroidInjector
    internal abstract fun mainFragment(): MainFragment

    @ContributesAndroidInjector
    internal abstract fun platformFragment(): PlatformFragment

    @ContributesAndroidInjector
    internal abstract fun informationFragment(): InformationFragment

    @ContributesAndroidInjector
    internal abstract fun controlsFragment(): ControlsFragment

    @ContributesAndroidInjector
    internal abstract fun aboutFragment(): AboutFragment

    @ContributesAndroidInjector
    internal abstract fun logFragment(): LogFragment

    @ContributesAndroidInjector
    internal abstract fun settingsFragment(): SettingsFragment

    @ContributesAndroidInjector
    internal abstract fun alarmSettingsFragment(): AlarmSettingsFragment

    @ContributesAndroidInjector
    internal abstract fun notificationsSettingsFragment(): NotificationsSettingsFragment

    @ContributesAndroidInjector
    internal abstract fun platformSettingsFragment(): PlatformSettingsFragment

    @ContributesAndroidInjector
    internal abstract fun screenSettingsFragment(): ScreenSettingsFragment

    @ContributesAndroidInjector
    internal abstract fun weatherSettingsFragment(): WeatherSettingsFragment

    @ContributesAndroidInjector
    internal abstract fun cameraSettingsFragment(): CameraSettingsFragment

    @ContributesAndroidInjector
    internal abstract fun mqttSettingsFragment(): MqttSettingsFragment
}