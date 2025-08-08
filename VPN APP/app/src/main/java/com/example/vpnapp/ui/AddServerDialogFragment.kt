package com.example.vpnapp.ui

import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.vpnapp.R
import com.example.vpnapp.data.ProfileStore
import com.example.vpnapp.model.VpnProfile
import com.example.vpnapp.parser.LinkParser
import com.google.android.material.textfield.TextInputEditText

class AddServerDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val v = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_server, null, false)
        val edit = v.findViewById<TextInputEditText>(R.id.editLink)
        val ctx = requireContext()
        return AlertDialog.Builder(ctx)
            .setTitle("افزودن سرور")
            .setView(v)
            .setPositiveButton("افزودن") { _, _ ->
                val link = edit.text?.toString()?.trim().orEmpty()
                val parsed: VpnProfile? = LinkParser.parseLink(link)
                ProfileStore.add(ctx, link, parsed)
            }
            .setNeutralButton("Paste از کلیپ‌بورد") { _, _ ->
                val cm = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val item: ClipData.Item? = cm.primaryClip?.getItemAt(0)
                val text = item?.text?.toString()?.trim().orEmpty()
                if (text.isNotBlank()) {
                    val parsed: VpnProfile? = LinkParser.parseLink(text)
                    ProfileStore.add(ctx, text, parsed)
                }
            }
            .setNegativeButton("انصراف", null)
            .create()
    }
}


