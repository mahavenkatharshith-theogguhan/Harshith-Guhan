package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.AlertLog
import com.example.data.model.HealthRecord
import com.example.data.model.MedicationReminder
import com.example.data.repository.HealthRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HealthViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = HealthRepository(db.healthDao())

    // UI flows from Room DB
    val recentVitals: StateFlow<List<HealthRecord>> = repository.recentHealthRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val alertLogs: StateFlow<List<AlertLog>> = repository.allAlertLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val medicationReminders: StateFlow<List<MedicationReminder>> = repository.medicationReminders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ESP32 Live/Mock Connection States
    private val _isEsp32Connected = MutableStateFlow(true)
    val isEsp32Connected: StateFlow<Boolean> = _isEsp32Connected.asStateFlow()

    private val _liveHeartRate = MutableStateFlow(76)
    val liveHeartRate: StateFlow<Int> = _liveHeartRate.asStateFlow()

    private val _liveSpo2 = MutableStateFlow(98)
    val liveSpo2: StateFlow<Int> = _liveSpo2.asStateFlow()

    private val _liveTemperature = MutableStateFlow(36.6f)
    val liveTemperature: StateFlow<Float> = _liveTemperature.asStateFlow()

    private val _liveLatitude = MutableStateFlow(13.0827) // Chennai Default Location
    val liveLatitude: StateFlow<Double> = _liveLatitude.asStateFlow()

    private val _liveLongitude = MutableStateFlow(80.2707)
    val liveLongitude: StateFlow<Double> = _liveLongitude.asStateFlow()

    private val _lastTransmissionTime = MutableStateFlow(System.currentTimeMillis())
    val lastTransmissionTime: StateFlow<Long> = _lastTransmissionTime.asStateFlow()

    // AI Prediction states
    private val _aiPrediction = MutableStateFlow("")
    val aiPrediction: StateFlow<String> = _aiPrediction.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    // Voice assistant states
    private val _assistantChatLog = MutableStateFlow<List<Pair<String, String>>>(
        listOf("Assistant" to "வணக்கம்! உயிர் காவலன் உங்களை அன்போடு வரவேற்கிறது. நான் உங்களுக்கு எவ்வாறு உதவட்டும்? (Hello! Welcome to Uyir Kaavalan. How can I assist you today?)")
    )
    val assistantChatLog: StateFlow<List<Pair<String, String>>> = _assistantChatLog.asStateFlow()

    private val _isAssistantLoading = MutableStateFlow(false)
    val isAssistantLoading: StateFlow<Boolean> = _isAssistantLoading.asStateFlow()

    init {
        // Prepopulate with a few helpful records/reminders if the database is brand new
        viewModelScope.launch {
            repository.medicationReminders.first().let { reminders ->
                if (reminders.isEmpty()) {
                    repository.addMedicationReminder("Metformin (Sugar tablet)", "500mg - 1 Tablet", "08:00 AM")
                    repository.addMedicationReminder("Atorvastatin (Cholesterol tablet)", "10mg - Half Tablet", "02:00 PM")
                    repository.addMedicationReminder("Aspirin (Blood thinner)", "75mg - 1 Tablet", "08:30 PM")
                }
            }

            repository.recentHealthRecords.first().let { records ->
                if (records.isEmpty()) {
                    // Populate some stable historical readings
                    val baseTime = System.currentTimeMillis() - 4 * 3600 * 1000 // 4 hours ago
                    repository.insertHealthRecord(72, 98, 36.5f, 13.0827, 80.2707, "ESP32")
                    repository.insertHealthRecord(75, 97, 36.7f, 13.0825, 80.2705, "ESP32")
                    repository.insertHealthRecord(80, 99, 36.6f, 13.0829, 80.2710, "ESP32")
                }
            }
        }
    }

    // Toggle ESP32 simulation connection status
    fun toggleEsp32Connection() {
        _isEsp32Connected.update { !it }
    }

    // Simulate sending an ESP32 data package (vitals)
    fun sendMockSensorPacket(hr: Int, spo2: Int, temp: Float, lat: Double, lng: Double) {
        _liveHeartRate.value = hr
        _liveSpo2.value = spo2
        _liveTemperature.value = temp
        _liveLatitude.value = lat
        _liveLongitude.value = lng
        _lastTransmissionTime.value = System.currentTimeMillis()

        if (_isEsp32Connected.value) {
            viewModelScope.launch {
                repository.insertHealthRecord(hr, spo2, temp, lat, lng, "ESP32 Wearable")
            }
        }
    }

    // Simulate a hard fall alert instantly (MPU6050 trigger)
    fun simulateFall() {
        _liveHeartRate.value = 118 // Spikes due to shock
        _lastTransmissionTime.value = System.currentTimeMillis()
        viewModelScope.launch {
            repository.triggerAlert(
                type = "FALL",
                message = "CRITICAL: Hard Fall Detected! No response from elder within 15 seconds.",
                lat = _liveLatitude.value,
                lng = _liveLongitude.value
            )
            // Save a health record matching the event
            repository.insertHealthRecord(_liveHeartRate.value, _liveSpo2.value, _liveTemperature.value, _liveLatitude.value, _liveLongitude.value, "ESP32 Accelerometer")
        }
    }

    // Trigger physical SOS button click
    fun triggerSos() {
        _liveHeartRate.value = 125 // Adrenaline spike
        _lastTransmissionTime.value = System.currentTimeMillis()
        viewModelScope.launch {
            repository.triggerAlert(
                type = "SOS",
                message = "EMERGENCY: User pressed the physical SOS button on the ESP32 wearable!",
                lat = _liveLatitude.value,
                lng = _liveLongitude.value
            )
            repository.insertHealthRecord(_liveHeartRate.value, _liveSpo2.value, _liveTemperature.value, _liveLatitude.value, _liveLongitude.value, "ESP32 SOS Button")
        }
    }

    // Resolve an emergency alert
    fun resolveAlert(alertId: Long) {
        viewModelScope.launch {
            repository.resolveAlert(alertId)
        }
    }

    // Clear alert history
    fun clearAllAlertLogs() {
        viewModelScope.launch {
            repository.clearAlerts()
        }
    }

    // Add medication
    fun addMedication(name: String, dosage: String, time: String) {
        viewModelScope.launch {
            repository.addMedicationReminder(name, dosage, time)
        }
    }

    // Toggle medication checked status
    fun toggleMedication(reminder: MedicationReminder) {
        viewModelScope.launch {
            repository.toggleMedicationTaken(reminder)
        }
    }

    // Delete medication
    fun deleteMedication(reminder: MedicationReminder) {
        viewModelScope.launch {
            repository.deleteMedication(reminder)
        }
    }

    // Generate AI prediction report
    fun generateAiReport() {
        viewModelScope.launch {
            _isAiLoading.value = true
            val records = recentVitals.value
            val prediction = repository.generateHealthPrediction(records)
            _aiPrediction.value = prediction
            _isAiLoading.value = false
        }
    }

    // Chat with AI Voice assistant
    fun sendChatMessage(message: String) {
        if (message.isBlank()) return

        // Append user chat message
        _assistantChatLog.update { current -> current + ("User" to message) }

        viewModelScope.launch {
            _isAssistantLoading.value = true
            val reply = repository.voiceAssistantChat(message)
            _assistantChatLog.update { current -> current + ("Assistant" to reply) }
            _isAssistantLoading.value = false
        }
    }

    // Clear assistant chat
    fun clearAssistantChat() {
        _assistantChatLog.value = listOf(
            "Assistant" to "வணக்கம்! உயிர் காவலன் உங்களை அன்போடு வரவேற்கிறது. நான் உங்களுக்கு எவ்வாறு உதவட்டும்? (Hello! Welcome to Uyir Kaavalan. How can I assist you today?)"
        )
    }
}
