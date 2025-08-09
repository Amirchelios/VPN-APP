// This file name is created to match the class after rename for clarity.
package com.example.vpnapp.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import androidx.core.app.NotificationCompat
import com.example.vpnapp.R
import com.example.vpnapp.core.CoreManager
import com.example.vpnapp.parser.LinkParser

class XrayVpnService : VpnService() {

    private var vpnInterface: ParcelFileDescriptor? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            when (intent?.action) {
                ACTION_START -> {
                    startForeground(NOTIFICATION_ID, createNotification())
                    val link = intent.getStringExtra(EXTRA_LINK).orEmpty()
                    startVpn(link)
                }
                ACTION_STOP -> {
                    stopVpn()
                    try { stopForeground(STOP_FOREGROUND_REMOVE) } catch (_: Throwable) {}
                    sendStatus(false)
                    stopSelf()
                }
                else -> return START_NOT_STICKY
            }
        } catch (_: Throwable) {
            try { stopForeground(STOP_FOREGROUND_REMOVE) } catch (_: Throwable) {}
            sendStatus(false)
            stopSelf()
            return START_NOT_STICKY
        }
        return START_STICKY
    }

    private fun startVpn(link: String) {
        try {
            val selected = com.example.vpnapp.data.ProfileStore.getSelected(applicationContext)
            val profile = if (link.isNotBlank()) LinkParser.parseLink(link) else selected?.let { LinkParser.parseLink(it.link) }
            if (profile == null) {
                android.util.Log.e("XrayVpnService", "No valid profile found")
                sendStatus(false)
                stopSelf()
                return
            }
            
            CoreManager.ensureCorePrepared(applicationContext)

            val builder = Builder()
            builder.setSession(profile.name)
            builder.addAddress("10.0.0.2", 24)
            builder.addDnsServer("1.1.1.1")
            builder.addDnsServer("8.8.8.8")
            builder.addRoute("0.0.0.0", 0)

            vpnInterface = builder.establish()

            val ok = CoreManager.startCoreWithProfile(applicationContext, profile)
            if (!ok) {
                android.util.Log.e("XrayVpnService", "Failed to start xray core")
                stopVpn()
                sendStatus(false)
                stopSelf()
                return
            }
            val fd = vpnInterface?.fd
            if (fd != null && fd > 0) {
                try { CoreManager.tryStartTun2Socks(applicationContext, fd) } catch (_: Throwable) {}
            }
            sendStatus(true)
        } catch (e: Throwable) {
            android.util.Log.e("XrayVpnService", "Error establishing VPN", e)
            stopVpn()
            sendStatus(false)
            stopSelf()
        }
    }

    private fun stopVpn() {
        CoreManager.stopCore()
        try { vpnInterface?.close() } catch (_: Exception) {}
        vpnInterface = null
    }

    private fun sendStatus(connected: Boolean) {
        try {
            val intent = Intent(ACTION_STATUS).apply { putExtra(EXTRA_CONNECTED, connected) }
            sendBroadcast(intent)
        } catch (_: Throwable) { }
    }

    private fun createNotification(): Notification {
        val channelId = ensureNotificationChannel()
        return NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.notification_running))
            .setOngoing(true)
            .build()
    }

    private fun ensureNotificationChannel(): String {
        val channelId = "vpn_service_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                channelId,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notification_channel_desc)
            }
            manager.createNotificationChannel(channel)
        }
        return channelId
    }

    override fun onDestroy() {
        stopVpn()
        super.onDestroy()
    }

    companion object {
        const val ACTION_START = "com.example.vpnapp.START_VPN"
        const val ACTION_STOP = "com.example.vpnapp.STOP_VPN"
        const val EXTRA_LINK = "link"
        const val ACTION_STATUS = "com.example.vpnapp.VPN_STATUS"
        const val EXTRA_CONNECTED = "connected"
        private const val NOTIFICATION_ID = 1
    }
}