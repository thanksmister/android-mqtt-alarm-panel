package com.thanksmister.iot.mqtt.alarmpanel.network;

import android.text.TextUtils;

import com.thanksmister.iot.mqtt.alarmpanel.utils.AlarmUtils;
import com.thanksmister.iot.mqtt.alarmpanel.utils.DeviceUtils;

import java.util.Locale;

import dpreference.DPreference;
import timber.log.Timber;

/**
 * For original implementation see https://github.com/androidthings/sensorhub-cloud-iot.
 */
public class MQTTOptions {
    
    private static final String SSL_BROKER_URL_FORMAT = "ssl://%s:%d";
    private static final String TCP_BROKER_URL_FORMAT = "tcp://%s:%d";
    private static final String HTTP_BROKER_URL_FORMAT = "%s:%d";
    
    public static final String PREF_USERNAME = "pref_username";
    public static final String PREF_COMMAND_TOPIC = "pref_command_topic";
    public static final String PREF_STATE_TOPIC = "pref_state_topic";
    public static final String PREF_TLS_CONNECTION = "pref_tls_connection";
    public static final String PREF_PASSWORD = "pref_password";
    public static final String PREF_PORT = "pref_port";
    public static final String PREF_CLIENT_ID = "pref_client_id";
    public static final String PREF_BROKER = "pref_broker";
    private static final String MQTT_OPTIONS_UPDATED = "pref_mqtt_options_updated";
    
    /**
     * Client id.
     */
    private String clientId;
    
    /**
     * Broker address.
     */
    private String broker;

    /**
     * Port number.
     */
    private int port;

    /**
     * MQTT State Topic.
     */
    private String stateTopic;

    /**
     * MQTT Command Topic.
     */
    private String commandTopic;

    /**
     * Username.
     */
    private String username;

    /**
     * Password.
     */
    private String password;

    /**
     * TSL connection.
     */
    private boolean tlsConnection;

    private final DPreference sharedPreferences;
    
    public String getBrokerUrl() {
        if(!TextUtils.isEmpty(broker)) {
            if (broker.contains("http://") || broker.contains("https://")) {
                return String.format(Locale.getDefault(), HTTP_BROKER_URL_FORMAT, broker, port);
            } else if (tlsConnection) {
                return String.format(Locale.getDefault(), SSL_BROKER_URL_FORMAT, broker, port);
            } else {
                return String.format(Locale.getDefault(), TCP_BROKER_URL_FORMAT, broker, port);
            }
        }
        return "";
    }

    public String getBroker() {
        return broker;
    }

    public String getClientId() {
        return clientId;
    }

    public String getStateTopic() {
        return stateTopic;
    }

    public String getCommandTopic() {
        return commandTopic;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public int getPort() {
        return port;
    }

    public boolean getTlsConnection() {
        return tlsConnection;
    }
    
    private MQTTOptions(DPreference sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    public boolean isValid() {
        if(tlsConnection) {
            return !TextUtils.isEmpty(broker) &&
                    !TextUtils.isEmpty(clientId) &&
                    !TextUtils.isEmpty(stateTopic) &&
                    !TextUtils.isEmpty(commandTopic) &&
                    !TextUtils.isEmpty(username) &&
                    !TextUtils.isEmpty(password);
        }
        return !TextUtils.isEmpty(broker) &&
                !TextUtils.isEmpty(clientId) &&
                !TextUtils.isEmpty(stateTopic) &&
                !TextUtils.isEmpty(commandTopic);
    }
    
    /**
     * Construct a MqttOptions object from Configuration.
     */
    public static MQTTOptions from(DPreference sharedPreferences) {
        try {
            MQTTOptions options = new MQTTOptions(sharedPreferences);
            
            String clientId = sharedPreferences.getPrefString(PREF_CLIENT_ID, null);
            if(TextUtils.isEmpty(clientId)) {
                clientId = DeviceUtils.getUuIdHash();
            }
            options.clientId = clientId;
            options.broker = sharedPreferences.getPrefString(PREF_BROKER, "");
            options.port = sharedPreferences.getPrefInt(PREF_PORT, AlarmUtils.PORT);
            options.username = sharedPreferences.getPrefString(PREF_USERNAME, null);
            options.password = sharedPreferences.getPrefString(PREF_PASSWORD, null);
            options.stateTopic = sharedPreferences.getPrefString(PREF_STATE_TOPIC, AlarmUtils.STATE_TOPIC);
            options.commandTopic = sharedPreferences.getPrefString(PREF_COMMAND_TOPIC, AlarmUtils.COMMAND_TOPIC);
            options.tlsConnection = sharedPreferences.getPrefBoolean(PREF_TLS_CONNECTION, false);
            return options;
        } catch (Exception e) {
            throw new IllegalArgumentException("While processing configuration options", e);
        }
    }

    public void setUsername(String value) {
        this.sharedPreferences.setPrefString(PREF_USERNAME, value);
        setOptionsUpdated(true);
    }
    
    public void setClientId(String value) {
        this.sharedPreferences.setPrefString(PREF_CLIENT_ID, value);
        setOptionsUpdated(true);
    }
    
    public void setBroker(String value) {
        this.sharedPreferences.setPrefString(PREF_BROKER, value);
        setOptionsUpdated(true);
    }

    public void setPort(int value) {
        this.sharedPreferences.setPrefInt(PREF_PORT, value);
        setOptionsUpdated(true);
    }

    public void setPassword(String value) {
        this.sharedPreferences.setPrefString(PREF_PASSWORD, value);
        setOptionsUpdated(true);
    }

    public void setStateTopic(String value) {
        this.sharedPreferences.setPrefString(PREF_STATE_TOPIC, value);
        setOptionsUpdated(true);
    }

    public void setCommandTopic(String value) {
        this.sharedPreferences.setPrefString(PREF_COMMAND_TOPIC, value);
        setOptionsUpdated(true);
    }

    public void setTlsConnection(boolean value) {
        this.sharedPreferences.setPrefBoolean(PREF_TLS_CONNECTION, value);
        setOptionsUpdated(true);
    }

    public void setOptionsUpdated(boolean value) {
        this.sharedPreferences.setPrefBoolean(MQTT_OPTIONS_UPDATED, value);
    }
    
    public boolean hasUpdates() {
        boolean updates = sharedPreferences.getPrefBoolean(MQTT_OPTIONS_UPDATED, false);
        Timber.d("Updates: " + updates);
        return updates;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof MQTTOptions)) {
            return false;
        }
        MQTTOptions o = (MQTTOptions) obj;
        return TextUtils.equals(clientId , o.clientId)
                && TextUtils.equals(broker, o.broker)
                && TextUtils.equals(stateTopic, o.stateTopic)
                && TextUtils.equals(commandTopic, o.commandTopic)
                && TextUtils.equals(username, o.username)
                && TextUtils.equals(password, o.password)
                && port == o.port
                && tlsConnection == o.tlsConnection;
    }
}