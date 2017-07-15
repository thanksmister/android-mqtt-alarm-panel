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

package com.thanksmister.androidthings.iot.alarmpanel.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LocaleUtils {
    
    private static final String LANGUAGE_ENGLISH = "en";
    private static final String LANGUAGE_SPANISH = "es";
    private static final List<String> supportedLanguages = new ArrayList<String>();
    
    public LocaleUtils(){
    }

    static {
        supportedLanguages.add(LANGUAGE_ENGLISH);
        supportedLanguages.add(LANGUAGE_SPANISH);
    }

    /**
     * Get's the device country code
     * @return
     */
    public static String getDeviceCountryCode() {
        return Locale.getDefault().getCountry();
    }

    /**
     * Returns true if device has supported language
     * @return
     */
    public static boolean hasSupportedLanguage() {
        String defaultLanguage = Locale.getDefault().getLanguage();
        return supportedLanguages.contains(defaultLanguage);
    }

    /**
     * Returns the supported language Spanish (es) or defaults to English (en) for all 
     * other unsupported languages.
     * @return Language code in uppercase
     */
    public static String getSupportLanguage() {
        String defaultLanguage = Locale.getDefault().getLanguage();
        String supportedLanguage = defaultLanguage;
        if (!supportedLanguages.contains(defaultLanguage)) {
            supportedLanguage = LANGUAGE_ENGLISH;
        }
        return supportedLanguage;
    }
}