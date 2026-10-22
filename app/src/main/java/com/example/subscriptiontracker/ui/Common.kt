package com.example.subscriptiontracker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.subscriptiontracker.data.Category
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

val RuLocale: Locale = Locale.forLanguageTag("ru-RU")

val DateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy", RuLocale)

/** Форматирует сумму: 1490.5 -> «1 490,5 ₽». */
fun formatMoney(value: Double): String {
    val format = NumberFormat.getNumberInstance(RuLocale).apply {
        minimumFractionDigits = 0
        maximumFractionDigits = 2
    }
    return format.format(value) + " ₽"
}

/** Склонение слова «день»: 1 день, 2 дня, 5 дней. */
fun daysWord(n: Long): String {
    val mod100 = n % 100
    val mod10 = n % 10
    return when {
        mod100 in 11..14 -> "дней"
        mod10 == 1L -> "день"
        mod10 in 2..4 -> "дня"
        else -> "дней"
    }
}

/** Текст «сегодня», «завтра» или «через N дней» до даты списания. */
fun daysUntilText(date: LocalDate, today: LocalDate = LocalDate.now()): String {
    val days = ChronoUnit.DAYS.between(today, date)
    return when (days) {
        0L -> "сегодня"
        1L -> "завтра"
        else -> "через $days ${daysWord(days)}"
    }
}

/** Цвет категории в формате Compose. */
val Category.uiColor: Color get() = Color(color)

/** Круглый значок с первой буквой названия подписки на фоне цвета категории. */
@Composable
fun SubscriptionAvatar(name: String, category: Category, size: Dp = 44.dp) {
    Box(
        modifier = Modifier
            .size(size)
            .background(category.uiColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name.take(1).uppercase(),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium
        )
    }
}
