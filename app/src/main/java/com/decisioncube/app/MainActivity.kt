package com.decisioncube.app

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.sqrt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FitnessCubeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    FitnessCubeApp()
                }
            }
        }
    }
}

@Composable
fun FitnessCubeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFF1976D2),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFE3F2FD),
            onPrimaryContainer = Color(0xFF0D47A1),
            secondary = Color(0xFF424242),
            onSecondary = Color.White,
            surface = Color.White,
            onSurface = Color(0xFF212121),
            background = Color(0xFFFAFAFA),
            onBackground = Color(0xFF212121),
            surfaceVariant = Color(0xFFF5F5F5),
            onSurfaceVariant = Color(0xFF616161),
            outline = Color(0xFFBDBDBD)
        ),
        content = content
    )
}

enum class Screen {
    LOGIN, REGISTER, MAIN, STATISTICS, FRIENDS, LEADERBOARD
}

enum class WorkoutType(val displayName: String, val diceValue: Int) {
    CARDIO("Кардио", 1),
    STRENGTH("Силовые", 2),
    FLEXIBILITY("Растяжка", 3),
    CORE("Пресс", 4),
    LEGS("Ноги", 5),
    ARMS("Руки", 6)
}

data class User(
    val id: String,
    val username: String,
    val email: String,
    val totalExercises: Int = 0,
    val weeklyExercises: Int = 0,
    val streak: Int = 0,
    val isFriend: Boolean = false
)

data class Exercise(
    val name: String,
    val description: String,
    val duration: String,
    val type: WorkoutType
)

data class DailyStats(
    val date: String,
    val count: Int
)

data class FriendRequest(
    val id: String,
    val fromUser: User,
    val status: String
)

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

class FitnessCubeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(FitnessUiState())
    val uiState: StateFlow<FitnessUiState> = _uiState.asStateFlow()

    private var lastShakeTime = 0L
    private val shakeThrottleMs = 2000L

    private val exercises = mapOf(
        WorkoutType.CARDIO to listOf(
            Exercise("Прыжки на месте", "Энергичные прыжки с разведением рук и ног", "30 сек", WorkoutType.CARDIO),
            Exercise("Бег на месте", "Высоко поднимая колени", "45 сек", WorkoutType.CARDIO),
            Exercise("Берпи", "Полный цикл: присед-планка-отжимание-прыжок", "10 раз", WorkoutType.CARDIO),
            Exercise("Горизонтальные ножницы", "Быстрые движения руками в стороны", "30 сек", WorkoutType.CARDIO),
            Exercise("Прыжки через скакалку", "Имитация прыжков через скакалку", "1 мин", WorkoutType.CARDIO)
        ),
        WorkoutType.STRENGTH to listOf(
            Exercise("Отжимания", "Классические отжимания от пола", "10-15 раз", WorkoutType.STRENGTH),
            Exercise("Приседания", "Глубокие приседания с прямой спиной", "15-20 раз", WorkoutType.STRENGTH),
            Exercise("Выпады", "Поочередные выпады каждой ногой", "10 на каждую", WorkoutType.STRENGTH),
            Exercise("Отжимания от стены", "Отжимания стоя от стены", "15-20 раз", WorkoutType.STRENGTH),
            Exercise("Супермен", "Лежа на животе, поднимать руки и ноги", "10-15 раз", WorkoutType.STRENGTH)
        ),
        WorkoutType.FLEXIBILITY to listOf(
            Exercise("Наклоны к ногам", "Плавные наклоны к прямым ногам", "30 сек", WorkoutType.FLEXIBILITY),
            Exercise("Растяжка шеи", "Медленные повороты и наклоны головы", "1 мин", WorkoutType.FLEXIBILITY),
            Exercise("Кошка-корова", "На четвереньках прогибать и выгибать спину", "10 раз", WorkoutType.FLEXIBILITY),
            Exercise("Растяжка плеч", "Заведение рук за спину и растяжка", "30 сек", WorkoutType.FLEXIBILITY),
            Exercise("Поза ребенка", "Сидя на пятках, наклониться вперед", "1 мин", WorkoutType.FLEXIBILITY)
        ),
        WorkoutType.CORE to listOf(
            Exercise("Планка", "Удержание положения планки", "30-60 сек", WorkoutType.CORE),
            Exercise("Скручивания", "Подъемы корпуса лежа на спине", "15-20 раз", WorkoutType.CORE),
            Exercise("Велосипед", "Имитация езды на велосипеде лежа", "20 раз", WorkoutType.CORE),
            Exercise("Боковая планка", "Планка на боку, по 15 сек на каждую сторону", "30 сек", WorkoutType.CORE),
            Exercise("Подъемы ног", "Подъемы прямых ног лежа на спине", "10-15 раз", WorkoutType.CORE)
        ),
        WorkoutType.LEGS to listOf(
            Exercise("Приседания сумо", "Широкие приседания с разведенными носками", "15 раз", WorkoutType.LEGS),
            Exercise("Подъемы на стул", "Поочередные подъемы на стул или ступеньку", "10 на каждую", WorkoutType.LEGS),
            Exercise("Выпады назад", "Обратные выпады с каждой ноги", "10 на каждую", WorkoutType.LEGS),
            Exercise("Стенка", "Приседание у стены с удержанием", "30-45 сек", WorkoutType.LEGS),
            Exercise("Подъемы ног в стороны", "Подъемы ног в стороны стоя", "15 на каждую", WorkoutType.LEGS)
        ),
        WorkoutType.ARMS to listOf(
            Exercise("Отжимания с колен", "Отжимания с упором на колени", "10-15 раз", WorkoutType.ARMS),
            Exercise("Обратные отжимания", "Отжимания от стула спиной", "10-12 раз", WorkoutType.ARMS),
            Exercise("Планка на руках", "Переходы из планки на предплечьях на руки", "10 раз", WorkoutType.ARMS),
            Exercise("Круги руками", "Большие круги прямыми руками", "30 сек", WorkoutType.ARMS),
            Exercise("Имитация бокса", "Удары руками в воздух", "1 мин", WorkoutType.ARMS)
        )
    )

    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    // Демо данные
    private val demoUsers = listOf(
        User("1", "000", "000@example.com", 156, 23, 7),
        User("2", "111", "111@example.com", 203, 31, 12),
        User("3", "222", "222@example.com", 89, 15, 3),
        User("4", "333", "333e@example.com", 134, 28, 9),
        User("5", "444", "444@example.com", 178, 19, 5)
    )

    init {
        // Инициализация демо данных
        _uiState.value = _uiState.value.copy(
            leaderboard = demoUsers.sortedByDescending { it.totalExercises }
        )
    }

    fun login(username: String, password: String) {
        viewModelScope.launch {
            // Симуляция входа
            delay(1000)
            val user = User("current", username, "$username@example.com", 45, 12, 4)
            _uiState.value = _uiState.value.copy(
                isLoggedIn = true,
                currentUser = user,
                currentScreen = Screen.MAIN
            )
        }
    }

    fun register(username: String, email: String, password: String) {
        viewModelScope.launch {
            // Симуляция регистрации
            delay(1000)
            val user = User("new", username, email, 0, 0, 0)
            _uiState.value = _uiState.value.copy(
                isLoggedIn = true,
                currentUser = user,
                currentScreen = Screen.MAIN
            )
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
            val exercise = exercises[selectedType]?.random() ?: exercises[WorkoutType.CARDIO]!!.first()
            val today = dateFormat.format(Date())

            val currentStats = _uiState.value.dailyStats.toMutableList()
            val todayIndex = currentStats.indexOfFirst { it.date == today }

            if (todayIndex >= 0) {
                currentStats[todayIndex] = currentStats[todayIndex].copy(count = currentStats[todayIndex].count + 1)
            } else {
                currentStats.add(DailyStats(today, 1))
            }

            val last7Days = currentStats.takeLast(7)
            val todayCount = last7Days.find { it.date == today }?.count ?: 0

            // Обновляем статистику пользователя
            val currentUser = _uiState.value.currentUser
            val updatedUser = currentUser?.copy(
                totalExercises = currentUser.totalExercises + 1,
                weeklyExercises = currentUser.weeklyExercises + 1
            )

            _uiState.value = _uiState.value.copy(
                isRolling = false,
                currentExercise = exercise,
                selectedType = selectedType,
                dailyStats = last7Days,
                todayCount = todayCount,
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
    }

    fun clearStatistics() {
        _uiState.value = _uiState.value.copy(
            dailyStats = emptyList(),
            todayCount = 0
        )
    }

    fun searchUsers(query: String) {
        viewModelScope.launch {
            delay(500) // Симуляция поиска
            val results = demoUsers.filter {
                it.username.contains(query, ignoreCase = true) &&
                        it.id != _uiState.value.currentUser?.id
            }
            _uiState.value = _uiState.value.copy(searchResults = results)
        }
    }

    fun sendFriendRequest(user: User) {
        viewModelScope.launch {
            // Симуляция отправки запроса
            delay(500)
            val request = FriendRequest(
                id = UUID.randomUUID().toString(),
                fromUser = _uiState.value.currentUser!!,
                status = "pending"
            )
            // В реальном приложении здесь был бы API вызов
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

class ShakeDetector(
    private val context: Context,
    private val onShake: () -> Unit
) : SensorEventListener {

    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null
    private var lastUpdate: Long = 0
    private var lastX = 0f
    private var lastY = 0f
    private var lastZ = 0f

    companion object {
        private const val SHAKE_THRESHOLD = 1000
        private const val UPDATE_THRESHOLD = 100
    }

    fun start() {
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        accelerometer?.let { sensor ->
            sensorManager?.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stop() {
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let { sensorEvent ->
            val currentTime = System.currentTimeMillis()

            if (currentTime - lastUpdate > UPDATE_THRESHOLD) {
                val timeDiff = currentTime - lastUpdate
                lastUpdate = currentTime

                val x = sensorEvent.values[0]
                val y = sensorEvent.values[1]
                val z = sensorEvent.values[2]

                val speed = sqrt(
                    ((x - lastX) * (x - lastX) +
                            (y - lastY) * (y - lastY) +
                            (z - lastZ) * (z - lastZ)).toDouble()
                ) / timeDiff * 10000

                if (speed > SHAKE_THRESHOLD) {
                    onShake()
                }

                lastX = x
                lastY = y
                lastZ = z
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}

@Composable
fun LoginScreen(
    onLogin: (String, String) -> Unit,
    onNavigateToRegister: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Фитнес-кубик",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(48.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Имя пользователя") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Пароль") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { onLogin(username, password) },
            modifier = Modifier.fillMaxWidth(),
            enabled = username.isNotBlank() && password.isNotBlank()
        ) {
            Text("Войти")
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onNavigateToRegister) {
            Text("Нет аккаунта? Зарегистрироваться")
        }
    }
}

@Composable
fun RegisterScreen(
    onRegister: (String, String, String) -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Регистрация",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Имя пользователя") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Пароль") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Подтвердите пароль") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { onRegister(username, email, password) },
            modifier = Modifier.fillMaxWidth(),
            enabled = username.isNotBlank() &&
                    email.isNotBlank() &&
                    password.isNotBlank() &&
                    password == confirmPassword
        ) {
            Text("Зарегистрироваться")
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onNavigateToLogin) {
            Text("Уже есть аккаунт? Войти")
        }
    }
}

@Composable
fun WorkoutTypeSelector(
    selectedType: WorkoutType,
    onTypeSelected: (WorkoutType) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        WorkoutType.values().forEach { type ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onTypeSelected(type) },
                elevation = CardDefaults.cardElevation(
                    defaultElevation = if (selectedType == type) 4.dp else 1.dp
                ),
                colors = CardDefaults.cardColors(
                    containerColor = if (selectedType == type)
                        MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = type.displayName,
                        fontSize = 16.sp,
                        fontWeight = if (selectedType == type) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (selectedType == type)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Surface(
                        color = if (selectedType == type)
                            MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(
                                text = type.diceValue.toString(),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedType == type)
                                    MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DailyChart(
    dailyStats: List<DailyStats>,
    modifier: Modifier = Modifier
) {
    val maxCount = dailyStats.maxOfOrNull { it.count } ?: 1
    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    val displayFormat = SimpleDateFormat("dd.MM", Locale.getDefault())

    val last7Days = (6 downTo 0).map { daysAgo ->
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -daysAgo)
        val dateStr = dateFormat.format(calendar.time)
        val displayStr = displayFormat.format(calendar.time)
        val count = dailyStats.find { it.date == dateStr }?.count ?: 0
        DailyStats(displayStr, count)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "Активность за неделю",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                last7Days.forEach { day ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        val height = if (maxCount > 0) (day.count.toFloat() / maxCount * 80).coerceAtLeast(4f) else 4f

                        Box(
                            modifier = Modifier
                                .width(24.dp)
                                .height(height.dp)
                                .background(
                                    if (day.count > 0) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceVariant,
                                    RoundedCornerShape(4.dp)
                                )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = day.count.toString(),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (day.count > 0) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = day.date,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DiceFace(
    value: Int,
    modifier: Modifier = Modifier
) {
    val dotPositions = when (value) {
        1 -> listOf(Pair(1, 1))
        2 -> listOf(Pair(0, 0), Pair(2, 2))
        3 -> listOf(Pair(0, 0), Pair(1, 1), Pair(2, 2))
        4 -> listOf(Pair(0, 0), Pair(0, 2), Pair(2, 0), Pair(2, 2))
        5 -> listOf(Pair(0, 0), Pair(0, 2), Pair(1, 1), Pair(2, 0), Pair(2, 2))
        6 -> listOf(Pair(0, 0), Pair(0, 1), Pair(0, 2), Pair(2, 0), Pair(2, 1), Pair(2, 2))
        else -> listOf(Pair(1, 1))
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            repeat(3) { row ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    repeat(3) { col ->
                        Box(
                            modifier = Modifier.size(8.dp)
                        ) {
                            if (dotPositions.contains(Pair(row, col))) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            MaterialTheme.colorScheme.onPrimary,
                                            RoundedCornerShape(4.dp)
                                        )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RotatingCube(
    isRolling: Boolean,
    diceValue: Int,
    modifier: Modifier = Modifier
) {
    val infiniteRotation = rememberInfiniteTransition(label = "rotation")
    val rotation by infiniteRotation.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val rollRotation = remember { Animatable(0f) }

    LaunchedEffect(isRolling) {
        if (isRolling) {
            rollRotation.snapTo(0f)
            rollRotation.animateTo(
                targetValue = 360f * 4 + (0..360).random(),
                animationSpec = tween(
                    durationMillis = 1500,
                    easing = EaseOutBounce
                )
            )
        }
    }

    Card(
        modifier = modifier
            .size(100.dp)
            .rotate(if (isRolling) rollRotation.value else rotation),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            DiceFace(value = diceValue)
        }
    }
}

@Composable
fun BottomNavigationBar(
    currentScreen: Screen,
    onScreenSelected: (Screen) -> Unit,
    todayCount: Int,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            icon = {
                Text("🎲", fontSize = 20.sp)
            },
            label = { Text("Кубик") },
            selected = currentScreen == Screen.MAIN,
            onClick = { onScreenSelected(Screen.MAIN) }
        )

        NavigationBarItem(
            icon = {
                BadgedBox(
                    badge = {
                        if (todayCount > 0) {
                            Badge {
                                Text(todayCount.toString())
                            }
                        }
                    }
                ) {
                    Text("📊", fontSize = 20.sp)
                }
            },
            label = { Text("График") },
            selected = currentScreen == Screen.STATISTICS,
            onClick = { onScreenSelected(Screen.STATISTICS) }
        )

        NavigationBarItem(
            icon = {
                Text("👥", fontSize = 20.sp)
            },
            label = { Text("Друзья") },
            selected = currentScreen == Screen.FRIENDS,
            onClick = { onScreenSelected(Screen.FRIENDS) }
        )

        NavigationBarItem(
            icon = {
                Text("🏆", fontSize = 20.sp)
            },
            label = { Text("Рейтинг") },
            selected = currentScreen == Screen.LEADERBOARD,
            onClick = { onScreenSelected(Screen.LEADERBOARD) }
        )
    }
}

@Composable
fun FriendsScreen(
    friends: List<User>,
    friendRequests: List<FriendRequest>,
    searchResults: List<User>,
    onSearchUsers: (String) -> Unit,
    onSendFriendRequest: (User) -> Unit,
    onAcceptRequest: (FriendRequest) -> Unit,
    onRejectRequest: (FriendRequest) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    onSearchUsers(it)
                },
                label = { Text("Поиск пользователей") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Введите имя пользователя") }
            )
        }

        if (friendRequests.isNotEmpty()) {
            item {
                Text(
                    text = "Запросы в друзья",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            items(friendRequests) { request ->
                FriendRequestCard(
                    request = request,
                    onAccept = { onAcceptRequest(request) },
                    onReject = { onRejectRequest(request) }
                )
            }
        }

        if (friends.isNotEmpty()) {
            item {
                Text(
                    text = "Мои друзья",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            items(friends) { friend ->
                UserCard(
                    user = friend,
                    showAddButton = false,
                    onAddFriend = { }
                )
            }
        }

        if (searchResults.isNotEmpty()) {
            item {
                Text(
                    text = "Результаты поиска",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            items(searchResults) { user ->
                UserCard(
                    user = user,
                    showAddButton = true,
                    onAddFriend = { onSendFriendRequest(user) }
                )
            }
        }
    }
}

@Composable
fun UserCard(
    user: User,
    showAddButton: Boolean,
    onAddFriend: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Аватар
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        MaterialTheme.colorScheme.primary,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = user.username.first().uppercase(),
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.username,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${user.totalExercises} упражнений",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (showAddButton) {
                Button(
                    onClick = onAddFriend,
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("Добавить")
                }
            }
        }
    }
}

@Composable
fun FriendRequestCard(
    request: FriendRequest,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Аватар
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        MaterialTheme.colorScheme.primary,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = request.fromUser.username.first().uppercase(),
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = request.fromUser.username,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Хочет добавить вас в друзья",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onAccept,
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("✓")
                }

                OutlinedButton(
                    onClick = onReject,
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("✗")
                }
            }
        }
    }
}

@Composable
fun LeaderboardScreen(
    leaderboard: List<User>,
    currentUser: User?
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                text = "Рейтинг пользователей",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        item {
            // Заголовок таблицы
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "#",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(32.dp)
                    )
                    Text(
                        text = "Пользователь",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "Всего",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(60.dp),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Неделя",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(60.dp),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Серия",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(60.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        itemsIndexed(leaderboard) { index, user ->
            val isCurrentUser = user.id == currentUser?.id

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isCurrentUser)
                        MaterialTheme.colorScheme.secondaryContainer
                    else MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = if (isCurrentUser) 4.dp else 1.dp
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Позиция
                    Text(
                        text = "${index + 1}",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(32.dp),
                        color = when (index) {
                            0 -> Color(0xFFFFD700) // Золото
                            1 -> Color(0xFFC0C0C0) // Серебро
                            2 -> Color(0xFFCD7F32) // Бронза
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                    )

                    // Аватар и имя
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(
                                    MaterialTheme.colorScheme.primary,
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = user.username.first().uppercase(),
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = user.username,
                            fontWeight = if (isCurrentUser) FontWeight.Bold else FontWeight.Normal
                        )
                    }

                    // Статистика
                    Text(
                        text = user.totalExercises.toString(),
                        modifier = Modifier.width(60.dp),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = user.weeklyExercises.toString(),
                        modifier = Modifier.width(60.dp),
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = user.streak.toString(),
                        modifier = Modifier.width(60.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FitnessCubeApp(viewModel: FitnessCubeViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val vibro = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn) {
            val shakeDetector = ShakeDetector(context) {
                viewModel.rollCube()
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    vibro.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibro.vibrate(200)
                }
            }
            shakeDetector.start()
        }
    }

    when {
        !uiState.isLoggedIn -> {
            when (uiState.currentScreen) {
                Screen.LOGIN -> {
                    LoginScreen(
                        onLogin = viewModel::login,
                        onNavigateToRegister = { viewModel.navigateToScreen(Screen.REGISTER) }
                    )
                }
                Screen.REGISTER -> {
                    RegisterScreen(
                        onRegister = viewModel::register,
                        onNavigateToLogin = { viewModel.navigateToScreen(Screen.LOGIN) }
                    )
                }
                else -> {
                    LoginScreen(
                        onLogin = viewModel::login,
                        onNavigateToRegister = { viewModel.navigateToScreen(Screen.REGISTER) }
                    )
                }
            }
        }
        else -> {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                text = when (uiState.currentScreen) {
                                    Screen.MAIN -> "Фитнес-кубик"
                                    Screen.STATISTICS -> "Статистика"
                                    Screen.FRIENDS -> "Друзья"
                                    Screen.LEADERBOARD -> "Рейтинг"
                                    else -> "Фитнес-кубик"
                                },
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 22.sp
                            )
                        },
                        actions = {
                            TextButton(onClick = viewModel::logout) {
                                Text("Выйти")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                },
                bottomBar = {
                    BottomNavigationBar(
                        currentScreen = uiState.currentScreen,
                        onScreenSelected = viewModel::navigateToScreen,
                        todayCount = uiState.todayCount
                    )
                }
            ) { paddingValues ->
                when (uiState.currentScreen) {
                    Screen.MAIN -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues)
                                .padding(horizontal = 20.dp)
                                .padding(bottom = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            Card(
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp)
                                ) {
                                    Text(
                                        text = "Тип тренировки",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(bottom = 16.dp)
                                    )

                                    WorkoutTypeSelector(
                                        selectedType = uiState.selectedType,
                                        onTypeSelected = viewModel::selectWorkoutType
                                    )
                                }
                            }

                            uiState.currentExercise?.let { exercise ->
                                Card(
                                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = exercise.name,
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            textAlign = TextAlign.Center
                                        )

                                        Spacer(modifier = Modifier.height(12.dp))

                                        Text(
                                            text = exercise.description,
                                            fontSize = 16.sp,
                                            textAlign = TextAlign.Center,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            lineHeight = 22.sp
                                        )

                                        Spacer(modifier = Modifier.height(20.dp))

                                        Surface(
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text(
                                                text = "⏱️ ${exercise.duration}",
                                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                RotatingCube(
                                    isRolling = uiState.isRolling,
                                    diceValue = uiState.currentDiceValue
                                )
                            }

                            Button(
                                onClick = {
                                    viewModel.rollCube()
                                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                        vibro.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
                                    } else {
                                        @Suppress("DEPRECATION")
                                        vibro.vibrate(100)
                                    }
                                },
                                enabled = !uiState.isRolling,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text(
                                    text = if (uiState.isRolling) "Выбираю упражнение..." else "🎲 Получить упражнение",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Screen.STATISTICS -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues)
                                .padding(horizontal = 20.dp)
                                .padding(bottom = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            if (uiState.dailyStats.isNotEmpty()) {
                                DailyChart(
                                    dailyStats = uiState.dailyStats,
                                    modifier = Modifier.weight(1f)
                                )

                                OutlinedButton(
                                    onClick = { viewModel.clearStatistics() },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Text(
                                        "🗑️ Очистить статистику",
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            } else {
                                Card(
                                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(48.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "📊",
                                            fontSize = 64.sp
                                        )

                                        Spacer(modifier = Modifier.height(16.dp))

                                        Text(
                                            text = "Статистика пуста",
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )

                                        Spacer(modifier = Modifier.height(12.dp))

                                        Text(
                                            text = "Выполните упражнения, чтобы увидеть график активности",
                                            fontSize = 16.sp,
                                            textAlign = TextAlign.Center,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            lineHeight = 22.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Screen.FRIENDS -> {
                        FriendsScreen(
                            friends = uiState.friends,
                            friendRequests = uiState.friendRequests,
                            searchResults = uiState.searchResults,
                            onSearchUsers = viewModel::searchUsers,
                            onSendFriendRequest = viewModel::sendFriendRequest,
                            onAcceptRequest = viewModel::acceptFriendRequest,
                            onRejectRequest = viewModel::rejectFriendRequest
                        )
                    }

                    Screen.LEADERBOARD -> {
                        LeaderboardScreen(
                            leaderboard = uiState.leaderboard,
                            currentUser = uiState.currentUser
                        )
                    }

                    else -> {
                        // Fallback to main screen
                    }
                }
            }
        }
    }
}
