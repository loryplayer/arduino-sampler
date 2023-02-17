#define ready 'R'
//carattere che indica lo stato "Pronto"
#define delimiter_open '['
//carattere che indica l'inizio di trasmissione da parte di Arduino
#define delimiter_close ']'
//carattere che indica la fine della trasmissione da parte di Arduino

float test_T = 19.352;

float test_H = 10.321;
//32 bit = 4 byte

void setup() {
  pinMode(LED_BUILTIN, OUTPUT);
  Serial.begin(9600);
  // 8 bit (1 byte) = 10 bit trasmissione (1 start, 8 dati, 1 stop, 0 parità)
  // 9600 bit trasmissione = 960 data bit (120 byte)

  // max_f = 120 byte / 4 byte = 30 float al secondo
  while (!Serial) {
    ; // is attesa della connessione seriale
  }
  Serial.write(ready);
}
//Metodo utilizzato per convertire un numero di tipo float in uno in binario formato da quattro otteti.
void sentfloat(float v)
{
  byte *b  = (byte) &v;
  //Serial.write(b, 4);
  unsigned char c[sizeof(v)];
  // c = ['', '', '', '']
  //2^8 = 256 -> 1111111

  memcpy(c, &v, sizeof(v));
  
  Serial.write(delimiter_open);
  for (int i = 0; i< sizeof(v); i++)
  {
    Serial.write(c[i]);
  }
  // 0 1 2 3
  Serial.write(delimiter_close);

  

  /*
  float test = 35.35241;

  Serial.println();
  Serial.print(c[3], 2); // -> 01000010 -> 0 1000010
  Serial.print(" ");
  Serial.print(c[2], 2); // -> 00001101 -> 0 0001101
  Serial.print(" ");
  Serial.print(c[1], 2); // -> 01101000 -> 0 1101000
  Serial.print(" ");
  Serial.print(c[0], 2); // -> 11011110 -> 1 1011110
  Serial.println();
  */

}
int randomNumber;
int i = 0;

void loop() {
  if (Serial.available() > 0) {
    byte incomingByte = Serial.read();
    if (incomingByte != -1)
    {
      randomNumber=random(500);
      switch (char(incomingByte))
      {
        case 'T':
            sentfloat(test_T + (randomNumber)/100);
            break;
        case 'H':
            sentfloat(test_H + (randomNumber)/100);
            break;
      }
      i++;
    }
    
    }
}

