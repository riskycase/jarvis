package com.riskycase.jarvis

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener {
            NotificationMaker().makeNotification(applicationContext)
        }

        findViewById<FloatingActionButton>(R.id.fab).setOnLongClickListener { view ->
            Snackbar.make(view, "Refresh displayed notification", Snackbar.LENGTH_LONG).show()
            return@setOnLongClickListener true
        }
    }
}