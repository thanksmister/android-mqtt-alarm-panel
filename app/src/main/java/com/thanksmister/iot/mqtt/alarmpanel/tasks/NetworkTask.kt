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

package com.thanksmister.iot.mqtt.alarmpanel.tasks

import android.os.AsyncTask

abstract class NetworkTask<Params, Progress, Result> : AsyncTask<Params, Progress, Result>() {

    var exception: Exception? = null
    private var onCompleteListener: OnCompleteListener<Result>? = null
    private var onExceptionListener: OnExceptionListener? = null

    @SafeVarargs
    override fun doInBackground(vararg params: Params): Result? {
        if (isCancelled) {
            return null
        }

        try {
            return doNetworkAction(*params)
        } catch (e: Exception) {
            this.exception = e
        }

        return null
    }

    @Throws(Exception::class)
    protected abstract fun doNetworkAction(vararg params: Params): Result

    override fun onPostExecute(result: Result) {

        super.onPostExecute(result)

        if (isCancelled) {
            return
        }

        do {
            if (this.exception != null && this.onExceptionListener != null) {
                this.onExceptionListener!!.onException(this.exception!!)
                return
            }
        } while (this.onCompleteListener == null)

        this.onCompleteListener!!.onComplete(result)
    }

    override fun onPreExecute() {
        super.onPreExecute()
    }

    fun setOnCompleteListener(paramOnCompleteListener: OnCompleteListener<Result>) {
        this.onCompleteListener = paramOnCompleteListener
    }

    fun setOnExceptionListener(paramOnExceptionListener: OnExceptionListener) {
        this.onExceptionListener = paramOnExceptionListener
    }

    interface OnCompleteListener<in Result> {
        fun onComplete(paramResult: Result)
    }

    interface OnExceptionListener {
        fun onException(paramException: Exception)
    }
}