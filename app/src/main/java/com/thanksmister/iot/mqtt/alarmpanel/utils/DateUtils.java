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

package com.thanksmister.iot.mqtt.alarmpanel.utils;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;


/**
 * Date utils
 */
public final class DateUtils {

    public static int SECONDS_VALUE = 60000;
    public static int MINUTES_VALUE = 1800000;

    private DateUtils(){
    }
    
    public static String parseCreatedAtDate(String dateString) {
        String fmt = DateTimeFormat.patternForStyle("SS", Locale.getDefault());
        DateTime dateTime = new DateTime(dateString);
        dateTime.toLocalDateTime();
        return dateTime.toLocalDateTime().toString(fmt);
    }

    public static String generateCreatedAtDate() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US);
        return dateFormat.format(new Date());
    }

    /**
     * This converts the milliseconds to a day of the week, but we try to account
     * for time that is shorter than expected from DarkSky API . 
     * @param apiTime
     * @return
     */
    public static String dayOfWeek(long apiTime) {
        long time = apiTime;
        if(String.valueOf(apiTime).length() == 10) {
            time = (long)apiTime*1000;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE", Locale.getDefault());
        return sdf.format(new Date(time));
    }
    
    public static String convertInactivityTime(long inactivityValue) {
        if(inactivityValue < SECONDS_VALUE) {
            return String.valueOf(TimeUnit.MILLISECONDS.toSeconds(inactivityValue));
        } else if(inactivityValue > MINUTES_VALUE) {
            return String.valueOf(TimeUnit.MILLISECONDS.toHours(inactivityValue));
        } else {
            return String.valueOf(TimeUnit.MILLISECONDS.toMinutes(inactivityValue));
        }
    }
}