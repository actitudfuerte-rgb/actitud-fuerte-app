package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.Attendance
import com.example.data.model.Payment
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendances ORDER BY timestamp DESC")
    fun getAllAttendances(): Flow<List<Attendance>>

    @Query("SELECT * FROM attendances WHERE dateOnlyString = :dateStr ORDER BY timestamp DESC")
    fun getAttendancesByDate(dateStr: String): Flow<List<Attendance>>

    @Query("SELECT * FROM attendances WHERE clientId = :clientId ORDER BY timestamp DESC")
    fun getAttendancesForClient(clientId: Long): Flow<List<Attendance>>

    @Query("SELECT COUNT(*) FROM attendances WHERE dateOnlyString = :dateStr")
    fun getTodayCheckInCount(dateStr: String): Flow<Int>

    @Query("SELECT * FROM attendances ORDER BY timestamp DESC")
    suspend fun getAllAttendancesOnce(): List<Attendance>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: Attendance): Long
}

@Dao
interface PaymentDao {
    @Query("SELECT * FROM payments ORDER BY timestamp DESC")
    fun getAllPayments(): Flow<List<Payment>>

    @Query("SELECT * FROM payments ORDER BY timestamp DESC")
    suspend fun getAllPaymentsOnce(): List<Payment>

    @Query("SELECT * FROM payments WHERE clientId = :clientId ORDER BY timestamp DESC")
    fun getPaymentsForClient(clientId: Long): Flow<List<Payment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: Payment): Long

    @Query("SELECT SUM(amount) FROM payments")
    fun getTotalIncome(): Flow<Double?>
}
