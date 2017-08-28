package com.thanksmister.iot.mqtt.alarmpanel;

public class AlarmSounds {

    public static final double REST = -1;
    public static final double G4 = 391.995;
    public static final double E4_FLAT = 311.127;
    public static final double D_SHARP = 622.254;
    public static final double A4 = 880;
    public static final double A3 = 440;

    public static final double[] DOOR_OPEN_ARMED = {
            G4, E4_FLAT, REST
    };

    public static final double[] PENDING_ALARM = {
            REST, A4, REST, REST,  REST, REST, A4, REST, REST,  REST, REST, A4, REST, REST, REST, REST, A4, REST, REST,  REST, REST, A4, REST, REST,  REST, REST, A4, REST, REST,  REST, REST, A4, REST, REST,  REST, REST, A4
    };

    private AlarmSounds() {
        //no instance
    }
}