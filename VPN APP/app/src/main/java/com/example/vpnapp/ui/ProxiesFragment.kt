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
        adapter = ProxiesListAdapter()
        recycler.adapter = adapter
        
        v.findViewById<FloatingActionButton>(R.id.fabAdd).setOnClickListener {
            try {
                AddServerDialogFragment().show(childFragmentManager, "add_server")
            } catch (e: Exception) {
                android.util.Log.e("ProxiesFragment", "Error showing dialog", e)
            }
        }
        
        refreshUI()
        return v
    }

    private fun refreshUI() {
        try {
            adapter?.refresh(requireContext())
            val count = adapter?.itemCount ?: 0
            val hasItems = count > 0
            emptyState.visibility = if (hasItems) View.GONE else View.VISIBLE
            recycler.visibility = if (hasItems) View.VISIBLE else View.GONE
        } catch (e: Exception) {
            android.util.Log.e("ProxiesFragment", "Error refreshing UI", e)
            emptyState.visibility = View.VISIBLE
            recycler.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        refreshUI()
    }
}


