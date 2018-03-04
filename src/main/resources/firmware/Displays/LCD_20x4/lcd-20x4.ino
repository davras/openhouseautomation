// This #include statement was automatically added by the Spark IDE.
#include "LiquidCrystal.h"

// Wiring:
// D0 -> LCD4 (RS)
// D1 -> LCD6 (EN)
// D2 -> LCD11 (D4)
// D3 -> LCD12 (D5)
// D4 -> LCD13 (D6)
// D5 -> LCD14 (D7)
// GND -> LCD1
// Vin -> LCD2
// Contrast potentiometer wiper -> LCD3 (or ground for max contrast)
// Gnd -> LCD5

LiquidCrystal lcd(D0, D1, D2, D3, D4, D5);
unsigned long refreshtime=0L,
  displayrefresh=200L,
  nextdisplayrefresh=0L;
#define LED D7
boolean wifiready=false;
TCPClient client;
const char 
    endpoint[]      = "/lcd?k=2",
    host[]          = "gautoard.appspot.com";

unsigned long lastcomm = 0;


void setup() {
    pinMode(LED,OUTPUT);
    Time.zone(-8);
    // set up the LCD's number of columns and rows: 
    lcd.begin(20,4);
    lcd.clear();
    lcd.setCursor(0,0);
    lcd.println("booting");
    Serial.begin(9600);
}

void loop() {
    setWiFiLed();
    if ((Time.now() - lastcomm) > 60 && WiFi.ready()) {
        updateDisplay();
        lastcomm=Time.now();
    }
    if (!WiFi.ready()) {
        WiFi.connect();
    }
    showtime();
}

void showtime() {
    if (millis() < nextdisplayrefresh) {
        return;
    }
    nextdisplayrefresh = millis() + displayrefresh;
    lcd.setCursor(0,3);
    char rssidisp[20];
    sprintf(rssidisp, "RSSI: %d %i %i", WiFi.RSSI(), (Time.now()-lastcomm), (millis()/1000));
    lcd.print(rssidisp);
    
    //lcd.print(Time.timeStr().substring(0,19));
    //lcd.setCursor(0,2);
    //lcd.print(Time.now()-lastcomm);
    //lcd.print(" ");
    //lcd.print(Network.localIP());
}
void setWiFiLed() {
    if (!WiFi.ready()) {
        analogWrite(LED, 130);
        // prevents attempts at comms when wifi is discon.
        lastcomm = Time.now();
        return;
    } else if (!wifiready) {
        analogWrite(LED, 0);
        wifiready=true;
        lastcomm = Time.now();
    }
}

void updateDisplay() {
    Serial.print("connecting...");
    unsigned long starttime = millis();
    Particle.process();
    if (client.connect(host, 80)) {
        Serial.println("connected");
        client.print("GET ");
        client.print(endpoint);
        client.println(" HTTP/1.0");
        client.print("Host: ");
        client.println(host);
        client.println();
        
        long timeout=Time.now() + 20L;
        boolean currentLineIsBlank = true;
        while (client.available() == 0 && (Time.now() < timeout)) {
            delay(10);
            Particle.process();
        }
        int lcdline=0;
        lcd.clear();
        lcd.setCursor(0,lcdline);
        int charcount = 0;
        while (client.available() && (Time.now() < timeout)) {
            char c = client.read();
            Particle.process();
            if (c == '\n' && currentLineIsBlank ) {
                while (client.available() && (Time.now() < timeout) && (charcount < 90)) {
                    c = client.read();
                    charcount++;
                    if (c == '\n') {
                        lcdline++;
                        lcd.setCursor(0,lcdline);
                    } else if (c == '\r') {
                        // ignore
                    } else {
                        lcd.print(c);
                    }
                }
            }
            if (c == '\n') {
                currentLineIsBlank=true;
            } else if (c != '\r') {
                currentLineIsBlank=false;
            }
        }
        refreshtime = millis() - starttime;
    }
    client.stop();
}
