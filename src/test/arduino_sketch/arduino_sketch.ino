#define led_pin LED_BUILTIN
void setup() {
  pinMode(LED_BUILTIN, OUTPUT);
  Serial.begin(9600);
  while (!Serial) {
    ; // is attesa della connessione seriale
  }
}

void loop() {
  if (Serial.available() > 0) {    
    byte incomingByte = 0;
    incomingByte = Serial.read();
    if (incomingByte != -1)
    {
      if (incomingByte == 1)
      {
        digitalWrite(LED_BUILTIN, HIGH);
      }else
      {
        digitalWrite(LED_BUILTIN, LOW);
      }
    }
  }
}
