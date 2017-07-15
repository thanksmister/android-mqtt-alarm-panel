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

import android.content.Context;
import android.os.Build;
import android.provider.Settings.Secure;
import android.text.TextUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import timber.log.Timber;

public class DeviceUtils {

    private DeviceUtils(){
    }
    
    public static String getDeviceIdHash(Context context) {
        
        String deviceId = null;

        // get internal ANDROID_ID from device 
        try {
            Timber.d("Fetching Android ID");
            deviceId = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
        } catch (Exception e) {
            Timber.e("ANDROID_ID Error: " + e.getMessage());
        }

        // if ANDROID_ID fails then get SERIAL id
        if (deviceId == null) {
            try {
                deviceId = Build.SERIAL;
            } catch (Exception e) {
                Timber.e("Build.SERIAL Error: " + e.getMessage());
            }
        }
        
        if(TextUtils.isEmpty(deviceId)) {
            deviceId = String.valueOf(System.currentTimeMillis());
        }

        return md5(deviceId);
    }

    // https://stackoverflow.com/questions/4846484/md5-hashing-in-android
    private static String md5(final String s) {
        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest
                    .getInstance(MD5);
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }
}