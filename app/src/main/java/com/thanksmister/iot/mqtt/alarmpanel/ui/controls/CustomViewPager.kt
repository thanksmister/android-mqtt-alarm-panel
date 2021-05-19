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

package com.thanksmister.iot.mqtt.alarmpanel.ui.controls

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent

import androidx.viewpager.widget.ViewPager

/**
 * https://stackoverflow.com/questions/31000076/how-to-make-swipe-disable-in-a-view-pager-in-android
 */
class CustomViewPager(context: Context, attrs: AttributeSet) : ViewPager(context, attrs) {

    private var swipeEnabled: Boolean = false

    override fun performClick(): Boolean {
        return swipeEnabled && super.performClick()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        performClick()
        return super.onTouchEvent(event)
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        return this.swipeEnabled && super.onInterceptTouchEvent(event)
    }

    fun setPagingEnabled(enabled: Boolean) {
        this.swipeEnabled = enabled
    }
}