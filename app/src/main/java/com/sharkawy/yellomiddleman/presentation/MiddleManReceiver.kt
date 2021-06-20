package com.sharkawy.yellomiddleman.presentation

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences.Editor


class MiddleManReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent != null && intent.hasExtra("EmitterUser")) {
            val user = intent.getStringExtra("EmitterUser")
            val prefs = context!!.getSharedPreferences(
                "YelloPref",
                MODE_PRIVATE
            )
            val editor: Editor = prefs.edit()
            editor.putString("User", user)
            editor.apply()
        }

    }
}