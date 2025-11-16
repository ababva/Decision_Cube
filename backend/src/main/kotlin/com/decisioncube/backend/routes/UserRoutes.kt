package com.decisioncube.backend.routes

import com.decisioncube.backend.database.Users
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.configureUserRoutes() {
    routing {
        route("/api/users") {
            get("/{id}") {
                val userId = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                
                val user = transaction {
                    Users.select { Users.id eq userId }.firstOrNull()
                }
                
                if (user == null) {
                    call.respond(HttpStatusCode.NotFound)
                    return@get
                }
                
                call.respond(UserResponse(
                    user[Users.id].value,
                    user[Users.username],
                    user[Users.email],
                    user[Users.totalExercises],
                    user[Users.weeklyExercises],
                    user[Users.streak]
                ))
            }
            
            get("/search") {
                val query = call.request.queryParameters["query"] ?: ""
                
                val users = transaction {
                    Users.select { Users.username like "%$query%" }
                        .map {
                            UserResponse(
                                it[Users.id].value,
                                it[Users.username],
                                it[Users.email],
                                it[Users.totalExercises],
                                it[Users.weeklyExercises],
                                it[Users.streak]
                            )
                        }
                }
                
                call.respond(users)
            }
            
            get("/leaderboard") {
                val users = transaction {
                    Users.select { Users.totalExercises greaterEq 0 }
                        .orderBy(Users.totalExercises to org.jetbrains.exposed.sql.SortOrder.DESC)
                        .limit(100)
                        .map {
                            UserResponse(
                                it[Users.id].value,
                                it[Users.username],
                                it[Users.email],
                                it[Users.totalExercises],
                                it[Users.weeklyExercises],
                                it[Users.streak]
                            )
                        }
                }
                
                call.respond(users)
            }
        }
    }
}

