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
import android.util.Log

class NotificationListener: NotificationListenerService() {

    private lateinit var databaseHelper: DatabaseHelper

    override fun onCreate() {
        databaseHelper = DatabaseHelper(applicationContext)
        val mainHandler = Handler(Looper.getMainLooper())

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
            var sender: String? = null;
            if(sbn.notification.extras.getString(Notification.EXTRA_TITLE, "default value").substring(0, 5) == "from " ||
                    sbn.notification.extras.getString(Notification.EXTRA_TEXT, "default value").substring(0, 5) == "from ") {
                sender =
                    if (sbn.notification.extras.getString(Notification.EXTRA_TITLE, "default value").substring(0, 5) == "from ")
                        sbn.notification.extras.getString(Notification.EXTRA_TITLE, "default value").substring(5)
                    else
                        sbn.notification.extras.getString(Notification.EXTRA_TEXT, "default value").substring(5)

            }
            else if(sbn.notification.extras.getString(Notification.EXTRA_TEXT, "default value").substring(0,11) == "sent a Snap")
                sender = sbn.notification.extras.getString(Notification.EXTRA_TITLE, "default value")
            if(!sender.isNullOrBlank()){
                val snap = Snap(sbn.key.plus("|").plus(sbn.postTime), sender, sbn.postTime)
                databaseHelper.add(snap)
                super.cancelNotification(sbn.key)
                NotificationMaker().makeNotification(applicationContext)
            }
        }
    }
}