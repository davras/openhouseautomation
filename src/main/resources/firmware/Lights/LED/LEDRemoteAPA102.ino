// Replace controller_id with your controller's id

// This #include statement was automatically added by the Particle IDE.
#include <FastLED.h>

FASTLED_USING_NAMESPACE;
// How many leds in your strip?
#define NUM_LEDS 120

// For led chips like Neopixels, which have a data line, ground, and power, you just
// need to define DATA_PIN.  For led chipsets that are SPI based (four wires - data, clock,
// ground, and power), like the LPD8806, define both DATA_PIN and CLOCK_PIN
#define DATA_PIN A5
#define CLOCK_PIN A3

CRGB leds[NUM_LEDS];

int controller_id = 1234567890;
int r=0,g=50,b=0;
long lastcomm=0L,lastsec=0L,mscounter=0L;

void setup() {
  LEDS.addLeds<APA102,DATA_PIN,CLOCK_PIN>(leds,NUM_LEDS);
  LEDS.setBrightness(255);
  // Subscribe to the integration response event
  Particle.subscribe("hook-response/lightcolor/0", myHandler, MY_DEVICES);
  Particle.function("updateOHA", updateOHA);
  lastsec=Time.local();
}

// Cloud functions must return int and take one String
int updateOHA(String update) {
  // this will force an update/response
  lastcomm=0L;
  return 0;
}

void myHandler(const char *event, const char *data) {
  // Handle the integration response
    sscanf(data, "%02x%02x%02x", &r, &g, &b);
}

void loop() {
    if (Time.local() != lastsec) {
        mscounter=millis();
        lastsec=Time.local();
    }
    showClock();
}

void showClock() {
    // paint the background
    for (int i=0; i < NUM_LEDS; i++) {
        leds[i] = CRGB(r, g, b);
    }
    // fade the second hand
    long msoffset = millis() - mscounter;
    int sec = map(Time.second(), 0, 59, 0, NUM_LEDS-1);
    
    leds[sec] = CRGB(0,0,0);
    int psec = sec-1;
    if (psec < 0) {
        psec += NUM_LEDS;
    }
    leds[psec] = CRGB(0,0,0);

    int pr=0,pg=0,pb=0;
    pr=map(msoffset, 0, 1000, 0, r);
    pg=map(msoffset, 0, 1000, 0, g);
    pb=map(msoffset, 0, 1000, 0, b);
    psec = sec-2;
    if (psec < 0) psec += NUM_LEDS;
    leds[sec-2] = CRGB(pr, pg, pb);
    psec = sec-3;
    if (psec < 0) psec += NUM_LEDS;
    leds[sec-3] = CRGB(pr, pg, pb);
    
    int nr=0,ng=0,nb=0;
    nr=map(msoffset, 0, 1000, r, 0);
    ng=map(msoffset, 0, 1000, g, 0);
    nb=map(msoffset, 0, 1000, b, 0);
    leds[sec+1] = CRGB(nr, ng, nb);
    leds[sec+2] = CRGB(nr, ng, nb);
    
    int hr = map(Time.hourFormat12(Time.local()), 1, 12, 0, NUM_LEDS-1);
    int min = map(Time.minute(), 0, 59, 0, NUM_LEDS-1);
    leds[hr] = CRGB(255, g, b);
    leds[hr-1] = CRGB(255, g, b);
    leds[hr+1] = CRGB(255, g, b);
    leds[min] = CRGB(r, 255, b);
    leds[min-1] = CRGB(r, 255, b);
    FastLED.show();
}














