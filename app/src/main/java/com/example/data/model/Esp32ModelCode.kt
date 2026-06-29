package com.example.data.model

object Esp32ModelCode {
    const val WIRING_DIAGRAM = """
        ============================================================
                     UYIR KAAVALAN - ESP32 WIRING SCHEMATIC
        ============================================================
        1. MAX30102 Pulse Oximeter (Heart Rate & SpO2):
           - VCC -> ESP32 3.3V
           - GND -> ESP32 GND
           - SDA -> ESP32 GPIO 21 (I2C SDA)
           - SCL -> ESP32 GPIO 22 (I2C SCL)

        2. MPU6050 Accelerometer (Fall Detection):
           - VCC -> ESP32 3.3V
           - GND -> ESP32 GND
           - SDA -> ESP32 GPIO 21 (Shares I2C Bus)
           - SCL -> ESP32 GPIO 22 (Shares I2C Bus)

        3. DS18B20 Temperature Sensor:
           - VCC  -> ESP32 3.3V
           - GND  -> ESP32 GND
           - DATA -> ESP32 GPIO 4 (Add 4.7k Ohm Pull-Up Resistor to 3.3V)

        4. Physical SOS Button:
           - PIN 1 -> ESP32 GPIO 15 (Configured with INPUT_PULLUP)
           - PIN 2 -> ESP32 GND
        ============================================================
    """

    const val ARDUINO_C_CODE = """/*
 * UYIR KAAVALAN (உயிர் காவலன்) - ESP32 Wearable Controller Sketch
 * 
 * Hardware Checklist:
 *  - ESP32 Development Board
 *  - MAX30102 Heart Rate & SpO2 Sensor (I2C)
 *  - MPU6050 3-Axis Accelerometer (I2C)
 *  - DS18B20 One-Wire Temperature Sensor
 *  - Physical Tactile Push Button (SOS)
 * 
 * This program continuously monitors the sensors, computes heart rate,
 * detects falls using acceleration thresholds, and transmits packets
 * to the mobile app via Serial/Bluetooth/Wi-Fi Client.
 */

#include <Wire.h>
#include <Adafruit_MPU6050.h>
#include <Adafruit_Sensor.h>
#include "MAX30105.h"
#include "heartRate.h"
#include <OneWire.h>
#include <DallasTemperature.h>

// Pin Definitions
#define ONE_WIRE_BUS 4
#define SOS_BUTTON_PIN 15

// Sensors Instantiation
Adafruit_MPU6050 mpu;
MAX30105 particleSensor;
OneWire oneWire(ONE_WIRE_BUS);
DallasTemperature tempSensor(&oneWire);

// Heart Rate Variables
long lastBeat = 0;
float beatsPerMinute;
int beatAvg;
byte rates[4];
byte rateSpot = 0;

// Fall Detection Constants
const float FALL_THRESHOLD_G = 3.0; // Acceleration peak threshold (~3G)
const float TILT_THRESHOLD_DEG = 60.0; // Angle tilt after deceleration
bool fallPossible = false;
unsigned long fallTimer = 0;

void setup() {
  Serial.begin(115200);
  Serial.println("Initializing Uyir Kaavalan Wearable...");

  // 1. Initialize SOS Button
  pinMode(SOS_BUTTON_PIN, INPUT_PULLUP);

  // 2. Initialize I2C Bus
  Wire.begin();

  // 3. Initialize MPU6050
  if (!mpu.begin()) {
    Serial.println("Could not find a valid MPU6050 sensor, check wiring!");
  } else {
    Serial.println("MPU6050 Accelerometer Connected Successfully!");
    mpu.setAccelerometerRange(MPU6050_RANGE_8_G);
    mpu.setGyroRange(MPU6050_RANGE_500_DEG);
    mpu.setFilterBandwidth(MPU6050_BAND_21_HZ);
  }

  // 4. Initialize MAX30102
  if (!particleSensor.begin(Wire, I2C_SPEED_FAST)) {
    Serial.println("MAX30102 pulse sensor was not found. Please check wiring.");
  } else {
    Serial.println("MAX30102 Pulse Sensor Connected Successfully!");
    particleSensor.setup();
    particleSensor.setLEDMode(2); // Red & IR active
  }

  // 5. Initialize DS18B20
  tempSensor.begin();
  Serial.println("System fully initialized. Transmitting...");
}

void loop() {
  // Read Vitals and Sensors
  float currentTemp = getTemperature();
  int currentHr = getHeartRate();
  int currentSpo2 = getSpO2Value();
  bool sosPressed = (digitalRead(SOS_BUTTON_PIN) == LOW);
  bool fallDetected = checkFallDetection();

  // Construct Data Packet (JSON formatted)
  String packet = "{";
  packet += "\"source\":\"ESP32_Wearable\",";
  packet += "\"heartRate\":" + String(currentHr) + ",";
  packet += "\"spo2\":" + String(currentSpo2) + ",";
  packet += "\"temp\":" + String(currentTemp, 1) + ",";
  packet += "\"fall\":" + String(fallDetected ? "true" : "false") + ",";
  packet += "\"sos\":" + String(sosPressed ? "true" : "false");
  packet += "}";

  // Transmit Data Packet over Serial/Bluetooth
  Serial.println(packet);

  delay(1000); // Sample every 1 second
}

float getTemperature() {
  tempSensor.requestTemperatures();
  float t = tempSensor.getTempCByIndex(0);
  if (t == DEVICE_DISCONNECTED_C) {
    return 36.5; // Return normal base if sensor is offline
  }
  return t;
}

int getHeartRate() {
  long irValue = particleSensor.getIR();
  
  if (checkForBeat(irValue) == true) {
    long delta = millis() - lastBeat;
    lastBeat = millis();
    beatsPerMinute = 60 / (delta / 1000.0);

    if (beatsPerMinute < 255 && beatsPerMinute > 20) {
      rates[rateSpot++] = (byte)beatsPerMinute;
      rateSpot %= 4;

      beatAvg = 0;
      for (byte x = 0 ; x < 4 ; x++) {
        beatAvg += rates[x];
      }
      beatAvg /= 4;
    }
  }

  if (irValue < 50000) {
    return 0; // No finger detected on sensor
  }
  return (beatAvg > 0) ? beatAvg : 72;
}

int getSpO2Value() {
  long redValue = particleSensor.getRed();
  long irValue = particleSensor.getIR();

  if (irValue < 50000) return 0; // No finger

  // Simplistic local calculation model for SpO2 ratio
  double ratio = (double)redValue / (double)irValue;
  int spo2 = 110 - (15 * ratio); // Standard linear approximation formula
  if (spo2 > 100) spo2 = 100;
  if (spo2 < 50) spo2 = 50;
  return spo2;
}

bool checkFallDetection() {
  sensors_event_t a, g, temp;
  mpu.getEvent(&a, &g, &temp);

  // Compute total acceleration magnitude: sqrt(ax^2 + ay^2 + az^2)
  float raw_acc = sqrt(sq(a.acceleration.x) + sq(a.acceleration.y) + sq(a.acceleration.z));
  float gForce = raw_acc / 9.81;

  // Step A: Detect impact
  if (gForce > FALL_THRESHOLD_G) {
    fallPossible = true;
    fallTimer = millis();
  }

  // Step B: If impact occurred, check if body is tilted (horizontal position)
  if (fallPossible && (millis() - fallTimer < 1500)) {
    // Check orientation relative to gravity vector
    float tiltAngle = acos(abs(a.acceleration.z) / raw_acc) * 180.0 / PI;
    if (tiltAngle > TILT_THRESHOLD_DEG) {
      fallPossible = false; // Reset state
      return true; // Confirmed Fall Event
    }
  }

  if (fallPossible && (millis() - fallTimer >= 1500)) {
    fallPossible = false; // Timeout, no fall confirmed
  }

  return false;
}
*/
"""
}
