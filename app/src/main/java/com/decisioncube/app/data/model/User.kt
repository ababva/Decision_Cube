package com.decisioncube.app.data.model

data class User(
    val id: String,
    val username: String,
    val email: String,
    val totalExercises: Int = 0,
    val weeklyExercises: Int = 0,
    val streak: Int = 0,
    val isFriend: Boolean = false
)

