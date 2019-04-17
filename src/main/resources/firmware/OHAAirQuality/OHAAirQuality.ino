//#define SENSORID 123456890
//Replace with your actual sensorid
//Thank you @ladyada! Air Quality Modules available on adafruit.com

#define MAXRETRIES 3

int sensorid=SENSORID; // because sprintf doesn't like #defines
static char rpvalue[25];
long lastupdate=0L;
boolean ledlit=false;
static int pmmax=0;

void setup() {
  Particle.variable("rpvalue", rpvalue);
  pinMode(D7, OUTPUT);
  // our debugging output
  Serial.begin(115200);
 
  // sensor baud rate is 9600
  Serial1.begin(9600);
  
}

struct pms5003data {
  uint16_t framelen;
  uint16_t pm10_standard, pm25_standard, pm100_standard;
  uint16_t pm10_env, pm25_env, pm100_env;
  uint16_t particles_03um, particles_05um, particles_10um, particles_25um, particles_50um, particles_100um;
  uint16_t unused;
  uint16_t checksum;
};

struct pms5003data data;
    
void loop() {
    // if there's no data, bail  
    if (!readPMSdata(&Serial1)) return;
    
    // reading data was successful!
    Serial.print(data.pm25_standard);
    Serial.print(" max=");
    Serial.print(pmmax);
    Serial.print(" rpvalue=");
    Serial.print(rpvalue);
    if (data.pm25_standard > pmmax) {
        Serial.print(" *** old high:");
        Serial.print(pmmax);
        pmmax = data.pm25_standard;
        Serial.print(" ret=");
        //Serial.print(sprintf(rpvalue, "%u/%i", SENSORID, pmmax));
        // using #define will ignore var and fill in zero, ???
        Serial.print(sprintf(rpvalue, "%u/%i", sensorid, pmmax));
        Serial.print(" new:");
        Serial.print(rpvalue);
    }
    Serial.print(" tl=");
    Serial.println(lastupdate+240L-Time.now());

    if (lastupdate==0) lastupdate=Time.now();
    
    if (Time.now() > (lastupdate + 240L)) {
        updatecloud();
        pmmax=0;
        lastupdate=Time.now();
    }
}
 
boolean readPMSdata(Stream *s) {
  if (! s->available()) {
    return false;
  }
  // Read a byte at a time until we get to the special '0x42' start-byte
  if (s->peek() != 0x42) {
    s->read();
    return false;
  }

  // Now read all 32 bytes
  if (s->available() < 32) {
    return false;
  }
    
  char buffer[32];    
  uint16_t sum = 0;
  s->readBytes(buffer, 32);

  // get checksum ready
  for (uint8_t i=0; i<30; i++) {
    sum += buffer[i];
  }

  /* debugging
  for (uint8_t i=2; i<32; i++) {
    Serial.print("0x"); Serial.print(buffer[i], HEX); Serial.print(", ");
  }
  Serial.println();
  */
  
  // The data comes in endian'd, this solves it so it works on all platforms
  uint16_t buffer_u16[15];
  for (uint8_t i=0; i<15; i++) {
    buffer_u16[i] = buffer[2 + i*2 + 1];
    buffer_u16[i] += (buffer[2 + i*2] << 8);
  }

  // put it into a nice struct :)
  memcpy((void *)&data, (void *)buffer_u16, 30);

  if (sum != data.checksum) {
    Serial.println("Checksum failure");
    return false;
  }
  // success!
  flipLED();
  return true;
}

void flipLED() {
    if (ledlit) {
        ledlit=false;
        digitalWrite(D7, LOW);
    } else {
        ledlit=true;
        digitalWrite(D7, HIGH);
    }
}

void updatecloud() {
    sprintf(rpvalue, "%u/%i", sensorid, pmmax);
    int counter=0;
    boolean success=false;
    do {
        Serial.println(rpvalue);
        success = Particle.publish("sens", rpvalue, 60, PRIVATE);
        //success = true;
        Particle.process();
        delay(1000 * counter + random(1000));
    } while (!success && (counter++ < MAXRETRIES));
}








