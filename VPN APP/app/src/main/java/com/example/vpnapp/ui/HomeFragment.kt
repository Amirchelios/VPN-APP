package com.example.vpnapp.ui

import android.app.Activity
import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.vpnapp.R
import com.example.vpnapp.service.XrayVpnService
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar

class HomeFragment : Fragment() {
    private lateinit var txtStatus: TextView
    private lateinit var txtServerInfo: TextView
    private lateinit var txtPing: TextView
    private lateinit var txtUptime: TextView
    private lateinit var imgStatus: ImageView
    private lateinit var btnConnect: MaterialButton
    private lateinit var btnDisconnect: MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val v = inflater.inflate(R.layout.fragment_home, container, false)
        
        initializeViews(v)
        setupClickListeners(v)
        updateUI()
        
        return v
    }

    private fun initializeViews(v: View) {
        txtStatus = v.findViewById(R.id.txtStatus)
        txtServerInfo = v.findViewById(R.id.txtServerInfo)
        txtPing = v.findViewById(R.id.txtPing)
        txtUptime = v.findViewById(R.id.txtUptime)
        imgStatus = v.findViewById(R.id.imgStatus)
        btnConnect = v.findViewById(R.id.btnConnect)
        btnDisconnect = v.findViewById(R.id.btnDisconnect)
    }

    private fun setupClickListeners(v: View) {
        btnConnect.setOnClickListener {
            val selected = com.example.vpnapp.data.ProfileStore.getSelected(requireContext())
            if (selected == null) {
                Snackbar.make(v, "یک پروکسی انتخاب کنید", Snackbar.LENGTH_SHORT).show()
            } else {
                requestVpnPermissionAndStart()
            }
        }
        btnDisconnect.setOnClickListener { 
            try {
                stopVpn()
            } catch (e: Exception) {
                Snackbar.make(v, "خطا در قطع اتصال", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateUI() {
        val selected = com.example.vpnapp.data.ProfileStore.getSelected(requireContext())
        if (selected != null) {
            txtServerInfo.text = selected.name
        } else {
            txtServerInfo.text = "سرور انتخاب نشده"
        }
        
        // Default values
        txtStatus.text = "قطع شده"
        txtPing.text = "-- ms"
        txtUptime.text = "00:00:00"
        imgStatus.setImageResource(android.R.drawable.presence_offline)
    }

    override fun onResume() {
        super.onResume()
        updateUI()
    }

    private fun requestVpnPermissionAndStart() {
        val act = requireActivity()
        val intent = VpnService.prepare(act)
        if (intent != null) {
            startActivityForResult(intent, REQ_VPN_PREPARE)
        } else {
            onActivityResult(REQ_VPN_PREPARE, Activity.RESULT_OK, null)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_VPN_PREPARE && resultCode == Activity.RESULT_OK) {
            val act = requireActivity()
            val intent = Intent(act, XrayVpnService::class.java).apply {
                action = XrayVpnService.ACTION_START
                putExtra(XrayVpnService.EXTRA_LINK, "")
            }
            ContextCompat.startForegroundService(act, intent)
        }
    }

    private fun stopVpn() {
        val act = requireActivity()
        val intent = Intent(act, XrayVpnService::class.java).apply {
            action = XrayVpnService.ACTION_STOP
        }
        ContextCompat.startForegroundService(act, intent)
    }

    companion object { private const val REQ_VPN_PREPARE = 200 }
}


