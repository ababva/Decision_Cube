package com.decisioncube.app.data.repository

import com.decisioncube.app.data.database.ExerciseDao
import com.decisioncube.app.data.database.ExerciseEntity
import com.decisioncube.app.data.database.UserDao
import com.decisioncube.app.data.database.UserEntity
import com.decisioncube.app.data.model.DailyStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.*

class FitnessRepository(
    private val exerciseDao: ExerciseDao,
    private val userDao: UserDao
) {
    fun getAllExercises(): Flow<List<ExerciseEntity>> = exerciseDao.getAllExercises()

    fun getExercisesByDate(date: String): Flow<List<ExerciseEntity>> = 
        exerciseDao.getExercisesByDate(date)

    suspend fun getLast7DaysStats(): List<DailyStats> {
        return exerciseDao.getLast7DaysStatsRaw().map { 
            DailyStats(it.date, it.count) 
        }
    }

    suspend fun insertExercise(exercise: ExerciseEntity) {
        exerciseDao.insertExercise(exercise)
    }

    suspend fun clearStatistics() {
        exerciseDao.clearAll()
    }

    fun getUserById(userId: String): Flow<UserEntity?> = userDao.getUserById(userId)

    suspend fun insertUser(user: UserEntity) {
        userDao.insertUser(user)
    }

    suspend fun updateUser(user: UserEntity) {
        userDao.updateUser(user)
    }

    fun getTodayDate(): String {
        return SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())
    }
}

