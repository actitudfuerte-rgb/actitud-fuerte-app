package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.Attendance
import com.example.data.model.Client
import com.example.data.model.Measurement
import com.example.data.model.Payment
import com.example.data.model.Routine
import com.example.data.model.SocialPost
import com.example.data.model.WorkoutLoadLog

@Database(
    entities = [
        Client::class,
        Measurement::class,
        Attendance::class,
        Payment::class,
        Routine::class,
        SocialPost::class,
        WorkoutLoadLog::class
    ],
    version = 8,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun clientDao(): ClientDao
    abstract fun measurementDao(): MeasurementDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun paymentDao(): PaymentDao
    abstract fun routineDao(): RoutineDao
    abstract fun socialPostDao(): SocialPostDao
    abstract fun workoutLoadLogDao(): WorkoutLoadLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_6_7 = object : androidx.room.migration.Migration(6, 7) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE clients ADD COLUMN gender TEXT NOT NULL DEFAULT 'Hombre'")
            }
        }

        private val MIGRATION_7_8 = object : androidx.room.migration.Migration(7, 8) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS workout_load_logs (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        clientId INTEGER NOT NULL,
                        clientAccessId TEXT NOT NULL DEFAULT '',
                        exerciseName TEXT NOT NULL,
                        muscleGroup TEXT NOT NULL DEFAULT '',
                        dayOfWeek TEXT NOT NULL DEFAULT '',
                        weightKg REAL NOT NULL,
                        reps INTEGER NOT NULL,
                        setsCount INTEGER NOT NULL DEFAULT 4,
                        notes TEXT NOT NULL DEFAULT '',
                        timestamp INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "actitud_fuerte.db"
                )
                .addMigrations(MIGRATION_6_7, MIGRATION_7_8)
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
