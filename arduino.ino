#include <SoftwareSerial.h>
#define L1 5
#define L2 6
#define R1 9
#define R2 10
#define bluetoothTx 2
#define bluetoothRx 3

String content = "";
SoftwareSerial bluetooth(bluetoothTx, bluetoothRx);

void setup()
{
  pinMode(L1, OUTPUT);
  pinMode(L2, OUTPUT);
  pinMode(R1, OUTPUT);
  pinMode(R2, OUTPUT);

  Serial.begin(9600);
  bluetooth.begin(9600);  // Start bluetooth serial at 9600}
}
void loop()
{
  if (bluetooth.available()) {
    char character = (char) bluetooth.read();
    if (character == '/') {
      Serial.println(content);
      check(content);
      content = "";
    } else {
      content.concat(character);
    }
  }
}

void check(String content) {
  int left = split(content, ',', 0).toInt();
  int right = split(content, ',', 1).toInt();
  rightWheels(right);
  leftWheels(left);
}

void rightWheels(int rightWheel) {
  if (rightWheel > 250) {
    analogWrite(R1, rightWheel - 250);
    digitalWrite(R2, LOW);
  }
  if (rightWheel < 250) {
    digitalWrite(R1, LOW);
    analogWrite(R2, 250 - rightWheel);
  }
}

void leftWheels(int leftWheel) {
  if (leftWheel > 250) {
    analogWrite(L1, leftWheel - 250);
    digitalWrite(L2, LOW);
  }
  if (leftWheel < 250) {
    digitalWrite(L1, LOW);
    analogWrite(L2, 250 - leftWheel);
  }
}

String split(String data, char separator, int index) {
  int found = 0;
  int strIndex[] = {0, -1};
  int maxIndex = data.length() - 1;

  for (int i = 0; i <= maxIndex && found <= index; i++) {
    if (data.charAt(i) == separator || i == maxIndex) {
      found++;
      strIndex[0] = strIndex[1] + 1;
      strIndex[1] = (i == maxIndex) ? i + 1 : i;
    }
  }

  return found > index ? data.substring(strIndex[0], strIndex[1]) : "";
}
