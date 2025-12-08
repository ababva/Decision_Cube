package com.decisioncube.core.model
import org.junit.Test
import kotlin.test.assertEquals
class UserTest {
    @Test
    fun testUserCreation() {
        val user = User("1", "Gordey", "")
        assertEquals("Gordey", user.name)
    }
}
