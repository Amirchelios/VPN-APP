package com.example.vpnapp.ui

import android.app.Activity
import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.vpnapp.R
import com.example.vpnapp.service.XrayVpnService
import com.google.android.material.button.MaterialButton

class HomeFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val v = inflater.inflate(R.layout.fragment_home, container, false)
        v.findViewById<MaterialButton>(R.id.btnConnect).setOnClickListener {
            // Require a selected profile before requesting VPN permission
            val selected = com.example.vpnapp.data.ProfileStore.getSelected(requireContext())
            if (selected == null) {
                com.google.android.material.snackbar.Snackbar.make(v, "یک پروکسی انتخاب کنید", com.google.android.material.snackbar.Snackbar.LENGTH_SHORT).show()
            } else {
                requestVpnPermissionAndStart()
            }
        }
        v.findViewById<MaterialButton>(R.id.btnDisconnect).setOnClickListener { stopVpn() }
        return v
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


