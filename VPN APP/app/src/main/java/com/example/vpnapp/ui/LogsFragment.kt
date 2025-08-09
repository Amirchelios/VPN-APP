package com.example.vpnapp.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.vpnapp.R
import java.io.File

class LogsFragment : Fragment() {
    private lateinit var txtLogs: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val v = inflater.inflate(R.layout.fragment_logs, container, false)
        txtLogs = v.findViewById(R.id.txtLogs)
        v.findViewById<View>(R.id.btnCopy).setOnClickListener { copyLogs() }
        v.findViewById<View>(R.id.btnClear).setOnClickListener { clearLogs() }
        loadLogs()
        return v
    }

    override fun onResume() {
        super.onResume()
        loadLogs()
    }

    private fun coreDir(): File = File(requireContext().codeCacheDir, "xray")
    private fun logFile(): File = File(coreDir(), "xray.log")

    private fun loadLogs() {
        try {
            val content = if (logFile().exists()) logFile().readText() else "No logs yet"
            txtLogs.text = content
        } catch (_: Exception) {
            txtLogs.text = "Failed to load logs"
        }
    }

    private fun copyLogs() {
        try {
            val cm = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            cm.setPrimaryClip(ClipData.newPlainText("xray-log", txtLogs.text))
        } catch (_: Exception) { }
    }

    private fun clearLogs() {
        try {
            logFile().delete()
            loadLogs()
        } catch (_: Exception) { }
    }
}
