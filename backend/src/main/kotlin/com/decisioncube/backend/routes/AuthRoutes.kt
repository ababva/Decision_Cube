package com.decisioncube.backend.routes

import com.decisioncube.backend.database.Users
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

fun Application.configureAuthRoutes() {
    routing {
        route("/api/auth") {
            post("/register") {
                val request = call.receive<RegisterRequest>()
                
                val existingUser = transaction {
                    Users.select { Users.username eq request.username }.firstOrNull()
                }
                
                if (existingUser != null) {
                    call.respond(HttpStatusCode.Conflict, mapOf("error" to "User already exists"))
                    return@post
                }
                
                val userId = UUID.randomUUID().toString()
                transaction {
                    Users.insert {
                        it[id] = userId
                        it[username] = request.username
                        it[email] = request.email
                        it[passwordHash] = request.password // В реальном приложении нужно хешировать
                        it[totalExercises] = 0
                        it[weeklyExercises] = 0
                        it[streak] = 0
                        it[createdAt] = System.currentTimeMillis()
                    }
                }
                
                call.respond(HttpStatusCode.Created, LoginResponse(
                    token = "demo_token_$userId",
                    user = UserResponse(userId, request.username, request.email, 0, 0, 0)
                ))
            }
            
            post("/login") {
                val request = call.receive<LoginRequest>()
                
                val user = transaction {
                    Users.select { Users.username eq request.username }.firstOrNull()
                }
                
                if (user == null || user[Users.passwordHash] != request.password) {
                    call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid credentials"))
                    return@post
                }
                
                call.respond(LoginResponse(
                    token = "demo_token_${user[Users.id]}",
                    user = UserResponse(
                        user[Users.id],
                        user[Users.username],
                        user[Users.email],
                        user[Users.totalExercises],
                        user[Users.weeklyExercises],
                        user[Users.streak]
                    )
                ))
            }
        }
    }
}

data class LoginRequest(val username: String, val password: String)
data class RegisterRequest(val username: String, val email: String, val password: String)
data class LoginResponse(val token: String, val user: UserResponse)
data class UserResponse(
    val id: String,
    val username: String,
    val email: String,
    val totalExercises: Int,
    val weeklyExercises: Int,
    val streak: Int
)

