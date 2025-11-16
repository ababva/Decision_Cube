package com.decisioncube.app.data.model

enum class WorkoutType(val displayName: String, val diceValue: Int) {
    CARDIO("Кардио", 1),
    STRENGTH("Силовые", 2),
    FLEXIBILITY("Растяжка", 3),
    CORE("Пресс", 4),
    LEGS("Ноги", 5),
    ARMS("Руки", 6)
}

