package com.decisioncube.backend.database

import org.jetbrains.exposed.sql.Table

object Users : Table("users") {
    val id = varchar("id", 50).primaryKey()
    val username = varchar("username", 100)
    val email = varchar("email", 200)
    val passwordHash = varchar("password_hash", 255)
    val totalExercises = integer("total_exercises").default(0)
    val weeklyExercises = integer("weekly_exercises").default(0)
    val streak = integer("streak").default(0)
    val createdAt = long("created_at")
}

object Exercises : Table("exercises") {
    val id = integer("id").autoIncrement().primaryKey()
    val userId = varchar("user_id", 50).references(Users.id)
    val name = varchar("name", 200)
    val description = text("description")
    val duration = varchar("duration", 50)
    val type = varchar("type", 50)
    val date = varchar("date", 20)
    val timestamp = long("timestamp")
}

object DailyStats : Table("daily_stats") {
    val id = integer("id").autoIncrement().primaryKey()
    val userId = varchar("user_id", 50).references(Users.id)
    val date = varchar("date", 20)
    val count = integer("count").default(0)
}

