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

class NotificationListener: NotificationListenerService() {

    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var filters: Array<Filter>

    override fun onCreate() {
        databaseHelper = DatabaseHelper(applicationContext)
        val mainHandler = Handler(Looper.getMainLooper())

        filters = emptyArray()
        filters = filters.plusElement(Filter(title = "from $1", text = ""))
        filters = filters.plusElement(Filter(title = "", text = "from $1"))
        filters = filters.plusElement(Filter(title = "$1", text = "sent a Snap"))

        mainHandler.post(object : Runnable{
            override fun run() {
                val events = (applicationContext.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager).queryEvents(System.currentTimeMillis() - 1000, System.currentTimeMillis())
                while (events.hasNextEvent()){
                    val event = UsageEvents.Event()
                    events.getNextEvent(event)
                    if(event.packageName == "com.snapchat.android" && event.eventType == UsageEvents.Event.ACTIVITY_RESUMED){
                        databaseHelper.removeAll()
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
        if(sbn.packageName == "com.snapchat.android") {
            val sender: String? = parseNotification(
                notificationTitle = sbn.notification.extras.getString(Notification.EXTRA_TITLE, ""),
                notificationText = sbn.notification.extras.getString(Notification.EXTRA_TEXT, "")
            )
            if(!sender.isNullOrBlank()){
                val snap = Snap(sbn.key.plus("|").plus(sbn.postTime), sender, sbn.postTime)
                databaseHelper.add(snap)
                super.cancelNotification(sbn.key)
                NotificationMaker().makeNotification(applicationContext)
            }
        }
    }

    private fun parseNotification(notificationTitle: String, notificationText: String):String? {
        var sender: String? = null
        for(filter in filters) {
            var title = filter.title
            var text = filter.text
            if(title.indexOf("$1") >= 0) {
                title = title.substring(0, title.indexOf("$1"))
                if(notificationTitle.startsWith(title) && notificationText.startsWith(text))
                    sender = notificationTitle.substring(filter.title.indexOf("$1"))
            }
            else if(text.indexOf("$1") >= 0) {
                text = text.substring(0, text.indexOf("$1"))
                if(notificationTitle.startsWith(title) && notificationText.startsWith(text))
                    sender = notificationTitle.substring(filter.text.indexOf("$1"))
            }
            if(!sender.isNullOrBlank()) break
        }
        return sender
    }
}