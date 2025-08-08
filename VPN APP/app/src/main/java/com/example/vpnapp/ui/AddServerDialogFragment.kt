package com.example.vpnapp.ui

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.vpnapp.R
import com.google.android.material.textfield.TextInputEditText

class AddServerDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val v = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_server, null, false)
        val edit = v.findViewById<TextInputEditText>(R.id.editLink)
        return AlertDialog.Builder(requireContext())
            .setTitle("افزودن سرور")
            .setView(v)
            .setPositiveButton("افزودن") { _, _ ->
                // TODO: save link to storage and update list
            }
            .setNegativeButton("انصراف", null)
            .create()
    }
}


