package com.example.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.data.database.AppDatabase
import com.example.data.model.Subject
import com.example.data.model.Chapter
import com.example.data.model.StudySession
import com.example.data.dao.StudyDao

@Database(
    entities = [Subject::class, Chapter::class, StudySession::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun studyDao(): StudyDao
}
