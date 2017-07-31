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

package com.thanksmister.androidthings.iot.alarmpanel.network.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Currently {

    @SerializedName("time")
    @Expose
    private Integer time;
    @SerializedName("summary")
    @Expose
    private String summary;
    @SerializedName("icon")
    @Expose
    private String icon;
    @SerializedName("precipIntensity")
    @Expose
    private Integer precipIntensity;
    @SerializedName("precipProbability")
    @Expose
    private Integer precipProbability;
    @SerializedName("temperature")
    @Expose
    private Double temperature;
    @SerializedName("apparentTemperature")
    @Expose
    private Double apparentTemperature;
    @SerializedName("dewPoint")
    @Expose
    private Double dewPoint;
    @SerializedName("humidity")
    @Expose
    private Double humidity;
    @SerializedName("windSpeed")
    @Expose
    private Double windSpeed;
    @SerializedName("windGust")
    @Expose
    private Double windGust;
    @SerializedName("windBearing")
    @Expose
    private Integer windBearing;
    @SerializedName("cloudCover")
    @Expose
    private Double cloudCover;
    @SerializedName("pressure")
    @Expose
    private Double pressure;
    @SerializedName("ozone")
    @Expose
    private Double ozone;
    @SerializedName("uvIndex")
    @Expose
    private Integer uvIndex;

    public Integer getTime() {
        return time;
    }

    public void setTime(Integer time) {
        this.time = time;
    }

    public Currently withTime(Integer time) {
        this.time = time;
        return this;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public Currently withSummary(String summary) {
        this.summary = summary;
        return this;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Currently withIcon(String icon) {
        this.icon = icon;
        return this;
    }

    public Integer getPrecipIntensity() {
        return precipIntensity;
    }

    public void setPrecipIntensity(Integer precipIntensity) {
        this.precipIntensity = precipIntensity;
    }

    public Currently withPrecipIntensity(Integer precipIntensity) {
        this.precipIntensity = precipIntensity;
        return this;
    }

    public Integer getPrecipProbability() {
        return precipProbability;
    }

    public void setPrecipProbability(Integer precipProbability) {
        this.precipProbability = precipProbability;
    }

    public Currently withPrecipProbability(Integer precipProbability) {
        this.precipProbability = precipProbability;
        return this;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Currently withTemperature(Double temperature) {
        this.temperature = temperature;
        return this;
    }

    public Double getApparentTemperature() {
        return apparentTemperature;
    }

    public void setApparentTemperature(Double apparentTemperature) {
        this.apparentTemperature = apparentTemperature;
    }

    public Currently withApparentTemperature(Double apparentTemperature) {
        this.apparentTemperature = apparentTemperature;
        return this;
    }

    public Double getDewPoint() {
        return dewPoint;
    }

    public void setDewPoint(Double dewPoint) {
        this.dewPoint = dewPoint;
    }

    public Currently withDewPoint(Double dewPoint) {
        this.dewPoint = dewPoint;
        return this;
    }

    public Double getHumidity() {
        return humidity;
    }

    public void setHumidity(Double humidity) {
        this.humidity = humidity;
    }

    public Currently withHumidity(Double humidity) {
        this.humidity = humidity;
        return this;
    }

    public Double getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(Double windSpeed) {
        this.windSpeed = windSpeed;
    }

    public Currently withWindSpeed(Double windSpeed) {
        this.windSpeed = windSpeed;
        return this;
    }

    public Double getWindGust() {
        return windGust;
    }

    public void setWindGust(Double windGust) {
        this.windGust = windGust;
    }

    public Currently withWindGust(Double windGust) {
        this.windGust = windGust;
        return this;
    }

    public Integer getWindBearing() {
        return windBearing;
    }

    public void setWindBearing(Integer windBearing) {
        this.windBearing = windBearing;
    }

    public Currently withWindBearing(Integer windBearing) {
        this.windBearing = windBearing;
        return this;
    }

    public Double getCloudCover() {
        return cloudCover;
    }

    public void setCloudCover(Double cloudCover) {
        this.cloudCover = cloudCover;
    }

    public Currently withCloudCover(Double cloudCover) {
        this.cloudCover = cloudCover;
        return this;
    }

    public Double getPressure() {
        return pressure;
    }

    public void setPressure(Double pressure) {
        this.pressure = pressure;
    }

    public Currently withPressure(Double pressure) {
        this.pressure = pressure;
        return this;
    }

    public Double getOzone() {
        return ozone;
    }

    public void setOzone(Double ozone) {
        this.ozone = ozone;
    }

    public Currently withOzone(Double ozone) {
        this.ozone = ozone;
        return this;
    }

    public Integer getUvIndex() {
        return uvIndex;
    }

    public void setUvIndex(Integer uvIndex) {
        this.uvIndex = uvIndex;
    }

    public Currently withUvIndex(Integer uvIndex) {
        this.uvIndex = uvIndex;
        return this;
    }

}
