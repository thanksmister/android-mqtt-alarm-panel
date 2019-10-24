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

package com.thanksmister.iot.mqtt.alarmpanel

import android.app.Activity
import android.app.Application
import android.os.Bundle

import timber.log.Timber


class LifecycleHandler : Application.ActivityLifecycleCallbacks {

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle) {}

    override fun onActivityDestroyed(activity: Activity) {}

    override fun onActivityResumed(activity: Activity) {
        ++resumed
    }

    override fun onActivityPaused(activity: Activity) {
        ++paused
        Timber.w("application is in foreground: " + (resumed > paused))
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityStarted(activity: Activity) {
        ++started
    }

    override fun onActivityStopped(activity: Activity) {
        ++stopped
        Timber.w("application is visible: " + (started > stopped))
    }

    companion object {

        private var resumed: Int = 0
        private var paused: Int = 0
        private var started: Int = 0
        private var stopped: Int = 0

        // And these two public static functions
        val isApplicationVisible: Boolean
            get() = started > stopped

        val isApplicationInForeground: Boolean
            get() = resumed > paused
    }
}
