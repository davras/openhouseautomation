// This #include statement was automatically added by the Particle IDE.
#include <lpd8806.h>

#define NUM_LEDS 32

// For led chips like Neopixels, which have a data line, ground, and power, you just
// need to define DATA_PIN.  For led chipsets that are SPI based (four wires - data, clock,
// ground, and power), like the LPD8806, define both DATA_PIN and CLOCK_PIN
#define DATA_PIN A5
#define CLOCK_PIN A3

lpd8806 strip = lpd8806(NUM_LEDS);

int r=0,g=50,b=0;
long lastminute=0L,mscounter=0L;

void setup() {
  strip.begin();
  strip.show();
  // Subscribe to the integration response event
  Particle.subscribe("hook-response/lightcolor/0", myHandler, MY_DEVICES);
}

void myHandler(const char *event, const char *data) {
  // Handle the integration response
    sscanf(data, "%02x%02x%02x", &r, &g, &b);
}

void loop() {
    if (Time.minute() != lastminute) {
        mscounter=millis();
        lastminute=Time.minute();
    }
    showClock();
}

void showClock() {
    long animtime = 60000/NUM_LEDS;
    // paint the background
    for (int i=0; i < NUM_LEDS; i++) {
        strip.setPixelColor(i, r, g, b);
    }
    // fade the second hand
    long msoffset = millis() - mscounter;
    int sec = msoffset/animtime;
    
    strip.setPixelColor(sec,0,0,0);

    int pr=0,pg=0,pb=0;
    pr=map(msoffset%animtime, 0, animtime, 0, r);
    pg=map(msoffset%animtime, 0, animtime, 0, g);
    pb=map(msoffset%animtime, 0, animtime, 0, b);
    if (sec == 0) {
        strip.setPixelColor(NUM_LEDS-1, pr, pg, pb);
    } else {
        strip.setPixelColor(sec-1, pr, pg, pb);
    }

    int nr=0,ng=0,nb=0;
    nr=map(msoffset%animtime, 0, animtime, r, 0);
    ng=map(msoffset%animtime, 0, animtime, g, 0);
    nb=map(msoffset%animtime, 0, animtime, b, 0);
    strip.setPixelColor(sec+1,nr, ng, nb);

    int min = map(Time.minute(), 0, 59, 0, NUM_LEDS-1);
    strip.setPixelColor(min, r, 255, b);

    //int hr = map(Time.hourFormat12(Time.local()), 1, 12, 0, NUM_LEDS-1);
    int hr = map((Time.hourFormat12()*60)+Time.minute(), 60, 12*60, 0, NUM_LEDS-1);
    strip.setPixelColor(hr, 255, g, b);
    strip.setPixelColor(hr-1, 255, g, b);
    strip.show();
}














