package com.decisioncube.app.data.model

data class FitnessUiState(
    val isLoggedIn: Boolean = false,
    val currentUser: User? = null,
    val isRolling: Boolean = false,
    val currentExercise: Exercise? = null,
    val selectedType: WorkoutType = WorkoutType.CARDIO,
    val dailyStats: List<DailyStats> = emptyList(),
    val currentScreen: Screen = Screen.LOGIN,
    val todayCount: Int = 0,
    val currentDiceValue: Int = 1,
    val friends: List<User> = emptyList(),
    val friendRequests: List<FriendRequest> = emptyList(),
    val searchResults: List<User> = emptyList(),
    val leaderboard: List<User> = emptyList()
)

