// Replace controllerid with your controller's id
// Replace app-id with your app's id

volatile unsigned long nextcloudcomm=0L;
const long cloudcomminterval=60000L;
const char
    controllerid[] = "1234567890",
    endpoint[]      = "/controller/device?",
    access_token[] = "&auth=test",
    host[]          = "<app-id>.appspot.com",
    useragent[]    = "OHA Projector Firmware v.1",
    // these must match valid states for the controller
    desiredpoweroff[] = "=0;",
    desiredpoweron[] = "=1;",
    devicepoweroff[] = "*001",
    devicepoweron[] = "Model PRO9000";
volatile boolean dirtyflag=false;
TCPClient client;
int rgbred=255,rgbgreen=255,rgbblue=255;

void setup() {
    pinMode(D7, OUTPUT);
    digitalWrite(D7, LOW);
    Serial.begin(9600);
    delay(1000);
    Serial.println("Booting");
    RGB.control(true);
    updateLed(0,0); // set white bootup color
    delay(1000);
    dirtyflag=true;
    nextcloudcomm=millis() + 60000L;
}

void updateLed(int color, int value) {
    if (color==1) {
        rgbred = value;
    } else if (color == 2) {
        rgbgreen = value;
    } else if (color == 3) {
        rgbblue = value;
    }
    RGB.color(rgbred, rgbgreen, rgbblue);
    rgbred-=1; // fade out
    if (rgbred<0) rgbred=0;
}


void loop() {
    updateLed(0,0); // dim the red
    if (dirtyflag) {
        nextcloudcomm = millis() - 10L;
        // force a cloud update
    }
    if (millis() < nextcloudcomm) {
        delay(100);
        return;
    }
    updateLed(3, 255); // blue on
    // update cloud status and get desired state
    int desstate = cloud_comm();
    // set the desired state
    if (desstate == 0) {
        Serial.println("Sending device off");
        digitalWrite(D7, LOW);
    } else if (desstate == 1) {
        // turn on, lower screen
        Serial.println("Sending device on");
        digitalWrite(D7, HIGH);
    }
    nextcloudcomm = millis() + cloudcomminterval;
    dirtyflag=false;
    updateLed(3, 0); // blue off
}

int cloud_comm() {
    // get the desired status
    int sizeofstr=0;
    if (client.connect(host,80)) {
        Serial.print("Get:");
        client.print("GET ");
        client.print(endpoint);
        client.print("k=");
        client.print(controllerid);
        client.print(access_token);
        client.println(" HTTP/1.0");
        client.println("Content-Length: 0");
        client.print("Host: ");
        client.println(host);
        client.print("User-Agent: ");
        client.println(useragent);
        client.println();
        int timeout = Time.now()+15;
        while (!client.available() && (Time.now() < timeout)) {
            delay(10);
        }
        if (Time.now() < timeout) {
            int c = 0;
            while(((c = client.read()) > 0) && (c != 'H'));
            if(c == 'H')   Serial.println("OK");
            else if(c < 0) Serial.println("timeout");
            else           Serial.println("error");
            //        return (c == 'H');
        }
        // read the response
        char readresponse[30];
        boolean currentLineIsBlank = true;
        int charcount=0;
        while (client.available() && Time.now() < timeout) {
            char c = client.read();
            if (c == '\n' && currentLineIsBlank ) {
                while (client.available() && Time.now() < timeout && charcount < 29) {
                    c = client.read();
                    charcount++;
                    readresponse[charcount]=c;
                }
                charcount++;
                readresponse[charcount] = 0; // null terminate
            }
            if (c == '\n') {
                currentLineIsBlank=true;
            } else if (c != '\r') {
                currentLineIsBlank=false;
            }
        }
        client.stop();
        if (charcount == 0) {
            updateLed(1, 255); // red on
            return -1;
        }
        //Serial.println(readresponse);
        // now figure out what the response is
        if (strstr(readresponse, desiredpoweroff)) {
            Serial.println("match desired_power off");
            return 0;
        } else if (strstr(readresponse, desiredpoweron)) {
            Serial.println("match desired_power on");
            return 1;
        } else {
            Serial.println(readresponse);
        }
        
    } else {
        Serial.println("connection failed");
        updateLed(1, 255); // red on
    }
    return -1;
}

