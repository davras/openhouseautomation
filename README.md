Goals:
======

- Use devices to automate a house.
 - Thermostat
 - Lights (X-10) and LED strip accent lighting
 - Home entertainment (IR LED)
 - Alarm monitoring
 - Whole House Fan
 - Serial control (remote turn on/off, input select, etc.)
- Cheap...each device needs to be < $100 (including the CPU, sensors, displays, comms, etc.)
- Easy to use. Set the keys, load the firmware, done.
- Use the cloud, not an expensive "central controller".
- Control from:
 - Android app
 - Web page
 - RPi controller
 - Arduino controller.

Features:
=========

- Open hardware, open software, open firmware
- Make GAE 'learn' the scenes, like "arrived home", "going to bed", etc.
- Triggers allow if-then. If the front door opens, turn on the porch light. If the garage door opens, turn on the garage lights. If the back door opens, turn on the back porch light for 5 mins.
- Simple, secure protocol
 - Each device has a numeric ID
 - Each request will have a "time+shared secret"-based hash

Code for:
=========

- Arduino (with sensors) to talk to the cloud (Google App Engine).
- Google AppEngine? instance to talk back to the Arduinos.
- Home automation control via browser, mobile, and RPi.

Current status:
===============

- Arduino code finished for wired ethernet Arduino
- Arduino code finished for wireless Arduino (CC3000 and WiFi shields)
- Arduino code finished for Makershed arLCD + WiFi shield.
- Arduino code finished for Sparkfun Weather Shield, but needs wiring diagram.
- Spark code done for BMP180 and DS18B20 sensors
- Arduino+X10 controls.
- Intelligent Whole House Fan control for energy savings (peeks at tomorrow's forecast)

To do:
======
- Better UI for arLCD+WiFi.
- RPi + CM11A + Heyu for lights (is it possible?)

Maybe:
======

- Insteon (if it gets cheap enough)
- Wireless X10 (if it gets cheap enough)

Setup Instructions:
===================
Requires [Apache Maven](http://maven.apache.org) 3.1.0 or greater, and JDK 7+ in order to run.

First time setup, run

    mvn clean install

To build, run

    mvn package

Building will run the tests, but to explicitly run tests you can use the test target

    mvn test

To start the app, use the [App Engine Maven Plugin](http://code.google.com/p/appengine-maven-plugin/) that is already included.  Just run the command.

    mvn appengine:devserver

To see all the available goals for the App Engine plugin, run

    mvn help:describe -Dplugin=appengine
