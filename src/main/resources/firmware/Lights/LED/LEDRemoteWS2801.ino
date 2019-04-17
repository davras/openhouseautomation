// Replace controller_id with your light controller's id

// This #include statement was automatically added by the Particle IDE.
#include <WS2801.h>

// PIXEL_PIN_CLOCK A3
// PIXEL_PIN_DATA A5
#define PIXEL_COUNT 50

// Uses SPI for WS2801 LEDs, not compatible with NeoPixels or DotStars

// Set the argument to the NUMBER of pixels.
Adafruit_WS2801 strip = Adafruit_WS2801(PIXEL_COUNT);
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

int controller_id = 1234567890;
int r=0,g=0,b=0,w=0,maxbrt=220;
long nextcomm=0L,lastminute=0L,mscounter=0L;
boolean dumb=true; // don't publish color until you hear someone else advertise current color

void setup() {
    srand(Time.now());
    strip.begin();
    strip.show();
    rainbowCycle(10);

//    teststrand();
    // Subscribe to the integration response event
    Particle.subscribe("hook-response/lightcolor", myHandler, MY_DEVICES);
    Serial.begin(9600);
}

// Cloud functions must return int and take one String
int updateOHA(String update) {
  // this will force an update/response
  nextcomm=0L;
  return 0;
}

void myHandler(const char *event, const char *data) {
  // Handle the integration response
    sscanf(data, "#%02x%02x%02x", &r, &g, &b);
    Serial.println(data);
    nextcomm = Time.now() + 30 + rand()%60;
    dumb=false;
    blink(false);
}

void loop() {
    if (Time.minute() != lastminute) {
        mscounter=millis();
        lastminute=Time.minute();
    }
    showClockRGB();
    strip.show();
    if (Time.now() < nextcomm || millis() < 120000) {
        // remember this is reversed to escape if it's not time
        delay(10);
        return;
    }
    // Get some data
    // time to publish the current color
    blink(true);
    nextcomm = Time.now() + 30 + rand()%120;
    char data[30];
    sprintf(data, "%i/#%02x%02x%02x", controller_id, r, g, b);
    // Trigger the integration
    Particle.publish("lightcolor", data, PRIVATE);
    if (millis() > 120000) {
        RGB.control(true);
    }
}

void showClockRGB() {
    RGB.color(gamma[r], gamma[g], gamma[b]);
    long animtime = 60000/PIXEL_COUNT;
    // paint the background
    for (int i=0; i < PIXEL_COUNT; i++) {
            strip.setPixelColor(i, gamma[r], gamma[g], gamma[b]);
    }
    // fade the second hand
    // mscounter is the millis() time of the last minute rollover
    int msoffset = millis() - mscounter;
    int sec = msoffset/animtime;
    
        strip.setPixelColor(sec,0,0,0);

        int pr=0,pg=0,pb=0;
        pr=map(msoffset%animtime, 0, animtime, 0, r);
        pg=map(msoffset%animtime, 0, animtime, 0, g);
        pb=map(msoffset%animtime, 0, animtime, 0, b);
        if (sec == 0) {
            strip.setPixelColor(PIXEL_COUNT-1, gamma[pr], gamma[pg], gamma[pb]);
        } else {
            strip.setPixelColor(sec-1, gamma[pr], gamma[pg], gamma[pb]);
        }

        int nr=0,ng=0,nb=0;
        nr=map(msoffset%animtime, 0, animtime, r, 0);
        ng=map(msoffset%animtime, 0, animtime, g, 0);
        nb=map(msoffset%animtime, 0, animtime, b, 0);
        strip.setPixelColor(sec+1, gamma[nr], gamma[ng], gamma[nb]);

        int min = map(Time.minute(), 0, 59, 0, PIXEL_COUNT-1);
        strip.setPixelColor(min, gamma[(r+64)%255], gamma[g], gamma[b]);

        //int hr = map(Time.hourFormat12(Time.local()), 1, 12, 0, NUM_LEDS-1);
        int hr = map((Time.hourFormat12()*60)+Time.minute(), 60, 12*60, 0, PIXEL_COUNT-1);
        strip.setPixelColor(hr, gamma[r], gamma[(g+64)%255], gamma[b]);
        strip.setPixelColor(hr-1, gamma[r], gamma[(g+64)%255], gamma[b]);
}

void blink(bool dir) {
    int pixel=0;
    for (int j=0; j < PIXEL_COUNT; j++) {
        showClockRGB();
        if (dir) {
            pixel=j;
        } else {
            pixel=PIXEL_COUNT-j-1;
        }
        strip.setPixelColor(j, gamma[r], gamma[g], gamma[b]);
        strip.show();
        delay(10);
    }
}

void teststrand() {
    for (int i=0; i < PIXEL_COUNT; i++) {
        strip.setPixelColor(i, 128, 0, 0);
    }
    strip.show();
    delay(500);
    for (int i=0; i < PIXEL_COUNT; i++) {
            strip.setPixelColor(i, 0, 128, 0);
    }
    strip.show();
    delay(500);
    for (int i=0; i < PIXEL_COUNT; i++) {
            strip.setPixelColor(i, 0, 0, 128);
    }
    strip.show();
    delay(500);
    
}

void rainbowCycle(uint8_t wait) {
  int i, j;
  
  for (j=0; j < 256 * 5; j++) {     // 5 cycles of all 25 colors in the wheel
    for (i=0; i < strip.numPixels(); i++) {
      // tricky math! we use each pixel as a fraction of the full 96-color wheel
      // (thats the i / strip.numPixels() part)
      // Then add in j which makes the colors go around per pixel
      // the % 96 is to make the wheel cycle around
      strip.setPixelColor(i, Wheel( ((i * 256 / strip.numPixels()) + j) % 256) );
    }  
    strip.show();   // write all the pixels out
    delay(wait);
  }
}

uint32_t Wheel(byte WheelPos)
{
  if (WheelPos < 85) {
   return Color(WheelPos * 3, 255 - WheelPos * 3, 0);
  } else if (WheelPos < 170) {
   WheelPos -= 85;
   return Color(255 - WheelPos * 3, 0, WheelPos * 3);
  } else {
   WheelPos -= 170; 
   return Color(0, WheelPos * 3, 255 - WheelPos * 3);
  }
}


// Create a 24 bit color value from R,G,B
uint32_t Color(byte r, byte g, byte b)
{
  uint32_t c;
  c = r;
  c <<= 8;
  c |= g;
  c <<= 8;
  c |= b;
  return c;
}













