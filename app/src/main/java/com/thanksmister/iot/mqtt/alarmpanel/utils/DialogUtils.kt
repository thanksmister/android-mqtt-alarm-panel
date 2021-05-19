/*
 * Copyright (c) 2018 ThanksMister LLC
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

package com.thanksmister.iot.mqtt.alarmpanel.utils

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.ContextWrapper
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.thanksmister.iot.mqtt.alarmpanel.R
import com.thanksmister.iot.mqtt.alarmpanel.network.ImageOptions
import com.thanksmister.iot.mqtt.alarmpanel.persistence.Dashboard
import com.thanksmister.iot.mqtt.alarmpanel.persistence.Sensor
import com.thanksmister.iot.mqtt.alarmpanel.persistence.WeatherDao
import com.thanksmister.iot.mqtt.alarmpanel.ui.views.*
import timber.log.Timber

/**
 * Dialog utils
 */
class DialogUtils(base: Context) : ContextWrapper(base), LifecycleObserver {

    private var alertDialog: AlertDialog? = null
    private var dialog: Dialog? = null
    private var screenSaverDialog: Dialog? = null

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun clearDialogs() {
        try {
            dialog?.let {
                if (it.isShowing) {
                    it.dismiss()
                    dialog = null
                }
            }
        } catch (e: IllegalArgumentException) {
            Timber.e(e.message)
        }
        hideAlertDialog()
        hideScreenSaverDialog()
    }

    fun hideScreenSaverDialog(): Boolean {
        try {
            screenSaverDialog?.let {
                if (it.isShowing) {
                    it.dismiss()
                    it.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    screenSaverDialog = null
                    return true
                }
            }
            return false
        } catch (e: IllegalArgumentException) {
            Timber.e(e.message)
            return false
        }
    }

    fun hideAlertDialog() {
        try {
            alertDialog?.let {
                if (it.isShowing) {
                    it.dismiss()
                    alertDialog = null
                }
            }
        } catch (e: IllegalArgumentException) {
            Timber.e(e.message)
        }
    }

    fun showAlertDialog(context: Context, message: String) {
        hideAlertDialog()
        alertDialog = AlertDialog.Builder(context, R.style.CustomAlertDialog)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null)
                .show()
    }

    fun showAlertDialogToDismiss(activity: Context, title: String, message: String) {
        hideAlertDialog()
        alertDialog = AlertDialog.Builder(activity, R.style.CustomAlertDialog)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null)
                .show()
    }

    fun showAlertDialog(activity: Context, title: String, message: String) {
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

    fun showAlertDialog(context: Context, title: String, message: String, onPositiveButton: DialogInterface.OnClickListener,
                        onNegativeButton: DialogInterface.OnClickListener) {
        hideAlertDialog()
        Timber.d("showAlertDialog")
        alertDialog = AlertDialog.Builder(context, R.style.CustomAlertDialog)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, onPositiveButton)
                .setNegativeButton(android.R.string.cancel, onNegativeButton)
                .show()
    }

    fun showAlertDialogCancel(context: Context, message: String, onClickListener: DialogInterface.OnClickListener) {
        hideAlertDialog()
        Timber.d("showAlertDialog")
        alertDialog = AlertDialog.Builder(context, R.style.CustomAlertDialog)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, onClickListener)
                .setNegativeButton(android.R.string.cancel, null)
                .show()
    }

    fun showEditTextDialog(context: Context, value: String, listener: EditTextDialogView.ViewListener) {
        clearDialogs()
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.dialog_edit_text, null, false)
        val editTextDialogView = view.findViewById<EditTextDialogView>(R.id.editTextDialogView)
        editTextDialogView.setListener(listener)
        editTextDialogView.setValue(value)
        alertDialog = AlertDialog.Builder(context, R.style.CustomAlertDialog)
                .setView(editTextDialogView)
                .setPositiveButton(android.R.string.ok) { _, _ -> listener.onComplete(editTextDialogView.value)}
                .setNegativeButton(android.R.string.cancel, { _, _ -> listener.onCancel() })
                .show()
    }

    fun showCodeDialog(activity: Context, confirmCode: Boolean,
                       listener: AlarmCodeView.ViewListener,
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
        dialog = Dialog(activity, R.style.CustomAlertDialog)
        dialog?.setContentView(view)
        dialog?.setOnCancelListener(onCancelListener)
        dialog?.show()
    }

    // TODO make this custom dialog with remove button
    fun showSensorDialog(context: Context, sensor: Sensor, topic: String, listener: SensorDialogView.ViewListener) {
        clearDialogs()
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.dialog_sensor, null, false)
        val sensorDialogView = view.findViewById<SensorDialogView>(R.id.sensorDialogView)
        sensorDialogView.setListener(listener)
        sensorDialogView.setSensor(sensor, topic)
        dialog = Dialog(context, R.style.CustomAlertDialog)
        dialog?.setContentView(view)
        dialog?.show()
    }

    fun showDashboardDialog(context: Context, dashboard: Dashboard, listener: EditDashboardDialogView.ViewListener) {
        clearDialogs()
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.dialog_edit_dashboard, null, false)
        val dashboardDialogView = view.findViewById<EditDashboardDialogView>(R.id.editDashboardDialog)
        dashboardDialogView.setListener(listener)
        dashboardDialogView.setValue(dashboard)
        dialog = Dialog(context, R.style.CustomAlertDialog)
        dialog?.setContentView(view)
        dialog?.show()
    }

    /**
     * Show the screen saver only if the alarm isn't triggered. This shouldn't be an issue
     * with the alarm disabled because the disable time will be longer than this.
     */
    @SuppressLint("InflateParams")
    fun showScreenSaver(activity: AppCompatActivity,
                        showUnsplashSaver: Boolean = false,
                        showClockSaver: Boolean = false,
                        hasWeather: Boolean = false,
                        isImperial: Boolean = false,
                        hasWebScreensaver: Boolean = false,
                        imageOptions: ImageOptions,
                        dataSource: WeatherDao,
                        webUrl: String = "",
                        onClickListener: View.OnClickListener) {
        if (screenSaverDialog == null) {
            clearDialogs() // clear any alert dialogs
            val inflater = activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view = inflater.inflate(R.layout.dialog_screen_saver, null, false)
            val screenSaverView = view.findViewById<ScreenSaverView>(R.id.screenSaverView)
            screenSaverView.init(
                    showUnsplashSaver,
                    showClockSaver,
                    hasWeather,
                    isImperial,
                    hasWebScreensaver,
                    imageOptions,
                    webUrl,
                    dataSource)
            screenSaverView.setOnClickListener(onClickListener)
            screenSaverDialog = buildImmersiveDialog(activity, true, screenSaverView, true)
            screenSaverDialog?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    private fun buildFullscreenDialog(context: AppCompatActivity, cancelable: Boolean, view: View, fullscreen: Boolean): Dialog {
        val dialog: Dialog
        dialog = Dialog(context, R.style.CustomAlertDialogFullscreen)
        dialog.setCancelable(cancelable)
        dialog.setContentView(view)
        //Set the dialog to not focusable (makes navigation ignore us adding the window)
        dialog.window?.setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
        dialog.window?.decorView?.systemUiVisibility = context.window.decorView.systemUiVisibility
        dialog.show()
        dialog.window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
        try {
            val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            wm.updateViewLayout(context.window.decorView, context.window.attributes)
        } catch (e: IllegalArgumentException) {
            Timber.e("Problem starting window decor for screen saver.")
        }
        return dialog
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
        dialog.window?.setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
        dialog.window?.decorView?.systemUiVisibility = context.window.decorView.systemUiVisibility
        dialog.show()
        dialog.window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
        try {
            val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            wm.updateViewLayout(context.window.decorView, context.window.attributes)
        } catch (e: IllegalArgumentException) {
            Timber.e("Problem starting window decor for screen saver.")
        }
        return dialog
    }
}