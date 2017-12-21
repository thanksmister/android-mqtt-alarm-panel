package com.thanksmister.iot.mqtt.alarmpanel.utils;

public class StringUtils {

    private static String strSeparator = ",";

    public static String convertArrayToString(String[] array){
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            str.append(array[i]);
            // Do not append comma at the end of last element
            if(i<array.length-1){
                str.append(strSeparator);
            }
        }
        return str.toString();
    }

    public static String[] convertStringToArray(String str){
        return str.split(strSeparator);
    }
}