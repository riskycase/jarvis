package com.riskycase.jarvis

import android.app.AlertDialog
import android.app.AppOpsManager
import android.app.NotificationManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar

class MainFragment : Fragment() {
    private lateinit var databaseHelper: DatabaseHelper

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main, container, false)

    }

    override fun onResume() {

        val view = requireView()

        val notificationListeners = Settings.Secure.getString(context?.contentResolver,"enabled_notification_listeners")
        if(notificationListeners!!.contains(requireContext().packageName)) {
            view.findViewById<TextView>(R.id.notification_access_text).setText(R.string.granted)
            view.findViewById<TextView>(R.id.notification_access_text).setTextColor(resources.getColor(R.color.green, null))
            view.findViewById<ImageView>(R.id.notification_access_icon).setImageResource(R.drawable.ic_check)
            view.findViewById<Button>(R.id.notification_access_button).setText(R.string.revoke_permission)
        }
        else {
            view.findViewById<TextView>(R.id.notification_access_text).setText(R.string.unavailable)
            view.findViewById<TextView>(R.id.notification_access_text).setTextColor(resources.getColor(R.color.red, null))
            view.findViewById<ImageView>(R.id.notification_access_icon).setImageResource(R.drawable.ic_error)
            view.findViewById<Button>(R.id.notification_access_button).setText(R.string.grant_perm)
        }

        val usageAccess = (requireContext().getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager).unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), requireContext().packageName)
        if(usageAccess == AppOpsManager.MODE_ALLOWED) {
            view.findViewById<TextView>(R.id.usage_status_text).setText(R.string.granted)
            view.findViewById<TextView>(R.id.usage_status_text).setTextColor(resources.getColor(R.color.green, null))
            view.findViewById<ImageView>(R.id.usage_status_icon).setImageResource(R.drawable.ic_check)
            view.findViewById<Button>(R.id.usage_status_button).setText(R.string.revoke_permission)
        }
        else {
            view.findViewById<TextView>(R.id.usage_status_text).setText(R.string.unavailable)
            view.findViewById<TextView>(R.id.usage_status_text).setTextColor(resources.getColor(R.color.red, null))
            view.findViewById<ImageView>(R.id.usage_status_icon).setImageResource(R.drawable.ic_error)
            view.findViewById<Button>(R.id.usage_status_button).setText(R.string.grant_perm)
        }

        super.onResume()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        databaseHelper = DatabaseHelper(view.context)

        view.findViewById<Button>(R.id.button_first).setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle("Flush current buffer?")
                .setMessage("Are you sure you want to cancel the current notification and clear the database?")
                .setPositiveButton("Yes") { _: DialogInterface, _: Int ->
                    databaseHelper.removeAllSnaps()
                    (context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                        .cancel("snap", 1)
                }
                .setNegativeButton("No") { _: DialogInterface, _: Int -> }
                .show()
        }

        view.findViewById<Button>(R.id.button_first).setOnLongClickListener { view ->
            Snackbar.make(view, "Clear generated notification", Snackbar.LENGTH_SHORT).show()
            return@setOnLongClickListener true
        }

        view.findViewById<Button>(R.id.notification_access_button).setOnClickListener {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            Toast.makeText(context, "Select ${getString(R.string.app_name)} and enable notification access for it", Toast.LENGTH_SHORT).show()
        }

        view.findViewById<Button>(R.id.usage_status_button).setOnClickListener {
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
            Toast.makeText(context, "Select ${getString(R.string.app_name)} and enable usage access for it", Toast.LENGTH_SHORT).show()
        }

    }
}