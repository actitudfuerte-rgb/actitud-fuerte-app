package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.WorkoutLoadLog
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutLoadLogDao {

    @Query("SELECT * FROM workout_load_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<WorkoutLoadLog>>

    @Query("SELECT * FROM workout_load_logs WHERE clientId = :clientId ORDER BY timestamp DESC")
    fun getLogsForClient(clientId: Long): Flow<List<WorkoutLoadLog>>

    @Query("SELECT * FROM workout_load_logs WHERE clientId = :clientId AND exerciseName = :exerciseName ORDER BY timestamp DESC")
    fun getLogsForExercise(clientId: Long, exerciseName: String): Flow<List<WorkoutLoadLog>>

    @Query("SELECT * FROM workout_load_logs WHERE clientId = :clientId AND exerciseName = :exerciseName ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestLogForExercise(clientId: Long, exerciseName: String): WorkoutLoadLog?

    @Query("SELECT * FROM workout_load_logs ORDER BY timestamp DESC")
    suspend fun getAllLogsOnce(): List<WorkoutLoadLog>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: WorkoutLoadLog): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLogs(logs: List<WorkoutLoadLog>)

    @Update
    suspend fun updateLog(log: WorkoutLoadLog)

    @Delete
    suspend fun deleteLog(log: WorkoutLoadLog)

    @Query("DELETE FROM workout_load_logs WHERE id = :id")
    suspend fun deleteLogById(id: Long)
}
