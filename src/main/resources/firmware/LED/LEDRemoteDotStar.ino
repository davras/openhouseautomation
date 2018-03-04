// This #include statement was automatically added by the Particle IDE.
#include <dotstar.h>

#define NUM_LEDS 64

#define DATAPIN   A5
#define CLOCKPIN  A3
Adafruit_DotStar strip = Adafruit_DotStar(NUM_LEDS, DATAPIN, CLOCKPIN);
int controller_id = 28131427;
int r=0,g=0,b=0;
long lastcomm=0L,lastminute=0L,mscounter=0L;

void setup() {
  strip.begin();
  strip.show();
  // Subscribe to the integration response event
  Particle.subscribe("hook-response/lightcolor/0", myHandler, MY_DEVICES);
  bool success = Particle.function("updateOHA", updateOHA);
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
}

void loop() {
    if (Time.minute() != lastminute) {
        mscounter=millis();
        lastminute=Time.minute();
    }
    showClock();
    if (Time.now()-300 < lastcomm) {
        delay(10);
        return;
    }
    // Get some data
    char data[30];
    sprintf(data, "%i/#%02x%02x%02x", controller_id, r, g, b);
    // Trigger the integration
    Particle.publish("lightcolor", data, PRIVATE);
    lastcomm=Time.now();
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














