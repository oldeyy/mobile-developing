package com.example.subscriptiontracker.data

import java.time.LocalDate

/** Примеры подписок для быстрого знакомства с приложением. */
fun sampleSubscriptions(today: LocalDate): List<Subscription> = listOf(
    Subscription(name = "Онлайн-кинотеатр", price = 399.0, period = BillingPeriod.MONTHLY,
        category = Category.VIDEO, startDate = today.minusMonths(2).plusDays(1)),
    Subscription(name = "Музыкальный сервис", price = 299.0, period = BillingPeriod.MONTHLY,
        category = Category.MUSIC, startDate = today.minusMonths(5).plusDays(6)),
    Subscription(name = "Облако 200 ГБ", price = 1490.0, period = BillingPeriod.YEARLY,
        category = Category.CLOUD, startDate = today.minusMonths(10).plusDays(12)),
    Subscription(name = "Игровой пропуск", price = 599.0, period = BillingPeriod.MONTHLY,
        category = Category.GAMES, startDate = today.minusMonths(1).plusDays(18)),
    Subscription(name = "Курсы английского", price = 2400.0, period = BillingPeriod.QUARTERLY,
        category = Category.EDUCATION, startDate = today.minusMonths(3).plusDays(25)),
    Subscription(name = "Фитнес-приложение", price = 99.0, period = BillingPeriod.WEEKLY,
        category = Category.OTHER, startDate = today.minusWeeks(3).plusDays(3))
)
