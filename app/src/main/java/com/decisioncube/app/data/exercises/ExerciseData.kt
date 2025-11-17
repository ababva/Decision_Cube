package com.decisioncube.app.data.exercises

import com.decisioncube.app.data.model.Exercise
import com.decisioncube.app.data.model.WorkoutType

object ExerciseData {
    val exercises = mapOf(
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
}

