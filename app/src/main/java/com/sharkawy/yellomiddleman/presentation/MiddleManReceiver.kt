package com.sharkawy.yellomiddleman.presentation

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast

class MiddleManReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if(intent != null && intent.hasExtra("Yello")){
            val data = intent.getStringExtra("Yello")
            Log.i("BR", "Data received:  $data")
            Toast.makeText(context,"$data", Toast.LENGTH_LONG).show()
        }

    }
}