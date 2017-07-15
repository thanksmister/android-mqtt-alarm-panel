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

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Test the if latitude and longitude are valid
 */
public class LocationUtilsTest {

    @Test
    public void latitudeValid() throws Exception {
        boolean value = LocationUtils.latitudeValid(null);
        assertEquals(false, value);
        value = LocationUtils.latitudeValid("0");
        assertEquals(false, value);
        value = LocationUtils.latitudeValid("180");
        assertEquals(false, value);
        value = LocationUtils.latitudeValid("90");
        assertEquals(true, value);
        value = LocationUtils.latitudeValid("-90");
        assertEquals(true, value);
    }

    @Test
    public void longitudeValid() throws Exception {
        boolean value = LocationUtils.longitudeValid(null);
        assertEquals(false, value);
        value = LocationUtils.longitudeValid("0");
        assertEquals(false, value);
        value = LocationUtils.longitudeValid("181");
        assertEquals(false, value);
        value = LocationUtils.longitudeValid("180");
        assertEquals(true, value);
        value = LocationUtils.longitudeValid("-180");
        assertEquals(true, value);
    }

    @Test
    public void coordinatesValid() throws Exception {
        boolean value = LocationUtils.coordinatesValid(null, null);
        assertEquals(false, value);
        value = LocationUtils.coordinatesValid("0", "0");
        assertEquals(false, value);
        value = LocationUtils.coordinatesValid("91", "181");
        assertEquals(false, value);
        value = LocationUtils.coordinatesValid("90", "180");
        assertEquals(true, value);
        value = LocationUtils.coordinatesValid("-90", "-180");
        assertEquals(true, value);
    }
}