package com.bignerdranch.android.auto

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.Room
import com.bignerdranch.android.auto.database.AutoDatabase
import java.util.UUID
import java.util.concurrent.Executors

private const val DATABASE_NAME = "auto-database"

class AutoRepository private constructor(context: Context) {

    private val database : AutoDatabase = Room.databaseBuilder(
        context.applicationContext,
        AutoDatabase::class.java,
        DATABASE_NAME
    ).build()

    private val autoDao = database.autoDao()
    private val executor = Executors.newSingleThreadExecutor()

    fun getAutos(): LiveData<List<Auto>> = autoDao.getAutos()

    fun getAuto(id: UUID): LiveData<Auto?> = autoDao.getAuto(id)

    fun updateAuto(auto: Auto) {
        executor.execute {
            autoDao.updateAuto(auto)
        }
    }

    fun addAuto(auto: Auto) {
        executor.execute {
            autoDao.addAuto(auto)
        }
    }

    companion object {
        private var INSTANCE: AutoRepository? = null
        fun initialize(context: Context) {
            if (INSTANCE == null) {
                INSTANCE = AutoRepository(context)
            }
        }
        fun get(): AutoRepository {
            return INSTANCE ?:
            throw IllegalStateException("AutoRepository must be initialized")
        }
    }
}