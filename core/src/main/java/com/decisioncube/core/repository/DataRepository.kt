package com.decisioncube.core.repository
import com.decisioncube.core.model.User
interface DataRepository {
    suspend fun getUser(id: String): User?
}