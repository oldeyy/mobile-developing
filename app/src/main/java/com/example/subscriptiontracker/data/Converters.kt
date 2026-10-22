package com.example.subscriptiontracker.data

import androidx.room.TypeConverter
import java.time.LocalDate

/** Преобразование дат для хранения в SQLite: LocalDate <-> номер дня от 01.01.1970. */
class Converters {
    @TypeConverter
    fun fromDate(date: LocalDate): Long = date.toEpochDay()

    @TypeConverter
    fun toDate(value: Long): LocalDate = LocalDate.ofEpochDay(value)
}
