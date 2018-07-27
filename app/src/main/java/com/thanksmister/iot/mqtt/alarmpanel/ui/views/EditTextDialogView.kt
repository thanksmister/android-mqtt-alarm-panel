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

package com.thanksmister.iot.mqtt.alarmpanel.ui.views

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.widget.*
import kotlinx.android.synthetic.main.dialog_edit_text.view.*
import timber.log.Timber

class EditTextDialogView : RelativeLayout {

    internal var editTextDialogListener: ViewListener? = null
    internal var value: String = ""

    interface ViewListener {
        fun onComplete(value: String)
        fun onCancel()
    }

    fun setListener(listener: ViewListener) {
        this.editTextDialogListener = listener
    }

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    fun setValue(value: String) {
        editTextValue?.setText(value)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        editTextValue?.addTextChangedListener(object:TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                value = s.toString()
                Timber.d("Edit Value: " + value)
            }
            override fun afterTextChanged(s: Editable?) {
            }
        })
    }
}