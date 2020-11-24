package app.covidshield.receiver.worker

import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import app.covidshield.MainActivity
import app.covidshield.R
import app.covidshield.extensions.log
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

private const val CHANNEL_ID = "1"
private const val CHANNEL_NAME = "COVID Alert"

class NotificationWorker(private val context: Context, parameters: WorkerParameters) :
        CoroutineWorker(context, parameters) {

    private val notificationManager: NotificationManager = context.applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    override suspend fun doWork(): Result {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(CHANNEL_ID, CHANNEL_NAME)
        }

        val notification: Notification = NotificationCompat.Builder(context, "1")
                .setSmallIcon(inputData.getInt("smallIcon", R.drawable.ic_notification_icon))
                .setContentTitle(inputData.getString("title"))
                .setContentText(inputData.getString("body"))
                .setStyle(NotificationCompat.BigTextStyle().bigText(inputData.getString("body")))
                .setPriority(inputData.getInt("priority", NotificationCompat.PRIORITY_MAX))
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setChannelId(CHANNEL_ID)
                .build()

        val foregroundInfo = ForegroundInfo(1, notification)
        setForeground(foregroundInfo)

        // TODO: How long should the notification appear?
        delay(5000)

        return Result.success()
    }

    /**
     * Create the required notification channel for O+ devices.
     */
    @TargetApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(
            channelId: String,
            name: String
    ): NotificationChannel {
        return NotificationChannel(
                channelId, name, NotificationManager.IMPORTANCE_HIGH
        ).also { channel ->
            notificationManager.createNotificationChannel(channel)
        }
    }

}