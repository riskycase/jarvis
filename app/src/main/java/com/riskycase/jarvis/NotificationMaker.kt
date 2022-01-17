package com.riskycase.jarvis

import android.app.*
import android.content.Context
import android.widget.Toast
import androidx.core.app.NotificationCompat

class NotificationMaker {
    private lateinit var databaseHelper: DatabaseHelper

    fun makeNotification(context: Context) {
        databaseHelper = DatabaseHelper(context)

        val snaps = databaseHelper.getAllSnaps()

        if(snaps.isNotEmpty()) {

            val senders = mutableMapOf<String, Int>()

            for (snap in snaps) {
                if (senders.containsKey(snap.sender))
                    senders[snap.sender] = senders[snap.sender]!!.plus(1)
                else
                    senders[snap.sender] = 1
            }
            val sendersText = senders.keys.joinToString(", ") { key ->
                return@joinToString if (senders[key] == 1)
                    key
                else
                    key.plus(" (${senders[key]})")
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel: NotificationChannel = android.app.NotificationChannel(
                "messages",
                "Message notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.description = "Channel for all message notifications"
            channel.group = "system"
            val notificationGroup: NotificationChannelGroup =
                android.app.NotificationChannelGroup("system", "Channels")
            notificationManager.createNotificationChannelGroup(notificationGroup)
            notificationManager.createNotificationChannel(channel)
            val builder = NotificationCompat.Builder(context, "messages")
                .setSmallIcon(R.drawable.ic_snapchat_icon)
                .setContentTitle(
                    if (snaps.size == 1) {
                        "You have 1 new snap"
                    } else {
                        "You have ${snaps.size} new snaps"
                    }
                )
                .setStyle(NotificationCompat.BigTextStyle().bigText("from ".plus(sendersText)))
                .setContentText("last received from ${snaps[0].getSender()}")
                .setNumber(senders.size)
                .setPriority(NotificationManager.IMPORTANCE_DEFAULT)
                .setWhen(snaps[0].getTime())
                .setAutoCancel(true)
                .setContentIntent(
                    PendingIntent.getActivity(
                        context,
                        0,
                        context.packageManager.getLaunchIntentForPackage("com.snapchat.android"),
                        0
                    )
                )

            notificationManager.notify("snap", 1, builder.build())
        }

        else
            Toast.makeText(context, "No snap recorded yet", Toast.LENGTH_SHORT).show()
    }
}