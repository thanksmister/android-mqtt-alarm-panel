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

import java.lang.Double.parseDouble

object StringUtils {

    private val strSeparator = ","

    fun convertArrayToString(array: Array<String>): String {
        val str = StringBuilder()
        for (i in array.indices) {
            str.append(array[i])
            // Do not append comma at the end of last element
            if (i < array.size - 1) {
                str.append(strSeparator)
            }
        }
        return str.toString()
    }

    fun isDouble(value: String?): Boolean {
        if(value.isNullOrEmpty()) return false
        var numeric = true
        try {
            parseDouble(value)
        } catch (e: NumberFormatException) {
            numeric = false
        }
        return numeric
    }

    fun stringToDouble(value: String?): Double {
        var num = 0.0
        if(value.isNullOrEmpty()) return num
        try {
            num = parseDouble(value)
        } catch (e: NumberFormatException) {
            // na-da
        }
        return num
    }

    fun convertStringToArray(str: String): Array<String> {
        return str.split(strSeparator.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
    }
}