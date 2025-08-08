package com.example.vpnapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.vpnapp.R

class ProxiesFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val v = inflater.inflate(R.layout.fragment_proxies, container, false)
        val rv = v.findViewById<RecyclerView>(R.id.recycler)
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = ProxiesListAdapter()
        return v
    }
}


