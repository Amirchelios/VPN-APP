package com.example.vpnapp

import android.app.Activity
import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.vpnapp.service.XrayVpnService
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class MainActivity : AppCompatActivity() {

    private lateinit var editLink: TextInputEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        editLink = findViewById(R.id.editLink)
        val btnImportStart = findViewById<MaterialButton>(R.id.btnImportStart)
        val btnStop = findViewById<MaterialButton>(R.id.btnStop)

        btnImportStart.setOnClickListener { requestVpnPermissionAndStart() }
        btnStop.setOnClickListener { stopVpn() }
    }

    private fun requestVpnPermissionAndStart() {
        val intent = VpnService.prepare(this)
        if (intent != null) {
            startActivityForResult(intent, REQ_VPN_PREPARE)
        } else {
            onActivityResult(REQ_VPN_PREPARE, Activity.RESULT_OK, null)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_VPN_PREPARE && resultCode == Activity.RESULT_OK) {
            val link = editLink.text?.toString()?.trim().orEmpty()
            val intent = Intent(this, XrayVpnService::class.java).apply {
                action = XrayVpnService.ACTION_START
                putExtra(XrayVpnService.EXTRA_LINK, link)
            }
            ContextCompat.startForegroundService(this, intent)
        }
    }

    private fun stopVpn() {
        val intent = Intent(this, XrayVpnService::class.java).apply {
            action = XrayVpnService.ACTION_STOP
        }
        ContextCompat.startForegroundService(this, intent)
    }

    companion object {
        private const val REQ_VPN_PREPARE = 100
    }
}


