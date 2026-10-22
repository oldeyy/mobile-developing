package com.example.subscriptiontracker.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.subscriptiontracker.data.Subscription
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/** Одно предстоящее списание: дата и подписка. */
data class Payment(val date: LocalDate, val subscription: Subscription)

/** Все списания по подпискам в интервале [from; to], отсортированные по дате. */
fun upcomingPayments(subscriptions: List<Subscription>, from: LocalDate, to: LocalDate): List<Payment> =
    subscriptions
        .flatMap { s -> s.paymentsBetween(from, to).map { Payment(it, s) } }
        .sortedWith(compareBy({ it.date }, { it.subscription.name }))

private val DayFormat = DateTimeFormatter.ofPattern("d MMMM, EEEE", RuLocale)
private val PeriodOptions = listOf(7, 30, 90)

/** Экран 3: календарь ближайших списаний. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentsScreen(subscriptions: List<Subscription>, onOpen: (Long) -> Unit) {
    var days by rememberSaveable { mutableIntStateOf(30) }
    val today = LocalDate.now()
    val payments = upcomingPayments(subscriptions, today, today.plusDays(days.toLong() - 1))
    // Группируем списания по дням, чтобы вывести заголовок с датой
    val byDate = payments.groupBy { it.date }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Ближайшие списания",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(12.dp))
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            PeriodOptions.forEachIndexed { index, option ->
                SegmentedButton(
                    selected = days == option,
                    onClick = { days = option },
                    shape = SegmentedButtonDefaults.itemShape(index, PeriodOptions.size)
                ) { Text("$option ${daysWord(option.toLong())}") }
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Всего за $days ${daysWord(days.toLong())}: ${formatMoney(payments.sumOf { it.subscription.price })}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Количество списаний: ${payments.size}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))

        if (payments.isEmpty()) {
            Text(
                text = "В выбранном периоде списаний нет",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(vertical = 24.dp)
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                byDate.forEach { (date, list) ->
                    item(key = date.toEpochDay()) {
                        val title = when (date) {
                            today -> "Сегодня"
                            today.plusDays(1) -> "Завтра"
                            else -> date.format(DayFormat).replaceFirstChar { it.uppercase() }
                        }
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    items(list, key = { "${it.date}-${it.subscription.id}" }) { payment ->
                        PaymentRow(payment, onClick = { onOpen(payment.subscription.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentRow(payment: Payment, onClick: () -> Unit) {
    val s = payment.subscription
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SubscriptionAvatar(s.name, s.category, size = 36.dp)
            Spacer(Modifier.width(12.dp))
            Text(s.name, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            Text(formatMoney(s.price), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        }
    }
}
