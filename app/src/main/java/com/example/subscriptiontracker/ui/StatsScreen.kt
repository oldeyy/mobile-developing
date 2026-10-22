package com.example.subscriptiontracker.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.subscriptiontracker.data.Category
import com.example.subscriptiontracker.data.Subscription
import com.example.subscriptiontracker.data.monthlyTotal

/** Доля расходов одной категории. */
data class CategoryShare(val category: Category, val monthly: Double, val percent: Double)

/** Расходы в месяц по категориям, от большей к меньшей. */
fun categoryShares(subscriptions: List<Subscription>): List<CategoryShare> {
    val total = subscriptions.monthlyTotal()
    if (total <= 0.0) return emptyList()
    return subscriptions
        .groupBy { it.category }
        .map { (category, list) ->
            val sum = list.monthlyTotal()
            CategoryShare(category, sum, sum / total * 100)
        }
        .sortedByDescending { it.monthly }
}

/** Экран 4: статистика расходов. */
@Composable
fun StatsScreen(subscriptions: List<Subscription>) {
    val shares = categoryShares(subscriptions)
    val monthly = subscriptions.monthlyTotal()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Статистика",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(12.dp))

        if (subscriptions.isEmpty()) {
            Text(
                text = "Добавьте подписки, чтобы увидеть статистику расходов",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(vertical = 24.dp)
            )
            return@Column
        }

        // Кольцевая диаграмма с суммой в центре
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp),
            contentAlignment = Alignment.Center
        ) {
            DonutChart(shares, Modifier.size(220.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("в месяц", style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = formatMoney(monthly),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(Modifier.height(16.dp))

        // Легенда диаграммы
        Card(Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                shares.forEach { share ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier
                                .size(14.dp)
                                .background(share.category.uiColor, CircleShape)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(share.category.title, modifier = Modifier.weight(1f))
                        Text(
                            text = "${formatMoney(share.monthly)}  (${"%.0f".format(share.percent)}%)",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))

        val mostExpensive = subscriptions.maxBy { it.monthlyCost }
        StatCard("Расходы в год", formatMoney(monthly * 12))
        StatCard("Средняя подписка в месяц", formatMoney(monthly / subscriptions.size))
        StatCard(
            "Самая дорогая подписка",
            "${mostExpensive.name} — ${formatMoney(mostExpensive.monthlyCost)} / мес."
        )
    }
}

@Composable
private fun StatCard(title: String, value: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

/** Кольцевая диаграмма: каждый сектор — доля расходов категории. */
@Composable
fun DonutChart(shares: List<CategoryShare>, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val strokeWidth = 36.dp.toPx()
        val diameter = size.minDimension - strokeWidth
        val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
        val gap = if (shares.size > 1) 1.5f else 0f // небольшой зазор между секторами
        var startAngle = -90f // начинаем сверху
        shares.forEach { share ->
            val sweep = (share.percent / 100 * 360).toFloat()
            drawArc(
                color = share.category.uiColor,
                startAngle = startAngle + gap / 2,
                sweepAngle = (sweep - gap).coerceAtLeast(0.5f),
                useCenter = false,
                topLeft = topLeft,
                size = Size(diameter, diameter),
                style = Stroke(width = strokeWidth)
            )
            startAngle += sweep
        }
    }
}
