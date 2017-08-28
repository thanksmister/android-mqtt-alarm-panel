package com.thanksmister.iot.mqtt.alarmpanel.network.model;

/**
 * Created by michaelritchie on 8/24/17.
 */

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class InstagramResponse {

    @SerializedName("items")
    @Expose
    private List<InstagramItem> items = null;

    public List<InstagramItem> getItems() {
        return items;
    }

    public void setItems(List<InstagramItem> items) {
        this.items = items;
    }
}

