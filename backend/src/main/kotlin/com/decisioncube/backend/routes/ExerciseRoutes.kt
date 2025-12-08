package com.decisioncube.backend.routes

import com.decisioncube.backend.database.Exercises
import com.decisioncube.backend.database.Users
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

fun Application.configureExerciseRoutes() {
    routing {
        route("/api/exercises") {
            post {
                val request = call.receive<ExerciseRequest>()
                val userId = call.request.headers["X-User-Id"] ?: "demo_user"
                
                transaction {
                    Exercises.insert {
                        it[this.userId] = userId
                        it[name] = request.name
                        it[description] = request.description
                        it[duration] = request.duration
                        it[type] = request.type
                        it[date] = request.date
                        it[timestamp] = System.currentTimeMillis()
                    }
                    
                    val user = Users.select { Users.id eq (userId as String) }.firstOrNull()
                    if (user != null) {
                        Users.update({ Users.id eq (userId as String) }) {
                            it[totalExercises] = user[Users.totalExercises] + 1
                            it[weeklyExercises] = user[Users.weeklyExercises] + 1
                        }
                    }
                }
                
                call.respond(HttpStatusCode.Created)
            }
        }
    }
}

data class ExerciseRequest(
    val name: String,
    val description: String,
    val duration: String,
    val type: String,
    val date: String
)

