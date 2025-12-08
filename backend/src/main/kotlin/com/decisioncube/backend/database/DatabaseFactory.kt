package com.decisioncube.backend.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {
    fun init() {
        val config = HikariConfig().apply {
            jdbcUrl = System.getenv("DATABASE_URL") ?: "jdbc:postgresql://localhost:5433/decision_cube"
            driverClassName = "org.postgresql.Driver"
            username = System.getenv("DB_USER") ?: "dc_user"
            password = System.getenv("DB_PASSWORD") ?: "dc_password"
            maximumPoolSize = 10
            isAutoCommit = false
        }

        val dataSource = HikariDataSource(config)
        Database.connect(dataSource)

        transaction {
            SchemaUtils.create(Users, Exercises, DailyStats)
        }
    }
}

