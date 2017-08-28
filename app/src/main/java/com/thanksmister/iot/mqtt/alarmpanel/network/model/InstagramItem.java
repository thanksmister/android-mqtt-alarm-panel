package com.thanksmister.iot.mqtt.alarmpanel.network.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by michaelritchie on 8/24/17.
 */

public class InstagramItem {
    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("code")
    @Expose
    private String code;
    @SerializedName("user")
    @Expose
    private InstagramUser user;
    @SerializedName("images")
    @Expose
    private InstagramImages images;
    @SerializedName("created_time")
    @Expose
    private String createdTime;
    @SerializedName("caption")
    @Expose
    private InstagramCaption caption;
    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("link")
    @Expose
    private String link;
    @SerializedName("location")
    @Expose
    private InstagramLocation location;
    @SerializedName("alt_media_url")
    @Expose
    private Object altMediaUrl;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public InstagramUser getUser() {
        return user;
    }

    public void setUser(InstagramUser user) {
        this.user = user;
    }

    public InstagramImages getImages() {
        return images;
    }

    public void setImages(InstagramImages images) {
        this.images = images;
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }

    public InstagramCaption getCaption() {
        return caption;
    }

    public void setCaption(InstagramCaption caption) {
        this.caption = caption;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public InstagramLocation getLocation() {
        return location;
    }

    public void setLocation(InstagramLocation location) {
        this.location = location;
    }

    public Object getAltMediaUrl() {
        return altMediaUrl;
    }

    public void setAltMediaUrl(Object altMediaUrl) {
        this.altMediaUrl = altMediaUrl;
    }
}
