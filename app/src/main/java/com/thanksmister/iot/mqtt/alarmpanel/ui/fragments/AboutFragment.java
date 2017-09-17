/*
 * Copyright (c) 2017 ThanksMister LLC
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

package com.thanksmister.iot.mqtt.alarmpanel.ui.fragments;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.thanksmister.iot.mqtt.alarmpanel.BaseActivity;
import com.thanksmister.iot.mqtt.alarmpanel.BaseFragment;
import com.thanksmister.iot.mqtt.alarmpanel.R;

import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

public class AboutFragment extends BaseFragment {

    public static final String GOOGLE_PLAY_RATING = "com.thanksmister.iot.mqtt.alarmpanel";
    public static final String GITHUB_URL = "https://github.com/thanksmister/android-mqtt-alarm-panel";
    public static final String EMAIL_ADDRESS = "mister@thanksmister.com";

    @OnClick(R.id.sendFeedbackButton)
    public void sendButtonClicked() {
        feedback();
    }

    @OnClick(R.id.rateApplicationButton)
    public void rateButtonClicked() {
        rate();
    }

    @OnClick(R.id.licenseButton)
    public void licenseButtonClicked() {
        showLicense();
    }
    
    @OnClick(R.id.githubButton)
    public void githubButtonClicked() {
        showGitHub();
    }
    
    private String versionNumber;

    public static AboutFragment newInstance() {
        return new AboutFragment();
    }

    public AboutFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_about, container, false);
    }

    @Override
    public void onViewCreated(View fragmentView, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(fragmentView, savedInstanceState);
        try {
            PackageInfo packageInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
            TextView versionName = getActivity().findViewById(R.id.versionName);
            versionNumber = " v" + packageInfo.versionName;
            versionName.setText(versionNumber);
        } catch (PackageManager.NameNotFoundException e) {
            Timber.e(e.getMessage());
        }
    }
    
    @Override
    public void onDetach() {
        super.onDetach();
        ButterKnife.unbind(this);
    }

    private void rate() {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + GOOGLE_PLAY_RATING)));
        } catch (android.content.ActivityNotFoundException ex) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + GOOGLE_PLAY_RATING)));
        }
    }

    private void showGitHub() {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(GITHUB_URL)));
    }

    private void feedback() {
        Intent Email = new Intent(Intent.ACTION_SENDTO);
        Email.setType("text/email");
        Email.setData(Uri.parse("mailto:" + EMAIL_ADDRESS));
        Email.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.mail_to_subject_text) + " " + versionNumber);
        startActivity(Intent.createChooser(Email, getString(R.string.mail_subject_text)));
    }

    private void showLicense() {
        if(isAdded()) {
            ((BaseActivity) getActivity()).showAlertDialog(getString(R.string.license));
        }
    }
}