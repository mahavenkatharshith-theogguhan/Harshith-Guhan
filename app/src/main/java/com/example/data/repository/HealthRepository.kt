package com.example.data.repository

import com.example.BuildConfig
import com.example.data.api.ContentItem
import com.example.data.api.GeminiApiService
import com.example.data.api.GeminiRequest
import com.example.data.api.PartItem
import com.example.data.dao.HealthDao
import com.example.data.model.AlertLog
import com.example.data.model.HealthRecord
import com.example.data.model.MedicationReminder
import kotlinx.coroutines.flow.Flow
import java.util.Date

class HealthRepository(
    private val healthDao: HealthDao,
    private val geminiApiService: GeminiApiService = GeminiApiService.create()
) {
    val allHealthRecords: Flow<List<HealthRecord>> = healthDao.getAllHealthRecords()
    val recentHealthRecords: Flow<List<HealthRecord>> = healthDao.getRecentHealthRecords()
    val allAlertLogs: Flow<List<AlertLog>> = healthDao.getAllAlertLogs()
    val medicationReminders: Flow<List<MedicationReminder>> = healthDao.getAllMedicationReminders()

    // Thresholds
    private val MIN_NORMAL_HR = 55
    private val MAX_NORMAL_HR = 110
    private val MIN_NORMAL_SPO2 = 93 // Under 93% is dangerous (hypoxia)
    private val MAX_NORMAL_TEMP = 38.2f // In Celsius
    private val MIN_NORMAL_TEMP = 35.0f

    suspend fun insertHealthRecord(
        heartRate: Int,
        spo2: Int,
        temperature: Float,
        latitude: Double,
        longitude: Double,
        sensorSource: String = "ESP32"
    ) {
        val record = HealthRecord(
            heartRate = heartRate,
            spo2 = spo2,
            temperature = temperature,
            latitude = latitude,
            longitude = longitude,
            timestamp = System.currentTimeMillis(),
            sensorSource = sensorSource
        )
        healthDao.insertHealthRecord(record)

        // Perform threshold checks and automatically insert alerts
        checkVitalsAndTriggerAlerts(record)
    }

    private suspend fun checkVitalsAndTriggerAlerts(record: HealthRecord) {
        val now = System.currentTimeMillis()
        if (record.heartRate > MAX_NORMAL_HR) {
            triggerAlert(
                type = "ABNORMAL_HR",
                message = "High Heart Rate Detected: ${record.heartRate} BPM (Normal range: $MIN_NORMAL_HR - $MAX_NORMAL_HR)",
                lat = record.latitude,
                lng = record.longitude
            )
        } else if (record.heartRate < MIN_NORMAL_HR && record.heartRate > 0) {
            triggerAlert(
                type = "ABNORMAL_HR",
                message = "Low Heart Rate Detected: ${record.heartRate} BPM (Bradycardia warning)",
                lat = record.latitude,
                lng = record.longitude
            )
        }

        if (record.spo2 < MIN_NORMAL_SPO2 && record.spo2 > 0) {
            triggerAlert(
                type = "ABNORMAL_SPO2",
                message = "Critical SpO2 Level Detected: ${record.spo2}% (Oxygen saturation too low)",
                lat = record.latitude,
                lng = record.longitude
            )
        }

        if (record.temperature > MAX_NORMAL_TEMP) {
            triggerAlert(
                type = "ABNORMAL_TEMP",
                message = "High Body Temperature Detected: ${"%.1f".format(record.temperature)}°C (High fever)",
                lat = record.latitude,
                lng = record.longitude
            )
        } else if (record.temperature < MIN_NORMAL_TEMP && record.temperature > 0) {
            triggerAlert(
                type = "ABNORMAL_TEMP",
                message = "Low Body Temperature Detected: ${"%.1f".format(record.temperature)}°C (Hypothermia warning)",
                lat = record.latitude,
                lng = record.longitude
            )
        }
    }

    suspend fun triggerAlert(type: String, message: String, lat: Double, lng: Double) {
        val alert = AlertLog(
            alertType = type,
            alertMessage = message,
            latitude = lat,
            longitude = lng,
            timestamp = System.currentTimeMillis(),
            resolved = false
        )
        healthDao.insertAlertLog(alert)
    }

    suspend fun resolveAlert(alertId: Long) {
        healthDao.resolveAlert(alertId)
    }

    suspend fun clearAlerts() {
        healthDao.clearAlertLogs()
    }

    // Medication CRUD
    suspend fun addMedicationReminder(medicineName: String, dosage: String, time: String) {
        val reminder = MedicationReminder(
            medicineName = medicineName,
            dosage = dosage,
            time = time
        )
        healthDao.insertMedicationReminder(reminder)
    }

    suspend fun toggleMedicationTaken(reminder: MedicationReminder) {
        val taken = !reminder.isTakenToday
        val ts = if (taken) System.currentTimeMillis() else 0L
        healthDao.updateMedicationStatus(reminder.id, taken, ts)
    }

    suspend fun resetDailyMedications() {
        // Normally run once a day, resetting isTakenToday to false
        // We'll expose a quick manual reset trigger in the UI for simulation
    }

    suspend fun deleteMedication(reminder: MedicationReminder) {
        healthDao.deleteMedicationReminder(reminder)
    }

    // Gemini AI predictions & report generation
    suspend fun generateHealthPrediction(records: List<HealthRecord>): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return "Please configure your Gemini API Key in the Secrets panel to activate AI Predictions."
        }

        if (records.isEmpty()) {
            return "No health records available to analyze yet. Please start transmitting vitals from the wearable or simulator!"
        }

        val vitalsSummary = records.take(15).joinToString("\n") {
            "Time: ${Date(it.timestamp)}, HR: ${it.heartRate} bpm, SpO2: ${it.spo2}%, Temp: ${it.temperature}°C"
        }

        val prompt = """
            You are "Uyir Kaavalan AI" - a specialized, compassionate health assistant and predictive caregiver for senior citizens.
            Analyze the following list of recent vitals:
            $vitalsSummary
            
            Identify any trends, potential hazards, or health predictions.
            Provide actionable, warm advice in 3-4 bullet points.
            Conclude with a brief Tamil safety greeting or blessing (e.g. "வாழ்க வளமுடன்!" or similar).
            Be concise and medical-alert conscious. Do not give direct diagnoses, but offer helpful preventive advice.
        """.trimIndent()

        return try {
            val response = geminiApiService.generateContent(
                apiKey = apiKey,
                request = GeminiRequest(
                    contents = listOf(ContentItem(parts = listOf(PartItem(text = prompt))))
                )
            )
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text 
                ?: "No prediction generated from AI. Please try again."
        } catch (e: Exception) {
            "Error communicating with Uyir Kaavalan AI: ${e.localizedMessage}"
        }
    }

    // Voice assistant supporting English and Tamil
    suspend fun voiceAssistantChat(userMessage: String): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return "Voice assistant is offline. Please register your Gemini API key in AI Studio Secrets."
        }

        val prompt = """
            You are "Uyir Kaavalan Assistant" (உயிர் காவலன்), an AI voice assistant designed specifically for senior citizens and elders.
            Your role is to respond warmly, reassuringly, and clearly.
            You must support both English and Tamil. If the user asks something in Tamil or with Tamil characters, reply primarily in friendly Tamil. If they ask in English, reply in English, but you can add a short, comforting Tamil phrase if appropriate.
            Keep your answers brief (under 3 sentences) because this will be spoken back or read easily by an elder.
            
            User message: "$userMessage"
            
            Speak directly to the elder with utmost respect (using polite pronouns like 'நீங்கள்' or 'உங்களுக்கு' in Tamil).
        """.trimIndent()

        return try {
            val response = geminiApiService.generateContent(
                apiKey = apiKey,
                request = GeminiRequest(
                    contents = listOf(ContentItem(parts = listOf(PartItem(text = prompt))))
                )
            )
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "வணக்கம். என்னால் இப்போது பதில் கூற முடியவில்லை. (I am unable to reply at this moment.)"
        } catch (e: Exception) {
            "Assistant Error: ${e.localizedMessage}"
        }
    }
}
