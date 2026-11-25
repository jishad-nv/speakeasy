package com.example.notifications

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object SpeakUpNotificationHelper {
    const val CHANNEL_ID = "speakup_daily_reminders"
    const val EXTRA_START_TODAY_PRACTICE = "extra_start_today_practice"
    const val ACTION_SNOOZE_30 = "com.example.action.SNOOZE_30"
    private const val NOTIFICATION_ID = 1001

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Leo Daily Practice Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Friendly daily reminders from Leo to practice English speaking."
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun showSmartScheduledReminder(context: Context, isManualPreview: Boolean = false) {
        val sp = context.getSharedPreferences("speakup_reminder_meta", Context.MODE_PRIVATE)
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val storedDate = sp.getString("today_key", "") ?: ""
        val completedToday = if (storedDate == today) sp.getInt("completed_count", 0) else 0
        val streakDays = sp.getInt("streak_days", 1)
        val missedDays = sp.getInt("missed_days", 0)
        val unfixedMistakes = sp.getInt("unfixed_mistakes_count", 0)
        val ieltsDaysLeft = sp.getInt("ielts_days_left", 30)

        val sentDate = sp.getString("sent_date", "") ?: ""
        val sentCountToday = if (sentDate == today) sp.getInt("sent_count_today", 0) else 0

        if (!isManualPreview) {
            // Smart logic: skip if today's 5 tasks are already completed or max 3/day reached
            if (completedToday >= 5 || sentCountToday >= 3) return
        }

        val rotatingMessages = buildList {
            add("Time to speak! Just 5 minutes today 🎤")
            if (unfixedMistakes > 0) {
                add("ഇന്നലെ തെറ്റിയ വാക്ക് ഒന്നുകൂടി പറഞ്ഞു നോക്കാം")
            }
            add("ഇന്ന് IELTS Speaking practice ചെയ്യാം")
            if (ieltsDaysLeft in 1..60) {
                add("Your exam is in $ieltsDaysLeft days — warm up with one 2-min drill!")
            }
            add("Your ${streakDays}-day streak is in danger, come save it!")
        }

        val body = when {
            missedDays >= 2 -> "No pressure—just one tiny English sentence with Leo keeps your voice warm."
            else -> rotatingMessages[sentCountToday % rotatingMessages.size]
        }

        showLeoReminderNotification(
            context = context,
            title = "Leo is waiting!",
            body = body
        )

        sp.edit()
            .putString("sent_date", today)
            .putInt("sent_count_today", sentCountToday + 1)
            .apply()
    }

    fun showLeoReminderNotification(
        context: Context,
        title: String = "Leo is waiting!",
        body: String = "Time to speak! Just 5 minutes today 🎤"
    ) {
        ensureChannel(context)
        if (!hasNotificationPermission(context)) return

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_START_TODAY_PRACTICE, true)
        }
        val startNowPi = PendingIntent.getActivity(
            context,
            101,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val snoozeIntent = Intent(context, ReminderReceiver::class.java).apply {
            action = ACTION_SNOOZE_30
        }
        val snoozePi = PendingIntent.getBroadcast(
            context,
            102,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val leoLargeIcon = BitmapFactory.decodeResource(context.resources, R.drawable.ic_launcher_art)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setLargeIcon(leoLargeIcon)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(startNowPi)
            .addAction(android.R.drawable.ic_media_play, "Start now", startNowPi)
            .addAction(android.R.drawable.ic_menu_recent_history, "Remind me in 30 min", snoozePi)
            .setAutoCancel(true)
            .build()

        runCatching {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        }
    }

    fun scheduleSnooze30Min(context: Context) {
        ensureChannel(context)
        runCatching { NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID) }
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java)
        val pi = PendingIntent.getBroadcast(
            context,
            299,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val triggerAt = System.currentTimeMillis() + 30L * 60L * 1000L
        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
    }

    fun scheduleDailyReminders(context: Context, times: List<String>, enabled: Boolean) {
        ensureChannel(context)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        for (slot in 0..2) {
            val intent = Intent(context, ReminderReceiver::class.java)
            val pi = PendingIntent.getBroadcast(
                context,
                200 + slot,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pi)
        }

        if (!enabled) return

        times.take(3).forEachIndexed { slot, timeStr ->
            val parts = timeStr.split(":")
            val hour = parts.getOrNull(0)?.toIntOrNull() ?: return@forEachIndexed
            val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0

            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                if (timeInMillis <= System.currentTimeMillis()) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }

            val intent = Intent(context, ReminderReceiver::class.java)
            val pi = PendingIntent.getBroadcast(
                context,
                200 + slot,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                pi
            )
        }
    }
}

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == SpeakUpNotificationHelper.ACTION_SNOOZE_30) {
            SpeakUpNotificationHelper.scheduleSnooze30Min(context)
        } else {
            SpeakUpNotificationHelper.showSmartScheduledReminder(context, isManualPreview = false)
        }
    }
}
