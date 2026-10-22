package com.example.subscriptiontracker

import android.app.Application
import com.example.subscriptiontracker.notify.NotificationHelper
import com.example.subscriptiontracker.notify.ReminderWorker

/** Класс приложения: при запуске создаёт канал уведомлений и планирует напоминания. */
class TrackerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createChannel(this)
        ReminderWorker.schedule(this)
    }
}
