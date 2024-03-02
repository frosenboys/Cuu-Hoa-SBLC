#include "BluetoothSerial.h"

BluetoothSerial SerialBT;


void Move(char input){
    if (input == 'f'){
        Serial.println("Forward");
    }
    if (input == 'b'){
        Serial.println("Backward");
    }
    if (input == 'c'){
        // Stop moving Backward or Forward (button releasing)
    }
}

void Turn(char input){
    if (input == 'r'){
        Serial.println("Right");
    }
    if (input == 'l'){
        Serial.println("Left");
    }
    if (input == 's'){
        // Stop turn left or right (button releasing)
    }
}

void Phun(char input){
    //Hold for phun nuoc
    if (input == 'p'){
        Serial.println("Phun nuoc");
    }
    if (input == 'k'){
        Serial.println("Dung phun nuoc"); 
    }
}

void setup()
{
  SerialBT.begin("SBLC Car #01");
  Serial.begin(115200);
  Serial.println("The device started, now you can pair it with bluetooth!");
}

void loop()
{
  if(SerialBT.available() > 0)
  {
    char ch = SerialBT.read();
    if(ch == 'f' || ch == 'b' || ch == 'c') Move(ch);
    else if(ch == 'r' || ch == 'l' || ch == 's') Turn(ch);
    else if(ch == 'p' || ch == 'k') Phun(ch);
  }
}



