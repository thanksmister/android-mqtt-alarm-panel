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

package com.thanksmister.androidthings.iot.alarmpanel.ui.fragments;


import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.thanksmister.androidthings.iot.alarmpanel.R;
import com.thanksmister.androidthings.iot.alarmpanel.data.database.model.SubscriptionModel;
import com.thanksmister.androidthings.iot.alarmpanel.network.sync.SyncProvider;
import com.thanksmister.androidthings.iot.alarmpanel.ui.adapters.SubscriptionCursorAdapter;

import butterknife.Bind;
import butterknife.ButterKnife;

public class LogFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    // The loader's unique id. Loader ids are specific to the Activity or
    private static final int DATA_LOADER_ID = 1;
    
    @Bind(R.id.logsListView)
    ListView listView;

    private SubscriptionCursorAdapter cursorAdapter;
    
    public LogFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     */
    public static LogFragment newInstance() {
        return new LogFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getLoaderManager().initLoader(DATA_LOADER_ID, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_logs, container, false);
        ButterKnife.bind(this, fragmentView);
        return fragmentView;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        if(id == DATA_LOADER_ID) {
            return new CursorLoader(getActivity(), SyncProvider.SUBSCRIPTION_DATA_TABLE_URI, SubscriptionModel.COLUMN_NAMES, null, null, null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case DATA_LOADER_ID:
                cursorAdapter = new SubscriptionCursorAdapter(getActivity(), data, false);
                listView.setAdapter(cursorAdapter);
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        cursorAdapter.swapCursor(null);
    }
}