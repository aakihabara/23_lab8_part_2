package com.bignerdranch.android.auto.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.bignerdranch.android.auto.Auto

@Database(entities = [ Auto::class ], version=1)
@TypeConverters(AutoTypeConverters::class)
abstract class AutoDatabase : RoomDatabase() {

    abstract fun autoDao(): AutoDao

}