package com.example.vpnapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.vpnapp.R
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ProxiesFragment : Fragment() {
    private var adapter: ProxiesListAdapter? = null
    private lateinit var emptyState: View
    private lateinit var recycler: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val v = inflater.inflate(R.layout.fragment_proxies, container, false)
        
        emptyState = v.findViewById(R.id.emptyState)
        recycler = v.findViewById(R.id.recycler)
        
        recycler.layoutManager = LinearLayoutManager(requireContext())
        adapter = ProxiesListAdapter { refreshUI() }
        recycler.adapter = adapter
        
        v.findViewById<FloatingActionButton>(R.id.fabAdd).setOnClickListener {
            AddServerDialogFragment().show(parentFragmentManager, "add_server")
        }
        
        refreshUI()
        return v
    }

    private fun refreshUI() {
        adapter?.refresh(requireContext())
        val hasItems = adapter?.itemCount ?: 0 > 0
        emptyState.visibility = if (hasItems) View.GONE else View.VISIBLE
        recycler.visibility = if (hasItems) View.VISIBLE else View.GONE
    }

    override fun onResume() {
        super.onResume()
        refreshUI()
    }
}


