package com.sharkawy.yellomiddleman.presentation

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView
import com.google.gson.Gson
import com.sharkawy.yellomiddleman.R
import com.sharkawy.yellomiddleman.entities.User
import com.sharkawy.yellomiddleman.presentation.core.getNetworkIp
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.ServerSocket
import java.net.Socket


class MainActivity : AppCompatActivity() {
    private val INTENT_ACTION_MiddleMan = "com.sharkawy.yellomiddleman"
    private val INTENT_ACTION_Emitter = "com.sharkawy.yelloemitter"
    private val INTENT_KEY = "MiddleManUser"
    private val INTENT_COMPONENT_PACKAGE = "com.sharkawy.yelloemitter"
    private val INTENT_COMPONENT_PACKAGE_CLASS =
        "com.sharkawy.yelloemitter.presentation.core.EmitterReceiver"

    private var receiver: MiddleManReceiver? = null
    private var user: User? = null

    private lateinit var progress: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()
        showLoading()
    }

    private fun showLoading() {
        progress = ProgressDialog(this@MainActivity)
        progress.setTitle("Loading")
        progress.setMessage("Waiting...")
        progress.setCancelable(false)
        progress.show()

        Handler(Looper.getMainLooper()).postDelayed({
            progress.dismiss()
            registerBroadCastReceiver()
            listenForBroadCastReceivedData()
        }, 2000)
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
            writeToClientServer()
        }
    }

    private fun writeToClientServer() {
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


            } catch (e: Exception) {
                Log.e("EXCEPTION", e.toString())
            }
        }

        readFromClientServer()
        //743215398522000
    }

    private fun readFromClientServer() {
        GlobalScope.launch {
            try {
                Log.e("Client", " Create socket connection")
                val socket = Socket(getNetworkIp(this@MainActivity), 9999)
                val inStream = DataInputStream(socket.getInputStream())
                val serverMessage = inStream.readUTF()

                Handler(Looper.getMainLooper()).post {
                    Log.e("Client", " ${serverMessage}")
                    showReceivedStatus()
                    sendSavingStatusToEmitter(serverMessage)
                }

                inStream.close()
                socket.close()
            } catch (e: Exception) {
                Log.e("EXCEPTION", e.toString())
            }
        }

    }

    private fun registerBroadCastReceiver() {
        receiver = MiddleManReceiver()
        val intentFilter = IntentFilter(INTENT_ACTION_MiddleMan)
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

    private fun showReceivedStatus() {
        val dialogView =
            LayoutInflater.from(this@MainActivity).inflate(R.layout.dialog_receive_status, null)
        val builder = AlertDialog.Builder(this@MainActivity)
            .setView(dialogView)
            .show()

        val confirmBtn = dialogView.findViewById<MaterialButton>(R.id.confirm_btn)

        confirmBtn.setOnClickListener {
            builder.dismiss()
        }

    }

    private fun sendSavingStatusToEmitter(status: String) {
        val intent = Intent()
        intent.action = INTENT_ACTION_Emitter
        intent.putExtra(INTENT_KEY, status)
        intent.component =
            ComponentName(
                INTENT_COMPONENT_PACKAGE,
                INTENT_COMPONENT_PACKAGE_CLASS
            )
        sendBroadcast(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }

}