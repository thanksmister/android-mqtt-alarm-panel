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
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.thanksmister.androidthings.iot.alarmpanel.BaseFragment;
import com.thanksmister.androidthings.iot.alarmpanel.R;
import com.thanksmister.androidthings.iot.alarmpanel.ui.Configuration;
import com.thanksmister.androidthings.iot.alarmpanel.ui.views.ArmOptionsView;
import com.thanksmister.androidthings.iot.alarmpanel.ui.views.CodeVerificationView;
import com.thanksmister.androidthings.iot.alarmpanel.ui.views.CountDownView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class ControlsFragment extends BaseFragment {

    @Bind(R.id.armedAwayView)
    View armedAwayView;

    @Bind(R.id.armedStayView)
    View armedStayView;

    @Bind(R.id.disarmedView)
    LinearLayout disarmedView;
 
    @OnClick(R.id.armButton)
    public void armButtonClick() {
        showArmOptionsDialog();
    }

    @OnClick(R.id.disarmAwayButton)
    public void disarmAwayButtonClick() {
        showAlarmCodeDialog();
    }

    @OnClick(R.id.disarmStayButton)
    public void disarmStayClick() {
        showAlarmCodeDialog();
    }
    
    private AlertDialog countDownAlarm;

    private OnControlsFragmentListener mListener;

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnControlsFragmentListener {
        void publishArmedStay();
        void publishArmedAway();
        void publishDisarmed();
        void publishPending();
        void publishTriggered();
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
            mListener = (OnControlsFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnControlsFragmentListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_controls, container, false);
        ButterKnife.bind(this, fragmentView);
        return fragmentView;
    }
    
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(getConfiguration().isArmed()) {
            String armedMode = getConfiguration().getAlarmMode();
            if (armedMode.equals(Configuration.ARMED_AWAY)) {
                setArmedAwayView();
            } else if (armedMode.equals(Configuration.ARMED_STAY)) {
                setArmedStayView();
            }
        } else {
           setDisarmedView();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void setArmedAwayView() {
        mListener.publishArmedAway();
        armedStayView.setVisibility(View.GONE);
        armedAwayView.setVisibility(View.VISIBLE);
        disarmedView.setVisibility(View.GONE);
        /*if(getSupportActionBar() != null){
            getSupportActionBar().setTitle(" System is armed");
            getSupportActionBar().setLogo(R.drawable.ic_lock_lg);
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.red)));
        }*/
    }

    private void setArmedStayView() {
        mListener.publishArmedStay();
        armedStayView.setVisibility(View.VISIBLE);
        armedAwayView.setVisibility(View.GONE);
        disarmedView.setVisibility(View.GONE);
        /*if(getSupportActionBar() != null){
            getSupportActionBar().setTitle(" System is armed");
            getSupportActionBar().setLogo(R.drawable.ic_lock_lg);
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.red)));
        }*/
    }

    private void setDisarmedView() {
        mListener.publishDisarmed();
        armedStayView.setVisibility(View.GONE);
        armedAwayView.setVisibility(View.GONE);
        disarmedView.setVisibility(View.VISIBLE);
        /*if(getSupportActionBar() != null){
            getSupportActionBar().setTitle(" System disarmed");
            getSupportActionBar().setLogo(R.drawable.ic_unlock_lg);
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.blue)));
        }*/
    }

    // TODO check sensors open
    private void showArmOptionsDialog() {
        showArmOptionsDialog(new ArmOptionsView.ViewListener() {
            @Override
            public void onArmStay() {
                setArmedStayView();
                hideArmOptionsDialog();
            }

            @Override
            public void onArmAway() {
                showCountDownAlarm();
                hideArmOptionsDialog();
            }
        });
    }

    private void showAlarmCodeDialog() {
        final int alarmCode = getConfiguration().getAlarmCode();
        showAlarmCodeDialog(new CodeVerificationView.ViewListener() {
            @Override
            public void onComplete() {
                hideAlarmCodeDialog();
                setDisarmedView();
                Toast.makeText(getActivity(), R.string.toast_alarm_deactivated, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCancel() {
                hideAlarmCodeDialog();
                Toast.makeText(getActivity(), R.string.toast_alarm_cancelled, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError() {
                Toast.makeText(getActivity(), R.string.toast_code_invalid, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onTimedOut() {
                hideAlarmCodeDialog();
                Toast.makeText(getActivity(), R.string.alarm_timed_out, Toast.LENGTH_SHORT).show();
            }
        }, alarmCode);
    }

    /**
     * Shows a count down dialog before setting alarm to away
     */
    public void showCountDownAlarm() {
        showCountDownDialog(new CountDownView.ViewListener() {
            @Override
            public void onComplete() {
                setArmedAwayView();
                hideCountDownDialog();
                Toast.makeText(getActivity(), R.string.toast_code_alarm_set, Toast.LENGTH_LONG).show();
            }
            @Override
            public void onCancel() {
                hideCountDownDialog();
                Toast.makeText(getActivity(), R.string.toast_alarm_cancelled, Toast.LENGTH_SHORT).show();
            }
        });
    }
}