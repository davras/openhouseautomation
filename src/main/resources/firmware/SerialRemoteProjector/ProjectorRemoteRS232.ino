int baudrate=9600;
volatile int status=-1,
    desiredstatus=0;
volatile unsigned long nextcloudcomm=0L,
  nextdevicecomm=0L;
const long cloudcomminterval=60000L,
  devicecomminterval=5000L;
const char
    controllerid[] = "4157520376",
    endpoint[]      = "/controller/device?",
    access_token[] = "&auth=test",
    host[]          = "gautoard.appspot.com",
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
    Serial.begin(9600);
    delay(1000);
    Serial.println("Booting");
    Serial1.begin(baudrate);
    RGB.control(true);
    updateLed(0,0); // set white bootup color
    delay(1000);
    dirtyflag=true;
    nextdevicecomm=millis() - 10L; // force a device comm before cloud comm, important!
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

void readSerial() {
    if (!Serial.available()) return;
    int val = Serial.read();
    if (val == '0') {
        retry(3, "#@&@232002");
    } else if (val == '1') {
        retry(3, "#@&@232001");
    } else {
        Serial.print("Status:");
        if (status == 1) {
            Serial.println("On");
        } else if (status == 0) {
            Serial.println("Off");
        } else {
            Serial.println("Unknown");
        }
    }
}

void loop() {
    //readSerial();
    readSerial1();
    updateLed(0,0); // dim the red
    if (dirtyflag) {
        nextcloudcomm = millis() - 10L;
        // force a cloud update
    }
    if (millis() < nextcloudcomm && millis() < nextdevicecomm) {
        delay(100);
        return;
    }
    if (millis() > nextdevicecomm) {
        updateLed(2, 255); // green on
        // update projector's current status
        send("#@&@232033");
        nextdevicecomm = millis() + devicecomminterval;
        updateLed(2, 0); // green off
        return;
    }
    if (millis() > nextcloudcomm) {
        updateLed(3, 255); // blue on
        // update cloud status and get desired state
        int desstate = cloud_comm();
        // set the desired state
        if (desstate == 0 && status == 1) {
            Serial.println("Sending device off");
            retry(5, "#@&@232002");
        } else if (desstate == 1 && status == 0) {
            Serial.println("Sending device on");
            retry(5, "#@&@232001");
        }
        nextcloudcomm = millis() + cloudcomminterval;
        dirtyflag=false;
        updateLed(3, 0); // blue off
        return;
    }
}

void retry(int maxattempts, char const *msg) {
    for (int i=0; i < maxattempts; i++) {
        delay(100);
        send(msg);
        delay(500);
        Serial1.flush();
    }
}
int cloud_comm() {
    push_current();
    return get_desired();
}

void push_current() {
    Serial.print("pushing current status:");
    Serial.println(status);
    if (status == -1) return;
    // push the current status
    if (client.connect(host,80)) {
        Serial.print("Post:");
        client.print("POST ");
        client.print(endpoint);
        client.print("k=");
        client.print(controllerid);
        client.print("&v=");
        client.print(status);
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
            delay(5);
        }
        if (Time.now() < timeout) {
            int c = 0;
            while(((c = client.read()) > 0) && (c != 'H'));
            if(c == 'H')   Serial.println("OK");
            else if(c < 0) Serial.println("timeout");
            else           Serial.println("error");
            //        return (c == 'H');
        }
        client.stop();
    }
    // connect failed
    else {
        Serial.println("connection failed");
        updateLed(1, 255); // red on
    }

}
int get_desired() {
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
void send(const char *s) {
    delay(100);
    Serial1.flush();
    Serial1.println(s);
    //Serial.println("msg sent, waiting for reply");
}

void readSerial1() {
    if (!Serial1.available()) return;
    char b[20];
    int i=0;
    while (Serial1.available() && (i < 19)) {
        int j = Serial1.read();
        b[i] = (char)j;
        //Serial.print(b[i]);
        i++;
    }
    // drain leftovers
    while (Serial1.available()) {
        Serial1.read();
    }
    if (i>19) {
        b[19] = 0;
    } else {
        b[i] = 0;
    }
    Serial.println();
    Serial.print("Matching: ");
    if (strstr(b, devicepoweroff)) {
        Serial.println("Device is off");
        if (status != 0) dirtyflag=true;
        status=0;
    } else if (strstr(b, devicepoweron)) {
        Serial.println("Device is on");
        if (status != 1) dirtyflag=true;
        status=1;
    } else {
        status=-1; // unknown
    }
}
