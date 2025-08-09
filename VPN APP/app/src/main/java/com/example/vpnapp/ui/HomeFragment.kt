package com.example.vpnapp.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.vpnapp.R
import com.example.vpnapp.data.ProfileStore
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
        
        // Add button handler
        v.findViewById<FloatingActionButton>(R.id.fabAdd).setOnClickListener {
            AddServerDialogFragment().show(parentFragmentManager, "add_server")
        }
        
        updateUI()
        return v
    }

    private fun startVpn() {
        val selected = ProfileStore.getSelected(requireContext())
        if (selected == null) {
            Snackbar.make(requireView(), "ابتدا یک سرور انتخاب کنید", Snackbar.LENGTH_LONG).show()
            return
        }
        
        val intent = Intent(requireContext(), XrayVpnService::class.java).apply {
            action = XrayVpnService.ACTION_START
            putExtra(XrayVpnService.EXTRA_LINK, selected.link)
        }
        requireContext().startService(intent)
    }

    private fun stopVpn() {
        try {
            val intent = Intent(requireContext(), XrayVpnService::class.java).apply {
                action = XrayVpnService.ACTION_STOP
            }
            requireContext().startService(intent)
        } catch (e: Exception) {
            // Handle gracefully
        }
    }

    private fun updateUI() {
        val selected = ProfileStore.getSelected(requireContext())
        if (selected != null) {
            txtServerInfo.text = selected.name
        } else {
            txtServerInfo.text = "سرور انتخاب نشده"
        }
    }

    override fun onResume() {
        super.onResume()
        updateUI()
    }
}


