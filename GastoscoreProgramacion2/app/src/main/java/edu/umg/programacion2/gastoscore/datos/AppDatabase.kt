package edu.umg.programacion2.gastoscore.datos

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [GastoEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gastoDao(): GastoDao
}
