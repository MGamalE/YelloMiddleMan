package com.sharkawy.yellomiddleman.presentation

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView
import com.google.gson.Gson
import com.sharkawy.yellomiddleman.R
import com.sharkawy.yellomiddleman.entities.User
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.DataOutputStream
import java.net.ServerSocket
import java.net.Socket


class MainActivity : AppCompatActivity() {
    private val INTENT_ACTION = "com.sharkawy.yellomiddleman"

    private var receiver: MiddleManReceiver? = null
    private var user: User? = null

    private lateinit var progress: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onStart() {
        super.onStart()
        progress = ProgressDialog(this@MainActivity)
        progress.setTitle("Loading")
        progress.setMessage("Waiting...")
        progress.setCancelable(false)
        progress.show()

        Handler(Looper.getMainLooper()).postDelayed({
            progress.dismiss()
        }, 2000)
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

        checkIfBroadCastReceivedData()
    }

    private fun checkIfBroadCastReceivedData() {
        if (user != null) {
            showReceivedUser()
            sendReceivedUserWithClientServer()
        }
    }

    private fun sendReceivedUserWithClientServer() {
        GlobalScope.launch {
            try {
                val server = ServerSocket(8888)
                server.reuseAddress = true
                val serverClient: Socket = server.accept()
                val outStream = DataOutputStream(serverClient.getOutputStream())


                outStream.writeUTF(
                    Gson().toJson(User(user?.username, user?.phone))
                )
                outStream.flush()

                outStream.close()
                serverClient.close()
                server.close()

                Toast.makeText(
                    applicationContext,
                    "Server started and sent data to receiver!",
                    Toast.LENGTH_SHORT
                ).show()

            } catch (e: Exception) {
                Log.e("EXCEPTION", e.toString())
            }
        }

    }

    private fun registerBroadCastReceiver() {
        receiver = MiddleManReceiver()
        val intentFilter = IntentFilter(INTENT_ACTION)
        registerReceiver(receiver, intentFilter)
    }

    private fun showReceivedUser() {
        val dialogView = LayoutInflater.from(this@MainActivity).inflate(R.layout.dialog_user, null)
        val builder = AlertDialog.Builder(this@MainActivity)
            .setView(dialogView)
            .show()

        val confirmBtn = dialogView.findViewById<MaterialButton>(R.id.confirm_btn)
        val userPhone = dialogView.findViewById<MaterialTextView>(R.id.user_phone_tv)
        val userName = dialogView.findViewById<MaterialTextView>(R.id.user_name_tv)

        userPhone.text = user?.phone
        userName.text = user?.username

        confirmBtn.setOnClickListener {
            builder.dismiss()
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }

}