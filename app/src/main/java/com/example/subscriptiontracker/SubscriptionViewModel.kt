package com.example.subscriptiontracker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.subscriptiontracker.data.AppDatabase
import com.example.subscriptiontracker.data.Subscription
import com.example.subscriptiontracker.data.sampleSubscriptions
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

const val MAX_NAME_LENGTH = 30
const val MAX_PRICE = 1_000_000.0

/** Разбирает стоимость, введённую пользователем: допускаются точка и запятая. */
fun parsePrice(text: String): Double? = text.trim().replace(',', '.').toDoubleOrNull()

/**
 * Проверка введённых данных. Возвращает текст ошибки или null, если всё верно.
 * existing — уже сохранённые подписки (для проверки повтора названия).
 */
fun validateSubscription(draft: Subscription, priceText: String, existing: List<Subscription>): String? {
    val title = draft.name.trim()
    val price = parsePrice(priceText)
    return when {
        title.isEmpty() -> "Введите название подписки"
        title.length > MAX_NAME_LENGTH -> "Название не длиннее $MAX_NAME_LENGTH символов"
        existing.any { it.id != draft.id && it.name.equals(title, ignoreCase = true) } ->
            "Подписка с таким названием уже есть"
        price == null -> "Введите стоимость числом, например 299 или 149,90"
        price <= 0.0 -> "Стоимость должна быть больше нуля"
        price > MAX_PRICE -> "Слишком большая стоимость"
        else -> null
    }
}

/**
 * Общая ViewModel для всех экранов приложения.
 * Данные хранятся в базе Room, поэтому не теряются после закрытия приложения.
 */
class SubscriptionViewModel(app: Application) : AndroidViewModel(app) {

    private val dao = AppDatabase.get(app).subscriptionDao()

    /** Список подписок; обновляется автоматически при изменении таблицы. */
    val subscriptions: StateFlow<List<Subscription>> = dao.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    suspend fun getById(id: Long): Subscription? = dao.getById(id)

    /**
     * Проверяет и сохраняет подписку draft, стоимость которой введена строкой priceText.
     * Возвращает null при успехе, иначе текст ошибки.
     */
    fun save(draft: Subscription, priceText: String): String? {
        validateSubscription(draft, priceText, subscriptions.value)?.let { return it }
        val subscription = draft.copy(
            name = draft.name.trim(),
            price = parsePrice(priceText)!!,
            note = draft.note.trim()
        )
        viewModelScope.launch { dao.upsert(subscription) }
        return null
    }

    fun delete(subscription: Subscription) {
        viewModelScope.launch { dao.delete(subscription) }
    }

    /** Заполняет пустой список примерами, чтобы быстро посмотреть работу приложения. */
    fun addSamples() {
        viewModelScope.launch { dao.insertAll(sampleSubscriptions(LocalDate.now())) }
    }
}
