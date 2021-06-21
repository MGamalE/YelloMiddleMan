package com.sharkawy.yellomiddleman.presentation.core

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Build
import android.text.format.Formatter
import java.net.InetAddress
import java.net.NetworkInterface
import java.util.*


fun isMobileDataConnected(context: Context): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        cm.getNetworkCapabilities(cm.activeNetwork)
            ?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)!!
    } else {
        cm.activeNetworkInfo?.type == ConnectivityManager.TYPE_MOBILE
    }
}

fun getNetworkIp(context: Context): String? {
    return if (isMobileDataConnected(context))
        getDataIPAddress(true)
    else
        getWifiIPAddress(context)

}

fun getWifiIPAddress(context: Context): String? {
    val wm = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
    return Formatter.formatIpAddress(wm.connectionInfo.ipAddress)
}

fun getDataIPAddress(useIPv4: Boolean): String? {
    try {
        val interfaces: List<NetworkInterface> =
            Collections.list(NetworkInterface.getNetworkInterfaces())
        for (intf in interfaces) {
            val addrs: List<InetAddress> =
                Collections.list(intf.inetAddresses)
            for (addr in addrs) {
                if (!addr.isLoopbackAddress()) {
                    val sAddr: String = addr.getHostAddress()
                    val isIPv4 = sAddr.indexOf(':') < 0
                    if (useIPv4) {
                        if (isIPv4) return sAddr
                    } else {
                        if (!isIPv4) {
                            val delim = sAddr.indexOf('%')
                            return if (delim < 0) sAddr.toUpperCase() else sAddr.substring(
                                0,
                                delim
                            ).toUpperCase()
                        }
                    }
                }
            }
        }
    } catch (ignored: java.lang.Exception) {
    }
    return "localhost"
}
