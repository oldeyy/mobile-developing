package com.example.subscriptiontracker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/** База данных приложения (SQLite через Room). */
@Database(entities = [Subscription::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun subscriptionDao(): SubscriptionDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        /** Возвращает единственный экземпляр базы данных (создаёт его при первом обращении). */
        fun get(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "subscriptions.db"
                ).build().also { instance = it }
            }
    }
}
