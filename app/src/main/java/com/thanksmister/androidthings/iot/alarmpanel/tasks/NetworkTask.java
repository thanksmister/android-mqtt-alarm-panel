/*
 * <!--
 *   ~ Copyright (c) 2017. ThanksMister LLC
 *   ~
 *   ~ Licensed under the Apache License, Version 2.0 (the "License");
 *   ~ you may not use this file except in compliance with the License. 
 *   ~ You may obtain a copy of the License at
 *   ~
 *   ~ http://www.apache.org/licenses/LICENSE-2.0
 *   ~
 *   ~ Unless required by applicable law or agreed to in writing, software distributed 
 *   ~ under the License is distributed on an "AS IS" BASIS, 
 *   ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 *   ~ See the License for the specific language governing permissions and 
 *   ~ limitations under the License.
 *   -->
 */

package com.thanksmister.androidthings.iot.alarmpanel.tasks;

import android.os.AsyncTask;

abstract class NetworkTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {
    
    public Exception exception;
    private OnCompleteListener<Result> onCompleteListener;
    private OnExceptionListener onExceptionListener;

    @SafeVarargs
    protected final Result doInBackground(Params... params) {
        if (isCancelled()) {
            return null;
        }
        
        try {
            return doNetworkAction(params);
        } catch (Exception e) {
            this.exception = e;
        }
        return null;
    }
    
    @SuppressWarnings("unchecked")
    protected abstract Result doNetworkAction(Params... params) throws Exception;

    protected void onPostExecute(Result result) {
        
        super.onPostExecute(result);
        
        if (isCancelled()) {
            return;
        }
        
        do {
            if ((this.exception != null) && (this.onExceptionListener != null)) {
                this.onExceptionListener.onException(this.exception);
                return;
            }
        } while (this.onCompleteListener == null);
        
        this.onCompleteListener.onComplete(result);
    }

    protected void onPreExecute() {
        super.onPreExecute();
    }

    public void setOnCompleteListener(OnCompleteListener<Result> paramOnCompleteListener) {
        this.onCompleteListener = paramOnCompleteListener;
    }
    
    public void setOnExceptionListener(OnExceptionListener paramOnExceptionListener) {
        this.onExceptionListener = paramOnExceptionListener;
    }

    public static abstract interface OnCompleteListener<Result> {
        public abstract void onComplete(Result paramResult);
    }

    public static abstract interface OnExceptionListener {
        public abstract void onException(Exception paramException);
    }
}