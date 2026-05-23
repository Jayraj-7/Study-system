package com.example.data.database

import android.content.Context
import androidx.room.Room

object DatabaseProvider {
    @Volatile
    private var instance: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        return instance ?: synchronized(this) {
            val existing = instance
            if (existing != null) {
                existing
            } else {
                val newInstance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "shonen_study_database"
                )
                // Use fallback to destructive migration for fast prototyping / schema updates
                .fallbackToDestructiveMigration()
                .build()
                instance = newInstance
                newInstance
            }
        }
    }
}
