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

import com.thanksmister.androidthings.iot.alarmpanel.data.stores.StoreManager;
import com.thanksmister.androidthings.iot.alarmpanel.network.model.FeedData;

public class UpdateFeedDataTask extends NetworkTask<FeedData, Void, Boolean> {
    
    private StoreManager storeManager;

    public UpdateFeedDataTask(StoreManager storeManager) {
        this.storeManager = storeManager;
    }

    protected Boolean doNetworkAction(FeedData... params) throws Exception {
        if (params.length != 1) {
            throw new Exception("Wrong number of params, expected 1, received " + params.length);
        }
        FeedData feedData = params[0];
        storeManager.updateFeedData(feedData);
        return true;
    }
}