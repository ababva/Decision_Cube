package com.decisioncube.app.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {
    @Query("SELECT * FROM exercises ORDER BY timestamp DESC")
    fun getAllExercises(): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercises WHERE date = :date")
    fun getExercisesByDate(date: String): Flow<List<ExerciseEntity>>

    @Query("SELECT date, COUNT(*) as count FROM exercises GROUP BY date ORDER BY date DESC LIMIT 7")
    suspend fun getLast7DaysStatsRaw(): List<DailyStatsEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(exercise: ExerciseEntity)

    @Query("DELETE FROM exercises")
    suspend fun clearAll()
}

data class DailyStatsEntity(
    val date: String,
    val count: Int
)

