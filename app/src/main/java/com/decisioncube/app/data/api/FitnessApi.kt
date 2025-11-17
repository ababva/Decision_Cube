package com.decisioncube.app.data.api

import com.decisioncube.app.data.model.User
import retrofit2.Response
import retrofit2.http.*

interface FitnessApi {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<LoginResponse>

    @GET("users/{id}")
    suspend fun getUser(@Path("id") id: String): Response<User>

    @GET("leaderboard")
    suspend fun getLeaderboard(): Response<List<User>>

    @GET("users/search")
    suspend fun searchUsers(@Query("query") query: String): Response<List<User>>

    @POST("exercises")
    suspend fun saveExercise(@Body exercise: ExerciseRequest): Response<Unit>

    @GET("statistics/daily")
    suspend fun getDailyStats(@Query("days") days: Int = 7): Response<List<DailyStatsResponse>>
}

data class LoginRequest(val username: String, val password: String)
data class RegisterRequest(val username: String, val email: String, val password: String)
data class LoginResponse(val token: String, val user: User)
data class ExerciseRequest(
    val name: String,
    val description: String,
    val duration: String,
    val type: String,
    val date: String
)
data class DailyStatsResponse(val date: String, val count: Int)

