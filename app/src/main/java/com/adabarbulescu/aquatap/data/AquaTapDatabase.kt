package com.adabarbulescu.aquatap.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [IntakeEventEntity::class], version = 1, exportSchema = false)
abstract class AquaTapDatabase : RoomDatabase() {
    abstract fun intakeDao(): IntakeDao

    companion object {
        @Volatile
        private var Instance: AquaTapDatabase? = null

        fun getDatabase(context: Context): AquaTapDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, AquaTapDatabase::class.java, "aquatap_database")
                    .build()
                    .also { Instance = it }
            }
        }
    }
}
