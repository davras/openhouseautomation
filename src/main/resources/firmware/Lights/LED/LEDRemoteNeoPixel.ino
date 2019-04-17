#include "neopixel.h"

// IMPORTANT: Set pixel COUNT, PIN and TYPE
#define PIXEL_PIN D2
#define PIXEL_COUNT 60
#define PIXEL_TYPE SK6812RGBW
//#define PIXEL_TYPE WS2812B
#define BRIGHTNESS 255 // 0 - 255

Adafruit_NeoPixel strip(PIXEL_COUNT, PIXEL_PIN, PIXEL_TYPE);

int gamma[] = {
    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  1,  1,  1,  1,
    1,  1,  1,  1,  1,  1,  1,  1,  1,  2,  2,  2,  2,  2,  2,  2,
    2,  3,  3,  3,  3,  3,  3,  3,  4,  4,  4,  4,  4,  5,  5,  5,
    5,  6,  6,  6,  6,  7,  7,  7,  7,  8,  8,  8,  9,  9,  9, 10,
   10, 10, 11, 11, 11, 12, 12, 13, 13, 13, 14, 14, 15, 15, 16, 16,
   17, 17, 18, 18, 19, 19, 20, 20, 21, 21, 22, 22, 23, 24, 24, 25,
   25, 26, 27, 27, 28, 29, 29, 30, 31, 32, 32, 33, 34, 35, 35, 36,
   37, 38, 39, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 50,
   51, 52, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 66, 67, 68,
   69, 70, 72, 73, 74, 75, 77, 78, 79, 81, 82, 83, 85, 86, 87, 89,
   90, 92, 93, 95, 96, 98, 99,101,102,104,105,107,109,110,112,114,
  115,117,119,120,122,124,126,127,129,131,133,135,137,138,140,142,
  144,146,148,150,152,154,156,158,160,162,164,167,169,171,173,175,
  177,180,182,184,186,189,191,193,196,198,200,203,205,208,210,213,
  215,218,220,223,225,228,231,233,236,239,241,244,247,249,252,255 };

int controller_id = 28131427;
int r=12,g=24,b=12,w=0;
long lastcomm=0L,lastminute=0L,mscounter=0L;






void setup() {
  strip.setBrightness(BRIGHTNESS);
  strip.begin();
  strip.clear();
  strip.show();
  // Subscribe to the integration response event
  Particle.subscribe("hook-response/lightcolor", myHandler, MY_DEVICES);
  Particle.function("updateOHA", updateOHA);
}

// Cloud functions must return int and take one String
int updateOHA(String update) {
  // this will force an update/response
  lastcomm=0L;
  return 0;
}

void myHandler(const char *event, const char *data) {
  // Handle the integration response
    sscanf(data, "#%02x%02x%02x", &r, &g, &b);
    lastcomm=Time.now();
}

void loop() {
    if (Time.minute() != lastminute) {
        mscounter=millis();
        lastminute=Time.minute();
    }
    showClockRGBW();
    if (Time.now()-60 > lastcomm) {
        // Get some data
        char data[30];
        sprintf(data, "%i/#%02x%02x%02x", controller_id, r, g, b);
        // Trigger the integration
        Particle.publish("lightcolor", data, PRIVATE);
        lastcomm=Time.now();
    }
}

void showClockRGB() {
    long animtime = 60000/PIXEL_COUNT;
    // paint the background
    for (int i=0; i < PIXEL_COUNT; i++) {
        strip.setPixelColor(i, gamma[r], gamma[g], gamma[b]);
    }
    // fade the second hand
    long msoffset = millis() - mscounter;
    int sec = msoffset/animtime;
    
    strip.setPixelColor(sec,0,0,0);

    int pr=0,pg=0,pb=0;
    pr=map(msoffset%animtime, 0, animtime, 0, gamma[r]);
    pg=map(msoffset%animtime, 0, animtime, 0, gamma[g]);
    pb=map(msoffset%animtime, 0, animtime, 0, gamma[b]);
    if (sec == 0) {
        strip.setPixelColor(PIXEL_COUNT-1, pr, pg, pb);
    } else {
        strip.setPixelColor(sec-1, pr, pg, pb);
    }

    int nr=0,ng=0,nb=0;
    nr=map(msoffset%animtime, 0, animtime, gamma[r], 0);
    ng=map(msoffset%animtime, 0, animtime, gamma[g], 0);
    nb=map(msoffset%animtime, 0, animtime, gamma[b], 0);
    strip.setPixelColor(sec+1, nr, ng, nb);

    int min = map(Time.minute(), 0, 59, 0, PIXEL_COUNT-1);
    strip.setPixelColor(min, r, 255, b);

    //int hr = map(Time.hourFormat12(Time.local()), 1, 12, 0, NUM_LEDS-1);
    int hr = map((Time.hourFormat12()*60)+Time.minute(), 60, 12*60, 0, PIXEL_COUNT-1);
    strip.setPixelColor(hr, 255, gamma[g], gamma[b]);
    strip.setPixelColor(hr-1, 255, gamma[g], gamma[b]);
    strip.show();
}

void showClockRGBW() {
    long animtime = 666;
    // paint the background
    for (int i=0; i < PIXEL_COUNT; i++) {
        strip.setPixelColor(i, strip.Color(g, r, b, 0));
        if (r==255 && b==255 && g==255) {
            strip.setPixelColor(i, strip.Color(0, 0, 0, 255));
        }
        if (r==254 && b==254 && g==254) {
            strip.setPixelColor(i, strip.Color(255, 255, 255, 255));
        }
    }
    // fade the second hand
    long msoffset = millis() - mscounter;
    int sec = 60-(msoffset/animtime);
    
    // next pixel fades out RGB and fades in white
    int nr=map(msoffset%animtime, 0, animtime, r, 0);
    int ng=map(msoffset%animtime, 0, animtime, g, 0);
    int nb=map(msoffset%animtime, 0, animtime, b, 0);
    strip.setPixelColor(sec-1, strip.Color(nr, ng, nb, map(msoffset%animtime, 0, animtime, 0, BRIGHTNESS)));
    
    // current pixel fades out white to black
    strip.setPixelColor(sec, strip.Color(0,0,0,map(msoffset%animtime, 0, animtime, BRIGHTNESS, 0)));
    
    // previous pixel fades RGB back in
    strip.setPixelColor(sec+1, strip.Color(r-nr, g-ng, b-nb, 0));
    if (r==255 && b==255 && g==255) {
        strip.setPixelColor(sec+1, strip.Color(0, 0, 0, 255));
    }
    // minute hand
    int min = map(Time.minute(), 0, 59, PIXEL_COUNT-1, 0);
    strip.setPixelColor(min, strip.Color(120,0,0,0));

    // hour hand
    //int hr = map(Time.hourFormat12(Time.local()), 1, 12, 0, NUM_LEDS-1);
    int hr = map((Time.hourFormat12()*60)+Time.minute(), 60, 12*60, PIXEL_COUNT-1, 0);
    strip.setPixelColor(hr, strip.Color(r, 255, b, 0));
    strip.setPixelColor(hr-1, strip.Color(r, 255, b, 0));
    strip.show();
}













