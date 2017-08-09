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

package com.thanksmister.androidthings.iot.alarmpanel;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.thanksmister.androidthings.iot.alarmpanel.constants.ExceptionCodes;
import com.thanksmister.androidthings.iot.alarmpanel.data.stores.StoreManager;
import com.thanksmister.androidthings.iot.alarmpanel.network.sync.SyncAdapter;
import com.thanksmister.androidthings.iot.alarmpanel.ui.Configuration;
import com.thanksmister.androidthings.iot.alarmpanel.ui.views.AlarmDisableView;
import com.thanksmister.androidthings.iot.alarmpanel.ui.views.ArmOptionsView;

import butterknife.ButterKnife;

import static com.thanksmister.androidthings.iot.alarmpanel.constants.ExceptionCodes.SYNC_ERROR_CODE;

abstract public class BaseActivity extends AppCompatActivity {

    private static IntentFilter syncIntentFilter = new IntentFilter(SyncAdapter.ACTION_SYNC);
    
    private StoreManager storeManager;
    private Configuration configuration;
    private AlertDialog progressDialog;
    private AlertDialog alertDialog;
    private AlertDialog alarmDisarmDialog;
    private AlertDialog armOptionsDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        /*ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }*/

        // TODO add this to manifest?
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //Hide the keyboard when the activity first starts.
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(syncBroadcastReceiver, syncIntentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(syncBroadcastReceiver);
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

        ButterKnife.unbind(this);

        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
            alertDialog = null;
        }

        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    public StoreManager getStoreManager() {
        if (storeManager == null) {
            BaseApplication baseApplication = BaseApplication.getInstance();
            storeManager = new StoreManager(getApplicationContext(), getContentResolver(), baseApplication.getAppSharedPreferences());
        }
        return storeManager;
    }
    
    public Configuration getConfiguration() {
        if (configuration == null) {
            BaseApplication baseApplication = BaseApplication.getInstance();
            configuration = new Configuration(getApplicationContext(), baseApplication.getAppSharedPreferences());
        }
        return configuration;
    }

    public void showProgressDialog(String message, boolean modal) {
        if (progressDialog != null) {
            return;
        }

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.dialog_progress, null, false);
        TextView progressDialogMessage = (TextView) dialogView.findViewById(R.id.progressDialogMessage);
        progressDialogMessage.setText(message);

        progressDialog = new AlertDialog.Builder(this)
                .setCancelable(modal)
                .setView(dialogView)
                .show();
    }

    public void showProgressDialog() {
        if (progressDialog != null) {
            return;
        }

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.dialog_progress_no_text, null, false);
        progressDialog = new AlertDialog.Builder(this)
                .setCancelable(false)
                .setView(dialogView)
                .show();
    }

    public void hideProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    public AlertDialog showAlertDialog(String message) {
        return new AlertDialog.Builder(this)
                .setMessage(Html.fromHtml(message))
                .setNeutralButton(android.R.string.ok, null)
                .show();
    }

    public AlertDialog showAlertDialog(String title, String message) {
        return new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(Html.fromHtml(message))
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    public void showAlertDialog(String title, String message, DialogInterface.OnClickListener onClickListener) {
        if (alertDialog != null) {
            alertDialog.dismiss();
            alertDialog = null;
        }

        alertDialog = new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(Html.fromHtml(message))
                .setPositiveButton(android.R.string.ok, onClickListener)
                .show();
    }

    public void showAlertDialog(String message, DialogInterface.OnClickListener onClickListener) {
        if (alertDialog != null) {
            alertDialog.dismiss();
            alertDialog = null;
        }

        alertDialog = new AlertDialog.Builder(this)
                .setMessage(Html.fromHtml(message))
                .setPositiveButton(android.R.string.ok, onClickListener)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home ) {
            View view = this.getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }

            return true;
        } 
        return false;
    }

    public void hideAlarmDisableDialog() {
        if(alarmDisarmDialog != null) {
            alarmDisarmDialog.dismiss();
            alarmDisarmDialog = null;
        }
    }

    public void hideArmOptionsDialog() {
        if(armOptionsDialog != null) {
            armOptionsDialog.dismiss();
            armOptionsDialog = null;
        }
    }
    
    public void showArmOptionsDialog(ArmOptionsView.ViewListener armListener) {
        hideArmOptionsDialog();
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_alarm_options, null, false);
        final ArmOptionsView optionsView = (ArmOptionsView) view.findViewById(R.id.armOptionsView);
        optionsView.setListener(armListener);
        armOptionsDialog = new AlertDialog.Builder(BaseActivity.this)
                .setCancelable(true)
                .setView(view)
                .show();
    }
    
    public void showAlarmDisableDialog(AlarmDisableView.ViewListener alarmCodeListener, int code) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_alarm_disable, null, false);
        final AlarmDisableView alarmCodeView = view.findViewById(R.id.alarmDisableView);
        alarmCodeView.setListener(alarmCodeListener);
        alarmCodeView.setCode(code);
        alarmCodeView.startCountDown(configuration.getPendingTime());
        alarmDisarmDialog = new AlertDialog.Builder(BaseActivity.this)
                .setCancelable(true)
                .setView(view)
                .show();
    }
    
    public void onError(final String message, final int code) {
        runOnUiThread(new Runnable() {
            public void run() {
                if (code == ExceptionCodes.NETWORK_CONNECTION_ERROR_CODE) {
                    Toast.makeText(BaseActivity.this, message, Toast.LENGTH_LONG).show();
                } else {
                    showAlertDialog("Error: " + message + " Code: " + code);
                }
            }
        });
    }

    /**
     * Override this in the activity to handle sync events
     *
     * @param syncActionType
     * @param errorMessage
     * @param errorCode
     */
    protected void handleStartSync(String syncActionType, String errorMessage, int errorCode) {
        switch (syncActionType) {
            case SyncAdapter.ACTION_TYPE_START:
                break;
            case SyncAdapter.ACTION_TYPE_COMPLETE:
                break;
            case SyncAdapter.ACTION_TYPE_CANCELED:
                break;
            case SyncAdapter.ACTION_TYPE_ERROR:
                onError(errorMessage, errorCode);
                break;
        }
    }

    private BroadcastReceiver syncBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String syncActionType = intent.getStringExtra(SyncAdapter.EXTRA_ACTION_TYPE);
            assert syncActionType != null; // this should never be null

            String extraErrorMessage = "";
            int extraErrorCode = SYNC_ERROR_CODE;

            if (intent.hasExtra(SyncAdapter.EXTRA_ERROR_MESSAGE)) {
                extraErrorMessage = intent.getStringExtra(SyncAdapter.EXTRA_ERROR_MESSAGE);
            }
            if (intent.hasExtra(SyncAdapter.EXTRA_ERROR_CODE)) {
                extraErrorCode = intent.getIntExtra(SyncAdapter.EXTRA_ERROR_CODE, SYNC_ERROR_CODE);
            }
            
            handleStartSync(syncActionType, extraErrorMessage, extraErrorCode);
        }
    };
}