package com.sharkawy.yellomiddleman.presentation

import android.content.Context
import android.content.IntentFilter
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.sharkawy.yellomiddleman.R
import com.sharkawy.yellomiddleman.entities.User


class MainActivity : AppCompatActivity() {
    private val INTENT_ACTION = "com.sharkawy.yellomiddleman"

    private var receiver: MiddleManReceiver? = null
    private var user: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()
        registerBroadCastReceiver()
        listenForBroadCastReceivedData()
    }

    private fun listenForBroadCastReceivedData() {
        val prefs = this.getSharedPreferences(
            "YelloPref",
            Context.MODE_PRIVATE
        )
        val userPrefs = prefs.getString("User", null)
        user = Gson().fromJson(userPrefs, User::class.java)
    }

    private fun registerBroadCastReceiver() {
        receiver = MiddleManReceiver()
        val intentFilter = IntentFilter(INTENT_ACTION)
        registerReceiver(receiver, intentFilter)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }

}