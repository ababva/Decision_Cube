package com.decisioncube.backend.routes

import com.decisioncube.backend.database.DailyStats
import com.decisioncube.backend.database.Exercises
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.configureStatisticsRoutes() {
    routing {
        route("/api/statistics") {
            get("/daily") {
                val days = call.request.queryParameters["days"]?.toIntOrNull() ?: 7
                val userId = call.request.headers["X-User-Id"] ?: "demo_user"
                
                val stats = transaction {
                    Exercises
                        .slice(Exercises.date, Exercises.id.count())
                        .select { Exercises.userId.eq(userId) }
                        .groupBy(Exercises.date)
                        .orderBy(Exercises.date to SortOrder.DESC)
                        .limit(days)
                        .map {
                            DailyStatsResponse(
                                it[Exercises.date],
                                it[Exercises.id.count()].toInt()
                            )
                        }
                }
                
                call.respond(stats)
            }
        }
    }
}

data class DailyStatsResponse(val date: String, val count: Int)

