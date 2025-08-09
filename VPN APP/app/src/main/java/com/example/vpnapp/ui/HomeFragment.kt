package com.example.vpnapp.ui

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.VpnService
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.vpnapp.R
import com.example.vpnapp.data.ProfileStore
import com.example.vpnapp.parser.LinkParser
import com.example.vpnapp.service.XrayVpnService
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar

class HomeFragment : Fragment() {
    private lateinit var imgStatus: ImageView
    private lateinit var txtStatus: TextView
    private lateinit var txtServerInfo: TextView
    private lateinit var txtPing: TextView
    private lateinit var txtUptime: TextView
    private lateinit var btnConnect: MaterialButton
    private lateinit var btnDisconnect: MaterialButton

    private var connected: Boolean = false

    private val statusReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == XrayVpnService.ACTION_STATUS) {
                connected = intent.getBooleanExtra(XrayVpnService.EXTRA_CONNECTED, false)
                applyStatus()
            }
        }
    }

    private val vpnPrepareLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            actuallyStartVpn()
        } else {
            Snackbar.make(requireView(), "مجوز VPN داده نشد", Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val v = inflater.inflate(R.layout.fragment_home, container, false)
        
        imgStatus = v.findViewById(R.id.imgStatus)
        txtStatus = v.findViewById(R.id.txtStatus)
        txtServerInfo = v.findViewById(R.id.txtServerInfo)
        txtPing = v.findViewById(R.id.txtPing)
        txtUptime = v.findViewById(R.id.txtUptime)
        btnConnect = v.findViewById(R.id.btnConnect)
        btnDisconnect = v.findViewById(R.id.btnDisconnect)
        
        btnConnect.setOnClickListener { startVpn() }
        btnDisconnect.setOnClickListener { stopVpn() }
        
        v.findViewById<FloatingActionButton>(R.id.fabAdd).setOnClickListener {
            try {
                AddServerDialogFragment().show(childFragmentManager, "add_server")
            } catch (e: Exception) {
                Snackbar.make(v, "خطا در نمایش فرم افزودن سرور", Snackbar.LENGTH_SHORT).show()
            }
        }
        
        updateUI()
        return v
    }

    override fun onResume() {
        super.onResume()
        requireContext().registerReceiver(statusReceiver, IntentFilter(XrayVpnService.ACTION_STATUS))
        updateUI()
    }

    override fun onPause() {
        super.onPause()
        try { requireContext().unregisterReceiver(statusReceiver) } catch (_: Exception) {}
    }

    private fun startVpn() {
        val selected = ProfileStore.getSelected(requireContext())
        if (selected == null) {
            Snackbar.make(requireView(), "ابتدا یک سرور انتخاب کنید", Snackbar.LENGTH_LONG).show()
            return
        }
        val parsed = LinkParser.parseLink(selected.link)
        if (parsed == null) {
            Snackbar.make(requireView(), "لینک نامعتبر است", Snackbar.LENGTH_LONG).show()
            return
        }
        val prepareIntent = VpnService.prepare(requireContext())
        if (prepareIntent != null) {
            vpnPrepareLauncher.launch(prepareIntent)
        } else {
            actuallyStartVpn()
        }
    }

    private fun actuallyStartVpn() {
        val selected = ProfileStore.getSelected(requireContext()) ?: return
        val intent = Intent(requireContext(), XrayVpnService::class.java).apply {
            action = XrayVpnService.ACTION_START
            putExtra(XrayVpnService.EXTRA_LINK, selected.link)
        }
        ContextCompat.startForegroundService(requireContext(), intent)
        txtStatus.text = "در حال اتصال..."
    }

    private fun stopVpn() {
        try {
            val intent = Intent(requireContext(), XrayVpnService::class.java).apply {
                action = XrayVpnService.ACTION_STOP
            }
            ContextCompat.startForegroundService(requireContext(), intent)
        } catch (e: Exception) { }
    }

    private fun updateUI() {
        val selected = ProfileStore.getSelected(requireContext())
        if (selected != null) {
            txtServerInfo.text = selected.name
        } else {
            txtServerInfo.text = "سرور انتخاب نشده"
        }
        applyStatus()
    }

    private fun applyStatus() {
        if (connected) {
            txtStatus.text = "متصل"
            imgStatus.setImageResource(android.R.drawable.presence_online)
        } else {
            txtStatus.text = "قطع شده"
            imgStatus.setImageResource(android.R.drawable.presence_offline)
        }
    }
}


