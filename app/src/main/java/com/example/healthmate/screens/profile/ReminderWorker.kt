package com.example.healthmate.screens.profile

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.healthmate.R

class ReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        // Nhận dữ liệu xem đây là thông báo uống nước hay tập luyện
        val type = inputData.getString("type") ?: return Result.success()
        showNotification(type)
        return Result.success()
    }

    private fun showNotification(type: String) {
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "healthmate_reminders"


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Nhắc nhở sức khỏe",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            manager.createNotificationChannel(channel)
        }


        val title = if (type == "water") "Đến giờ uống nước rồi! 💧" else "Đã đến giờ tập luyện! 🔥"
        val content = if (type == "water") "Hãy uống một ly nước để cơ thể luôn tươi trẻ nhé." else "Dành ra 15 phút tập luyện để nâng cao sức khỏe nào."

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.mipmap.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        val notificationId = if (type == "water") 101 else 102
        manager.notify(notificationId, notification)
    }
}