package com.example.subscriptiontracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/** Периодичность списания. */
enum class BillingPeriod(val title: String, val shortTitle: String) {
    WEEKLY("Неделя", "нед."),
    MONTHLY("Месяц", "мес."),
    QUARTERLY("3 месяца", "3 мес."),
    YEARLY("Год", "год");

    /** Дата k-го списания, считая от даты первого списания start. */
    fun dateAt(start: LocalDate, k: Long): LocalDate = when (this) {
        WEEKLY -> start.plusWeeks(k)
        MONTHLY -> start.plusMonths(k)
        QUARTERLY -> start.plusMonths(3 * k)
        YEARLY -> start.plusYears(k)
    }

    /** Сколько периодов (приблизительно) прошло между датами — нужно для быстрого поиска. */
    fun periodsBetween(start: LocalDate, end: LocalDate): Long = when (this) {
        WEEKLY -> ChronoUnit.WEEKS.between(start, end)
        MONTHLY -> ChronoUnit.MONTHS.between(start, end)
        QUARTERLY -> ChronoUnit.MONTHS.between(start, end) / 3
        YEARLY -> ChronoUnit.YEARS.between(start, end)
    }

    /** Коэффициент пересчёта стоимости периода в стоимость за месяц. */
    val perMonth: Double
        get() = when (this) {
            WEEKLY -> 52.0 / 12.0
            MONTHLY -> 1.0
            QUARTERLY -> 1.0 / 3.0
            YEARLY -> 1.0 / 12.0
        }
}

/** Категория подписки и её цвет на диаграмме. */
enum class Category(val title: String, val color: Long) {
    VIDEO("Видео", 0xFFE53935),
    MUSIC("Музыка", 0xFF8E24AA),
    CLOUD("Облако", 0xFF1E88E5),
    GAMES("Игры", 0xFF43A047),
    SOFTWARE("Программы", 0xFFFB8C00),
    EDUCATION("Обучение", 0xFF00ACC1),
    OTHER("Другое", 0xFF6D4C41)
}

/** Подписка — одна строка таблицы subscriptions в базе данных. */
@Entity(tableName = "subscriptions")
data class Subscription(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val price: Double,
    val period: BillingPeriod,
    val category: Category,
    val startDate: LocalDate,
    val notify: Boolean = true,
    val note: String = ""
) {
    /** Стоимость подписки в пересчёте на один месяц. */
    val monthlyCost: Double get() = price * period.perMonth

    /** Номер ближайшего списания, дата которого не раньше from. */
    private fun nextIndex(from: LocalDate): Long {
        if (!startDate.isBefore(from)) return 0
        var k = maxOf(0L, period.periodsBetween(startDate, from) - 1)
        while (period.dateAt(startDate, k).isBefore(from)) k++
        return k
    }

    /** Ближайшая дата списания, не раньше from. */
    fun nextPayment(from: LocalDate = LocalDate.now()): LocalDate =
        period.dateAt(startDate, nextIndex(from))

    /** Все даты списаний в интервале [from; to]. */
    fun paymentsBetween(from: LocalDate, to: LocalDate): List<LocalDate> {
        val result = mutableListOf<LocalDate>()
        var k = nextIndex(from)
        var date = period.dateAt(startDate, k)
        while (!date.isAfter(to)) {
            result.add(date)
            k++
            date = period.dateAt(startDate, k)
        }
        return result
    }
}

/** Суммарные расходы на подписки за месяц. */
fun List<Subscription>.monthlyTotal(): Double = sumOf { it.monthlyCost }
