*** 01/10/2021 Update ***

The documentation has been updated for the new verson of the Alarm Panel currently in beta testing.  You may side-load this application from the release section, it is labeled `pre-release`.  This version is not yet available on the Google Store. 


# MQTT Alarm Panel for Home Automation Platforms

This project is an MQTT Alarm Control Panel was originally created for use with [Home Assistant's Manual Alarm Control Panel](https://home-assistant.io/components/alarm_control_panel.manual_mqtt/) component. However, the Alarm Control Panel should work with any home automation platform that supports MQTT messaging such as OpenHab, Node-Red, and SmartThings.  There is also a [Android Things and Raspbery Pi 3](https://github.com/thanksmister/androidthings-mqtt-alarm-panel) version if you want to use a Raspberry Pi wall mounted panel. 


## Support

For issues, feature requests, comments or questions, use the [Github issues tracker](https://github.com/thanksmister/android-mqtt-alarm-panel/issues).  For HASS specific questions, you can join the [Home Assistant Community Discussion](https://community.home-assistant.io/t/mqtt-alarm-control-panel-for-raspberry-pi-and-android/26484/94) page which already has a lot information from the community.  You can also join the [ThanksMister Community](https://community.thanksmister.com/) or hope o the Discord channel to ask questions or get support https://discord.com/invite/euh9J2d. 

***If you are looking for support for older devices, those running less than Android 4.4, then install one of the APK's from the release section prior to release v0.8.7-beta.0***

- [Alarm Panel Video](https://youtu.be/xspCZoRIBNQ)
- [Google Play Store](https://play.google.com/store/apps/details?id=com.thanksmister.iot.mqtt.alarmpanel). 
- [Android Things and Raspbery Pi 3](https://github.com/thanksmister/androidthings-mqtt-alarm-panel) (Deprecated).   

## Features

The alarm panel acts as an interface for Home Assistant's manual alarm control panel component. You can set the alarm state to away or home, or disarm the alarm using a code. In addition it has some nice features such as weather forecast and screen saver mode.

MQTT allows for communication between the alarm panel and the manual alarm panel. The alarm panel interface will reflect the current state of the manual alarm control panel component and vice versa. However, your home automation platform is responsible for triggering the alarm through automation and sensor states.

- Alarm sensors, display up to four sensors and their states in the alarm screen.
- Stream video, detect motion, detect faces, and read QR Codes.
- Capture and emailing images when the alarm is disabled.
- MQTT commands to remotely control the application (speak text, play audio, display notifications, alerts, etc.).
- Device sensor data reporting over MQTT (temperature, light, pressure, battery, etc.).
- Day/Night mode themes based on MQTT sun values.
- Optional screensaver mode using a digital clock, Imgur images or webpage. 
- Three day Weather forecast using MQTT.
- Home Automation Platform webpage support for viewing home automation dashboards.
- Support for Android 4.4 (API level 19) or later.

## Hardware & Software 

- Android Device running  Android 4.4 (API level 19) or later.  It'a also recommended that you use your own screensaver, like Daydream for Android so that your device does not go to sleep. You also want to disable your lock screen. The application will not work if your device sleeps (i.e. you need to unlock your device to open).  

- There is a known issue with Fire OS devices from Amazon, they usually have a custom OS and may not include Daydream.  You will then need to use the built-in screensaver features or you need to install an alternative solution.   Some Fire OS devices also lack haptic feedback for key presses and the ability to customize the alarm sound. 

- The WebView shipped with Android 4.4 (KitKat) is based on the same code as Chrome for Android version 30. This WebView does not have full feature parity with Chrome for Android and is given the version number 30.0.0.0.  


## Screen Shots:

![alarm_home](https://user-images.githubusercontent.com/142340/29889460-9f615642-8d9a-11e7-99a6-1a49529dd580.png)

![alarm_weather](https://user-images.githubusercontent.com/142340/29889463-9f64e550-8d9a-11e7-8d06-cbb046588875.png)

![alarm_arm](https://user-images.githubusercontent.com/142340/29889458-9f33509e-8d9a-11e7-8bdf-aaad28d94328.png)

![alarm_pending](https://user-images.githubusercontent.com/142340/29889461-9f62d238-8d9a-11e7-9a0f-77baf385d812.png)

![alarm_disarm](https://user-images.githubusercontent.com/142340/29889459-9f557980-8d9a-11e7-996e-dcbfd54d44cc.png)

![alarm_triggered](https://user-images.githubusercontent.com/142340/29889462-9f6422dc-8d9a-11e7-923a-06cfcd6acff7.png)

You can also load your home automation platform website by entering the address with port into the settings.  It slides from the right on the main screen. 

![platform_panel](https://user-images.githubusercontent.com/142340/34175188-53419a14-e4da-11e7-970a-77d2ff753d31.png)

## Installation

You can clone the repository and compile the APK using Android Studio, then side load the APK file onto your device. You can also side load the built APK from from the [release section](https://github.com/thanksmister/android-mqtt-alarm-panel/releases) or just install the application the [Google Play Store](https://play.google.com/store/apps/details?id=com.thanksmister.iot.mqtt.alarmpanel). 

## Home Assistant Setup

Alarm Panel should work with any MQTT broker and does not require Home Assistant.  If you are using Home Assistant, then you will want to use the Manual MQTT component and the built in MQTT integration for your setup.

- Setup [Home Assistant](https://home-assistant.io/getting-started/)
- Configure the [MQTT service](https://home-assistant.io/components/mqtt/) note the broker address and username/password if applicable.
- Add the [MQTT Manual](https://home-assistant.io/components/alarm_control_panel.manual_mqtt/) Home Assistant component to your configuration with the default settings.
- Add any sensors (like Zwave door sensors or sirens) and configure automations to trigger the alarm.

### Example Home Assistant Manual MQTT component:

```
alarm_control_panel:
  - platform: manual_mqtt
    state_topic: home/alarm
    command_topic: home/alarm/set
    pending_time: 60
    trigger_time: 1800
    disarm_after_trigger: false
    delay_time: 30
    armed_home:
      pending_time: 0
      delay_time: 0
    armed_away:
      pending_time: 60
      delay_time: 30
```

-- If I set the the alarm mode home, the alarm will immediately be on without any pending time.  If the alarm is triggered,      there will be no pending time before the siren sounds.   If the alarm mode is away, I have 60 seconds to leave before the      alarm is active and 30 seconds to disarm the alarm when entering.   
-- Notice that my trigger_time is 1800 and disarm_after_trigger is false, this means the alarm runs for 1800 seconds until it    stops and it doesn't reset after its triggerd. 

-- Be sure to change the settings in the Alarm Control Panel application to match these settings.   By default the                pending_time and delay_time are used for all alarm modes unless otherwise changed.

## MQTT Alarm Setup

Under the settings (gear icon) enter the MQTT information that you configured in your MQTT service.  This might include a username and password. If you are not using SSL, just enter the IP address of your broker like 192.168.1.1.   You enter the port and credentials in separate fields.   The alarm will try to connect using TCP unless you enter HTTP/HTTPS in front of the IP address like http://192.168.1.1.  However, for most MQTT brokers, using TCP is fine.

### Supported commands/payloads:

- Command topic:  home/alarm/set
- Command payloads: ARM_HOME, ARM_AWAY, ARM_NIGHT, DISARM
- Publish topic: home/alarm
- Publish payloads: disarmed, armed_away, armed_home, armed_night, pending, triggered.

### Alarm Security and Remote Code

Under the settings, you can update the default security code, its 1234 on first instalation. The security code is used to access the alarm settings and disarm the alarm from the alarm control panel app. You can choose to use the security code to disarm or arm the system. The security code is not sent over MQTT, it is only used from the application to control the alarm.   

There is an option called `Remote Code` that will send both the alarm state and the code entered when arming or optionally disarming the alarm to your MQTT broker.  This requires extra work on your part to parse the code and command paylaod from the MQTT message using your available platform tools.  Here is an example of an alarm command with the code:

```
{"command": "ARM_HOME", "code":1234}
```

When choose to use the feature, the security code you set within the application settings is only used to access the settings, is is no longer used to arm or disarm the system.   When entering a code to arm or disarm your system, the code will now be sent to the remote server along with the command as a JSON payload.  

The server must respond with either a status message or the new alarm state. You must write your own automations to validate the alarm code and either update the alarm state or return a message that the code is invalid.  If the code is invalid, the alarm panel will display an alert to the user. 

Here is what an invalid status message would look like: 

- Publish topic: home/alarm/status
- Publish payloads: INVALID, VALID


### Alarm Sensors

Under `Alarm Sensors` option in the settings, you can add up to four sensors devices to display on the alarm interface. For each sensor, you must specificy the topic and active state of the sensor, such as closed for a door or off for motion. The active state shows as green in the interface and red for any other reported states. The topic should include the entity id or name of your sensor and the current.   Here is a sample automation for a Home Assistant platform automation.

```
alias: MQTT Inside Motion
description: ''
trigger:
  - entity_id: sensor.inside_motion
    platform: state
condition: []
action:
  - data:
      topic: home/sensor/inside_motion
      payload_template: '{{states("sensor.inside_motion")}}'
      retain: true
    service: mqtt.publish
mode: single
```

Create an automation like this for each of the four components sending the topic with the entity id and using a payload template to send the current state.  The resulting payload for a door sensor might look like this for a closed door sensor:

Command topic: /home/sensor/inside_motion
Command payloads: no


And this when the door sensor is open:

Command topic: /home/sensor/inside_motion
Command payloads: yes

This really depends entirely on your platform and setup.  You can configure the alarm sensors to have the topic and state in the settings to match your MQTT setup. 


### Panic Button

This feature will display a panic button on the main interface that when pressed, will send a command with a single payload, it's up to your MQTT and automation platform how to handle it.  This would be like an emergency call or way to silently trigger the alarm.

The alarm panel will send the following command and payload:

Command topic:  home/alarm/panic
Command payload: ON


## MQTT Communication

The Alarm Panel application can display and control components using the MQTT protocal. Alarm Panel and Home Assistant work together to control the Home Assistant Alarm Control Panel, display weather data, receive sensor data, control the application Day/Night mode, and send various remote commands to the application.

You can also interact and control the application and device remotely using either MQTT commands, including using your device as an announcer with Google Text-To-Speach. Each device required a unique base topic which you set in the MQTT settings, the default is "alarmpanel".  This distinguishes your device if you are running multiple devices. 

### MQTT Weather

![weather](https://user-images.githubusercontent.com/142340/47173511-a193e200-d2e4-11e8-8cbc-f2d57cdb6346.png)

***Update***

We have deprecated support for Darksky, now you can use any weather integration.

You can also use MQTT to publish the weather to the Alarm Panel application, which it will then display on the main view. To do this you need to setup an automation that publishes a formatted MQTT message on an interval.  Then in the application settings, you can use most any weather integration as long as it supports the standard attributes. I am using Met.no integration (https://www.home-assistant.io/integrations/met/) for my weather with this automation to send weather data to the application:

```
alias: MQTT Weather
trigger:
  - minutes: /15
    platform: time_pattern
condition: []
action:
  - data:
      payload_template: >-
        {'weather':{'condition':'{{states.weather.home.state}}','humidity':{{states.weather.home.attributes.humidity}},'temperature':{{states.weather.home.attributes.temperature}},'forecast':{{states.weather.home.attributes.forecast}}}}
      retain: true
      topic: alarmpanel/command
    service: mqtt.publish
mode: single
```

For alternate way of getting the payload with the current condition, you can this for the payload_template:

```
payload_template: >-
        {% set attrs = states.weather.home.attributes %}
        {% set result = dict(attrs, condition = states('weather.home')) %}
        {'weather':{{result}}}
```


In the developer tools template, you can test out the payload by pasting this: 

```
{''weather'':{''condition'':''{{states.weather.home.state}}'',''humidity'':{{states.weather.home.attributes.humidity}},''temperature'':{{states.weather.home.attributes.temperature}},''forecast'':{{states.weather.home.attributes.forecast}}}}
```

The output will be this:

```
{''weather'':{''condition'':''sunny'',''humidity'':61,''temperature'':27.2,''forecast'':[{'condition': 'sunny', 'temperature': 19.8, 'templow': 13.5, 'datetime': '2020-11-30T15:00:00+00:00', 'wind_bearing': 101.4, 'wind_speed': 30.6}, {'condition': 'cloudy', 'precipitation': 1.2, 'temperature': 23.0, 'templow': 16.8, 'datetime': '2020-12-01T15:00:00+00:00', 'wind_bearing': 17.1, 'wind_speed': 24.5}, {'condition': 'partlycloudy', 'temperature': 26.0, 'templow': 20.5, 'datetime': '2020-12-02T15:00:00+00:00', 'wind_bearing': 48.8, 'wind_speed': 14.0}, {'condition': 'partlycloudy', 'precipitation': 0.3, 'temperature': 25.2, 'templow': 19.7, 'datetime': '2020-12-03T15:00:00+00:00', 'wind_bearing': 71.5, 'wind_speed': 22.7}, {'condition': 'partlycloudy', 'temperature': 22.7, 'templow': 18.0, 'datetime': '2020-12-04T15:00:00+00:00', 'wind_bearing': 105.5, 'wind_speed': 33.5}]}}
```

You can also test this using the "mqtt.publish" service under the Home Assistant Developer Tools:

```
{
  "payload_template": "{''weather'':{''condition'':''{{states.weather.home.state}}'',''humidity'':{{states.weather.home.attributes.humidity}},''temperature'':{{states.weather.home.attributes.temperature}},''forecast'':{{states.weather.home.attributes.forecast}}}}",
  "retain": true,
  "topic": "alarmpanel/command"
}
```

Note that many weather sources work.  For example, the Accuweather integration has been affirmatively tested.  Also note that the template engine has an issue if you have an entity where a section starts with a digit.  This means that if you have an entity like `weather.1234_main_st` you will have to use a different template method to access the entity data:

```
states.weather['1234_main_st'].attributes
```

### MQTT Day/Night Mode

Similar to how weather works, you can control the Voice Panel to display the day or night mode by sending a formatted MQTT message with the sun's position (above or below the horizon).  To do this add the [sun component](https://www.home-assistant.io/components/sun/) to Home Assistant, then setup an automation to publish an MQTT message on an interval:

```
alias: MQTT Sun
trigger:
- minutes: /30
  platform: time_pattern
condition: []
action:
- data:
    payload_template: '{''sun'':''{{states(''sun.sun'')}}''}'
    retain: true
    topic: alarmpanel/command
  service: mqtt.publish
```

The resulting payload will look like this:

```
{
  "payload": "{'sun':'below_horizon'}",
  "topic": "alarmpanel/command"
}
```

You can also test this using the "mqtt.publish" service under the Home Assistant Developer Tools:

```
{
  "payload_template": "{'sun':'{{states('sun.sun')}}'}",
  "topic": "alarmpanel/command"
}
```

If you wish, you can use an offset to change the day or night mode values or send a MQTT message at the desired time with "above_horizon" to show day mode or "below_horizon" to show night mode.  If you wish to always be night, you need only send one MQTT message with "below_horizon" and the app will not switch back to day mode.  Be sure to turn on the Day/Night mode under the Display settings in the application.

### MQTT Alarm Commands with Mosquitto 

Sending command to your MQTT Broker to arm or disarm the alarm, include your MQTT Broker IP address, port, and optionally the username and password if needed.  The "-d" is to get the debug information. 
```
mosquitto_pub -h 192.168.1.2  -t home/alarm/set -m "ARM_HOME" -d -p 1883 -u username -P password
mosquitto_pub -h 192.168.1.2 -t home/alarm/set -m "DISARM" -d -p 1883 -u username -P password
```
Publish a message from your MQTT Broker to your MQTT client (the Android application).  You may need to add `-h localhost`, but you shouldn't since you are publishing directly from your MQTT broker. 
```
mosquitto_pub -t home/alarm -m "armed_home" 
mosquitto_pub -t home/alarm -m "disarmed" 
```

Note that the application when sending a command expects an MQTT response. If you use the application to set the alarm to be armed home, the MQTT Broker should respond with the message that the alarm was set to armed home.  The application is just an interface for the MQTT service, its not the alarm system, the alarm system is your server, either Home Assistant or your MQTT Broker and server. 


### MQTT Commands
Key | Value | Example Payload | Description
-|-|-|-
audio | URL | ```{"audio": "http://<url>"}``` | Play the audio specified by the URL immediately
wake | true | ```{"wake": true}``` | Wakes the screen if it is asleep
speak | data | ```{"speak": "Hello!"}``` | Uses the devices TTS to speak the message
alert | data | ```{"alert": "Hello!"}``` | Displays an alert dialog within the application
notification | data | ```{"notification": "Hello!"}``` | Displays a system notification on the device
sun | data | ```{"sun": "above_horizon"}``` | Changes the application day or night mode based on sun value (above_horizona, below_horizon)

* The base topic value (default is "alarmpanel") should be unique to each device running the application unless you want all devices to receive the same command. The base topic and can be changed in the application settingssettings.
* Commands are constructed via valid JSON. It is possible to string multiple commands together:
  * eg, ```{"clearCache":true, "relaunch":true}```
* MQTT
  * WallPanel subscribes to topic ```[alarmpanel]/command```
    * Default Topic: ```alarmpanel/command```
  * Publish a JSON payload to this topic (be mindfula of quotes in JSON should be single quotes not double)

### Google Text-To-Speach Command
You can send a command using either HTTP or MQTT to have the device speak a message using Google's Text-To-Speach. Note that the device must be running Android Lollipop or above. 

Example format for the message topic and payload: 

```{"topic":"alarmpanel/command", "payload":"{'speak':'Hello!'}"}```

### MQTT Sensor and State Data
If MQTT is enabled in the settings and properly configured, the application can publish data and states for various device sensors, camera detections, and application states. Each device required a unique base topic which you set in the MQTT settings, the default is "alarmpanel".  This distinguishes your device if you are running multiple devices.  

### Device Sensors
The application will post device sensors data per the API description and Sensor Reading Frequency. Currently device sensors for Pressure, Temperature, Light, and Battery Level are published. 

#### Sensor Data
Sensor | Keys | Example | Notes
-|-|-|-
battery | unit, value, charging, acPlugged, usbPlugged | ```{"unit":"%", "value":"39", "acPlugged":false, "usbPlugged":true, "charging":true}``` |
light | unit, value | ```{"unit":"lx", "value":"920"}``` |
magneticField | unit, value | ```{"unit":"uT", "value":"-1780.699951171875"}``` |
pressure | unit, value | ```{"unit":"hPa", "value":"1011.584716796875"}``` |
temperature | unit, value | ```{"unit":"°C", "value":"24"}``` |

*NOTE:* Sensor values are device specific. Not all devices will publish all sensor values.

* Sensor values are constructued as JSON per the above table
* For MQTT
  * WallPanel publishes all sensors to MQTT under ```[alarmpanel]/sensor```
  * Each sensor publishes to a subtopic based on the type of sensor
    * Example: ```alarmpanel/sensor/battery```
    
#### Home Assistant Examples
```YAML
sensor:
  - platform: mqtt
    state_topic: "alarmpanel/sensor/battery"
    name: "Alarm Panel Battery Level"
    unit_of_measurement: "%"
    value_template: '{{ value_json.value }}'
    
 - platform: mqtt
    state_topic: "alarmpanel/sensor/temperature"
    name: "WallPanel Temperature"
    unit_of_measurement: "°C"
    value_template: '{{ value_json.value }}'

  - platform: mqtt
    state_topic: "alarmpanel/sensor/light"
    name: "Alarm Panel Light Level"
    unit_of_measurement: "lx"
    value_template: '{{ value_json.value }}'
    
  - platform: mqtt
    state_topic: "alarmpanel/sensor/magneticField"
    name: "Alarm Panel Magnetic Field"
    unit_of_measurement: "uT"
    value_template: '{{ value_json.value }}'

  - platform: mqtt
    state_topic: "alarmpanel/sensor/pressure"
    name: "Alarm Panel Pressure"
    unit_of_measurement: "hPa"
    value_template: '{{ value_json.value }}'
```

### Camera Motion, Face, and QR Codes Detections
In additional to device sensor data publishing. The application can also publish states for Motion detection and Face detection, as well as the data from QR Codes derived from the device camera.  

Detection | Keys | Example | Notes
-|-|-|-
motion | value | ```{"value": false}``` | Published immediately when motion detected
face | value | ```{"value": false}``` | Published immediately when face detected
qrcode | value | ```{"value": data}``` | Published immediately when QR Code scanned

* MQTT
  * WallPanel publishes all sensors to MQTT under ```[alarmpanel]/sensor```
  * Each sensor publishes to a subtopic based on the type of sensor
    * Example: ```alarmpanel/sensor/motion```

#### Home Assistant Examples

```YAML
binary_sensor:
  - platform: mqtt
    state_topic: "alarmpanel/sensor/motion"
    name: "Motion"
    payload_on: '{"value":true}'
    payload_off: '{"value":false}'
    device_class: motion 
    
binary_sensor:
  - platform: mqtt
    state_topic: "alarmpanel/sensor/face"
    name: "Face Detected"
    payload_on: '{"value":true}'
    payload_off: '{"value":false}'
    device_class: motion 
  
sensor:
  - platform: mqtt
    state_topic: "alarmpanel/sensor/qrcode"
    name: "QR Code"
    value_template: '{{ value_json.value }}'
    
```

### Application State Data
The application canl also publish state data about the application such as the current dashboard url loaded or the screen state.

Key | Value | Example | Description
-|-|-|-
currentUrl | URL String | ```{"currentUrl":"http://hasbian:8123/states"}``` | Current URL the Dashboard is displaying
screenOn | true/false | ```{"screenOn":true}``` | If the screen is currently on

* State values are presented together as a JSON block
  * eg, ```{"currentUrl":"http://hasbian:8123/states","screenOn":true}```
* MQTT
  * WallPanel publishes state to topic ```[alarmpanel]/state```
    * Default Topic: ```alarmpanel/state```

## Capture Images (Telegram/Mailgun)

If you would like to capture and email images when the alarm is deactivated then you need to setup a [Mailgun](https://www.mailgun.com/) account. You will need to enter the domain address and API key from your Mailgun accoint into the application setting screen along with other information. 

You may also use Telegram to recieve a notification with the image when the alarm is deactivated.  To use Telegram you need a chat Id and a Telegram Bot API token.  Follow the [Telegram guide on Home Assistant](https://home-assistant.io/components/notify.telegram/) to setup Telegram.  Enter the chat Id and token into the application settings screen.

The camera only captures images when activated in the settings and MailGun is setup properly.  Images are captured each time the alarm is deactivated. You may use either Mailgun, Telegram, or both to send notifications. 

## Screensaver, Unsplash, Clock, Webpage

Since the applicaiton was designed to run as a full-screen dedicated wall panel application, it is important that you have a screensaver enabled.  You can use Android's built-in DayDream which does a nice job dimming the screen.   However, on some devices like the Fire OS tablets, this is not an option.  Intead, activate the screensaver feature under the `Display Settings` section. The default screen saver will display the current time, and if you are sending weather data, the current weather conditions.  You can change this to display a webpage of your choosing, even with animation or you can display one from Unsplash.  Performance may vary depending on your device. 

## Platform Screen or Webpage View

You can load your Home Assistant (or any web page) as alternative view by entering your Home Assistant address.  The address should be in the format http://192.168.1.1:8123 and include the port number.  You can use HADashboard or Home Assistant kiosk mode as well.  This feature uses an Android web view component and may not work on older SDK versions. 

## MJPEG Video Streaming

Use the device camera as a live MJPEG stream. Just connect to the stream using the device IP address and end point. Be sure to turn on the camera streaming options in the settings and set the number of allowed streams and HTTP port number. Note that performance depends upon your device (older devices will be slow).

#### Browser Example:

```http://192.168.1.1:2971/camera/stream```

#### Home Assistant Example:

```YAML
camera:
  - platform: mjpeg
    mjpeg_url: http://192.168.1.1:2971/camera/stream
    name: Alarm Panel Camera
```

## Notes

- To use TTS and the Camera you will need Android Lollipop SDK or greater as well as camera permissions. Older versions of Android are currently not supported.  The application is locked into the landscape mode for usability.  It is meant to run on dedicated tablets or large screen devices that will be used mainly for an alarm control panel. 

## Acknowledgements

Special thanks to Colin O'Dell who's work on the Home Assistant Manual Alarm Control Panel component and his [MQTT Alarm Panel](https://github.com/colinodell/mqtt-control-panel) helped make this project possible.  Thanks to [Juan Manuel Vioque](https://github.com/tremebundo) for Spanish translations and [Gerben Bol](https://gerbenbol.com/) for Dutch translations, [Jorge Assunção](https://github.com/jorgeassuncao) for Portuguese, [electricJP](https://github.com/electricJP) and [jncanches](https://github.com/jncanches) for French translations.

## Contributors
[Sergio Viudes](https://github.com/sjvc) for Fingerprint unlock support and his [Home-Assistant-WebView](https://github.com/sjvc/Home-Assistant-WebView) component.
