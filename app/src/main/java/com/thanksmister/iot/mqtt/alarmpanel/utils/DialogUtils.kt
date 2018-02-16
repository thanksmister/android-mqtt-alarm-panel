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

package com.thanksmister.iot.mqtt.alarmpanel.utils

import android.app.Dialog
import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import android.content.Context
import android.content.ContextWrapper
import android.content.DialogInterface
import android.graphics.Rect
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.Html
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.TextView

import com.thanksmister.iot.mqtt.alarmpanel.R
import com.thanksmister.iot.mqtt.alarmpanel.network.ImageOptions
import com.thanksmister.iot.mqtt.alarmpanel.network.model.Daily
import com.thanksmister.iot.mqtt.alarmpanel.ui.views.AlarmCodeView
import com.thanksmister.iot.mqtt.alarmpanel.ui.views.AlarmDisableView
import com.thanksmister.iot.mqtt.alarmpanel.ui.views.ArmOptionsView
import com.thanksmister.iot.mqtt.alarmpanel.ui.views.ExtendedForecastView
import com.thanksmister.iot.mqtt.alarmpanel.ui.views.ScreenSaverView
import com.thanksmister.iot.mqtt.alarmpanel.ui.views.SettingsCodeView
import timber.log.Timber

/**
 * Dialog utils
 */
class DialogUtils(base: Context?) : ContextWrapper(base), LifecycleObserver {

    private var alertDialog: AlertDialog? = null
    private var dialog: Dialog? = null
    private var screenSaverDialog: Dialog? = null

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun clearDialogs() {
        if (dialog != null && dialog!!.isShowing) {
            dialog!!.dismiss()
            dialog = null
        }

        if (alertDialog != null && alertDialog!!.isShowing) {
            alertDialog!!.dismiss()
            alertDialog = null
        }
        if (screenSaverDialog != null && screenSaverDialog!!.isShowing) {
            screenSaverDialog!!.dismiss()
            screenSaverDialog = null
        }
    }

    fun hideScreenSaverDialog() {
        if (screenSaverDialog != null && screenSaverDialog!!.isShowing) {
            screenSaverDialog!!.dismiss()
            screenSaverDialog = null
        }
    }

    fun hideAlertDialog() {
        if (alertDialog != null && alertDialog!!.isShowing) {
            alertDialog!!.dismiss()
            alertDialog = null
        }
    }

    fun showAlertDialog(activity: AppCompatActivity, message: String) {
        hideAlertDialog()
        alertDialog = AlertDialog.Builder(activity, R.style.CustomAlertDialog)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null)
                .show()
    }

    fun showAlertDialogToDismiss(activity: AppCompatActivity, title: String, message: String) {
        hideAlertDialog()
        alertDialog = AlertDialog.Builder(activity, R.style.CustomAlertDialog)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null)
                .show()
    }

    fun showAlertDialog(activity: AppCompatActivity, title: String, message: String) {
        hideAlertDialog()
        alertDialog = AlertDialog.Builder(activity, R.style.CustomAlertDialog)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null)
                .show()
    }

    fun showAlertDialog(context: Context, message: String, onClickListener: DialogInterface.OnClickListener) {
        hideAlertDialog()
        Timber.d("showAlertDialog")
        alertDialog = AlertDialog.Builder(context, R.style.CustomAlertDialog)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, onClickListener)
                .show()
    }

    fun showArmOptionsDialog(activity: AppCompatActivity, armListener: ArmOptionsView.ViewListener) {
        clearDialogs()
        val inflater = activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.dialog_alarm_options, null, false)
        val displayRectangle = Rect()
        val window = activity.window
        window.decorView.getWindowVisibleDisplayFrame(displayRectangle)
        val density = activity.resources.displayMetrics.densityDpi
        if (density == DisplayMetrics.DENSITY_TV) {
            view.minimumWidth = (displayRectangle.width() * 0.6f).toInt()
            view.minimumHeight = (displayRectangle.height() * 0.7f).toInt()
        } else if (density == DisplayMetrics.DENSITY_MEDIUM) {
            view.minimumWidth = (displayRectangle.width() * 0.5f).toInt()
            view.minimumHeight = (displayRectangle.height() * 0.6f).toInt()
        } else {
            view.minimumWidth = (displayRectangle.width() * 0.7f).toInt()
            view.minimumHeight = (displayRectangle.height() * 0.8f).toInt()
        }
        val optionsView = view.findViewById<ArmOptionsView>(R.id.armOptionsView)
        optionsView.setListener(armListener)
        dialog = buildImmersiveDialog(activity, true, view, false)
    }

    /**
     * Shows the disable alarm dialog with countdown. It is important that this
     * dialog only be shown once and not relaunched when already displayed as
     * it resets the timer.
     */
    fun showAlarmDisableDialog(activity: AppCompatActivity, alarmCodeListener: AlarmDisableView.ViewListener,
                               code: Int, beep: Boolean, timeRemaining: Int, systemSounds: Boolean) {
        clearDialogs()
        val inflater = activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.dialog_alarm_disable, null, false)
        val alarmCodeView = view.findViewById<AlarmDisableView>(R.id.alarmDisableView)
        alarmCodeView.setListener(alarmCodeListener)
        alarmCodeView.setCode(code)
        alarmCodeView.setUseSound(systemSounds)
        alarmCodeView.startCountDown(timeRemaining)
        if (beep) {
            alarmCodeView.playContinuousBeep()
        }
        dialog = buildImmersiveDialog(activity, true, view, false)
        dialog!!.setOnDismissListener { alarmCodeView.destroySoundUtils() }
    }

    fun showSettingsCodeDialog(activity: AppCompatActivity, code: Int, listener: SettingsCodeView.ViewListener, systemSounds: Boolean) {
        clearDialogs()
        val inflater = activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.dialog_settings_code, null, false)
        val settingsCodeView = view.findViewById<SettingsCodeView>(R.id.settingsCodeView)
        settingsCodeView.setCode(code)
        settingsCodeView.setListener(listener)
        settingsCodeView.setUseSound(systemSounds)
        dialog = buildImmersiveDialog(activity, true, view, false)
    }

    fun showCodeDialog(activity: AppCompatActivity, confirmCode: Boolean, listener: AlarmCodeView.ViewListener,
                       onCancelListener: DialogInterface.OnCancelListener, systemSounds: Boolean) {
        clearDialogs()
        val inflater = activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.dialog_alarm_code_set, null, false)
        val alarmCodeView = view.findViewById<AlarmCodeView>(R.id.alarmCodeView)
        val titleTextView = alarmCodeView.findViewById<TextView>(R.id.codeTitle)
        if (confirmCode) {
            titleTextView.setText(R.string.text_renter_alarm_code_title)
        }
        alarmCodeView.setListener(listener)
        alarmCodeView.setUseSound(systemSounds)
        dialog = buildImmersiveDialog(activity, true, view, false)
        dialog!!.setOnCancelListener(onCancelListener)
    }

    fun showExtendedForecastDialog(activity: AppCompatActivity, daily: Daily) {
        clearDialogs()
        val inflater = activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.dialog_extended_forecast, null, false)
        val displayRectangle = Rect()
        val window = activity.window
        window.decorView.getWindowVisibleDisplayFrame(displayRectangle)
        view.minimumWidth = (displayRectangle.width() * 0.7f).toInt()
        val density = activity.resources.displayMetrics.densityDpi
        if (density == DisplayMetrics.DENSITY_MEDIUM) {
            view.minimumHeight = (displayRectangle.height() * 0.6f).toInt()
        } else {
            view.minimumHeight = (displayRectangle.height() * 0.8f).toInt()
        }
        val extendedForecastView = view.findViewById<ExtendedForecastView>(R.id.extendedForecastView)
        extendedForecastView.setExtendedForecast(daily)
        dialog = buildImmersiveDialog(activity, true, view, false)
    }

    /**
     * Show the screen saver only if the alarm isn't triggered. This shouldn't be an issue
     * with the alarm disabled because the disable time will be longer than this.
     */
    fun showScreenSaver(activity: AppCompatActivity, showPhotoScreenSaver: Boolean, showClockScreenSaver: Boolean, options:ImageOptions, listener: ScreenSaverView.ViewListener, onClickListener: View.OnClickListener) {

        if (screenSaverDialog != null && screenSaverDialog!!.isShowing) {
            return
        }
        clearDialogs()
        val inflater = activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.dialog_screen_saver, null, false)
        val screenSaverView = view.findViewById<ScreenSaverView>(R.id.screenSaverView)
        screenSaverView.setScreenSaver(showPhotoScreenSaver, showClockScreenSaver, options)
        screenSaverView.setListener(listener)
        screenSaverView.setOnClickListener(onClickListener)
        screenSaverDialog = buildImmersiveDialog(activity, true, screenSaverView, true)
    }

    // immersive dialogs without navigation
    // https://stackoverflow.com/questions/22794049/how-do-i-maintain-the-immersive-mode-in-dialogs
    private fun buildImmersiveDialog(context: AppCompatActivity, cancelable: Boolean, view: View, fullscreen: Boolean): Dialog {
        val dialog: Dialog
        if (fullscreen) {
            dialog = Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        } else {
            dialog = Dialog(context, R.style.CustomAlertDialog)
        }
        dialog.setCancelable(cancelable)
        dialog.setContentView(view)
        //Set the dialog to not focusable (makes navigation ignore us adding the window)
        dialog.window!!.setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
        dialog.window!!.decorView.systemUiVisibility = context.window.decorView.systemUiVisibility
        dialog.show()
        dialog.window!!.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        wm.updateViewLayout(context.window.decorView, context.window.attributes)
        return dialog
    }
}