package com.zolarm.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [AlarmEntity::class], version = 1, exportSchema = false)
@TypeConverters(ZolarmConverters::class)
abstract class ZolarmDatabase : RoomDatabase() {

    abstract fun alarmDao(): AlarmDao

    companion object {
        private const val DB_NAME = "zolarm.db"

        @Volatile private var INSTANCE: ZolarmDatabase? = null

        fun get(context: Context): ZolarmDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    ZolarmDatabase::class.java,
                    DB_NAME
                ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
            }
    }
}
