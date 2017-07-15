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

package com.thanksmister.androidthings.iot.alarmpanel.network.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.os.Bundle;

import com.thanksmister.androidthings.iot.alarmpanel.BaseApplication;
import com.thanksmister.androidthings.iot.alarmpanel.data.stores.StoreManager;
import com.thanksmister.androidthings.iot.alarmpanel.network.NetworkApi;
import com.thanksmister.androidthings.iot.alarmpanel.network.fetchers.MQTTFetcher;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import dpreference.DPreference;
import timber.log.Timber;

public class SyncAdapter extends AbstractThreadedSyncAdapter {

    public static final String ACTION_SYNC = "com.thanksmister.androidthings.iot.ACTION_SYNC";
    public static final String ACTION_TYPE_START = "com.thanksmister.androidthings.iot.ACTION_SYNC_START";
    public static final String ACTION_TYPE_COMPLETE = "com.thanksmister.androidthings.iot.ACTION_SYNC_COMPLETE";
    public static final String ACTION_TYPE_CANCELED = "com.thanksmister.androidthings.iot.ACTION_SYNC_CANCELED";
    public static final String ACTION_TYPE_ERROR = "com.thanksmister.androidthings.iot.ACTION_SYNC_ERROR";

    public static final String EXTRA_ACTION_TYPE = "com.thanksmister.androidthings.iot.extra.EXTRA_ACTION";
    public static final String EXTRA_ERROR_CODE = "com.thanksmister.androidthings.iot.extra.EXTRA_ERROR_CODE";
    public static final String EXTRA_ERROR_MESSAGE = "com.thanksmister.androidthings.iot.extra.EXTRA_ERROR_MESSAGE";
    
    private DPreference sharedPreferences;
    private StoreManager storeManager;
    private ContentResolver contentResolver;
    private MQTTFetcher mqttFetcher;
    private HashMap<String, Boolean> syncMap;
    private final AtomicBoolean canceled = new AtomicBoolean(false);
   
    SyncAdapter(Context context) {
        
        super(context, true);
        
        BaseApplication baseApplication = BaseApplication.getInstance();
        contentResolver = getContext().getApplicationContext().getContentResolver();
        NetworkApi networkApi = new NetworkApi();

        sharedPreferences = baseApplication.getAppSharedPreferences();
        storeManager = new StoreManager(baseApplication.getApplicationContext(), contentResolver, sharedPreferences);
        
        mqttFetcher = new MQTTFetcher(baseApplication, networkApi);
        syncMap = new HashMap<>(); // init sync map
    }
    
    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        
        this.canceled.set(false);
        
        if(!isSyncing()) {
            //getFeedData();
            if(!isSyncing()) {
                onSyncComplete();
            }
        }
    }

    /**
     * Keep a map of all syncing calls to update sync status and
     * broadcast when no more syncs running
     * @param key
     * @param value
     */
    private void updateSyncMap(String key, boolean value) {
        Timber.d("updateSyncMap: " + key + " value: " + value);
        syncMap.put(key, value);
        if(isSyncing()) {
            onSyncStart();
        } else {
            resetSyncing();
            onSyncComplete();
        }
    }

    /**
     * Prints the sync map for debugging
     */
    private void printSyncMap() {
        for (Object o : syncMap.entrySet()) {
            Map.Entry pair = (Map.Entry) o;
            Timber.d("Sync Map>>>>>> " + pair.getKey() + " = " + pair.getValue());
        }
    }

    /**
     * Checks if any active syncs are going one
     * @return
     */
    private boolean isSyncing() {
        printSyncMap();
        Timber.d("isSyncing: " + syncMap.containsValue(true));
        return syncMap.containsValue(true);
    }

    /**
     * Resets the syncing map
     */
    private void resetSyncing() {
        syncMap = new HashMap<>();
    }
    
    /**
     * Check if the sync has been canceled due to error or network
     * @return
     */
    private boolean isCanceled() {
        return canceled.get();
    }

    /*private void getFeedData() {
        Timber.d("getFeedData");
        if(!isCanceled()) {
            mqttFetcher.getFeedData( new Callback<List<FeedData>>() {
                @Override
                public void onResponse(Call<List<FeedData>> call, Response<List<FeedData>> response) {
                    if(response.isSuccessful()) {
                        storeManager.updateFeedDataList(response.body());
                    } else {
                        int statusCode  = response.code();
                        Timber.e("Error loading data code: " + statusCode);
                    }
                }
                @Override
                public void onFailure(Call<List<FeedData>> call, Throwable t) {
                    Timber.e("Error loading data message: " + t.getMessage());
                    onSyncFailed(t.getMessage(), ExceptionCodes.SYNC_ERROR_CODE);
                }
            },storeManager.getPassword(), storeManager.getUserName(), storeManager.getTopic());
        }
    }
    
    *//**
     * Get all the last data in the queue
     *//*
    private void getLastDataInQueue() {
        Timber.d("getLastDataInQueue");
        if(!isCanceled()) {
            mqttFetcher.getLastDataInQueue( new Callback<FeedData>() {
                @Override
                public void onResponse(Call<FeedData> call, Response<FeedData> response) {
                    if(response.isSuccessful()) {
                        storeManager.updateFeedData(response.body());
                    } else {
                        int statusCode  = response.code();
                        Timber.e("Error loading data code: " + statusCode);
                    }
                }
                @Override
                public void onFailure(Call<FeedData> call, Throwable t) {
                    Timber.e("Error loading data message: " + t.getMessage());
                    onSyncFailed(t.getMessage(), ExceptionCodes.SYNC_ERROR_CODE);
                }
            },storeManager.getPassword(), storeManager.getUserName(), storeManager.getTopic());
        }
    }*/
    
    private void onSyncStart() {
        Timber.d("onSyncStart");
        Intent intent = new Intent(ACTION_SYNC);
        intent.putExtra(EXTRA_ACTION_TYPE, ACTION_TYPE_START);
        getContext().sendBroadcast(intent);
    }

    private void onSyncComplete() {
        Timber.d("onSyncComplete");
        Intent intent = new Intent(ACTION_SYNC);
        intent.putExtra(EXTRA_ACTION_TYPE, ACTION_TYPE_COMPLETE);
        getContext().sendBroadcast(intent);
    }
    
    @Override
    public void onSyncCanceled() {
        Timber.d("onSyncCanceled");
        super.onSyncCanceled();
        Intent intent = new Intent(ACTION_SYNC);
        intent.putExtra(EXTRA_ACTION_TYPE, ACTION_TYPE_CANCELED);
        getContext().sendBroadcast(intent);
    }
    
    private void onSyncFailed(String message, int code) {
        Intent intent = new Intent(ACTION_SYNC);
        intent.putExtra(EXTRA_ACTION_TYPE, ACTION_TYPE_ERROR);
        intent.putExtra(EXTRA_ERROR_MESSAGE, message);
        intent.putExtra(EXTRA_ERROR_CODE, code);
        getContext().sendBroadcast(intent);
    }
}