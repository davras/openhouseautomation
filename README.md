This project has been obsoleted by the pricing changes (Summer 2021) for Particle network.
Previously, up to 720k data operations per month were free, allowing real-time control
of the house.  Now the same controls will cost $180/month for Particle network.

I now recommend using ESP32+Wifi with HomeAssistant to avoid being extorted.

Goals:
======
- Make a house intelligent, with independent decision making.
 - Save energy.
 - Transparent, house should work like a normal dumb house during failure.
 - Cheap...each device needs to be < $100 (including the CPU, sensors, displays, comms, etc.)
 - Easy to use. Set the keys, load the firmware, done.
 - Use the cloud, not an expensive "central controller".
 
- Use devices to automate a house:
 - Thermostat
 - Lights and LED strip accent lighting
 - Alarm
 - Whole House Fan
 - Serial control (remote turn on/off, input select, etc.)

- Control from:
 - Android app
 - Web page
 - RPi controller
 - Particle controller.

Features done:
=========
- Triggers allow if-then. If the front door opens, turn on the porch light. If the garage door opens, turn on the garage lights. If the back door opens, turn on the back porch light for 5 mins.
- Whole House Fan pre-cools the house the night before based on tomorrow's forecast high.
 - Saves energy by not running AC on hot days.
 - Maintains comfortable sleeping temperature during the night.
 - Integrated with Google Cloud Pubsub and webhooks.

Todo:
=========
- Open hardware, open software, open firmware
- Make GAE 'learn' the scenes, like "arrived home", "going to bed", etc.
- RGB LED strips to set lighting for optimal colors.
 - Blue during daytime, yellow evening, red night, off for sleeping.
 - Auto-on red night light if movement detected.

Code for:
=========

- Arduino (with sensors) to talk to the cloud (Google App Engine).
- Google AppEngine? instance to talk back to the Arduinos.
- Home automation control via browser, mobile, and RPi.

Current status:
===============

- Done:
 - Ambient light/proximity sensor
 - Temperature (DS18B20 and TMD2271)
 - Shell script for controlling the Whole House Fan
 - Particle Core for remote projector

- In progress:
 - Front Door Lock
 - Thermostat
 - PIR/Occupancy/Movement Sensor

Setup Instructions:
===================
Requires [Apache Maven](http://maven.apache.org) 3.1.0 or greater, and JDK 7+ in order to run.

First time setup, run

    mvn clean install

To build, run

    mvn package

Building will run the tests, but to explicitly run tests you can use the test target

    mvn test

To start the app locally, use the [App Engine Maven Plugin](http://code.google.com/p/appengine-maven-plugin/) that is already included.  Just run the command.

    mvn appengine:devserver

To deploy the app, run:
    
    mvn appengine:update

To see all the available goals for the App Engine plugin, run

    mvn help:describe -Dplugin=appengine

Errata:
===================
My house is my dogfood.  Please be nice.
