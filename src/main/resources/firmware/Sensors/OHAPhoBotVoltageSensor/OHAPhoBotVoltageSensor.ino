// Replace voltssensor with your sensor id

// This #include statement was automatically added by the Particle IDE.
#include "PhoBot.h"
#define BATTV A2

const unsigned long voltssensor=1234567890;
PhoBot p = PhoBot();
volatile unsigned long nextcloudcomm=0L;
const long cloudcomminterval=240000L;
float volts;

void setup() {
    Particle.variable("volts", (double)volts);
    nextcloudcomm=millis() + 30000L;
    pinMode(BATTV, INPUT);
}

void loop() {
    volts = p.batteryVolts();
    if (millis() > nextcloudcomm) {
        publishVoltage();
        nextcloudcomm = millis() + cloudcomminterval;
        return;
    }
}

void publishVoltage() {
    char buffer[30];
    sprintf(buffer, "%u/%1.2f", voltssensor, volts);
    Particle.publish("sens", buffer, 60, PRIVATE);
}

