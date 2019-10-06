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

import android.content.Context
import android.os.Bundle
import android.view.View

import dagger.android.support.DaggerFragment
import io.reactivex.disposables.CompositeDisposable

open class BaseFragment : DaggerFragment() {

    val disposable = CompositeDisposable()


    override fun onDetach() {
        super.onDetach()
        disposable.dispose()
    }

    fun hasNetworkConnectivity(): Boolean {
        return (activity as BaseActivity).hasNetworkConnectivity()
    }

    fun handleNetworkDisconnect() {
        (activity as BaseActivity).handleNetworkDisconnect()
    }

    open fun onBackPressed() : Boolean {
        return false
    }
}// Required empty public constructor
