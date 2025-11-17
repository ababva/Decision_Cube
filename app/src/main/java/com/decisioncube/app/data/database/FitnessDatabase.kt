package com.decisioncube.app.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [ExerciseEntity::class, UserEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(WorkoutTypeConverter::class)
abstract class FitnessDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: FitnessDatabase? = null

        fun getDatabase(context: Context): FitnessDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FitnessDatabase::class.java,
                    "fitness_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

