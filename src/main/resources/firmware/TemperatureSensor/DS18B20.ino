// This #include statement was automatically added by the Particle IDE.
#include "spark-dallas-temperature.h"

// This #include statement was automatically added by the Particle IDE.
#include <OneWire.h>

#define SENSORID 28131427
#define MAXRETRIES 15
OneWire ow(D7);
DallasTemperature dt(&ow);
long lastupdate=0L;

void setup() {
    dt.begin();
    dt.setResolution(12);
}

void loop() {
    delay(10000);
    if (Time.now() > (lastupdate + 180L)) {
        updatecloud();
    }
}

void updatecloud() {
    dt.requestTemperatures();
    delay(1000);
    float temperature = dt.getTempFByIndex(0);
    char value[25];
    sprintf(value, "%u/%1.1f", SENSORID, temperature);
    int counter=0;
    boolean success=false;
    do {
        success = Particle.publish("sens", value, 60, PRIVATE);
        Particle.process();
        delay(1000 * counter + random(1000));
    } while (!success && (counter++ < MAXRETRIES));
    lastupdate=Time.now();
}

