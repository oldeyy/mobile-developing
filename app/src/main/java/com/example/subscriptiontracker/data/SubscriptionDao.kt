package com.example.subscriptiontracker.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

/** Запросы к таблице подписок. */
@Dao
interface SubscriptionDao {

    /** Поток со всеми подписками: Room сам присылает новый список при каждом изменении таблицы. */
    @Query("SELECT * FROM subscriptions ORDER BY name COLLATE NOCASE")
    fun observeAll(): Flow<List<Subscription>>

    @Query("SELECT * FROM subscriptions")
    suspend fun getAll(): List<Subscription>

    @Query("SELECT * FROM subscriptions WHERE id = :id")
    suspend fun getById(id: Long): Subscription?

    /** Добавляет новую запись (id = 0) или обновляет существующую. */
    @Upsert
    suspend fun upsert(subscription: Subscription)

    @Insert
    suspend fun insertAll(subscriptions: List<Subscription>)

    @Delete
    suspend fun delete(subscription: Subscription)
}
