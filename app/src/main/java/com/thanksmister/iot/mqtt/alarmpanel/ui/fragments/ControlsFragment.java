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
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.thanksmister.iot.mqtt.alarmpanel.BaseActivity;
import com.thanksmister.iot.mqtt.alarmpanel.BaseFragment;
import com.thanksmister.iot.mqtt.alarmpanel.R;
import com.thanksmister.iot.mqtt.alarmpanel.data.database.model.SubscriptionModel;
import com.thanksmister.iot.mqtt.alarmpanel.data.provider.ContentProvider;
import com.thanksmister.iot.mqtt.alarmpanel.ui.Configuration;
import com.thanksmister.iot.mqtt.alarmpanel.ui.views.AlarmPendingView;
import com.thanksmister.iot.mqtt.alarmpanel.ui.views.ArmOptionsView;
import com.thanksmister.iot.mqtt.alarmpanel.utils.AlarmUtils;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.thanksmister.iot.mqtt.alarmpanel.ui.Configuration.PREF_ARM_AWAY;
import static com.thanksmister.iot.mqtt.alarmpanel.ui.Configuration.PREF_ARM_AWAY_PENDING;
import static com.thanksmister.iot.mqtt.alarmpanel.ui.Configuration.PREF_ARM_HOME;
import static com.thanksmister.iot.mqtt.alarmpanel.ui.Configuration.PREF_ARM_HOME_PENDING;
import static com.thanksmister.iot.mqtt.alarmpanel.ui.Configuration.PREF_ARM_PENDING;
import static com.thanksmister.iot.mqtt.alarmpanel.ui.Configuration.PREF_DISARM;
import static com.thanksmister.iot.mqtt.alarmpanel.ui.Configuration.PREF_TRIGGERED_PENDING;

public class ControlsFragment extends BaseFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    // The loader's unique id. Loader ids are specific to the Activity or
    private static final int DATA_LOADER_ID = 1;
   
    @Bind(R.id.alarmPendingLayout)
    View alarmPendingLayout;
    
    @Bind(R.id.alarmButtonBackground)
    View alarmButtonBackground;
    
    @Bind(R.id.alarmText)
    TextView alarmText;
    
    @Bind(R.id.alarmPendingView)
    AlarmPendingView alarmPendingView;
 
    @OnClick(R.id.alarmView)
    public void armButtonClick() {
        if(getConfiguration().hasConnectionCriteria()) {
            if(getConfiguration().getAlarmMode().equals(Configuration.PREF_DISARM)){
                showArmOptionsDialog();
            } else {
                int countDownTimeRemaining = alarmPendingView.getCountDownTimeRemaining();
                if(countDownTimeRemaining > 0) {
                    listener.showAlarmDisableDialog(false, countDownTimeRemaining);
                } else {
                    listener.showAlarmDisableDialog(false, getConfiguration().getPendingTime());
                }
            }
        } else {
            if(isAdded()) {
                ((BaseActivity) getActivity()).showAlertDialog(getString(R.string.text_error_no_alarm_setup));
            }
        }
    }
    
    private SubscriptionObserver subscriptionObserver;
    private OnControlsFragmentListener listener;

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnControlsFragmentListener {
        void publishArmedHome();
        void publishArmedAway();
        void showAlarmDisableDialog(boolean beep, int timeRemaining);
    }

    public ControlsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     */
    public static ControlsFragment newInstance() {
        return new ControlsFragment();
    }
    
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnControlsFragmentListener) {
            listener = (OnControlsFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnControlsFragmentListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_controls, container, false);
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
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(getConfiguration().isArmed()) {
            String armedMode = getConfiguration().getAlarmMode();
            if (armedMode.equals(PREF_ARM_AWAY)) {
                setArmedAwayView();
            } else if (armedMode.equals(PREF_ARM_HOME)) {
                setArmedHomeView();
            }
        } else {
           setDisarmedView();
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
        ButterKnife.unbind(this);
    }

    private void showArmOptionsDialog() {
        showArmOptionsDialog(new ArmOptionsView.ViewListener() {
            @Override
            public void onArmHome() {
                hideDialog();
                listener.publishArmedHome();
                setPendingView(PREF_ARM_HOME_PENDING);
            }
            @Override
            public void onArmAway() {
                hideDialog();
                listener.publishArmedAway();
                setPendingView(PREF_ARM_AWAY_PENDING);
            }
        });
    }

    /**
     * Handles the application state from the state topic payload changes from the subscriptoin.
     * @param state Payload from the state topic subscription.
     */
    @AlarmUtils.AlarmStates
    private void handleStateChange(String state) {
        switch (state) {
            case AlarmUtils.STATE_ARM_AWAY:
                hideDialog();
                hideAlarmPendingView();
                setArmedAwayView();
                break;
            case AlarmUtils.STATE_ARM_HOME:
                hideDialog();
                hideAlarmPendingView();
                setArmedHomeView();
                break;
            case AlarmUtils.STATE_DISARM:
                hideDialog();
                hideAlarmPendingView();
                setDisarmedView();
                break;
            case AlarmUtils.STATE_PENDING:
                if(getConfiguration().getAlarmMode().equals(Configuration.PREF_ARM_AWAY_PENDING) 
                        || getConfiguration().getAlarmMode().equals(PREF_ARM_HOME_PENDING)) {
                    if(PREF_ARM_HOME_PENDING.equals(getConfiguration().getAlarmMode())) {
                        alarmText.setText(R.string.text_arm_home);
                        alarmText.setTextColor(getResources().getColor(R.color.yellow));
                        alarmButtonBackground.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_round_yellow));
                    } else if (PREF_ARM_AWAY_PENDING.equals(getConfiguration().getAlarmMode())) {
                        alarmText.setText(R.string.text_arm_away);
                        alarmText.setTextColor(getResources().getColor(R.color.red));
                        alarmButtonBackground.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_round_red));
                    }
                } else if(!getConfiguration().getAlarmMode().equals(Configuration.PREF_ARM_HOME) 
                        && !getConfiguration().getAlarmMode().equals(PREF_ARM_AWAY)
                        && !getConfiguration().getAlarmMode().equals(PREF_TRIGGERED_PENDING)) {
                    setPendingView(PREF_ARM_PENDING);
                } 
                break;
            case AlarmUtils.STATE_ERROR:
                hideAlarmPendingView();
                setDisarmedView();
                break;
            default:
                break;
        }
    }

    private void setArmedAwayView() {
        getConfiguration().setArmed(true);
        getConfiguration().setAlarmMode(PREF_ARM_AWAY);
        alarmText.setText(R.string.text_arm_away);
        alarmText.setTextColor(getResources().getColor(R.color.red));
        alarmButtonBackground.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_round_red));
    }

    private void setArmedHomeView() {
        getConfiguration().setArmed(true);
        getConfiguration().setAlarmMode(PREF_ARM_HOME);
        alarmText.setText(R.string.text_arm_home);
        alarmText.setTextColor(getResources().getColor(R.color.yellow));
        alarmButtonBackground.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_round_yellow));
    }

    /**
     * We want to show a pending countdown view for the given 
     * mode which can be arm home, arm away, or arm pending (from HASS).
     * @param mode PREF_ARM_HOME_PENDING, PREF_ARM_AWAY_PENDING, PREF_ARM_PENDING
     */
    private void setPendingView(String mode) {
        getConfiguration().setArmed(true);
        getConfiguration().setAlarmMode(mode);
        if(PREF_ARM_HOME_PENDING.equals(mode)) {
            alarmText.setText(R.string.text_arm_home);
            alarmText.setTextColor(getResources().getColor(R.color.yellow));
            alarmButtonBackground.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_round_yellow));
        } else if (PREF_ARM_AWAY_PENDING.equals(mode)) {
            alarmText.setText(R.string.text_arm_away);
            alarmText.setTextColor(getResources().getColor(R.color.red));
            alarmButtonBackground.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_round_red));
        } else if (PREF_ARM_PENDING.equals(mode)) {
            alarmText.setText(R.string.text_alarm_pending);
            alarmText.setTextColor(getResources().getColor(R.color.gray));
            alarmButtonBackground.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_round_gray));
        }
        showAlarmPendingView();
    }

    private void setDisarmedView() {
        getConfiguration().setArmed(false);
        getConfiguration().setAlarmMode(PREF_DISARM);
        alarmText.setText(R.string.text_disarmed);
        alarmText.setTextColor(getResources().getColor(R.color.green));
        alarmButtonBackground.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_round_green));
    }
    
    private void showAlarmPendingView() {
        if(alarmPendingLayout.isShown()) {
            return;
        }
        alarmPendingLayout.setVisibility(View.VISIBLE);
        alarmPendingView.setListener(new AlarmPendingView.ViewListener() {
            @Override
            public void onTimeOut() {
                hideAlarmPendingView();
            }
        });
        alarmPendingView.startCountDown(getConfiguration().getPendingTime());
    }
    
    private void hideAlarmPendingView() {
        alarmPendingLayout.setVisibility(View.GONE);
        alarmPendingView.stopCountDown();
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
                    // get the last message
                    SubscriptionModel subscriptionModel = subscriptionModels.get(subscriptionModels.size() - 1);
                    // if we have a payload has one of the states
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
                getActivity().getSupportLoaderManager().restartLoader(DATA_LOADER_ID, null, ControlsFragment.this);
            }
        }
    }
}