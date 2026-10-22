package com.example.subscriptiontracker.notify

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.subscriptiontracker.data.AppDatabase
import com.example.subscriptiontracker.ui.formatMoney
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit

/**
 * Фоновая задача: раз в день проверяет, у каких подписок списание завтра,
 * и показывает по ним уведомления.
 */
class ReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val tomorrow = LocalDate.now().plusDays(1)
        val prefs = applicationContext.getSharedPreferences("reminders", Context.MODE_PRIVATE)
        val dao = AppDatabase.get(applicationContext).subscriptionDao()

        dao.getAll()
            .filter { it.notify && it.nextPayment(tomorrow) == tomorrow }
            .forEach { s ->
                // Запоминаем, о каком дне уже напомнили, чтобы не повторять уведомление
                val key = "notified_${s.id}"
                if (prefs.getLong(key, -1) != tomorrow.toEpochDay()) {
                    NotificationHelper.show(
                        applicationContext,
                        id = s.id.toInt(),
                        title = "Завтра списание: ${s.name}",
                        text = "Будет списано ${formatMoney(s.price)}"
                    )
                    prefs.edit().putLong(key, tomorrow.toEpochDay()).apply()
                }
            }
        return Result.success()
    }

    companion object {
        private const val DAILY_WORK = "daily_reminder"
        private const val CHECK_NOW_WORK = "check_now"
        private val REMIND_TIME: LocalTime = LocalTime.of(10, 0)

        /** Планирует ежедневную проверку в 10:00 и одну немедленную проверку при запуске. */
        fun schedule(context: Context) {
            val now = LocalDateTime.now()
            var next = now.toLocalDate().atTime(REMIND_TIME)
            if (!next.isAfter(now)) next = next.plusDays(1)
            val delay = Duration.between(now, next).toMinutes()

            val daily = PeriodicWorkRequestBuilder<ReminderWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(delay, TimeUnit.MINUTES)
                .build()
            val workManager = WorkManager.getInstance(context)
            workManager.enqueueUniquePeriodicWork(DAILY_WORK, ExistingPeriodicWorkPolicy.KEEP, daily)
            workManager.enqueueUniqueWork(
                CHECK_NOW_WORK,
                ExistingWorkPolicy.REPLACE,
                OneTimeWorkRequestBuilder<ReminderWorker>().build()
            )
        }
    }
}
