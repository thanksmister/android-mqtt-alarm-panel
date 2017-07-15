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

package com.thanksmister.androidthings.iot.alarmpanel.ui;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import dpreference.DPreference;
import static junit.framework.Assert.assertEquals;

/**
 * Configuration unit tests
 */
public class ConfigurationTest {
    
    Context appContext;
    DPreference preference;
    Configuration configuration;
    
    @Before
    public void setUp() throws Exception {
        this.appContext = InstrumentationRegistry.getTargetContext();
        preference = new DPreference(appContext, "test");
        configuration = new Configuration(appContext, preference);
        configuration.reset();
    }

    @After
    public void tearDown() throws Exception {
        configuration.reset();
    }

    @Test
    public void showWeatherModule() throws Exception {
        boolean value = configuration.showWeatherModule();
        assertEquals(false, value);

        configuration.setShowWeatherModule(true);

        value = configuration.showWeatherModule();
        assertEquals(true, value);
    }

    @Test
    public void setLon() throws Exception {

    }

    @Test
    public void setLat() throws Exception {

    }

    @Test
    public void getLatitude() throws Exception {

    }

    @Test
    public void getLongitude() throws Exception {

    }

    @Test
    public void getIsCelsius() throws Exception {

    }

    @Test
    public void getWeatherUnits() throws Exception {

    }

    @Test
    public void setIsCelsius() throws Exception {

    }

    @Test
    public void getDarkSkyKey() throws Exception {

    }

    @Test
    public void setDarkSkyKey() throws Exception {

    }

    @Test
    public void getUserName() throws Exception {

    }

    @Test
    public void setUserName() throws Exception {

    }

    @Test
    public void getPassword() throws Exception {

    }

    @Test
    public void setPassword() throws Exception {

    }

    @Test
    public void getClientId() throws Exception {

    }

    @Test
    public void setClientId() throws Exception {

    }

    @Test
    public void getPort() throws Exception {

    }

    @Test
    public void setPort() throws Exception {

    }

    @Test
    public void getBroker() throws Exception {

    }

    @Test
    public void setBroker() throws Exception {

    }

    @Test
    public void getTopic() throws Exception {

    }

    @Test
    public void setTopic() throws Exception {

    }

    @Test
    public void getAlarmCode() throws Exception {

    }

    @Test
    public void setAlarmCode() throws Exception {

    }

    @Test
    public void isArmed() throws Exception {

    }

    @Test
    public void setArmed() throws Exception {

    }

    @Test
    public void getAlarmMode() throws Exception {

    }

    @Test
    public void setAlarmMode() throws Exception {

    }

    @Test
    public void reset() throws Exception {

    }
}