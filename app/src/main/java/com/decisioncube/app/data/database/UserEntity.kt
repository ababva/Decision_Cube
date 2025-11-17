package com.decisioncube.app.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String,
    val username: String,
    val email: String,
    val totalExercises: Int = 0,
    val weeklyExercises: Int = 0,
    val streak: Int = 0
)

