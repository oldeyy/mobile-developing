package com.example.subscriptiontracker.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.subscriptiontracker.data.BillingPeriod
import com.example.subscriptiontracker.data.Category
import com.example.subscriptiontracker.data.Subscription
import com.example.subscriptiontracker.parsePrice
import java.time.LocalDate

private const val MILLIS_IN_DAY = 86_400_000L

/**
 * Экран 2: добавление новой подписки (id = -1) или редактирование существующей.
 * Функция onSave возвращает null при успехе или текст ошибки.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditSubscriptionScreen(
    id: Long,
    loadSubscription: suspend (Long) -> Subscription?,
    onSave: (Subscription, String) -> String?,
    onDelete: (Subscription) -> Unit,
    onBack: () -> Unit
) {
    val isNew = id < 0
    var name by rememberSaveable { mutableStateOf("") }
    var priceText by rememberSaveable { mutableStateOf("") }
    var period by rememberSaveable { mutableStateOf(BillingPeriod.MONTHLY) }
    var category by rememberSaveable { mutableStateOf(Category.VIDEO) }
    var startDay by rememberSaveable { mutableLongStateOf(LocalDate.now().toEpochDay()) }
    var notify by rememberSaveable { mutableStateOf(true) }
    var note by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    var loaded by rememberSaveable { mutableStateOf(isNew) }
    var original by remember { mutableStateOf<Subscription?>(null) }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }

    // При редактировании один раз загружаем подписку из базы и заполняем поля
    LaunchedEffect(id) {
        if (!isNew) {
            val s = loadSubscription(id) ?: return@LaunchedEffect
            original = s
            if (!loaded) {
                name = s.name
                priceText = s.price.toString().removeSuffix(".0").replace('.', ',')
                period = s.period
                category = s.category
                startDay = s.startDate.toEpochDay()
                notify = s.notify
                note = s.note
                loaded = true
            }
        }
    }

    val startDate = LocalDate.ofEpochDay(startDay)

    /** Собирает подписку из полей формы и передаёт её на сохранение. */
    fun submit() {
        val draft = Subscription(
            id = if (isNew) 0 else id,
            name = name,
            price = 0.0, // цена проверяется и разбирается в ViewModel
            period = period,
            category = category,
            startDate = startDate,
            notify = notify,
            note = note
        )
        val result = onSave(draft, priceText)
        if (result == null) onBack() else error = result
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(if (isNew) "Новая подписка" else "Редактирование") },
                windowInsets = WindowInsets(0, 0, 0, 0),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    if (!isNew) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Удалить")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it.replace("\n", ""); error = null },
                label = { Text("Название") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = priceText,
                onValueChange = { priceText = it; error = null },
                label = { Text("Стоимость") },
                suffix = { Text("₽") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            Text("Период списания", style = MaterialTheme.typography.titleSmall)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BillingPeriod.entries.forEach { p ->
                    FilterChip(
                        selected = period == p,
                        onClick = { period = p },
                        label = { Text(p.title) }
                    )
                }
            }

            Text("Категория", style = MaterialTheme.typography.titleSmall)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Category.entries.forEach { c ->
                    FilterChip(
                        selected = category == c,
                        onClick = { category = c },
                        label = { Text(c.title) }
                    )
                }
            }

            // Поле даты только для чтения: по нажатию открывается календарь
            Box {
                OutlinedTextField(
                    value = startDate.format(DateFormat),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Дата первого списания") },
                    trailingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth()
                )
                Box(
                    Modifier
                        .matchParentSize()
                        .clickable { showDatePicker = true }
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Напомнить за день до списания",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                Switch(checked = notify, onCheckedChange = { notify = it })
            }

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Заметка (необязательно)") },
                modifier = Modifier.fillMaxWidth()
            )

            // Подсказка с расчётом: ближайшая дата и стоимость в месяц
            val price = parsePrice(priceText)
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
                    val preview = Subscription(
                        name = name, price = price ?: 0.0, period = period,
                        category = category, startDate = startDate
                    )
                    val next = preview.nextPayment()
                    Text(
                        text = "Ближайшее списание: ${next.format(DateFormat)} (${daysUntilText(next)})",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (price != null && price > 0) {
                        Text(
                            text = "В пересчёте на месяц: ${formatMoney(preview.monthlyCost)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(Modifier.height(4.dp))
            Button(
                onClick = { submit() },
                enabled = loaded,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text("Сохранить", style = MaterialTheme.typography.titleMedium)
            }
        }
    }

    if (showDatePicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = startDay * MILLIS_IN_DAY)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { startDay = it / MILLIS_IN_DAY }
                    showDatePicker = false
                }) { Text("Готово") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Отмена") }
            }
        ) {
            DatePicker(state = state)
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Удалить подписку?") },
            text = { Text("Подписка «$name» будет удалена без возможности восстановления.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    original?.let(onDelete)
                    onBack()
                }) { Text("Удалить", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Отмена") }
            }
        )
    }
}
