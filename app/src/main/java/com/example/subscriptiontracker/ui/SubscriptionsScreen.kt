package com.example.subscriptiontracker.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.subscriptiontracker.data.Category
import com.example.subscriptiontracker.data.Subscription
import com.example.subscriptiontracker.data.monthlyTotal
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/** Экран 1: список подписок и сумма расходов. */
@Composable
fun SubscriptionsScreen(
    subscriptions: List<Subscription>,
    onAdd: () -> Unit,
    onOpen: (Long) -> Unit,
    onAddSamples: () -> Unit
) {
    var filter by rememberSaveable { mutableStateOf<Category?>(null) }
    val today = LocalDate.now()
    // Сортируем по ближайшему списанию и применяем фильтр по категории
    val visible = subscriptions
        .filter { filter == null || it.category == filter }
        .sortedBy { it.nextPayment(today) }
    val usedCategories = Category.entries.filter { c -> subscriptions.any { it.category == c } }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAdd,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Добавить") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Мои подписки",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(12.dp))
            SummaryCard(subscriptions)
            Spacer(Modifier.height(12.dp))

            if (subscriptions.isEmpty()) {
                EmptyState(onAddSamples)
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = filter == null,
                            onClick = { filter = null },
                            label = { Text("Все") }
                        )
                    }
                    items(usedCategories) { category ->
                        FilterChip(
                            selected = filter == category,
                            onClick = { filter = if (filter == category) null else category },
                            label = { Text(category.title) }
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    // Отступ снизу, чтобы кнопка «Добавить» не закрывала последнюю карточку
                    contentPadding = PaddingValues(bottom = 88.dp)
                ) {
                    items(visible, key = { it.id }) { subscription ->
                        SubscriptionCard(subscription, today, onClick = { onOpen(subscription.id) })
                    }
                }
            }
        }
    }
}

/** Карточка с итоговыми суммами. */
@Composable
private fun SummaryCard(subscriptions: List<Subscription>) {
    val monthly = subscriptions.monthlyTotal()
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Column(Modifier.padding(20.dp)) {
            Text("Расходы в месяц", style = MaterialTheme.typography.titleSmall)
            Text(
                text = formatMoney(monthly),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "В год: ${formatMoney(monthly * 12)}  •  Подписок: ${subscriptions.size}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

/** Карточка одной подписки в списке. */
@Composable
private fun SubscriptionCard(subscription: Subscription, today: LocalDate, onClick: () -> Unit) {
    val next = subscription.nextPayment(today)
    val soon = ChronoUnit.DAYS.between(today, next) <= 3
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SubscriptionAvatar(subscription.name, subscription.category)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = subscription.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subscription.category.title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${formatMoney(subscription.price)} / ${subscription.period.shortTitle}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = daysUntilText(next, today),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (soon) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (soon) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

/** Подсказка для пустого списка. */
@Composable
private fun EmptyState(onAddSamples: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Подписок пока нет",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Нажмите «Добавить», чтобы внести первую подписку",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(16.dp))
        OutlinedButton(onClick = onAddSamples) { Text("Заполнить примерами") }
    }
}
