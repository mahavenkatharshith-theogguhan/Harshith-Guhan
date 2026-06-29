package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "health_records")
data class HealthRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val heartRate: Int,
    val spo2: Int,
    val temperature: Float,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long,
    val sensorSource: String = "ESP32"
)

@Entity(tableName = "alert_logs")
data class AlertLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val alertType: String, // "FALL", "SOS", "ABNORMAL_HR", "ABNORMAL_SPO2"
    val alertMessage: String,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long,
    val resolved: Boolean = false
)

@Entity(tableName = "medication_reminders")
data class MedicationReminder(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val medicineName: String,
    val dosage: String,
    val time: String, // e.g., "08:00 AM" or "02:00 PM"
    val isTakenToday: Boolean = false,
    val lastTakenTimestamp: Long = 0L,
    val isActive: Boolean = true
)
