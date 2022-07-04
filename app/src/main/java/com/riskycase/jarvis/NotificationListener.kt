package com.riskycase.jarvis

import android.app.Notification
import android.app.NotificationManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.widget.Toast

class NotificationListener : NotificationListenerService() {

    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var filters: Array<Filter>

    override fun onCreate() {
        databaseHelper = DatabaseHelper(applicationContext)
        val mainHandler = Handler(Looper.getMainLooper())
        filters = databaseHelper.getFilters()

        mainHandler.post(object : Runnable {
            override fun run() {
                val events =
                    (applicationContext.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager).queryEvents(
                        System.currentTimeMillis() - 1000,
                        System.currentTimeMillis()
                    )
                while (events.hasNextEvent()) {
                    val event = UsageEvents.Event()
                    events.getNextEvent(event)
                    if (event.packageName == "com.snapchat.android" && event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                        databaseHelper.removeAllSnaps()
                        (applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                            .cancel("snap", 1)
                    }
                }
                mainHandler.postDelayed(this, 500)
            }
        })

        super.onCreate()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (sbn.packageName == "com.snapchat.android") {
            val sender: String? = parseNotification(
                notificationTitle = sbn.notification.extras.getString(Notification.EXTRA_TITLE, ""),
                notificationText = sbn.notification.extras.getString(Notification.EXTRA_TEXT, "")
            )
            if (!sender.isNullOrBlank()) {
                val snap = Snap(sbn.key.plus("|").plus(sbn.postTime), sender, sbn.postTime)
                databaseHelper.addSnap(snap)
                super.cancelNotification(sbn.key)
                NotificationMaker().makeNotification(applicationContext)
            }
        }
    }

    private fun parseNotification(notificationTitle: String, notificationText: String): String? {
        var sender: String? = null
        filters.forEach { filter ->
            if (filter.title.type == Match.MatchType.EXTRACT) {
                if ((filter.text.type == Match.MatchType.EXACT && filter.text.string == notificationText) || (filter.text.type == Match.MatchType.CONTAINS && notificationText.contains(
                        filter.text.string
                    ))
                )
                    if (Regex(filter.title.string).containsMatchIn(notificationTitle))
                        sender = Regex(filter.title.string).find(notificationTitle)?.value
            } else if (filter.text.type == Match.MatchType.EXTRACT) {
                if ((filter.title.type == Match.MatchType.EXACT && filter.title.string == notificationTitle) || (filter.title.type == Match.MatchType.CONTAINS && notificationTitle.contains(
                        filter.text.string
                    ))
                )
                    if (Regex(filter.title.string).containsMatchIn(notificationText))
                        sender = Regex(filter.text.string).find(notificationText)?.value
            }
        }
        return sender
    }
}