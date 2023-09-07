String inputString = ""; // A string to hold incoming data
bool stringComplete = false; // Whether the string is complete

// Define pins for each of the actions
const int motor1 = 8;
const int motor2 = 7;
const int motor3 = 6;
const int motor4 = 5;
const int pinLight = 4;
//const int pinStart = 3;

const int ledPin = 13;

void setup() {
  Serial.begin(9600); // Initialize serial communication at 9600 bps
  inputString.reserve(200); // Reserve 200 bytes for the inputString

    // Set pin modes for each of the defined pins
  pinMode(motor1, OUTPUT);
  pinMode(motor2, OUTPUT);
  pinMode(motor3, OUTPUT);
  pinMode(motor4, OUTPUT);
  pinMode(pinLight, OUTPUT);
 

    pinMode(ledPin, OUTPUT); 
  // Initialize the pins to LOW
  digitalWrite(motor1, LOW);
  digitalWrite(motor2, LOW);
  digitalWrite(motor3, LOW);
  digitalWrite(motor4, LOW);
  digitalWrite(pinLight, LOW);
 
  digitalWrite(ledPin, LOW);
}

void loop() {
  // Check if we have a complete string from Serial input
  // Check if we have a complete string from Serial input
 if (stringComplete) {
    // Switch case based on the input string using .equals()
    if (inputString.equals("forward")) {
      Serial.println("Moving forward");
      digitalWrite(motor1,HIGH);
      digitalWrite(motor2,LOW);
      digitalWrite(motor3,HIGH);
      digitalWrite(motor4,LOW);
      delay(300);
      digitalWrite(motor1,LOW);
      digitalWrite(motor2,LOW);
      digitalWrite(motor3,LOW);
      digitalWrite(motor4,LOW);
        blinkLED(2);
    } else if (inputString.equals("backward")) {
         Serial.println("Moving backward");
      digitalWrite(motor1,LOW);
      digitalWrite(motor2,HIGH);
      digitalWrite(motor3,LOW);
      digitalWrite(motor4,HIGH);
      delay(300);
      digitalWrite(motor1,LOW);
      digitalWrite(motor2,LOW);
      digitalWrite(motor3,LOW);
      digitalWrite(motor4,LOW);
        blinkLED(3);
    } else if (inputString.equals("left")) {
         Serial.println("Moving Left");
      digitalWrite(motor1,LOW);
      digitalWrite(motor2,HIGH);
      digitalWrite(motor3,HIGH);
      digitalWrite(motor4,LOW);
      delay(300);
      digitalWrite(motor1,LOW);
      digitalWrite(motor2,LOW);
      digitalWrite(motor3,LOW);
      digitalWrite(motor4,LOW);
        blinkLED(4);
    } else if (inputString.equals("right")) {
      digitalWrite(motor1,HIGH);
      digitalWrite(motor2,LOW);
      digitalWrite(motor3,LOW);
      digitalWrite(motor4,HIGH);
      delay(300);
      digitalWrite(motor1,LOW);
      digitalWrite(motor2,LOW);
      digitalWrite(motor3,LOW);
      digitalWrite(motor4,LOW);
        blinkLED(5);
    } else if (inputString.equals("light")) {
        Serial.println("light");
        digitalWrite(pinLight, HIGH);
        delay(1000);
        digitalWrite(pinLight, LOW);
       blinkLED(6);
    }  else {
        // Unknown command received
        Serial.print("Unknown command: ");
        Serial.println(inputString);
    }

    // Clear the string for new input
    inputString = "";
    stringComplete = false;
  }
}
void blinkLED(int times) {
  for (int i = 0; i < times; i++) {
    digitalWrite(ledPin, HIGH);  // Turn the LED on
    delay(100);                  // Wait for half a second (500 milliseconds)
    digitalWrite(ledPin, LOW);   // Turn the LED off

  }
}

void serialEvent() {
  while (Serial.available()) {
    // Get the new byte
    char inChar = (char)Serial.read();
    // If the incoming character is a newline, set a flag so the main loop can process it
    if (inChar == '\n') {
      stringComplete = true;
      break;  // Exit the loop once a newline is detected
    } else {
      // If the character is not a newline, add it to the inputString
      inputString += inChar;
    }
  }
}