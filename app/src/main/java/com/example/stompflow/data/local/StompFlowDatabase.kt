package com.example.stompflow.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [SessionEntity::class], version = 1, exportSchema = false)
abstract class StompFlowDatabase : RoomDatabase() {
    abstract fun sessionDao(): SessionDao

    companion object {
        @Volatile
        private var INSTANCE: StompFlowDatabase? = null

        fun getDatabase(context: Context): StompFlowDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StompFlowDatabase::class.java,
                    "stompflow_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
