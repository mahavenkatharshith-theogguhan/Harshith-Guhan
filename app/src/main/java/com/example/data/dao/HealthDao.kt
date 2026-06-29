package com.example.data.dao

import androidx.room.*
import com.example.data.model.AlertLog
import com.example.data.model.HealthRecord
import com.example.data.model.MedicationReminder
import kotlinx.coroutines.flow.Flow

@Dao
interface HealthDao {
    // Health Records
    @Query("SELECT * FROM health_records ORDER BY timestamp DESC")
    fun getAllHealthRecords(): Flow<List<HealthRecord>>

    @Query("SELECT * FROM health_records ORDER BY timestamp DESC LIMIT 50")
    fun getRecentHealthRecords(): Flow<List<HealthRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHealthRecord(record: HealthRecord): Long

    @Query("DELETE FROM health_records")
    suspend fun clearAllHealthRecords()

    // Alert Logs
    @Query("SELECT * FROM alert_logs ORDER BY timestamp DESC")
    fun getAllAlertLogs(): Flow<List<AlertLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlertLog(log: AlertLog): Long

    @Update
    suspend fun updateAlertLog(log: AlertLog)

    @Query("UPDATE alert_logs SET resolved = 1 WHERE id = :alertId")
    suspend fun resolveAlert(alertId: Long)

    @Query("DELETE FROM alert_logs")
    suspend fun clearAlertLogs()

    // Medication Reminders
    @Query("SELECT * FROM medication_reminders ORDER BY time ASC")
    fun getAllMedicationReminders(): Flow<List<MedicationReminder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicationReminder(reminder: MedicationReminder): Long

    @Update
    suspend fun updateMedicationReminder(reminder: MedicationReminder)

    @Delete
    suspend fun deleteMedicationReminder(reminder: MedicationReminder)

    @Query("UPDATE medication_reminders SET isTakenToday = :isTaken, lastTakenTimestamp = :timestamp WHERE id = :reminderId")
    suspend fun updateMedicationStatus(reminderId: Long, isTaken: Boolean, timestamp: Long)
}
