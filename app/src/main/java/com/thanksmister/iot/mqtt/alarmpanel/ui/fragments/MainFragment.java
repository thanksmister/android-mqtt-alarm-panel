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

package com.thanksmister.iot.mqtt.alarmpanel.ui.fragments;

import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.thanksmister.iot.mqtt.alarmpanel.BaseFragment;
import com.thanksmister.iot.mqtt.alarmpanel.R;
import com.thanksmister.iot.mqtt.alarmpanel.data.database.model.SubscriptionModel;
import com.thanksmister.iot.mqtt.alarmpanel.data.provider.ContentProvider;
import com.thanksmister.iot.mqtt.alarmpanel.ui.Configuration;
import com.thanksmister.iot.mqtt.alarmpanel.ui.activities.LogActivity;
import com.thanksmister.iot.mqtt.alarmpanel.ui.views.AlarmTriggeredView;
import com.thanksmister.iot.mqtt.alarmpanel.utils.AlarmUtils;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

import static com.thanksmister.iot.mqtt.alarmpanel.ui.Configuration.PREF_ARM_AWAY;
import static com.thanksmister.iot.mqtt.alarmpanel.ui.Configuration.PREF_AWAY_TRIGGERED_PENDING;
import static com.thanksmister.iot.mqtt.alarmpanel.ui.Configuration.PREF_HOME_TRIGGERED_PENDING;
import static com.thanksmister.iot.mqtt.alarmpanel.ui.Configuration.PREF_TRIGGERED_PENDING;

public class MainFragment extends BaseFragment implements 
        LoaderManager.LoaderCallbacks<Cursor>  {

    private static final int DATA_LOADER_ID = 1;
    
    @Bind(R.id.triggeredView)
    View triggeredView;
    
    @Bind(R.id.mainView)
    View mainView;
    
    @OnClick(R.id.buttonSettings)
    void buttonSettingsClicked() {
        listener.showSettingsCodeDialog();
    }

    @OnClick(R.id.buttonLogs)
    void buttonLogsClicked() {
        Intent intent = LogActivity.createStartIntent(getActivity());
        startActivity(intent);
    }

    @OnClick(R.id.buttonSleep)
    void buttonSleep() {
        listener.manuallyLaunchScreenSaver();
    }

    private SubscriptionObserver subscriptionObserver;
    private OnMainFragmentListener listener;

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnMainFragmentListener {
        void manuallyLaunchScreenSaver();
        void publishDisarmed();
        void showSettingsCodeDialog();
        void showAlarmDisableDialog(boolean beep, int timeRemaining);
    }
    
    public MainFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     */
    public static MainFragment newInstance() {
        return new MainFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnMainFragmentListener) {
            listener = (OnMainFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnMainFragmentListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        subscriptionObserver = new SubscriptionObserver(new Handler());
        getActivity().getContentResolver().registerContentObserver(ContentProvider.SUBSCRIPTION_DATA_TABLE_URI, true, subscriptionObserver);
        getLoaderManager().restartLoader(DATA_LOADER_ID, null, this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().getContentResolver().unregisterContentObserver(subscriptionObserver);
        getLoaderManager().destroyLoader(DATA_LOADER_ID);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
        ButterKnife.unbind(this);
    }

    /**
     * Handles the application state from the state topic payload changes from the subscription.
     * @param state Payload from the state topic subscription.
     */
    @AlarmUtils.AlarmStates
    private void handleStateChange(String state) {
        Timber.d("state: " + state);
        Timber.d("mode: " + getConfiguration().getAlarmMode());
        switch (state) {
            case AlarmUtils.STATE_ARM_AWAY:
            case AlarmUtils.STATE_ARM_HOME:
                hideDialog();
                hideDisableDialog();
                break;
            case AlarmUtils.STATE_DISARM:
                hideDisableDialog();
                hideTriggeredView();
                break;
            case AlarmUtils.STATE_PENDING:
                hideDialog();
                hideProgressDialog();
                if(getConfiguration().getAlarmMode().equals(Configuration.PREF_ARM_HOME) 
                        || getConfiguration().getAlarmMode().equals(PREF_ARM_AWAY) 
                        || getConfiguration().getAlarmMode().equals(PREF_HOME_TRIGGERED_PENDING) 
                        || getConfiguration().getAlarmMode().equals(PREF_AWAY_TRIGGERED_PENDING) 
                        || getConfiguration().getAlarmMode().equals(PREF_TRIGGERED_PENDING)) {
                    // we need a pending time greater than zero to show the dialog, or its just going to go to trigger
                    if(getConfiguration().getPendingTime() > 0) {
                        listener.showAlarmDisableDialog(true, getConfiguration().getPendingTime());
                    }
                } 
                break;
            case AlarmUtils.STATE_TRIGGERED:
                hideDialog();
                hideDisableDialog();
                hideProgressDialog();
                showAlarmTriggered();
                break;
            default:
                break;
        }
    }

    // Control Fragment Listener
    private void showAlarmTriggered() {
        if(isAdded()) {
            mainView.setVisibility(View.GONE);
            triggeredView.setVisibility(View.VISIBLE);
            int code = getConfiguration().getAlarmCode();
            final AlarmTriggeredView disarmView = getActivity().findViewById(R.id.alarmTriggeredView);
            disarmView.setCode(code);
            disarmView.setListener(new AlarmTriggeredView.ViewListener() {
                @Override
                public void onComplete(int code) {
                    listener.publishDisarmed();
                }
                @Override
                public void onError() {
                    Toast.makeText(getActivity(), R.string.toast_code_invalid, Toast.LENGTH_SHORT).show();
                }
                @Override
                public void onCancel() {
                }
            });
        }
    }
    
    private void hideTriggeredView() {
        mainView.setVisibility(View.VISIBLE);
        triggeredView.setVisibility(View.GONE);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        if(id == DATA_LOADER_ID) {
            return new CursorLoader(getActivity(), ContentProvider.SUBSCRIPTION_DATA_TABLE_URI, SubscriptionModel.COLUMN_NAMES, null, null, null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        switch (loader.getId()) {
            case DATA_LOADER_ID:
                List<SubscriptionModel> subscriptionModels = SubscriptionModel.getModelList(cursor);
                if(subscriptionModels != null && !subscriptionModels.isEmpty()) {
                    SubscriptionModel subscriptionModel = subscriptionModels.get(subscriptionModels.size() - 1);
                    if(AlarmUtils.hasSupportedStates(subscriptionModel.payload())) {
                        handleStateChange(subscriptionModel.payload());
                    }
                }
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    private class SubscriptionObserver extends ContentObserver {
        SubscriptionObserver(Handler handler) {
            super(handler);
        }
        @Override
        public void onChange(boolean selfChange) {
            if(selfChange) {
                getActivity().getSupportLoaderManager().restartLoader(DATA_LOADER_ID, null, MainFragment.this);
            }
        }
    }
}