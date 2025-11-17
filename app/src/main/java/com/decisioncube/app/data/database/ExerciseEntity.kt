package com.decisioncube.app.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.decisioncube.app.data.model.WorkoutType

@Entity(tableName = "exercises")
data class ExerciseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String,
    val duration: String,
    val type: WorkoutType,
    val date: String,
    val timestamp: Long = System.currentTimeMillis()
)

