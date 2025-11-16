package com.decisioncube.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.decisioncube.app.data.api.ApiClient
import com.decisioncube.app.data.database.FitnessDatabase
import com.decisioncube.app.data.database.ExerciseEntity
import com.decisioncube.app.data.exercises.ExerciseData
import com.decisioncube.app.data.model.*
import com.decisioncube.app.data.repository.FitnessRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class FitnessViewModel(application: Application) : AndroidViewModel(application) {
    private val database = FitnessDatabase.getDatabase(application)
    private val repository = FitnessRepository(
        database.exerciseDao(),
        database.userDao()
    )
    private val api = ApiClient.fitnessApi

    private val _uiState = MutableStateFlow(FitnessUiState())
    val uiState: StateFlow<FitnessUiState> = _uiState.asStateFlow()

    private var lastShakeTime = 0L
    private val shakeThrottleMs = 2000L
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    // Демо данные для рейтинга
    private val demoUsers = listOf(
        User("1", "000", "000@example.com", 156, 23, 7),
        User("2", "111", "111@example.com", 203, 31, 12),
        User("3", "222", "222@example.com", 89, 15, 3),
        User("4", "333", "333@example.com", 134, 28, 9),
        User("5", "444", "444@example.com", 178, 19, 5)
    )

    init {
        loadStatistics()
        _uiState.value = _uiState.value.copy(
            leaderboard = demoUsers.sortedByDescending { it.totalExercises }
        )
    }

    fun login(username: String, password: String) {
        viewModelScope.launch {
            try {
                val response = api.login(com.decisioncube.app.data.api.LoginRequest(username, password))
                if (response.isSuccessful) {
                    response.body()?.let {
                        _uiState.value = _uiState.value.copy(
                            isLoggedIn = true,
                            currentUser = it.user,
                            currentScreen = Screen.MAIN
                        )
                    }
                } else {
                    // Fallback для демо
                    val user = User("current", username, "$username@example.com", 45, 12, 4)
                    _uiState.value = _uiState.value.copy(
                        isLoggedIn = true,
                        currentUser = user,
                        currentScreen = Screen.MAIN
                    )
                }
            } catch (e: Exception) {
                // Offline mode - используем локальные данные
                val user = User("current", username, "$username@example.com", 45, 12, 4)
                _uiState.value = _uiState.value.copy(
                    isLoggedIn = true,
                    currentUser = user,
                    currentScreen = Screen.MAIN
                )
            }
        }
    }

    fun register(username: String, email: String, password: String) {
        viewModelScope.launch {
            try {
                val response = api.register(com.decisioncube.app.data.api.RegisterRequest(username, email, password))
                if (response.isSuccessful) {
                    response.body()?.let {
                        _uiState.value = _uiState.value.copy(
                            isLoggedIn = true,
                            currentUser = it.user,
                            currentScreen = Screen.MAIN
                        )
                    }
                } else {
                    val user = User("new", username, email, 0, 0, 0)
                    _uiState.value = _uiState.value.copy(
                        isLoggedIn = true,
                        currentUser = user,
                        currentScreen = Screen.MAIN
                    )
                }
            } catch (e: Exception) {
                val user = User("new", username, email, 0, 0, 0)
                _uiState.value = _uiState.value.copy(
                    isLoggedIn = true,
                    currentUser = user,
                    currentScreen = Screen.MAIN
                )
            }
        }
    }

    fun logout() {
        _uiState.value = FitnessUiState(currentScreen = Screen.LOGIN)
    }

    fun rollCube() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastShakeTime < shakeThrottleMs || _uiState.value.isRolling) {
            return
        }
        lastShakeTime = currentTime

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRolling = true)

            delay(1500)

            val randomDiceValue = (1..6).random()
            val selectedType = WorkoutType.values().find { it.diceValue == randomDiceValue } ?: WorkoutType.CARDIO
            val exercise = ExerciseData.exercises[selectedType]?.random() 
                ?: ExerciseData.exercises[WorkoutType.CARDIO]!!.first()
            val today = repository.getTodayDate()

            // Сохраняем в Room
            val exerciseEntity = ExerciseEntity(
                name = exercise.name,
                description = exercise.description,
                duration = exercise.duration,
                type = exercise.type,
                date = today
            )
            repository.insertExercise(exerciseEntity)

            // Отправляем на сервер (если доступен)
            try {
                api.saveExercise(
                    com.decisioncube.app.data.api.ExerciseRequest(
                        exercise.name,
                        exercise.description,
                        exercise.duration,
                        exercise.type.name,
                        today
                    )
                )
            } catch (e: Exception) {
                // Игнорируем ошибки сети
            }

            // Обновляем статистику
            loadStatistics()

            val currentUser = _uiState.value.currentUser
            val updatedUser = currentUser?.copy(
                totalExercises = currentUser.totalExercises + 1,
                weeklyExercises = currentUser.weeklyExercises + 1
            )

            _uiState.value = _uiState.value.copy(
                isRolling = false,
                currentExercise = exercise,
                selectedType = selectedType,
                currentDiceValue = randomDiceValue,
                currentUser = updatedUser
            )
        }
    }

    fun selectWorkoutType(type: WorkoutType) {
        _uiState.value = _uiState.value.copy(
            selectedType = type,
            currentDiceValue = type.diceValue
        )
    }

    fun navigateToScreen(screen: Screen) {
        _uiState.value = _uiState.value.copy(currentScreen = screen)
        if (screen == Screen.STATISTICS) {
            loadStatistics()
        }
    }

    fun clearStatistics() {
        viewModelScope.launch {
            repository.clearStatistics()
            _uiState.value = _uiState.value.copy(
                dailyStats = emptyList(),
                todayCount = 0
            )
        }
    }

    private fun loadStatistics() {
        viewModelScope.launch {
            val stats = repository.getLast7DaysStats()
            val today = repository.getTodayDate()
            val todayCount = stats.find { it.date == today }?.count ?: 0
            _uiState.value = _uiState.value.copy(
                dailyStats = stats,
                todayCount = todayCount
            )
        }
    }

    fun searchUsers(query: String) {
        viewModelScope.launch {
            try {
                val response = api.searchUsers(query)
                if (response.isSuccessful) {
                    response.body()?.let {
                        _uiState.value = _uiState.value.copy(searchResults = it)
                    }
                } else {
                    // Fallback
                    val results = demoUsers.filter {
                        it.username.contains(query, ignoreCase = true) &&
                                it.id != _uiState.value.currentUser?.id
                    }
                    _uiState.value = _uiState.value.copy(searchResults = results)
                }
            } catch (e: Exception) {
                val results = demoUsers.filter {
                    it.username.contains(query, ignoreCase = true) &&
                            it.id != _uiState.value.currentUser?.id
                }
                _uiState.value = _uiState.value.copy(searchResults = results)
            }
        }
    }

    fun sendFriendRequest(user: User) {
        viewModelScope.launch {
            // В реальном приложении здесь был бы API вызов
            delay(500)
        }
    }

    fun acceptFriendRequest(request: FriendRequest) {
        val currentRequests = _uiState.value.friendRequests.toMutableList()
        val currentFriends = _uiState.value.friends.toMutableList()

        currentRequests.removeAll { it.id == request.id }
        currentFriends.add(request.fromUser.copy(isFriend = true))

        _uiState.value = _uiState.value.copy(
            friendRequests = currentRequests,
            friends = currentFriends
        )
    }

    fun rejectFriendRequest(request: FriendRequest) {
        val currentRequests = _uiState.value.friendRequests.toMutableList()
        currentRequests.removeAll { it.id == request.id }
        _uiState.value = _uiState.value.copy(friendRequests = currentRequests)
    }
}

