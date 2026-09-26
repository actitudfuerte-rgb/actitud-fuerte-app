package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.Measurement
import kotlinx.coroutines.flow.Flow

@Dao
interface MeasurementDao {
    @Query("SELECT * FROM measurements WHERE clientId = :clientId ORDER BY timestamp DESC")
    fun getMeasurementsForClient(clientId: Long): Flow<List<Measurement>>

    @Query("SELECT * FROM measurements WHERE clientId = :clientId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestMeasurementForClient(clientId: Long): Measurement?

    @Query("SELECT * FROM measurements WHERE clientId = :clientId ORDER BY timestamp DESC LIMIT 2")
    suspend fun getLastTwoMeasurementsForClient(clientId: Long): List<Measurement>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeasurement(measurement: Measurement): Long

    @Query("SELECT * FROM measurements ORDER BY timestamp DESC")
    fun getAllMeasurements(): Flow<List<Measurement>>

    @Query("SELECT * FROM measurements ORDER BY timestamp DESC")
    suspend fun getAllMeasurementsOnce(): List<Measurement>
}
