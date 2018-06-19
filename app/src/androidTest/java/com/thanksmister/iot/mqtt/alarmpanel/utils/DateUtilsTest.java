package com.thanksmister.iot.mqtt.alarmpanel.utils;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Unit tests for the Date utils.
 */
public class DateUtilsTest {
    
    @Test
    public void dayOfWeek() throws Exception {
        long time = 1502766000; // this is short from DarkSky API
        String day = DateUtils.INSTANCE.dayOfWeek(time);
        assertEquals("Tuesday", day);
    }
}