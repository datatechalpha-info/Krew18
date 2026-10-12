package com.krew18.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        GigEntity::class,
        ApplicationEntity::class,
        ProfileEntity::class,
        SessionEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class KrewDatabase : RoomDatabase() {
    abstract fun gigDao(): GigDao
    abstract fun profileDao(): ProfileDao

    companion object {
        const val NAME = "krew18.db"
    }
}
