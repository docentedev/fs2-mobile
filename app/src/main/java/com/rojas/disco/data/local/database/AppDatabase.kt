package com.rojas.disco.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.rojas.disco.data.local.entities.AsistenciaClaseEntity
import com.rojas.disco.data.local.entities.ClaseEntity
import com.rojas.disco.data.local.entities.RolEntity
import com.rojas.disco.data.local.entities.UsuarioEntity
import com.rojas.disco.data.local.entities.UsuarioRolCrossRef

@Database(
    entities = [
        UsuarioEntity::class,
        RolEntity::class,
        UsuarioRolCrossRef::class,
        ClaseEntity::class,
        AsistenciaClaseEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun universidadDao(): UniversidadDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "universidad_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
