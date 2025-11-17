package com.decisioncube.app.data.database

import androidx.room.TypeConverter
import com.decisioncube.app.data.model.WorkoutType

class WorkoutTypeConverter {
    @TypeConverter
    fun fromWorkoutType(type: WorkoutType): String {
        return type.name
    }

    @TypeConverter
    fun toWorkoutType(name: String): WorkoutType {
        return WorkoutType.valueOf(name)
    }
}

